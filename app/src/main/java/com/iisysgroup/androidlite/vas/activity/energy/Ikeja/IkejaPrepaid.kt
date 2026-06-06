package com.iisysgroup.androidlite.vas.activity.energy.Ikeja

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.iisysgroup.androidlite.App
import com.iisysgroup.androidlite.PrintActivity
import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.TermMagmActivity
import com.iisysgroup.androidlite.cardpaymentprocessors.PurchaseProcessor
import com.iisysgroup.androidlite.cardpaymentprocessors.VasPurchaseProcessor
import com.iisysgroup.androidlite.generators.PfmJournalGenerator
import com.iisysgroup.androidlite.generators.PfmStateGenerator
import com.iisysgroup.androidlite.login.Helper
import com.iisysgroup.androidlite.login.securestorage.SecureStorage
import com.iisysgroup.androidlite.models.Pfm
import com.iisysgroup.androidlite.models.ReceiptModel
import com.iisysgroup.androidlite.payments_menu.BasePaymentActivity
import com.iisysgroup.androidlite.utils.NetworkUtils
import com.iisysgroup.androidlite.utils.PinAlertUtils
import com.iisysgroup.androidlite.utils.SharedPreferenceUtils
import com.iisysgroup.androidlite.utils.StringUtils
import com.iisysgroup.androidlite.vas.services.IkejaService
import com.iisysgroup.payvice.securestorage.SecureStorageUtils
import com.iisysgroup.poslib.deviceinterface.DeviceState
import com.iisysgroup.poslib.host.entities.ConfigData
import com.iisysgroup.poslib.host.entities.TransactionResult
import com.iisysgroup.poslib.host.entities.VasTerminalData
import com.iisysgroup.poslib.utils.AccountType
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_ikeja_prepaid.*
import kotlinx.android.synthetic.main.activity_ikeja_prepaid.toolbar
import kotlinx.android.synthetic.main.content_prepaid.*
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

@Suppress("IMPLICIT_CAST_TO_ANY")
class IkejaPrepaid : AppCompatActivity() {

