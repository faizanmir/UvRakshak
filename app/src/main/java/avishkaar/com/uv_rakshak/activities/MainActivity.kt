package avishkaar.com.uv_rakshak.activities

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.CompoundButton
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import avishkaar.com.uv_rakshak.modelClasses.ActivityStateManagement
import avishkaar.com.uv_rakshak.helpers.BluetoothState
import avishkaar.com.uv_rakshak.R
import avishkaar.com.uv_rakshak.constants.Constants
import avishkaar.com.uv_rakshak.helpers.BluetoothHelper.Companion.bluetoothGatt
import avishkaar.com.uv_rakshak.helpers.CountdownCounter
import avishkaar.com.uv_rakshak.services.BleService
import avishkaar.com.uv_rakshak.singletons.Singleton
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), ServiceConnection,
    CountdownCounter.OnTimerStartedListener,
    CompoundButton.OnCheckedChangeListener,
    View.OnTouchListener, View.OnClickListener,
    BleService.ServiceActionListener {
    var bleService: BleService? = null
    var deviceAddress: String? = ""
    var countdownCounter: CountdownCounter? = null
    var isCountdownRunning: Boolean = false
    var isUvOn = false
    var batteryDialog: AlertDialog? = null
    var uvDialog: AlertDialog? = null
    var lowRssi: AlertDialog? = null
    var autoModeActive = false
    var isDisinfectionInProgress = false
    var autoModeDialog: AlertDialog? = null
    var activityStateManagement: ActivityStateManagement? = null
    var device_name_string: String? = null
    var connectionState: BluetoothState = BluetoothState.isDisconnected
    var isTimerOn = false
    var temp: ActivityStateManagement? = null


    var isDeviceConnected =  false;


    companion object {
        var ACTIVITY_IS_RUNNING = false
    }

    override fun onStart() {
        super.onStart()

    }

    @RequiresApi(Build.VERSION_CODES.O)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initialize()


    }

    override fun onServiceDisconnected(name: ComponentName?) {
        Log.e("Failed","Connection to service failed")

    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        bleService =  (service as BleService.LocalBinder).getService()
        if(bluetoothGatt  == null) {
            Log.e("GATT is null", "null")
            bleService?.connectToDevice(address = deviceAddress)
            bleService?.setServiceActionListener(this)

        }
    }


   private fun startService(){

        val intent =  Intent(this,BleService::class.java)
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        }else{
            startService(intent)
        }
        bindService(intent)


    }

    private fun bindService(intent:Intent){
        Log.e("bindService","Attempting connection")
        bindService(intent,this, 0)
    }




    override fun onResume() {
        super.onResume()
        ACTIVITY_IS_RUNNING = true
        startService()
        bleService?.setServiceActionListener(this)
        countdownCounter?.registerOnTimerStartedListener(Constants.ACTIVITY_ID, this)


    }

    override fun onPause() {
        super.onPause()
        ACTIVITY_IS_RUNNING = false
        if (bleService != null) {
            unbindService(this)
        }
        if (countdownCounter != null) {
            countdownCounter?.unregisterOnTimerStartedListener(Constants.ACTIVITY_ID)
        }
        bleService?.unRegisterServiceActionListener()
        //   activityStateManagement = ActivityStateManagement(isUvOn,device_name_string!!,connectionState,isTimerOn,autoModeActive)
        //   bleService?.setActivityStateManagementVariables(activityStateManagement!!)

    }



   private fun initialize() {
       //  bleService?.getActivityStateManagementObject()


       countdownCounter = Singleton.countdownCounter




       makeDialogs()

       rootLayout.setBackgroundResource(R.drawable.gradient)

       leftButton.setOnTouchListener(this)
       rightButton.setOnTouchListener(this)
       forwardButton.setOnTouchListener(this)
       backButton.setOnTouchListener(this)
       uvSwitch.setOnCheckedChangeListener(this)
       autoToggle.setOnCheckedChangeListener(this)
       mainActivityBackButton.setOnClickListener(this)
       connectionButton.setOnClickListener (this)
       setUiForManualMode()


       deviceAddress = intent.getStringExtra(Constants.DEVICE_ADDRESS)
       device_name_string = intent.getStringExtra(Constants.DEVICE_NAME)
       deviceName.text = device_name_string


       //restoreState()


   }


    override fun onTick(millisInFuture: Long) {

        timeTextView?.text = String.format(
            "%02d:%02d  Minutes Left",
            TimeUnit.MILLISECONDS.toMinutes(millisInFuture).toInt(),
            TimeUnit.MILLISECONDS.toSeconds(millisInFuture) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisInFuture))
        )
        setCountdownProgress(
            TimeUnit.MILLISECONDS.toMinutes(millisInFuture).toDouble() / Constants.MINUTES * 100
        )
    }

    override fun onFinish() {
        timeTextView.text = getString(R.string.disinfection_complete)
        bleService?.setNotification(Constants.DISINFECTION_COMPLETE)
        bleService?.setNotification(Constants.SWITCH_TO_MANUAL_MODE)
        isDisinfectionInProgress = false
    }


    private fun setCountdownProgress(value: Double) {
        val progress = 100 - value.toInt()
        timeProgressbar.progress = progress
    }


    fun setUiForAutoMode() {
        timeProgressbar.visibility = View.VISIBLE
        buttonHolder.visibility = View.INVISIBLE
    }


    fun setUiForManualMode() {
        timeProgressbar.visibility = View.VISIBLE
        buttonHolder.visibility = View.VISIBLE
        timeTextView.text = getString(R.string.timer_will_begin)
        timeProgressbar.progress = 0
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView?.id) {
            R.id.uvSwitch -> {
                if (isChecked) {
                    isUvOn = true
                    if (!uvDialog?.isShowing!!) {
                        uvDialog?.show()
                    }
                } else {
                    bleService?.setNotification(Constants.DISINFECTION_COMPLETE)
                    isUvOn = false
                    countdownCounter?.cancel()
                    isCountdownRunning = false
                    isDisinfectionInProgress = false
                }
            }
            R.id.autoToggle -> {
                when (isChecked) {
                    true -> {
                        if (!autoModeDialog?.isShowing!!) {
                            autoModeDialog?.show()
                        }

                    }
                    false -> {
                        autoModeActive = false
                        if (isCountdownRunning) {
                            isCountdownRunning = false
                            bleService?.setNotification(Constants.SWITCH_TO_MANUAL_MODE)
                        }

                    }
                }

            }
        }

    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            when (v?.id) {
                R.id.forwardButton -> {
                    bleService?.write("*f")
                }
                R.id.leftButton -> {
                    bleService?.write("*l")
                }
                R.id.rightButton -> {
                    bleService?.write("*r")
                }
                R.id.backButton -> {
                    bleService?.write("*b")
                }


            }
        } else if (event?.action == MotionEvent.ACTION_UP) {
            bleService?.write("*x")
        }
        return true
    }

    override fun onDone() {

    }

    override fun startAutoMode() {
        countdownCounter?.start()
        setUiForAutoMode()
    }

    override fun shutdownService() {

    }

    override fun disconnect() {

    }

    override fun onDeviceOutOfRange() {

    }


    override fun onDisinfectionProcessStarted() {


    }

    override fun onDisinfectionComplete() {
        //Stub
    }

    override fun onBatteryLow() {
        if (!batteryDialog?.isShowing!!) {
            batteryDialog?.show()
        }
    }

    override fun onDeviceRSSIlow() {
        if (!lowRssi?.isShowing!!) {
            lowRssi?.show()
        }

    }

    override fun onDisinfectionInProgress() {
        Log.e("onDisinfection", "Activity")
        if (!isDisinfectionInProgress && !uvDialog?.isShowing!!) {
            uvDialog?.show()
        }


    }

    override fun stopDisinfection() {

        if (isUvOn) {
            uvSwitch.performClick()
        }

    }

    override fun startManualMode() {
        countdownCounter?.cancel() //Discuss this more
        setUiForManualMode()
        if (autoModeActive) {
            autoToggle.performClick()
        }

    }

    override fun onDeviceConnected() {
        isDeviceConnected  =  true
        runOnUiThread {
            connectionButton.setImageResource(R.drawable.connected_button)
            bluetoothIndicator.setImageResource(R.drawable.bluetooth_indicator)
            deviceStatus.text  =  "Device connected"
        }

    }

    override fun onDeviceDisconnected() {
        isDeviceConnected  =  false
        runOnUiThread{connectionButton.setImageResource(R.drawable.disconnect_button)
            bluetoothIndicator.setImageResource(R.drawable.bluetooth_disconnected)
            deviceStatus.text = "Device disconnected"
        }

    }


    private fun alertDialogBuilder(
        title: String, message: String, positive: String?, negative: String?,
        positiveAction: () -> Unit?, negativeAction: () -> Unit?
    ): AlertDialog {
        return AlertDialog.Builder(this).setTitle(title)
            .setMessage(message)
            .setPositiveButton(positive) { _, _ -> positiveAction() }
            .setNegativeButton(negative) { dialog, _ ->
                run {
                    negativeAction()
                    dialog.dismiss()
                }
            }
            .create()
    }


    private fun makeDialogs() {
        batteryDialog = alertDialogBuilder("Battery low", getString(R.string.battery_drained),
            "Ok", null, { batteryDialog?.dismiss() }, {})



        uvDialog = alertDialogBuilder("Warning", getString(R.string.uv_switch_on_warning),
            "PROCEED", "CANCEL", {
                if (!uvSwitch?.isChecked!!) {
                    uvSwitch.performClick()
                }


                countdownCounter?.start()
                isCountdownRunning = true
                isDisinfectionInProgress = true
                bleService?.setNotification(Constants.DISINFECTION_IN_PROGRESS)
            }
            , {
                if (isUvOn) {
                    uvSwitch.performClick()
                }
            })





        lowRssi = alertDialogBuilder("Device Moving out of range",
            "Device is moving out of range,bot has been turned off to prevent accidental exposure",
            "Ok",
            null,
            { lowRssi?.dismiss() }, {})



        autoModeDialog = alertDialogBuilder("Switch to AUTO mode",
            getString(R.string.auto_mode),
            "Proceed",
            "Cancel", {
                autoModeActive = true
                isCountdownRunning = true
                bleService?.setNotification(Constants.SWITCH_TO_AUTO)


            }, {
                if (!autoModeActive) {
                    autoToggle.performClick()
                }

                autoModeDialog?.dismiss()

            })


    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.mainActivityBackButton -> finish()
            R.id.connectionButton->{
                if(isDeviceConnected)
                {
                    bleService?.disconnectBluetooth()
                }else{
                    bleService?.connectToDevice(deviceAddress)
                }
            }
        }
    }


    override fun onStop() {
        super.onStop()


    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }


//    private fun restoreState(){
//        Log.e("RESTOR",BleService.stateManagementObject.toString())
//        if( BleService.stateManagementObject != null )
//        {
//            deviceName.text = BleService.stateManagementObject?.deviceName
//            if(BleService.stateManagementObject?.isUvOn!! ){uvSwitch.isChecked  =  true}
//            connectionState = BleService.stateManagementObject!!.connectionState!!
//            if(BleService.stateManagementObject!!.isAutoModeOn!!) {autoToggle.isChecked =  true}
//        }
//    }


}
