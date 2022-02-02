package com.lixiangya.cet46phrase.viewmodel

import android.app.Application
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import cn.hutool.crypto.symmetric.AES
import com.lixiangya.cet46phrase.entity.Phrase
import com.google.gson.Gson
import com.lixiangya.cet46phrase.util.PreferencesUtil
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import io.reactivex.rxjava3.schedulers.Schedulers
import io.realm.Realm
import java.io.InputStreamReader

class FragmentLoadDataViewModel(application: Application) : AndroidViewModel(application) {

    var count: Int = 0
    private val context = application
    val gson = Gson()

    fun load(): Observable<Int> {
        return Observable.create<Int> {
            val realm = Realm.getDefaultInstance()
            val sharePreferences = PreferencesUtil.getSharePreferences(context)
            val secretKey = sharePreferences.getString("secretKey", null)
            val inputStream = context.assets.open("DejNBqfXGyGaDGYl")
            val isr = InputStreamReader(inputStream)
            val aes = AES(Base64.decode(secretKey,Base64.DEFAULT))
            realm.beginTransaction()
            isr.readLines().forEachIndexed { index, line ->
                count++
                val plainText = String(aes.decrypt(line))
                val phrase = gson.fromJson(plainText, Phrase::class.java)
                phrase.id = index
                realm.insertOrUpdate(phrase)
                it.onNext(index)
            }
            realm.commitTransaction()
            realm.close()
            inputStream.close()
            it.onComplete()
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}