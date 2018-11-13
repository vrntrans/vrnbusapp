package ru.boomik.vrnbus

import android.os.Bundle
import moe.shizuku.preference.EditTextPreference
import moe.shizuku.preference.Preference
import moe.shizuku.preference.PreferenceFragment
import moe.shizuku.preference.SwitchPreference

class SettingsFragment : PreferenceFragment(), Preference.OnPreferenceChangeListener {
    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {

        return true
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val zoomButtons = SwitchPreference(context)
        zoomButtons.title = "Кнопки масштабирования"

        val referer = EditTextPreference(context)
        referer.text = context?.getString(R.string.Referer)
        referer.summary = context?.getString(R.string.enter_referer)

        preferenceScreen.addPreference(zoomButtons)
        preferenceScreen.addPreference(referer)

        zoomButtons.onPreferenceChangeListener = this
        referer.onPreferenceChangeListener = this
    }

}
