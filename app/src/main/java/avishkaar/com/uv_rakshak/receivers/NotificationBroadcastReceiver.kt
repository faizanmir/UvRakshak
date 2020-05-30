package avishkaar.com.uv_rakshak.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import avishkaar.com.uv_rakshak.constants.Constants

class NotificationBroadcastReceiver : BroadcastReceiver() {

    /**
    This class is  the actual broadcast receiver and receives broadcasts from the service as well as from the notifications
    **/
    interface NotificationChangeListener
    {
        fun changeNotificationAndPerformAction(action:String,className:String)
    }

    companion object{
        var mListener:NotificationChangeListener? =  null
       // var listenerMap = hashMapOf<Context,NotificationChangeListener?>()

        fun makeIntentFilter():IntentFilter{
            return IntentFilter().apply {
                Constants.DISINFECTION_IN_PROGRESS
                Constants.DISINFECTION_STARTED
                Constants.LOW_RSSI
                Constants.DEVICE_CONNECTED
                Constants.BATTERY_DRAINED
                Constants.OUT_OF_RANGE
                Constants.DISINFECTION_COMPLETE

            }
        }
    }



    override fun onReceive(context: Context, intent: Intent) {
       //notifyAllReceivers(intent.action)
        mListener?.changeNotificationAndPerformAction(intent.action!!,context::class.java.name)
    }



    fun registerNotificationChangeListener(notificationChangeListener: NotificationChangeListener?)
    {
        mListener =  notificationChangeListener

    }


    fun unregisterNotificationChangeListener( )
    {
        mListener   = null
    }


}
