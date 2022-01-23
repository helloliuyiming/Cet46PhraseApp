package com.lixiangya.cet46phrase.entity

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.io.Serializable
import java.util.*

open class Phrase : RealmObject(),Serializable {
    @PrimaryKey
    var id: Int = 0
    lateinit var explains: RealmList<Explain>
    var notice: String? = null
    lateinit var origin: String
    lateinit var phrase: String
    var synonym: String? = null
    lateinit var type: String
    lateinit var unit: String

    var reviewDate:Date? = null

    var isActive = false
    var level:Int = 0;

    var score: Int = 0

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

open class Explain:RealmObject() ,Serializable {
    var id: Int = 0
    lateinit var explain: String
    var examples: RealmList<Example> = RealmList()
}

open class Example :RealmObject(), Serializable {
    lateinit var cn: String
    lateinit var en: String
}