package com.example.cet46phrase.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.cet46phrase.entity.Phrase

@Dao
interface PhraseDao {

    @Query("select * from phrase")
    fun queryAll(): MutableList<Phrase>

    @Query("select * from phrase where unit = :unit")
    fun queryByUnit(unit: String): MutableList<Phrase>

    @Query("select * from phrase where unit = :unit and type = :type")
    fun queryByUnitAndType(unit: String, type: String): MutableList<Phrase>

    @Update
    fun update(phrase: Phrase)

    @Insert
    fun insert(phrase: Phrase)

}