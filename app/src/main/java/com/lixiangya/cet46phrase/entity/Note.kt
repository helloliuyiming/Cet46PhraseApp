package com.lixiangya.cet46phrase.entity

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Note : RealmObject() {

    @PrimaryKey
    lateinit var id: String
    lateinit var phrase: String
    lateinit var content: String
    var createdTime: Long = System.currentTimeMillis()
}