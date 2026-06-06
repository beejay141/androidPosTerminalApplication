package com.iisysgroup.androidlite.cardpaymentprocessors

import android.app.Application
import com.iisysgroup.androidlite.App
import com.iisysgroup.poslib.host.entities.TransactionResult
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class DbManager(application : Application) {
    private val db by lazy {
        (application as App).db
    }

    fun saveTransactionData(transactionResult : TransactionResult){
        Single.fromCallable {
            db.transactionResultDao.save(transactionResult)
        }.subscribeOn(Schedulers.io())
                .subscribe { _ ->

                }

    }





    fun retrieveTransactionByRrn(rrn : String) = db.transactionResultDao.get(rrn)

}