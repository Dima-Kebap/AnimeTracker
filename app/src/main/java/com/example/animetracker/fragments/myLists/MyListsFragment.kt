package com.example.animetracker.fragments.myLists

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
import com.example.animetracker.databinding.FragmentMyListsBinding
import com.google.android.material.tabs.TabLayoutMediator


class MyListsFragment : Fragment() {
    private lateinit var binding: FragmentMyListsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyListsBinding.inflate(inflater, container, false)
        val networkInfo = (requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
        if (networkInfo == null || !networkInfo.isConnected)
            Toast.makeText(context, getString(R.string.no_internet), Toast.LENGTH_LONG).show()
        init()
        return binding.root
    }

    private fun init() = with(binding) {
         val fList: List<Fragment> = listOf(//список фрагментів(аніме по доданим спискам)
             ListFragment.newInstance(getString(R.string.All_list)),
             ListFragment.newInstance(getString(R.string.Watching_list)),
             ListFragment.newInstance(getString(R.string.Completed_list)),
             ListFragment.newInstance(getString(R.string.On_Hold_list)),
             ListFragment.newInstance(getString(R.string.Dropped_list)),
             ListFragment.newInstance(getString (R.string.Plan_to_Watch_list)),
        )

         val tList = listOf(//назви фрагментів
             getString(R.string.All_list),
             getString(R.string.Watching_list),
             getString(R.string.Completed_list),
             getString(R.string.On_Hold_list),
             getString(R.string.Dropped_list),
             getString (R.string.Plan_to_Watch_list)
        )
        val adapter = VpAdapter(activity as FragmentActivity, fList)
        vp.adapter = adapter

        TabLayoutMediator(tabLayout, vp) { tab, pos ->
            tab.text = tList[pos]
        }.attach()
    }
}