package com.example.animetracker.dataBase

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface Dao{
    @Query("DELETE FROM animes")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun replaceItem(item: MyAnime)

    @Query("SELECT * FROM animes ORDER BY id ASC")
    fun getAll():List<MyAnime>

    @Query("SELECT * FROM animes WHERE id=:id")
    fun getById(id:Int): MyAnime

    @Query("DELETE FROM animes WHERE id=:id")
    fun deleteById(id:String)

    @Query("SELECT * FROM animes WHERE listName=:listName ORDER BY id ASC")
    fun getByListName(listName: String): List<MyAnime>

}