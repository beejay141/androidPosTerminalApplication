package com.iisysgroup.androidlite.vas.internet.smile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.iisysgroup.androidlite.App
import com.iisysgroup.androidlite.PrintActivity
import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.TermMagmActivity
import com.iisysgroup.androidlite.cardpaymentprocessors.PurchaseProcessor
import com.iisysgroup.androidlite.cardpaymentprocessors.VasPurchaseProcessor
import com.iisysgroup.androidlite.models.ReceiptModel
import com.iisysgroup.androidlite.payments_menu.BasePaymentActivity
import com.iisysgroup.androidlite.utils.PinAlertUtils
import com.iisysgroup.androidlite.utils.SharedPreferenceUtils
import com.iisysgroup.androidlite.utils.StringUtils
import com.iisysgroup.payvice.securestorage.SecureStorageUtils
import com.iisysgroup.poslib.deviceinterface.DeviceState
import com.iisysgroup.poslib.host.entities.TransactionResult
import com.iisysgroup.poslib.utils.AccountType
import com.itex.richard.payviceconnect.model.SmileModel
import com.itex.richard.payviceconnect.wrapper.PayviceServices
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_smile_topup.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.okButton
import org.jetbrains.anko.toast
import java.util.*

class SmileTopup : AppCompatActivity() {

    //CHANGE ALL HARDCODED USERNAMER, PASSWORD BACK TO VARIABLES!!!!!!!!!!!!!!

    private val mPayviceUsername by lazy {
        SharedPreferenceUtils.getPayviceUsername(this)
    }

    private val mPayvicePassword by lazy {
        SharedPreferenceUtils.getPayvicePassword(this)
    }

    private var isCard = false

    private val mPayviceWalletId by lazy {
        SharedPreferenceUtils.getPayviceWalletId(this)
    }

    private lateinit var mRrn : String


    private val mPayviceServices by lazy {
        PayviceServices.getInstance(this)
    }

    private val progressDialog by lazy {
        indeterminateProgressDialog(message = "Getting customer details").also {
            it.setCancelable(false)
        }
    }


