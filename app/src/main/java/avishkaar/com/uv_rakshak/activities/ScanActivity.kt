package avishkaar.com.uv_rakshak.activities

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
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
import kotlinx.android.synthetic.main.activity_scan.*

class ScanActivity : AppCompatActivity(), BluetoothListAdapter.OnDeviceSelectedListener {
    var isScanning =  false
    var scanResult = ArrayList<BluetoothDevice?> ()
     var scanCallback:ScanCallback? = null
    var bluetoothScanner: BluetoothLeScanner? = null
    var bluetoothAdapter: BluetoothAdapter? = null
    lateinit var handler:Handler
    lateinit var adapter: BluetoothListAdapter
    var selectedDevice :BluetoothDevice? =  null


    override fun onCreate(savedInstanceState: Bundle?)  {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)


        scanCallback = object :ScanCallback(){
           override fun onScanFailed(errorCode: Int) {
               scanProgressBar.visibility = View.INVISIBLE
               Toast.makeText(this@ScanActivity,"Scan Failed",Toast.LENGTH_SHORT).show()
           }

           override fun onScanResult(callbackType: Int, result: ScanResult?) {

               if(result?.device?.name!=null && !scanResult.contains(result.device)) {
                   scanResult.add(result.device)
                   adapter.listListener(scanResult)
                   scanProgressBar.visibility  = View.INVISIBLE
               }


           }

           override fun onBatchScanResults(results: MutableList<ScanResult>?) {

           }
       }

        init()

        rescan.setOnClickListener{
            scanProgressBar.visibility = View.VISIBLE

            scanResult.clear()
            adapter.notifyDataSetChanged()

            if(!isScanning) {
                bluetoothScanner?.startScan(scanCallback)
                isScanning = true
                scanProgressBar.visibility = View.VISIBLE
                handler.postDelayed({
                    bluetoothScanner?.stopScan(scanCallback)
                    scanProgressBar.visibility = View.INVISIBLE
                }, 3000)

            }
            else
            {
                bluetoothScanner?.stopScan(scanCallback)
                isScanning = false
                bluetoothScanner?.startScan(scanCallback)

            }
        }

        back_button.setOnClickListener {
            finish()
        }

//        stopScan.setOnClickListener{
//            isScanning = false
//            bluetoothScanner?.stopScan(scanCallback)
//            scanProgressBar.visibility = View.INVISIBLE
//        }
    }

    override fun onDeviceSelected(bluetoothDevice: BluetoothDevice?) {
        bluetoothScanner?.stopScan(scanCallback)
        selectedDevice  =  bluetoothDevice


    }

    private fun init()
    {
        adapter = BluetoothListAdapter(scanResult,this)
        bluetoothRecyclerView.adapter = adapter
        bluetoothRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        handler = Handler()

        if(BluetoothAdapter.getDefaultAdapter() != null) {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            bluetoothScanner = bluetoothAdapter?.bluetoothLeScanner
        }


        bluetoothScanner?.startScan(scanCallback)
        isScanning  = true


        handler.postDelayed({if (isScanning){
            bluetoothScanner?.stopScan(scanCallback)
            scanProgressBar.visibility = View.INVISIBLE
            isScanning  = false
        } },3000)

        scanProgressBar.visibility = View.VISIBLE

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
        if (selectedDevice  !=  null) {

            val intent = Intent(this, MainActivity::class.java).also {
                it.putExtra(Constants.DEVICE_NAME, selectedDevice?.name)
                it.putExtra(Constants.DEVICE_ADDRESS ,selectedDevice?.address  )
            }
            startActivity(intent)
        }else{
            Toast.makeText(this,"Please select a Uv Rakshak from the list to continue",Toast.LENGTH_LONG).show()
        }


    }


}
