package ru.boomik.vrnbus.managers

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import ru.boomik.vrnbus.Consts
import ru.boomik.vrnbus.DataBus


object AnalyticsManager {
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

    fun initByActivity(activity: Activity, disabled: Boolean) {
        isDisabled = disabled
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity)
        subscribeEvents()
    }

    private var isDisabled : Boolean = false

    private fun subscribeEvents() {
        DataBus.subscribe<Pair<String, Any?>>(DataBus.Settings) {
            if (it.data.first == Consts.SETTINGS_ANALYTICS) {
                isDisabled = it.data.second as Boolean
            }
            logPreference(it.data.first, it.data.second)
        }
    }

    private fun logPreference(preferenceName: String, data: Any?) {
        if (isDisabled) return
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, preferenceName)
        bundle.putString(FirebaseAnalytics.Param.VALUE, data?.toString())
        mFirebaseAnalytics.logEvent("preference", bundle)
        mFirebaseAnalytics.setUserProperty("preference", data?.toString())
    }

    fun logPermission(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (isDisabled) return
        permissions.forEachIndexed { id, name ->
            val status = grantResults[0]
            val statusName = when (status) {
                PackageManager.PERMISSION_GRANTED -> "Granted"
                PackageManager.PERMISSION_DENIED -> "Denied"
                else -> "Unknown"
            }
            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name)
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, requestCode.toString())
            bundle.putString(FirebaseAnalytics.Param.VALUE, statusName)
            mFirebaseAnalytics.logEvent("permission", bundle)
            mFirebaseAnalytics.setUserProperty("permission", statusName)
        }
    }

    fun logScreen(screen: String) {
        if (isDisabled) return
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.VALUE, screen)
        mFirebaseAnalytics.logEvent("screen", bundle)
    }

    fun logEvent(event: String, value: String? = null, map: Map<String, String>? = null) {
        if (isDisabled) return
        val bundle = Bundle()
        if (value != null)
            bundle.putString(FirebaseAnalytics.Param.VALUE, value)
        map?.forEach {
            bundle.putString(it.key, it.value)
        }
        mFirebaseAnalytics.logEvent(event, bundle)
    }
}