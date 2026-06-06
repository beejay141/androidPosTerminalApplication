package com.iisysgroup.androidlite.payments_menu.transfer

//import AmpEmvL2Android.AMPDevice
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.google.gson.Gson
import com.iisysgroup.androidlite.App
import com.iisysgroup.androidlite.PrintActivity
import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.cardpaymentprocessors.VasPurchaseProcessor
import com.iisysgroup.androidlite.generators.PfmJournalGenerator
import com.iisysgroup.androidlite.generators.PfmStateGenerator
import com.iisysgroup.androidlite.login.Helper
import com.iisysgroup.androidlite.login.securestorage.SecureStorage
import com.iisysgroup.androidlite.models.*
import com.iisysgroup.androidlite.models.WithdrawalWalletResponse.WithdrawalWalletCreditModel
import com.iisysgroup.androidlite.models.transfer.TransferSuccessModel
import com.iisysgroup.androidlite.payments_menu.BasePaymentActivity
import com.iisysgroup.androidlite.payments_menu.Services.TransferService
import com.iisysgroup.androidlite.payments_menu.models.*
import com.iisysgroup.androidlite.payments_menu.utils.HashUtils
import com.iisysgroup.androidlite.utils.PinAlertUtils
import com.iisysgroup.androidlite.utils.SharedPreferenceUtils
import com.iisysgroup.androidlite.utils.StringUtils.getClientRef
import com.iisysgroup.androidlite.utils.TimeUtils
import com.iisysgroup.payvice.securestorage.SecureStorageUtils
import com.iisysgroup.poslib.deviceinterface.DeviceState
import com.iisysgroup.poslib.host.entities.ConfigData
import com.iisysgroup.poslib.host.entities.TransactionResult
import com.iisysgroup.poslib.host.entities.VasTerminalData
import com.iisysgroup.poslib.utils.AccountType
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_transfer_amount_entry.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.okButton
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import java.io.UnsupportedEncodingException
import java.math.BigDecimal
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URLEncoder
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class TransferAmountEntry : AppCompatActivity(), View.OnClickListener  {

    enum class TRANSACTION_TYPE {TRANSFER, WITHDRAWAL, DEPOSIT}

    private lateinit var mConvenienceFee : String
    private lateinit var mRrn : String

    private lateinit var mEncryptedPin : String
    private var inview :  Boolean = false
    private var withdrawalDetails: WithdrawalDetails? = null

    lateinit var vasTerminalData : VasTerminalData
    lateinit var configData : ConfigData

    private val mTerminalId by lazy {
        SharedPreferenceUtils.getTerminalId(this)
    }

    private val mTransactionType by lazy {
        intent.getSerializableExtra(TransferBankSelection.TRANSACTION_TYPE) as TRANSACTION_TYPE
    }

    private val progressDialog by lazy {
        indeterminateProgressDialog(message = "Processing")
    }

    private val mBankName by lazy {
        intent.getStringExtra(TransferBankSelection.BANK_NAME)
    }

    private val mBankCode by lazy {
        intent.getStringExtra(TransferBankSelection.BANK_CODE)
    }

    private val mAccountNumber by lazy {
        intent.getStringExtra(TransferBankSelection.ACCOUNT_NUMBER)
    }

    private val mWalletUsername by lazy {
        SharedPreferenceUtils.getPayviceUsername(this)
    }

    private val mPlainPassword by lazy {
        SharedPreferenceUtils.getPlainPassword(this)
    }
//  private val mUserPhone by lazy {
//        SharedPreferenceUtils.getUserPhone(this)
//    }

    lateinit var mWalletId: String

    private val mWalletPassword by lazy {
        SharedPreferenceUtils.getPayvicePassword(this)
    }


    private lateinit var mAccountName : String
    private lateinit var mProductCode : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer_amount_entry)
        //Log.e("details", mPlainPassword )
        setConfig()
        initializeAmountEntryElements()
    }

    override fun onClick(view: View) {

        val displayStr = StringBuilder(txtAmount.text.toString())
        when (view.id) {
            R.id.btn1 -> fixAppend(displayStr, "1")
            R.id.btn2 -> fixAppend(displayStr, "2")
            R.id.btn3 -> fixAppend(displayStr, "3")
            R.id.btn4 -> fixAppend(displayStr, "4")
            R.id.btn5 -> fixAppend(displayStr, "5")
            R.id.btn6 -> fixAppend(displayStr, "6")
            R.id.btn7 -> fixAppend(displayStr, "7")
            R.id.btn8 -> fixAppend(displayStr, "8")
            R.id.btn9 -> fixAppend(displayStr, "9")
            R.id.btn0 -> fixAppend(displayStr, "0")
            R.id.btn00 -> fixAppend(displayStr, "00")
            R.id.btnenter -> onEnterPressed()
            R.id.btnclr -> {
                if (displayStr.length == 1) {
                    displayStr.deleteCharAt(0)
                    fixDelete(displayStr)
                } else if (displayStr.length > 1) {
                    val index = displayStr.length - 1
                    displayStr.deleteCharAt(index)
                    fixDelete(displayStr)
                }
            }
            R.id.btncancel -> onBackPressed()

        }
    }

    private fun onEnterPressed() {
        if (txtAmount.text.toString().isNotEmpty()){
            if (mTransactionType.toString().equals("WITHDRAWAL")){
                verifyWithdrawalAccountDetails()
            }else if (mTransactionType.toString().equals("TRANSFER")){
                verifyTransferAccountDetails()
            }


        } else {
            txtAmount.error = "Enter valid amount"
        }

    }

    private fun payWithCard(response: WithdrawalLookupSuccessModel) {
        val view = View.inflate(this, R.layout.activity_enter_pin, null)
        val encryptedPassword = SecureStorage.retrieve(Helper.STORED_PASSWORD, "")

        PinAlertUtils.getPin(this, view){
            mEncryptedPin = SecureStorageUtils.hashIt(it!!, encryptedPassword)!!

            val intent = Intent(this, VasPurchaseProcessor::class.java)
            intent.putExtra(BasePaymentActivity.TRANSACTION_ACCOUNT_TYPE, AccountType.DEFAULT_UNSPECIFIED)


            //times 100 because of the conversion to kobo
            val amount = (txtAmount.text.toString().toFloat() * 100) + response.convenienceFee
            Log.e("amount", amount.toString())
            intent.putExtra(BasePaymentActivity.TRANSACTION_AMOUNT,  amount.toLong())
            intent.putExtra(BasePaymentActivity.TRANSACTION_ADDITIONAL_AMOUNT, 0L)
            startActivityForResult(intent, 9090)
        }

    }

    private fun initializeAmountEntryElements() {
        findViewById<View>(R.id.btn1).setOnClickListener(this)
        findViewById<View>(R.id.btn2).setOnClickListener(this)
        findViewById<View>(R.id.btn3).setOnClickListener(this)
        findViewById<View>(R.id.btn4).setOnClickListener(this)
        findViewById<View>(R.id.btn5).setOnClickListener(this)
        findViewById<View>(R.id.btn6).setOnClickListener(this)
        findViewById<View>(R.id.btn7).setOnClickListener(this)
        findViewById<View>(R.id.btn8).setOnClickListener(this)
        findViewById<View>(R.id.btn9).setOnClickListener(this)
        findViewById<View>(R.id.btn0).setOnClickListener(this)
        findViewById<View>(R.id.btn00).setOnClickListener(this)
        findViewById<View>(R.id.btnclr).setOnClickListener(this)
        findViewById<View>(R.id.btnenter).setOnClickListener(this)
        findViewById<View>(R.id.btncancel).setOnClickListener(this)
    }

    private fun fixAppend(displayStr : StringBuilder, digit : String) {
        if(displayStr.length <= 11)
        {
            displayStr.append(digit)
            var newAmount = displayStr.toString().toDouble()
            // fix new input
            newAmount *= 10.00
            if("00" == digit) newAmount *= 10.00


            val updatedAmount = DecimalFormat("0.00").format(newAmount)
            txtAmount.text = updatedAmount.toString()
        }
    }

    private fun fixDelete(displayStr: StringBuilder) {
        var bd = BigDecimal(displayStr.toString())
        bd = bd.movePointLeft(1)

        txtAmount.text = bd.toString()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (requestCode == 9090){
            if (resultCode == Activity.RESULT_OK) {

                val state = data?.getSerializableExtra("state") as DeviceState
                mRrn = data.getStringExtra("rrn")

                when (state){
                    DeviceState.DECLINED, DeviceState.FAILED -> {
                        alert {
                            title = "Transaction Result"
                            message = "Transaction declined. Please try again later"
                            positiveButton(buttonText = "Print"){
                                (application as App).db.transactionResultDao.get(mRrn).observe({lifecycle}){

                                    it?.let {
                                        transactionResult ->

                                        val map = hashMapOf<String, String>(
                                                "MID" to transactionResult.merchantID,
                                                "RRN" to transactionResult.RRN,
                                                "Transaction approved" to "False",
                                                "Terminal ID" to mTerminalId,
                                                "Card Holder" to transactionResult.cardHolderName,
                                                "Card Expiry" to transactionResult.cardExpiry,
                                                "PAN" to transactionResult.PAN,
                                                "STAN" to transactionResult.STAN,
                                                "Auth ID" to transactionResult.authID,
                                                "Bank Name" to mBankName,
                                                "Beneficiary" to mAccountName,
                                                "Fee" to (mConvenienceFee.toDouble()/100).toString()
                                        )
                                        val date = TimeUtils.convertLongToString(transactionResult.longDateTime)
                                        val receiptModel = ReceiptModel(date, "Transfer", transactionResult.transactionStatus, map,  txtAmount.text.toString(), transactionResult.transactionStatusReason)

                                        val intent = Intent(this@TransferAmountEntry, PrintActivity::class.java)
                                        intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_MODEL_KEY, receiptModel)
                                        intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_VAS_TYPE, PrintActivity.VasType.NOT_INCLUDED)
                                        //finish()
                                        startActivity(intent)
                                    }

                                }
                            }
                        }.show()

                    }

                    DeviceState.APPROVED -> {
                        // completeCardPayment()


                        alert {
                            title = "Transaction Result"
                            message = "Transaction approved."
                            positiveButton(buttonText = "Print"){
                                (application as App).db.transactionResultDao.get(mRrn).observe({lifecycle}){

                                    it?.let {
                                        transactionResult ->
                                        creditWallet(transactionResult)
                                        val map = hashMapOf<String, String>(
                                                "MID" to transactionResult.merchantID,
                                                "RRN" to transactionResult.RRN,
                                                "Transaction approved" to "True",
                                                "Terminal ID" to mTerminalId,
                                                "Card Holder" to transactionResult.cardHolderName,
                                                "Card Expiry" to transactionResult.cardExpiry,
                                                "PAN" to transactionResult.PAN,
                                                "STAN" to transactionResult.STAN,
                                                "Amount" to (transactionResult.amount/100).toString(),
                                                "Auth ID" to transactionResult.authID,
                                                "Bank Name" to mBankName,
                                                "Beneficiary" to mAccountName,
                                                "Fee" to (mConvenienceFee.toInt()/100).toString()
                                        )
                                        val date = TimeUtils.convertLongToString(transactionResult.longDateTime)
                                        val receiptModel = ReceiptModel(date, "Withdrawal", transactionResult.transactionStatus, map,  txtAmount.text.toString(), transactionResult.transactionStatusReason)

                                        val intent = Intent(this@TransferAmountEntry, PrintActivity::class.java)
                                        intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_MODEL_KEY, receiptModel)
                                        intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_VAS_TYPE, PrintActivity.VasType.NOT_INCLUDED)
                                        startActivity(intent)
                                    }

                                }
                            }
                        }.show()
                    } else -> {
                    toast("No data!")
                }
                }
            }
        }
    }

    private fun verifyWithdrawalAccountDetails(){
        val progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(true)
        progressDialog.setTitle("Verification")
        progressDialog.setMessage("Now looking for account details")
//        progressDialog.show()

        mWalletId = SharedPreferenceUtils.getPayviceWalletId(this@TransferAmountEntry)

        try {
            val accountDetails = AccountLookUpDetailWithdrawal(wallet = mWalletId, username = mWalletUsername, type = "default", password = mWalletPassword, amount = txtAmount.text.toString().toFloat() * 100, channel = "POS")
            TransferService.create().lookUpAccountNumberWithdrawal(accountDetails).enqueue(object : Callback<WithdrawalLookupSuccessModel> {

                override fun onFailure(call: Call<WithdrawalLookupSuccessModel>, t: Throwable) {
                    Log.d("okh", t.message)
                }

                override fun onResponse(call: Call<WithdrawalLookupSuccessModel>, response: retrofit2.Response<WithdrawalLookupSuccessModel>) {

                    Log.d("okh", response.toString())
                    val amount = txtAmount.text.toString()
                    if (response.body()!!.status != 1) {
                        alert {
                            title = "Response"
                            message = response.body()!!.message
                            okButton { }
                        }.show()
                    } else {
                        mProductCode = response.body()!!.productCode
                        mAccountName = response.body()!!.beneficiaryName
                        mConvenienceFee = response.body()!!.convenienceFee.toString()

                        SecureStorage.store("currentName", mAccountName)
                        SecureStorage.store("conveniencefee", mConvenienceFee)

                        alert {
                            title = "${response.body()!!.message}"
                            message = "${response.body()!!.beneficiaryName}\nAmount - N$amount\nConvenience fee - N${response.body()!!.convenienceFee.toFloat() / 100}"
                            positiveButton("Continue") {
                                payWithCard(response.body()!!)
                            }
                        }.show()
                    }
                }

            })


        }
        catch (e : Exception) {
        }
    }

    private fun verifyTransferAccountDetails(){

        lateinit var response : LookupSuccessModel
        mWalletId = SharedPreferenceUtils.getPayviceWalletId(this@TransferAmountEntry)

        val  accountDetails = AccountLookUpDetailTransfer(wallet = mWalletId, username = mWalletUsername, type = "default", password = mWalletPassword, amount = txtAmount.text.toString().toDouble() * 100, channel = "ANDROIDPOS", beneficiary = mAccountNumber, vendorBankCode = mBankCode)

        val amount = txtAmount.text.toString()

        if (Helper.isOnline(this@TransferAmountEntry)) {

            try {
                val progressDialog = ProgressDialog.show(this@TransferAmountEntry, "Verification",
                        "Now looking for account details", true, true)
                progressDialog.show()

                val service = TransferService.create()
                service.lookUpAccountNumberTransfer(accountDetails).enqueue(object : Callback<LookupSuccessModel> {

                    override fun onFailure(call: Call<LookupSuccessModel>, t: Throwable) {
                        Log.d("okh", "error " + t.message)
                    }

                    override fun onResponse(call: Call<LookupSuccessModel>, response: retrofit2.Response<LookupSuccessModel>) {
                        //  val responses = response as LookupSuccessModel
                        progressDialog.cancel()
                        if (response.body() != null) {
                            Log.d("okh", "response " + response.body().toString())
                            if (response.body()!!.status != 1) {
                                alert {
                                    title = "Response"
                                    message = response.body()!!.message
                                    okButton { }
                                }.show()
                            } else {
                                mProductCode = response.body()!!.productCode
                                mAccountName = response.body()!!.beneficiaryName
                                mConvenienceFee = response.body()!!.convenienceFee.toString()

                                if (inview) {
                                    alert {
                                        title = "${response.body()!!.message}"
                                        message = "${response.body()!!.message} - ${response.body()!!.beneficiaryName}\nAmount - N$amount\nConvenience fee - N${response.body()!!.convenienceFee.toFloat() / 100}"
                                        positiveButton("Continue") {
                                            debitWallet()
                                        }

                                    }.show()
                                }

                            }
                        }

                    }
                })
            } catch (e: Exception) {

            }
        }
        else{
            toast("Please check your internet connection")
        }

    }

    private fun debitWallet(){
        val progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setTitle("Wallet")
        progressDialog.setMessage("Debitting wallet")
        progressDialog.show()

        val clientReference = getClientRef(this@TransferAmountEntry, "")
        lateinit var transferResponse : TransferSuccessModel
        lateinit var transferDetails : TransferDetails
        val view = View.inflate(this, R.layout.activity_enter_pin, null)
        val encryptedPassword = SecureStorage.retrieve(Helper.STORED_PASSWORD, "")
        var amount =  txtAmount.text.toString().toFloat()

        Log.d("debit amount", txtAmount.text.toString().toFloat().toString())
        //Log.d("debit mConvenienceFee",  (mConvenienceFee.toFloat() / 100).toString())
        Log.d("debit amount to debit",  amount.toString())
        PinAlertUtils.getPin(this, view) {
            mEncryptedPin = SecureStorageUtils.hashIt(it!!, encryptedPassword)!!
            launch(CommonPool) {
                try {

                    val action = when (mTransactionType) {
                        TRANSACTION_TYPE.TRANSFER -> "transfer"
                        TRANSACTION_TYPE.DEPOSIT -> "deposit"
                        TRANSACTION_TYPE.WITHDRAWAL -> "withdrawal"
                    }
                    configData = ConfigData()
                    val journal = PfmJournalGenerator(TransactionResult(), configData, false, amount.toLong(), null).generateJournal()
                    val state = PfmStateGenerator(this@TransferAmountEntry).generateState()
                    val pfm = Pfm(state, journal)


//clientReference = clientReference,
                    transferDetails = TransferDetails(wallet = mWalletId, username = mWalletUsername,
                            password = mPlainPassword, pin = mEncryptedPin, type = "default", amount = amount.toInt().toFloat()*100,
                            beneficiary = mAccountNumber, vendorBankCode = mBankCode, channel = "ANDROIDPOS",
                            phone = "", paymentMethod = "cash", productCode = mProductCode, pfm = pfm)

                    val clientReference = getClientRef(this@TransferAmountEntry, "")

                    Log.e("transfer details", mWalletId + " " + mWalletUsername + " " + mWalletPassword + " " + mPlainPassword + " " + mEncryptedPin + " " + " " + txtAmount.text.toString() + " " + mAccountNumber + " " + mBankCode )
                    val gson = Gson()
                    val jsonPayload = gson.toJson(transferDetails)
                    val base64encoded = String(org.apache.commons.codec.binary.Base64.encodeBase64(jsonPayload.toByteArray()))
                    val encoded = URLEncoder.encode(base64encoded, "UTF-8")
                    val nonce = clientReference
                    Log.e("sign", base64encoded + " " + encoded)
                    val encryptedStuff = "${nonce}IL0v3Th1sAp11111111UC4NDoV4SSWITHVICEBANKING$encoded"
                    val signature = HashUtils.sha512(encryptedStuff)


                    transferResponse = TransferService.create().transfer(transferDetails, "application/json", signature, nonce).await()
                    val amount = txtAmount.text.toString()
                    try {
                        launch(UI) {
                            progressDialog.dismiss()

                            if (transferResponse.status != 1) {
                                alert {
                                    title = "Response"
                                    message = "${transferResponse.message}"
                                    okButton { }
                                }.show()
                            } else {
                                alert {
                                    title = "Response"
                                    message = "${transferResponse.message}. Your wallet has been debitted \n " +
                                            "\n#${(transferResponse.amountDebited.toFloat()/100).toString()} \nBeneficiary : "+ mAccountName
                                    positiveButton(buttonText = "Print") {

                                        val map = hashMapOf<String, String>(
                                                "Reference" to transferResponse.reference,
                                                "Message" to transferResponse.message,
                                                "Account Name" to transferResponse.beneficiaryName,
                                                "Bank Name" to mBankName,
                                                "Account Number" to transferResponse.beneficiary,
                                                "Amount Debited" to (transferResponse.amountDebited/100).toString(),
                                                "Convenience Fee" to (transferResponse.convenienceFee/100).toString()
                                        )
                                        val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().time)

                                        val receiptModel = ReceiptModel(formattedDate, "Transfer", " ", map, (transferResponse.amountDebited/100).toString(), transferResponse.message)

                                        Log.d("debit print",  transferResponse.amountDebited.toString())

                                        val intent = Intent(this@TransferAmountEntry, PrintActivity::class.java)
                                        intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_MODEL_KEY, receiptModel)
                                        intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_VAS_TYPE, PrintActivity.VasType.ITEX)
                                        //finish()
                                        startActivity(intent)

                                    }
                                }.show()

                            }

                        }

                    } catch (e: Exception) {
                        launch(UI) {
                            progressDialog.dismiss()
                            alert {
                                title = "Response"
                                message = transferResponse.message
                            }.show()
                        }
                    }
                } catch (e: SocketTimeoutException) {
                    launch(UI) {
                        progressDialog.dismiss()
                        alert {
                            title = "Error"
                            message = "Connection taking too long to be established. Please try again"
                            okButton { onBackPressed() }
                        }.show()
                    }

                } catch (e: ConnectException) {
                    launch(UI) {
                        progressDialog.dismiss()
                        alert {
                            title = "Error"
                            message = "Connection not established. Please try again"
                            okButton { }
                        }.show()
                    }

                } catch (e: retrofit2.HttpException) {
                    launch(UI) {
                        progressDialog.dismiss()
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

    var amountToDebit : Double = 0.0

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

    private fun creditWallet(transactionresult : TransactionResult) {

        val amount = transactionresult.amount.toDouble()
        Log.d("okh", amount!!.toString() + "")
        lateinit var withdrawalResponse : WithdrawalWalletCreditModel

        launch(CommonPool) {

            val clientReference = getClientRef(this@TransferAmountEntry, "")
            val pfm = Pfm(PfmStateGenerator(this@TransferAmountEntry).generateState(), PfmJournalGenerator(transactionresult, configData, false, amount.toLong(), null).generateJournal())

            withdrawalDetails = WithdrawalDetails(wallet = mWalletId, username = mWalletUsername, password = mPlainPassword, pin = mEncryptedPin, type = "default", amount = amount, vendorBankCode = "", channel = "ANDROIDPOS", phone = "", paymentMethod = "card", productCode = mProductCode, pfm = pfm)

            Log.d("okh", transactionresult.terminalID)
            Log.d("okh", transactionresult.transactionStatus)
            Log.d("okh", transactionresult.responseCode)
            val gson = Gson()
            val jsonPayload = gson.toJson(withdrawalDetails)
            val base64encoded = String(org.apache.commons.codec.binary.Base64.encodeBase64(jsonPayload.toByteArray()))

            var encoded: String? = null
            try {
                encoded = URLEncoder.encode(base64encoded, "UTF-8")
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
            val nonce = clientReference
            Log.e("sign", "$base64encoded $encoded")
            val encryptedStuff = nonce + "IL0v3Th1sAp11111111UC4NDoV4SSWITHVICEBANKING" + encoded
            val signature = HashUtils.sha512(encryptedStuff)

            withdrawalResponse = TransferService.create().withdraw(withdrawalDetails!!, "application/json", signature, nonce).await()

            launch(UI) {
                try {
                    alert {
                        title = "Transaction Result"
                        message = withdrawalResponse.message
                        okButton { }
                    }.show()
                }catch (e : Exception){
                    alert {
                        title = "Transaction Result"
                        message = withdrawalResponse.message
                        okButton { }
                    }.show()
                }

            }


        }
    }

    override fun onResume() {
        inview = true
        super.onResume()
    }

    override fun onPause() {
        inview = false
        super.onPause()
    }

}
