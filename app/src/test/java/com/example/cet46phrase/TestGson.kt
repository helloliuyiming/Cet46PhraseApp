package com.example.cet46phrase

import com.example.cet46phrase.entity.Phrase
import com.google.gson.Gson
import org.junit.Test
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class TestGson {

    @Test
    fun a() {
        val gson = Gson()
        val json =
            "{\"phrase\":\"worthy of\",\"explains\":[{\"explain\":\"值得的；配得上的\",\"examples\":[\"Your diligence is worthy of encouragement, but effort does not equal accomplishment. 你的勤奋值得鼓励，但是你的努力不一定会得到相应的成就。（阅读/CET6，2010.6）\"]}]}"
        val phrase = gson.fromJson<Phrase>(json, Phrase::class.java)
        println(phrase)
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
}