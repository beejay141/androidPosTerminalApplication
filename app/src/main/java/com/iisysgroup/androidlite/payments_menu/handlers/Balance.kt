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
import com.iisysgroup.poslib.utils.AccountType
import com.iisysgroup.poslib.utils.InputData
import com.iisysgroup.poslib.utils.TransactionData
import kotlinx.coroutines.experimental.runBlocking

/**
 * Created by Agbede on 3/4/2018.
 */
class Balance(owner: LifecycleOwner, db: PosLibDatabase, hostInteractor: HostInteractor, connData: ConnectionData,
              emvInteractor : EmvInteractor, private val accountType: AccountType): BaseHandler(owner, db, hostInteractor, connData, emvInteractor){


    override fun getTransactionResult(): LiveData<TransactionResult> = runBlocking {
        val cardData = emvInteractor.startEmvTransaction(0, 0, EmvTransactionType.EMV_INQUIRY)

        Transformations.switchMap(LiveDataReactiveStreams.fromPublisher(cardData.toFlowable())) {
            val transactionData = TransactionData(InputData(0,0,accountType ), it, configData, keyHolder)

            LiveDataReactiveStreams.fromPublisher(hostInteractor.getTransactionResult(Host.TransactionType.BALANCE_INQUIRY, connData, transactionData, null, null).toFlowable())
        }
    }

}