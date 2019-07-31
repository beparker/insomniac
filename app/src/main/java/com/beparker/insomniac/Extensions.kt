package com.beparker.insomniac

import android.content.Context
import android.content.Intent
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import com.beparker.insomniac.db.Package

fun Disposable.addTo(disposable: CompositeDisposable) {
    disposable.add(this)
}

fun Context.loadInstalledApps() {
    packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER), 0)
        .mapNotNull {
            Package(
                it.activityInfo.applicationInfo.packageName,
                packageManager.getApplicationLabel(it.activityInfo.applicationInfo)?.toString()
            )
        }.apply {
            packageDatabase.packageDao().insertAll(this)
        }
}