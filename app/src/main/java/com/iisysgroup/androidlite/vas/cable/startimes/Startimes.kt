package com.iisysgroup.androidlite.vas.cable.startimes

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import com.afollestad.materialdialogs.MaterialDialog
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.iisysgroup.androidlite.App
import com.iisysgroup.androidlite.PrintActivity
import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.TermMagmActivity
import com.iisysgroup.androidlite.cardpaymentprocessors.VasPurchaseProcessor
import com.iisysgroup.androidlite.login.Helper
import com.iisysgroup.androidlite.login.securestorage.SecureStorage
import com.iisysgroup.androidlite.models.ReceiptModel
import com.iisysgroup.androidlite.payments_menu.BasePaymentActivity
import com.iisysgroup.androidlite.utils.PinAlertUtils
import com.iisysgroup.androidlite.utils.SharedPreferenceUtils
import com.iisysgroup.androidlite.utils.StringUtils
import com.iisysgroup.androidlite.vas.activity.energy.model.EnergyModel
import com.iisysgroup.androidlite.vas.services.DstvService
import com.iisysgroup.payvice.securestorage.SecureStorageUtils
import com.iisysgroup.poslib.deviceinterface.DeviceState
import com.iisysgroup.poslib.host.Host
import com.iisysgroup.poslib.utils.AccountType
import kotlinx.android.synthetic.main.activity_ds_tv_vas.*
import kotlinx.android.synthetic.main.activity_ds_tv_vas.toolbar
import kotlinx.android.synthetic.main.activity_start_times.*
import kotlinx.android.synthetic.main.content_prepaid.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.okButton
import org.jetbrains.anko.toast
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException

class Startimes : AppCompatActivity(), PinAlertUtils.PinEnteredListener {
    override fun onPinEntered(pin: String?) {
        val ePassword = SecureStorage.retrieve(Helper.STORED_PASSWORD,"")
        val encryptedPin = SecureStorageUtils.hashIt(pin!!, ePassword)
        this.pin = pin!!
        if (isCard){
            payWithCard(smart_card_no,phoneNumber, amount, pin!!)
        } else {
            payWithWallet(smart_card_no, phoneNumber, amount, false, encryptedPin)
        }
    }

    private lateinit var beneficiaryName : String
    private lateinit var productCode : String
    private lateinit var pin : String
    private lateinit var smart_card_no : String
    private lateinit var amount : String
    private lateinit var phoneNumber: String

    private val walletUsername by lazy {
        SharedPreferenceUtils.getPayviceUsername(this@Startimes)
    }

    private val walletId by lazy {
        SharedPreferenceUtils.getPayviceWalletId(this@Startimes)
    }

    private val walletPassword by lazy {
        SharedPreferenceUtils.getPayvicePassword(this@Startimes)
    }


