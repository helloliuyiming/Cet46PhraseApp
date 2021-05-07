package com.example.cet46phrase.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.cet46phrase.entity.Note
import com.example.cet46phrase.entity.Phrase
import com.example.cet46phrase.util.AppDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*

class FragmentLearnViewModel(application: Application) : AndroidViewModel(application) {
    val viewTypeLiveData: MutableLiveData<Int> = MutableLiveData()
    val phraseLiveData: MutableLiveData<Phrase> = MutableLiveData()
    val notesLiveData: MutableLiveData<MutableList<Note>> = MutableLiveData()
    var count = 0
    var completedLiveData:MutableLiveData<Int> = MutableLiveData(0)
    var done = false

    private val context = application
    private val phraseQueue: Queue<Phrase> = LinkedList<Phrase>()
    private val activePhraseQueue: Queue<Phrase> = LinkedList<Phrase>()
    private val phraseDao = AppDatabase.getInstance(application).phraseDao()
     private lateinit var followMap:MutableMap<String,MutableList<String>>
    private val gson = Gson()

    fun load() {
        val sharedPreferences = context.getSharedPreferences("config", Context.MODE_PRIVATE)
        val followsJson = sharedPreferences.getString("follows", "")
        followMap = gson.fromJson<MutableMap<String, MutableList<String>>>(followsJson,
            object : TypeToken<MutableMap<String, MutableList<String>>>() {}.type)
        Single.create<Boolean> {
            val list:MutableList<Phrase> = mutableListOf()
            followMap.forEach {
                val type = it.key
                it.value.forEach {
                    list.addAll(phraseDao.queryByUnitAndType(it,type))
                }
            }
            count = list.size
            completedLiveData.postValue(completedLiveData.value)
            list.shuffle()
            list.forEach {
                if (activePhraseQueue.size < 11) {
                    activePhraseQueue.offer(it)
                }else{
                    phraseQueue.offer(it)
                }
            }
            it.onSuccess(true)
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                       next()
            },{
                Log.e("main","${it.message}")
                it.printStackTrace()
            })

    }

    fun next() {
        if (phraseLiveData.value!=null) activePhraseQueue.offer(phraseLiveData.value)
        if (activePhraseQueue.size == 0 && phraseQueue.size == 0) {
            done = true
            phraseLiveData.value = null
            return
        }
        if (activePhraseQueue.size == 0 && phraseQueue.size > 0) {
            loadMore()
            next()
            return
        }
        if (activePhraseQueue.size < 6) {
            loadMore()
        }
        phraseLiveData.value = activePhraseQueue.poll()
    }

    fun pass() {

        if (activePhraseQueue.size == 0 && phraseQueue.size == 0) {
            done = true
            phraseLiveData.value = null
            return
        }
        if (activePhraseQueue.size == 0 && phraseQueue.size > 0) {
            loadMore()
            next()
            return
        }
        if (activePhraseQueue.size < 6) {
            loadMore()
        }
        phraseLiveData.value = activePhraseQueue.poll()
        if (completedLiveData.value == null) {
            completedLiveData.value = 1
        }else{
            completedLiveData.value = completedLiveData.value!!+1
        }

    }
    fun save(){

    }

    private fun loadMore() {
        for (i in 0..5) {
            val p = phraseQueue.poll()
            if (p != null) {
                activePhraseQueue.offer(p)
            }else{
                return
            }
        }
    }

    companion object {
        const val VIEW_TYPE_SHOW = 1
        const val VIEW_TYPE_TEST = 2
        const val VIEW_TYPE_WRITE = 3
    }

}