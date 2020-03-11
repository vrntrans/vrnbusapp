package ru.boomik.vrnbus

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.Toolbar
import androidx.preference.*
import ru.boomik.vrnbus.dialogs.alertQuestion
import ru.boomik.vrnbus.managers.AnalyticsManager
import ru.boomik.vrnbus.managers.DataManager
import ru.boomik.vrnbus.managers.SettingsManager

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {
    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        if (preference==null) return false

        if (preference.key == Consts.SETTINGS_ANALYTICS && newValue is Boolean && newValue) {
            alertQuestion(activity!!, "Отключить аналитику?", "Запретить приложению собирать данные об использовании. Собираемые данные обезличены и необходимы для улучшения приложения.\nПросмотреть политику конфиденциальности можно через боковое меню.\nПри переключении параметра приложение будет перезапущено"
                    , "Отключить", "Не отключать") {
                if (!it) return@alertQuestion
                processPreference(preference, newValue)
                val analytics = preferenceScreen.findPreference<SwitchPreference>(Consts.SETTINGS_ANALYTICS as CharSequence)
                analytics?.isChecked = true
            }
            return false
        }
        return processPreference(preference, newValue)
    }

    fun processPreference(preference: Preference, newValue: Any?) : Boolean{
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

        val nightMode = preferenceScreen.findPreference<ListPreference>(Consts.SETTINGS_NIGHT as CharSequence)
        nightMode?.value = SettingsManager.getString(Consts.SETTINGS_NIGHT)
        nightMode?.onPreferenceChangeListener = this

        preferenceScreen.findPreference<Preference>(Consts.SETTINGS_REFERER)?.onPreferenceChangeListener = this

        val zoom = preferenceScreen.findPreference<SwitchPreference>(Consts.SETTINGS_ZOOM as CharSequence)
        zoom?.isChecked = SettingsManager.getBool(Consts.SETTINGS_ZOOM)
        zoom?.onPreferenceChangeListener = this

        val osm = preferenceScreen.findPreference<SwitchPreference>(Consts.SETTINGS_OSM as CharSequence)
        osm?.isChecked = SettingsManager.getBool(Consts.SETTINGS_OSM)
        osm?.onPreferenceChangeListener = this

        val rotation = preferenceScreen.findPreference<SwitchPreference>(Consts.SETTINGS_ROTATE as CharSequence)
        rotation?.isChecked = SettingsManager.getBool(Consts.SETTINGS_ROTATE)
        rotation?.onPreferenceChangeListener = this

        val analytics = preferenceScreen.findPreference<SwitchPreference>(Consts.SETTINGS_ANALYTICS as CharSequence)
        analytics?.isChecked = SettingsManager.getBool(Consts.SETTINGS_ANALYTICS)
        analytics?.onPreferenceChangeListener = this

        val cache = preferenceScreen.findPreference<Preference>(Consts.SETTINGS_CACHE as CharSequence)
        cache?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            clearCache()
            true
        }

    }

    private fun clearCache() {
        DataManager.prepareForReload()
        activity?.finish()
        context?.startActivity( Intent(context, MapsActivity::class.java))
    }

}
