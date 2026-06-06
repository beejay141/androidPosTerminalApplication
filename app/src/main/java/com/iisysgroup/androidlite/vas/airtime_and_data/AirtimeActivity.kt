package com.iisysgroup.androidlite.vas.airtime_and_data

//import AmpEmvL2Android.AMPDevice
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import com.iisysgroup.androidlite.App
import com.iisysgroup.androidlite.PrintActivity
import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.R.id.naira_sign
import com.iisysgroup.androidlite.VasActivity
import com.iisysgroup.androidlite.cardpaymentprocessors.PurchaseProcessor
import com.iisysgroup.androidlite.cardpaymentprocessors.VasPurchaseProcessor
import com.iisysgroup.androidlite.db.AirtimeModel
import com.iisysgroup.androidlite.generators.PfmJournalGenerator
import com.iisysgroup.androidlite.generators.PfmStateGenerator
import com.iisysgroup.androidlite.login.Helper
import com.iisysgroup.androidlite.login.securestorage.SecureStorage
import com.iisysgroup.androidlite.models.PfmDetails
import com.iisysgroup.androidlite.models.ReceiptModel
import com.iisysgroup.androidlite.payments_menu.BasePaymentActivity
import com.iisysgroup.androidlite.utils.PinAlertUtils
import com.iisysgroup.androidlite.utils.SharedPreferenceUtils
import com.iisysgroup.payvice.securestorage.SecureStorageUtils
import com.iisysgroup.poslib.commons.emv.EmvCard
import com.iisysgroup.poslib.deviceinterface.DeviceState
import com.iisysgroup.poslib.host.Host
import com.iisysgroup.poslib.host.entities.ConfigData
import com.iisysgroup.poslib.host.entities.KeyHolder
import com.iisysgroup.poslib.host.entities.TransactionResult
import com.iisysgroup.poslib.utils.AccountType
import com.pax.PaxDevice
//import com.pos.device.printer.Printer
import kotlinx.android.synthetic.main.airtime_provider_select.*
import kotlinx.android.synthetic.main.enter_amount.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.*
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.*


class AirtimeActivity : AirTimeBaseActivity(), AirtimeProcessor.onAirtimeTransactionResultListener {

    private var isCard = false
    private var transactionResult: TransactionResult ?= null

    private val beneficiariesDao by lazy {
        (application as App).beneficiariesDatabase.getAirtimeBeneficiariesDao()
    }

    private val hostInteractor by lazy {
        (application as App).hostInteractor
    }

    private val mTerminalId by lazy {
        SharedPreferenceUtils.getTerminalId(this)

    }

    private val ampDevice by lazy {
        PaxDevice(this)
    }

    private lateinit var mPin : String


    //Boolean value to indicate if current top-up recipient is a beneficiary saved
    private var isBeneficiary = false

    lateinit var constraintLayout: ConstraintLayout

    private var pfmDetails : PfmDetails ?= null


    private val progressDialog by lazy {
        indeterminateProgressDialog(message = "Processing request", title = "Status") {}
    }
    var airtime_amount: String = ""
    var phone_number: String = ""
    var airtime_provider = ""

    lateinit var enter: Button
    lateinit var cancel: Button

    var isFromVasPage = false

    override fun onResponse(model: AirtimeSuccessResponse) {

        if (isBeneficiary) {
            alert {
                title = "Response"
                message = model.message
                isCancelable = false
                positiveButton(buttonText = "Print") {
                    generateReceipt(model)
                }
            }.show()
        } else {
            alert {
                title = "Beneficiary"
                isCancelable = false
                message = "${model.message}. This number is not currently saved. Would you want to save this number for future transactions"
                yesButton {
                    addBeneficiary(model)
                }
                negativeButton(buttonText = "No") {
                    generateReceipt(model)
                }

            }.show()

        }

        progressDialog.hide()

    }

