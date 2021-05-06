package com.example.cet46phrase.util

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.cet46phrase.dao.PhraseDao
import com.example.cet46phrase.entity.Phrase

@Database(entities = [Phrase::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun phraseDao(): PhraseDao

    companion object {
        private var database: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            if (database == null) {
                database = Room.databaseBuilder(context, AppDatabase::class.java, "app")
                    .build()
            }
            return database!!
        }
    }
}

