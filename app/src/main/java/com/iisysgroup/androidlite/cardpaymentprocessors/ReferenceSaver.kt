package com.iisysgroup.androidlite.cardpaymentprocessors

import android.app.Application
import com.iisysgroup.androidlite.App
import com.iisysgroup.poslib.host.entities.TransactionResult
import kotlinx.coroutines.experimental.async

class ReferenceSaver(application : Application) {

    interface ReferenceSavedListener  {
        fun onSaved(transactionResult: TransactionResult)
        fun onFailed(message : String)
    }

    //Retrieve room db instance
    private val mDb by lazy {
        (application as App).db
    }

    fun saveReference(transactionResult : TransactionResult, listener : ReferenceSavedListener){
        async {
            try {
                mDb.transactionResultDao.save(transactionResult)
                listener.onSaved(transactionResult)
            } catch (e : Exception){
                listener.onFailed(e.toString())
            } finally {
                //todo set listener to null
            }


        }
    }
}