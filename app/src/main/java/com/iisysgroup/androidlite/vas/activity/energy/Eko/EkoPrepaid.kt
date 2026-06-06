package com.iisysgroup.androidlite.vas.activity.energy.Eko

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
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
import com.iisysgroup.androidlite.cardpaymentprocessors.PurchaseProcessor
import com.iisysgroup.androidlite.cardpaymentprocessors.VasPurchaseProcessor
import com.iisysgroup.androidlite.generators.PfmJournalGenerator
import com.iisysgroup.androidlite.generators.PfmStateGenerator
import com.iisysgroup.androidlite.login.Helper
import com.iisysgroup.androidlite.login.securestorage.SecureStorage
import com.iisysgroup.androidlite.models.BaseReceiptDetails
import com.iisysgroup.androidlite.models.Pfm
import com.iisysgroup.androidlite.models.ReceiptModel
import com.iisysgroup.androidlite.payments_menu.BasePaymentActivity
import com.iisysgroup.androidlite.utils.NetworkUtils
import com.iisysgroup.androidlite.utils.PinAlertUtils
import com.iisysgroup.androidlite.utils.SharedPreferenceUtils
import com.iisysgroup.androidlite.vas.services.EkoService
import com.iisysgroup.payvice.securestorage.SecureStorageUtils
import com.iisysgroup.poslib.deviceinterface.DeviceState
import com.iisysgroup.poslib.host.entities.ConfigData
import com.iisysgroup.poslib.host.entities.TransactionResult
import com.iisysgroup.poslib.host.entities.VasTerminalData
import com.iisysgroup.poslib.utils.AccountType
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_eko_prepaid.*
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
import java.util.*

