package com.beparker.insomniac.db

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_PACKAGE_ADDED
import android.content.Intent.ACTION_PACKAGE_FULLY_REMOVED
import androidx.core.app.JobIntentService
import com.beparker.insomniac.loadInstalledApps
import com.beparker.insomniac.packageDatabase

class UpdateDbJobIntentService : JobIntentService() {

    companion object {
        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, UpdateDbJobIntentService::class.java, 2002, work)
        }
    }

    override fun onHandleWork(intent: Intent) {
        val pkg = intent.data?.schemeSpecificPart ?: return
        if (intent.action == ACTION_PACKAGE_ADDED) {
            loadInstalledApps()
        } else if (intent.action == ACTION_PACKAGE_FULLY_REMOVED) {
            packageDatabase.packageDao().delete(pkg)
        }
    }
}