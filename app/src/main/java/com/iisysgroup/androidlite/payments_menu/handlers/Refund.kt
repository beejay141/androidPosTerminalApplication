package com.iisysgroup.androidlite.payments_menu.handlers

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.Transformations
import com.iisysgroup.poslib.ISO.common.IsoRefundProcessData
import com.iisysgroup.poslib.ISO.common.IsoReversalTransactionData
import com.iisysgroup.poslib.commons.emv.EmvTransactionType
import com.iisysgroup.poslib.deviceinterface.interactors.EmvInteractor
import com.iisysgroup.poslib.host.Host
import com.iisysgroup.poslib.host.HostInteractor
import com.iisysgroup.poslib.host.dao.PosLibDatabase
import com.iisysgroup.poslib.host.entities.ConnectionData
import com.iisysgroup.poslib.host.entities.TransactionResult
import com.iisysgroup.poslib.utils.InputData
import kotlinx.coroutines.experimental.runBlocking
/**
 * Created by Agbede on 3/4/2018.
 */
class Refund(val lifecycleOwner: LifecycleOwner, db: PosLibDatabase, val inputData: InputData,
              hostInteractor: HostInteractor, connData: ConnectionData,
              device: EmvInteractor, val stan: String, val prevTran: TransactionResult): BaseHandler(lifecycleOwner, db, hostInteractor, connData, device) {


    override fun getTransactionResult(): LiveData<TransactionResult> = runBlocking{


            val isoRefundProcessData = IsoRefundProcessData(prevTran.STAN, prevTran.isoTransmissionDateTime,
                    prevTran.RRN,prevTran.amount, prevTran.originalForwardingInstitutionCode)
            val cardData = emvInteractor.startEmvTransaction(inputData.amount, 0, EmvTransactionType.EMV_REFUND)

            Transformations.switchMap(LiveDataReactiveStreams.fromPublisher(cardData.toFlowable())){
                val isoRefundTransactionData = IsoReversalTransactionData(inputData, it, configData, keyHolder, isoRefundProcessData)
                LiveDataReactiveStreams.fromPublisher(hostInteractor.getTransactionResult(Host.TransactionType.REFUND, connData, isoRefundTransactionData, null, null).toFlowable())
            }
    }

}
