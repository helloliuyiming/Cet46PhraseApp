package com.lixiangya.cet46phrase.entity

import com.lixiangya.cet46phrase.algorithm.FlashCard
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.io.Serializable
import java.util.*

open class Phrase : RealmObject(),Serializable, FlashCard {
    @PrimaryKey
    var id: Int = 0
    lateinit var explains: RealmList<Explain>
    var notice: String? = null
    lateinit var origin: String
    lateinit var phrase: String
    var synonym: String? = null
    lateinit var type: String
    lateinit var unit: String

    var repetition = 0

    var reviewDate:Date? = null

    var isActive = false

    var step = 1

    var level:Int = 0

    var score: Int = 0

    var status = 0


    companion object {
        const val STATUS_UNACTIVE = 0
        const val STATUS_ACTIVE = 1
        const val STATUS_NEW = 2
        const val STATUS_REVIEW = 3
    }

    override fun getRepetitions(): Int {
        return repetition
    }

    override fun getInterval(): Int {
        return 1
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