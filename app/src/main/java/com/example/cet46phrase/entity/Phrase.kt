package com.example.cet46phrase.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
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
}

@Entity
class Explain {
    lateinit var explain: String
    var examples: MutableList<String> = mutableListOf()

}