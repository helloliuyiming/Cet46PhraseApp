package com.lixiangya.cet46phrase.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class PreferencesUtil {
    companion object {
        private var preferences: SharedPreferences? = null
        private const val fileName = "config"

        fun getSharePreferences(context: Context): SharedPreferences {
            if (preferences == null) {
                preferences = createSecurityPreference(context)
            }
            return preferences!!
        }

        private fun createSecurityPreference(context: Context): SharedPreferences {
            val mainKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            return EncryptedSharedPreferences.create(context,
                fileName,mainKey,EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
        }
    }
}