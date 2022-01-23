package com.lixiangya.cet46phrase

import android.app.Application
import android.util.Base64
import com.lixiangya.cet46phrase.util.PreferencesUtil
import io.realm.Realm
import io.realm.RealmConfiguration
import java.security.SecureRandom

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)

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
}