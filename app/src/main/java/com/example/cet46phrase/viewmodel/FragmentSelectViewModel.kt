package com.example.cet46phrase.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson

class FragmentSelectViewModel(application: Application) : AndroidViewModel(application) {

    val unitListLiveData: MutableLiveData<MutableMap<String, MutableList<String>>> =
        MutableLiveData()
    private val gson = Gson()
    private val json =
        "{\"verb_phrase\":[\"Unit 1\",\"Unit 2\",\"Unit 3\",\"Unit 4\",\"Unit 5\",\"Unit 6\",\"Unit 7\",\"Unit 8\",\"Unit 9\",\"Unit 10\",\"Unit 11\",\"Unit 12\"],\"prep_phrase\":[\"Unit 1\",\"Unit 2\",\"Unit 3\",\"Unit 4\",\"Unit 5\",\"Unit 6\"],\"other_phrase\":[\"Unit 1\",\"Unit 2\"]}"

    fun load() {
        unitListLiveData.value =
            gson.fromJson<MutableMap<String, MutableList<String>>>(json, MutableMap::class.java)
    }
}