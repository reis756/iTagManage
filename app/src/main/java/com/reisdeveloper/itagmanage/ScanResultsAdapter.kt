package com.reisdeveloper.itagmanage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.clj.fastble.data.BleDevice

internal class ScanResultsAdapter(
    private val onClickListener: (BleDevice) -> Unit
) : RecyclerView.Adapter<ScanResultsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val device: TextView = itemView.findViewById(android.R.id.text1)
        val rssi: TextView = itemView.findViewById(android.R.id.text2)
    }

    private val data = mutableListOf<BleDevice>()

    fun addScanResult(bleScanResult: BleDevice) {
        data.withIndex()
            .firstOrNull { it.value.device == bleScanResult.device }
            ?.let {
                data[it.index] = bleScanResult
                notifyItemChanged(it.index)
            }
            ?: run {
                with(data) {
                    add(bleScanResult)
                    sortBy { it.device.address }
                }
                notifyDataSetChanged()
            }
    }

    fun clearScanResults() {
        data.clear()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(data[position]) {
            holder.device.text = String.format("%s (%s)", device.address, device.name)
            holder.rssi.text = String.format("RSSI: %d", rssi)
            holder.itemView.setOnClickListener { onClickListener(this) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        LayoutInflater.from(parent.context)
            .inflate(android.R.layout.two_line_list_item, parent, false)
            .let { ViewHolder(it) }
}
