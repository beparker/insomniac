package com.beparker.insomniac

import android.Manifest
import android.app.AppOpsManager
import android.app.Service
import android.content.Context
import android.content.pm.PackageManager
import android.os.Process

fun Context.hasUsageStatsPermission(): Boolean {
    val appOps = getSystemService(Service.APP_OPS_SERVICE) as? AppOpsManager
    val mode = appOps?.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName
    )

    return if (mode == AppOpsManager.MODE_DEFAULT) {
        checkCallingOrSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED
    } else {
        mode == AppOpsManager.MODE_ALLOWED
    }
}