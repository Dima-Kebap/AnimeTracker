package com.example.animetracker.fragments.seasonal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.animetracker.R

class CustomSeasonFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        childFragmentManager.beginTransaction().replace(R.id.customSeason, SeasonsListFragment()).commit()

        return inflater.inflate(R.layout.fragment_custom_season, container, false)
    }


}