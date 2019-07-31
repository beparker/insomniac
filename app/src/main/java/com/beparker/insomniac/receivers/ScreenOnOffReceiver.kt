package com.beparker.insomniac.receivers

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.KEYGUARD_SERVICE
import android.content.Intent
import android.content.IntentFilter
import com.beparker.insomniac.InsomniacEvent
import com.beparker.insomniac.rxEventBus

object ScreenOnEvent : InsomniacEvent

object ScreenOffEvent : InsomniacEvent

class ScreenOnOffReceiver : BroadcastReceiver() {

    private var pendingUnlock = false

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == null) return
        val action = intent.action
        if (action == Intent.ACTION_USER_PRESENT) {
            val keyguardManager = context?.getSystemService(KEYGUARD_SERVICE) as? KeyguardManager
            if (keyguardManager == null || (!keyguardManager.isDeviceLocked && !keyguardManager.isKeyguardLocked)) {
                rxEventBus.post(ScreenOnEvent)
            } else {
                pendingUnlock = true
            }
        } else if (action == Intent.ACTION_SCREEN_ON) {
            if (pendingUnlock) {
                rxEventBus.post(ScreenOnEvent)
            }
            this.pendingUnlock = false
        } else if (action == Intent.ACTION_SCREEN_OFF) {
            rxEventBus.post(ScreenOffEvent)
            pendingUnlock = false
        }
    }
}

fun Context.registerScreenOnOffReceiver(): ScreenOnOffReceiver {
    val intentFilter = IntentFilter().apply {
        addAction(Intent.ACTION_SCREEN_ON)
        addAction(Intent.ACTION_SCREEN_OFF)
        addAction(Intent.ACTION_USER_PRESENT)
    }
    val screenOnOffReceiver = ScreenOnOffReceiver()
    registerReceiver(screenOnOffReceiver, intentFilter)
    return screenOnOffReceiver
}