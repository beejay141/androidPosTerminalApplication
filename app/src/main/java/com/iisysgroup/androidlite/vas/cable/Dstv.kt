package com.iisysgroup.androidlite.vas.cable

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.iisysgroup.androidlite.*
import com.iisysgroup.androidlite.cardpaymentprocessors.VasPurchaseProcessor
import com.iisysgroup.androidlite.generators.PfmJournalGenerator
import com.iisysgroup.androidlite.generators.PfmStateGenerator
import com.iisysgroup.androidlite.login.Helper
import com.iisysgroup.androidlite.login.securestorage.SecureStorage
import com.iisysgroup.androidlite.models.Pfm
import com.iisysgroup.androidlite.models.ReceiptModel
import com.iisysgroup.androidlite.payments_menu.BasePaymentActivity
import com.iisysgroup.androidlite.payments_menu.TransactionProcessActivity
import com.iisysgroup.androidlite.utils.PinAlertUtils
import com.iisysgroup.androidlite.utils.SharedPreferenceUtils
import com.iisysgroup.androidlite.vas.activity.CableTVActivity
import com.iisysgroup.androidlite.vas.activity.energy.model.EnergyModel
import com.iisysgroup.androidlite.vas.services.DstvService
import com.iisysgroup.payvice.securestorage.SecureStorageUtils
import com.iisysgroup.poslib.deviceinterface.DeviceState
import com.iisysgroup.poslib.host.Host
import com.iisysgroup.poslib.host.entities.ConfigData
import com.iisysgroup.poslib.host.entities.TransactionResult
import com.iisysgroup.poslib.host.entities.VasTerminalData
import com.iisysgroup.poslib.utils.AccountType
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_ds_tv_vas.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.okButton
import org.jetbrains.anko.toast
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.*

class Dstv : AppCompatActivity() {

    private lateinit var beneficiaryName : String
    private lateinit var amount : String
    private lateinit var smartCardNo : String
    private lateinit var configData: ConfigData
    private lateinit var vasTerminalData: VasTerminalData

    private val walletUsername by lazy {
        SharedPreferenceUtils.getPayviceUsername(this@Dstv)
    }

    private val walletId by lazy {
        SharedPreferenceUtils.getPayviceWalletId(this@Dstv)
    }

    private val walletPassword by lazy {
        SharedPreferenceUtils.getPayvicePassword(this@Dstv)
    }


    private val mProgressDialog by lazy {
        indeterminateProgressDialog("Processing")
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId){
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return false
    }