    //AirtimeSuccessResponse represents both successful and failed transactions
    private fun generateReceipt(model: AirtimeSuccessResponse) {
        val intent = Intent(this@AirtimeActivity, PrintActivity::class.java)

        val date = Calendar.getInstance().time.toString()
        val map = HashMap<String, String>()

        if (isCard) {
            map.put("RRN", transactionResult!!.RRN)
            map.put("STAN", transactionResult!!.STAN)
            map.put("MID", transactionResult!!.merchantID)
            map.put("Card PAN", transactionResult!!.PAN)
            map.put("Card Holder", transactionResult!!.cardHolderName)
            map.put("Card Expiry", transactionResult!!.cardExpiry)
            map.put("Auth ID", transactionResult!!.authID)
        }

        map.put("Terminal ID", mTerminalId)
        map.put("Wallet ID", SharedPreferenceUtils.getPayviceWalletId(this@AirtimeActivity))
        map.put("Recipient's Number", phone_number)
        map.put("Recipient's Network", airtime_provider)
        map.put("Ref", model.ref)
        map.put("Trans Id", model.transactionID)
        val printReceipt = if (model.error) {
            if (isCard) {
                ReceiptModel(date, "Airtime Transaction", "Declined", map, airtime_amount, transactionResult!!.transactionStatusReason)
            } else {
                ReceiptModel(date, "Airtime Transaction", "Declined", map, airtime_amount, "Declined")
            }
        } else {
            if (isCard) {
                ReceiptModel(date, "Airtime Transaction", "", map, airtime_amount, transactionStatusReason = transactionResult!!.transactionStatusReason)
            } else {
                ReceiptModel(date, "Airtime Transaction", "Approved", map, airtime_amount, transactionStatusReason = "")
            }
        }

        val vasType = when(airtime_provider){
            "AIRTELVTU" -> PrintActivity.VasType.AIRTEL_VTU
            "MTNVTU" -> PrintActivity.VasType.MTN_VTU
            "ETISALATVTU" -> PrintActivity.VasType.ETISALAT_VTU
            "GLOVTU" -> PrintActivity.VasType.GLO_VTU
            else -> PrintActivity.VasType.NOT_INCLUDED
        }

        intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_VAS_TYPE, vasType)
        intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_MODEL_KEY, printReceipt)



        startActivity(intent)
        finish()

    }

    private fun addBeneficiary(model: AirtimeSuccessResponse) {
        async {
            val beneficiary = AirtimeModel(phone_number, airtime_provider)
            beneficiariesDao.insert(beneficiary)
        }
        toast("Beneficiary saved")
        generateReceipt(model)
    }

    private fun goHome() {
        finish()
        startActivity(Intent(this@AirtimeActivity, VasActivity::class.java))
    }

    override fun onError(errorMessage: String, isCard: Boolean) {
        if (isCard) {
            hostInteractor.rollBackTransaction()
        }
        alert {
            title = "Response"
            message = errorMessage
            okButton { finish() }
        }.show()
        progressDialog.hide()
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return false
    }

    internal override fun getTextLayoutId(): Int {
        return R.id.txtAmount
    }

    internal override fun getMaxCount(): Int {
        return 13
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_airtime)
        super.onCreate(savedInstanceState)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        enter = findViewById(R.id.btnenter)
        cancel = findViewById(R.id.btncancel)


        constraintLayout = findViewById(R.id.constraint_layout)
        constraintLayout.tag = 0

        amount.text = ""
        enter.setOnClickListener { moveToNextPage() }

        cancel.setOnClickListener { moveToPreviousPage() }

        mtn.setOnClickListener {
            resetAirtimeValues()
            showPhoneNumberScreen()
            airtime_provider = "MTNVTU"
        }
        glo.setOnClickListener {
            resetAirtimeValues()
            showPhoneNumberScreen()
            airtime_provider = "GLOVTU"
        }
        nine_mobile.setOnClickListener {
            resetAirtimeValues()
            showPhoneNumberScreen()
            airtime_provider = "ETISALATVTU"
        }
        airtel.setOnClickListener {
            resetAirtimeValues()
            showPhoneNumberScreen()
            airtime_provider = "AIRTELVTU"
        }

        beneficiaries.setOnClickListener {
            startActivity(Intent(this@AirtimeActivity, AirtimeBeneficiariesActivity::class.java))
        }

        if (intent.hasExtra(TAGS.AIRTIME_PURCHASE_KEY)) {
            isFromVasPage = intent.getBooleanExtra(TAGS.AIRTIME_PURCHASE_KEY, false)
            airtime_provider = intent.getStringExtra(TAGS.AIRTIME_PURCHASE_PROVIDER_TYPE)
            showPhoneNumberScreen()
        } else if (intent.hasExtra("airtimeModel")) {
            nine_mobile.performClick()
            val airtimeModel = intent.getParcelableExtra("airtimeModel") as AirtimeModel
            phone_number = airtimeModel.phone_number
            txtAmount.text = phone_number

            enter.performClick()
        }

    }

    private fun resetAirtimeValues() {
        amount.text = ""
        phone_number = ""
        airtime_amount = ""
    }

    private fun payWithWallet(phone_number: String, airtimeProvider: String) {
        isCard = false

        val pinView = LayoutInflater.from(this).inflate(R.layout.activity_enter_pin, null, false)
        PinAlertUtils.getPin(this, pinView) {
            val encryptedPassword = SecureStorage.retrieve(Helper.STORED_PASSWORD, "")
            val pin = SecureStorageUtils.hashIt(it, encryptedPassword)
            val airtimeProcessor = AirtimeProcessor(this, this, airtimeProvider, phone_number.replace(" ", ""), airtime_amount)
            progressDialog.show()
            try {
                airtimeProcessor.performTransaction(pin = pin!!, transactionRes = null)
            } catch (error: ConnectException) {0
                progressDialog.dismiss()
                toast("Connection error, Check your internet connection")
            } catch (error: SocketTimeoutException) {
                toast("Connection taking too long. Please try again")
            }
        }
    }

    private fun payWithCard() {
        val view = View.inflate(this, R.layout.activity_enter_pin, null)

        PinAlertUtils.getPin(this, view) {
            //todo validate pin
            val password = SecureStorage.retrieve(Helper.STORED_PASSWORD, "")
            mPin = SecureStorageUtils.hashIt(it, password)!!

            isCard = true
            val intent = Intent(this, VasPurchaseProcessor::class.java)

            intent.putExtra(BasePaymentActivity.TRANSACTION_ACCOUNT_TYPE, AccountType.DEFAULT_UNSPECIFIED)
            intent.putExtra(BasePaymentActivity.TRANSACTION_TYPE, Host.TransactionType.BILL_PAYMENT)
            //times 100 because of the conversion to kobo
            intent.putExtra(BasePaymentActivity.TRANSACTION_AMOUNT, (airtime_amount.toLong() * 100))
            intent.putExtra(BasePaymentActivity.TRANSACTION_ADDITIONAL_AMOUNT, 0L)
            startActivityForResult(intent, AirtimeActivity.TAGS.AIRTIME_REQUEST_CODE)
        }


    }

    private fun performTransaction(airtimeProvider: String) {

        if (airtime_amount.isEmpty()) {
            toast("Enter valid amount")
            return
        }

        alert {
            title = "Transaction Type"
            message = "Select the type of transaction you want to make"
            positiveButton(buttonText = "Card") { _ -> payWithCard() }
            negativeButton(buttonText = "Wallet") { _ -> payWithWallet(phone_number, airtimeProvider) }
        }.show()


    }

    private fun moveToNextPage() {
        when (constraintLayout.tag as Int) {
            1 -> {
                showAmountScreen()
            }
            2 -> {
                if (amount.text.toString().isNotEmpty() || amount.text.toString().toInt() < 50) {
                    airtime_amount = amount.text.toString()
                    performTransaction(airtime_provider)
                } else {
                    toast("Enter valid amount - amount must not be less than 50 Naira")
                }
            }
        }
    }

    private fun moveToPreviousPage() {
        when (constraintLayout.tag as Int) {
            0 -> {
                startActivity(Intent(this@AirtimeActivity, SelectionActivity::class.java))
                finish()
            }
            1 -> {
                amount.text = phone_number
                showAirtimeProviderScreen()
            }
            2 -> {
                amount.text = airtime_amount
                showPhoneNumberScreen()
            }
        }
    }


