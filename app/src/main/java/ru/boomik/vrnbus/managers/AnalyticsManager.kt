package ru.boomik.vrnbus.managers

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import ru.boomik.vrnbus.DataBus


object AnalyticsManager {
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

    fun initByActivity(activity: Activity) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity)
        subscribeEvents()
    }

    private fun subscribeEvents() {
        DataBus.subscribe<Pair<String, Any?>>(DataBus.Settings) {
            logPreference(it.data.first, it.data.second)
        }
    }

    private fun logPreference(preferenceName: String, data: Any?) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, preferenceName)
        bundle.putString(FirebaseAnalytics.Param.VALUE, data?.toString())
        mFirebaseAnalytics.logEvent("preference", bundle)
        mFirebaseAnalytics.setUserProperty(preferenceName, data?.toString())
    }

    fun logPermission(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
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
            mFirebaseAnalytics.setUserProperty(name, statusName)
        }
    }

    fun logScreen(screen: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.VALUE, screen)
        mFirebaseAnalytics.logEvent("screen", bundle)
    }

    fun logEvent(event: String, value : String?=null, map : Map<String, String>? = null) {
        val bundle = Bundle()
        if(value!=null)
            bundle.putString(FirebaseAnalytics.Param.VALUE, value)
        map?.forEach {
            bundle.putString(it.key, it.value)
        }
        mFirebaseAnalytics.logEvent(event, bundle)
    }
}