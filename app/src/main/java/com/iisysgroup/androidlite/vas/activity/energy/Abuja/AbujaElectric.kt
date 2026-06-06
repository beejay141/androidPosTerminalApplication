package com.iisysgroup.androidlite.vas.activity.energy.Abuja

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.afollestad.materialdialogs.MaterialDialog
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.iisysgroup.androidlite.App
import com.iisysgroup.androidlite.PrintActivity

import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.TermMagmActivity
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
import com.iisysgroup.androidlite.vas.activity.energy.model.EnergyModel
import com.iisysgroup.androidlite.vas.activity.energy.services.EnergyServices
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
import kotlinx.android.synthetic.main.activity_abuja_electric.*
import kotlinx.android.synthetic.main.activity_abuja_electric.toolbar
import kotlinx.android.synthetic.main.activity_kano_electric.*
import kotlinx.android.synthetic.main.energy_activity.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.okButton
import org.jetbrains.anko.toast
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException

class AbujaElectric : AppCompatActivity(), PinAlertUtils.PinEnteredListener {


    override fun onPinEntered(pin: String?) {
            val ePassword = SecureStorage.retrieve(Helper.STORED_PASSWORD,"")
            val encryptedPin = SecureStorageUtils.hashIt(pin!!, ePassword)
            this.pin = pin!!
            if (isCard){
                payWithCard(meterNumber, phoneNumber, amount, encryptedPin!!)

            } else {
                payWithWallet(meterNumber, phoneNumber, amount, false, encryptedPin)
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kano_electric)

        setSupportActionBar(toolbar)
        setConfig()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title= "Abuja Electric"

        selectEnergyType()

        enter.setOnClickListener { showPhoneNumberInput() }
        if (!NetworkUtils.isConnectionAvailable(this@AbujaElectric)){
            Snackbar.make(disco_layout,
                    "No Internet Connection", Snackbar.LENGTH_LONG).show()
        }
    }
    private lateinit var meterNumber : String
    private lateinit var amount : String
    private lateinit var pin : String
    private lateinit var phoneNumber: String
    private lateinit var type : String
    private lateinit var prodCode: String
    private lateinit var configData : ConfigData
    private lateinit var vasTerminalData : VasTerminalData

