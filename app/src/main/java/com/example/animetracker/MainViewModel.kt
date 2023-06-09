package com.example.animetracker

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.animetracker.adapters.AnimeModel

class MainViewModel : ViewModel() {
    //список пошуку аніме
    val searchDataList = MutableLiveData<List<AnimeModel>>()

    //списки для збережених аніме
    val listsDataList = mapOf(
        "All" to MutableLiveData<List<AnimeModel>>(),
        "Watching" to MutableLiveData<List<AnimeModel>>(),
        "Completed" to MutableLiveData<List<AnimeModel>>(),
        "On Hold" to MutableLiveData<List<AnimeModel>>(),
        "Dropped" to MutableLiveData<List<AnimeModel>>(),
        "Plan to watch" to MutableLiveData<List<AnimeModel>>()
    )
    //списки для аніме по сезонам
    val seasonDataList = mapOf(
        "Previous" to MutableLiveData<List<AnimeModel>>(),
        "This" to MutableLiveData<List<AnimeModel>>(),
        "Next" to MutableLiveData<List<AnimeModel>>(),
        "Custom" to MutableLiveData<List<AnimeModel>>()
    )

}