package com.iisysgroup.androidlite.vas.activity.energy.Ibadan

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.util.Log
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
import com.iisysgroup.androidlite.models.PfmState
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
import com.itex.richard.payviceconnect.model.IbadanModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_kano_electric.*
import kotlinx.android.synthetic.main.activity_movie_house.*
import kotlinx.android.synthetic.main.activity_phelectric.*
import kotlinx.android.synthetic.main.activity_phelectric.toolbar
import kotlinx.android.synthetic.main.energy_activity.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.okButton
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException

class Ibedc: AppCompatActivity(), PinAlertUtils.PinEnteredListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kano_electric)
        setSupportActionBar(toolbar)
        setConfig()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title= "Ibadan Electric"
        selectEnergyType()

        enter.setOnClickListener { showPhoneNumberInput() }
        if (!NetworkUtils.isConnectionAvailable(this@Ibedc)){
            Snackbar.make(disco_layout,
                    "No Internet Connection", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onPinEntered(pin: String?) {
        val ePassword = SecureStorage.retrieve(Helper.STORED_PASSWORD,"")
        val encryptedPin = SecureStorageUtils.hashIt(pin!!, ePassword)
        this.pin = pin!!
        if (isCard){
             payWithCard(meterNumber, phoneNumber, amount, encryptedPin!!)
             //this.finish()
        } else {
            payWithWallet(meterNumber, phoneNumber, amount, false, encryptedPin)
        }
    }
    private lateinit var meterNumber : String
    private lateinit var amount : String
    private lateinit var pin : String
    private lateinit var phoneNumber: String
    private lateinit var type : String
    private lateinit var prodCode: String
    private lateinit var cust_name: String
    private lateinit var configData : ConfigData
    lateinit var vasTerminalData : VasTerminalData

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId){
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return false
    }

    private fun selectEnergyType(){
        val energyType = arrayOf("prepaid", "postpaid")

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
                    type = p0.getItemAtPosition(p2).toString()
                }
            }

        }
    }
    var isCard = false

    private lateinit var lookupResponse : EnergyModel.IbLookupSuccessResponse


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
        if (!NetworkUtils.isConnectionAvailable(this@Ibedc)) {
            Snackbar.make(disco_layout,
                    "No Internet Connection", Snackbar.LENGTH_LONG).show()
        } else {
            mProgressDialog.setCancelable(false)
            mProgressDialog.show()
            async {
                try {
                    val ibadanDetails = EnergyModel.IbLookupDetails(wallet_id, wallet_username, type,
                            meterNumber, "POS")
                    val response = EnergyServices.create().IbLookup(ibadanDetails).await()

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
                            }.show()
                        }
                    } else {
                        // Log.d("response: ", jsonResponse.asString)
                        lookupResponse = gson.fromJson(jsonResponse, EnergyModel.IbLookupSuccessResponse::class.java)
                        launch(UI) {
                            alert {
                                title = "Confirm"
                                message = "${lookupResponse.message}\nName : ${lookupResponse.name}"
                                okButton { enterAmount(meterNumber, phoneNumber) }
                                prodCode = lookupResponse.productCode
                                cust_name = lookupResponse.name
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
                            okButton { }
                        }.show()
                    }
                }
            }
        }
    }

    private fun enterAmount(meter : String, phoneNumber: String) {
        MaterialDialog.Builder(this@Ibedc).title("Enter amount").content("Amount").
                inputType(InputType.TYPE_CLASS_NUMBER).input("Amount", "")
        { _, input -> selectTransactionType(meter, phoneNumber, input.toString().toInt()*100) }.show()
    }

    private fun selectTransactionType(meter: String, phoneNumber: String, amount : Int) {
        meterNumber = meter
        this.amount = amount.toString()

        if (amount < 100) {
            alert {
                title = "Low Amount"
                message = "Minimum Of #50"
                positiveButton(buttonText = "Ok") {}
            }.show()
        } else {
            val view = LayoutInflater.from(this@Ibedc).inflate(R.layout.activity_enter_pin, null, false)
            alert {
                title = "Transaction Type"
                message = "Select the type of transaction you want to make"
                positiveButton(buttonText = "Card") { _ ->
                    isCard = true
                    PinAlertUtils.getPin(this@Ibedc, view, this@Ibedc)
                }
                negativeButton(buttonText = "Wallet") { _ ->
                    isCard = false

                    PinAlertUtils.getPin(this@Ibedc, view, this@Ibedc)
                }
            }.show()
        }
    }

    private fun payWithWallet(meterNumber: String, phoneNumber: String, amount: String, isCard : Boolean,
                              pin : String?)
    {
        mProgressDialog.show()
        mProgressDialog.setCancelable(false)
        var ibadanPayDetails: EnergyModel.IbPayDetails
        val clientReference = StringUtils.getClientRef(this@Ibedc, "")
        //configData = ConfigData()
        //val pfm = Pfm(PfmStateGenerator(this@Ibedc).generateState(), PfmJournalGenerator(TransactionResult(), configData, false, amount.toLong(), null).generateJournal())

        launch(CommonPool) {

            configData = ConfigData()
            val journal = PfmJournalGenerator(TransactionResult(), configData, false, amount.toLong(), null).generateJournal()
            val state = PfmStateGenerator(this@Ibedc).generateState()
            val pfm = Pfm(state, journal)
            val testin = Gson()
            val json = testin.toJson(pfm)
            //val pfm = Pfm(PfmStateGenerator(this@Ibedc).generateState(), PfmJournalGenerator(TransactionResult(), configData, false, amount.toLong(), null).generateJournal())

            //Log.d("ok", json)

            val gson = GsonBuilder().create()
//            ibadanPayDetails =  EnergyModel.IbPayDetails(wallet = wallet_id,username = wallet_username,
//                    type = type,channel = "ANDROIDPOS",pin = pin!!,account = meterNumber,
//                        amount = amount,phone = phoneNumber,productCode = prodCode,paymentMethod = "cash",
//                    clientReference = clientReference,customerName = cust_name, pfm = pfm)
            ibadanPayDetails =  EnergyModel.IbPayDetails(wallet = wallet_id,username = wallet_username,
                    type = type,channel = "ANDROIDPOS",pin = pin!!,account = meterNumber,
                        amount = amount,phone = phoneNumber,productCode = prodCode,paymentMethod = "cash",
                    clientReference = clientReference,customerName = cust_name, pfm = pfm)
            val paydetils = testin.toJson(ibadanPayDetails)

           // Log.d("okibadan", paydetils)

            try {
                val request = EnergyServices.create().IbadanPay(ibadanPayDetails).await()
                //var ibResponse = EnergyServices.create().IbadanPay(ibadanPayDetails).await()


                val jsonResponse = Gson().toJsonTree(request).asJsonObject
                val response1 = gson.fromJson(jsonResponse.toString(), EnergyModel.PhPayFailedResponse::class.java)


                launch(UI){
                    mProgressDialog.dismiss()
                }
                if (response1.error.equals(true)) {
                    val response = gson.fromJson(jsonResponse.toString(), EnergyModel.PhPayFailedResponse::class.java)

                    launch(UI){
                        alert {
                            title = "Response"
                            message = "Error : ${response.message}"

                            okButton { }
                        }.show()
                    }

                } else {
                    val response = gson.fromJson(jsonResponse.toString(), EnergyModel.IbPaySuccessResponse::class.java)
                    launch(UI){
                        alert {
                            title = "Response"
                            message = "${response.message}"
                            isCancelable = false
                            positiveButton(buttonText = "Print"){
                                var receiptMap : HashMap<String, String>? = null
                                if (response.token.isNullOrEmpty()){
                                    receiptMap = hashMapOf<String, String>(
                                            "Terminal ID" to SharedPreferenceUtils.getTerminalId(this@Ibedc),
                                            "Wallet ID" to wallet_id,
                                            "Name" to response.name,
                                            "Ref" to response.reference,
                                            "Transaction Id" to response.transactionID,
                                            "Account Type" to response.type,
//                                            "Receipt Number" to response.receiptNumber,
//                                            "Meter Number" to response.meterNumber,
                                            "Arrears" to response.arrears,
                                            "Payment Method" to "Cash"
//                                            "Tariff" to response.tariff,
//                                            "Address" to response.address
                                    )
                                }else {
                                    receiptMap = hashMapOf<String, String>(
                                            "Terminal ID" to SharedPreferenceUtils.getTerminalId(this@Ibedc),
                                            "Wallet ID" to wallet_id,
                                            "Name" to response.name,
                                            "Ref" to response.reference,
                                            "Transaction Id" to response.transactionID,
                                            "Account Type" to response.type,
//                                            "Receipt Number" to response.receiptNumber,
//                                            "Meter Number" to response.meterNumber,
                                            "Token" to response.token,
                                            "Payment Method" to "Cash"
//                                            "Arrears" to response.arrears,
//                                            "Tariff" to response.tariff,
//                                            "Address" to response.address
                                    )
                                }

//                                    val receiptMap = hashMapOf<String, String>(
//                                            "Terminal ID" to SharedPreferenceUtils.getTerminalId(this@Ibedc),
//                                            "Wallet ID" to wallet_id,
//                                            "Ref" to response.reference,
//                                            "Transaction Id" to response.transactionID,
//                                            "Account Type" to response.type,
//                                            "Name" to response.name,
//                                            "Account" to response.account,
//                                            "Address" to response.address,
//                                            "Token" to response.token
//
//                                    )
                                    val receiptModel = ReceiptModel("", "IBADAN ${response.type.toUpperCase()}", "", receiptMap, (response.amount.toFloat()/100).toString(), "APPROVED")

                                    val intent = Intent(this@Ibedc, PrintActivity::class.java)
                                    intent.putExtra("print_map", receiptModel)
                                    intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_VAS_TYPE, PrintActivity.VasType.IBEDC)
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
                        okButton { moveToHome() }
                    }.show()
                }
            }
            catch (e : HttpException){
                launch(UI){
                    mProgressDialog.dismiss()
                    alert {
                        title = "Error"
                        message = "Error from server. Please try again"
                        okButton { moveToHome() }
                    }.show()
                }
            }
            catch (e : IOException){
                launch(UI){
                    mProgressDialog.dismiss()
                    alert {
                        title = "Error"
                        message = "Error from server. Please try again"
                        okButton { moveToHome() }
                    }.show()
                }
            }
            catch (e : IllegalStateException){
                launch(UI){
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
            startActivityForResult(intent, KEYS.IBADAN_PREPAID_INTENT_CODE)
        } else {
            alert {
                isCancelable = false
                title = "Terminal not configured"
                message = "Click O.K to go to configuration page"
                okButton {
                    startActivity(Intent(this@Ibedc, TermMagmActivity::class.java))
                    //this@EkoPostpaid.finish()
                }
            }.show()
        }


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            KEYS.IBADAN_PREPAID_INTENT_CODE ->
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val deviceState = data?.getSerializableExtra("state") as DeviceState
                        val rrn = data?.getStringExtra("rrn")

                        when (deviceState) {
                            DeviceState.APPROVED -> {
                                mProgressDialog.show()
                                (application as App).db.transactionResultDao.get(rrn).observe({ lifecycle }) {
                                    it?.let { transactionResult ->

                                        try {
                                            launch(CommonPool) {
                                                configData = ConfigData()
                                                val pfm = Pfm(PfmStateGenerator(this@Ibedc).generateState(), PfmJournalGenerator(transactionResult, configData, false, amount.toLong(), null).generateJournal())

                                                val clientReference = StringUtils.getClientRef(this@Ibedc, "")
                                                val payDetails = EnergyModel.IbPayDetails(wallet = wallet_id, username = wallet_username, type = type, channel = "ANDROIDPOS", pin = pin!!, account = meterNumber,
                                                        amount = amount, phone = phoneNumber, productCode = prodCode, paymentMethod = "card", clientReference = clientReference, customerName = cust_name, pfm = pfm)

                                                val cardResponse = EnergyServices.Factory.create().IbadanPay(payDetails).await()

                                                val jsonResponse = Gson().toJsonTree(cardResponse).asJsonObject
                                                val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                                                val response1 = gson.fromJson(jsonResponse.toString(), EnergyModel.PhPayFailedResponse::class.java)

                                                if (response1.error == "true") {
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
                                                    val response = gson.fromJson(jsonResponse.toString(), EnergyModel.IbPaySuccessResponse::class.java)
                                                    launch(UI) {
                                                        alert {
                                                            title = "Response"
                                                            message = "Message : ${response.message}"
                                                            isCancelable = false
                                                            positiveButton(buttonText = "Print") {
                                                                var receiptMap: HashMap<String, String>? = null
                                                                if (response.token.isNullOrEmpty()) {
                                                                    receiptMap = hashMapOf<String, String>(
                                                                            "Terminal ID" to SharedPreferenceUtils.getTerminalId(this@Ibedc),
                                                                            "Wallet ID" to wallet_id,
                                                                            "RRN" to transactionResult.RRN,
                                                                            "Card PAN" to transactionResult.PAN,
                                                                            "CardHolder" to transactionResult.cardHolderName,
                                                                            "Card Expiry" to transactionResult.cardExpiry,
                                                                            "Auth ID" to transactionResult.authID,
                                                                            "MID" to transactionResult.merchantID,
                                                                            "STAN" to transactionResult.STAN,
                                                                            "Address" to response.address,
                                                                            "Ref" to response.reference,
                                                                            "Payment Method" to "Card",
                                                                            "Transaction Id" to response.transactionID,
                                                                            "Account Type" to response.type,
                                                                            "Name" to response.name,
                                                                            "Account" to response.account,
                                                                            "Address" to response.address
                                                                    )
                                                                } else {
                                                                    receiptMap = hashMapOf<String, String>(
                                                                            "Terminal ID" to SharedPreferenceUtils.getTerminalId(this@Ibedc),
                                                                            "Wallet ID" to wallet_id,
                                                                            "RRN" to transactionResult.RRN,
                                                                            "Card PAN" to transactionResult.PAN,
                                                                            "CardHolder" to transactionResult.cardHolderName,
                                                                            "Card Expiry" to transactionResult.cardExpiry,
                                                                            "Auth ID" to transactionResult.authID,
                                                                            "MID" to transactionResult.merchantID,
                                                                            "STAN" to transactionResult.STAN,
                                                                            "Address" to response.address,
                                                                            "Ref" to response.reference,
                                                                            "Payment Method" to "Card",
                                                                            "Transaction Id" to response.transactionID,
                                                                            "Account Type" to response.type,
                                                                            "Name" to response.name,
                                                                            "Token" to response.token,
                                                                            "Account" to response.account,
                                                                            "Address" to response.address
                                                                    )
                                                                }

                                                                val receiptModel = ReceiptModel("", "IBADAN " + type.toUpperCase() + " PURCHASE", transactionResult.transactionStatus, receiptMap, (response.amount.toFloat() / 100).toString(), transactionResult.transactionStatusReason)

                                                                val intent = Intent(this@Ibedc, PrintActivity::class.java)
                                                                intent.putExtra("print_map", receiptModel)
                                                                intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_VAS_TYPE, PrintActivity.VasType.IBEDC)
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
                                                            "Terminal ID" to SharedPreferenceUtils.getTerminalId(this@Ibedc),
                                                            "Wallet ID" to wallet_id,
                                                            "RRN" to transactionResult.RRN,
                                                            "Card PAN" to transactionResult.PAN,
                                                            "CardHolder" to transactionResult.cardHolderName,
                                                            "Card Expiry" to transactionResult.cardExpiry,
                                                            "Auth ID" to transactionResult.authID,
                                                            "MID" to transactionResult.merchantID,
                                                            "STAN" to transactionResult.STAN
                                                    )

                                                    val receiptModel = ReceiptModel("", "IBADAN " + type.toUpperCase() + " PURCHASE", "Declined", receiptMap, (amount.toInt()/100).toString(), "")

                                                    val intent = Intent(this@Ibedc, PrintActivity::class.java)
                                                    intent.putExtra("print_map", receiptModel)
                                                    intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_VAS_TYPE, PrintActivity.VasType.PHEDC)
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
        val intent = Intent(this, Ibedc::class.java)
        startActivity(intent)
    }



    object KEYS {
        const val IBADAN_PREPAID_INTENT_CODE = 3432
    }

}