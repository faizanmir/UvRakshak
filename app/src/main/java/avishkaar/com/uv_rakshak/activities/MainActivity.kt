package avishkaar.com.uv_rakshak.activities

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.CompoundButton
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import avishkaar.com.uv_rakshak.modelClasses.ActivityStateClass
import avishkaar.com.uv_rakshak.R
import avishkaar.com.uv_rakshak.constants.Constants
import avishkaar.com.uv_rakshak.helpers.CountdownCounter
import avishkaar.com.uv_rakshak.services.BleService
import avishkaar.com.uv_rakshak.singletons.Singleton
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothStatus
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit


class MainActivity :
    AppCompatActivity(),
    ServiceConnection,
    CountdownCounter.OnTimerStartedListener,
    CompoundButton.OnCheckedChangeListener,
    View.OnTouchListener,
    View.OnClickListener,
    BleService.ServiceActionListener {
    private var bleService: BleService? = null
    private var countdownCounter: CountdownCounter? = null
    private var isCountdownRunning: Boolean = false
    private var isUvOn = false
    private var batteryDialog: AlertDialog? = null
    private var uvDialog: AlertDialog? = null
    private var lowRssi: AlertDialog? = null
    private var autoModeActive = false
    private var isDisinfectionInProgress = false
    private var autoModeDialog: AlertDialog? = null
    private var service :BluetoothService?  =  null
    private var millis  :Long = 0
    private var isDeviceConnected =  false


    companion object {
        var ACTIVITY_IS_RUNNING = false
    }



    @RequiresApi(Build.VERSION_CODES.O)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initialize()
        makeDialogs()
        setGestureListeners()
        checkForPreviousState()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        Log.e("Failed","Connection to service failed")

    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        bleService =  (service as BleService.LocalBinder).getService()
        bleService?.setServiceActionListener(this)
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
        bindService(intent,this, 0)
    }




    override fun onResume() {
        super.onResume()

        if(service?.status ==  BluetoothStatus.CONNECTED)
        {
            setUiForConnected()
        }

        ACTIVITY_IS_RUNNING = true
        startService()
        Log.e("TAG","Registering service listener")
        bleService?.setServiceActionListener(this)
        countdownCounter?.registerOnTimerStartedListener(Constants.ACTIVITY_ID, this)
        setActivityState(isUvOn,autoModeActive)


    }

    override fun onPause() {
        super.onPause()
        ACTIVITY_IS_RUNNING = false
        if (bleService != null) {
            unbindService(this)
        }

        setActivityState(isUvOn,autoModeActive)

        if (countdownCounter != null) {
            countdownCounter?.unregisterOnTimerStartedListener(Constants.ACTIVITY_ID)
        }
        bleService?.unRegisterServiceActionListener()
        //   activityStateManagement = ActivityStateManagement(isUvOn,device_name_string!!,connectionState,isTimerOn,autoModeActive)
        //   bleService?.setActivityStateManagementVariables(activityStateManagement!!)

    }



   private fun initialize() {
        service  = BleService.Companion.BluetoothInitializerClass.service
        countdownCounter = Singleton.countdownCounter

       if(service?.status  ==  BluetoothStatus.CONNECTED)
       {
          setUiForConnected()
           rootLayout.setBackgroundResource(R.drawable.gradient)
       }
       deviceName.text  = BleService.Companion.BluetoothInitializerClass.device?.name

   }


    override fun onTick(millisInFuture: Long) {
        millis   = millisInFuture
        setTimerProgress(millisInFuture)
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


    private fun setUiForManualMode() {
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
                        autoModeActive  =  true
                        if (
                            !autoModeDialog?.isShowing!!) {
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
                  v.performClick()
                }
                R.id.leftButton -> {
                    v.performClick()
                }
                R.id.rightButton -> {
                    v.performClick()
                }
                R.id.backButton -> {
                   v.performClick()
                }




            }
        } else if (event?.action == MotionEvent.ACTION_UP) {
            bleService?.write("*x")
            when(v?.id)
            {

                R.id.forwardButton -> {

                    (v as ImageView).setImageResource(R.drawable.fwd_button)
                }
                R.id.leftButton -> {

                    (v as ImageView).setImageResource(R.drawable.left_button)
                }
                R.id.rightButton -> {

                    (v as ImageView).setImageResource(R.drawable.right_button)
                }
                R.id.backButton -> {

                    (v as ImageView).setImageResource(R.drawable.back_button)
                }

            }
        }
        return true
    }

    override fun onDone() {
        finish()
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
        setUiForManualMode()
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
            deviceStatus.text  =  getString(R.string.device_connected)
            overlay.visibility =  View.INVISIBLE
            rootLayout.setBackgroundResource(R.drawable.gradient)
        }

    }

    override fun onDeviceDisconnected() {
        isDeviceConnected  =  false
        runOnUiThread{connectionButton.setImageResource(R.drawable.disconnect_button)
            bluetoothIndicator.setImageResource(R.drawable.bluetooth_disconnected)
            deviceStatus.text = getString(R.string.device_disconnected)
            overlay.visibility =  View.INVISIBLE
            rootLayout.setBackgroundColor(Color.parseColor("#dfdfdf"))
        }

    }

    override fun deviceCharging() {
        setUiForChargingMode()
    }

    override fun deviceUnplugged() {
        setUiForManualMode()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
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
                    bleService?.startAutoMode()
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
                    service?.disconnect()
                    bleService?.setNotification(Constants.ON_DONE)
                  //  bleService?.setActivityStateManagementVariables(null)
                }else{
                    overlay.visibility = View.VISIBLE
//                   if(service?.status == BluetoothStatus.CONNECTED) {
//                       bleService?.disconnect()
//                   }

                    bleService?.connect(BleService.Companion.BluetoothInitializerClass.device)
                }
            }
            R.id.forwardButton -> {
                bleService?.write("*f")
                (v as ImageView).setImageResource(R.drawable.fwd_button_pressed)
            }
            R.id.leftButton -> {
                bleService?.write("*l")
                (v as ImageView).setImageResource(R.drawable.left_button_pressed)
            }
            R.id.rightButton -> {
                bleService?.write("*r")
                (v as ImageView).setImageResource(R.drawable.right_button_pressed)
            }
            R.id.backButton -> {
                bleService?.write("*b")
                (v as ImageView).setImageResource(R.drawable.back_button_pressed)
            }

        }
    }



    private fun setActivityState(isUvOn:Boolean, isAutoOn:Boolean){
        bleService?.setActivityStateManagementVariables(ActivityStateClass(isUvOn,isAutoOn,millis))
    }



    private fun setUiForConnected(){
        Log.e("TAG","Device already connected")
        isDeviceConnected  =  true
        overlay.visibility  = View.GONE
        bluetoothIndicator.setImageResource(R.drawable.bluetooth_indicator)
        deviceStatus.text  =  getString(R.string.device_connected)
        connectionButton.setImageResource(R.drawable.connected_button)
    }


    private fun setTimerProgress(millis:Long){
        timeTextView?.text = String.format(
            "%02d:%02d  Minutes Left",
            TimeUnit.MILLISECONDS.toMinutes(millis).toInt(),
            TimeUnit.MILLISECONDS.toSeconds(millis) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        )
        setCountdownProgress(TimeUnit.MILLISECONDS.toMinutes(millis).toDouble() / Constants.MINUTES * 100)
    }



    private fun setGestureListeners()
    {

        leftButton.setOnTouchListener(this)
        rightButton.setOnTouchListener(this)
        forwardButton.setOnTouchListener(this)
        backButton.setOnTouchListener(this)
        uvSwitch.setOnCheckedChangeListener(this)
        autoToggle.setOnCheckedChangeListener(this)
        mainActivityBackButton.setOnClickListener(this)
        rightButton.setOnClickListener (this)
        leftButton.setOnClickListener(this)
        forwardButton.setOnClickListener(this)
        backButton.setOnClickListener(this)
        connectionButton.setOnClickListener (this)
    }


    private fun checkForPreviousState(){
        if(BleService.stateClassObject != null) {

            if (BleService.stateClassObject?.isAutoModeOn!!) {
                autoToggle.setOnCheckedChangeListener(null)
                autoToggle.isChecked = true
                autoToggle.setOnCheckedChangeListener(this@MainActivity)
                autoModeActive  =  true
                setUiForAutoMode()
            }


            if (BleService.stateClassObject?.isUvOn!!) {
                uvSwitch.setOnCheckedChangeListener(null)
                uvSwitch.isChecked = true
                uvSwitch.setOnCheckedChangeListener(this@MainActivity)
                isUvOn  = true
            }

            setCountdownProgress(millis.toDouble())
        }
        else{
            setUiForManualMode()
        }
    }


    private fun setUiForChargingMode(){
        buttonHolder.visibility =  View.GONE
        chargingTextView.visibility  =  View.VISIBLE
    }
}

