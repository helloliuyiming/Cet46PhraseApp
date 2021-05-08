package com.example.cet46phrase.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FragmentMainViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application
    private val gson = Gson()
    var verbUnitList:MutableList<String>? = null
    var prepUnitList:MutableList<String>? = null
    var otherUnitList:MutableList<String>? = null
    val followUnitLiveData: MutableLiveData<MutableMap<String, MutableList<String>>> =
        MutableLiveData()


    fun load() {
        val sharedPreferences = context.getSharedPreferences("config", Context.MODE_PRIVATE)
        val string = sharedPreferences.getString("follows", "")
        if (string == "") {
            followUnitLiveData.value = null
            return
        }
        val followMap = gson.fromJson<MutableMap<String, MutableList<String>>>(string,
            object : TypeToken<MutableMap<String, MutableList<String>>>() {}.type)
        followMap.forEach {
            if (it.key == "verb_phrase") {
                verbUnitList = it.value
            }else if (it.key == "prep_phrase") {
                prepUnitList = it.value
            }else if (it.key == "other_phrase") {
                otherUnitList = it.value
            }
        }

    }
}