    private var isCard = false

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId){
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return false
    }

    private lateinit var mEncryptedPin : String

    private val wallet_username by lazy {
        SharedPreferenceUtils.getPayviceUsername(this)
    }

    private val wallet_id by lazy {
        SharedPreferenceUtils.getPayviceWalletId(this)
    }

    private val terminal_id by lazy {
        SharedPreferenceUtils.getTerminalId(this)
    }

    private val wallet_password by lazy {
        SecureStorage.retrieve(Helper.PLAIN_PASSWORD, "")
    }

    private val mProgressDialog by lazy {
        indeterminateProgressDialog("Processing")
    }

    lateinit var meterNumber : String
    lateinit var amount : String
    lateinit var phoneNumber : String
    lateinit var configData : ConfigData
    lateinit var vasTerminalData : VasTerminalData



    private lateinit var mIkejaPrepaidLookupSuccess : IkejaModel.IkejaLookUpSuccessResponse


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ikeja_prepaid)

        setSupportActionBar(toolbar)

        submit.setOnClickListener { showPhoneNumberInput() }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    private fun showPhoneNumberInput() {
        val customerNumber = customerNumber.text.toString()

        if (customerNumber.length != 11){
            this.customerNumber.error = "Enter valid customer number"
            return
        }

        MaterialDialog.Builder(this@IkejaPrepaid).title("Phone number input").content("Please enter your phone number").inputType(InputType.TYPE_CLASS_PHONE).input("Phone number", "") { _, input -> handleIkejaPayments(input.toString(), customerNumber) }.show()
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

    private fun handleIkejaPayments(phoneNumber : String, customerNumber: String) {
        mProgressDialog.show()

        try {
            async {
                val details = IkejaModel.IkejaLookupDetails(meter = customerNumber, account = "", user_id = wallet_username, password = wallet_password, terminal_id = wallet_id)
                val response = IkejaService.create().ikejaLookup(details).await()

                val jsonResponse = Gson().toJsonTree(response)

                val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()

                launch(UI){
                    mProgressDialog.hide()
                }

                if (jsonResponse.toString().contains("\"error\":true"))
                {
                    launch(UI){
                        toast("Error")
                        val response = gson.fromJson(jsonResponse, IkejaModel.IkejaLookUpFailedResponse::class.java)
                        alert {
                            title = "Validation error"
                            message = response.message
                        }.show()
                    }
                }
                else
                {
                    mIkejaPrepaidLookupSuccess = gson.fromJson(jsonResponse, IkejaModel.IkejaLookUpSuccessResponse::class.java)
                    launch(UI){
                        alert {
                            title = "Confirm"
                            message = "Name : ${mIkejaPrepaidLookupSuccess.name}\nAddress : ${mIkejaPrepaidLookupSuccess.address}\nAgent : ${mIkejaPrepaidLookupSuccess.agent}"
                            okButton { enterAmount(customerNumber, phoneNumber) }
                        }.show()
                    }
                }
            }
        } catch (error: ConnectException) {
            mProgressDialog.dismiss()
            toast("Connection error, Check your internet connection")
        } catch (error: SocketTimeoutException) {
            mProgressDialog.dismiss()
            toast("Connection taking too long. Please try again")
        } catch (e: retrofit2.HttpException) {
            launch(UI) {
                mProgressDialog.dismiss()
                alert {
                    title = "Error"
                    message = "Error from server. Please try again"
                    okButton { }
                }.show()
            }
        }

    }

    private fun enterAmount(meter : String, phoneNumber: String) {
        MaterialDialog.Builder(this@IkejaPrepaid).title("Enter amount").content("Amount").inputType(InputType.TYPE_CLASS_NUMBER).input("Amount", "") { _, input -> selectTransactionType(meter, phoneNumber, input.toString()) }.show()
    }

    private fun selectTransactionType(meter: String, phoneNumber: String, amount : String) {
        if (amount.toFloat() < 10) {
            alert {
                title = "Low Amount"
                message = "Minimum Of #100"
                positiveButton(buttonText = "Ok") {}
            }.show()
        } else {
            alert {
                title = "Transaction Type"
                message = "Select the type of transaction you want to make"
                positiveButton(buttonText = "Card") { _ -> payWithCard(meter, phoneNumber, amount) }
                negativeButton(buttonText = "Wallet") { _ -> payWithWallet(meter, phoneNumber, amount) }
            }.show()
        }
    }

    private fun payWithCard(meter : String, phoneNumber: String, amount: String){

        val view = View.inflate(this, R.layout.activity_enter_pin, null)
        val encryptedPassword = SecureStorage.retrieve(Helper.STORED_PASSWORD, "")


        PinAlertUtils.getPin(this, view){
            mEncryptedPin = SecureStorageUtils.hashIt(it!!, encryptedPassword)!!


            isCard = true

            meterNumber = meter
            this.phoneNumber = phoneNumber
            this.amount = amount
            val intent = Intent(this, VasPurchaseProcessor::class.java)
            intent.putExtra(BasePaymentActivity.TRANSACTION_ACCOUNT_TYPE, AccountType.DEFAULT_UNSPECIFIED)

            //amount * 100 to convert the amount to long
            intent.putExtra(BasePaymentActivity.TRANSACTION_AMOUNT,  (amount.toLong() * 100))
            intent.putExtra(BasePaymentActivity.TRANSACTION_ADDITIONAL_AMOUNT, 0L)

            if (SharedPreferenceUtils.getIsTerminalPrepped(this)){
                startActivityForResult(intent, IkejaPrepaid.KEYS.IKEJA_PREPAID_INTENT_CODE)
            } else {
                alert {
                    isCancelable = false
                    title = "Terminal not configured"
                    message = "Click O.K to go to configuration page"
                    okButton {
                        startActivity(Intent(this@IkejaPrepaid, TermMagmActivity::class.java))
                    }
                }.show()
            }
        }

    }

    private fun payWithWallet(customerNumber: String, phoneNumber: String, amount: String){
        isCard = false
        mProgressDialog.show()

        val view = View.inflate(this, R.layout.activity_enter_pin, null)
        val encryptedPassword = SecureStorage.retrieve(Helper.STORED_PASSWORD, "")

        PinAlertUtils.getPin(this, view){
            val encryptedPin = SecureStorageUtils.hashIt(it!!, encryptedPassword)
            val clientReference = StringUtils.getClientRef(this@IkejaPrepaid, "")


            try {
                async {
                    configData = ConfigData()
                    val journal = PfmJournalGenerator(TransactionResult(), configData, false, amount.toLong(), null).generateJournal()
                    val state = PfmStateGenerator(this@IkejaPrepaid).generateState()
                    val pfm = Pfm(state, journal)
                    val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                    val payDetails = IkejaModel.IkejaPrePayDetails(meter = customerNumber, amount = amount, phone = phoneNumber,
                            service_type = "vend", password = wallet_password, terminal_id = wallet_id, user_id = wallet_username,
                            pin = encryptedPin!!, terminal = terminal_id, clientReference = clientReference, pfm = pfm)

                    val request = IkejaService.create().prepay(payDetails).await()
                    val jsonResponse = Gson().toJsonTree(request).asJsonObject
                    val response1 = gson.fromJson(jsonResponse.toString(), IkejaModel.IkejaPayFailedResponse::class.java)

                    launch(UI){
                        mProgressDialog.dismiss()
                    }
                    if (response1.error == "true"){
                        val response = gson.fromJson(jsonResponse.toString(), IkejaModel.IkejaPayFailedResponse::class.java)


                        launch(UI) {
                            alert {
                                title = "Response"
                                message = "Error : ${response.message}"
                                okButton { moveToHome() }
                            }.show()
                        }

                    } else {
                        val response = gson.fromJson(jsonResponse.toString(), IkejaModel.IkejaPaySuccessResponse::class.java)
                        launch(UI){
                            alert {
                                title = "Response"
                                message = "Message:  ${response.message}"


                                positiveButton(buttonText = "Print"){
                                    val receiptMap = hashMapOf<String, String>(
                                            "Terminal ID" to SharedPreferenceUtils.getTerminalId(this@IkejaPrepaid),
                                            "Token" to response.token,
                                            "Wallet ID" to wallet_id,
                                            "Payer" to response.payer,
                                            "Account Type" to response.type,
                                            "Client ID" to response.client_id,
                                            "Ref" to response.ref,
                                            "Tran ID" to response.tran_id,
                                            "Payment Type" to "Cash",
//                                            "SGC" to response.sgc,
//                                            "MSNO" to response.msno,
//                                            "KRN" to response.krn,
//                                            "TI" to response.ti,
                                            "Agent" to response.agent,
//                                            "VAT" to response.vat,
                                            "Address" to response.address
                                    )

                                    val receiptModel = ReceiptModel(response.date, "IKEJA PREPAID PURCHASE", "APPROVED", receiptMap, response.amount, "")

                                    val intent = Intent(this@IkejaPrepaid, PrintActivity::class.java)
                                    intent.putExtra("print_map", receiptModel)
                                    intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_VAS_TYPE, PrintActivity.VasType.IKEDC_PREPAID)
                                    startActivity(intent)
                                    finish()
                                }
                            }.show()
                        }
                    }
                }
            } catch (error: ConnectException) {
                mProgressDialog.dismiss()
                toast("Connection error, Check your internet connection")
            } catch (error: SocketTimeoutException) {
                mProgressDialog.dismiss()
                toast("Connection taking too long. Please try again")
            } catch (e: retrofit2.HttpException) {
                launch(UI) {
                    mProgressDialog.dismiss()
                    alert {
                        title = "Error"
                        message = "Error from server. Please try again"
                        okButton { }
                    }.show()
                }
            }

        }

    }



    private fun moveToHome() {
        finish()
        val intent = Intent(this, IkejaElectric::class.java)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode){
            KEYS.IKEJA_PREPAID_INTENT_CODE -> when (resultCode){
                Activity.RESULT_OK -> {
                    val state = data?.getSerializableExtra("state") as DeviceState
                    val rrn = data.getStringExtra("rrn")


                    when (state){
                        DeviceState.APPROVED -> {
                            (application as App).db.transactionResultDao.get(rrn).observe({lifecycle}) { transactionResult ->
                                transactionResult?.let { transactionResult ->
                                    try {
                                        mProgressDialog.show()
                                        launch(CommonPool) {
                                            configData = ConfigData()
                                            val pfm = Pfm(PfmStateGenerator(this@IkejaPrepaid).generateState(), PfmJournalGenerator(transactionResult, configData, false, amount.toLong(), null).generateJournal())
                                            val payDetails = IkejaModel.IkejaPayDetails(account = "", meter = meterNumber, service_type = "vend",
                                                    amount = amount, phone = phoneNumber, terminal_id = wallet_id, user_id = wallet_username, password = wallet_password, pin = mEncryptedPin,pfm = pfm)
                                            val cardResponse = IkejaService.Factory.create().payWithCard(payDetails).await()

                                            val jsonResponse = Gson().toJsonTree(cardResponse).asJsonObject
                                            val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                                            if (cardResponse.toString().contains("error:true")) {
                                                val response = gson.fromJson(jsonResponse.toString(), IkejaModel.IkejaPayFailedResponse::class.java)
                                                launch(UI) {
                                                    alert {
                                                        title = "Response"
                                                        message = "Error : ${response.message}"
                                                        positiveButton(buttonText = "Print") {
                                                            val receiptMap = hashMapOf<String, String>(
                                                                    "Terminal ID" to SharedPreferenceUtils.getTerminalId(this@IkejaPrepaid),
                                                                    "Wallet ID" to wallet_id,
                                                                    "RRN" to transactionResult.RRN,
                                                                    "Card PAN" to transactionResult.PAN,
                                                                    "CardHolder" to transactionResult.cardHolderName,
                                                                    "Card Expiry" to transactionResult.cardExpiry,
                                                                    "Auth ID" to transactionResult.authID,
                                                                    "MID" to transactionResult.merchantID,
                                                                    "STAN" to transactionResult.STAN,
                                                                    "Payer" to mIkejaPrepaidLookupSuccess.name,
                                                                    "Account Type" to "IKEJA PREPAID",
                                                                    "Agent" to mIkejaPrepaidLookupSuccess.agent,
                                                                    "Address" to mIkejaPrepaidLookupSuccess.address
                                                            )

                                                            val receiptModel = ReceiptModel(response.date, "IKEJA PREPAID PURCHASE", transactionResult.transactionStatus, receiptMap, (transactionResult.amount / 100).toString(), transactionResult.transactionStatusReason)

                                                            val intent = Intent(this@IkejaPrepaid, PrintActivity::class.java)
                                                            intent.putExtra("print_map", receiptModel)
                                                            intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_VAS_TYPE, PrintActivity.VasType.IKEDC_PREPAID)
                                                            startActivity(intent)
                                                            finish()
                                                        }
                                                    }.show()
                                                }

                                            } else {
                                                val response = gson.fromJson(jsonResponse.toString(), IkejaModel.IkejaPaySuccessResponse::class.java)
                                                launch(UI) {
                                                    alert {
                                                        title = "Response"
                                                        message = "Message: ${response.message}"
                                                        positiveButton(buttonText = "Print") {
                                                            val receiptMap = hashMapOf<String, String>(
                                                                    "Terminal ID" to SharedPreferenceUtils.getTerminalId(this@IkejaPrepaid),
                                                                    "Wallet ID" to wallet_id,
                                                                    "RRN" to transactionResult.RRN,
                                                                    "Card PAN" to transactionResult.PAN,
                                                                    "CardHolder" to transactionResult.cardHolderName,
                                                                    "Card Expiry" to transactionResult.cardExpiry,
                                                                    "Auth ID" to transactionResult.authID,
                                                                    "MID" to transactionResult.merchantID,
                                                                    "TOoken" to response.token,
                                                                    "STAN" to transactionResult.STAN,
                                                                    "Payer" to response.payer,
                                                                    "Account Type" to response.type,
                                                                    "Trans Id" to response.tran_id,
                                                                    "Ref" to response.ref,
                                                                    "Agent" to response.agent,
                                                                    "VAT" to response.vat,
                                                                    "Payment Method" to "Card",
                                                                    "Address" to response.address
                                                            )

                                                            val receiptModel = ReceiptModel(response.date, "IKEJA PREPAID PURCHASE", transactionResult.transactionStatus, receiptMap, response.amount, transactionResult.transactionStatusReason)

                                                            val intent = Intent(this@IkejaPrepaid, PrintActivity::class.java)
                                                            intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_VAS_TYPE, PrintActivity.VasType.IKEDC_PREPAID)
                                                            intent.putExtra("print_map", receiptModel)
                                                            intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_VAS_TYPE, PrintActivity.VasType.IKEDC_PREPAID)
                                                            startActivity(intent)
                                                            finish()
                                                        }

                                                    }.show()
                                                }
                                            }
                                        }
                                    } catch (error: ConnectException) {
                                        mProgressDialog.dismiss()
                                        toast("Connection error, Check your internet connection")
                                    } catch (error: SocketTimeoutException) {
                                        launch(UI) {
                                            mProgressDialog.dismiss()
                                            alert {
                                                title = "Response"
                                                message = "This connection is taking too long. Please try again"
                                                okButton { moveToHome() }
                                            }.show()
                                        }

                                    }

                                }
                            }
                        }

                        DeviceState.DECLINED -> {
                            alert {
                                title = "Card Processing Response"
                                message = "Transaction was declined"
                                okButton {
                                    moveToHome()
                                }
                            }.show()
                        }

                        DeviceState.FAILED -> {
                            alert {
                                title = "Card Processing Response"
                                message = "Transaction was declined"
                                okButton {
                                    moveToHome()
                                }
                            }.show()
                        }
                    }
                }
            }
        }
    }

    object KEYS {
        const val IKEJA_PREPAID_INTENT_CODE = 23922
    }
}
