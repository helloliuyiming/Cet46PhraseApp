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
    val followUnitLiveData: MutableLiveData<MutableMap<String, MutableList<String>>> =
        MutableLiveData()


    fun load() {
        val sharedPreferences = context.getSharedPreferences("config", Context.MODE_PRIVATE)
        val string = sharedPreferences.getString("follows", "")
        if (string == "") {
            followUnitLiveData.value = null
        } else {
            val followMap = gson.fromJson<MutableMap<String, MutableList<String>>>(string,
                object : TypeToken<MutableMap<String, MutableList<String>>>() {}.type)
            var count = 0
            followMap.forEach {
                count += it.value.size

            }
            if (count == 0) {
                followUnitLiveData.value = null
            } else {
                followUnitLiveData.value = followMap
            }
        }
    }
}