@Suppress("IMPLICIT_CAST_TO_ANY")
class EkoPrepaid : AppCompatActivity(), PinAlertUtils.PinEnteredListener {
    override fun onPinEntered(pin: String?) {
         val ePassword = SecureStorage.retrieve(Helper.STORED_PASSWORD,"")
         val encryptedPin = SecureStorageUtils.hashIt(pin!!, ePassword)
        this.pin = pin!!
        if (isCard){
            payWithCard(meterNumber, phoneNumber, amount, pin!!)
//            this.finish()
        } else {
            payWithWallet(meterNumber, phoneNumber, amount, false, pin!!)
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
    private lateinit var lookupResponse : EkoModel.EkoLookUpSuccessResponse

    private lateinit var meterNumber : String
    private lateinit var amount : String
    private lateinit var phoneNumber : String
    private lateinit var pin : String
    private lateinit var configData : ConfigData
    private lateinit var vasTerminalData : VasTerminalData


    private val wallet_username by lazy {
        SharedPreferenceUtils.getPayviceUsername(this)
    }

    private val mTerminalId by lazy {
        SharedPreferenceUtils.getTerminalId(this)
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


    private var isCard : Boolean = false

    private lateinit var mTransactionResult : TransactionResult


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eko_prepaid)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        submit.setOnClickListener { showPhoneNumberInput() }
        if (!NetworkUtils.isConnectionAvailable(this@EkoPrepaid)){
            Snackbar.make(ekedc_prepaid,
                    "No Internet Connection", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun showPhoneNumberInput() {
        val meterNumber = customerNumber.text.toString()

        if (meterNumber.length < 8){
            this.customerNumber.error = "Enter valid meter number"
            return
        }

        MaterialDialog.Builder(this@EkoPrepaid).title("Phone number input").content("Please enter your phone number").inputType(InputType.TYPE_CLASS_PHONE).input("Phone number", "") { _, input -> handleEkoPayments(input.toString(), meterNumber) }.show()

    }

    private fun handleEkoPayments(phoneNumber : String, meterNumber: String) {
        if (phoneNumber.length != 11){
            toast("Enter valid phone number")
            return
        }
        if (!NetworkUtils.isConnectionAvailable(this@EkoPrepaid)){
            Snackbar.make(ekedc_prepaid,
                    "No Internet Connection", Snackbar.LENGTH_LONG).show()
        }else {
            mProgressDialog.show()
            mProgressDialog.setCancelable(false)

            async {
                try {
                    val ekoDetails = EkoModel.EkoLookupDetails(meter = meterNumber)
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
                        lookupResponse = gson.fromJson(jsonResponse, EkoModel.EkoLookUpSuccessResponse::class.java)
                        launch(UI) {
                            alert {
                                title = "Confirm"
                                message = "Name : ${lookupResponse.name}\nAddress : ${lookupResponse.address}"
                                okButton { enterAmount(meterNumber, phoneNumber) }
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
                } catch (e: retrofit2.HttpException) {
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
        MaterialDialog.Builder(this@EkoPrepaid).title("Enter amount").content("Amount").inputType(InputType.TYPE_CLASS_NUMBER).input("Amount", "") { _, input -> selectTransactionType(meter, phoneNumber, input.toString()) }.show()
    }

    private fun selectTransactionType(meter: String, phoneNumber: String, amount : String) {
        meterNumber = meter
        this.phoneNumber = phoneNumber
        this.amount = amount
        if (amount.toFloat() < 900) {
            alert {
                title = "Low Amount"
                message = "Minimum Of #900"
                positiveButton(buttonText = "Ok") {}
            }.show()
        } else {
            val view = LayoutInflater.from(this@EkoPrepaid).inflate(R.layout.activity_enter_pin, null, false)
            alert {
                title = "Transaction Type"
                message = "Select the type of transaction you want to make"
                positiveButton(buttonText = "Card") { _ ->
                    isCard = true
                    PinAlertUtils.getPin(this@EkoPrepaid, view, this@EkoPrepaid)
                }
                negativeButton(buttonText = "Wallet") { _ ->
                    isCard = false

                    PinAlertUtils.getPin(this@EkoPrepaid, view, this@EkoPrepaid)
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

    private fun payWithCard(meter : String, phoneNumber: String, amount: String, pin : String){
        meterNumber = meter
        this.phoneNumber = phoneNumber
        this.amount = amount
        val intent = Intent(this, VasPurchaseProcessor::class.java)
        intent.putExtra(BasePaymentActivity.TRANSACTION_ACCOUNT_TYPE, AccountType.DEFAULT_UNSPECIFIED)

        //amount * 100 to convert the amount to long
        intent.putExtra(BasePaymentActivity.TRANSACTION_AMOUNT,  (amount.toLong() * 100))
        intent.putExtra(BasePaymentActivity.TRANSACTION_ADDITIONAL_AMOUNT, 0L)

        if (SharedPreferenceUtils.getIsTerminalPrepped(this)){
            startActivityForResult(intent, EkoPostpaid.KEYS.EKO_PREPAID_INTENT_CODE)
        } else {
            alert {
                isCancelable = false
                title = "Terminal not configured"
                message = "Click O.K to go to configuration page"
                okButton {
                    startActivity(Intent(this@EkoPrepaid, TermMagmActivity::class.java))
                    //this@EkoPrepaid.finish()
                }
            }.show()
        }


    }

    private fun payWithWallet(meterNumber: String, phoneNumber: String, amount: String, isCard : Boolean, pin : String){
        mProgressDialog.show()
        launch(CommonPool) {
            configData = ConfigData()
            val journal = PfmJournalGenerator(TransactionResult(), configData, false, amount.toLong(), null).generateJournal()
            val state = PfmStateGenerator(this@EkoPrepaid).generateState()
            val pfm = Pfm(state, journal)

            val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
            val payDetails = if (isCard){
                EkoModel.EkoPayDetails(meter = meterNumber, phone = phoneNumber, amount = amount,
                        terminal_id = wallet_id, type = "card", password = wallet_password, user_id = wallet_username,
                        pin = pin, channel = "ANDROIDPOS", pfm = pfm)
            } else {
                EkoModel.EkoPayDetails(meter = meterNumber, phone = phoneNumber, amount = amount,
                        terminal_id = wallet_id, type = "cash", password = wallet_password, user_id = wallet_username,
                        pin = pin, channel = "ANDROIDPOS", pfm = pfm)
            }

            try {
                val request = EkoService.create().pay(payDetails).await()

                val jsonResponse = Gson().toJsonTree(request).asJsonObject
                val response1 = gson.fromJson(jsonResponse.toString(), EkoModel.EkoPayFailedResponse::class.java)


                launch(UI){
                    mProgressDialog.dismiss()
                }
                if (response1.error == "true"){
                    val response = gson.fromJson(jsonResponse.toString(), EkoModel.EkoPayFailedResponse::class.java)

                    launch(UI){
                        alert {
                            title = "Response"
                            message = "Error : ${response.message}"

                            okButton { moveToHome() }
                        }.show()
                    }

                } else {
                    val response = gson.fromJson(jsonResponse.toString(), EkoModel.EkoPaySuccessResponse::class.java)
                    launch(UI){
                        alert {
                            title = "Response"
                            message = "${response.message}"
                            positiveButton(buttonText = "Print"){

                                val intent = Intent(this@EkoPrepaid, PrintActivity::class.java)
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
                                            "STAN" to mTransactionResult.STAN,
                                            "Token " to response.token
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
                                            "Token " to response.token,
                                            "Terminal ID" to mTerminalId)
                                }
                                val receiptModel = if (isCard){
                                    ReceiptModel(date, "Eko Prepaid Payment", "Approved", receiptMap, response.amount, mTransactionResult.transactionStatusReason)
                                } else {
                                    ReceiptModel(date, "Eko Prepaid Payment", "", receiptMap, response.amount, "Approved")
                                }
                                intent.putExtra("print_map", receiptModel)
                                intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_VAS_TYPE, PrintActivity.VasType.EKEDC_POSTPAID)
                                startActivity(intent)
                                finish()
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
                        okButton {
                            moveToHome()
                        }
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
            catch (e : retrofit2.HttpException){
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode){
            KEYS.EKO_PREPAID_INTENT_CODE -> when (resultCode){
                Activity.RESULT_OK -> {
                    val state = data?.getSerializableExtra("state") as DeviceState
                    val rrn = data?.getStringExtra("rrn")
                    toast(state.toString())

                    state?.let {
                        when (it){
                            DeviceState.APPROVED -> {
                                mProgressDialog.show()
                                launch(CommonPool){
                                    (application as App).db.transactionResultDao.get(rrn).observe({lifecycle}){
                                        mTransactionResult = it!!
                                        payWithWallet(meterNumber, phoneNumber, amount, true, pin)
                                    }
                                }
                            }
                            DeviceState.DECLINED -> {
                                toast("Transaction declined")
                                moveToHome()
                            }
                            DeviceState.FAILED -> {
                                toast("Transaction declined")
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
