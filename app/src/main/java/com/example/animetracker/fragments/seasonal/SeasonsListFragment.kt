package com.example.animetracker.fragments.seasonal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.animetracker.R
import com.example.animetracker.adapters.SeasonAdapter
import com.example.animetracker.databinding.FragmentSeasonsListBinding
import java.util.Calendar


class SeasonsListFragment : Fragment(), SeasonAdapter.OnChipClickListener {
    private lateinit var binding: FragmentSeasonsListBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSeasonsListBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    private fun init() = with(binding) {
        val layoutManager = LinearLayoutManager(activity)
        rcViewSeasons.layoutManager = layoutManager
        val adapter = SeasonAdapter(this@SeasonsListFragment)
        rcViewSeasons.adapter = adapter
        //масив років аніме(з цього року+1 до 1917, коли було випущено перше аніме)
        val year = Calendar.getInstance().get(Calendar.YEAR)

        adapter.submitList((year+1 downTo 1917).map { it.toString() })
    }

    override fun onChipClick(chipText: String, year:String) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.customSeason, SeasonalAnimeFragment.newInstance(year,chipText,"Custom",true))
            .addToBackStack("SeasonsList").commit()
    }


}