package com.lixiangya.cet46phrase.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.lixiangya.cet46phrase.entity.Note
import com.lixiangya.cet46phrase.entity.Phrase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lixiangya.cet46phrase.algorithm.SM2
import com.lixiangya.cet46phrase.util.PreferencesUtil
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers
import io.realm.Realm
import io.realm.RealmAny
import java.util.*

class FragmentLearnViewModel(application: Application) : AndroidViewModel(application) {
    val viewTypeLiveData: MutableLiveData<Int> = MutableLiveData()
    val phraseLiveData: MutableLiveData<Phrase?> = MutableLiveData()
    val phraseListLiveData: MutableLiveData<MutableList<Phrase>> = MutableLiveData()
    val notesLiveData: MutableLiveData<MutableList<Note>> = MutableLiveData()
    val editNoteLiveData: MutableLiveData<Note?> = MutableLiveData()
    var deepLinkSignal: Boolean = false
    var reviewModeSignal: Boolean = false
    var count = 0
    var completedLiveData: MutableLiveData<Int> = MutableLiveData(0)
    var done = false

    val newPhraseProgressObservableField:ObservableField<String> = ObservableField()
    val reviewPhraseProgressObservableField:ObservableField<String> = ObservableField()

    private var newPhraseTotal = 0
    private var reviewPhraseTotal = 0
    private var newPhraseDone = 0
    private var reviewPhraseDone = 0

    private val context = application
    private val phraseQueue: Queue<Phrase> = LinkedList<Phrase>()
    private val activePhraseQueue: Queue<Phrase> = LinkedList<Phrase>()
    private val gson = Gson()
    private val order: Boolean = PreferencesUtil.getSharePreferences(application).getBoolean("order", false)


