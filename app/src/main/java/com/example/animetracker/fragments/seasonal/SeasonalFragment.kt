package com.example.animetracker.fragments.seasonal

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.animetracker.R
import com.example.animetracker.adapters.VpAdapter
import com.example.animetracker.databinding.FragmentSeasonalBinding
import com.google.android.material.tabs.TabLayoutMediator
import java.util.Calendar


class SeasonalFragment : Fragment() {

    private lateinit var binding: FragmentSeasonalBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSeasonalBinding.inflate(inflater, container, false)
        val networkInfo = (requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
        if (networkInfo == null || !networkInfo.isConnected)
            Toast.makeText(context, getString(R.string.no_internet), Toast.LENGTH_LONG).show()
        init()
        return binding.root
    }

    private fun init() = with(binding) {
        val month = Calendar.getInstance().get(Calendar.MONTH) + 1 // додаємо 1, оскільки значення місяця починається з 0
        val thisSeason = when(month){
            in 3..5-> "Spring"
            in 6..8-> "Summer"
            in 9..11-> "Fall"
            else-> "Winter"
        }
        val previousSeason = when(thisSeason){
            "Spring"->"Winter"
            "Summer"->"Spring"
            "Fall"->"Summer"
            else->"Fall"
        }
        val nextSeason = when(thisSeason){
            "Spring"->"Summer"
            "Summer"->"Fall"
            "Fall"->"Winter"
            else->"Spring"
        }

        var thisYear = Calendar.getInstance().get(Calendar.YEAR)
        thisYear = if(month==12) thisYear+1 else thisYear
        val previousYear = if(thisSeason=="Winter") thisYear-1 else thisYear
        val nextYear = if(thisSeason=="Fall") thisYear+1 else thisYear


        val fList: List<Fragment> = listOf(//список фрагментів(сезонів)
            SeasonalAnimeFragment.newInstance(previousYear.toString(),previousSeason,"Previous",false),
            SeasonalAnimeFragment.newInstance(thisYear.toString(),thisSeason,"This",false),
            SeasonalAnimeFragment.newInstance(nextYear.toString(),nextSeason,"Next",false),
            CustomSeasonFragment()
        )
        val tList = listOf(//назви фрагментів
            "Previous",
            "This",
            "Next",
            "Custom"
        )
        val adapter = VpAdapter(activity as FragmentActivity, fList)
        vp.adapter = adapter
        TabLayoutMediator(tabLayout, vp) { tab, pos ->
            tab.text = tList[pos]
        }.attach()
    }

}