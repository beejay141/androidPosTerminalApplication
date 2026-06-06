package com.iisysgroup.androidlite.payments_menu.handlers

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.Transformations
import android.util.Log
import com.iisysgroup.poslib.commons.emv.EmvCard
import com.iisysgroup.poslib.commons.emv.EmvTransactionType
import com.iisysgroup.poslib.deviceinterface.interactors.EmvInteractor
import com.iisysgroup.poslib.host.Host
import com.iisysgroup.poslib.host.HostInteractor
import com.iisysgroup.poslib.host.dao.PosLibDatabase
import com.iisysgroup.poslib.host.entities.ConnectionData
import com.iisysgroup.poslib.host.entities.TransactionResult
import com.iisysgroup.poslib.utils.InputData
import com.iisysgroup.poslib.utils.TransactionData
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.experimental.runBlocking

/**
 * Created by Agbede on 3/3/2018.
 */
class Purchase(owner: LifecycleOwner, db: PosLibDatabase, val inputData: InputData,
               hostInteractor: HostInteractor, connData: ConnectionData,
                emvInteractor: EmvInteractor, pinKey : String? = null, sessionKey : String? = null) : BaseHandler(owner, db, hostInteractor, connData, emvInteractor,pinKey,sessionKey) {


    init {
        emvInteractor.observeStatus().subscribe {
            Log.d("PURCHASE_DEVICE_STATUS", it.state.toString())

        }
    }

    //This method does not work. /
     override fun getTransactionResult(): LiveData<TransactionResult> = runBlocking {

        val cardData = emvInteractor.startEmvTransaction(inputData.amount, inputData.additionalAmount, EmvTransactionType.EMV_PURCHASE)


        Transformations.switchMap(LiveDataReactiveStreams.fromPublisher(cardData.toFlowable())) {
            val transactionData = TransactionData(inputData, it, configData, keyHolder)


            LiveDataReactiveStreams.fromPublisher(hostInteractor.getTransactionResult(Host.TransactionType.PURCHASE, connData, transactionData, null, null).toFlowable())
        }
    }


    fun getTransactionResult2(): Single<TransactionResult> {
        val cardData = emvInteractor.startEmvTransaction(inputData.amount,
                inputData.additionalAmount, EmvTransactionType.EMV_PURCHASE).subscribeOn(Schedulers.io())


        return cardData.flatMap {
            val transactionData = TransactionData(inputData, it, configData, keyHolder)
            hostInteractor.getTransactionResult(Host.TransactionType.PURCHASE, connData, transactionData, sessionKey, pinKey).subscribeOn(Schedulers.io())
        }
    }


}


