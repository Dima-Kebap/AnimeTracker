package com.example.animetracker.fragments.myLists

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.animetracker.DetailsActivity
import com.example.animetracker.EditActivity
import com.example.animetracker.dataBase.MainDb
import com.example.animetracker.MainViewModel
import com.example.animetracker.R
import com.example.animetracker.RequestParse
import com.example.animetracker.adapters.AnimeAdapter
import com.example.animetracker.adapters.AnimeModel
import com.example.animetracker.databinding.FragmentListBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Serializable
import java.util.Arrays
import kotlin.math.round


private const val ARG_PARAM1 = "list_name"
private const val REQUEST_CODE = 12345

class ListFragment : Fragment(), AnimeAdapter.Listener {
    private lateinit var listName: String//назва списку
    private lateinit var binding: FragmentListBinding
    private lateinit var adapter: AnimeAdapter
    private val model: MainViewModel by activityViewModels()
    private  var thisList: List<Int> = ArrayList()//айді аніме, які входять в цей список
    private var isLoading = false//пауза між запитами до апі
    private var sort = ""//критерій сортування
    private var sortOrder = true//порядок сортування: true - по спаданню, false-по зростанню
    private lateinit var previousSort:String
    private var previousMenuItem: MenuItem? = null
    private lateinit var db: MainDb

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentListBinding.inflate(inflater, container, false)
        arguments?.let {
                listName = it.getString(ARG_PARAM1).toString()
        }
        db = MainDb.getDb(requireActivity())
        return binding.root
    }

    override fun onResume() {//оновити всі списки в яких фігурує змінене аніме
        super.onResume()
        binding.butListInfo.text = "${thisList.size} Entres"
        Thread{
            if(thisList!=getAnimeList())
                refreshList()
        }.start()

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        model.listsDataList[listName]?.observe(viewLifecycleOwner) {
            adapter.submitList(it.subList(0, it.size))
        }
    }


    private fun init() = with(binding) {
        val popupMenu = PopupMenu(ContextThemeWrapper(context,R.style.PopupMenuStyle),menu)
        popupMenu.menuInflater.inflate(R.menu.sort, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item -> // Обробка натискання на елемент меню
            sortList(item)
            true
        }
        butListInfo.setOnClickListener {
            butListInfo()
        }
        swipeRefreshLayout.setOnRefreshListener { refreshList() }

        // Показ випадаючого меню при натисканні на кнопку
        menu.setOnClickListener { popupMenu.show() }
        val layoutManager = LinearLayoutManager(activity)
        rcView2.layoutManager = layoutManager
        adapter = AnimeAdapter(this@ListFragment)
        rcView2.adapter = adapter
        rcView2.isVerticalScrollBarEnabled = true
        rcView2.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                if (!isLoading && firstVisibleItemPosition + visibleItemCount >= totalItemCount && totalItemCount < thisList.size) {
                    isLoading = true
                    searchAnimeList(totalItemCount)
                }
            }
        })
    }


    private fun searchAnimeList(offset: Int) {
        //додається listOf("0") бо якщо передавати масив id у запит до апі, то перший результат неповертає
        var url = "https://kitsu.io/api/edge/anime?page[limit]=20&page[offset]=$offset=&filter[id]=${listOf("0") + thisList}"
        if (offset == 0)//якщо немає зміщення, то шукається по новому
            model.listsDataList[listName]?.value = ArrayList()
        if(sort!="")
            url += if(sortOrder) "&sort=-$sort" else "&sort=$sort"
        searchAnime(url)
    }

    private fun searchAnime(url: String) {//запит до апі
        val queue = Volley.newRequestQueue(context)
        val request = StringRequest(
            Request.Method.GET,
            url,
            { result -> model.listsDataList[listName]?.value = model.listsDataList[listName]?.value?.plus(RequestParse().parseAnime(result))
                isLoading = false
              binding.swipeRefreshLayout.isRefreshing = false

            },
            { error -> Log.d("MyLog", "Error: $error")
                Toast.makeText(activity, "timeout", Toast.LENGTH_SHORT).show()}
        )
        queue.add(request)
    }


    override fun onClick(item: AnimeModel, position: Int) {//при натискані на якесь аніме відбувається перехід активність з детальною інформацією про нього
        val intent = Intent(requireContext(), DetailsActivity::class.java).apply {
            putExtra("anime", item.id)
            putExtra("position", position)}
        startActivityForResult(intent, REQUEST_CODE)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode!=-1)
                binding.rcView2.adapter?.notifyItemChanged(resultCode)
    }

    private fun refreshList() {//оновленя сторінки
        CoroutineScope(Dispatchers.IO).launch {
            thisList=getAnimeList()
            sort=""//спадає сортування
            previousMenuItem?.title = previousSort
            previousMenuItem = null
            withContext(Dispatchers.Main) {
                binding.butListInfo.text = "${thisList.size} Entres"
                searchAnimeList(0)
            }
        }

    }


    private fun getAnimeList():List<Int>{
        val list:MutableList<Int> = ArrayList()
        if(listName!=getString(R.string.All_list)){
            db.getDao().getByListName(listName).forEach { list.add(it.id) }
        }else
            db.getDao().getAll().forEach { list.add(it.id) }
        //щоб після зміни даних про аніме та перезаходжені на цю вкладку воно не оновлювалось
        //(треба лише самостійно оновити вкладку all)
        return  list
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
                    previousSort = "\uD83D\uDC64"
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
        searchAnimeList(0)
    }

    companion object {
        @JvmStatic
        fun newInstance(list_name: String) = ListFragment().apply {
            arguments = Bundle().apply { putString(ARG_PARAM1, list_name) }
        }
    }

    private fun butListInfo() {
        var meanRating = 0.0
        var allEpisodesCount = 0
        CoroutineScope(Dispatchers.IO).launch {
            if(thisList.isNotEmpty()){
                val listRatings= ArrayList<Int>()
                val listEpisodes= ArrayList<Int>()
                for(i in thisList){
                    val anime = db.getDao().getById(i)
                    listEpisodes.add(anime.episodes)
                    if(anime.rating!=0)
                        listRatings.add(anime.rating)
                }
                meanRating = round(listRatings.average()*100)/100
                allEpisodesCount = listEpisodes.sum()
            }
            withContext(Dispatchers.Main) {
                val dialogMessage = SpannableString("Info\n\nEntres: ${thisList.size}\nYour mean ⭐️:$meanRating\nWatched episodes: $allEpisodesCount")
                dialogMessage.setSpan(ForegroundColorSpan(Color.RED), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                dialogMessage.setSpan(AbsoluteSizeSpan(20, true), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                val dialogLayout = LayoutInflater.from(context).inflate(R.layout.listinfo_layout, null)
                dialogLayout.findViewById<TextView>(R.id.dialog_message).text = dialogMessage
                val alertDialog = AlertDialog.Builder(context).setView(dialogLayout).create()
                alertDialog.show()
                dialogLayout.setOnClickListener { alertDialog.dismiss() }
            }
        }
    }

}

