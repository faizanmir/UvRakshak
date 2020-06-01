package avishkaar.com.uv_rakshak.helpers

import android.os.CountDownTimer
import android.util.Log


class CountdownCounter(var millisInFuture: Long, var countDownInterval: Long) :CountDownTimer(millisInFuture, countDownInterval){


    interface OnTimerStartedListener{
        fun onTick(millisInFuture: Long)
        fun onFinish()
    }

    var listenerMap  =  hashMapOf<String ,OnTimerStartedListener?>()



    fun registerOnTimerStartedListener(id:String,onTimerStartedListener: OnTimerStartedListener){
        listenerMap[id] =  onTimerStartedListener
    }

    fun unregisterOnTimerStartedListener(id: String)
    {
        listenerMap[id] =  null

    }





    override fun onFinish() {
        notifyOnFinishToAllListeners()
    }

    override fun onTick(millisUntilFinished: Long) {
     notifyOnTickToAllListeners(millisUntilFinished)
    }


    private fun notifyOnTickToAllListeners(millisUntilFinished: Long)
    {
        for (mListener in listenerMap.values)
        {
            mListener?.onTick(millisUntilFinished)
        }
    }

    private fun notifyOnFinishToAllListeners()
    {
        for (mListener in listenerMap.values)
        {
            mListener?.onFinish()
        }
    }
}