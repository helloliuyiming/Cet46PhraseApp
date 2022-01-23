package com.lixiangya.cet46phrase

import com.lixiangya.cet46phrase.entity.Phrase
import com.google.gson.Gson
import org.junit.Test
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.*

class TestGson {

    @Test
    fun a() {
        val gson = Gson()
        val json = "{\"unit\":\"Unit　1\",\"phrase\":\"abandon oneself to\",\"origin\":\"《四六级常考核心词组》\",\"explains\":[{\"explain\":\"沉溺于，放纵（感情）\",\"examples\":[{\"en\":\"Don't abandon yourself to this kind of pleasure.\",\"cn\":\"别沉溺于这种享乐中。\"}]}],\"type\":\"verb_phrase\"}"
        try {
            val phrase = gson.fromJson<Phrase>(json, Phrase::class.java)
            println(phrase)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @Test
    fun read() {
        val file = File("C:\\Users\\gliuy\\Desktop\\outJson.json")
        val gson = Gson()
        val fileReader = FileReader(file)
        val bufferReader = BufferedReader(fileReader)
        var count = 0
        val unitMap: MutableMap<String, MutableList<String>> = mutableMapOf()
        bufferReader.lines().forEach {
            if (it != null) {
                val map = gson.fromJson<MutableMap<String, Any>>(it, MutableMap::class.java)
                val unit = map["unit"] as String
                val type = map["type"] as String
                var list = unitMap[type]
                if (list == null) {
                    list = mutableListOf()
                    unitMap.put(type, list)
                }
                if (!list.contains(unit)) {
                    list.add(unit)
                }
            }
            count++
        }
        println("count = ${count}")
        println("unitMap = ${gson.toJson(unitMap)}")

        bufferReader.close()
        fileReader.close()
    }

    @Test
    fun tD(){


    }
}