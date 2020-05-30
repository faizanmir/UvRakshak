package avishkaar.com.uv_rakshak.modelClasses

import avishkaar.com.uv_rakshak.helpers.BluetoothState

data class ActivityStateManagement (var isUvOn:Boolean?,
                                    var deviceName:String?,
                                    var connectionState: BluetoothState?,
                                    var isTimerOn:Boolean?,
                                    var isAutoModeOn:Boolean?){

}