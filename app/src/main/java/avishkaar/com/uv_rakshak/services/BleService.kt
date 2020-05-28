package avishkaar.com.uv_rakshak.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import avishkaar.com.uv_rakshak.receivers.NotificationBroadcastReceiver
import avishkaar.com.uv_rakshak.R
import avishkaar.com.uv_rakshak.constants.Constants
import avishkaar.com.uv_rakshak.constants.Constants.Companion.LOW_RSSI
import avishkaar.com.uv_rakshak.helpers.BluetoothHelper
import avishkaar.com.uv_rakshak.helpers.NotificationHelper


class BleService : Service(),BluetoothHelper.BluetoothCallbacks,NotificationHelper.OnNotificationChangeListener {

    private var broadcastReceiver: NotificationBroadcastReceiver? = null
    private var bluetoothHelper:BluetoothHelper? =  null
    private var notificationHelper :NotificationHelper? =  null
    var binder: LocalBinder = LocalBinder()
    companion object {
        const val CHANNEL_ID = "Uv"
        const val CHANNEL_NAME = "UV_RAKSHAK"
        const val NOTIFICATION_ID = 7006
    }

    inner class LocalBinder : Binder() {
        fun getService(): BleService {
            return this@BleService
        }
    }




    override fun onBind(intent: Intent): IBinder {
        return binder
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        broadcastReceiver = NotificationBroadcastReceiver.getReceiverInstance()
        broadcastReceiver?.registerNotificationChangeListener(notificationHelper)
        notificationHelper?.createNotificationChannel(CHANNEL_ID, CHANNEL_NAME)
        registerReceiver(broadcastReceiver, NotificationBroadcastReceiver.makeIntentFilter())

        return START_STICKY
    }






    override fun onCreate() {
        notificationHelper   = NotificationHelper(this)
        bluetoothHelper  =  BluetoothHelper(this)
        startForeground(NOTIFICATION_ID,notificationHelper?.notificationBuilder(contentText = "Service Started",
            supportsAction = false,
            hasProgressBar = false,
            descriptionTextForAction = null,
            action = LOW_RSSI,
            icon = R.drawable.ic_android_black_24dp)
        )
    }


    override fun onDestroy() {
        unregisterReceiver(broadcastReceiver)
        broadcastReceiver?.unregisterNotificationChangeListener()
    }






    override fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
        broadcastNotification(action)
    }



    fun testNotifications(action: String)
    {
        broadcastNotification(action)
    }



    private fun broadcastNotification(action: String){
        val intent = Intent().apply { this.action  =  action}
        sendOrderedBroadcast(intent,null,broadcastReceiver,null,0,null,null)
    }



    fun connectToDevice(address:String?)
    {
        bluetoothHelper?.connect(address)
    }


    fun write(instruction:String)
    {
        bluetoothHelper?.write(instruction)
    }

    override fun onDone() {
        bluetoothHelper?.onDone()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(true)
            stopSelf()
        }else{
            stopSelf()
            // STOPSHIP: 2020-05-29
            
        }

    }

    override fun startAutoMode() {
        broadcastNotification(Constants.SWITCH_TO_AUTO)
        bluetoothHelper?.startAutoMode()

    }

    override fun shutdownService() {
        bluetoothHelper?.shutdownService()
    }

    override fun disconnect() {
        bluetoothHelper?.disconnect()
    }

    override fun onDeviceOutOfRange() {
       bluetoothHelper?.onDeviceOutOfRange()
    }

    override fun onAutoModeChosen() {
        bluetoothHelper?.onAutoModeChosen()
    }

    override fun onDisinfectionProcessStarted() {
       bluetoothHelper?.onDisinfectionProcessStarted()
    }

    override fun onDisinfectionComplete() {
       bluetoothHelper?.onDisinfectionComplete()
    }

    override fun onBatteryLow() {
        bluetoothHelper?.onBatteryLow()
    }

    override fun onDeviceRSSIlow() {
        bluetoothHelper?.onDeviceRSSIlow()
    }

    override fun onDisconnectionInProgress() {
        bluetoothHelper?.onDisconnectionInProgress()
    }

    override fun stopDisinfection() {
        bluetoothHelper?.stopDisinfection()
    }


}

