package com.example.cet46phrase.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Note{
    @PrimaryKey(autoGenerate = true)
    var id:Int = 0
    lateinit var phrase:String
    lateinit var content:String
    var createdTime:Long = System.currentTimeMillis()
}