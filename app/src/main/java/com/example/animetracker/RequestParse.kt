package com.example.animetracker

import com.example.animetracker.adapters.AnimeModel
import com.example.animetracker.adapters.ExtendedAnimeModel
//import com.example.animetracker.adapters.ExtendedAnimeData
import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable

class RequestParse {
     fun parseGenres(result: String): String{
         return if (JSONObject(result).getJSONArray("data").length() != 0) {
             val list = ArrayList<String>()
             val genresArray = JSONObject(result).getJSONArray("data")
             for (i in 0 until genresArray.length()) {
                 val genre = genresArray.getJSONObject(i).getJSONObject("attributes").getString("name")
                 list.add(genre)
             }
             list.joinToString(", ")
         } else "???"

    }

    fun parseAnime(result: String): List<AnimeModel> {
        val list = ArrayList<AnimeModel>()
        if (JSONObject(result).getJSONArray("data").length() != 0) {
            val animeArray = JSONObject(result).getJSONArray("data")
            for (i in 0 until animeArray.length()) {
                val anime = animeArray.getJSONObject(i).getJSONObject("attributes")
                val averageRating: String = anime.getString("averageRating")
                val episodeCount = anime.getString("episodeCount")
                val item = AnimeModel(
                    title = anime.getString("canonicalTitle"),
                    averageRating = if (averageRating != "null")String.format("%.2f", averageRating.toDouble()/10)  else  "???",
                    smallPoster =  try{anime.getJSONObject("posterImage").getString("small")}catch (e: JSONException){"https://upload.wikimedia.org/wikipedia/commons/1/14/No_Image_Available.jpg"},
                    episodeCount = if(episodeCount!="null") episodeCount else "???",
                    id = animeArray.getJSONObject(i).getString("id")
                )
                list.add(item)
            }
        }
        return list
    }


    fun parseOneAnime(result: String): ExtendedAnimeModel {
        val anime = JSONObject(result).getJSONArray("data").getJSONObject(0).getJSONObject("attributes")
        val averageRating: String = anime.getString("averageRating")
        val ratingRank: String = anime.getString("ratingRank")
        val episodeCount = anime.getString("episodeCount")
        val episodeLength = anime.getString("episodeLength")
        return ExtendedAnimeModel(
            title = anime.getString("canonicalTitle"),
            averageRating = if (averageRating != "null")String.format("%.2f", averageRating.toDouble()/10)  else  "???",
            episodeCount = if(episodeCount!="null") episodeCount else "???",
            description = anime.getString("description"),
            ratingRank = if (ratingRank != "null") ratingRank else  "???",
            popularityRank = anime.getString("popularityRank"),
            largePoster = try{anime.getJSONObject("posterImage").getString("large")}catch (e: JSONException){"https://upload.wikimedia.org/wikipedia/commons/1/14/No_Image_Available.jpg"},
            episodeLength = if(episodeLength!="null") episodeLength else "???",
            ratingFrequencies = ratingFrequencies(anime.getJSONObject("ratingFrequencies")),
            season = getAnimeSeason(anime.getString("startDate")),
            genres = "???",
            id = JSONObject(result).getJSONArray("data").getJSONObject(0).getString("id")
        )
    }



    private fun getAnimeSeason(date: String): String{
       val month = date.substring(5, 7).toInt()
       val season = when(month){
               in 3..5-> "Spring"
               in 6..8-> "Summer"
               in 9..11-> "Fall"
               else-> "Winter"
        }
        var year = date.substring(0, 4).toInt()
        year = if(month==12) year+1 else year
        return "$year $season"
    }

//перероблення списку оцінок з апі (2-20) в оцінки(1-10)
    private fun ratingFrequencies(freq: JSONObject): List<Int> {
        val ratings = ArrayList<Int>()
        for (i in 2..20) {
            try {
                ratings.add(freq.getString(i.toString()).toInt())
            } catch (e: JSONException) {
                ratings.add(0)
            }
        }
        return listOf(ratings[0],
            (ratings[1] + ratings[2]),
            (ratings[3] + ratings[4]),
            (ratings[5] + ratings[6]),
            (ratings[7] + ratings[8]),
            (ratings[9] + ratings[10]),
            (ratings[11] + ratings[12]),
            (ratings[13] + ratings[14]),
            (ratings[15] + ratings[16]),
            (ratings[17] + ratings[18]))
    }
}