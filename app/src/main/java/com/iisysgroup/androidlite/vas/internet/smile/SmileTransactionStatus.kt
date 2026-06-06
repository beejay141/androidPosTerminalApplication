package com.iisysgroup.androidlite.vas.internet.smile

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.utils.SharedPreferenceUtils
import com.itex.richard.payviceconnect.model.SmileModel
import com.itex.richard.payviceconnect.wrapper.PayviceServices
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_smile_transaction_status.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.indeterminateProgressDialog

class SmileTransactionStatus : AppCompatActivity() {

    private val mPayviceUsername by lazy {
        SharedPreferenceUtils.getPayviceUsername(this)
    }

    private val mPayvicePassword by lazy {
        SharedPreferenceUtils.getPayvicePassword(this)
    }


    private val mPayviceWalletId by lazy {
        SharedPreferenceUtils.getPayviceWalletId(this)
    }

    private val mPayviceServices by lazy {
        PayviceServices.getInstance(this)
    }

    private val progressDialog by lazy {
        indeterminateProgressDialog(message = "Getting customer details").also {
            it.setCancelable(false)
        }
    }

    private fun isValidated(): Boolean {
        if (smile_transaction_ref_number.text.toString().isEmpty()) {
            smile_transaction_ref_number.error = "Enter valid account number"
            return false
        }


        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smile_transaction_status)

        smile_continue_btn.setOnClickListener {
            if (isValidated()){
                val refNumber = smile_transaction_ref_number.text.toString()

                getTransactionStatus(refNumber)

            }
        }
    }

    private fun getTransactionStatus(refNumber: String) {
        val details = SmileModel.SmileTransactionStatusRequest(refNumber, mPayviceWalletId, mPayviceWalletId, mPayvicePassword, null)

        progressDialog.show()

        mPayviceServices.SmileTranscationStatus(details)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(object : Observer<SmileModel.SmileSuccessResponse> {
                    override fun onComplete() {
                    }

                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onNext(t: SmileModel.SmileSuccessResponse) {
                        Log.e("responses", t.reference)

                    }

                    override fun onError(it: Throwable) {
                        progressDialog.dismiss()
                        it.printStackTrace()
                        Log.e("error", it.message.toString())
                        alert {
                            title = "Response"
                            message = it.message.toString()
                        }.show()
                    }

                })
    }
}
