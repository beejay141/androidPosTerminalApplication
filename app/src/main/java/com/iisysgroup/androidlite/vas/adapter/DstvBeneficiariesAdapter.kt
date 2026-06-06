package com.iisysgroup.androidlite.vas.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.vas.cable.DstvBeneficiariesModel
import java.util.*

class DstvBeneficiariesAdapter(private var items: ArrayList<DstvBeneficiariesModel>, private var context: Context, private var listener: DataClickListener) : RecyclerView.Adapter<DstvBeneficiariesAdapter.BeneficiaryViewHolder>() {

    interface DataClickListener {
        fun onDataItemClick(data: ArrayList<DstvBeneficiariesModel>, position: Int)
    }

    override fun onBindViewHolder(holder: BeneficiaryViewHolder, position: Int) {
        if (position % 2 == 0)
            holder.itemView.setBackgroundColor(context.resources.getColor(R.color.recyclerview_dark))
        else
            holder.itemView.setBackgroundColor(context.resources.getColor(R.color.recyclerview_light))

        holder.dataTitle.text = String.format("%d. %s", position + 1, items[position].name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeneficiaryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.individual_data_item, parent, false)
        return BeneficiaryViewHolder(view)
    }


    override fun getItemCount(): Int {
        return items.size
    }

    inner class BeneficiaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var dataTitle: TextView = itemView.findViewById(R.id.dataTitle)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            listener.onDataItemClick(items, adapterPosition)
        }
    }
}
