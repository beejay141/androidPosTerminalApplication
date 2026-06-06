package com.iisysgroup.androidlite.vas.internet.smile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import com.iisysgroup.androidlite.App
import com.iisysgroup.androidlite.PrintActivity
import com.iisysgroup.androidlite.PrintTest
import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.R.id.smile_account_number
import com.iisysgroup.androidlite.R.id.smile_continue_btn
import com.iisysgroup.androidlite.cardpaymentprocessors.PurchaseProcessor
import com.iisysgroup.androidlite.cardpaymentprocessors.VasPurchaseProcessor
import com.iisysgroup.androidlite.models.ReceiptModel
import com.iisysgroup.androidlite.payments_menu.BasePaymentActivity
import com.iisysgroup.androidlite.utils.EncryptionUtils
import com.iisysgroup.androidlite.utils.PinAlertUtils
import com.iisysgroup.androidlite.utils.SharedPreferenceUtils
import com.iisysgroup.androidlite.utils.StringUtils
import com.iisysgroup.poslib.deviceinterface.DeviceState
import com.iisysgroup.poslib.host.entities.TransactionResult
import com.iisysgroup.poslib.utils.AccountType
import com.itex.richard.payviceconnect.model.SmileModel
import com.itex.richard.payviceconnect.wrapper.PayviceServices
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_buy_bundle.*
import kotlinx.android.synthetic.main.activity_print.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.okButton
import org.jetbrains.anko.toast
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.*

class BuyBundle : AppCompatActivity(){

    private lateinit var rrn : String

    private val smileData by lazy {
        intent.getParcelableExtra("smile_extra") as SmileModel.Bundle
    }

    private val progressDialog by lazy {
        indeterminateProgressDialog(message = "Getting customer details").also {
            it.setCancelable(false)
        }
    }

    private var isCard = false
    private var status = 1


    private val mPayviceUsername by lazy {
        SharedPreferenceUtils.getPayviceUsername(this)
    }

    private val mPayvicePassword by lazy {
        SharedPreferenceUtils.getPayvicePassword(this)
    }


    private val mPayviceWalletId by lazy {
        SharedPreferenceUtils.getPayviceWalletId(this)
    }

    private val mPayviceServices by lazy {
        PayviceServices.getInstance(this)
    }

    private lateinit var cardDetails : SmileModel.SmileSubscribe

    private lateinit var lookupDetails: SmileModel.SmileValidateCustomerResponse

    private lateinit var accountId : String