    private lateinit var payDetails : PayDetails

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ds_tv_vas)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        beneficiaryName = ""


        dstv_continue_btn.setOnClickListener {
            if (dstv_smart_card_no.text.toString().isEmpty() || dstv_smart_card_no.text.toString().length < 10 ){
                dstv_smart_card_no.error = "Enter valid number"
                smartCardNo = dstv_smart_card_no.text.toString()
            } else {
                lookupDstv()
            }
        }

        dstv_beneficiaries.setOnClickListener {
            mProgressDialog.show()
            async {
                val db = (application as App).beneficiariesDatabase
                val beneficiariesDao = db.getDstvBeneficiariesDao()
                val beneficiaries = beneficiariesDao.getAllBeneficiaries()
                Log.d("OkH", beneficiaries.toString())
                mProgressDialog.dismiss()
                if (beneficiaries.isEmpty()) {
                    launch(UI){
                        toast("You have no beneficiaries")
                    }

                    return@async
                }
                launch(UI){
                    showBeneficiaries(beneficiaries)
                }
                }
            }
        }

    private fun lookupDstv(iuc : String = "empty"){
        mProgressDialog.show()
        launch(CommonPool) {
            try {
                if (iuc == "empty"){
                    val iuc = dstv_smart_card_no.text.toString()
                    processTransaction(iuc)
                } else {
                    processTransaction(iuc)
                }

            }
            catch (exception : ConnectException){
                launch(UI){
                    mProgressDialog.dismiss()
                    alert {
                        title = " Response"
                        message = "Connection is faulty. Please check your internet connection"

                    }.show()

                }
            }
            catch (exception : SocketTimeoutException){
                launch(UI){
                    mProgressDialog.dismiss()
                    alert {
                        title = " Response"
                        message = "Connection is taking too long. Please try again later"
                    }.show()
                }
            }
            catch (e : retrofit2.HttpException){
                launch(UI){
                    mProgressDialog.dismiss()
                    alert {
                        title = "Error"
                        message = "Error from server. Please try again"
                        okButton {  }
                    }.show()
                }
            }
        }
    }

    fun setConfig() {

        Single.fromCallable<VasTerminalData> {
            (application as App).db.vasTerminalDataDao.get()
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    vasTerminalData = it
                    configData = ConfigData()
                    configData.storeConfigData("06003", vasTerminalData.countryCode)
                    //MCC
                    configData.storeConfigData("08004", vasTerminalData.mcc)
                    //Merchant's name - 40 length
                    configData.storeConfigData("52040", vasTerminalData.merchantName)
                    //Merchant Id - 15 length
                    configData.storeConfigData("03015", vasTerminalData.mid)
                    //Currency Code
                    configData.storeConfigData("05003", vasTerminalData.currencyCode)

                }, {
                    //Log.d("PaxDevice",it.message)
                });
    }

    private suspend fun processTransaction(iuc: String) {
            val lookup = DstvLookup(iuc = iuc)
            val response = DstvService.Factory.create().dstvLookup(lookup).await()

        try {
            beneficiaryName = response.fullname!!
            Log.e("beneficiaryName", beneficiaryName+"")
        }
        catch (e: Exception) {
            // handler
        }


        if(!beneficiaryName.equals("")){
            launch(UI){
                mProgressDialog.dismiss()
                alert {
                    title = "DSTV Purchase"
                    message = "Card number : $iuc \nName : ${response.fullname} \nUnit : ${response.unit}"
                    positiveButton(buttonText = "Confirm", onClicked = {_ ->
                        bottomSheetDialog(response.data, iuc)})
                }.show()
            }
        }
        else{
            launch(UI) {
                mProgressDialog.dismiss()
                alert {
                    title = "Wrong IUC"
                    message = "Please check IUC Number"
                    positiveButton(buttonText = "Ok", onClicked = { _ ->

                    })
                }.show()
            }
        }


        }

    private fun bottomSheetDialog(data: List<Data>, iuc: String) {
        val networkDialog = BottomSheetDialog(this)

        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_list_layout, null, false)
        networkDialog.setContentView(view)
        val textTitle = view.findViewById<TextView>(R.id.titleText)
        textTitle.text = "Select DSTV Service"

        val itemList = ArrayList<SingleImageTitleObject>()

        for (index in 0 until data.size){
            if (index == 0)
            itemList.add(SingleImageTitleObject("Active plan : ${data[index].amount} ${data[index].name}", R.drawable.dstv_logo, data[index].amount))
            else
                itemList.add(SingleImageTitleObject("${data[index].amount} ${data[index].name}", R.drawable.dstv_logo, data[index].amount))
        }

        val listview = view.findViewById<ListView>(R.id.list)


        listview.adapter = SingleImageTitleObject.SingleImageTitleAdapter(itemList, this,
                R.layout.bottom_sheet_list_item)
        listview.setOnItemClickListener {
            _, _, position, _ ->
            amount = itemList[position].amount
            mProgressDialog.show()
            async {
                makeDstvPayments(iuc = iuc, positionClicked = position, data = data)
            }
            networkDialog.dismiss()
        }

        networkDialog.show()

    }

    private fun showBeneficiaries( data : List<DstvBeneficiariesModel>){
            val networkDialog = BottomSheetDialog(this)

            val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_list_layout, null, false)
            networkDialog.setContentView(view)
            val textTitle = view.findViewById<TextView>(R.id.titleText)
            textTitle.text = "Select Beneficiary"

            val itemList = ArrayList<SingleImageTitleObject>()

            for (index in 0 until data.size){
                itemList.add(SingleImageTitleObject("${data[index].name} ${data[index].dstvNumber}", R.drawable.dstv_logo))
            }

            val listview = view.findViewById<ListView>(R.id.list)



            listview.adapter = SingleImageTitleObject.SingleImageTitleAdapter(itemList, this,
                    R.layout.bottom_sheet_list_item)

            listview.setOnItemClickListener {
                _, _, position, _ ->
                mProgressDialog.show()
                async {
                    val iuc = data[position].dstvNumber
                    lookupDstv(iuc)
                }
                networkDialog.dismiss()
            }

            networkDialog.show()

    }

    private fun makeDstvPayments(iuc : String, positionClicked : Int, data: List<Data> ) {
        val bouquet = data[positionClicked]

        mProgressDialog.dismiss()
        launch(UI){
            alert {
                title = "Transaction Type"
                message = "Select the type of transaction you want to make"
                positiveButton(buttonText = "Card") { _ -> payWithCard(bouquet, iuc)}
                negativeButton(buttonText = "Wallet") {_ -> payWithWallet(bouquet, iuc)}
            }.show()
        }
    }

    private fun payWithCard(bouquet : Data, iuc: String){
        val view = View.inflate(this, R.layout.activity_enter_pin, null)

        val encryptedPassword = SecureStorage.retrieve(Helper.STORED_PASSWORD, "")
        PinAlertUtils.getPin(this, view){
            val encryptedPin = SecureStorageUtils.hashIt(it!!, encryptedPassword)
            configData = ConfigData()
            val pfm = Pfm(PfmStateGenerator(this@Dstv).generateState(), PfmJournalGenerator(TransactionResult(), configData, false, amount.toLong(), null).generateJournal())
            payDetails = PayDetails(iuc = iuc, product_code = bouquet.product_code, user_id = walletUsername,
                    terminal_id = walletId, pin = encryptedPin!!, unit = "DSTV", pfm = pfm)

            val amountBrokenDown = bouquet.amount.split(".")

//            val intent = Intent(this, TransactionProcessActivity::class.java)
//            intent.putExtra(BasePaymentActivity.TRANSACTION_ACCOUNT_TYPE, AccountType.DEFAULT_UNSPECIFIED)
//            intent.putExtra(BasePaymentActivity.TRANSACTION_TYPE, Host.TransactionType.BILL_PAYMENT)
//            //times 100 because of the conversion to kobo
//            intent.putExtra(BasePaymentActivity.TRANSACTION_AMOUNT,  (amountBrokenDown[0].toLong()* 100))
//            intent.putExtra(BasePaymentActivity.TRANSACTION_ADDITIONAL_AMOUNT, 0L)
//            startActivityForResult(intent, KEYS.PURCHASE_INTENT_CODE)
            val intent = Intent(this, VasPurchaseProcessor::class.java)
            intent.putExtra(BasePaymentActivity.TRANSACTION_ACCOUNT_TYPE, AccountType.DEFAULT_UNSPECIFIED)
            intent.putExtra(BasePaymentActivity.TRANSACTION_TYPE, Host.TransactionType.BILL_PAYMENT)

            //amount * 100 to convert the amount to long
            intent.putExtra(BasePaymentActivity.TRANSACTION_AMOUNT, (amountBrokenDown[0].toLong()* 100))
            intent.putExtra(BasePaymentActivity.TRANSACTION_ADDITIONAL_AMOUNT, 0L)

            if (SharedPreferenceUtils.getIsTerminalPrepped(this)) {
                startActivityForResult(intent, KEYS.PURCHASE_INTENT_CODE)
            } else {
                alert {
                    isCancelable = false
                    title = "Terminal not configured"
                    message = "Click O.K to go to configuration page"
                    okButton {
                        startActivity(Intent(this@Dstv, TermMagmActivity::class.java))
                        //this@EkoPostpaid.finish()
                    }
                }.show()
            }
        }
    }

    private fun payWithWallet(bouquet : Data, iuc : String){
        val password = SecureStorage.retrieve(Helper.STORED_PASSWORD, "")
        mProgressDialog.show()
        mProgressDialog.setCancelable(false)

        val view = View.inflate(this, R.layout.activity_enter_pin, null)
        PinAlertUtils.getPin(this, view) {
            val encryptedPin = SecureStorageUtils.hashIt(it!!, password)

            launch(CommonPool) {
                configData = ConfigData()
                val journal = PfmJournalGenerator(TransactionResult(), configData, false, amount.toLong(), null).generateJournal()
                val state = PfmStateGenerator(this@Dstv).generateState()
                val pfm = Pfm(state, journal)
                payDetails = PayDetails(iuc = iuc, product_code = bouquet.product_code, user_id = walletUsername, terminal_id = walletId,
                        pin = encryptedPin!!, unit = "DSTV", pfm = pfm)
                val response = DstvService.Factory.create().pay(payDetails).await()
                val jsonResponse = Gson().toJsonTree(response).asJsonObject
                val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                val response1 = gson.fromJson(jsonResponse.toString(), EnergyModel.PhPayFailedResponse::class.java)

                if (response1.error == "true") {
                    launch(UI) {
                        alert {
                            title = "Response"
                            isCancelable = false
                            message = response.message
                            positiveButton(buttonText = "Ok") {
                                moveToHome()
                            }
                        }.show()
                    }
                } else {
                    launch(UI) {
                        mProgressDialog.dismiss()
                        alert {
                            title = "DSTV Payment Purchase"
                            isCancelable = false
                            message = "Message : ${response.message} \n\nWould you want to save this name as a beneficiary?"
                            positiveButton(buttonText = "Yes") {
                                addToBeneficiaries(bouquet, iuc)
                                var receiptMap = hashMapOf<String, String>(
                                        "Terminal ID" to SharedPreferenceUtils.getTerminalId(this@Dstv),
                                        "Wallet ID" to walletId,
                                        "Ref" to response.ref,
                                        "Smart Card Number" to iuc
                                )
                                val receiptModel = ReceiptModel("", "DSTV", "", receiptMap, (amount).toString(), "APPROVED")

                                val intent = Intent(this@Dstv, PrintActivity::class.java)
                                intent.putExtra("print_map", receiptModel)
                                intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_VAS_TYPE, PrintActivity.VasType.DSTV)
                                startActivity(intent)
                                finish()
                            }

                            negativeButton(buttonText = "No") {
                                var receiptMap: HashMap<String, String>? = null
                                receiptMap = hashMapOf<String, String>(
                                        "Terminal ID" to SharedPreferenceUtils.getTerminalId(this@Dstv),
                                        "Wallet ID" to walletId,
                                        "Ref" to response.ref,
                                        "Smart Card Number" to iuc
                                )
                                val receiptModel = ReceiptModel("", "DSTV", "", receiptMap, (amount).toString(), "APPROVED")

                                val intent = Intent(this@Dstv, PrintActivity::class.java)
                                intent.putExtra("print_map", receiptModel)
                                intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_VAS_TYPE, PrintActivity.VasType.DSTV)
                                startActivity(intent)
                                finish()
                            }
                            //moveToHome()

                        }.show()

                    }
                }
            }
        }
    }

    private fun addToBeneficiaries(bouquet : Data, iuc : String) {
        val db = (application as App).beneficiariesDatabase
        val dstvBeneficiariesDao = db.getDstvBeneficiariesDao()

        val beneficiary = DstvBeneficiariesModel(dstvNumber = iuc, name = beneficiaryName)

        async {
            dstvBeneficiariesDao.insert(beneficiary)
            launch(UI){
                toast("Beneficiary successfully added")
                startActivity(Intent(this@Dstv, CableTVActivity::class.java))
                finish()
            }
        }
    }

    private fun moveToHome() {
        finish()
        val intent = Intent(this, CableTVActivity::class.java)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode){
            KEYS.PURCHASE_INTENT_CODE -> when (resultCode){
                Activity.RESULT_OK -> {
                    val deviceState = data!!.getSerializableExtra("state") as DeviceState
                    val rrn = data!!.getStringExtra("rrn")
//                    val isApproved = data?.getBooleanExtra("isApproved", false)
//                    toast(isApproved.toString())
//                    isApproved?.let {
//                        if (it){
//                            launch(CommonPool){
//                                launch(UI){
//                                    mProgressDialog.show()
//                                }
//                                val cardResponse = DstvService.Factory.create().payWithCard(payDetails).await()
//
//                                launch(UI){
//                                    mProgressDialog.dismiss()
//                                    val result = TransactionResult()
//                                    alert {
//                                        title = "DSTV Payment Purchase"
//                                        message = "Message : ${cardResponse.message} \nReference number : ${cardResponse.ref}"
//                                        okButton { moveToHome() }
//                                    }.show()
//                                }
//
//                            }
//                        } else {
//                            toast("Transaction declined")
//                        }
//                    }
                    when (deviceState) {
                        DeviceState.APPROVED -> {
                            mProgressDialog.show()
                            (application as App).db.transactionResultDao.get(rrn).observe({ lifecycle }) {
                                it?.let { transactionResult ->
                                    launch(CommonPool) {
                                        launch(UI) {
                                            mProgressDialog.show()
                                        }
                                        val cardResponse = DstvService.Factory.create().payWithCard(payDetails).await()
                                        val jsonResponse = Gson().toJsonTree(cardResponse).asJsonObject
                                        val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                                        val response1 = gson.fromJson(jsonResponse.toString(), EnergyModel.PhPayFailedResponse::class.java)

                                        if (response1.error == "true") {
                                            launch(UI) {
                                                alert {
                                                    title = "Response"
                                                    message = cardResponse.message
                                                    positiveButton(buttonText = "Ok") {
                                                        moveToHome()
                                                    }
                                                }.show()
                                            }
                                        } else {

                                            launch(UI) {
                                                mProgressDialog.dismiss()
                                                val result = TransactionResult()
                                                alert {
                                                    title = "DSTV Payment Purchase"
                                                    message = "Message : ${cardResponse.message} \nReference number : ${cardResponse.ref}"
                                                    positiveButton(buttonText = "Print") {
                                                        val receiptMap = hashMapOf<String, String>(
                                                                "Terminal ID" to SharedPreferenceUtils.getTerminalId(this@Dstv),
                                                                "Wallet ID" to walletId,
                                                                "RRN" to transactionResult.RRN,
                                                                "Card PAN" to transactionResult.PAN,
                                                                "CardHolder" to transactionResult.cardHolderName,
                                                                "Card Expiry" to transactionResult.cardExpiry,
                                                                "Auth ID" to transactionResult.authID,
                                                                "MID" to transactionResult.merchantID,
                                                                "STAN" to transactionResult.STAN,
                                                                "Ref" to cardResponse.ref,
                                                                "Smart Card Number" to smartCardNo

                                                        )

                                                        val receiptModel = ReceiptModel("", "DSTV", "", receiptMap, (amount).toString(), "APPROVED")

                                                        val intent = Intent(this@Dstv, PrintActivity::class.java)
                                                        intent.putExtra("print_map", receiptModel)
                                                        intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_VAS_TYPE, PrintActivity.VasType.DSTV)
                                                        startActivity(intent)

                                                        finish()
                                                    }
                                                }.show()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        DeviceState.DECLINED, DeviceState.FAILED -> {
//                            alert {
//                                title = "Transaction Failed"
//                                message = "Purchase transaction failed. Please try again"
//                            }.show()
                            (application as App).db.transactionResultDao.get(rrn).observe({ lifecycle }) {
                                it?.let { transactionResult ->
                                    launch(UI) {
                                        alert {
                                            title = "Response"
                                            message = "Purchase transaction failed. Please try again"
                                            positiveButton(buttonText = "Print") {
                                                val receiptMap = hashMapOf<String, String>(
                                                        "Terminal ID" to SharedPreferenceUtils.getTerminalId(this@Dstv),
                                                        "Wallet ID" to walletId,
                                                        "RRN" to transactionResult.RRN,
                                                        "Card PAN" to transactionResult.PAN,
                                                        "CardHolder" to transactionResult.cardHolderName,
                                                        "Card Expiry" to transactionResult.cardExpiry,
                                                        "Auth ID" to transactionResult.authID,
                                                        "MID" to transactionResult.merchantID,
                                                        "STAN" to transactionResult.STAN

                                                )

                                                val receiptModel = ReceiptModel("", "DSTV", "", receiptMap, (amount).toString(), "Declined")

                                                val intent = Intent(this@Dstv, PrintActivity::class.java)
                                                intent.putExtra("print_map", receiptModel)
                                                intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_VAS_TYPE, PrintActivity.VasType.DSTV)
                                                startActivity(intent)

                                                finish()
                                            }
                                        }.show()
                                    }
                                }
                            }
                        }
                        else -> {
                        }
                    }
                }
            }
        }
    }

    object KEYS {
        const val PURCHASE_INTENT_CODE = 23

    }
}

