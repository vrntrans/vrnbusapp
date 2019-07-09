package ru.boomik.vrnbus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.Toolbar
import androidx.preference.*
import ru.boomik.vrnbus.managers.AnalyticsManager
import ru.boomik.vrnbus.managers.SettingsManager

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {
    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        if (preference==null) return false
        DataBus.sendEvent(DataBus.Settings, Pair(preference.key, newValue))
        if (newValue!=null) {
            when (newValue) {
                is String -> DataBus.sendEvent(preference.key, newValue)
                is Boolean -> DataBus.sendEvent(preference.key, newValue)
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
        AnalyticsManager.logScreen("Settings")
        return rootView
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, null)

        val nightMode = preferenceScreen.findPreference(Consts.SETTINGS_NIGHT) as ListPreference
        nightMode.value = SettingsManager.getString(Consts.SETTINGS_NIGHT)
        nightMode.onPreferenceChangeListener = this

        preferenceScreen.findPreference(Consts.SETTINGS_REFERER).onPreferenceChangeListener = this

        val zoom = preferenceScreen.findPreference(Consts.SETTINGS_ZOOM) as SwitchPreference
        zoom.isChecked = SettingsManager.getBool(Consts.SETTINGS_ZOOM)
        zoom.onPreferenceChangeListener = this

        val osm = preferenceScreen.findPreference(Consts.SETTINGS_OSM) as SwitchPreference
        osm.isChecked = SettingsManager.getBool(Consts.SETTINGS_OSM)
        osm.onPreferenceChangeListener = this

        val analytics = preferenceScreen.findPreference(Consts.SETTINGS_ANALYTICS) as SwitchPreference
        analytics.isChecked = SettingsManager.getBool(Consts.SETTINGS_ANALYTICS)
        analytics.onPreferenceChangeListener = this

    }

}
