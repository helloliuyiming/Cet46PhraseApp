package com.lixiangya.cet46phrase.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.lixiangya.cet46phrase.entity.Phrase
import com.lixiangya.cet46phrase.util.AppDatabase
import com.google.gson.Gson
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.InputStreamReader

class FragmentLoadDataViewModel(application: Application) : AndroidViewModel(application) {

    var count: Int = 0
    private val context = application
    val phraseDao = AppDatabase.getInstance(context).phraseDao()
    val gson = Gson()

    fun load(): Observable<Int> {
        return Observable.create<Int>(ObservableOnSubscribe {
            Log.i("main", "load()")
            val inputStream = context.assets.open("phrases.json")
            val isr = InputStreamReader(inputStream)
            isr.readLines().forEachIndexed { index, line ->
                count++
                Log.i("main", "当前进度：$index")
                Log.i("main", "line=$line")
                val phrase = gson.fromJson(line, Phrase::class.java)
                phraseDao.insert(phrase)
                it.onNext(index)
            }
            Log.i("main", "load done")
            inputStream.close()
            it.onComplete()
        }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}