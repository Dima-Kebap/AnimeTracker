package com.example.animetracker.adapters

import java.io.Serializable

data class AnimeModel(
    val title: String,
    val averageRating: String,
    val smallPoster: String,
    val episodeCount: String,
    val id: String
) : Serializable

data class ExtendedAnimeModel(
    val title: String,
    val averageRating: String,
    val episodeCount: String,
    val description: String,
    val ratingRank: String,
    val popularityRank: String,
    val largePoster: String,
    val episodeLength: String,
    val ratingFrequencies: List<Int>,
    val season: String,
    var genres: String,
    val id: String
) : Serializable
