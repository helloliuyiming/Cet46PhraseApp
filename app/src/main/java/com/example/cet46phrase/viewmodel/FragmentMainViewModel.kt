package com.example.cet46phrase.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.cet46phrase.entity.Phrase
import com.example.cet46phrase.util.AppDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

class FragmentMainViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application
    private val gson = Gson()
    var verbUnitList: MutableList<String>? = null
    var prepUnitList: MutableList<String>? = null
    var otherUnitList: MutableList<String>? = null
    val followUnitLiveData: MutableLiveData<MutableMap<String, MutableList<String>>> =
        MutableLiveData()
    val searchPhrasesLiveData: MutableLiveData<MutableList<Phrase>> = MutableLiveData()
    val phraseDao = AppDatabase.getInstance(application).phraseDao()
    val order: Boolean

    init {
        val config = application.getSharedPreferences("config", Context.MODE_PRIVATE)
        order = config.getBoolean("order", false)
    }

    fun load() {
        verbUnitList = null
        prepUnitList = null
        otherUnitList = null
        val sharedPreferences = context.getSharedPreferences("config", Context.MODE_PRIVATE)
        val string = sharedPreferences.getString("follows", "")
        if (string == "") {
            followUnitLiveData.value = null
            return
        }
        val followMap = gson.fromJson<MutableMap<String, MutableList<String>>>(string,
            object : TypeToken<MutableMap<String, MutableList<String>>>() {}.type
        )
        followMap.forEach {
            if (it.key == "verb_phrase") {
                verbUnitList = it.value
            } else if (it.key == "prep_phrase") {
                prepUnitList = it.value
            } else if (it.key == "other_phrase") {
                otherUnitList = it.value
            }
        }

    }

    fun searchPhraseByKeyWord(keyWord: String) {
        if (keyWord.trim().isEmpty()) {
            searchPhrasesLiveData.value = null
        }
        Single.create<MutableList<Phrase>> {
            it.onSuccess(phraseDao.queryByKeyWord(keyWord))
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                searchPhrasesLiveData.value = it
            }, {
                Log.e("main", "searchPhraseByKeyWord() ERROR:${it.message}")
                it.printStackTrace()
            })
    }
}