    private lateinit var cardDetails: SmileModel.SmileTopUpRequestDetails
    private lateinit var lookupDetails: SmileModel.SmileValidateCustomerResponse
    private var accountNumber = ""
    private lateinit var mTransactionResult : TransactionResult

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_smile_topup)

        smile_continue_btn.setOnClickListener {
            var id: Int = smile_radio.checkedRadioButtonId
            if (isValidated()) {
                accountNumber = smile_account_number.text.toString()
                val amount = smile_topup_amount.text.toString()
                val amountInt = amount.toInt()
                if (amountInt >= 100) {
                    progressDialog.show()
                    val details = SmileModel.SmileValidateCustomerRequest(accountNumber, mPayviceUsername, mPayvicePassword, "", mPayviceWalletId)
                    mPayviceServices.SmileValidateCustomer(details)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .doOnError {
                                Log.d("OkH", "Error")
                            }
                            .subscribe { response: SmileModel.SmileValidateCustomerResponse ->
                                lookupDetails = response
                                progressDialog.dismiss()
                                if (response.status == 1) {
                                    alert {
                                        title = "Customer's details"
                                        message = "Name : ${response.customerName}"
                                        // Log.e(response.)
                                        positiveButton(buttonText = "Confirm") {

                                         if (id==R.id.smileWallet){
                                            payWithWallet(accountNumber, amount)
                                            }
                                         else if(id==R.id.smileCard){
                                             payWithCard(accountNumber, amount)
                                         }
                                        }
                                        negativeButton(buttonText = "Cancel") {

                                        }
                                    }.show()
                                } else {
                                    alert {
                                        title = "Customer's details"
                                        message = "${response.message}"
                                        okButton {

                                        }
                                    }.show()
                                }
                                val returned_message = if (response.status == 1) "Name : ${response.customerName}"
                                else response.message

                                var paymentOption = smileWallet.text

                                if (paymentOption.equals("Confirm"))
                                    alert {
                                        title = "Customer Search"
                                        message = returned_message

                                        positiveButton(buttonText = "Confirm") {
                                            showPaymentOptions(accountNumber, amount)
                                        }
                                        negativeButton(buttonText = "Cancel") {

                                        }
                                    }.show()

                            }
                }
                else{
                    smile_topup_amount.error = "Mininum amount is 100"
                }
            }
        }
    }

    private fun printReceipt(response: SmileModel.SmileSuccessResponse) {

        //Smile Print Model
        var transactionType = ""
        var printStatus = ""
        var pan = ""
        var cardHolderName = ""
        var cardExpiry = ""

        if (response.status == 1){
            printStatus = "APPROVED"
        }else{
            printStatus = "DECLINED"
        }
        transactionType = "SMILE TOP-UP"
        val intent = Intent(this@SmileTopup, PrintActivity::class.java)
        val amount = smile_topup_amount.text.toString()
        val referenceNumber = response.reference
        val map = HashMap<String, String>()
        val accountID = smile_account_number.text.toString()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        SharedPreferenceUtils.getTerminalId(this)
        val terminalId = sharedPreferences.getString(this.getString(R.string.key_terminal_id), "")
        val walletId = sharedPreferences.getString(this.getString(R.string.key_payvice_wallet_id), "")
        val calendar = Calendar.getInstance()
        val date = calendar.time

        map.put("Transaction Status", printStatus+"")
        map.put("Terminal ID", terminalId)
        map.put("Wallet ID", walletId)
        map.put("Reference No", referenceNumber+"")
        map.put("Transaction Type", transactionType)
        map.put("Customer Name", lookupDetails.customerName+"")
        map.put("Customer Acct", accountNumber)
        map.put("CustomerID", accountID)
        if (isCard) {
            pan =  mTransactionResult.PAN.toString()
            cardHolderName  = mTransactionResult.cardHolderName.toString()
            cardExpiry  = mTransactionResult.cardExpiry.toString()
            map.put("PAN", pan)
            map.put("Card Name", cardHolderName)
            map.put("Card Expiry", cardExpiry)
        }



        val receiptModel =
                if (isCard){
                    ReceiptModel(date.toString(), transactionType, response.message, map, amount, mTransactionResult.transactionStatusReason)
                } else {
                    ReceiptModel(date.toString(), transactionType, response.message, map, amount, response.message)
                }


        intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_MODEL_KEY, receiptModel)
        intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_VAS_TYPE, PrintActivity.VasType.SMILE_DATA_PURCHASE)


        startActivity(intent)
        finish()
    }

    private fun pay(details: SmileModel.SmileTopUpRequestDetails) {
       // SmileModel.SmileSuccessResponse
        progressDialog.setMessage("Processing Payment")
        progressDialog.show()

        mPayviceServices.SmileTopUp(details)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError {
                    progressDialog.dismiss()
                    toast(it.message.toString())
                }
                .subscribe { response ->
                    progressDialog.dismiss()
                    alert {
                        title = "Response"
                        message = response.message
                        positiveButton(buttonText = "Print"){
                            printReceipt(response)
                        }
                    }.show()
                }

    }

    fun showPaymentOptions(accountNumber: String, amount: String) {
        alert {
            title = "Transaction Type"
            message = "Select the type of transaction you want to make"
            positiveButton(buttonText = "Card") { _ -> payWithCard(accountNumber, amount) }
            negativeButton(buttonText = "Wallet") { _ -> payWithWallet(accountNumber, amount) }
        }.show()
    }

    private fun payWithCard(accountNumber: String, amount: String) {
        isCard = true
        val clientReference = StringUtils.getClientRef(this@SmileTopup, "")

        cardDetails = SmileModel.SmileTopUpRequestDetails(accountNumber, amount, mPayviceWalletId, mPayviceUsername, mPayvicePassword, null, method = "card",clientReference = clientReference)

        val intent = Intent(this@SmileTopup, VasPurchaseProcessor::class.java)
        intent.putExtra(BasePaymentActivity.TRANSACTION_ACCOUNT_TYPE, AccountType.DEFAULT_UNSPECIFIED)

        //times 100 because of the conversion to kobo
        val amount = amount.toLong() * 100

        intent.putExtra(BasePaymentActivity.TRANSACTION_AMOUNT, amount)
        intent.putExtra(BasePaymentActivity.TRANSACTION_ADDITIONAL_AMOUNT, 0L)

        if (SharedPreferenceUtils.getIsTerminalPrepped(this)){
            startActivityForResult(intent, 9090)
        } else {
            alert {
                isCancelable = false
                title = "Terminal not configured"
                message = "Click O.K to go to configuration page"
                okButton {
                    startActivity(Intent(this@SmileTopup, TermMagmActivity::class.java))

                }
            }.show()
        }


    }


    private fun payWithWallet(accountNumber: String, amount: String) {
        isCard = false
        val view = View.inflate(this, R.layout.activity_enter_pin, null)
        PinAlertUtils.getPin(this, view){ pin ->
            val clientReference = StringUtils.getClientRef(this@SmileTopup, "")

            val encryptedPin = SecureStorageUtils.hashIt(pin, mPayvicePassword)
            val details = SmileModel.SmileTopUpRequestDetails(accountNumber, amount, mPayviceWalletId, mPayviceUsername, mPayvicePassword, encryptedPin, method = "cash",clientReference = clientReference)

            pay(details)

        }
    }

    private fun isValidated(): Boolean {
        if (smile_account_number.text.toString().length != 10) {
            smile_account_number.error = "Enter a valid account number"
            return false
        }

        if (smile_topup_amount.text.toString().isEmpty()) {
            smile_topup_amount.error = "Enter a valid top-up amount"
            return false
        }


        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            9090 -> when (resultCode) {
                Activity.RESULT_OK -> {
                    val state = data?.getSerializableExtra("state") as DeviceState
                    mRrn = data?.getStringExtra("rrn")

                    when (state) {
                        DeviceState.APPROVED -> {

                            (application as App).db.transactionResultDao.get(mRrn).observe({lifecycle}){
                                mTransactionResult = it!!
                                pay(cardDetails)
                            }
                        }

                        DeviceState.DECLINED, DeviceState.FAILED -> {
                            gotoHome()
                            toast("Transaction declined")
                        }
                    }


                }
            }
        }
    }

    private fun gotoHome() {

        finish()
        startActivity(Intent(this, SmileLookup::class.java))

    }
}
