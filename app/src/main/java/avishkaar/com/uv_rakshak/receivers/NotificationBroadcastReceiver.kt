package avishkaar.com.uv_rakshak.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import avishkaar.com.uv_rakshak.constants.Constants

class NotificationBroadcastReceiver : BroadcastReceiver() {

    interface NotificationChangeListener
    {
        fun changeNotificationAndPerformAction(action:String)
    }

    companion object{
        var instance : NotificationBroadcastReceiver?  =  null
        var mListener:NotificationChangeListener? =  null
       // var listenerMap = hashMapOf<Context,NotificationChangeListener?>()

        fun getReceiverInstance(): NotificationBroadcastReceiver?{
            return if(instance ==  null) {
                NotificationBroadcastReceiver()
            }else{
                instance
            }
        }


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
        mListener?.changeNotificationAndPerformAction(intent.action!!)
    }



    fun registerNotificationChangeListener(notificationChangeListener: NotificationChangeListener?)
    {
        mListener =  notificationChangeListener

    }


    fun unregisterNotificationChangeListener( )
    {
        mListener   = null
    }


    private fun notifyAllReceivers(action: String?){
        mListener?.changeNotificationAndPerformAction(action!!)

    }
}
