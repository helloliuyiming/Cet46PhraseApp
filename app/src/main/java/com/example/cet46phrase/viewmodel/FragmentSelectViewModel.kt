package com.example.cet46phrase.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson

class FragmentSelectViewModel(application: Application) : AndroidViewModel(application) {


    val actionModeLiveData:MutableLiveData<Int> = MutableLiveData(1)
    var verbUnitList:MutableList<String>? = null
    var prepUnitList:MutableList<String>? = null
    var otherUnitList:MutableList<String>? = null

    var followCount = 0

    private val gson = Gson()
    private val json =
        "{\"verb_phrase\":[\"Unit 1\",\"Unit 2\",\"Unit 3\",\"Unit 4\",\"Unit 5\",\"Unit 6\",\"Unit 7\",\"Unit 8\",\"Unit 9\",\"Unit 10\",\"Unit 11\",\"Unit 12\"],\"prep_phrase\":[\"Unit 1\",\"Unit 2\",\"Unit 3\",\"Unit 4\",\"Unit 5\",\"Unit 6\"],\"other_phrase\":[\"Unit 1\",\"Unit 2\"]}"

    fun load() {
        val data = gson.fromJson<MutableMap<String, MutableList<String>>>(json, MutableMap::class.java)
        data.forEach {
            if (it.key == "verb_phrase") {
                verbUnitList = it.value
            }else if (it.key == "prep_phrase") {
                prepUnitList = it.value
            }else if (it.key == "other_phrase") {
                otherUnitList = it.value
            }
        }
    }

    fun addUnit(){

    }

    fun existUnit(){

    }

    fun delUnit(){

    }
    fun followUnit(){

    }

    fun followUnitCount(){

    }

    companion object{
        const val ACTION_MODE_SELECT_SINGLE = 1
        const val ACTION_MODE_SELECT_MULTIPLE = 2

    }
}