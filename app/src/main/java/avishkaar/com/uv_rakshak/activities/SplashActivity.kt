package avishkaar.com.uv_rakshak.activities

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import avishkaar.com.uv_rakshak.R
import avishkaar.com.uv_rakshak.services.BleService
import avishkaar.com.uv_rakshak.singletons.Singleton
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothStatus
import pub.devrel.easypermissions.EasyPermissions


class SplashActivity : AppCompatActivity() {




    private var perms = arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION,
       android.Manifest.permission.ACCESS_FINE_LOCATION,
       android.Manifest.permission.BLUETOOTH,
       android.Manifest.permission.BLUETOOTH_ADMIN
        )


    var bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        if(BleService.Companion.BluetoothInitializerClass.service !=  null) {
            if (BleService.Companion.BluetoothInitializerClass.service?.status == BluetoothStatus.CONNECTED) {
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
        else {
                BleService.Companion.BluetoothInitializerClass(applicationContext).setService()
            }

    }

    fun proceedToScan(view: View) {
        if(EasyPermissions.hasPermissions(this,*perms ) && bluetoothAdapter.isEnabled && isLocationEnabled(this)){
            startActivity(Intent(this,ScanActivity::class.java))
        }

        else
        {
            EasyPermissions.requestPermissions(this,"Please grant the following permissions to use the app",1,*perms)
        }

        if(!isLocationEnabled(this))
        {
            makeAlertBox("Location not enabled",
                "Enable Location",
                "Turn on",
                "Dismiss",
                ( {startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))})).show()
        }

        if (!bluetoothAdapter.isEnabled){

            makeAlertBox("Bluetooth not enabled",
                "Enable Bluetooth",
                "Turn on",
                "Dismiss")
            {startActivityForResult( Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),1)}.show()

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this)
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

    private fun isLocationEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // This is new method provided in API 28
            val lm =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            lm.isLocationEnabled
        } else {
            // This is Deprecated in API 28
            val mode: Int = Settings.Secure.getInt(
                context.contentResolver, Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF
            )
            mode != Settings.Secure.LOCATION_MODE_OFF
        }
    }

    private fun makeAlertBox(title:String,message:String,positive:String,negative:String,action:()->Unit):AlertDialog{
        return AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positive) { _, _ -> action()}.setNegativeButton(negative) { dialog, _ ->dialog.dismiss()}
            .create()
    }
}