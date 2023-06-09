package com.example.animetracker.fragments

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
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
import com.example.animetracker.dataBase.MainDb
import com.example.animetracker.databinding.FragmentSearchBinding
import java.io.Serializable

private const val REQUEST_CODE = 12345

class SearchFragment : Fragment(), AnimeAdapter.Listener {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var adapter: AnimeAdapter
    private val model: MainViewModel by activityViewModels()
    private var isLoading = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        val networkInfo = (requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
        if (networkInfo == null || !networkInfo.isConnected)
            Toast.makeText(context, getString(R.string.no_internet), Toast.LENGTH_LONG).show()
        init()
        model.searchDataList.observe(viewLifecycleOwner) {
            adapter.submitList(it.subList(0, it.size))
        }
        if(model.searchDataList.value==null){//якщо список пошуку порожній(лише увімкнуто додаток)
            model.searchDataList.value = ArrayList()
            searchAnime("https://kitsu.io/api/edge/anime?page[limit]=20&sort=popularityRank")
        }
        return binding.root
    }


    private fun searchAnimeByTitle(title: String, offset: Int) {//пошук аніме за назвою,offset-зміщення результату
        val url = "https://kitsu.io/api/edge/anime?page[limit]=20&page[offset]=$offset&filter[text]=$title"
        searchAnime(url)
    }


    private fun searchAnime(url: String) {//запит до апі
        val queue = Volley.newRequestQueue(context)
        val request = StringRequest(
            Request.Method.GET,
            url,
            { result ->
                model.searchDataList.value = model.searchDataList.value?.plus(RequestParse().parseAnime(result))
                binding.swipeRefreshLayout.isRefreshing = false
                isLoading = false
                if (model.searchDataList.value!!.isEmpty())
                    Toast.makeText(activity, "NO RESULTS", Toast.LENGTH_LONG).show()
            },
            { error -> Log.d("MyLog", "Error: $error")
                Toast.makeText(activity, "timeout", Toast.LENGTH_SHORT).show()}
        )
        queue.add(request)
    }


    private fun init() = with(binding) {
        searchAnime.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                val search = searchAnime.text.toString().replace("\\s+".toRegex(), "%20")
                model.searchDataList.value = ArrayList()
                if (search != "")
                    searchAnimeByTitle(search, 0)
                else
                    searchAnimeByTitle("Attack%20on%20Titan", 0)
                return@OnKeyListener true
            }
            false
        })
        val layoutManager = LinearLayoutManager(activity)
        rcView.layoutManager = layoutManager
        adapter = AnimeAdapter(this@SearchFragment)
        rcView.adapter = adapter


        swipeRefreshLayout.setOnRefreshListener {
            val search = searchAnime.text.toString().replace("\\s+".toRegex(), "%20")
            model.searchDataList.value = ArrayList()
            if (search != "")
                searchAnimeByTitle(search, 0)
            else
                searchAnime("https://kitsu.io/api/edge/anime?page[limit]=20&sort=popularityRank")
        }
        rcView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                if (!isLoading && firstVisibleItemPosition + visibleItemCount >= totalItemCount) {
                    isLoading = true
                    val search: String = binding.searchAnime.text.toString()
                    if (search != "")
                        searchAnimeByTitle(search.replace("\\s+".toRegex(), "%20"), totalItemCount)
                    else
                        searchAnime("https://kitsu.io/api/edge/anime?page[limit]=20&page[offset]=$totalItemCount&sort=popularityRank")
                }
            }
        })

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

    override fun onClick(item: AnimeModel,position:Int) {//при натискані на якесь аніме відбувається перехід активність з детальною інформацією про нього
        val intent = Intent(requireContext(), DetailsActivity::class.java).apply {
            putExtra("anime", item.id)
            putExtra("position", position)
        }
        //для оновленя сторінки(якщо список змінвся чи ще щось)
        startActivityForResult(intent, REQUEST_CODE)


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode!=-1)
            binding.rcView.adapter?.notifyItemChanged(resultCode)
    }

}