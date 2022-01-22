package com.lixiangya.cet46phrase

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.MobileAds
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MobileAds.initialize(this){
            Log.d("main", "initAdsense() called")
            Log.i("main",it.adapterStatusMap.toString())
        }

        BuildConfig.APPLICATION_ID


        AppCenter.start(
            application, "65bdda75-3028-4229-9956-be5cf3299805",
            Analytics::class.java, Crashes::class.java
        )


    }
}