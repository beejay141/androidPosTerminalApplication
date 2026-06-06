package com.iisysgroup.androidlite.cardpaymentprocessors

import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.gson.Gson
import com.iisysgroup.androidlite.App
import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.payments_menu.BasePaymentActivity
import com.iisysgroup.androidlite.payments_menu.handlers.Purchase
import com.iisysgroup.poslib.ISO.GTMS.GtmsKeyProcessor
import com.iisysgroup.poslib.ISO.POSVAS.PosvasKeyProcessor
import com.iisysgroup.poslib.host.entities.TransactionResult
import com.iisysgroup.poslib.utils.AccountType
import com.iisysgroup.poslib.utils.InputData
import com.itex.richard.payviceconnect.wrapper.PayviceServices
import com.pax.app.Models.PaxConfigData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.indeterminateProgressDialog

class PurchaseProcessor : BaseCardPaymentProcessor(){
    //Receives amount, additional amount, account type,
    //Returns value to calling library or class


    private val mAmount by lazy {
        intent.getLongExtra(BasePaymentActivity.TRANSACTION_AMOUNT, 0L)
    }

    private val mAdditionalAmount by lazy {
        intent.getLongExtra(BasePaymentActivity.TRANSACTION_ADDITIONAL_AMOUNT, 0L)
    }

    private val mAccountType by lazy {
        intent.getSerializableExtra(BasePaymentActivity.TRANSACTION_ACCOUNT_TYPE) as AccountType
    }


    override fun initializeDefaultUI(): DefaultUIModel {
        return DefaultUIModel(transactionTitle = "Purchase", amount = mAmount)
    }

    lateinit var mProgressDialog: ProgressDialog;


    public var broadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val tmpType = intent!!.getByteExtra("TYPE", (-1).toByte())
            skDevice.setReaderType(tmpType);
//            PaxDevice.setReadType(tmpType);
            if (!cardSeen)
            {
//                if (context != null) {
//                    context.indeterminateProgressDialog("Processing"){
//                        setCancelable(false)
//                    }
//                }
                startEmvTransaction()
                cardSeen = true
            }

        }
    };


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launch(CommonPool) {

            var conData = mDb.configDataDao.get()
            var keyHld =  mDb.keyHolderDao.get()

            launch (UI){

                configData = conData
                paxConfigData = PaxConfigData()
                paxConfigData.TerminalId = mConnectionData.terminalID
                paxConfigData.key03015 = configData.getConfigData("03015").toString()
                paxConfigData.key05003 = configData.getConfigData("05003").toString()
                paxConfigData.key06003 = configData.getConfigData("06003").toString()
                paxConfigData.key08004 = configData.getConfigData("08004").toString()
                paxConfigData.key52040 = configData.getConfigData("52040").toString()

                skDevice.setPaxConfigData(paxConfigData);

                keyHolder = keyHld
                val  hostString = getString(R.string.key_host_type)
                val hostKey = sharedPreferences.getString(hostString, "")
                val host = when (hostKey) {
                    "POSVAS" -> {

                        Log.d("pinkey-tmk",keyHolder.masterKey);
                        var clearMk = PosvasKeyProcessor.getMasterKey(keyHolder.masterKey,keyHolder.isTestPlatform);

                        Log.d("pinkey",keyHolder.isTestPlatform.toString());
                        Log.d("pinkey",clearMk);

                        var clearPk = PosvasKeyProcessor.decryptKey(keyHolder.pinKey,clearMk);
                        skDevice.clearPinKey = clearPk;

                        pinKey = clearPk;
                        sessionKey = PosvasKeyProcessor.decryptKey(keyHolder.sessionKey,clearMk);

                        Log.d("pinkey",clearPk);
                    }
                    else -> {
                        var clearMk = GtmsKeyProcessor.getMasterKey(keyHolder.masterKey,keyHolder.isTestPlatform);
                        var clearPk = GtmsKeyProcessor.decryptKey(keyHolder.pinKey,clearMk);
                        skDevice.clearPinKey = clearPk;

                        pinKey = clearPk;
                        sessionKey = GtmsKeyProcessor.decryptKey(keyHolder.sessionKey,clearMk);
                    }
                }

            }

        }

        skDevice.startDetector(broadcastReceiver)

    }


    private fun startEmvTransaction()
    {
        mProgressDialog = this.indeterminateProgressDialog(message = "Processing") { setCancelable(false) }
        Log.d("ImplEmv","amp amt "+mAmount)
        val inputData = InputData(mAmount, mAdditionalAmount, mAccountType)
        val purchase = Purchase(this, mDb, inputData, mHostInteractor, mConnectionData, mEmvInteractor,pinKey,sessionKey)

        purchase.getTransactionResult2()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { result, error ->
                    error?.let {
                        it.printStackTrace()
                    }

                    result?.let {
                        Log.d("OkHTransactionResult",  it.toString())

                        mEmvInteractor.processOnlineResponse(it.responseCode,it.authID ,it.issuerAuthData91, it.issuerScript71, it.issuerScript72)


                        DbManager(application).saveTransactionData(it)
                        setTransactionRrn(it.RRN)
                    }
                }
    }


}
