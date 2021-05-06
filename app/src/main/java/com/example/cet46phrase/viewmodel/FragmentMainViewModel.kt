package com.example.cet46phrase.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class FragmentMainViewModel(application: Application) : AndroidViewModel(application) {
    val followUnitLiveData: MutableLiveData<MutableMap<String, String>> = MutableLiveData()


    fun load() {

    }
}