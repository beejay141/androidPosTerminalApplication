package com.iisysgroup.androidlite.vas.activity.Genesis.adapters

import android.content.Context
import android.preference.PreferenceManager
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.utils.SharedPreferenceUtils
import com.itex.richard.payviceconnect.model.Genesis
import com.itex.richard.payviceconnect.wrapper.PayviceServices
import org.jetbrains.anko.alert
import org.jetbrains.anko.indeterminateProgressDialog
import java.text.SimpleDateFormat
import java.util.*

class MovieListAdapter(internal var mData: List<Genesis.Movies>, internal var context: Context, internal var onMovieSelectedListener: OnMovieSelectedListener) : RecyclerView.Adapter<MovieListAdapter.MyViewHolder>() {

    interface OnMovieSelectedListener {
        fun startPayment(movies: Genesis.Movies)
    }

    private val layoutInflater by lazy {
        LayoutInflater.from(context)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieListAdapter.MyViewHolder {
        val view = layoutInflater.inflate(R.layout.mivie_view, parent, false)


        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieListAdapter.MyViewHolder, position: Int) {
        val movie = mData[position]
        holder.setData(movie)
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val movieImage by lazy {
            itemView.findViewById<ImageView>(R.id.movieimg)
        }

        private val name by lazy {
            itemView.findViewById<TextView>(R.id.name)
        }

        private val regularPrice by lazy {
            itemView.findViewById<TextView>(R.id.regularPrice)
        }
//        private val premiumPrice by lazy {
//            itemView.findViewById<TextView>(R.id.premiumPrice)
//        }
//        private val comboPrice by lazy {
//            itemView.findViewById<TextView>(R.id.comboPrice)
//        }
//        private val vipPrice by lazy {
//            itemView.findViewById<TextView>(R.id.vipPrice)
//        }

        private val time by lazy {
            itemView.findViewById<TextView>(R.id.movieTime)
        }

        private val movieView by lazy {
            itemView.findViewById<CardView>(R.id.cardview)
        }

        fun setData(movie: Genesis.Movies) {
//            Glide.with(context)
//                    .load(movie.poster)
//                    .placeholder(R.drawable.ic_genesis)
//                    .into(movieImage)

            Glide.with(context)
                    .load(movie.poster)
                    .apply(RequestOptions().placeholder(R.drawable.ic_genesis))
                    .into(movieImage)


            name.text = movie.short_title

            //   regularPrice.text = setPrice(movie.amount.regular);
//            premiumPrice.text = setPrice(movie.amount.premium);
//            comboPrice.text = setPrice(movie.amount.combo);
//            vipPrice.text = setPrice(movie.amount.vip);


            time.text = formatDate(movie.start_date + " " + movie.start_time)
            movieView.setOnClickListener {
                onMovieSelectedListener.startPayment(movie)

            }
        }
    }

    private fun setPrice(amount : Double?) : String
    {
        var price = "₦ ";
        if (amount != null)
            price += amount.toString()
        else price = "Price not available"

        return price;
    }

    private fun formatDate(date : String) : String
    {
        val d = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date)
        val cal = Calendar.getInstance()
        cal.setTime(d)
        return SimpleDateFormat("EEE, d MMM 'at' hh:mm aaa").format(cal.getTime())
    }



}

