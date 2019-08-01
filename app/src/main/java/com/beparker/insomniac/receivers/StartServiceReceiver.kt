package com.beparker.insomniac.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.beparker.insomniac.AppMonitorService
import org.jetbrains.anko.intentFor

class StartServiceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && (intent?.action == Intent.ACTION_BOOT_COMPLETED || intent?.action == Intent.ACTION_MY_PACKAGE_REPLACED)) {
            ContextCompat.startForegroundService(context, context.intentFor<AppMonitorService>())
        }
    }
}