package avishkaar.com.uv_rakshak.helpers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import avishkaar.com.uv_rakshak.activities.MainActivity
import avishkaar.com.uv_rakshak.receivers.NotificationBroadcastReceiver
import avishkaar.com.uv_rakshak.R
import avishkaar.com.uv_rakshak.constants.Constants.Companion.BATTERY_DRAINED
import avishkaar.com.uv_rakshak.constants.Constants.Companion.DEVICE_CONNECTED
import avishkaar.com.uv_rakshak.constants.Constants.Companion.DISINFECTION_COMPLETE
import avishkaar.com.uv_rakshak.constants.Constants.Companion.DISINFECTION_IN_PROGRESS
import avishkaar.com.uv_rakshak.constants.Constants.Companion.LOW_RSSI
import avishkaar.com.uv_rakshak.constants.Constants.Companion.ON_DONE
import avishkaar.com.uv_rakshak.constants.Constants.Companion.OUT_OF_RANGE
import avishkaar.com.uv_rakshak.constants.Constants.Companion.STOP_DISINFECTION
import avishkaar.com.uv_rakshak.constants.Constants.Companion.SWITCH_TO_AUTO
import avishkaar.com.uv_rakshak.services.BleService
import java.lang.Exception

class NotificationHelper(var context: Context) :
    NotificationBroadcastReceiver.NotificationChangeListener{
    var mListener:OnNotificationChangeListener? =  null
    init {
        try {
            mListener = context as OnNotificationChangeListener
        }catch (e:Exception)
        {
            e.printStackTrace()
        }
    }


    interface OnNotificationChangeListener{
        fun onDone()
        fun startAutoMode()
        fun shutdownService()
        fun disconnect()
        fun onDeviceOutOfRange()
        fun onAutoModeChosen()
        fun onDisinfectionProcessStarted()
        fun onDisinfectionComplete()
        fun onBatteryLow()
        fun onDeviceRSSIlow()
        fun onDisconnectionInProgress()
        fun stopDisinfection()


    }


     fun notificationBuilder(
        title :String = "Uv Rakshak",
        contentText: String
        ,supportsAction: Boolean,
        icon: Int,
        action: String?,
        hasProgressBar: Boolean,
        descriptionTextForAction: String?


    ): Notification {



        val notificationBuilder = NotificationCompat.Builder(context, BleService.CHANNEL_ID)


        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val activityPendingIntent: PendingIntent =
            PendingIntent.getActivity(context, 0, activityIntent, 0)



        if (supportsAction) {
            when (action) {
                DEVICE_CONNECTED -> {
                    notificationBuilder.addAction(
                        icon, descriptionTextForAction, makePendingIntent(
                            DISINFECTION_IN_PROGRESS
                        )
                    )
                }
                DISINFECTION_IN_PROGRESS -> {
                    notificationBuilder.addAction(
                        icon, descriptionTextForAction, makePendingIntent(
                            DISINFECTION_COMPLETE
                        )
                    )
                    notificationBuilder.setProgress(0, 100, true)
                }
                DISINFECTION_COMPLETE -> {
                    notificationBuilder.addAction(
                        icon, descriptionTextForAction, makePendingIntent(
                            STOP_DISINFECTION
                        )
                    )

                }
                LOW_RSSI -> {
                    notificationBuilder.addAction(
                        icon, descriptionTextForAction, makePendingIntent(
                            SWITCH_TO_AUTO
                        )
                    )
                }
                OUT_OF_RANGE -> {
                    notificationBuilder.addAction(
                        icon, descriptionTextForAction, makePendingIntent(
                            SWITCH_TO_AUTO
                        )
                    )
                }
                STOP_DISINFECTION -> {
                    notificationBuilder.addAction(
                        icon, descriptionTextForAction, makePendingIntent(
                            ON_DONE
                        )
                    )

                }

            }
        }

        notificationBuilder.apply {
            setContentTitle(title)
            setContentText(contentText)
            priority = NotificationCompat.PRIORITY_DEFAULT
            setContentIntent(activityPendingIntent)
            color = context.resources.getColor(android.R.color.holo_blue_bright,null)
            setSmallIcon(R.drawable.ic_android_black_24dp)

        }
        return notificationBuilder.build()

    }


    private fun makePendingIntent(action: String):PendingIntent{
            val intent =  Intent(context, NotificationBroadcastReceiver::class.java).apply { this.action  =  action }
            return PendingIntent.getBroadcast(context,0,intent,0)
        }





    fun createNotificationChannel(
        channelId: String,
        name: String,
        descriptionText: String = "channel"
    ): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        return channelId
    }





    private fun showNotification(notification:Notification)
    {
        with(NotificationManagerCompat.from(context))
        {
            notify(BleService.NOTIFICATION_ID,notification)
        }
    }





    override fun changeNotificationAndPerformAction(action: String) {
        Log.e("Change Notification",action)
        when(action)
        {
            DEVICE_CONNECTED -> {
                showNotification(
                    notificationBuilder(
                        contentText = "Device Connected",
                        supportsAction = true,
                        hasProgressBar = false,
                        descriptionTextForAction = "Start Disinfection",
                        action = DEVICE_CONNECTED,
                        icon = R.drawable.ic_android_black_24dp)
                )

            }
            DISINFECTION_IN_PROGRESS -> {
                showNotification(
                    notificationBuilder(
                        contentText = "Disinfection in progress",
                        supportsAction = true,
                        hasProgressBar = true,
                        descriptionTextForAction = "Stop Disinfection",
                        action = DISINFECTION_IN_PROGRESS,
                        icon = R.drawable.ic_android_black_24dp)
                )
                mListener?.onDisconnectionInProgress()
            }
            DISINFECTION_COMPLETE -> {
                showNotification(
                    notificationBuilder(
                        contentText = "Disinfection Complete",
                        supportsAction = true,
                        hasProgressBar = false,
                        descriptionTextForAction = "Disconnect from device",
                        action = DISINFECTION_COMPLETE,
                        icon = R.drawable.ic_android_black_24dp)
                )
                mListener?.onDisinfectionComplete()
            }
            LOW_RSSI -> {
                showNotification(
                    notificationBuilder(
                        contentText = "Device is going out of range",
                        supportsAction = true,
                        hasProgressBar = false,
                        descriptionTextForAction = "Switch to Auto mode",
                        action = LOW_RSSI,
                        icon = R.drawable.ic_android_black_24dp)
                )
                mListener?.onDeviceRSSIlow()
            }
            OUT_OF_RANGE -> {
                showNotification(
                    notificationBuilder(
                        contentText = "Bot out of Range",
                        supportsAction = true,
                        hasProgressBar = false,
                        descriptionTextForAction = "Switch to Auto mode",
                        action = OUT_OF_RANGE,
                        icon = R.drawable.ic_android_black_24dp)
                )
                mListener?.onDeviceOutOfRange()
            }


            BATTERY_DRAINED -> {
                showNotification(
                    notificationBuilder(
                        contentText = "Battery Drained",
                        supportsAction = false,
                        hasProgressBar = false,
                        descriptionTextForAction = null,
                        action =BATTERY_DRAINED,
                        icon = R.drawable.ic_android_black_24dp)
                )
                mListener?.onBatteryLow()
            }

            STOP_DISINFECTION -> {
                showNotification(
                    notificationBuilder(
                        contentText = "Disinfection stopped",
                        supportsAction = true,
                        hasProgressBar = false,
                        descriptionTextForAction = "Ok",
                        action = STOP_DISINFECTION,
                        icon = R.drawable.ic_android_black_24dp)
                )
                mListener?.stopDisinfection()
            }


            SWITCH_TO_AUTO -> {
                showNotification(
                    notificationBuilder(
                        contentText = "Auto mode enabled",
                        supportsAction = false,
                        hasProgressBar = false,
                        descriptionTextForAction = null,
                        action = SWITCH_TO_AUTO,
                        icon = R.drawable.ic_android_black_24dp)
                )
                mListener?.startAutoMode()
            }


            ON_DONE -> {
                showNotification(
                    notificationBuilder(
                        contentText = "Done",
                        supportsAction = false,
                        hasProgressBar = false,
                        descriptionTextForAction = "Ok",
                        action = ON_DONE,
                        icon = R.drawable.ic_android_black_24dp)
                    //kill service here

                )
                mListener?.onDone()
            }

        }
    }


}