    private fun selectEnergyType(){
        val energyType = arrayOf("prepaid","smart meter", "postpaid")

        val adapter = ArrayAdapter(
                this, // Context
                android.R.layout.simple_spinner_item, // Layout
                energyType // Array
        )
        // Set the drop down view resource
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        energySpinner.adapter = adapter

        energySpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (p0 != null) {
                     type = p2.toString()
                }
            }

        }
    }
    var isCard = false

    private lateinit var lookupResponse : EnergyModel.AbjLookupSuccessResponse


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

    private fun showPhoneNumberInput() {
        meterNumber = accountNumber.text.toString()

        if (meterNumber.length < 8){
            this.accountNumber.error = "Enter valid meter number"
            return
        }

        phoneNumber = mobileNumber.text.toString()
        if (phoneNumber.length != 11){
            mobileNumber.error = "Enter valid phone number"
            return
        }
        handleValidation(meterNumber, phoneNumber)

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
    private fun handleValidation(meterNumber: String, phoneNumber: String) {
        if (!NetworkUtils.isConnectionAvailable(this@AbujaElectric)) {
            Snackbar.make(disco_layout,
                    "No Internet Connection", Snackbar.LENGTH_LONG).show()
        } else {
        mProgressDialog.show()
            mProgressDialog.setCancelable(false)
        async {
            try {
                val abjDetails = EnergyModel.AbjLookupDetails(wallet_id, wallet_username, meterType = type,
                        meterNo = meterNumber, channel = "POS", requestType = "0")
                val response = EnergyServices.create().AbjLookup(abjDetails).await()

                val jsonResponse = Gson().toJsonTree(response)
                val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()

                launch(UI) {
                    mProgressDialog.hide()
                }
                if (jsonResponse.toString().contains("\"error\":true")) {
                    launch(UI) {
                        toast("Error")
                        val response = gson.fromJson(jsonResponse, EnergyModel.LookUpFailedResponse::class.java)
                        alert {
                            title = "Validation error"
                            message = response.message
                            okButton { moveToHome() }
                        }.show()
                    }
                } else {
                    // Log.d("response: ", jsonResponse.asString)
                    lookupResponse = gson.fromJson(jsonResponse, EnergyModel.AbjLookupSuccessResponse::class.java)
                    launch(UI) {
                        alert {
                            title = "Confirm"
                            message = "${lookupResponse.message}\nName : ${lookupResponse.name}\nMeter No : ${lookupResponse.customerMeterNo}"
                            okButton { enterAmount(meterNumber, phoneNumber) }
                            prodCode = lookupResponse.productCode
                            isCancelable = false
                        }.show()
                    }
                }
            } catch (exception: ConnectException) {
                launch(UI) {
                    mProgressDialog.dismiss()
                    alert {
                        title = "Response"
                        message = "Error in connection. Please check your internet connection"
                        okButton { }
                    }.show()
                }

            } catch (exception: SocketTimeoutException) {
                launch(UI) {
                    mProgressDialog.dismiss()
                    alert {
                        title = "Response"
                        message = "This connection is taking too long. Please try again"
                    }.show()
                }
            } catch (e: HttpException) {
                launch(UI) {
                    mProgressDialog.dismiss()
                    alert {
                        title = "Error"
                        message = "Error from server. Please try again"
                        okButton { }
                    }.show()
                }
            } catch (e: IllegalStateException) {
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

    private fun enterAmount(meter : String, phoneNumber: String) {
        MaterialDialog.Builder(this@AbujaElectric).title("Enter amount").content("Amount").
                inputType(InputType.TYPE_CLASS_NUMBER).input("Amount", "")
        { _, input -> selectTransactionType(meter, phoneNumber, input.toString().toInt()*100) }.show()
    }

    private fun selectTransactionType(meter: String, phoneNumber: String, amount : Int) {
        meterNumber = meter
        this.amount = amount.toString()

        if (amount < 100) {
            alert {
                title = "Response"
                message = "Invalid Amount!! Minimum of #50"

                positiveButton(buttonText = "Ok"){

                }
            }.show()
        } else {

            val view = LayoutInflater.from(this@AbujaElectric).inflate(R.layout.activity_enter_pin, null, false)
            alert {
                title = "Transaction Type"
                message = "Select the type of transaction you want to make"
                positiveButton(buttonText = "Card") { _ ->
                    isCard = true
                    PinAlertUtils.getPin(this@AbujaElectric, view, this@AbujaElectric)
                }
                negativeButton(buttonText = "Wallet") { _ ->
                    isCard = false

                    PinAlertUtils.getPin(this@AbujaElectric, view, this@AbujaElectric)
                }
            }.show()
        }
    }



    private fun payWithWallet(meterNumber: String, phoneNumber: String, amount: String, isCard : Boolean,
                              pin : String?)
    {
        mProgressDialog.show()
        mProgressDialog.setCancelable(false)
        var abjPayDetails: EnergyModel.AbjPayDetails
        val clientReference = StringUtils.getClientRef(this@AbujaElectric, "")
        configData = ConfigData()
        val pfm = Pfm(PfmStateGenerator(this@AbujaElectric).generateState(), PfmJournalGenerator(TransactionResult(), configData, false, amount.toLong(), null).generateJournal())

        launch(CommonPool) {

            val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
            abjPayDetails = if (isCard){
                EnergyModel.AbjPayDetails(wallet = wallet_id,username = wallet_username,requestType = "1",channel = "ANDROIDPOS",pin = pin!!,meterNo = meterNumber,
                        amount = amount,phone = phoneNumber,productCode = prodCode,paymentMethod = "card", meterType = type, pfm = pfm)
            } else {
                EnergyModel.AbjPayDetails(wallet = wallet_id,username = wallet_username,requestType = "1",channel = "ANDROIDPOS",pin = pin!!,meterNo = meterNumber,
                        amount = amount,phone = phoneNumber,productCode = prodCode,paymentMethod = "cash", meterType = type, pfm = pfm)
            }

            try {
                val request =EnergyServices.create().AbjPay(abjPayDetails).await()

                val jsonResponse = Gson().toJsonTree(request).asJsonObject
                val response1 = gson.fromJson(jsonResponse.toString(), EnergyModel.PhPayFailedResponse::class.java)

                launch(UI){
                    mProgressDialog.dismiss()
                }
                if (response1.error == "true"){
                    val response = gson.fromJson(jsonResponse.toString(), EnergyModel.PhPayFailedResponse::class.java)

                    launch(UI){
                        alert {
                            title = "Response"
                            message = "Error : ${response.message}\n Description: ${response.description}"

                            okButton {
                                moveToHome()
                            }
                        }.show()
                    }

                } else {
                    val response = gson.fromJson(jsonResponse.toString(), EnergyModel.AbjPaySuccessResponse::class.java)
                    launch(UI) {
                        alert {
                            title = "Response"
                            message = "${response.message}"
                            isCancelable = false
                            positiveButton(buttonText = "Print") {
                                var receiptMap: HashMap<String, String>? = null
                                if (response.token.isNullOrEmpty()) {
                                    receiptMap = hashMapOf<String, String>(
                                            "Terminal ID" to SharedPreferenceUtils.getTerminalId(this@AbujaElectric),
                                            "Wallet ID" to wallet_id,
                                            "Meter No" to response.customerMeterNo,
                                            "Ref" to response.reference,
                                            "Transaction Id" to response.transactionID,
                                            "Account Type" to response.type,
                                            "Receipt Number" to response.receiptNumber,
                                            "Name" to response.description,
//                                            "Description" to response.description,
                                            "Payment Method" to "Cash",
                                            "Address" to response.address,
                                            "Date" to response.txndate
                                    )
                                } else {
                                    receiptMap = hashMapOf<String, String>(
                                            "Terminal ID" to SharedPreferenceUtils.getTerminalId(this@AbujaElectric),
                                            "Wallet ID" to wallet_id,
                                            "Meter No" to response.customerMeterNo,
                                            "Ref" to response.reference,
                                            "Transaction Id" to response.transactionID,
                                            "Account Type" to response.type,
                                            "Receipt Number" to response.receiptNumber,
                                            "Name" to response.description,
//                                            "Description" to response.description,
                                            "Payment Method" to "Cash",
                                            "Address" to response.address,
                                            "Token" to response.token,
                                            "Date" to response.txndate)
                                }
                                val amountToPrint: Int = response.amountPaid.toInt() / 100
                                val receiptModel = ReceiptModel(response.txndate, "ABUJA ${response.type.toUpperCase()}", "", receiptMap, amountToPrint.toString(), "APPROVED")

                                val intent = Intent(this@AbujaElectric, PrintActivity::class.java)
                                intent.putExtra("print_map", receiptModel)
                                intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_VAS_TYPE, PrintActivity.VasType.AEDC)
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
    private fun payWithCard(meter: String, phoneNumber: String, amount: String, pin: String) {
        this.meterNumber = meter
        this.phoneNumber = phoneNumber
        this.amount = amount

        this.pin = pin
        val intent = Intent(this, VasPurchaseProcessor::class.java)
        intent.putExtra(BasePaymentActivity.TRANSACTION_ACCOUNT_TYPE, AccountType.DEFAULT_UNSPECIFIED)
        intent.putExtra(BasePaymentActivity.TRANSACTION_TYPE, Host.TransactionType.BILL_PAYMENT)

        //amount * 100 to convert the amount to long
        intent.putExtra(BasePaymentActivity.TRANSACTION_AMOUNT, (amount.toLong()))
        intent.putExtra(BasePaymentActivity.TRANSACTION_ADDITIONAL_AMOUNT, 0L)

        if (SharedPreferenceUtils.getIsTerminalPrepped(this)) {
            startActivityForResult(intent, KEYS.ABJ_PREPAID_INTENT_CODE)
        } else {
            alert {
                isCancelable = false
                title = "Terminal not configured"
                message = "Click O.K to go to configuration page"
                okButton {
                    startActivity(Intent(this@AbujaElectric, TermMagmActivity::class.java))
                    //this@EkoPostpaid.finish()
                }
            }.show()
        }


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            KEYS.ABJ_PREPAID_INTENT_CODE ->
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val deviceState = data?.getSerializableExtra("state") as DeviceState
                        val rrn = data.getStringExtra("rrn")

                        when (deviceState) {
                            DeviceState.APPROVED -> {
                                mProgressDialog.show()
                                (application as App).db.transactionResultDao.get(rrn).observe({ lifecycle }) {
                                    it?.let { transactionResult ->

                                        try {
                                            launch(CommonPool) {
                                                configData = ConfigData()
                                                val pfm = Pfm(PfmStateGenerator(this@AbujaElectric).generateState(), PfmJournalGenerator(transactionResult, configData, false, amount.toLong(), null).generateJournal())

                                                val payDetails = EnergyModel.AbjPayDetails(wallet = wallet_id, username = wallet_username, requestType = "0", channel = "ANDROIDPOS", pin = pin!!, meterNo = meterNumber,
                                                        amount = amount, phone = phoneNumber, productCode = prodCode, paymentMethod = "card", meterType = type, pfm = pfm)

                                                val cardResponse = EnergyServices.Factory.create().AbjPay(payDetails).await()

                                                val jsonResponse = Gson().toJsonTree(cardResponse).asJsonObject
                                                val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                                                val response1 = gson.fromJson(jsonResponse.toString(), EnergyModel.PhPayFailedResponse::class.java)
                                                if (response1.equals("true")) {
                                                    val response = gson.fromJson(jsonResponse.toString(), EnergyModel.PhPayFailedResponse::class.java)
                                                    launch(UI) {
                                                        alert {
                                                            title = "Response"
                                                            message = response.message
                                                            positiveButton(buttonText = "Ok") {
                                                                moveToHome()
                                                            }
                                                        }.show()
                                                    }

                                                } else {
                                                    val response = gson.fromJson(jsonResponse.toString(), EnergyModel.AbjPaySuccessResponse::class.java)
                                                    launch(UI) {
                                                        alert {
                                                            title = "Response"
                                                            message = "Message : ${response.message}"
                                                            isCancelable = false
                                                            positiveButton(buttonText = "Print") {
                                                                var receiptMap: HashMap<String, String>? = null
                                                                if (response.token.isNullOrEmpty()) {
                                                                    receiptMap = hashMapOf<String, String>(
                                                                            "Terminal ID" to SharedPreferenceUtils.getTerminalId(this@AbujaElectric),
                                                                            "Wallet ID" to wallet_id,
                                                                            "RRN" to transactionResult.RRN,
                                                                            "Card PAN" to transactionResult.PAN,
                                                                            "CardHolder" to transactionResult.cardHolderName,
                                                                            "Card Expiry" to transactionResult.cardExpiry,
                                                                            "Address" to response.address,
                                                                            "Meter No" to response.customerMeterNo,
                                                                            "Ref" to response.reference,
                                                                            "Transaction Id" to response.transactionID,
                                                                            "Name" to response.description,
//                                                                        "Account Type" to response.type,
                                                                            "Payment Method" to "Card",
                                                                            "Address" to response.address,
                                                                            "Date" to response.txndate
                                                                    )
                                                                } else {
                                                                    receiptMap = hashMapOf<String, String>(
                                                                            "Terminal ID" to SharedPreferenceUtils.getTerminalId(this@AbujaElectric),
                                                                            "Wallet ID" to wallet_id,
                                                                            "RRN" to transactionResult.RRN,
                                                                            "Card PAN" to transactionResult.PAN,
                                                                            "CardHolder" to transactionResult.cardHolderName,
                                                                            "Card Expiry" to transactionResult.cardExpiry,
                                                                            "Address" to response.address,
                                                                            "Meter No" to response.customerMeterNo,
                                                                            "Ref" to response.reference,
                                                                            "Transaction Id" to response.transactionID,
                                                                            "Name" to response.description,
//                                                                        "Account Type" to response.type,
//                                                                        "Receipt Number" to response.receiptNumber,
                                                                            "Payment Method" to "Card",
                                                                            "Token" to response.token,
                                                                            "Date" to response.txndate
                                                                    )
                                                                }

                                                                val receiptModel = ReceiptModel(response.txndate, "ABUJA ELECTRIC " + " PURCHASE", "", receiptMap, (response.amountPaid.toInt() / 100).toString(), transactionResult.transactionStatusReason)

                                                                val intent = Intent(this@AbujaElectric, PrintActivity::class.java)
                                                                intent.putExtra("print_map", receiptModel)
                                                                intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_VAS_TYPE, PrintActivity.VasType.AEDC)
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
                                        } catch (exception: ConnectException) {
                                            launch(UI) {
                                                mProgressDialog.dismiss()
                                                alert {
                                                    title = "Response"
                                                    message = "Error in connection. Please check your internet connection"
                                                    okButton { moveToHome() }
                                                }.show()
                                            }

                                        } catch (exception: SocketTimeoutException) {
                                            launch(UI) {
                                                mProgressDialog.dismiss()
                                                alert {
                                                    title = "Response"
                                                    message = "This connection is taking too long. Please try again"
                                                    okButton { moveToHome() }
                                                }.show()
                                            }
                                        } catch (e: HttpException) {
                                            launch(UI) {
                                                mProgressDialog.dismiss()
                                                alert {
                                                    title = "Error"
                                                    message = "Error from server. Please try again"
                                                    okButton { moveToHome() }
                                                }.show()
                                            }
                                        } catch (e: IllegalStateException) {
                                            launch(UI) {
                                                mProgressDialog.dismiss()
                                                alert {
                                                    title = "Error"
                                                    message = "Error from server. Please try again"
                                                    okButton { moveToHome() }
                                                }.show()
                                            }
                                        }
                                    }
                                }

                            }
                            DeviceState.DECLINED, DeviceState.FAILED -> {
//                                alert {
//                                    title = "Transaction Failed"
//                                    message = "Purchase transaction failed. Please try again"
//                                }.show()
                                (application as App).db.transactionResultDao.get(rrn).observe({ lifecycle }) {
                                    it?.let { transactionResult ->
                                        launch(UI) {
                                            alert {
                                                title = "Response"
                                                message = "Purchase transaction failed. Please try again"
                                                positiveButton(buttonText = "Print") {
                                                    val receiptMap = hashMapOf<String, String>(
                                                            "Terminal ID" to SharedPreferenceUtils.getTerminalId(this@AbujaElectric),
                                                            "Wallet ID" to wallet_id,
                                                            "RRN" to transactionResult.RRN,
                                                            "Card PAN" to transactionResult.PAN,
                                                            "CardHolder" to transactionResult.cardHolderName,
                                                            "Card Expiry" to transactionResult.cardExpiry,
                                                            "Auth ID" to transactionResult.authID,
                                                            "MID" to transactionResult.merchantID,
                                                            "STAN" to transactionResult.STAN

                                                    )

                                                    val receiptModel = ReceiptModel("", "ABUJA " + type.toUpperCase() + " PURCHASE", "", receiptMap, (amount.toInt()/100).toString(), "Declined")

                                                    val intent = Intent(this@AbujaElectric, PrintActivity::class.java)
                                                    intent.putExtra("print_map", receiptModel)
                                                    intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_VAS_TYPE, PrintActivity.VasType.AEDC)
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
            else -> {
                toast("Not OK")
            }

        }
    }

    private fun moveToHome() {
        finish()
        val intent = Intent(this, AbujaElectric::class.java)
        startActivity(intent)
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

    object KEYS {
        const val ABJ_PREPAID_INTENT_CODE = 3432
    }

}
