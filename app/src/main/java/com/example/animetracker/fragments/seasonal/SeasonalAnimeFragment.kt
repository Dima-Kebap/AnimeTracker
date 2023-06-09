package com.example.animetracker.fragments.seasonal

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.animetracker.*
import com.example.animetracker.adapters.AnimeAdapter
import com.example.animetracker.adapters.AnimeModel
import com.example.animetracker.databinding.FragmentSeasonalAnimeBinding
import java.io.Serializable

private const val ARG_PARAM1 = "year"
private const val ARG_PARAM2 = "season"
private const val ARG_PARAM3 = "season_list"
private const val ARG_PARAM4 = "butBack"
private const val REQUEST_CODE = 123456
class SeasonalAnimeFragment : Fragment(), AnimeAdapter.Listener {

    private lateinit var binding: FragmentSeasonalAnimeBinding
    private  var year: String? = null
    private  var season : String? = null
    private lateinit var adapter: AnimeAdapter
    private val model: MainViewModel by activityViewModels()
    private var isLoading = false
    private  var seasonList: String? = null
    private var butBackIsEnabled: Boolean = false
    private var sort = ""//критерій сортування
    private var sortOrder = true//порядок сортування false-по возростанію, true - по спаданню
    private lateinit var previousSort:String
    private var previousMenuItem: MenuItem? = null


    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        model.seasonDataList[seasonList]?.observe(viewLifecycleOwner) {
            adapter.submitList(it.subList(0, it.size))
        }
        searchAnimeBySeason(0)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSeasonalAnimeBinding.inflate(inflater, container, false)
        arguments?.let {
            year = it.getString(ARG_PARAM1)
            season = it.getString(ARG_PARAM2)
            seasonList = it.getString(ARG_PARAM3)
            butBackIsEnabled = it.getBoolean(ARG_PARAM4)
        }
        return binding.root
    }


    private fun init() = with(binding) {

        info.text = "$year $season"
        if(butBackIsEnabled)
            butBack.visibility = View.VISIBLE
        else
            butBack.visibility = View.INVISIBLE

        swipeRefreshLayout.setOnRefreshListener {
            previousMenuItem?.title = previousSort
            previousMenuItem = null
            sort=""
            searchAnimeBySeason(0)
        }
        butBack.setOnClickListener {
            parentFragmentManager.popBackStack("SeasonsList", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
        val layoutManager = LinearLayoutManager(activity)
        rcView2.layoutManager = layoutManager
        adapter = AnimeAdapter(this@SeasonalAnimeFragment)
        rcView2.adapter = adapter

        rcView2.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                if (!isLoading && firstVisibleItemPosition + visibleItemCount >= totalItemCount && model.seasonDataList[seasonList]?.value?.size!! >= 20) {
                    isLoading = true
                    searchAnimeBySeason( totalItemCount)
                }
            }
        })

        val popupMenu = PopupMenu(ContextThemeWrapper(context,R.style.PopupMenuStyle),menu)
        popupMenu.menuInflater.inflate(R.menu.sort, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item -> // Обробка натискання на елемент меню
            sortList(item)
            true
        }
        // Показ випадаючого меню при натисканні на кнопку
        menu.setOnClickListener { popupMenu.show() }

    }


    private fun searchAnimeBySeason(offset: Int) {
        var url = "https://kitsu.io/api/edge/anime?page[limit]=20&page[offset]=$offset&filter[seasonYear]=$year&filter[season]=${season?.lowercase()}&sort=-averageRating"
        if (offset == 0)
            model.seasonDataList[seasonList]?.value = ArrayList()
        if(sort!="")
            url += if(sortOrder) "&sort=-$sort" else "&sort=$sort"
        searchAnime(url)
    }


    @SuppressLint("SetTextI18n")
    private fun searchAnime(url: String) {
        val queue = Volley.newRequestQueue(context)
        val request = StringRequest(
            Request.Method.GET,
            url,
            { result ->
                model.seasonDataList[seasonList]?.value = model.seasonDataList[seasonList]?.value?.plus(RequestParse().parseAnime(result))
               binding. swipeRefreshLayout.isRefreshing = false
                isLoading = false
                if (model.seasonDataList[seasonList]?.value!!.isEmpty())
                    Toast.makeText(activity, "NO RESULTS", Toast.LENGTH_LONG).show()
            },
            { error -> Log.d("MyLog", "Error: $error")
                Toast.makeText(activity, "timeout", Toast.LENGTH_SHORT).show()}
        )
        queue.add(request)
    }

    override fun onButtonEditClicked(item: AnimeModel, position: Int) {
        val intent = Intent(requireContext(), EditActivity::class.java)
            .apply {
                putExtra("anime_id", item.id)
                putExtra("anime_title", item.title)
                putExtra("anime_ep_count", item.episodeCount)
                putExtra("position", position)
            }
        startActivityForResult(intent, REQUEST_CODE)//для оновленя гістограми оцінок(якщо користувач поставив іншу оцінку, то вона виділиться)
    }

    override fun onClick(item: AnimeModel, position: Int) {//при натискані на якесь аніме відбувається перехід активність з детальною інформацією про нього
        val intent = Intent(requireContext(), DetailsActivity::class.java).apply {
            putExtra("anime", item.id)
            putExtra("position", position)
        }
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode!=-1)
            binding.rcView2.adapter?.notifyItemChanged(resultCode)
    }


    private fun sortList(item: MenuItem){
        when (item.itemId) {
            R.id.sort_by_rait -> {
                if(sort!="averageRating") {
                    previousMenuItem?.title = previousSort
                    previousMenuItem = item
                    previousSort = "⭐️"
                    sortOrder=true
                }else
                    sortOrder=!sortOrder
                sort="averageRating"
            }
            R.id.sort_by_userCount -> {
                if(sort!="userCount") {
                    sortOrder=true
                    previousMenuItem?.title = previousSort
                    previousMenuItem = item
                    previousSort = getString(R.string.total_users)
                }else
                    sortOrder=!sortOrder
                sort="userCount"
            }
            R.id.sort_by_totalEpisodes -> {
                if(sort!="episodeCount") {
                    sortOrder=true
                    previousMenuItem?.title = previousSort
                    previousMenuItem = item
                    previousSort = getString(R.string.total_episodes)
                }else
                    sortOrder=!sortOrder
                sort="episodeCount"
            }
            R.id.sort_by_startDate -> {
                if(sort!="startDate") {
                    sortOrder=true
                    previousMenuItem?.title = previousSort
                    previousMenuItem = item
                    previousSort = getString(R.string.start_date)
                }else
                    sortOrder=!sortOrder
                sort="startDate"
            }
        }
        if (sortOrder)
            item.title ="↓$previousSort↓"
        else
            item.title ="↑$previousSort↑"
        searchAnimeBySeason(0)
    }

    companion object {
        @JvmStatic
        fun newInstance(year: String, season: String, tab: String, butBack: Boolean) = SeasonalAnimeFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, year)
                putString(ARG_PARAM2, season)
                putString(ARG_PARAM3, tab)
                putBoolean(ARG_PARAM4, butBack)
            }
        }
    }
}