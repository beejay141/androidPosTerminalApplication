package com.iisysgroup.androidlite.vas.internet.smile

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.iisysgroup.androidlite.R
import com.itex.richard.payviceconnect.model.SmileModel

class SmileDataAdapter(val context : Context, val listener : SmileDataAdapter.SmileDataClickListener, val list : List<SmileModel.Bundle>) : RecyclerView.Adapter<SmileDataAdapter.SmileViewHolder>() {

    interface SmileDataClickListener {
        fun onItemClicked(itemClicked : SmileModel.Bundle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmileViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.individual_smile_data, parent, false)
        return SmileViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: SmileViewHolder, position: Int) {
        holder.amount?.text = "₦${(list[position].displayPrice / 100)}"
        /*val validity = if (list[position].validity == 0) "" else " - ${list[position].displayPrice}"*/
        holder.description?.text = list[position].name + " - " + list[position].validity
    }

    inner class SmileViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        init {
            itemView?.setOnClickListener(this@SmileViewHolder)
        }
        override fun onClick(v: View?) {
            listener.onItemClicked(list[adapterPosition])
        }

        val description = itemView?.findViewById<TextView>(R.id.smile_data_description)
        val amount = itemView?.findViewById<TextView>(R.id.smile_data_amount)


    }
}