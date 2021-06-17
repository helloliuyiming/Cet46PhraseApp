package com.example.cet46phrase.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
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
    val phraseListLiveData: MutableLiveData<MutableList<Phrase>> = MutableLiveData()
    val notesLiveData: MutableLiveData<MutableList<Note>> = MutableLiveData()
    val editNoteLiveData: MutableLiveData<Note> = MutableLiveData()
    var deepLinkSignal: Boolean = false
    var reviewModeSignal: Boolean = false
    var count = 0
    var completedLiveData: MutableLiveData<Int> = MutableLiveData(0)
    var done = false

    private val context = application
    private val phraseQueue: Queue<Phrase> = LinkedList<Phrase>()
    private val activePhraseQueue: Queue<Phrase> = LinkedList<Phrase>()
    private val phraseDao = AppDatabase.getInstance(application).phraseDao()
    private val noteDao = AppDatabase.getInstance(application).noteDao()
    private lateinit var followMap: MutableMap<String, MutableList<String>>
    private val gson = Gson()
    private val order: Boolean

    init {
        val sharedPreferences = application.getSharedPreferences("config", Context.MODE_PRIVATE)
        order = sharedPreferences.getBoolean("order", false)
    }

    fun load() {
        if (deepLinkSignal) {
            return
        }
        if (phraseListLiveData.value != null) {
//            phraseListLiveData.value = phraseListLiveData.value
            next()
            return
        }
        val sharedPreferences = context.getSharedPreferences("config", Context.MODE_PRIVATE)
        val followsJson = sharedPreferences.getString("follows", "")
        followMap = gson.fromJson<MutableMap<String, MutableList<String>>>(
            followsJson,
            object : TypeToken<MutableMap<String, MutableList<String>>>() {}.type
        )
        Single.create<Boolean> {
            val list: MutableList<Phrase> = mutableListOf()
            followMap.forEach {
                val type = it.key
                it.value.forEach {
                    list.addAll(phraseDao.queryByUnitAndType(it, type))
                }
            }
            count = list.size
            completedLiveData.postValue(completedLiveData.value)
            if (!order) {
                list.shuffle()
            }
            Log.i("main", "phraseListLiveData.postValue(list)")
            phraseListLiveData.postValue(list)
            list.forEach {
                if (reviewModeSignal) {
                    it.score = 3
                    it.last = false
                }
                if (activePhraseQueue.size < 11) {
                    activePhraseQueue.offer(it)
                } else {
                    phraseQueue.offer(it)
                }
            }
            it.onSuccess(true)
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                next()
            }, {
                Log.e("main", "${it.message}")
                it.printStackTrace()
            })

    }

    fun next() {
        if (phraseLiveData.value != null) activePhraseQueue.offer(phraseLiveData.value)
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
        } else {
            completedLiveData.value = completedLiveData.value!! + 1
        }

    }

    fun save() {

    }

    private fun loadMore() {
        for (i in 0..5) {
            val p = phraseQueue.poll()
            if (p != null) {
                activePhraseQueue.offer(p)
            } else {
                return
            }
        }
    }

    fun loadNote(phrase: String) {
        notesLiveData.value = null
        Single.create<MutableList<Note>> {
            val queryByPhrase = noteDao.queryByPhrase(phrase)
            it.onSuccess(queryByPhrase)
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                notesLiveData.value = it
            }, {
                Log.e("main", "loadNote() ERROR:${it.message}")
                it.printStackTrace()
            })
    }

    fun saveNote(note: Note) {
        Single.create<Boolean> {
            noteDao.insert(note)
            it.onSuccess(true)
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Toast.makeText(context, "笔记保存成功", Toast.LENGTH_SHORT).show()
                loadNote(note.phrase)
                editNoteLiveData.value = null
            }, {
                Log.e("main", "saveNote() ERROR:${it.message}")
                it.printStackTrace()
            })
    }

    fun updateNote(note: Note) {
        Single.create<Boolean> {
            noteDao.update(note)
            it.onSuccess(true)
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Toast.makeText(context, "笔记保存成功", Toast.LENGTH_SHORT).show()
                loadNote(note.phrase)
                editNoteLiveData.value = null
            }, {
                Log.e("main", "updateNote() ERROR:${it.message}")
                it.printStackTrace()
            })
    }

    fun deleteNote(note: Note) {
        Single.create<Boolean> {
            noteDao.delete(note)
            it.onSuccess(true)
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Toast.makeText(context, "笔记删除成功", Toast.LENGTH_SHORT).show()
                loadNote(note.phrase)
                editNoteLiveData.value = null
            }, {
                Log.e("main", "updateNote() ERROR:${it.message}")
                it.printStackTrace()
            })
    }

    companion object {
        const val VIEW_TYPE_SHOW = 1
        const val VIEW_TYPE_TEST = 2
        const val VIEW_TYPE_WRITE = 3
    }

}