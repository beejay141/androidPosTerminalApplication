package com.iisysgroup.androidlite.vas.internet.smile

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.utils.SharedPreferenceUtils
import com.itex.richard.payviceconnect.model.SmileModel
import com.itex.richard.payviceconnect.wrapper.PayviceServices
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_smile.*
import org.jetbrains.anko.indeterminateProgressDialog

class SmileLookup : AppCompatActivity(), SmileDataAdapter.SmileDataClickListener {
    override fun onItemClicked(itemClicked : SmileModel.Bundle) {
        val intent = Intent(this@SmileLookup, BuyBundle::class.java)
        intent.putExtra("smile_extra", itemClicked)
        startActivity(intent)
    }

    private val progressDialog by lazy {
        indeterminateProgressDialog(message = "Loading available plans").also {
            it.setCancelable(false)
        }
    }

    private val mPayviceServices by lazy {
        PayviceServices.getInstance(this)
    }

    private val mPayviceUsername by lazy {
        SharedPreferenceUtils.getPayvicePassword(this)
    }

    private val mPayvicePassword by lazy {
        SharedPreferenceUtils.getPayvicePassword(this)
    }


    private val mPayviceWalletId by lazy {
        SharedPreferenceUtils.getPayviceWalletId(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smile)

        progressDialog.show()

        lookup()


    }

    private fun lookup(){
        val details = SmileModel.SmileGetBudlesRequest(mPayviceWalletId, mPayviceUsername, mPayvicePassword, "")

        mPayviceServices.SmileGetBundles(details)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(object : Observer<SmileModel.SmileGetBundleResponse>{
                    override fun onSubscribe(d: Disposable) {
                        Log.d("OkH", d.toString())
                    }

                    override fun onNext(t: SmileModel.SmileGetBundleResponse) {
                        progressDialog.dismiss()
                        val adapter = SmileDataAdapter(this@SmileLookup, this@SmileLookup, t.bundles!!)
                        smileRecyclerView.layoutManager = LinearLayoutManager(this@SmileLookup, LinearLayoutManager.VERTICAL, false)
                        smileRecyclerView.adapter = adapter
                    }

                    override fun onError(e: Throwable) {
                        progressDialog.dismiss()

                        Log.d("OkH", e.message.toString())
                        e.printStackTrace()
                    }

                    override fun onComplete() {
                        Log.d("OkH", "Completed")
                    }



                })
    }

 /*   private fun handleTopup(accountNumber: String) {
        progressDialog.show()

        val topupRequest = SmileModel.SmileTopUpRequestDetails(accountNumber, "1000", mPayviceWalletId, mPayviceUsername, mPayvicePassword, "", "cash")
        mPayviceServices.SmileTopUp(topupRequest).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .doOnError {
                    Log.d("OkH", it.toString())
                }
                .subscribe {
                    Log.d("OkH", it.toString())
                }
    }*/



}
