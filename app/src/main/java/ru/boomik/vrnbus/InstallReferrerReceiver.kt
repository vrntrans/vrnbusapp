package ru.boomik.vrnbus

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ru.boomik.vrnbus.managers.AnalyticsManager

class InstallReferrerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val referrer = intent?.getStringExtra("referrer")
        if (referrer.isNullOrBlank()) AnalyticsManager.logEvent("referrer", referrer)
    }
}