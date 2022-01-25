package com.lixiangya.cet46phrase

import android.app.Application
import android.util.Base64
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.gson.Gson
import com.lixiangya.cet46phrase.util.PreferencesUtil
import io.realm.Realm
import io.realm.RealmConfiguration
import java.security.SecureRandom
import java.util.*

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)

        initAdMod()
        val preference = PreferencesUtil.getSharePreferences(this)
        var dbKeyStr = preference.getString("db-secret-key", null)
        if (dbKeyStr == null) {
            val key = ByteArray(64)
            SecureRandom().nextBytes(key)
            dbKeyStr = Base64.encodeToString(key, Base64.DEFAULT)
            val edit = preference.edit()
            edit.putString("db-secret-key",dbKeyStr)
            edit.apply()
        }

        val realmConfig = RealmConfiguration.Builder()
            .name("databases.realm")
            .schemaVersion(1)
            .encryptionKey(Base64.decode(dbKeyStr!!,Base64.DEFAULT))
            .build()
        Realm.setDefaultConfiguration(realmConfig)
    }

    private fun initAdMod() {
//        Log.i("main","开始初始化广告")
//        val build = RequestConfiguration.Builder()
//            .setTestDeviceIds(Arrays.asList("C6C432BF02AF0624F1B24690DF3B51E0","745DCA27DECA48ABDF712821EFC57A3A")).build()
//        MobileAds.setRequestConfiguration(build)
//
//        val adRequest = AdRequest.Builder().build()
//        Log.i("main","测试设备？${adRequest.isTestDevice(this)}")
        MobileAds.initialize(this){
            Log.d("main", "initAdsense() called")
            Log.i("main", Gson().toJson(it.adapterStatusMap))
        }
    }
}