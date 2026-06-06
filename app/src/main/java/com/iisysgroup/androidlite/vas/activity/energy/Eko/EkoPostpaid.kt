package com.iisysgroup.androidlite.vas.activity.energy.Eko

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.view.LayoutInflater
import android.view.MenuItem
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
import com.iisysgroup.androidlite.vas.services.EkoService
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
import kotlinx.android.synthetic.main.activity_eko_postpaid.*
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
import java.util.*

@Suppress("IMPLICIT_CAST_TO_ANY")
class EkoPostpaid : AppCompatActivity(){
    var isCard = false

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId){
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return false
    }

    private lateinit var meter : String
    private lateinit var amount : String
    private lateinit var phoneNumber : String
    private lateinit var accNum : String
    private lateinit var configData: ConfigData
    private lateinit var vasTerminalData: VasTerminalData

    private lateinit var pin : String

    private val wallet_username by lazy {
        SharedPreferenceUtils.getPayviceUsername(this)
    }

    private val wallet_id by lazy {
        SharedPreferenceUtils.getPayviceWalletId(this)
    }

    private val wallet_password by lazy {
        SecureStorage.retrieve(Helper.PLAIN_PASSWORD, "")
    }

    private val mProgressDialog by lazy {
        indeterminateProgressDialog("Processing")
    }

    private val mTerminalId by lazy {
        SharedPreferenceUtils.getTerminalId(this)
    }

    private lateinit var mTransactionResult : TransactionResult

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eko_postpaid)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)



        submit.setOnClickListener { showPhoneNumberInput() }
        if (!NetworkUtils.isConnectionAvailable(this@EkoPostpaid)){
            Snackbar.make(ekedc_postpaid,
                    "No Internet Connection", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun showPhoneNumberInput() {

        accNum = accountNumber.text.toString()

        if (accNum.length < 10){
            this.accountNumber.error = "Enter valid Account number"
            return
        }

        MaterialDialog.Builder(this@EkoPostpaid).title("Phone number input").content("Please enter your phone number").inputType(InputType.TYPE_CLASS_PHONE).input("Phone number", "") { _, input -> handleEkoPayments(input.toString(), this.accNum) }.show()

    }

    private fun handleEkoPayments(phoneNumber : String, accNumber: String) {
        if (phoneNumber.length != 11){
            toast("Enter valid phone number")
            return
        }
        if (!NetworkUtils.isConnectionAvailable(this@EkoPostpaid)){
            Snackbar.make(ekedc_postpaid,
                    "No Internet Connection", Snackbar.LENGTH_LONG).show()
        }else {
            mProgressDialog.show()

            async {
                val ekoDetails = EkoModel.EkoLookupDetails(meter = accNum)
                val response = EkoService.create().ekoLookup(ekoDetails).await()

                val jsonResponse = Gson().toJsonTree(response)
                val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()

                launch(UI) {
                    mProgressDialog.hide()
                }

                if (jsonResponse.toString().contains("\"error\":true")) {
                    launch(UI) {
                        toast("Error")
                        val response = gson.fromJson(jsonResponse, EkoModel.EkoLookUpFailedResponse::class.java)
                        alert {
                            title = "Validation error"
                            message = response.message
                        }.show()
                    }
                } else {
                    val response = gson.fromJson(jsonResponse, EkoModel.EkoLookUpSuccessResponse::class.java)
                    launch(UI) {
                        alert {
                            title = "Confirm"
                            message = "Name : ${response.name}\nAddress : ${response.address}"
                            okButton { enterAmount(accNumber, phoneNumber) }
                        }.show()
                    }
                }
            }
        }
    }

    private fun enterAmount(meter : String, phoneNumber: String) {
        MaterialDialog.Builder(this@EkoPostpaid).title("Enter amount").content("Amount").inputType(InputType.TYPE_CLASS_NUMBER).input("Amount", "") { _, input -> selectTransactionType(meter, phoneNumber, input.toString()) }.show()
    }

    private fun selectTransactionType(meter: String, phoneNumber: String, amount : String) {
        this.meter = meter
        this.phoneNumber = phoneNumber
        this.amount = amount
        if (amount.toFloat() < 100) {
            alert {
                title = "Low Amount"
                message = "Minimum Of #100"
                positiveButton(buttonText = "Ok") {}
            }.show()
        } else {

            val view = LayoutInflater.from(this@EkoPostpaid).inflate(R.layout.activity_enter_pin, null, false)
            alert {
                title = "Transaction Type"
                message = "Select the type of transaction you want to make"
                positiveButton(buttonText = "Card") { _ ->
                    isCard = true

                    PinAlertUtils.getPin(this@EkoPostpaid, view) {
                        val encryptedPassword = SecureStorage.retrieve(Helper.STORED_PASSWORD, "")
                        val pin = SecureStorageUtils.hashIt(it, encryptedPassword)

                        payWithCard(meter, phoneNumber, amount, pin!!)
                    }
                }
                negativeButton(buttonText = "Wallet") { _ ->
                    isCard = false
                    PinAlertUtils.getPin(this@EkoPostpaid, view) {
                        val encryptedPassword = SecureStorage.retrieve(Helper.STORED_PASSWORD, "")
                        val pin = SecureStorageUtils.hashIt(it, encryptedPassword)
                        payWithWallet(meter, phoneNumber, amount, false, pin!!)
                    }
                }
            }.show()
        }
    }

    private fun payWithCard(meter : String, phoneNumber: String, amount: String, pin : String){
        this.meter = meter
        this.phoneNumber = phoneNumber
        this.amount = amount

        this.pin = pin
        val intent = Intent(this, VasPurchaseProcessor::class.java)
        intent.putExtra(BasePaymentActivity.TRANSACTION_ACCOUNT_TYPE, AccountType.DEFAULT_UNSPECIFIED)
        intent.putExtra(BasePaymentActivity.TRANSACTION_TYPE, Host.TransactionType.BILL_PAYMENT)

        //amount * 100 to convert the amount to long
        intent.putExtra(BasePaymentActivity.TRANSACTION_AMOUNT,  (amount.toLong() * 100))
        intent.putExtra(BasePaymentActivity.TRANSACTION_ADDITIONAL_AMOUNT, 0L)

        if (SharedPreferenceUtils.getIsTerminalPrepped(this)){
            startActivityForResult(intent, KEYS.EKO_PREPAID_INTENT_CODE)
        } else {
            alert {
                isCancelable = false
                title = "Terminal not configured"
                message = "Click O.K to go to configuration page"
                okButton {
                    startActivity(Intent(this@EkoPostpaid, TermMagmActivity::class.java))
                    //this@EkoPostpaid.finish()
                }
            }.show()
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

    private fun payWithWallet(accNum: String, phoneNumber: String, amount: String, isCard : Boolean, pin : String){

        mProgressDialog.show()
        mProgressDialog.setCancelable(false)
        async {
            configData = ConfigData()
            val journal = PfmJournalGenerator(TransactionResult(), configData, false, amount.toLong(), null).generateJournal()
            val state = PfmStateGenerator(this@EkoPostpaid).generateState()
            val pfm = Pfm(state, journal)
            val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
            val payDetails = if (isCard){
                EkoModel.EkoPayDetails(meter = accNum, phone = phoneNumber, amount = amount, terminal_id = wallet_id,
                        type = "card", password = wallet_password, user_id = wallet_username,
                        pin = pin, channel = "ANDROIDPOS", pfm = pfm)
            } else {
                EkoModel.EkoPayDetails(meter = accNum, phone = phoneNumber, amount = amount, terminal_id = wallet_id,
                        type = "cash", password = wallet_password, user_id = wallet_username,
                        pin = pin , channel = "ANDROIDPOS", pfm = pfm)
            }

            try {
                val request = EkoService.create().pay(payDetails).await()
                val jsonResponse = Gson().toJsonTree(request).asJsonObject

                launch(UI){
                    mProgressDialog.dismiss()
                }
                if (jsonResponse.toString().contains("\"error\":true")){
                    val response = gson.fromJson(jsonResponse.toString(), EkoModel.EkoPayFailedResponse::class.java)
                    launch(UI){
                        if (isCard){
                            alert {
                                title = "Response"
                                message = "Error : ${response.message}"
                                positiveButton(buttonText = "Print"){
                                    val date = Calendar.getInstance().time.toString()
                                    val receiptMap = hashMapOf<String, String>(
                                            "Account Number" to accNum,
                                            "Reference Number" to response.ref,
                                            "Wallet ID" to wallet_id.toString(),
                                            "Terminal ID" to mTerminalId,
                                            "PAN" to mTransactionResult.PAN,
                                            "Card Expiry" to mTransactionResult.cardExpiry,
                                            "Card Holder" to mTransactionResult.cardHolderName,
                                            "RRN" to mTransactionResult.RRN,
                                            "Merchant ID" to mTransactionResult.merchantID,
                                            "AID" to mTransactionResult.authID,
                                            "STAN" to mTransactionResult.STAN
                                    )

                                    val receiptModel = if (isCard) {
                                        ReceiptModel(date, "Eko Postpaid Card Payment", "Declined", receiptMap, amount, mTransactionResult.transactionStatusReason)
                                    } else {
                                        ReceiptModel(date, "Eko Postpaid Card Payment", "Declined", receiptMap, amount, "Declined")
                                    }
                                    val intent = Intent(this@EkoPostpaid, PrintActivity::class.java)
                                    intent.putExtra("print_map", receiptModel)
                                    intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_VAS_TYPE, PrintActivity.VasType.EKEDC_POSTPAID)
                                    startActivity(intent)
                                    finish()
                                }
                            }.show()
                        } else {
                            alert {
                                title = "Response"
                                message = "Error : ${response.message}"
                                okButton { moveToHome() }
                            }.show()
                        }
                    }

                }
                else {
                    val response = gson.fromJson(jsonResponse.toString(), EkoModel.EkoPaySuccessResponse::class.java)
                    launch(UI){
                        alert {
                            title = "Response"
                            message = "${response.message}"
                            isCancelable = false
                            positiveButton(buttonText = "Print"){
                                val intent = Intent(this@EkoPostpaid, PrintActivity::class.java)
                                val date = Calendar.getInstance().time.toString()
                                val receiptMap = if (isCard){
                                    hashMapOf<String, String>(
                                            "Reference Number" to response.ref,
                                            "Account Type" to response.account_type,
                                            "Payer" to response.payer,
                                            "Address" to response.address,
                                            "Business Unit" to response.customerBusinessUnit,
                                            "Terminal ID" to mTerminalId,
                                            "PAN" to mTransactionResult.PAN,
                                            "Card Expiry" to mTransactionResult.cardExpiry,
                                            "Card Holder" to mTransactionResult.cardHolderName,
                                            "RRN" to mTransactionResult.RRN,
                                            "Merchant ID" to mTransactionResult.merchantID,
                                            "AID" to mTransactionResult.authID,
                                            "STAN" to mTransactionResult.STAN
                                    )
                                } else
                                {
                                    hashMapOf<String, String>(
                                            "Reference Number" to response.ref,
                                            "Account Type" to response.account_type,
                                            "Payer" to response.payer,
                                            "Address" to response.address,
                                            "Business Unit" to response.customerBusinessUnit,
                                            "Wallet ID" to wallet_id.toString(),
                                            "Terminal ID" to mTerminalId)
                                }
                                val receiptModel = if (isCard){
                                    ReceiptModel(date, "Eko Postpaid Payment", "Approved", receiptMap, response.amount, mTransactionResult.transactionStatusReason)
                                } else {
                                    ReceiptModel(date, "Eko Postpaid Payment", "", receiptMap, response.amount, "Approved")
                                }
                                intent.putExtra("print_map", receiptModel)
                                intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_VAS_TYPE, PrintActivity.VasType.EKEDC_POSTPAID)
                                startActivity(intent)
                                finish()
                            }
                        }.show()
                    }
                }
            } catch (e : SocketTimeoutException){
                mProgressDialog.dismiss()
                alert {
                    title = "Response"
                    message = "Connection taking too long. Please try again later"
                    okButton {  }
                }.show()

            } catch (e : ConnectException){
                mProgressDialog.dismiss()
                alert {
                    title = "Response"
                    message = "Check your internet connection please. "
                    okButton {  }
                }
            }catch (e : Exception){
                alert {
                    "Error"
                    message = "Unknown error occured \n Please contact customer support"
                    okButton {  }
                }
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode){
            EkoPrepaid.KEYS.EKO_PREPAID_INTENT_CODE -> when (resultCode){
                Activity.RESULT_OK -> {
                    val state = data?.getSerializableExtra("state") as DeviceState
                    val rrn = data?.getStringExtra("rrn")
                    toast(state.toString())

                    state?.let {
                        when (it) {
                            DeviceState.APPROVED -> {
                                mProgressDialog.show()
                                launch(CommonPool) {
                                    (application as App).db.transactionResultDao.get(rrn).observe({lifecycle}){
                                        mTransactionResult = it!!
                                        payWithWallet(accNum, phoneNumber, amount, true, pin)
                                    }
                                }

                            }
                            DeviceState.DECLINED -> {
                                toast("Transaction declined")
                                moveToHome()
                            }
                            DeviceState.FAILED -> {
                                toast("Transaction failed")
                                moveToHome()
                            }
                            else -> {

                            }
                        }
                    }
                }
            }
        }
    }

    private fun moveToHome() {
        finish()
        val intent = Intent(this, EkoElectric::class.java)
        startActivity(intent)
    }

    object KEYS {
        const val EKO_PREPAID_INTENT_CODE = 34324
    }
}
