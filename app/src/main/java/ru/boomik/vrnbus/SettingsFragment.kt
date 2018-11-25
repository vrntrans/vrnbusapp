package ru.boomik.vrnbus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.Toolbar
import androidx.preference.*
import ru.boomik.vrnbus.managers.SettingsManager

class SettingsFragment : PreferenceFragmentCompat(), androidx.preference.Preference.OnPreferenceChangeListener {
    override fun onPreferenceChange(preference: androidx.preference.Preference?, newValue: Any?): Boolean {
        if (preference==null) return false
        DataBus.sendEvent(DataBus.Settings, Pair(preference.key, newValue))
        if (newValue!=null) {
            @Suppress("IMPLICIT_CAST_TO_ANY") when (preference.key) {
                Consts.SETTINGS_NIGHT ->  DataBus.sendEvent(preference.key, newValue.toString())
                Consts.SETTINGS_REFERER -> DataBus.sendEvent(preference.key, newValue.toString())
                Consts.SETTINGS_ZOOM -> DataBus.sendEvent(preference.key, newValue as Boolean)
                else -> return true
            }
        }
        return true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_container, container, false)
        val fragmentContainer = rootView.findViewById<FrameLayout>(R.id.container)
        val view = super.onCreateView(inflater, null, savedInstanceState)
        fragmentContainer.addView(view)

        val toolbar = rootView.findViewById<Toolbar>(R.id.toolbar)

        toolbar.title = context?.getString(R.string.action_settings)

        toolbar.setNavigationOnClickListener {
            val manager = activity?.supportFragmentManager
            manager?.beginTransaction()?.remove(this)?.commit()
            manager?.popBackStack()
        }
        return rootView
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, null)

        val nightMode = preferenceScreen.findPreference(Consts.SETTINGS_NIGHT) as androidx.preference.ListPreference
        nightMode.value = SettingsManager.instance.getString(Consts.SETTINGS_NIGHT)
        nightMode.onPreferenceChangeListener = this

        preferenceScreen.findPreference(Consts.SETTINGS_REFERER).onPreferenceChangeListener = this

        val zoom = preferenceScreen.findPreference(Consts.SETTINGS_ZOOM) as androidx.preference.SwitchPreference
        zoom.isChecked = SettingsManager.instance.getBool(Consts.SETTINGS_ZOOM)
        zoom.onPreferenceChangeListener = this

    }

}
