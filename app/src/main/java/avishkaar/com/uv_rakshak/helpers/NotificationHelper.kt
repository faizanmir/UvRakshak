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
import avishkaar.com.uv_rakshak.constants.Constants.Companion.SWITCH_TO_MANUAL_MODE
import avishkaar.com.uv_rakshak.services.BleService
import java.lang.Exception

class NotificationHelper(var context: Context) :
    NotificationBroadcastReceiver.NotificationChangeListener{

    var mListener:OnNotificationChangeListener? =  null

    var countdownProgress :Int =  0;

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
        fun startAutoMode()//change name of this function
        fun shutdownService()
        fun disconnect()
        fun onDeviceOutOfRange()
        fun onDisinfectionProcessStarted()
        fun onDisinfectionComplete()
        fun onBatteryLow()
        fun onDeviceRSSIlow()
        fun onDisnfectionInProgress()
        fun stopDisinfection()
        fun startManualMode()

        //change name of this function



    }


    /**
    * This method builds the notifications
    * [title] is the title for the notification
     * [contentText] is the text for the body of the notification
     * [supportsAction] is a boolean specifying if the notification has actions and a pending intent has to be made for iy
     * [icon] is the icon id
     * [identifier] identifies what kind of notification is to be made
     * [hasProgressBar] -can be refactored to not to contain
     * [descriptionTextForAction] -  if [supportsAction] is true ,one action can have a description text here
    * */


     fun notificationBuilder(
        title :String = "Uv Rakshak",
        contentText: String
        , supportsAction: Boolean,
        icon: Int,
        identifier: String?,
        hasProgressBar: Boolean,
        descriptionTextForAction: String?


    ): Notification {



        val notificationBuilder = NotificationCompat.Builder(context, BleService.CHANNEL_ID)


//        val activityIntent = Intent(context, MainActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//
//        }
//        val activityPendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, activityIntent, 0)



        if (supportsAction) {
            /*
            * This condition checks if there are any actions suppoerted by the notification and if there are ,we check the notification type
            * and add the actions accordingly
            *
            *
            *
            * */


            when (identifier) {


                DEVICE_CONNECTED -> {
                    notificationBuilder.addAction(
                        icon, descriptionTextForAction, makePendingIntent(
                            DISINFECTION_IN_PROGRESS
                        )
                    ).addAction(icon,"Disconnect from device",makePendingIntent(ON_DONE))


                }
                DISINFECTION_IN_PROGRESS -> {
                    notificationBuilder.addAction(
                        icon, descriptionTextForAction, makePendingIntent(
                            STOP_DISINFECTION
                        )
                    )
                    notificationBuilder.setProgress(100, getProgress(), true)

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
                BATTERY_DRAINED->{
                    notificationBuilder.addAction(icon,descriptionTextForAction,makePendingIntent(
                        SWITCH_TO_AUTO))
                }




//                SWITCH_TO_MANUAL_MODE->{
//                    notificationBuilder.addAction(icon,descriptionTextForAction,makePendingIntent(
//                        SWITCH_TO_AUTO))
//                    notificationBuilder.addAction(icon,"Disconnect from device",makePendingIntent(ON_DONE))
//                }
//
//                AUTO_MODE_STARTED->{
//                    notificationBuilder.addAction(icon,descriptionTextForAction,makePendingIntent(
//                        SWITCH_TO_MANUAL_MODE
//                    ))
//                    notificationBuilder.addAction(icon,"Disconnect from device",makePendingIntent(ON_DONE))
//                }

                SWITCH_TO_AUTO->{
                    notificationBuilder.addAction(icon,descriptionTextForAction,makePendingIntent(
                        SWITCH_TO_MANUAL_MODE))
                    notificationBuilder.addAction(icon,"Disconnect from device",makePendingIntent(ON_DONE))
                }







            }
        }

        notificationBuilder.apply {
            setContentTitle(title)
            setContentText(contentText)
            priority = NotificationCompat.PRIORITY_DEFAULT
            //setContentIntent(activityPendingIntent)
            color = context.resources.getColor(android.R.color.holo_blue_bright,null)
            setSmallIcon(R.drawable.ic_android_black_24dp)

        }
        return notificationBuilder.build()

    }

    /**
     *
     *  [makePendingIntent] method is used to make pending intents for notificationBuilder method
     *
     * **/



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



    /**
     *
    * This method [changeNotificationAndPerformAction] is triggered by an interface in the broadcast receiver
    * which in turn is triggered using a sendBroadcast or through
    * PendingIntent.getBroadCast() methods
    * [className] is for debugging
     * [action] is the broadcasted parameter which is received and is used to identify the type of
     *          notification to be made and [identifier] is the dame as [action] for notification builders knowledge
    * */





    override fun changeNotificationAndPerformAction(action: String,className:String) {
        Log.e("Change Notification",action)
        Log.e("class name",className)
        when(action)
        {
            DEVICE_CONNECTED -> {
                showNotification(
                    notificationBuilder(
                        contentText = "Device Connected",
                        supportsAction = true,
                        hasProgressBar = false,
                        descriptionTextForAction = "Start Disinfection",
                        identifier = DEVICE_CONNECTED,
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
                        identifier = DISINFECTION_IN_PROGRESS,
                        icon = R.drawable.ic_android_black_24dp)
                )
                mListener?.onDisnfectionInProgress()
            }
            DISINFECTION_COMPLETE -> {
                showNotification(
                    notificationBuilder(
                        contentText = "Disinfection Complete",
                        supportsAction = true,
                        hasProgressBar = false,
                        descriptionTextForAction = "Disconnect from device",
                        identifier = DISINFECTION_COMPLETE,
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
                        identifier = LOW_RSSI,
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
                        identifier = OUT_OF_RANGE,
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
                        identifier =BATTERY_DRAINED,
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
                        identifier = STOP_DISINFECTION,
                        icon = R.drawable.ic_android_black_24dp)
                )
                mListener?.stopDisinfection()
            }


            SWITCH_TO_AUTO -> {
//                showNotification(
//                    notificationBuilder(
//                        contentText = "Auto mode enabled",
//                        supportsAction = true,
//                        hasProgressBar = false,
//                        descriptionTextForAction = "Switch to Manual mode",
//                        identifier = SWITCH_TO_AUTO,
//                        icon = R.drawable.ic_android_black_24dp)
//                )
                mListener?.startAutoMode()
            }


            ON_DONE -> {
                showNotification(
                    notificationBuilder(
                        contentText = "Done",
                        supportsAction = false,
                        hasProgressBar = false,
                        descriptionTextForAction = "Ok",
                        identifier = ON_DONE,
                        icon = R.drawable.ic_android_black_24dp)
                    //kill service here

                )
                mListener?.onDone()
            }


            SWITCH_TO_MANUAL_MODE ->{
                //No notification here
                mListener?.startManualMode()
            }


        }
    }

    fun setProgress(progress: Int) {
        countdownProgress  =  progress
    }


    fun getProgress():Int{
        return  countdownProgress
    }


}