package com.lixiangya.cet46phrase.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.lixiangya.cet46phrase.entity.Phrase
import com.lixiangya.cet46phrase.util.PreferencesUtil
import io.realm.Realm

class FragmentSelectViewModel(application: Application) : AndroidViewModel(application) {


    val actionModeLiveData: MutableLiveData<Int> = MutableLiveData(1)
    var verbUnitList: MutableList<String>? = null
    var prepUnitList: MutableList<String>? = null
    var otherUnitList: MutableList<String>? = null
    @SuppressLint("StaticFieldLeak")
    val context:Context

    init {
        context = application
    }

    var followCount = 0

    val followSet:MutableSet<MutableMap<String,String>> = mutableSetOf()
    val signalLiveData:MutableLiveData<Int> = MutableLiveData()

    private val gson = Gson()
    private val json =
        "{\"verb_phrase\":[\"Unit 1\",\"Unit 2\",\"Unit 3\",\"Unit 4\",\"Unit 5\",\"Unit 6\",\"Unit 7\",\"Unit 8\",\"Unit 9\",\"Unit 10\",\"Unit 11\",\"Unit 12\"],\"prep_phrase\":[\"Unit 1\",\"Unit 2\",\"Unit 3\",\"Unit 4\",\"Unit 5\",\"Unit 6\"],\"other_phrase\":[\"Unit 1\",\"Unit 2\"]}"

    fun load() {
        val data =
            gson.fromJson<MutableMap<String, MutableList<String>>>(json, MutableMap::class.java)
        data.forEach {
            if (it.key == "verb_phrase") {
                verbUnitList = it.value
            } else if (it.key == "prep_phrase") {
                prepUnitList = it.value
            } else if (it.key == "other_phrase") {
                otherUnitList = it.value
            }
        }
    }

    fun rebuild(reset: Boolean = false) {

        val verbUnitList: MutableList<String> = mutableListOf()
        val prepUnitList: MutableList<String> = mutableListOf()
        val otherUnitList: MutableList<String> = mutableListOf()

        followSet.forEach {
            if (it["type"].equals("verb_phrase")) {
                verbUnitList.add(it["unit"]!!)
            }else if (it["type"].equals("prep_phrase")) {
                prepUnitList.add(it["unit"]!!)
            }else{
                otherUnitList.add(it["unit"]!!)
            }
        }

        val gson = Gson()
        val followsUnit: MutableMap<String, MutableList<String>> = mutableMapOf()
        val preferences = PreferencesUtil.getSharePreferences(context)
        val edit = preferences.edit()
        if (verbUnitList.size > 0) {
            followsUnit["verb_phrase"] = verbUnitList
        }
        if (prepUnitList.size > 0) {
            followsUnit["prep_phrase"] = prepUnitList
        }
        if (otherUnitList.size > 0) {
            followsUnit["other_phrase"] = otherUnitList
        }
        try {
            edit.putString("follows", gson.toJson(followsUnit))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        edit.apply()

        val realm = Realm.getDefaultInstance()

        realm.executeTransactionAsync({
            if (reset) {
                val findAll = it.where(Phrase::class.java)
                    .findAll()
                findAll.setInt("level",0)
                findAll.setDate("reviewDate",null)
            }
            it.where(Phrase::class.java)
                .findAll()
                .setBoolean("isActive",false)
            val where = it.where(Phrase::class.java)
            var isFirst = true
            followSet.forEach {
                if (!isFirst) {
                    where.or()
                }
                isFirst = false
                where.equalTo("type",it["type"]!!)
                    .equalTo("unit",it["unit"])
            }
            where.findAll()
                .setBoolean("isActive",true)
        }, Realm.Transaction.OnSuccess {
            Log.i("main","OnSuccess")
            signalLiveData.value = SIGNAL_SAVE_DONE
        })
    }

    fun addUnit() {

    }

    fun existUnit() {

    }

    fun delUnit() {

    }

    fun followUnit() {

    }

    fun followUnitCount() {

    }

    companion object {
        const val ACTION_MODE_SELECT_SINGLE = 1
        const val ACTION_MODE_SELECT_MULTIPLE = 2

        const val SIGNAL_SAVE_DONE = 1;
    }
}