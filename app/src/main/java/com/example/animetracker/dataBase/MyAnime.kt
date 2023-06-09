package com.example.animetracker.dataBase

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity ("animes")
data class MyAnime(
    @PrimaryKey
    var id:Int,
    @ColumnInfo(name = "listName")
    var listName: String,
    @ColumnInfo(name = "rating")
    val rating: Int,
    @ColumnInfo(name = "episodes")
    val episodes:Int
)