package avishkaar.com.uv_rakshak.adapters

import android.bluetooth.BluetoothDevice
import android.graphics.Color
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import avishkaar.com.uv_rakshak.R

class BluetoothListAdapter(var  bluetoothArrayList:ArrayList<BluetoothDevice?>,var mListerner: OnDeviceSelectedListener) : RecyclerView.Adapter<BluetoothListAdapter.BluetoothViewHolder>() {

    var sparseBooleanArray  : SparseBooleanArray  =  SparseBooleanArray(bluetoothArrayList.size)

    interface OnDeviceSelectedListener {
        fun onDeviceSelected(bluetoothDevice: BluetoothDevice?)
    }




    fun listListener(bluetoothArrayList: ArrayList<BluetoothDevice?>)
    {
        this.bluetoothArrayList  =  bluetoothArrayList
        notifyDataSetChanged()
    }


    inner class BluetoothViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        init {
            itemView.setOnClickListener(this)
        }
        var backgroundHolder: CardView =  itemView.findViewById(R.id.backgrondForDeviceItem)
        var deviceName: TextView = itemView.findViewById(R.id.deviceName)
        override fun onClick(p0: View?) {
            sparseBooleanArray.clear()
            mListerner.onDeviceSelected(bluetoothArrayList[adapterPosition])
            sparseBooleanArray.put(adapterPosition,true)
            notifyDataSetChanged()

        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BluetoothViewHolder {
        return BluetoothViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_item_bluetooth,
                parent,
                false
            )
        )
    }


    override fun getItemCount(): Int {
        return bluetoothArrayList.size
    }


    override fun onBindViewHolder(holder: BluetoothViewHolder, position: Int) {
        if (sparseBooleanArray[position]) {
            holder.backgroundHolder.setCardBackgroundColor(Color.parseColor("#a8e4e9"))
        }else
        {
            holder.backgroundHolder.setCardBackgroundColor(Color.parseColor("#ffffff"))
        }

        holder.deviceName.text = bluetoothArrayList[position]?.name


    }
}