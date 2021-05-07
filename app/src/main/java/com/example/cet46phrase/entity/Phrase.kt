package com.example.cet46phrase.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity
@TypeConverters(TypeConverter::class)
class Phrase {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    lateinit var explains: MutableList<Explain>
    var notice: String? = null
    lateinit var origin: String
    lateinit var phrase: String
    var synonym: String? = null
    lateinit var type: String
    lateinit var unit: String

    var score: Int = 0
    var last = false
    var status = 0

    companion object {
        const val STATUS_UNACTIVE = 0
        const val STATUS_ACTIVE = 1
        const val STATUS_NEW = 2
        const val STATUS_REVIEW = 3
    }
}

class Explain {
    //    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    lateinit var explain: String
    var examples: MutableList<String> = mutableListOf()
}