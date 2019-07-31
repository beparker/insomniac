package com.beparker.insomniac.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.beparker.insomniac.InsomniacEvent
import com.beparker.insomniac.rxEventBus

var charging = false

object DischargeEvent : InsomniacEvent

object ChargeEvent : InsomniacEvent

class BatteryReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent?.action == Intent.ACTION_BATTERY_CHANGED) {
            val status: Int = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            charging = status == BatteryManager.BATTERY_STATUS_CHARGING
                    || status == BatteryManager.BATTERY_STATUS_FULL
            if (charging) {
                rxEventBus.post(ChargeEvent)
            } else {
                rxEventBus.post(DischargeEvent)
            }
        }
    }
}

fun Context.registerBatteryReceiver(): BatteryReceiver {
    val intentFilter = IntentFilter().apply {
        addAction(Intent.ACTION_BATTERY_CHANGED)
    }
    val batteryReceiver = BatteryReceiver()
    registerReceiver(batteryReceiver, intentFilter)
    return batteryReceiver
}