    private lateinit var transactionResult: TransactionResult

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy_bundle)

        smile_continue_btn.setOnClickListener {
            if (validated()) {
                progressDialog.show()

                    val accountNumber = smile_account_number.text.toString()
                    val details = SmileModel.SmileValidateCustomerRequest(accountNumber, mPayviceUsername, mPayvicePassword, "", mPayviceWalletId)
                    accountId = accountNumber

                    mPayviceServices.SmileValidateCustomer(details)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({response: SmileModel.SmileValidateCustomerResponse ->
                                lookupDetails = response
                                progressDialog.dismiss()
                                 if (response.status == 1) {
                                     alert {
                                         title = "Customer's details"
                                         message = "Name : ${response.customerName}"
                                         positiveButton(buttonText = "Confirm"){
                                             showPaymentOptions(accountNumber)
                                         }
                                         negativeButton(buttonText = "Cancel"){

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


                            },
                            {onError: Throwable ->
                                progressDialog.dismiss()
                                Log.d("OkH", onError.message.toString())
                            })

                }
            }


    }


    private fun showPaymentOptions(accountNumber: String) {
        alert {
            title = "Transaction Type"
            message = "Select the type of transaction you want to make"
            positiveButton(buttonText = "Card") { _ -> payWithCard(accountNumber)}
            negativeButton(buttonText = "Wallet") {_ -> payWithWallet(accountNumber)}
        }.show()
    }

    private fun pay(details : SmileModel.SmileSubscribe) {
        progressDialog.setMessage("Processing Transaction")
        progressDialog.show()
        mPayviceServices.SmileSubscribe(details)
               .subscribeOn(Schedulers.io())
               .observeOn(AndroidSchedulers.mainThread())
               .subscribe({ response ->
                   progressDialog.dismiss()
                   alert {
                       title = "Response"
                       message = response.message
                       positiveButton(buttonText = "Print"){
                           printReceipt(response)

                       }
                   }.show()
               },{

                   when {
                       it as Exception is SocketTimeoutException -> {
                           progressDialog.dismiss()
                           alert {
                               title = "Response"
                               message = "Connection taking too long. Try again later."
                           }.show()
                       }
                       it is ConnectException -> {
                           progressDialog.dismiss()
                           alert{
                               title = "Response"
                               message = "Please check your internet connection. Connection could not be established"
                           }.show()
                       }
                       else -> alert{
                           title = "Response"
                           message = "Error. Please try again later."
                       }.show()
                   }


               })
    }

    private fun printReceipt(response: SmileModel.SmileSuccessResponse) {

        //Smile Print Model
        var transactionType = ""
        var printStatus = ""
        var pan = ""
        var cardHolderName = ""
        var cardExpiry = ""
        var terminalId = ""
        var walletId = ""



        if (response.status == 1){
            printStatus = "APPROVED"
        }else{
            printStatus = "DECLINED"
        }
        transactionType = "SMILE BUY BUNDLE"
        val intent = Intent(this@BuyBundle, PrintActivity::class.java)
        val amount = (smileData.displayPrice / 100).toString()
        val referenceNumber = response.reference
        val map = HashMap<String, String>()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        SharedPreferenceUtils.getTerminalId(this)
        val accountID = smile_account_number.text.toString()
        terminalId = sharedPreferences.getString(this.getString(R.string.key_terminal_id), "")
        walletId = sharedPreferences.getString(this.getString(R.string.key_payvice_wallet_id), "")
            val calendar = Calendar.getInstance()
            val date = calendar.time

        map.put("Transaction Status", printStatus+"")
        map.put("Terminal ID", terminalId)
        map.put("Wallet ID", walletId)
        map.put("CustomerID", accountID)
        map.put("Reference Number", referenceNumber+"")
        map.put("Transaction Type", transactionType)
        map.put("Customer Name", lookupDetails.customerName+"")
        map.put("Name", smileData.name)
        map.put("Validity", smileData.validity)
        if (isCard) {
            pan =  transactionResult.PAN.toString()
             cardHolderName  = transactionResult.cardHolderName.toString()
              cardExpiry  = transactionResult.cardExpiry.toString()
            map.put("PAN", pan)
            map.put("Card Name", cardHolderName)
            map.put("Card Expiry", cardExpiry)
        }



        val receiptModel =
                if (isCard){
                    ReceiptModel(date.toString(), transactionType, printStatus, map, amount, transactionResult.transactionStatusReason)
                } else {
                    ReceiptModel(date.toString(), transactionType, printStatus, map, amount, printStatus)
                }


        intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_VAS_TYPE, PrintActivity.VasType.SMILE_DATA_PURCHASE)
        intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_MODEL_KEY, receiptModel)

       startActivity(intent)



    }

    private fun payWithWallet(accountNumber: String) {
        isCard = false
        val view = LayoutInflater.from(this@BuyBundle).inflate(R.layout.activity_enter_pin, null, false)
        PinAlertUtils.getPin(this@BuyBundle, view){
            val clientReference = StringUtils.getClientRef(this@BuyBundle, "")

            val ePin = EncryptionUtils.encryptPin(this@BuyBundle, it)
            val details = SmileModel.SmileSubscribe(accountNumber, mPayviceWalletId, smileData.code.toString(), smileData.price.toString(), mPayviceUsername, mPayvicePassword,  ePin, "cash", clientReference)
            pay(details)
        }

    }

    private fun payWithCard(accountNumber: String) {
        isCard = true
        val intent = Intent(this, VasPurchaseProcessor::class.java)
        val clientReference = StringUtils.getClientRef(this@BuyBundle, "")


        cardDetails = SmileModel.SmileSubscribe(accountNumber, mPayviceWalletId, smileData.code.toString(), smileData.price.toString(), mPayviceUsername, mPayvicePassword, null, "card",clientReference)

        intent.putExtra(BasePaymentActivity.TRANSACTION_ACCOUNT_TYPE, AccountType.DEFAULT_UNSPECIFIED)

        //times 100 because of the conversion to kobo
        val amount = smileData.displayPrice.toLong()

        intent.putExtra(BasePaymentActivity.TRANSACTION_AMOUNT,  amount)
        intent.putExtra(BasePaymentActivity.TRANSACTION_ADDITIONAL_AMOUNT, 0L)
        startActivityForResult(intent, 9090)
    }


    private fun validated(): Boolean {
        if (smile_account_number.text.toString().length != 10){
            smile_account_number.error = "Enter valid account number"
            return false
        }

        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode){
            9090 -> when (resultCode){
                Activity.RESULT_OK -> {
                    val state = data?.getSerializableExtra("state") as DeviceState
                    rrn = data.getStringExtra("rrn")

                    when (state){
                        DeviceState.APPROVED -> {
                            (application as App).db.transactionResultDao.get(rrn).observe({lifecycle}){
                                transactionResult = it!!
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
        startActivity(Intent(this@BuyBundle, SmileLookup::class.java))
    }
}
