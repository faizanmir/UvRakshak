package avishkaar.com.uv_rakshak.helpers

import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothStatus

class BluetoothHelper1 :BluetoothService.OnBluetoothEventCallback{
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
}