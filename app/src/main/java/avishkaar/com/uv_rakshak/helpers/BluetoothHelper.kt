package avishkaar.com.uv_rakshak.helpers

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import android.util.TimeUtils
import avishkaar.com.uv_rakshak.constants.Constants
import avishkaar.com.uv_rakshak.services.BleService
import avishkaar.com.uv_rakshak.services.BleService.Companion.BluetoothInitializerClass.Companion.writer
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothStatus
import java.sql.Time
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Condition

class BluetoothHelper(var context :Context) :
    BluetoothService.OnBluetoothEventCallback,
    BluetoothService.OnBluetoothScanCallback,
    CountdownCounter.OnTimerStartedListener{

    interface BluetoothCallbacks{
        fun broadcastUpdate(action:String)
        fun onBluetoothStateChange(newState:BluetoothStatus?)
    }
    private var mListener: BluetoothCallbacks?=  null

    private  var countDownTimer:CountdownCounter  =  CountdownCounter(5*60*1000,1000)

    private val service:BluetoothService ? =  BleService.Companion.BluetoothInitializerClass.service
    init {
        countDownTimer.start()
        try {
            mListener = context as BluetoothCallbacks
        }catch (e:Exception)
        {
            e.printStackTrace()
        }


        countDownTimer.registerOnTimerStartedListener(Constants.BLUETOOTH_HELPER,this)
    }






    override fun onDataRead(buffer: ByteArray?, length: Int) {
        val strBuffer =  StringBuffer()
        for(byte in buffer!!)
        {
            strBuffer.append(byte.toChar())
        }

        if(strBuffer.toString().removeSurrounding("*").toInt() <= 20) {
            mListener?.broadcastUpdate(Constants.BATTERY_DRAINED)
        }else if(strBuffer.toString() ==  "*charging")
        {
            mListener?.broadcastUpdate(Constants.DEVICE_CHARGING)
        }else if(strBuffer.toString() == "*unplugged")
        {
            mListener?.broadcastUpdate(Constants.DEVICE_UNPLUGGED)
        }


    }





    override fun onStatusChange(status: BluetoothStatus?) {
        if (status == BluetoothStatus.CONNECTED)
        {

            mListener?.broadcastUpdate(Constants.DEVICE_CONNECTED)

        }
        mListener?.onBluetoothStateChange(status)

    }



    override fun onDataWrite(buffer: ByteArray?) {
        val builder  =  StringBuilder()
        for (byte  in buffer!!)
        {
            builder.append(byte.toChar())
        }
        Log.e("Written data...",builder.toString())



        builder.clear()




    }

    override fun onToast(message: String?) {

    }

    override fun onDeviceName(deviceName: String?) {
    }





    fun onDone() {
        Log.i("TAG","OnDone")
        service?.disconnect()
    }

    fun startAutoMode() {
        Log.i("TAG","startAutoMode")
        write("*amode")

    }

    fun shutdownService() {
        Log.i("TAG","shutdownService")
    }

    fun disconnect() {
        Log.i("TAG","disconnect")
       service?.disconnect()
    }

    fun onDeviceOutOfRange() {
        Log.i("TAG","device out of range")
    }

    fun onDisinfectionProcessStarted() {
        Log.i("TAG","on disinfection started")
    }

    fun onDisinfectionComplete() {
        Log.i("TAG","on disinfection complete")
        write("*finished")
    }

    fun onBatteryLow() {
        Log.i("TAG","onBattery low")

    }

    fun onDeviceRSSIlow() {
        Log.i("TAG","on device with low rssi")
    }

    fun onDisconnectionInProgress() {
        Log.i("TAG","on disinfection progress")
        write("*uvon")
    }

    fun stopDisinfection() {
        write("*uvoff")
        Log.i("TAG","stop disinfection")
    }


    fun startManualMode(){
        write("*mmode")
    }



    fun write(instruction: String) {
        Log.e("Tag",instruction)
        writer?.write("$instruction#")
    }

    fun connect(bluetoothDevice: BluetoothDevice?) {
        service?.connect(bluetoothDevice)
    }

    override fun onStopScan() {

    }

    override fun onStartScan() {

    }

    override fun onDeviceDiscovered(device: BluetoothDevice?, rssi: Int) {

    }

    override fun onTick(millisInFuture: Long) {
        Log.e("Thread",Thread.currentThread().name)
        Log.e("bluetooth counter",TimeUnit.MILLISECONDS.toMinutes(millisInFuture.toLong()).toString())
    }

    override fun onFinish() {
       if(service?.status  ==  BluetoothStatus.CONNECTED) {
           write("*bat")
       }
        countDownTimer.start()
    }
}