    fun load() {
        if (deepLinkSignal) {
            return
        }
        if (phraseListLiveData.value != null) {
            next()
            return
        }

        Single.create<Boolean> {
            val list: MutableList<Phrase> = mutableListOf()
            val realm = Realm.getDefaultInstance()

            var newPhrase = mutableListOf<Phrase>()
            if (!reviewModeSignal) {
                val newPhraseRealm = realm.where(Phrase::class.java)
                    .equalTo("isActive", true)
                    .equalTo("reviewDate", RealmAny.nullValue())
                    .findAll()
                newPhrase = realm.copyFromRealm(newPhraseRealm)
                newPhraseTotal = newPhrase.size
                if (!order) {
                    newPhrase.shuffle()
                }
            }

            val reviewListRealm = realm.where(Phrase::class.java)
                .between("reviewDate", today(), tomorrow())
                .findAll()

            val reviewPhrase:MutableList<Phrase> = realm.copyFromRealm(reviewListRealm)
            reviewPhraseTotal = reviewPhrase.size

            val newSize = newPhrase.size
            val reviewSize = reviewPhrase.size
            var newProgress = 1F
            var reviewProgress = 1F
            val length = newSize+reviewSize
            for (i in 0..length) {
                if (newPhrase.size == 0 && reviewPhrase.size == 0) {
                    break
                }
                if (newPhrase.size == 0 && reviewPhrase.size != 0) {
                    list.add(reviewPhrase[0])
                    reviewPhrase.removeAt(0)
                    continue
                }
                if (newPhrase.size != 0 && reviewPhrase.size == 0) {
                    list.add(newPhrase[0])
                    newPhrase.removeAt(0)
                    continue
                }
                if (newProgress / newSize < reviewProgress / reviewSize) {
                    list.add(newPhrase[0])
                    newPhrase.removeAt(0)
                    newProgress++
                }else{
                    list.add(reviewPhrase[0])
                    reviewPhrase.removeAt(0)
                    reviewProgress++
                }
            }

            count = list.size
            completedLiveData.postValue(completedLiveData.value)
            if (!order) {
                list.shuffle()
            }
            phraseListLiveData.postValue(list)
            list.forEach {
                if (activePhraseQueue.size < 11) {
                    activePhraseQueue.offer(it)
                } else {
                    phraseQueue.offer(it)
                }
            }
            it.onSuccess(true)
            updateProgress()
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                next()
            }, {
                Log.e("main", "${it.message}")
                it.printStackTrace()
            })
    }

    fun reload(isActive: Boolean = true,status:Int = 0,type:String? = null,unit:String? = null) {

        Observable.create<MutableList<Phrase>> {
            val realm = Realm.getDefaultInstance()
            val where = realm.where(Phrase::class.java)
            if (isActive) {
                where.equalTo("isActive",true)
            }
            if (status!=0) {

                if (status == 1) {
                    where.equalTo("reviewDate", RealmAny.nullValue())
                }else{
                    where.notEqualTo("reviewDate", RealmAny.nullValue())
                }

            }
            if (type != null) {
                where.equalTo("type",type)
            }
            if (unit != null) {
                where.equalTo("unit",unit)
            }

            val phraseListRealm = where.findAll()
            val list= mutableListOf<Phrase>()
            list.addAll(realm.copyFromRealm(phraseListRealm))
            realm.close()
            it.onNext(list)
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                phraseListLiveData.value = it
            }
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

        val phrase = phraseLiveData.value!!

        if (phrase.reviewDate == null) {
            newPhraseDone++
        }else{
            reviewPhraseDone++
        }
        updateProgress()

        var quality:Int = if (phrase.score >= 10) {
            5
        }else if (phrase.score >= 8) {
            4
        }else if (phrase.score >= 6) {
            3
        }else if (phrase.score >= 4) {
            2
        }else if (phrase.score >= 2) {
            1
        }else{
            0
        }

        val reviewDate = SM2.calculateSuperMemo2Algorithm(phrase, quality)
        phrase.reviewDate = Date(reviewDate)
        phrase.repetition++
        phrase.step = 2
        phrase.score = 10
        updatePhrase(phrase)
        //TODO 计算下次复习时间
        if (activePhraseQueue.size == 0 && phraseQueue.size == 0) {
            done = true
            phraseLiveData.value = Phrase()
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

    private fun updateProgress(){
        newPhraseProgressObservableField.set("$newPhraseDone / $newPhraseTotal")
        reviewPhraseProgressObservableField.set("$reviewPhraseDone / $reviewPhraseTotal")
    }
    private fun updatePhrase(phrase: Phrase) {
        Single.create<Phrase> {
            val realm = Realm.getDefaultInstance()
            realm.executeTransaction {
                realm.insertOrUpdate(phrase)
            }
            realm.close()
            it.onSuccess(phrase)
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer {
                Log.i("main","${it.phrase}保存成功，下次复习时间${it.reviewDate}")
            })

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

        Single.create<MutableList<Note>> {
            val realm = Realm.getDefaultInstance()
            val findAll = realm.where(Note::class.java)
                .equalTo("phrase", phrase)
                .findAll()
            val copyFromRealm = realm.copyFromRealm(findAll)
            realm.close()
            it.onSuccess(copyFromRealm)
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
            note.id = UUID.randomUUID().toString()
            val realm = Realm.getDefaultInstance();
            realm.executeTransaction {
                realm.copyToRealmOrUpdate(note)
            }
            realm.close()
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
            val realm = Realm.getDefaultInstance()
            realm.executeTransaction {
                realm.insertOrUpdate(note)
            }
            realm.close()
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
            val realm = Realm.getDefaultInstance()
            realm.executeTransaction {
                realm.where(Note::class.java)
                    .equalTo("id",note.id)
                    .findAll()
                    .deleteAllFromRealm()
            }
            realm.close()
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

    fun loadPhrase(string: String) {
        Single.create<Phrase>{
            val realm = Realm.getDefaultInstance()
            val findFirst = realm.where(Phrase::class.java)
                .equalTo("phrase", string)
                .findFirst()
            val copyFromRealm = realm.copyFromRealm(findFirst)
            realm.close()
            it.onSuccess(copyFromRealm)
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                       phraseLiveData.value = it
            },{
                Toast.makeText(context,"ERROR:${it.message}",Toast.LENGTH_SHORT).show()
            })

    }

    companion object {
        const val VIEW_TYPE_SHOW = 1
        const val VIEW_TYPE_TEST = 2
        const val VIEW_TYPE_WRITE = 3
    }

    private fun today():Date{
        val calendar: Calendar = Calendar.getInstance()
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val todayZero: Long = calendar.timeInMillis
        return Date(todayZero)
    }

    private fun tomorrow():Date{
        val calendar: Calendar = Calendar.getInstance()
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val todayZero: Long = calendar.timeInMillis+86400000
        return Date(todayZero)
    }
}