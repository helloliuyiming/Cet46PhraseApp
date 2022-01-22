package com.lixiangya.cet46phrase.fragment

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.lixiangya.cet46phrase.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}