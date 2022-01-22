package com.lixiangya.cet46phrase.entity

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

open class TypeConverter {

    val gson = Gson()

    @TypeConverter
    fun getExamplesFromString(value: String?): MutableList<String> {
        return gson.fromJson<MutableList<String>>(
            value,
            object : TypeToken<MutableList<String>>() {}.type
        )
    }

    @TypeConverter
    fun storeExamplesFromList(list: MutableList<String>): String {
        return gson.toJson(list)
    }

    @TypeConverter
    fun getExplainsFromString(value: String?): MutableList<Explain> {
        return gson.fromJson<MutableList<Explain>>(
            value,
            object : TypeToken<MutableList<Explain>>() {}.type
        )
    }

    @TypeConverter
    fun storeExplainsFromList(list: MutableList<Explain>): String {
        return gson.toJson(list)
    }


    @TypeConverter
    fun getExplainFromString(value: String?): Explain {
        return gson.fromJson<Explain>(value, Explain::class.java)
    }

    @TypeConverter
    fun storeExplainsFromList(list: Explain): String {
        return gson.toJson(list)
    }
}