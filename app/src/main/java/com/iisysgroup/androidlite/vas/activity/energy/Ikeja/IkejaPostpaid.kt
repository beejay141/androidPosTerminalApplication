package com.iisysgroup.androidlite.vas.activity.energy.Ikeja

import android.app.Activity
import android.content.Intent
import android.os.Bundle
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
import com.iisysgroup.androidlite.vas.services.IkejaService
import com.iisysgroup.payvice.securestorage.SecureStorageUtils
import com.iisysgroup.poslib.deviceinterface.DeviceState
import com.iisysgroup.poslib.host.entities.ConfigData
import com.iisysgroup.poslib.host.entities.TransactionResult
import com.iisysgroup.poslib.utils.AccountType
import kotlinx.android.synthetic.main.activity_ikeja_postpaid.*
import kotlinx.android.synthetic.main.activity_ikeja_postpaid.toolbar
import kotlinx.android.synthetic.main.activity_ikeja_prepaid.*
import kotlinx.android.synthetic.main.activity_print.*
import kotlinx.android.synthetic.main.content_postpaid.*
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

class IkejaPostpaid : AppCompatActivity() {

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return false
    }

    private val mProgressDialog by lazy {
        indeterminateProgressDialog("Processing")
    }

    private val wallet_username by lazy {
        SharedPreferenceUtils.getPayviceUsername(this)
    }

    private val wallet_id by lazy {
        SharedPreferenceUtils.getPayviceWalletId(this)
    }

    private val wallet_password by lazy {
        SecureStorage.retrieve(Helper.PLAIN_PASSWORD, "")
    }

    private lateinit var mEncryptedPin : String

    lateinit var meter: String
    lateinit var amount: String
    lateinit var phoneNumber: String
    lateinit var configData : ConfigData

    lateinit var mLookupResponse: IkejaModel.IkejaLookUpSuccessResponse

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ikeja_postpaid)

        setSupportActionBar(toolbar)

        submit.setOnClickListener { showPhoneNumberInput() }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (!NetworkUtils.isConnectionAvailable(this@IkejaPostpaid)){
            Snackbar.make(ikedcpost_linear,
                    "No Internet Connection", Snackbar.LENGTH_LONG).show()
        }

    }

    private fun showPhoneNumberInput() {

        val customerNumber = accountNumber.text.toString()

        if (customerNumber.length != 10) {
            this.accountNumber.error = "Enter valid account number"
            return
        }

        MaterialDialog.Builder(this@IkejaPostpaid).title("Phone number input").content("Please enter your phone number").inputType(InputType.TYPE_CLASS_PHONE).input("Phone number", "") { _, input -> handleIkejaPayments(input.toString(), customerNumber) }.show()

    }

    private fun handleIkejaPayments(phoneNumber: String, customerNumber: String) {
        if (!NetworkUtils.isConnectionAvailable(this@IkejaPostpaid)) {
            Snackbar.make(ikedc_linear,
                    "No Internet Connection", Snackbar.LENGTH_LONG).show()
        } else {
            mProgressDialog.show()
            mProgressDialog.setCancelable(false)

            async {
                val details = IkejaModel.IkejaLookupDetails(meter = "", account = customerNumber, service_type = "pay", password = wallet_password, terminal_id = wallet_id, user_id = wallet_username)

                try {
                    val response = IkejaService.create().ikejaLookup(details).await()
                    val jsonResponse = Gson().toJsonTree(response)

                    val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()

                    launch(UI) {
                        mProgressDialog.hide()
                    }

                    if (jsonResponse.toString().contains("\"error\":true")) {
                        launch(UI) {
                            toast("Error")
                            val response = gson.fromJson(jsonResponse, IkejaModel.IkejaLookUpFailedResponse::class.java)
                            alert {
                                title = "Validation error"
                                message = response.message
                            }.show()
                        }
                    } else {
                        mLookupResponse = gson.fromJson(jsonResponse, IkejaModel.IkejaLookUpSuccessResponse::class.java)
                        launch(UI) {
                            alert {
                                title = "Confirm"
                                message = "Name : ${mLookupResponse.name}\nAddress : ${mLookupResponse.address}\nAgent : ${mLookupResponse.agent}"
                                okButton { enterAmount(customerNumber, phoneNumber) }
                            }.show()
                        }
                    }
                } catch (error: SocketTimeoutException) {
                    mProgressDialog.dismiss()
                    launch(UI) {
                        toast("Connection taking too long to be established. Please try again")
                    }

                } catch (error: ConnectException) {
                    Log.d("OkHttp", "Error")
                    mProgressDialog.dismiss()
                    launch(UI) {
                        toast("Connection failed. Check your internet connection")
                    }
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
    }

    private fun enterAmount(customerNumber: String, phoneNumber: String) {
        MaterialDialog.Builder(this@IkejaPostpaid).title("Enter amount").content("Amount").inputType(InputType.TYPE_CLASS_NUMBER).input("Amount", "") { _, input -> selectTransactionType(customerNumber, phoneNumber, input.toString()) }.show()
    }

    private fun selectTransactionType(customerNumber: String, phoneNumber: String, amount: String) {
        alert {
            title = "Transaction Type"
            message = "Select the type of transaction you want to make"
            positiveButton(buttonText = "Card") { _ -> payWithCard(customerNumber, phoneNumber, amount) }
            negativeButton(buttonText = "Wallet") { _ -> payWithWallet(customerNumber, phoneNumber, amount) }
        }.show()
    }

    private fun payWithCard(customerNumber: String, phoneNumber: String, amount: String) {
        val encryptedPassword = SecureStorage.retrieve(Helper.STORED_PASSWORD, "")
        val pinEntryView = View.inflate(this, R.layout.activity_enter_pin, null)

        PinAlertUtils.getPin(this, pinEntryView){
            mEncryptedPin = SecureStorageUtils.hashIt(it!!, encryptedPassword)!!

            this.meter = customerNumber
            this.phoneNumber = phoneNumber
            this.amount = amount
            val intent = Intent(this, VasPurchaseProcessor::class.java)
            intent.putExtra(BasePaymentActivity.TRANSACTION_ACCOUNT_TYPE, AccountType.DEFAULT_UNSPECIFIED)

            //amount * 100 to convert the amount to long
            intent.putExtra(BasePaymentActivity.TRANSACTION_AMOUNT, (amount.toLong() * 100))
            intent.putExtra(BasePaymentActivity.TRANSACTION_ADDITIONAL_AMOUNT, 0L)

            if (SharedPreferenceUtils.getIsTerminalPrepped(this)) {
                startActivityForResult(intent, IkejaPostpaid.KEYS.IKEJA_POSTPAID_INTENT_CODE)
            } else {
                alert {
                    isCancelable = false
                    title = "Terminal not configured"
                    message = "Click O.K to go to configuration page"
                    okButton {
                        startActivity(Intent(this@IkejaPostpaid, TermMagmActivity::class.java))
                    }
                }.show()
            }

        }
    }

    private fun payWithWallet(customerNumber: String, phoneNumber: String, amount: String) {

        val encryptedPassword = SecureStorage.retrieve(Helper.STORED_PASSWORD, "")
        val pinEntryView = View.inflate(this, R.layout.activity_enter_pin, null)

        PinAlertUtils.getPin(this, pinEntryView) {
            val encryptedPin = SecureStorageUtils.hashIt(it!!, encryptedPassword)

            mProgressDialog.show()
            async {
                configData = ConfigData()
                val journal = PfmJournalGenerator(TransactionResult(), configData, false, amount.toLong(), null).generateJournal()
                val state = PfmStateGenerator(this@IkejaPostpaid).generateState()
                val pfm = Pfm(state, journal)
                val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                val payDetails = IkejaModel.IkejaPayDetails(meter = "", account = customerNumber, amount = amount,
                        phone = phoneNumber, service_type = "pay", password = wallet_password, user_id = wallet_username,
                        terminal_id = wallet_id, pin = encryptedPin!!, pfm = pfm)
                try {
                    val request = IkejaService.create().pay(payDetails).await()
                    val jsonResponse = Gson().toJsonTree(request).asJsonObject


                    launch(UI) {
                        mProgressDialog.dismiss()
                    }
                    if (jsonResponse.toString().contains("\"error\":true")) {
                        val response = gson.fromJson(jsonResponse.toString(), IkejaModel.IkejaPayFailedResponse::class.java)
                        launch(UI) {
                            alert {
                                title = "Response"
                                message = response.message
                                okButton { moveToHome() }
                            }.show()
                        }
                    } else {
                        val response = gson.fromJson(jsonResponse.toString(), IkejaModel.IkejaPaySuccessResponse::class.java)
                        launch(UI) {
                            alert {
                                title = "Response"
                                message = "message: ${response.message}"
                                isCancelable = false
                                positiveButton(buttonText = "Print") {
                                    val receiptMap = hashMapOf<String, String>(
                                            "Terminal ID" to SharedPreferenceUtils.getTerminalId(this@IkejaPostpaid),
                                            "Wallet ID" to wallet_id,
                                            "Payer" to response.payer,
                                            "Account Type" to response.type,
                                            //"Client ID" to response.client_id,
                                            "Transaction ID" to response.tran_id,
                                             "Reference" to response.ref,
                                            "Payment Method" to "Cash",
//                                            "SGC" to response.sgc,
//                                            "MSNO" to response.msno,
//                                            "KRN" to response.krn,
//                                            "TI" to response.ti,
                                            //"Agent" to response.agent,
//                                            "VAT" to response.vat,
                                            "Address" to response.address
                                    )

                                    val receiptModel = ReceiptModel(response.date, "IKEJA POSTPAID PURCHASE", "APPROVED", receiptMap, response.amount, "Approved")

                                    val intent = Intent(this@IkejaPostpaid, PrintActivity::class.java)
                                    intent.putExtra("print_map", receiptModel)
                                    intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_VAS_TYPE, PrintActivity.VasType.IKEDC_PREPAID)
                                    startActivity(intent)
                                    finish()
                                }

                                negativeButton(buttonText = "Cancel"){
                                    moveToHome()
                                }
                            }.show()
                        }
                    }
                } catch (error: SocketTimeoutException) {
                    mProgressDialog.dismiss()
                    launch(UI) {
//                        toast("Connection error. Please try again")
                        alert {
                            title = "Error"
                            message = "Connection error. Please try again"
                            okButton {
                                moveToHome()
                            }
                        }.show()
                    }

                } catch (error: ConnectException) {
                    mProgressDialog.dismiss()
                    launch(UI) {
                        toast("Error in connection. Check your internet connection")
                    }
                } catch (e: retrofit2.HttpException) {
                    launch(UI) {
                        mProgressDialog.dismiss()
                        alert {
                            title = "Error"
                            message = "Error from server. Please try again"
                            okButton {
                                moveToHome()
                            }
                        }.show()
                    }
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
        when (requestCode) {
            IkejaPostpaid.KEYS.IKEJA_POSTPAID_INTENT_CODE ->
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val deviceState = data?.getSerializableExtra("state") as DeviceState
                        val rrn = data.getStringExtra("rrn")

                        when (deviceState) {
                            DeviceState.APPROVED -> {
                                mProgressDialog.show()
                                (application as App).db.transactionResultDao.get(rrn).observe({ lifecycle }) {
                                    it?.let { transactionResult ->



                                            launch(CommonPool) {
                                                configData = ConfigData()
                                                val pfm = Pfm(PfmStateGenerator(this@IkejaPostpaid).generateState(), PfmJournalGenerator(transactionResult, configData, false, amount.toLong(), null).generateJournal())

                                                val payDetails = IkejaModel.IkejaPayDetails(account = meter, meter = "", service_type = "pay",
                                                        amount = amount, phone = phoneNumber, user_id = wallet_username, terminal_id = wallet_id,
                                                        password = wallet_password, pin = mEncryptedPin!!, pfm = pfm)

                                                val cardResponse = IkejaService.Factory.create().pay(payDetails).await()

                                                val jsonResponse = Gson().toJsonTree(cardResponse).asJsonObject
                                                val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                                                val response1 = gson.fromJson(jsonResponse.toString(), IkejaModel.IkejaPayFailedResponse::class.java)
                                                if (response1.error == "true") {
                                                    val response = gson.fromJson(jsonResponse.toString(), IkejaModel.IkejaPayFailedResponse::class.java)
                                                    launch(UI) {
                                                        alert {
                                                            title = "Response"
                                                            message = response.message
                                                            positiveButton(buttonText = "Print") {
                                                                val receiptMap = hashMapOf<String, String>(
                                                                        "Terminal ID" to SharedPreferenceUtils.getTerminalId(this@IkejaPostpaid),
                                                                        "Wallet ID" to wallet_id,
                                                                        "RRN" to transactionResult.RRN,
                                                                        "Card PAN" to transactionResult.PAN,
                                                                        "CardHolder" to transactionResult.cardHolderName,
                                                                        "Card Expiry" to transactionResult.cardExpiry,
                                                                        "Auth ID" to transactionResult.authID,
                                                                        "MID" to transactionResult.merchantID,
                                                                        "STAN" to transactionResult.STAN,
                                                                        "Payer" to mLookupResponse.name,
                                                                        "Account Type" to "IKEJA POSTPAID",
                                                                        "Address" to mLookupResponse.address,
                                                                        "Agent" to mLookupResponse.agent,
                                                                        "Address" to mLookupResponse.address
                                                                )

                                                                val receiptModel = ReceiptModel(" ", "IKEJA POSTPAID PURCHASE", "Declined", receiptMap, (transactionResult.amount / 100).toString(), transactionStatusReason = "")

                                                                val intent = Intent(this@IkejaPostpaid, PrintActivity::class.java)
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
                                                            message = "${response.message}"
                                                            isCancelable = false
                                                            positiveButton(buttonText = "Print") {
                                                                val receiptMap = hashMapOf<String, String>(
                                                                        "Terminal ID" to SharedPreferenceUtils.getTerminalId(this@IkejaPostpaid),
                                                                        "Wallet ID" to wallet_id,
                                                                        "RRN" to transactionResult.RRN,
                                                                        "Card PAN" to transactionResult.PAN,
                                                                        "CardHolder" to transactionResult.cardHolderName,
                                                                        "Card Expiry" to transactionResult.cardExpiry,
                                                                        "Auth ID" to transactionResult.authID,
                                                                        "MID" to transactionResult.merchantID,
                                                                        "STAN" to transactionResult.STAN,
                                                                        "Payer" to response.payer,
                                                                        "Account Type" to response.type,
                                                                        "Ref" to response.ref,
                                                                        "Trans_ID" to response.tran_id,
                                                                        "Payment Method" to "Card",
//                                                                        "Client ID" to response.client_id,
//                                                                        "SGC" to response.sgc,
//                                                                        "MSNO" to response.msno,
//                                                                        "KRN" to response.krn,
//                                                                        "TI" to response.ti,
                                                                        //"Agent" to response.agent,
                                                                        //"VAT" to response.vat,
                                                                        "Address" to response.address
                                                                )

                                                                val receiptModel = ReceiptModel(response.date, "IKEJA POSTPAID PURCHASE", transactionResult.transactionStatus, receiptMap, amount, "")

                                                                val intent = Intent(this@IkejaPostpaid, PrintActivity::class.java)
                                                                intent.putExtra("print_map", receiptModel)
                                                                intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_VAS_TYPE, PrintActivity.VasType.IKEDC_PREPAID)
                                                                startActivity(intent)

                                                                finish()
                                                            }
                                                            negativeButton(buttonText = "Cancel"){
                                                                moveToHome()
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
                                    okButton { moveToHome() }
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

    object KEYS {
        const val IKEJA_POSTPAID_INTENT_CODE = 23922
    }
}
