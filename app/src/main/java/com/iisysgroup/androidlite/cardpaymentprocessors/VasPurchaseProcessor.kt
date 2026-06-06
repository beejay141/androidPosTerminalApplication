package com.iisysgroup.androidlite.cardpaymentprocessors

import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.payments_menu.BasePaymentActivity
import com.iisysgroup.androidlite.payments_menu.handlers.VasPurchase
import com.iisysgroup.poslib.ISO.GTMS.GtmsKeyProcessor
import com.iisysgroup.poslib.ISO.POSVAS.PosvasKeyProcessor
import com.iisysgroup.poslib.commons.emv.EmvCard
import com.iisysgroup.poslib.host.entities.ConfigData
import com.iisysgroup.poslib.host.entities.ConnectionData
import com.iisysgroup.poslib.host.entities.KeyHolder
import com.iisysgroup.poslib.utils.AccountType
import com.iisysgroup.poslib.utils.InputData
import com.pax.app.Models.PaxConfigData
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.toast

class VasPurchaseProcessor: BaseCardPaymentProcessor(){
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
        return DefaultUIModel(transactionTitle = "VAS Purchase", amount = mAmount)
    }

    lateinit var mProgressDialog: ProgressDialog;

    var broadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val tmpType = intent!!.getByteExtra("TYPE", (-1).toByte())
            skDevice.setReaderType(tmpType);
//            PaxDevice.setReadType(tmpType);
            if (!cardSeen)
            {
//                if (context != null) {
//                    try {
//                        context.progressDialog("Processing"){
//                            setCancelable(false)
//                        }
//                    }catch (ex : Exception) {}
//
//                }
                startEmvTransaction()
                cardSeen = true
            }

        }
    };

//    private val mDb by lazy {
//        (application as App).db
//    }

