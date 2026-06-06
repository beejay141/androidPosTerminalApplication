package com.iisysgroup.androidlite.vas.activity.Genesis

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.utils.NetworkUtils
import com.iisysgroup.androidlite.vas.activity.Genesis.adapters.MoviesHouseAdapters
import com.itex.richard.payviceconnect.model.Genesis
import com.itex.richard.payviceconnect.wrapper.PayviceServices
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_movie_house.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.indeterminateProgressDialog

class MovieHouse : AppCompatActivity() {

    private val mProgressDialog by lazy {
        indeterminateProgressDialog("Processing")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_house)
        movieHouse_title.setText("Movies House")
        back_button_movhouse.setOnClickListener {
            onBackPressed()
        }
        val services = PayviceServices.getInstance(this)
        if (NetworkUtils.isConnectionAvailable(this@MovieHouse)) {
            mProgressDialog.show()
            services.GenesisGetCinemas().subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Observer<Genesis.GenesisResponses> {
                        override fun onSubscribe(d: Disposable) {

                        }

                        override fun onNext(genesisResponses: Genesis.GenesisResponses) {
                            if (!genesisResponses.error) {
                                setUpRecyclerView(genesisResponses.data!!)
                                mProgressDialog.dismiss()
                            } else {
                                mProgressDialog.dismiss()
                                alert {
                                    title = "Error"
                                    message = "Error Getting the movies house"
                                }.show()
                            }

                        }

                        override fun onError(e: Throwable) {

                        }

                        override fun onComplete() {

                        }
                    })
        }else{
            Snackbar.make(movies_linear,
                    "No Internet Connection", Snackbar.LENGTH_INDEFINITE).show()
        }
    }



    //Prepared the recycler view
    private fun setUpRecyclerView(cimaHouses: List<Genesis.CimaHouse>) {
        val adapter = MoviesHouseAdapters(cimaHouses, this)
        recyclerView.adapter = adapter
        val mLinearLayoutManagerVertical = LinearLayoutManager(this) // (Context context, int spanCount)
        mLinearLayoutManagerVertical.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = mLinearLayoutManagerVertical
        recyclerView.itemAnimator = DefaultItemAnimator()
    }
}