//    {"amount":"50","password":"","pfm":{
//
// "getRRN":"000208041013",
//
// "journal":{"acode":"692522","amount":"5000","customField":"","mPan":"559441******2991","mcc":"","mid":"203315000001987","mti":"200","oacode":"692522","orrn":"000208041013","ostan":"041013","ps":"0","rep":true,"resp":"00","rrn":"000208041013","stan":"041013","tap":true,"timeStamp":"1549638613109","transMethod":"card","vasCategory":"","vasProduct":"","vm":"offline"},
//
//
// "state":{"batteryLevel":"0","chargingStatus":"CHARGING","communicationsMethod":"WIFI","currentLocation":"cid:9149268, lac:9149268, mcc:121, mnc:765, ss\"\"","currentTime":"20190239041017","hasBattery":"true","lastTransactionTime":"","pads":"","paperStatus":"OK","serialNumber":"58eb456c","signalStrength":"","softwareVersion":"1.0","terminalManufacturer":"unknown","terminalModel":"AMP8000","terminal_id":"2033GP23"}},"phone":"08132383284","pin":"1428d061d0ce21be364a62a87ad61794b3e040ed8643f37b9e88e7e8ea189466","service":"MTNVTU","terminal_id":"99539669","user_id":"mikelis135@gmail.com"}