//    private val ampDevice by lazy {
//        AMPDevice(this)
//    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        Single.fromCallable {
//            mDb.vasTerminalDataDao.get()
//        }.subscribeOn(Schedulers.io())
//                .flatMap { vasTerminalDetails ->
//            val connectionData = ConnectionData(vasTerminalDetails.tid, "196.6.103.73", 5043, true)
//            val keyHolder = KeyHolder("", vasTerminalDetails.sessionKey, vasTerminalDetails.pinKey)
//
//            val configData = ConfigData()
//
//            paxConfigData = PaxConfigData()
//            paxConfigData.TerminalId = connectionData.terminalID
//
//            //Time out
//            configData.storeConfigData("04002", "90")
//
//            //Country Code
//            configData.storeConfigData("06003", vasTerminalDetails.countryCode)
//
//            //MCC
//            configData.storeConfigData("08004", vasTerminalDetails.mcc)
//
//
//            //Merchant's name - 40 length
//            configData.storeConfigData("52040", vasTerminalDetails.merchantName)
//
//            //Merchant Id - 15 length
//            configData.storeConfigData("03015", vasTerminalDetails.mid)
//
//            //Currency Code
//            configData.storeConfigData("05003", vasTerminalDetails.currencyCode)
//
//            paxConfigData.key03015 = configData.getConfigData("03015").toString()
//            paxConfigData.key05003 = configData.getConfigData("05003").toString()
//            paxConfigData.key06003 = configData.getConfigData("06003").toString()
//            paxConfigData.key08004 = configData.getConfigData("08004").toString()
//            paxConfigData.key52040 = configData.getConfigData("52040").toString()
//
//            val inputData = InputData(mAmount, mAdditionalAmount, mAccountType)
//
//            val vasPurchase = VasPurchase(this, mDb, inputData, mHostInteractor, connectionData, mEmvInteractor, configData, keyHolder)
//
//            vasPurchase.getTransactionResult()
//        }
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe { result, error ->
//
//                        result?.let {
//                            Log.d("OkHTransactionResult",  it.toString())
//
//                            val keyHolder = KeyHolder("", "", "")
//                            val pininfo = EmvCard.PinInfo(keyHolder.pinKey.toByteArray(), null, keyHolder.pinKey.toByteArray())
//                            mEmvInteractor.processOnlineResponse(it.responseCode, it.issuerAuthData91, it.issuerScript71, it.issuerScript72)
//                            setTransactionRrn(it.RRN)
//                            DbManager(application).saveTransactionData(it)
//
////                            val emvcard = EmvCard(it.cardHolderName, "", "",pininfo)
////
////                            val printer = Printer()
////
////                            val pfmJournal = PfmJournalGenerator(it, ConfigData(), printer = printer, isReceiptPrinted = true, vasCategory = "", vasProduct = "", cardData = emvcard).generateJournal()
////                            val pfmState = PfmStateGenerator(this@VasPurchaseProcessor, ampDevice).generateState()
////                            Log.d("pfmstate", pfmState.batteryLevel+" " +pfmState.chargingStatus)
////                            val pfmDetails = PfmDetails(pfmState, pfmJournal, it.RRN)
////
////                            async {
////
////                          Log.d("Pfm", pfmState.toString())
////                            Log.d("Pfm", pfmJournal.mcc)
////
////                                val response = PfmService.Factory.create().sendPfm(pfmDetails).await()
////                                Log.d("pfmresponse", response.message + " " + response.status)
//                            }
//
//                    error?.let {
//                        toast(it.message!!)
//                    }
//
//                    }


    }

    private fun startEmvTransaction()
    {
        mProgressDialog = this.indeterminateProgressDialog(message = "Processing") { setCancelable(false) }

        var varesr =    runBlocking(CommonPool){
            mDb.vasTerminalDataDao.get()
        }

        keyHolder =    runBlocking(CommonPool){
            mDb.keyHolderDao.get()
        }

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

                Log.d("pinkey",clearPk);
            }
            else -> {
                var clearMk = GtmsKeyProcessor.getMasterKey(keyHolder.masterKey,keyHolder.isTestPlatform);
                var clearPk = GtmsKeyProcessor.decryptKey(keyHolder.pinKey,clearMk);
                skDevice.clearPinKey = clearPk;
            }
        }

        var configData = ConfigData()
        configData.storeConfigData("06003", varesr.countryCode)
        //MCC
        configData.storeConfigData("08004", varesr.mcc)
        //Merchant's name - 40 length
        configData.storeConfigData("52040", varesr.merchantName)
        //Merchant Id - 15 length
        configData.storeConfigData("03015", varesr.mid)
        //Currency Code
        configData.storeConfigData("05003", varesr.currencyCode)

        paxConfigData = PaxConfigData()
        paxConfigData.TerminalId = varesr.tid
        paxConfigData.key03015 = varesr.mid
        paxConfigData.key05003 = varesr.currencyCode
        paxConfigData.key06003 = varesr.countryCode
        paxConfigData.key08004 = varesr.mcc
        paxConfigData.key52040 = varesr.merchantName

        skDevice.setPaxConfigData(paxConfigData)

        var keys = KeyHolder("" ,varesr.sessionKey,varesr.pinKey)
        val connectionData = ConnectionData(varesr.tid, "196.6.103.73", 5043, true)
        val inputData = InputData(mAmount, mAdditionalAmount, if(mAccountType == null){ AccountType.DEFAULT_UNSPECIFIED }else{mAccountType} )
        val purchase = VasPurchase(this, mDb, inputData, mHostInteractor, connectionData, mEmvInteractor, configData, keys)

        purchase.getTransactionResult()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { result, error ->
                    error?.let {
                        Log.d("error-vas", it.toString())
                        it.printStackTrace()
                    }


                    result?.let {
                        Log.d("OkHTransactionHere",  it.toString())
                        mEmvInteractor.processOnlineResponse(it.responseCode, it.issuerAuthData91, it.issuerScript71, it.issuerScript72)
                        if(it.transactionStatus == "Pending")
                            it.transactionStatus = "Timed-out";
                        DbManager(application).saveTransactionData(it)
                        setTransactionRrn(it.RRN)

                    }
                }

    }

    override fun onResume() {
        super.onResume()

        skDevice.startDetector(broadcastReceiver)
    }
}
