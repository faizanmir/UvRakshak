package avishkaar.com.uv_rakshak.singletons

import android.content.IntentFilter
import avishkaar.com.uv_rakshak.constants.Constants
import avishkaar.com.uv_rakshak.helpers.CountdownCounter
import avishkaar.com.uv_rakshak.receivers.NotificationBroadcastReceiver

object Singleton {
    val countdownCounter = CountdownCounter((Constants.MINUTES *60 *1000).toLong(),1000)
    val notificationBroadcastReceiver  =  NotificationBroadcastReceiver()
}