//    private fun processCardTransaction() {
//
//
//        val keyHolder = KeyHolder("", "", "")
//        val pininfo = EmvCard.PinInfo(keyHolder.pinKey.toByteArray(), null, keyHolder.pinKey.toByteArray())
//        val emvcard = EmvCard(transactionResult!!.cardHolderName, "", "",pininfo)
//        val printer = Printer()
//        val pfmJournal = PfmJournalGenerator(transactionResult!!, ConfigData(), printer = printer, isReceiptPrinted = true, vasCategory = "", vasProduct = "", cardData = emvcard).generateJournal()
//        val pfmState = PfmStateGenerator(this@AirtimeActivity, ampDevice).generateState()
//        Log.d("pfmstate", pfmState.bl+" " +pfmState.cs)
//        pfmDetails = PfmDetails(pfmState, pfmJournal, transactionResult!!.RRN)
//        Log.d("pfmdetails", pfmDetails!!.toString())
//        val airtimeProcessor = AirtimeProcessor(this, this, airtime_provider, phone_number.replace(" ", ""), airtime_amount)
//        progressDialog.show()
//        try {
//            airtimeProcessor.performTransaction(true, pin = mPin)
//        } catch (error: ConnectException) {
//            progressDialog.dismiss()
//            toast("Connection error, Check your internet connection")
//        } catch (error: SocketTimeoutException) {
//            toast("Connection taking too long. Please try again")
//        } catch (e: retrofit2.HttpException) {
//            launch(UI) {
//                progressDialog.dismiss()
//                alert {
//                    title = "Error"
//                    message = "Error from server. Please try again"
//                    okButton { }
//                }.show()
//            }
//        }
//    }
private fun processCardTransaction(transactionResult: TransactionResult) {

    val airtimeProcessor = AirtimeProcessor(this, this, airtime_provider, phone_number.replace(" ", ""), airtime_amount)
    progressDialog.show()
    try {
        airtimeProcessor.performTransaction(true, pin = mPin,transactionRes =transactionResult)
    } catch (error: ConnectException) {
        progressDialog.dismiss()
        toast("Connection error, Check your internet connection")
    } catch (error: SocketTimeoutException) {
        toast("Connection taking too long. Please try again")
    } catch (e: retrofit2.HttpException) {
        // launch(UI) {
        progressDialog.dismiss()
        alert {
            title = "Error"
            message = "Error from server. Please try again"
            okButton { }
        }.show()
        // }
    }
}



    private fun showAmountScreen() {
        phone_number = amount.text.toString()
        if (phone_number.length != maxCount) {
            toast("Enter valid phone number")
            return
        }

        async {
            val beneficiary = beneficiariesDao.getAirtimeBeneficiaryByPhoneNumber(phone_number)
            isBeneficiary = beneficiary != null
        }

        naira_sign.visibility = View.VISIBLE
        showVisibility(findViewById(R.id.enter_phone_number_or_amount))

        if (airtime_amount.isNotEmpty()) {
            amount.text = airtime_amount
        } else
            amount.text = ""
        dashboard_title.setText(R.string.amount)
        constraintLayout.tag = 2
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            TAGS.AIRTIME_REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val state = data?.getSerializableExtra("state") as DeviceState
                        val rrn = data?.getStringExtra("rrn")

                        when (state) {
                            DeviceState.FAILED -> {
                                toast("Transaction failed")
                                goHome()
                            }
                            DeviceState.APPROVED -> {
                                (application as App).db.transactionResultDao.get(rrn).observe({ lifecycle }) {
                                    transactionResult = it!!
                                    processCardTransaction(it)
                                }
                            }
                            DeviceState.DECLINED -> {
                                toast("Transaction declined")
                                goHome()
                            }
                            else -> {
                            }
                        }


                    }
                    Activity.RESULT_CANCELED -> {
                        toast("Request unsuccessful")
                    }
                }
            }
        }
    }

    fun showPhoneNumberScreen() {
        showVisibility(findViewById(R.id.enter_phone_number_or_amount))
        airtime_amount = txtAmount.text.toString()
        naira_sign.visibility = View.GONE

        if (phone_number.isNotEmpty()) {
            amount.text = phone_number
        } else amount.text = ""
        dashboard_title.setText(R.string.phone_number)
        constraintLayout.tag = 1
    }

    fun showAirtimeProviderScreen() {
        phone_number = txtAmount.text.toString()
        showVisibility(findViewById(R.id.airtime_provider_select))
        dashboard_title.setText(R.string.action_select_provider)
        constraintLayout.tag = 0
    }

    override fun onBackPressed() {
        moveToPreviousPage()
    }

    object TAGS {
        const val AIRTIME_REQUEST_CODE = 3423
        const val AIRTIME_PURCHASE_KEY = "airtime_purchase_key"
        const val AIRTIME_PURCHASE_PROVIDER_TYPE = "airtime_purchase_provider_type"
    }
}
