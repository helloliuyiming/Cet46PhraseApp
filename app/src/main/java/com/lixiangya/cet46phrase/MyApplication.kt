package com.lixiangya.cet46phrase

import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import android.util.Base64
import android.util.Log
import android.widget.Toast
import cn.hutool.crypto.digest.MD5
import com.google.android.gms.ads.MobileAds
import com.google.gson.Gson
import com.lixiangya.cet46phrase.util.PreferencesUtil
import io.realm.Realm
import io.realm.RealmConfiguration
import java.lang.StringBuilder
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*
import kotlin.math.log

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)

        val key = "secretKey"
        val sharePreferences = PreferencesUtil.getSharePreferences(this)
        if (sharePreferences.getString(key, null) == null) {
            val loadKey = loadKey()
            val edit = sharePreferences.edit()
            edit.putString(key,loadKey)
            edit.apply()
        }

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

        val packageManager = this.packageManager
        val packageName = packageName
        Log.i("main","packageName:$packageName")
        val signature: Signature
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val packageInfo =
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            if (packageInfo?.signingInfo == null) {
                throw Exception("应用签名信息错误")
            }
            signature = packageInfo.signingInfo.apkContentsSigners[0]
        }else{
            @SuppressLint("PackageManagerGetSignatures")
            val packageInfo =
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            signature = packageInfo.signatures[0]
        }
        val signatureByteArray = signature.toByteArray()
        Log.i("main","signaturePlainText = ${String(signatureByteArray)}")
        val mD5 = MD5.create()
        val digestHex16 = mD5.digestHex16(signatureByteArray)
        Log.i("main","app signature md5 = $digestHex16")
        Toast.makeText(this,"app signature md5 = $digestHex16",Toast.LENGTH_LONG).show()

        val md: MessageDigest = MessageDigest.getInstance("SHA1")
        val publicKey: ByteArray = md.digest(signatureByteArray)
        val hexString = StringBuilder()
        for (i in publicKey.indices) {
            val appendString = Integer.toHexString(0xFF and publicKey[i].toInt()).uppercase(Locale.US)
            if (appendString.length == 1) hexString.append("0")
            hexString.append(appendString)
            hexString.append(":")
        }
        Log.i("main","app signature sha1 = $hexString")
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

    external fun loadKey():String

    companion object{
        init {
            System.loadLibrary("cet46phrase");
        }
    }
}