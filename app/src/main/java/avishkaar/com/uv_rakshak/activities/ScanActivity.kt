package avishkaar.com.uv_rakshak.activities

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import avishkaar.com.uv_rakshak.R
import avishkaar.com.uv_rakshak.adapters.BluetoothListAdapter
import avishkaar.com.uv_rakshak.constants.Constants
import avishkaar.com.uv_rakshak.services.BleService
import avishkaar.com.uv_rakshak.singletons.Singleton
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothStatus
import kotlinx.android.synthetic.main.activity_scan.*

class ScanActivity : AppCompatActivity(), BluetoothListAdapter.OnDeviceSelectedListener
    ,BluetoothService.OnBluetoothScanCallback,BluetoothService.OnBluetoothEventCallback{
    var scanResult = ArrayList<BluetoothDevice?> ()
    var bluetoothScanner: BluetoothLeScanner? = null
    var bluetoothAdapter: BluetoothAdapter? = null
    lateinit var handler:Handler
    lateinit var adapter: BluetoothListAdapter
    var selectedDevice :BluetoothDevice? =  null
    var service:BluetoothService ? =  null


    override fun onCreate(savedInstanceState: Bundle?)  {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        init()

    }

    override fun onDeviceSelected(bluetoothDevice: BluetoothDevice?) {
        selectedDevice  =  bluetoothDevice
    }

    private fun init()
    {

        service  = BleService.Companion.BluetoothInitializerClass.service
        service?.setOnScanCallback(this)
        service?.setOnEventCallback(this)

        service?.startScan()

        adapter = BluetoothListAdapter(scanResult,this)

        bluetoothRecyclerView.adapter = adapter
        bluetoothRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        handler = Handler()

        handler.postDelayed({ service?.stopScan() },5000)


        if(BluetoothAdapter.getDefaultAdapter() != null) {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            bluetoothScanner = bluetoothAdapter?.bluetoothLeScanner
        }

        rescan.setOnClickListener{
            if(service?.status  == BluetoothStatus.CONNECTED) {
                service?.disconnect()
            }
            service?.startScan()
        }

        back_button.setOnClickListener {
            service?.stopScan()
            finish()
        }



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

    override fun onBackPressed() {
        finish()
    }

    fun connectToSelectedDevice(view: View) {
        service?.stopScan()
        if (selectedDevice  !=  null) {
            BleService.Companion.BluetoothInitializerClass.device  =  selectedDevice
            service?.disconnect()
            service?.connect(selectedDevice)
            val intent = Intent(this, MainActivity::class.java).also {
                it.putExtra(Constants.DEVICE_NAME, selectedDevice?.name)
               // it.putExtra(Constants.DEVICE_ADDRESS ,selectedDevice?.address  )
            }
            startActivity(intent)
        }else{
            Toast.makeText(this,"Please select a Uv Rakshak from the list to continue",Toast.LENGTH_LONG).show()
        }


    }

    override fun onStopScan() {
        scanProgressBar.visibility = View.INVISIBLE
    }

    override fun onStartScan() {
        scanProgressBar.visibility = View.VISIBLE
    }

    override fun onDeviceDiscovered(device: BluetoothDevice?, rssi: Int) {
        if(device?.name!=null && !scanResult.contains(device)) {
            scanResult.add(device)
            adapter.listListener(scanResult)
            scanProgressBar.visibility  = View.INVISIBLE
        }
    }

    override fun onDataRead(buffer: ByteArray?, length: Int) {

    }

    override fun onStatusChange(status: BluetoothStatus?) {
    }

    override fun onDataWrite(buffer: ByteArray?) {

    }

    override fun onToast(message: String?) {

    }

    override fun onDeviceName(deviceName: String?) {

    }


    override fun onPause() {
        super.onPause()
        service?.setOnEventCallback(null)
        service?.setOnEventCallback(null)
    }


}
