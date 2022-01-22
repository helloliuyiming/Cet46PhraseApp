package com.lixiangya.cet46phrase.dao

import androidx.room.*
import com.lixiangya.cet46phrase.entity.Note

@Dao
interface NoteDao {

    @Query("select * from note")
    fun queryAll(): MutableList<Note>

    @Query("select * from note where phrase = :phrase")
    fun queryByPhrase(phrase: String): MutableList<Note>

    @Update
    fun update(note: Note)

    @Insert
    fun insert(note: Note)

    @Delete
    fun delete(note: Note)

}