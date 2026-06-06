package com.iisysgroup.androidlite.payments_menu.handlers

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.Transformations
import com.iisysgroup.poslib.commons.emv.EmvTransactionType
import com.iisysgroup.poslib.deviceinterface.interactors.EmvInteractor
import com.iisysgroup.poslib.host.Host
import com.iisysgroup.poslib.host.HostInteractor
import com.iisysgroup.poslib.host.dao.PosLibDatabase
import com.iisysgroup.poslib.host.entities.ConnectionData
import com.iisysgroup.poslib.host.entities.TransactionResult
import com.iisysgroup.poslib.utils.InputData
import com.iisysgroup.poslib.utils.TransactionData
import kotlinx.coroutines.experimental.runBlocking

/**
 * Created by Agbede on 3/19/2018.
 */
class Transfer(val lifecycleOwner: LifecycleOwner, db: PosLibDatabase, val inputData: InputData,
               hostInteractor: HostInteractor, connData: ConnectionData,
               device: EmvInteractor): BaseHandler(lifecycleOwner, db, hostInteractor, connData, device) {


    override fun getTransactionResult(): LiveData<TransactionResult> = runBlocking {
        val cardData = emvInteractor.startEmvTransaction(inputData.amount, inputData.additionalAmount, EmvTransactionType.EMV_PURCHASE)

        Transformations.switchMap(LiveDataReactiveStreams.fromPublisher(cardData.toFlowable())) {
            val transactionData = TransactionData(inputData, it, configData, keyHolder)


            LiveDataReactiveStreams.fromPublisher(hostInteractor.getTransactionResult(Host.TransactionType.PURCHASE, connData, transactionData, null, null).toFlowable())
        }
    }
}