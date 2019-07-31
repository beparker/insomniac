package com.beparker.insomniac

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.usage.UsageEvents
import android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import com.beparker.insomniac.db.PackageViewModel
import com.beparker.insomniac.receivers.*
import io.reactivex.android.schedulers.AndroidSchedulers
import org.jetbrains.anko.doFromSdk
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

private const val TAG = "AppMonitorService"


class AppMonitorService : LifecycleService() {

    companion object {
        var running = false
    }

    private lateinit var broadcastReceiver: BroadcastReceiver
    private val keyboardApps = mutableSetOf<String>()

    private var currentApp: String? = null

    private var currentAppService: ScheduledExecutorService? = null

    private val insomniacConsumer = object : InsomniacEventConsumer {
        override fun accept(v: InsomniacEvent) {
            when (v) {
                ScreenOffEvent -> screenOff()
                ScreenOnEvent -> screenOn()
                DischargeEvent -> powerDischarge()
                ChargeEvent -> checkCurrentAppAgainstAppWakeList()
            }
        }
    }

    private lateinit var viewModel: PackageViewModel

    private val packages = mutableListOf<String>()

    override fun onCreate() {
        super.onCreate()

        running = true

        doFromSdk(Build.VERSION_CODES.O) {
            createChannel()
        }

        viewModel = PackageViewModel()

        rxEventBus.subscribe(
            AndroidSchedulers.mainThread(),
            AndroidSchedulers.mainThread(),
            insomniacConsumer
        )
        broadcastReceiver = registerScreenOnOffReceiver()
        startForeground(
            1,
            NotificationCompat.Builder(this, getString(R.string.notification_channel))
                .setContentTitle(getString(R.string.app_watch_service))
                .setSmallIcon(R.mipmap.ic_launcher)
                .build()
        )
        populateKeyboardApps()
        if (isScreenOn()) screenOn()

        viewModel.getEnabledPackages().observe(this, Observer {
            currentApp = null
            packages.clear()
            packages.addAll(it.map { p -> p.name })
        })
    }

    private fun powerDischarge() {
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    private fun isScreenOn(): Boolean {
        val powerManager = getSystemService(POWER_SERVICE) as? PowerManager
        return powerManager != null && powerManager.isInteractive
    }

    @SuppressLint("WakelockTimeout")
    private fun checkCurrentAppAgainstAppWakeList() {
        if (currentApp in packages) {
            if (!wakeLock.isHeld && charging) {
                Log.d(TAG, "Acquiring wakelock because of $currentApp")
                wakeLock.acquire()
            }
        } else if (wakeLock.isHeld) {
            Log.d(TAG, "Releasing wakelock")
            wakeLock.release()
        }
    }

    @Suppress("DEPRECATION")
    private val wakeLock: PowerManager.WakeLock by lazy {
        (getSystemService(POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.FULL_WAKE_LOCK, "Insomniac::InsomniacWakelockTag")
        }
    }

    private fun populateKeyboardApps() {
        val inputMethodManager =
            applicationContext.getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager ?: return
        val enabledInputMethodList = inputMethodManager.enabledInputMethodList
        enabledInputMethodList.forEach {
            for (i in 0 until it.subtypeCount) {
                if (it.getSubtypeAt(i).mode.contains("keyboard")) {
                    keyboardApps.add(it.packageName)
                }
            }
        }
    }

    private fun startPackageScan() {
        if (!isScreenOn()) return
        if (hasUsageStatsPermission()) {
            val currentTimeMillis = System.currentTimeMillis()
            val usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as? UsageStatsManager ?: return

            val queryEvents = usageStatsManager.queryEvents(currentTimeMillis - 20000, currentTimeMillis)
            val event = UsageEvents.Event()
            var packageName = ""
            var className = ""
            while (queryEvents.hasNextEvent()) {
                queryEvents.getNextEvent(event)
                if (event.eventType == MOVE_TO_FOREGROUND) {
                    packageName = event.packageName
                    className = event.className
                }
            }
            if (packageName.isNotEmpty()) {
                if (currentApp == packageName) return
                if (packageName == "com.android.systemui") return
                if (keyboardApps.contains(packageName)) return
                try {
                    packageManager.getActivityInfo(ComponentName(packageName, className), 0)
                } catch (e: PackageManager.NameNotFoundException) {
                    Log.e(TAG, "Package $packageName not found", e)
                    return
                }
                currentApp = packageName
                checkCurrentAppAgainstAppWakeList()
            }
        }
    }


    fun screenOn() {
        currentAppService = Executors.newSingleThreadScheduledExecutor()
        currentAppService?.scheduleWithFixedDelay({ startPackageScan() }, 0, 10, TimeUnit.SECONDS)
    }

    fun screenOff() {
        currentAppService?.shutdownNow()
    }

    @TargetApi(Build.VERSION_CODES.O)
    fun createChannel() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannel = NotificationChannel(
            getString(R.string.notification_channel),
            getString(R.string.notification_channel),
            IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(notificationChannel)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (wakeLock.isHeld) wakeLock.release()
        unregisterReceiver(broadcastReceiver)
        rxEventBus.unsubscribe(insomniacConsumer)
        currentAppService?.shutdownAndAwaitTermination()
        running = false
    }

    private fun ExecutorService.shutdownAndAwaitTermination() {
        if (isShutdown) return
        shutdown()
        try {
            if (!awaitTermination(60, TimeUnit.SECONDS)) {
                shutdownNow()
            }
        } catch (unused: InterruptedException) {
            shutdownNow()
            Thread.currentThread().interrupt()
        }
    }
}
