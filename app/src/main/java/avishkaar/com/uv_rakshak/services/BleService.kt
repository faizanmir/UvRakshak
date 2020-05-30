package avishkaar.com.uv_rakshak.services

import android.app.Service
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import avishkaar.com.uv_rakshak.modelClasses.ActivityStateManagement
import avishkaar.com.uv_rakshak.receivers.NotificationBroadcastReceiver
import avishkaar.com.uv_rakshak.R
import avishkaar.com.uv_rakshak.constants.Constants
import avishkaar.com.uv_rakshak.constants.Constants.Companion.LOW_RSSI
import avishkaar.com.uv_rakshak.helpers.BluetoothHelper
import avishkaar.com.uv_rakshak.helpers.CountdownCounter
import avishkaar.com.uv_rakshak.helpers.NotificationHelper
import avishkaar.com.uv_rakshak.singletons.Singleton
import java.util.concurrent.TimeUnit


class BleService : Service(),BluetoothHelper.BluetoothCallbacks
    ,NotificationHelper.OnNotificationChangeListener
    ,CountdownCounter.OnTimerStartedListener{

    private var broadcastReceiver: NotificationBroadcastReceiver? = null
    private var bluetoothHelper:BluetoothHelper? =  null
    private var notificationHelper :NotificationHelper? =  null
    var binder: LocalBinder = LocalBinder()
    var countDownTimer:CountdownCounter?  =  null
    var mListener:ServiceActionListener? = null
    var progress:Int  = 0;
    var activityStateManagement: ActivityStateManagement? =  null


    interface ServiceActionListener{
        fun onDone()
        fun startAutoMode()
        fun shutdownService()
        fun disconnect()
        fun onDeviceOutOfRange()
        fun onDisinfectionProcessStarted()
        fun onDisinfectionComplete()
        fun onBatteryLow()
        fun onDeviceRSSIlow()
        fun onDisinfectionInProgress()
        fun stopDisinfection()
        fun startManualMode()
        fun onDeviceConnected()
        fun onDeviceDisconnected()
    }


    companion object {
        const val CHANNEL_ID = "Uv"
        const val CHANNEL_NAME = "UV_RAKSHAK"
        const val NOTIFICATION_ID = 7006
        var stateManagementObject: ActivityStateManagement? = null
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

        broadcastReceiver = Singleton.notificationBroadcastReceiver
        broadcastReceiver?.registerNotificationChangeListener(notificationHelper)
        notificationHelper?.createNotificationChannel(CHANNEL_ID, CHANNEL_NAME)
        registerReceiver(broadcastReceiver, NotificationBroadcastReceiver.makeIntentFilter())

        return START_STICKY
    }






    override fun onCreate() {

        notificationHelper   = NotificationHelper(this)

        bluetoothHelper  =  BluetoothHelper(this)

        countDownTimer  =  Singleton.countdownCounter

        countDownTimer?.registerOnTimerStartedListener(Constants.SERVICE_ID,this)




        startForeground(NOTIFICATION_ID,notificationHelper?.notificationBuilder(contentText = "Service Started",
            supportsAction = false,
            hasProgressBar = false,
            descriptionTextForAction = null,
            identifier = LOW_RSSI,
            icon = R.drawable.ic_android_black_24dp)
        )
    }




    override fun onDestroy() {
//        unregisterReceiver(broadcastReceiver)
        broadcastReceiver?.unregisterNotificationChangeListener()
        countDownTimer?.unregisterOnTimerStartedListener(Constants.SERVICE_ID)
        super.onDestroy()
    }




    fun setServiceActionListener(serviceActionListener: ServiceActionListener?)
    {
        this.mListener =  serviceActionListener
    }

    fun unRegisterServiceActionListener(){
        this.mListener  =  null
    }


    override fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
        broadcastNotification(action)
    }

    override fun onBluetoothStateChange(newState: Int) {
        if(newState  == BluetoothProfile.STATE_CONNECTED)
        {
            mListener?.onDeviceConnected()
        }else if(newState  == BluetoothProfile.STATE_DISCONNECTED)
        {
            mListener?.onDeviceDisconnected()
        }
    }


    fun setNotification(action: String)
    {
        broadcastNotification(action)
    }

    /**
     * [broadcastNotification] is used to send the broadcast to [broadcastReceiver]
     * which is the subscribed broadcast receiver for the service
     * */

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
        val handler = Handler()
        bluetoothHelper?.onDone()
        mListener?.onDone()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            handler.postDelayed({
                stopForeground(true)
                stopSelf()
            },1000)

        }else{
            stopSelf()
            
        }

    }

    override fun startAutoMode() {
        bluetoothHelper?.startAutoMode()
        mListener?.startAutoMode()

    }

    override fun shutdownService() {
        bluetoothHelper?.shutdownService()
        mListener?.shutdownService()
    }

    override fun disconnect() {
        bluetoothHelper?.disconnect()
        mListener?.disconnect()
    }

    override fun onDeviceOutOfRange() {
       bluetoothHelper?.onDeviceOutOfRange()
        mListener?.onDeviceRSSIlow()
    }



    override fun onDisinfectionProcessStarted() {
       bluetoothHelper?.onDisinfectionProcessStarted()
        mListener?.onDisinfectionProcessStarted()
    }

    override fun onDisinfectionComplete() {
        bluetoothHelper?.onDisinfectionComplete()
        mListener?.onDisinfectionComplete()
    }

    override fun onBatteryLow() {
        bluetoothHelper?.onBatteryLow()
        mListener?.onBatteryLow()
    }

    override fun onDeviceRSSIlow() {
        bluetoothHelper?.onDeviceRSSIlow()
        mListener?.onDeviceRSSIlow()
    }

    override fun onDisnfectionInProgress() {
        Log.e("onDisinfection","service")
        bluetoothHelper?.onDisconnectionInProgress()
        mListener?.onDisinfectionInProgress()
    }


    override fun stopDisinfection() {
        bluetoothHelper?.stopDisinfection()
        mListener?.stopDisinfection()
    }

    override fun startManualMode() {
        bluetoothHelper?.startManualMode()
        mListener?.startManualMode()
    }

    override fun onTick(millisInFuture: Long) {
        progress  = 100 - ( TimeUnit.MILLISECONDS.toMinutes(millisInFuture).toDouble()/Constants.MINUTES *100).toInt()
        notificationHelper?.setProgress(progress)
    }

    override fun onFinish() {
        this@BleService .setNotification(Constants.DISINFECTION_COMPLETE)
    }


    fun handleDeviceDisconnection()
    {
        bluetoothHelper?.disconnect()
    }



    fun setActivityStateManagementVariables(stateManagementObject: ActivityStateManagement){
        Companion.stateManagementObject  =
            ActivityStateManagement(
                stateManagementObject.isUvOn,
                stateManagementObject.deviceName,
                stateManagementObject.connectionState,
                stateManagementObject.isTimerOn,
                stateManagementObject.isAutoModeOn
            )
        Log.e("ServiceSetUp",Companion.stateManagementObject.toString())
    }

    fun getActivityStateManagementObject(): ActivityStateManagement?{
        return stateManagementObject
    }



    fun disconnectBluetooth(){
        bluetoothHelper?.disconnect()
    }




}

