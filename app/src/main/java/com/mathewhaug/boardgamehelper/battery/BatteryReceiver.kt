package com.mathewhaug.boardgamehelper.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// registered in code by the Home Route and never in the manifest, so it only exists while Home
// is on screen. the callback keeps it free of any ViewModel, it just reports low or okay
class BatteryReceiver(private val onLowBatteryChanged: (Boolean) -> Unit) : BroadcastReceiver() {

    // the filter should already keep everything else out, but the action is checked anyway so a
    // stray or spoofed intent with some other action is ignored rather than read as "okay"
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BATTERY_LOW -> onLowBatteryChanged(true)
            Intent.ACTION_BATTERY_OKAY -> onLowBatteryChanged(false)
        }
    }
}
