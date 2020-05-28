package avishkaar.com.uv_rakshak.activities

import android.content.*
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import avishkaar.com.uv_rakshak.R
import avishkaar.com.uv_rakshak.constants.Constants
import avishkaar.com.uv_rakshak.helpers.BluetoothHelper.Companion.bluetoothGatt
import avishkaar.com.uv_rakshak.services.BleService

class MainActivity : AppCompatActivity(),ServiceConnection,View.OnClickListener {
    var bleService: BleService? = null
    var deviceAddress:String?  =  ""

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
            bleService?.connectToDevice(address = deviceAddress)

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


    override fun onClick(v: View?) {
        when(v?.id )
        {

        }
    }


    override fun onResume() {
        super.onResume()
        startService()

    }

    override fun onPause() {
        super.onPause()
        if(bleService !=  null) {
            unbindService(this)
        }
    }



   private fun initialize(){
        deviceAddress  = intent.getStringExtra(Constants.DEVICE_NAME)



    }

    fun connectionEstablished(view: View) {
        bleService?.testNotifications(Constants.DEVICE_CONNECTED)
    }
    fun disinfectionInProgress(view: View) {
        bleService?.testNotifications(Constants.DISINFECTION_IN_PROGRESS)
    }
    fun lowRssi(view: View) {
        bleService?.testNotifications(Constants.LOW_RSSI)
    }
    fun outOfRange(view: View) {
        bleService?.testNotifications(Constants.OUT_OF_RANGE)
    }
    fun batteryDrained(view: View) {
        bleService?.testNotifications(Constants.BATTERY_DRAINED)
    }
    fun disinfectionComplete(view: View) {
        bleService?.testNotifications(Constants.DISINFECTION_COMPLETE)
    }

    fun sendDataToBluetooth(view: View) {
        bleService?.write("av,6,BAT,~")
    }
}
