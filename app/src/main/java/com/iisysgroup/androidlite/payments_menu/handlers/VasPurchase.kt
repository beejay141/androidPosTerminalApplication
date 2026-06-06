package com.iisysgroup.androidlite.payments_menu.handlers

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.Transformations
import android.util.Log
import com.google.gson.Gson
import com.iisysgroup.androidlite.login.Helper
import com.iisysgroup.androidlite.login.securestorage.SecureStorage
import com.iisysgroup.poslib.commons.emv.EmvTransactionType
import com.iisysgroup.poslib.deviceinterface.interactors.EmvInteractor
import com.iisysgroup.poslib.host.Host
import com.iisysgroup.poslib.host.HostInteractor
import com.iisysgroup.poslib.host.dao.PosLibDatabase
import com.iisysgroup.poslib.host.entities.ConfigData
import com.iisysgroup.poslib.host.entities.ConnectionData
import com.iisysgroup.poslib.host.entities.KeyHolder
import com.iisysgroup.poslib.host.entities.TransactionResult
import com.iisysgroup.poslib.utils.InputData
import com.iisysgroup.poslib.utils.TransactionData
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class VasPurchase(owner: LifecycleOwner, db: PosLibDatabase, val inputData: InputData,
                  val hostInteractor: HostInteractor, val connData: ConnectionData,
                  val emvInteractor: EmvInteractor, val configData: ConfigData, val keyHolder: KeyHolder)  {


    init {
        emvInteractor.observeStatus().subscribe {
            Log.d("VAS_PURCHASE", it.state.toString())
        }
    }


    fun getTransactionResult(): Single<TransactionResult> {
        val cardData = emvInteractor.startEmvTransaction(inputData.amount,
                inputData.additionalAmount, EmvTransactionType.EMV_PURCHASE).subscribeOn(Schedulers.io())


        return cardData.flatMap {
            val transactionData = TransactionData(inputData, it, configData, keyHolder)


            //Stores the transaction data to a SharedPraeference
            SecureStorage.store(Helper.TRANSACTION_DATA, Gson().toJson(transactionData))




            val connnectionData= (Gson().toJson( connData))
            Log.d("reversal conectionData Raw  >>>>>>>",connnectionData)

            val keyHold = Gson().toJson( keyHolder)

            Log.d("reversal keyHolder Raw  >>>>>>>",keyHold)
           SecureStorage.store(Helper.CONNECTION_DATA,connnectionData )
            SecureStorage.store(Helper.KEY_HOLDER, keyHolder)

            Log.d("reversal conectionData >>>>>>>", SecureStorage.retrieve(Helper.CONNECTION_DATA,""))



            Log.d("reversal KeyHolderData >>>>>>>", SecureStorage.retrieve(Helper.KEY_HOLDER,""))








            hostInteractor.getTransactionResult(Host.TransactionType.PURCHASE, connData, transactionData, keyHolder.sessionKey, keyHolder.pinKey).subscribeOn(Schedulers.io())








        }
    }


}


