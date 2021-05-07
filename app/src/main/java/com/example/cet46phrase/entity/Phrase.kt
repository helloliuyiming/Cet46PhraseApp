package com.example.cet46phrase.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.annotations.Expose

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


    @Expose
    var score: Int = 0
    @Expose
    var last = false
    var status = 0


    companion object {
        const val STATUS_UNACTIVE = 0
        const val STATUS_ACTIVE = 1
        const val STATUS_NEW = 2
        const val STATUS_REVIEW = 3
    }

    override fun toString(): String {
        return "Phrase(id=$id, explains=$explains, notice=$notice, origin='$origin', phrase='$phrase', synonym=$synonym, type='$type', unit='$unit', score=$score, last=$last, status=$status)"
    }
}

class Explain {
    //    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    lateinit var explain: String
    var examples: MutableList<Example> = mutableListOf()
}

class Example{
    var cn:String? = null
    var  en:String? = null
}