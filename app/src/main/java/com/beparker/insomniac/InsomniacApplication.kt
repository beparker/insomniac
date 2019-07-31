package com.beparker.insomniac

import android.app.Application
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.beparker.insomniac.db.PackageDatabase
import com.beparker.insomniac.receivers.registerBatteryReceiver
import com.beparker.insomniac.receivers.registerPackageReceiver
import com.facebook.stetho.Stetho
import org.jetbrains.anko.intentFor

lateinit var packageDatabase: PackageDatabase

class InsomniacApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        ContextCompat.startForegroundService(this, intentFor<AppMonitorService>())
        registerBatteryReceiver()
        registerPackageReceiver()

        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this)
        }

        packageDatabase = Room.databaseBuilder(
            applicationContext,
            PackageDatabase::class.java, "package-db"
        ).build()
    }
}