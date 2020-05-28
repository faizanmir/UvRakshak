package avishkaar.com.uv_rakshak.helpers

import android.bluetooth.*
import android.content.Context
import android.util.Log
import avishkaar.com.uv_rakshak.constants.Constants
import avishkaar.com.uv_rakshak.constants.Constants.Companion.DEVICE_CONNECTED
import avishkaar.com.uv_rakshak.constants.Constants.Companion.DISINFECTION_IN_PROGRESS
import avishkaar.com.uv_rakshak.constants.Constants.Companion.ON_CHARACTERISTICS_CHANGED
import avishkaar.com.uv_rakshak.constants.Constants.Companion.ON_CHARACTERISTICS_READ
import avishkaar.com.uv_rakshak.constants.Constants.Companion.ON_CHARACTERISTICS_WRITE
import avishkaar.com.uv_rakshak.constants.Constants.Companion.ON_SERVICES_DISCOVERED
import java.lang.Exception
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BluetoothHelper(var context: Context) :BluetoothGattCallback() {

    private var uuidService: UUID? = UUID.fromString("00000021-0000-1000-8000-00805f9b34fb") // Required
    private var uuidCharacteristic: UUID? = UUID.fromString("00000052-0000-1000-8000-00805f9b34fb")
    private var characteristic: BluetoothGattCharacteristic? = null
    private var receivedValue: StringBuilder = StringBuilder()
    private var bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var mListener:BluetoothCallbacks? =  null
    private var executor :ExecutorService =  Executors.newSingleThreadExecutor()

    private var signalRunnable: Runnable  =  Runnable {
       while (true) {
           if (bluetoothGatt != null) {
               bluetoothGatt?.readRemoteRssi()

           }
           Thread.sleep(10000)
       }
    }



    init {
        try {
            mListener = context as BluetoothCallbacks
        }catch (e:Exception)
        {
            e.printStackTrace()
        }
    }


    companion object{
        var bluetoothGatt: BluetoothGatt? = null
    }





    interface BluetoothCallbacks{
        fun broadcastUpdate(action:String)
    }



    override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
        getSignalLevels(rssi,status)
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {

        mListener?.broadcastUpdate(ON_CHARACTERISTICS_READ)

    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        mListener?.broadcastUpdate(ON_CHARACTERISTICS_WRITE)
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        mListener?.broadcastUpdate(ON_SERVICES_DISCOVERED)
        searchForCharacteristics()

    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?
    ) {


        mListener?.broadcastUpdate(ON_CHARACTERISTICS_CHANGED)
        read(characteristic?.value)
    }

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            Log.i("OnConnectionChange", "Device connected ...Attempting service discovery...")
            bluetoothGatt?.discoverServices()
            //executor.submit(signalRunnable)// START THIS FOR RSSI

            mListener?.broadcastUpdate(DEVICE_CONNECTED)
            // these Broadcasts will update the notification corresponding to bluetooth event


        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

        }
    }


    private fun searchForCharacteristics() {
        val serviceList = bluetoothGatt!!.services
        for (bluetoothGattService in serviceList) {
            for (c in bluetoothGattService.characteristics) {
                if (bluetoothGattService.uuid == uuidService) {
                    val characteristicList = bluetoothGattService.characteristics
                    for (characteristic in characteristicList) {
                        if (characteristic.uuid == uuidCharacteristic) {
                            this.characteristic = characteristic
                            Log.e("Searching..", "found")
                           bluetoothGatt?.setCharacteristicNotification(characteristic, true)
                        }
                    }
                }
            }
        }

    }


    private fun getSignalLevels(rssi :Int,status :Int)
    {
        if( -90< rssi && rssi < -48  )
        {
          //  mListener?.broadcastUpdate(Constants.LOW_RSSI)
            //More testing needed for range
        }

    }








   fun write(instruction: String) {
        if (characteristic != null) {
            characteristic?.setValue(instruction)
            bluetoothGatt?.writeCharacteristic(characteristic)
        }
    }





    private fun read(data: ByteArray?) {
        for (element in data!!) {
            receivedValue.append(element.toChar())
        }
        Log.e("receiving", receivedValue.toString())
        receivedValue.clear()

    }


    fun connect(address: String?) {
            bluetoothGatt = bluetoothAdapter.getRemoteDevice(address)?.connectGatt(context, false, this)
    }


    fun onDone() {
        Log.i("TAG","OnDone")
    }

    fun startAutoMode() {
        Log.i("TAG","startAutoMode")
        mListener?.broadcastUpdate(DISINFECTION_IN_PROGRESS)
    }

    fun shutdownService() {
        Log.i("TAG","shutdownService")
    }

    fun disconnect() {
        Log.i("TAG","disconnect")
    }

    fun onDeviceOutOfRange() {
        Log.i("TAG","device out of range")
    }

    fun onAutoModeChosen() {
        Log.i("TAG","on Auto mode chosen")
    }

    fun onDisinfectionProcessStarted() {
        Log.i("TAG","on disinfection started")
    }

    fun onDisinfectionComplete() {
        Log.i("TAG","on disinfection complete")
    }

    fun onBatteryLow() {
        Log.i("TAG","onBattery low")
    }

    fun onDeviceRSSIlow() {
        Log.i("TAG","on device with low rssi")
    }

    fun onDisconnectionInProgress() {
        Log.i("TAG","on disinfection progress")
    }

    fun stopDisinfection() {
        Log.i("TAG","stop disinfection")
    }








    }