    private val mProgressDialog by lazy {
        indeterminateProgressDialog("Processing")
    }
    var isCard = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_times)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "StarTimes"

        star_times_continue_btn.setOnClickListener {
            enterButton()
        }
    }

    private fun enterButton(){
        if (star_times_smart_card_no.text.toString().isEmpty() || star_times_smart_card_no.text.toString().length < 10 ){
            star_times_smart_card_no.error = "Enter valid number"
            return
        }
        phoneNumber = startimes_phone_number.text.toString()
        if (phoneNumber.length!=11){
            startimes_phone_number.error = "Enter Valid Phone Number"
            return
        }
        lookupStarTimes()
    }

    private fun lookupStarTimes(smart_card_no: String = "empty") {
        mProgressDialog.show()
        launch(CommonPool) {
            try {
                if (smart_card_no == "empty"){
                    val smart_card_no = star_times_smart_card_no.text.toString()
                    processTransaction(smart_card_no)
                } else {
                    processTransaction(smart_card_no)
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
            catch (e : HttpException){
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

    private suspend fun processTransaction(smart_card_no: String) {
        val lookup = StartimesModel.StartimesLookupDetails(smartCardCode = smart_card_no,wallet = walletId
                ,username = walletUsername,type = "default",channel = "ANDROIDPOS")
        val request = DstvService.Factory.create().starTimesLookup(lookup).await()
        val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
        val jsonResponse = Gson().toJsonTree(request).asJsonObject
        val response = gson.fromJson(jsonResponse.toString(), StartimesModel.StartimesResponse::class.java)
        try {
            beneficiaryName = response.name
            productCode = response.productCode
            Log.e("beneficiaryName", beneficiaryName+"")
        }
        catch (e: Exception) {
            // handler
        }


        if(!response.error.equals(true)){
            launch(UI){
                mProgressDialog.dismiss()
                alert {
                    title = "STARTIMES Purchase"
                    message = "Card number : ${response.smartCardCode} \nName : ${response.name}"
                    positiveButton(buttonText = "Confirm", onClicked = {_ ->
                        enterAmount(smart_card_no, phoneNumber)
                       })
                }.show()
            }
        }
        else{
            launch(UI) {
                mProgressDialog.dismiss()
                alert {
                    title = "Wrong Smart Card Number"
                    message = "Please check your Number"
                    positiveButton(buttonText = "Ok", onClicked = { _ ->
                    })
                }.show()
            }
        }

    }
    private fun enterAmount(smart_card_no : String, phoneNumber : String) {
        MaterialDialog.Builder(this@Startimes).title("Enter amount").content("Amount").
                inputType(InputType.TYPE_CLASS_NUMBER).input("Amount", "")
        { _, input -> selectTransactionType(smart_card_no, input.toString().toInt()*100) }.show()
    }
    private fun selectTransactionType( smart_card_no: String, amount : Int) {
        this.smart_card_no = smart_card_no
        this.amount = amount.toString()
        mProgressDialog.dismiss()
        if (amount.toFloat() < 1000) {
            alert {
                title = "Low Amount"
                message = "Minimum Of #10"
                positiveButton(buttonText = "Ok") {}
            }.show()
        } else {
            val view = LayoutInflater.from(this@Startimes).inflate(R.layout.activity_enter_pin, null, false)

            launch(UI) {
                alert {
                    title = "Transaction Type"
                    message = "Select the type of transaction you want to make"
                    positiveButton(buttonText = "Card") { _ ->
                        isCard = true
                        PinAlertUtils.getPin(this@Startimes, view, this@Startimes)
                    }
                    negativeButton(buttonText = "Wallet") { _ ->
                        isCard = false
                        PinAlertUtils.getPin(this@Startimes, view, this@Startimes)
                    }
                }.show()
            }
        }
    }

    private fun payWithCard(smart_card_no: String, phoneNumber: String, amount: String, pin: String) {
        this.smart_card_no = smart_card_no
        this.amount = amount

        this.pin = pin
        val intent = Intent(this, VasPurchaseProcessor::class.java)
        intent.putExtra(BasePaymentActivity.TRANSACTION_ACCOUNT_TYPE, AccountType.DEFAULT_UNSPECIFIED)
        intent.putExtra(BasePaymentActivity.TRANSACTION_TYPE, Host.TransactionType.BILL_PAYMENT)

        //amount * 100 to convert the amount to long
        intent.putExtra(BasePaymentActivity.TRANSACTION_AMOUNT, (amount.toLong()))
        intent.putExtra(BasePaymentActivity.TRANSACTION_ADDITIONAL_AMOUNT, 0L)

        if (SharedPreferenceUtils.getIsTerminalPrepped(this)) {
            startActivityForResult(intent, KEYS.STARTIMES_INTENT_CODE)
        } else {
            alert {
                isCancelable = false
                title = "Terminal not configured"
                message = "Click O.K to go to configuration page"
                okButton {
                    startActivity(Intent(this@Startimes, TermMagmActivity::class.java))
                    //this@EkoPostpaid.finish()
                }
            }.show()
        }


    }


    private fun payWithWallet(smart_card_no : String, phoneNumber: String, amount: String, isCard : Boolean,
                              pin : String?)
    {
        mProgressDialog.show()
        var payDetails: StartimesModel.StartimesPayDetails
        val clientReference = StringUtils.getClientRef(this@Startimes, "")
        launch(CommonPool) {

            val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
            payDetails = if (isCard){
                StartimesModel.StartimesPayDetails(wallet = walletId,username = walletUsername,type = "default",channel = "ANDROIDPOS",pin = pin!!,smartCardCode = smart_card_no,
                        amount = amount,phone = phoneNumber,productCode = productCode,paymentMethod = "card",clientReference = clientReference, customerName = beneficiaryName)
            } else {
                StartimesModel.StartimesPayDetails(wallet = walletId,username = walletUsername,type = "default",channel = "ANDROIDPOS",pin = pin!!,smartCardCode = smart_card_no,
                        amount = amount,phone = phoneNumber,productCode = productCode,paymentMethod = "cash",clientReference = clientReference, customerName = beneficiaryName)
            }

            try {
                val request =DstvService.create().starTimesPay(payDetails).await()

                val jsonResponse = Gson().toJsonTree(request).asJsonObject
                val response1 = gson.fromJson(jsonResponse.toString(), EnergyModel.PhPayFailedResponse::class.java)


                launch(UI){
                    mProgressDialog.dismiss()
                }
                if (response1.error == "true"){
                    val response = gson.fromJson(jsonResponse.toString(), StartimesModel.PayFailedResponse::class.java)

                    launch(UI){
                        alert {
                            title = "Response"
                            message = "Error : ${response.message}"

                            okButton { moveToHome() }
                        }.show()
                    }

                } else {
                    val response = gson.fromJson(jsonResponse.toString(), StartimesModel.StartimesPaySuccessResponse::class.java)
                    launch(UI){
                        alert {
                            title = "Response"
                            message = "${response.message}"
                            positiveButton(buttonText = "Print"){

                                if (isCard){
//                                    val intent = Intent(this@PHElectric, EkoPrinter::class.java)
//                                    intent.putExtra("values", response)
//                                    intent.putExtra("lookupDetails",lookupResponse)
//                                    intent.putExtra("ekedc_type", EkoPrinter.EKEDC_RECEIPT_TYPE.CARD_SUCCESSFUL)
//                                    startActivity(intent)
//                                    finish()

                                } else {
                                    val receiptMap = hashMapOf<String, String>(
                                            "Terminal ID" to SharedPreferenceUtils.getTerminalId(this@Startimes),
                                            "Ref" to response.reference,
                                            "Transaction Id" to response.transactionID
                                    )
                                    val receiptModel = ReceiptModel("", "STARTIMES PURCHASE", "APPROVED", receiptMap, (amount.toFloat()/100).toString(), "")

                                    val intent = Intent(this@Startimes, PrintActivity::class.java)
                                    intent.putExtra("print_map", receiptModel)
                                    intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_VAS_TYPE, PrintActivity.VasType.STARTIMES)
                                    startActivity(intent)
                                    finish()
                                }
                            }

                        }.show()
                    }
                }
            }
            catch (exception : ConnectException){
                launch(UI){
                    mProgressDialog.dismiss()
                    alert {
                        title = "Response"
                        message = "Error in connection. Please check your internet connection"
                        okButton { }
                    }.show()
                }

            }
            catch (exception : SocketTimeoutException){
                launch(UI){
                    mProgressDialog.dismiss()
                    alert {
                        title = "Response"
                        message = "This connection is taking too long. Please try again"
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
            catch (e : IllegalStateException){
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            KEYS.STARTIMES_INTENT_CODE ->
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val deviceState = data!!.getSerializableExtra("state") as DeviceState
                        val rrn = data.getStringExtra("rrn")

                        when (deviceState) {
                            DeviceState.APPROVED -> {
                                mProgressDialog.show()
                                val clientReference = StringUtils.getClientRef(this@Startimes, "")

                                (application as App).db.transactionResultDao.get(rrn).observe({ lifecycle }) {
                                    it?.let { transactionResult ->

                                        launch(CommonPool) {
                                            val payDetails = StartimesModel.StartimesPayDetails(wallet = walletId,username = walletUsername,type = "default",channel = "ANDROIDPOS",pin = pin!!,smartCardCode = smart_card_no,
                                                    amount = amount,phone = phoneNumber,productCode = productCode,paymentMethod = "card",clientReference = clientReference, customerName = beneficiaryName)

                                            val cardResponse = DstvService.create().starTimesPay(payDetails).await()

                                            val jsonResponse = Gson().toJsonTree(cardResponse).asJsonObject
                                            val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                                            val response1 = gson.fromJson(jsonResponse.toString(), EnergyModel.PhPayFailedResponse::class.java)

                                            if (response1.error.equals("true")) {
                                                val response = gson.fromJson(jsonResponse.toString(), EnergyModel.PhPayFailedResponse::class.java)
                                                launch(UI) {
                                                    alert {
                                                        title = "Response"
                                                        message = response.message
                                                        positiveButton(buttonText = "Ok") {

                                                        }
                                                    }.show()
                                                }

                                            } else {
                                                val response = gson.fromJson(jsonResponse.toString(), EnergyModel.AbjPaySuccessResponse::class.java)
                                                launch(UI) {
                                                    alert {
                                                        title = "Response"
                                                        message = "Message : ${response.message}"
                                                        positiveButton(buttonText = "Print") {
                                                            val receiptMap = hashMapOf<String, String>(
                                                                    "Terminal ID" to SharedPreferenceUtils.getTerminalId(this@Startimes),
                                                                    "RRN" to transactionResult.RRN,
                                                                    "Card PAN" to transactionResult.PAN,
                                                                    "CardHolder" to transactionResult.cardHolderName,
                                                                    "Card Expiry" to transactionResult.cardExpiry,
                                                                    "Auth ID" to transactionResult.authID,
                                                                    "MID" to transactionResult.merchantID,
                                                                    "STAN" to transactionResult.STAN,
                                                                    "Ref" to response.reference,
                                                                    "Transaction Id" to response.transactionID
                                                            )

                                                            val receiptModel = ReceiptModel("", "STARTIMES PURCHASE", transactionResult.transactionStatus, receiptMap, (amount.toFloat()/100).toString(), transactionResult.transactionStatusReason)

                                                            val intent = Intent(this@Startimes, PrintActivity::class.java)
                                                            intent.putExtra("print_map", receiptModel)
                                                            intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_VAS_TYPE, PrintActivity.VasType.STARTIMES)
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
                                alert {
                                    title = "Transaction Failed"
                                    message = "Purchase transaction failed. Please try again"
                                }.show()
                            }
                            else -> {
                            }
                        }

                    }
                }
            else -> {
                toast("Not OK")
            }

        }
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

    private fun moveToHome() {
        finish()
        val intent = Intent(this, Startimes::class.java)
        startActivity(intent)
    }


    object KEYS {
        const val STARTIMES_INTENT_CODE = 3434
    }
}