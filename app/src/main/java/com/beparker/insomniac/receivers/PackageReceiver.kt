package com.beparker.insomniac.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.beparker.insomniac.db.UpdateDbJobIntentService

class PackageReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && (intent?.action == Intent.ACTION_PACKAGE_ADDED || intent?.action == Intent.ACTION_PACKAGE_FULLY_REMOVED)) {
            UpdateDbJobIntentService.enqueueWork(context, intent)
        }
    }
}

fun Context.registerPackageReceiver(): PackageReceiver {
    val intentFilter = IntentFilter().apply {
        addAction(Intent.ACTION_PACKAGE_ADDED)
        addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED)
        addDataScheme("package")
    }
    val packageReceiver = PackageReceiver()
    registerReceiver(packageReceiver, intentFilter)
    return packageReceiver
}