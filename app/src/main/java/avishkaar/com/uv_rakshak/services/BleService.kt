package avishkaar.com.uv_rakshak.services

import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import avishkaar.com.uv_rakshak.modelClasses.ActivityStateClass
import avishkaar.com.uv_rakshak.receivers.NotificationBroadcastReceiver
import avishkaar.com.uv_rakshak.R
import avishkaar.com.uv_rakshak.constants.Constants
import avishkaar.com.uv_rakshak.constants.Constants.Companion.LOW_RSSI
import avishkaar.com.uv_rakshak.helpers.*
import avishkaar.com.uv_rakshak.services.BleService.Companion.BluetoothInitializerClass.Companion.service
import avishkaar.com.uv_rakshak.singletons.Singleton
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothConfiguration
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothStatus
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothWriter
import com.github.douglasjunior.bluetoothlowenergylibrary.BluetoothLeService
import java.util.*
import java.util.concurrent.TimeUnit


class BleService : Service(), BluetoothHelper.BluetoothCallbacks
    ,NotificationHelper.OnNotificationChangeListener
    ,CountdownCounter.OnTimerStartedListener{

    private var broadcastReceiver: NotificationBroadcastReceiver? = null
    private var bluetoothHelper:BluetoothHelper? =  null
    private var notificationHelper :NotificationHelper? =  null
    var binder: LocalBinder = LocalBinder()
    var countDownTimer:CountdownCounter?  =  null
    var mListener:ServiceActionListener? = null
    var progress:Int  = 0;
    var activityStateClass: ActivityStateClass? =  null


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
        var stateClassObject: ActivityStateClass? = null

        class BluetoothInitializerClass(var context : Context){

            companion object{
                var device:BluetoothDevice ? =  null
                var service : BluetoothService?=  null
                var writer:BluetoothWriter? =  null
            }

            fun setService () {
                val config = BluetoothConfiguration()
                config.context = context
                config.bluetoothServiceClass =
                    BluetoothLeService::class.java // BluetoothClassicService.class or BluetoothLeService.class
                config.bufferSize = 1024
                config.characterDelimiter = ' '
                config.deviceName = "Bluetooth"
                config.callListenersInMainThread = true
                config.uuidService =
                    UUID.fromString("00000021-0000-1000-8000-00805f9b34fb") // Required
                config.uuidCharacteristic =
                    UUID.fromString("00000052-0000-1000-8000-00805f9b34fb") // Required
                config.transport = BluetoothDevice.TRANSPORT_LE // Required for dual-mode devices
                config.uuid =
                    UUID.fromString("00000021-0000-1000-8000-00805f9b34fb") // Used to filter found devices. Set null to find all devices.
                BluetoothService.init(config)
                service = BluetoothService.getDefaultInstance()
                writer  =  BluetoothWriter(service)
                BluetoothService.init(config)

            }

        }

    }



    inner class LocalBinder : Binder() {
        fun getService(): BleService {
            return this@BleService
        }
    }







    override fun onBind(intent: Intent): IBinder {
        Log.e("OnBind","Registering callback to bluetooth helper")
        service?.setOnEventCallback(bluetoothHelper)
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

        //bluetoothHelper  =  BluetoothHelper(this)

        bluetoothHelper  = BluetoothHelper(context = this)



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

    override fun onBluetoothStateChange(newState: BluetoothStatus?) {
        if(newState  == BluetoothStatus.CONNECTED)
        {
            Log.e("TAG","Device connected")
            mListener?.onDeviceConnected()
        }
        else if(newState  == BluetoothStatus.NONE)
        {
            Log.e("Disconnected","Here in Service")
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



//    fun connectToDevice(address:String?)
//    {
//        bluetoothHelper?.connect(address)
//    }


    fun write(instruction:String)
    {
        bluetoothHelper?.write(instruction)
    }

    override fun onDone() {
        unregisterReceiver(broadcastReceiver)
        bluetoothHelper?.onDone()

        mListener?.onDone()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Handler().postDelayed({
                stopForeground(true)
                stopSelf()
            },3000)

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



    fun setActivityStateManagementVariables(stateClassObject: ActivityStateClass?){
        if(stateClassObject != null) {
            Companion.stateClassObject = ActivityStateClass(
                stateClassObject.isUvOn,
                stateClassObject.isAutoModeOn,
                stateClassObject.progress
            )
        }else{
            Companion.stateClassObject =  null
        }
        Log.e("ServiceSetUp",Companion.stateClassObject.toString())
    }

    fun getActivityStateManagementObject(): ActivityStateClass?{
        return stateClassObject
    }




    override fun onUnbind(intent: Intent?): Boolean {
        return true
    }


    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        service?.setOnEventCallback(bluetoothHelper)
    }

    fun connect(device:BluetoothDevice?)
    {
        Log.e("TAG","Called connnect")
        Log.e("Device", device.toString())
        bluetoothHelper!!.connect(device)
    }







}

