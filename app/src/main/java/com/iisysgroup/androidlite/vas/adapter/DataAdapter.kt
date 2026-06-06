package com.iisysgroup.androidlite.vas.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.vas.airtime_and_data.DataModel
import java.util.*

class DataAdapter(private var dataItemsArrayList: ArrayList<DataModel.DataResponseElements>, private var context: Context, private var listener: DataClickListener) : RecyclerView.Adapter<DataAdapter.DataViewHolder>() {

    interface DataClickListener {
        fun onDataItemClick(data: ArrayList<DataModel.DataResponseElements>, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.individual_data_item, parent, false)
        return DataViewHolder(view)
    }

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        /*if (position % 2 == 0)
            holder.itemView.setBackgroundColor(context.resources.getColor(R.color.recyclerview_dark))
        else
            holder.itemView.setBackgroundColor(context.resources.getColor(R.color.recyclerview_light))*/
        holder.dataTitle.text = String.format("₦%s - %s", dataItemsArrayList[position].amount, dataItemsArrayList[position].value)
        holder.validity.text = dataItemsArrayList[position].duration
    }


    override fun getItemCount(): Int {
        return dataItemsArrayList.size
    }

    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var dataTitle: TextView = itemView.findViewById(R.id.dataTitle)
        var validity : TextView = itemView.findViewById(R.id.dataValidity)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            listener.onDataItemClick(dataItemsArrayList, adapterPosition)
        }
    }
}
