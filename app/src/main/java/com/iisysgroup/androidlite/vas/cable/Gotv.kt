package com.iisysgroup.androidlite.vas.cable

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.iisysgroup.androidlite.*
import com.iisysgroup.androidlite.cardpaymentprocessors.VasPurchaseProcessor
import com.iisysgroup.androidlite.generators.PfmJournalGenerator
import com.iisysgroup.androidlite.generators.PfmStateGenerator
import com.iisysgroup.androidlite.login.Helper
import com.iisysgroup.androidlite.login.securestorage.SecureStorage
import com.iisysgroup.androidlite.models.Pfm
import com.iisysgroup.androidlite.models.ReceiptModel
import com.iisysgroup.androidlite.payments_menu.BasePaymentActivity
import com.iisysgroup.androidlite.payments_menu.TransactionProcessActivity
import com.iisysgroup.androidlite.utils.PinAlertUtils
import com.iisysgroup.androidlite.utils.SharedPreferenceUtils
import com.iisysgroup.androidlite.vas.activity.CableTVActivity
import com.iisysgroup.androidlite.vas.activity.energy.model.EnergyModel
import com.iisysgroup.androidlite.vas.services.DstvService
import com.iisysgroup.payvice.securestorage.SecureStorageUtils
import com.iisysgroup.poslib.deviceinterface.DeviceState
import com.iisysgroup.poslib.host.Host
import com.iisysgroup.poslib.host.entities.ConfigData
import com.iisysgroup.poslib.host.entities.TransactionResult
import com.iisysgroup.poslib.utils.AccountType
import kotlinx.android.synthetic.main.activity_ds_tv_vas.*
import kotlinx.android.synthetic.main.activity_ds_tv_vas.dstv_beneficiaries
import kotlinx.android.synthetic.main.activity_ds_tv_vas.dstv_continue_btn
import kotlinx.android.synthetic.main.activity_ds_tv_vas.dstv_smart_card_no
import kotlinx.android.synthetic.main.activity_ds_tv_vas.toolbar
import kotlinx.android.synthetic.main.activity_go_tv_vas.*
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
import java.util.*

class Gotv : AppCompatActivity() {
    private lateinit var beneficiaryName : String
    private lateinit var amount : String
    private lateinit var smartCardNo : String

    private val dstvBeneficiariesDb by lazy {
        (application as App).beneficiariesDatabase
    }

    private val wallet_username by lazy {
        SharedPreferenceUtils.getPayviceUsername(this@Gotv)
    }

    private val wallet_id by lazy {
        SharedPreferenceUtils.getPayviceWalletId(this@Gotv)
    }

    private val wallet_password by lazy {
        SharedPreferenceUtils.getPayvicePassword(this@Gotv)
    }


    private val mProgressDialog by lazy {
        indeterminateProgressDialog("Processing")
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

    private lateinit var payDetails : PayDetails

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_go_tv_vas)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)



        dstv_continue_btn.setOnClickListener {
            if (dstv_smart_card_no.text.toString().isEmpty() || dstv_smart_card_no.text.toString().length < 10 || dstv_smart_card_no.text.toString().length > 10){
                dstv_smart_card_no.error = "Enter valid number"
                smartCardNo = dstv_smart_card_no.text.toString()
            } else {
                lookupGotv()
            }
        }

        dstv_beneficiaries.setOnClickListener {
            mProgressDialog.show()
            async {
                val db = (application as App).beneficiariesDatabase
                val beneficiariesDao = db.getDstvBeneficiariesDao()
                val beneficiaries = beneficiariesDao.getAllBeneficiaries()
                Log.d("OkH", beneficiaries.toString())
                mProgressDialog.dismiss()
                if (beneficiaries.isEmpty()) {
                    launch(UI){
                        toast("You have no beneficiaries")
                    }

                    return@async
                }
                launch(UI){
                    showBeneficiaries(beneficiaries)
                }
            }
        }
    }

    private fun lookupGotv(iuc : String = "empty"){
        mProgressDialog.show()
        launch(CommonPool) {
            try {
                if (iuc.equals("empty")){
                    val iuc = dstv_smart_card_no.text.toString()
                    processTransaction(iuc)
                } else {
                    processTransaction(iuc)
                }

            } catch (exception : ConnectException){
                launch(UI){
                    mProgressDialog.dismiss()
                    alert {
                        title = " Response"
                        message = "Connection is faulty. Please check your internet connection"

                    }.show()

                }
            } catch (exception : SocketTimeoutException){
                launch(UI){
                    mProgressDialog.dismiss()
                    alert {
                        title = " Response"
                        message = "Connection is taking too long. Please try again later"
                    }.show()
                }
            } catch (e : HttpException){
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

    private suspend fun processTransaction(iuc: String) {
        val lookup = GotvLookupModel(iuc = iuc)
        val response = DstvService.Factory.create().gotvLookup(lookup).await()

        beneficiaryName = response.fullname!!

        launch(UI){
            mProgressDialog.dismiss()
            alert {
                title = "GOTV Purchase"
                message = "Card number : $iuc \nName : ${response.fullname} \nUnit : ${response.unit}"
                positiveButton(buttonText = "Confirm", onClicked = {_ ->
                    bottomSheetDialog(response.data, iuc)})
            }.show()
        }
    }

    private fun bottomSheetDialog(data: List<Data>, iuc: String) {
        val networkDialog = BottomSheetDialog(this)

        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_list_layout, null, false)
        networkDialog.setContentView(view)
        val textTitle = view.findViewById<TextView>(R.id.titleText)
        textTitle.text = "Select GOTV Service"

        val itemList = ArrayList<SingleImageTitleObject>()

        for (index in 0 until data.size){
            if (index == 0)
                itemList.add(SingleImageTitleObject("Active plan : ${data[index].amount} ${data[index].name}", R.drawable.gotv_logo, data[index].amount))
            else
                itemList.add(SingleImageTitleObject("${data[index].amount} ${data[index].name}", R.drawable.gotv_logo, data[index].amount))
        }

        val listview = view.findViewById<ListView>(R.id.list)


        listview.adapter = SingleImageTitleObject.SingleImageTitleAdapter(itemList, this,
                R.layout.bottom_sheet_list_item)
        listview.setOnItemClickListener {
            _, _, position, _ ->
            amount = itemList[position].amount
            mProgressDialog.show()
            async {
                makeDstvPayments(iuc = iuc, positionClicked = position, data = data)
            }
            networkDialog.dismiss()
        }

        networkDialog.show()

    }

    private fun showBeneficiaries( data : List<DstvBeneficiariesModel>){
        val networkDialog = BottomSheetDialog(this)

        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_list_layout, null, false)
        networkDialog.setContentView(view)
        val textTitle = view.findViewById<TextView>(R.id.titleText)
        textTitle.text = "Select Beneficiary"

        val itemList = ArrayList<SingleImageTitleObject>()

        for (index in 0 until data.size){
            itemList.add(SingleImageTitleObject("${data[index].name} ${data[index].dstvNumber}", R.drawable.dstv_logo))
        }

        val listview = view.findViewById<ListView>(R.id.list)



        listview.adapter = SingleImageTitleObject.SingleImageTitleAdapter(itemList, this,
                R.layout.bottom_sheet_list_item)

        listview.setOnItemClickListener {
            _, _, position, _ ->
            mProgressDialog.show()
            async {
                val iuc = data[position].dstvNumber
                lookupGotv(iuc)
            }
            networkDialog.dismiss()
        }

        networkDialog.show()

    }

    private fun makeDstvPayments(iuc : String, positionClicked : Int, data: List<Data> ) {
        val bouquet = data[positionClicked]

        mProgressDialog.dismiss()
        launch(UI){
            alert {
                title = "Transaction Type"
                message = "Select the type of transaction you want to make"
                positiveButton(buttonText = "Card") { _ -> payWithCard(bouquet, iuc)}
                negativeButton(buttonText = "Wallet") {_ -> payWithWallet(bouquet, iuc)}
            }.show()
        }
    }

    var  configData = ConfigData()


    private fun payWithCard(bouquet : Data, iuc: String){
        val view = View.inflate(this, R.layout.activity_enter_pin, null)

        val password = SecureStorage.retrieve(Helper.STORED_PASSWORD, "")



        PinAlertUtils.getPin(this, view){
            val encryptedPin = SecureStorageUtils.hashIt(it!!, password)


            val journal = PfmJournalGenerator(TransactionResult(), configData, false, amount.toLong(), null).generateJournal()
            val state = PfmStateGenerator(this@Gotv).generateState()
            val pfm = Pfm(state, journal)

            payDetails = PayDetails(iuc = iuc, product_code = bouquet.product_code, user_id = wallet_username, terminal_id = wallet_id, pin = encryptedPin!!, unit = "GOTV",pfm = pfm)
            val amountBrokenDown = bouquet.amount.split(".")



            val intent = Intent(this, VasPurchaseProcessor::class.java)
            intent.putExtra(BasePaymentActivity.TRANSACTION_ACCOUNT_TYPE, AccountType.DEFAULT_UNSPECIFIED)
            intent.putExtra(BasePaymentActivity.TRANSACTION_TYPE, Host.TransactionType.BILL_PAYMENT)

            //amount * 100 to convert the amount to long
            intent.putExtra(BasePaymentActivity.TRANSACTION_AMOUNT, (amountBrokenDown[0].toLong()* 100))
            intent.putExtra(BasePaymentActivity.TRANSACTION_ADDITIONAL_AMOUNT, 0L)

            if (SharedPreferenceUtils.getIsTerminalPrepped(this)) {
                startActivityForResult(intent, KEYS.PURCHASE_INTENT_CODE)
            } else {
                alert {
                    isCancelable = false
                    title = "Terminal not configured"
                    message = "Click O.K to go to configuration page"
                    okButton {
                        startActivity(Intent(this@Gotv, TermMagmActivity::class.java))
                        //this@EkoPostpaid.finish()
                    }
                }.show()
            }
        }
    }

    private fun payWithWallet(bouquet : Data, iuc : String){
        mProgressDialog.show()
        mProgressDialog.setCancelable(false)
        val view = View.inflate(this, R.layout.activity_enter_pin, null)
        val encryptedPassword = SecureStorage.retrieve(Helper.STORED_PASSWORD, "")

        PinAlertUtils.getPin(this, view){
            val encryptedPin = SecureStorageUtils.hashIt(it!!, encryptedPassword)
            launch(CommonPool) {


                val journal = PfmJournalGenerator(TransactionResult(), configData, false, amount.toLong(), null).generateJournal()
                val state = PfmStateGenerator(this@Gotv).generateState()
                val pfm = Pfm(state, journal)

                payDetails = PayDetails(iuc = iuc, product_code = bouquet.product_code, user_id = wallet_username, terminal_id = wallet_id, pin = encryptedPin!!, unit = "GOTV",pfm = pfm)
                val response = DstvService.create().pay(payDetails).await()
                val jsonResponse = Gson().toJsonTree(response).asJsonObject
                val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                val response1 = gson.fromJson(jsonResponse.toString(), EnergyModel.PhPayFailedResponse::class.java)

                if (response1.error == "true") {3

                    launch(UI) {
                        mProgressDialog.dismiss()
                        alert {
                            isCancelable = false
                            title = "Response"
                            message = response.message
                            positiveButton(buttonText = "Ok") {
                                moveToHome()
                            }
                        }.show()
                    }
                } else {
                    launch(UI) {
                        mProgressDialog.dismiss()
                        alert {
                            title = "GOTV Payment Purchase"
                            message = "Message : ${response.message} \n\nWould you want to save this name as a beneficiary?"
                            positiveButton(buttonText = "Yes") {
                                addToBeneficiaries(bouquet, iuc)
                                var receiptMap = hashMapOf<String, String>(
                                        "Terminal ID" to SharedPreferenceUtils.getTerminalId(this@Gotv),
                                        "Wallet ID" to wallet_id,
                                        "Ref" to response.ref,
                                        "Smart Card Number" to iuc
                                )
                                val receiptModel = ReceiptModel("", "GOTV", "", receiptMap, (amount).toString(), "APPROVED")

                                val intent = Intent(this@Gotv, PrintActivity::class.java)
                                intent.putExtra("print_map", receiptModel)
                                intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_VAS_TYPE, PrintActivity.VasType.GOTV)
                                startActivity(intent)
                                finish()
                            }

                            negativeButton(buttonText = "No") {
                                //                            moveToHome()
                                var receiptMap: HashMap<String, String>? = null
                                receiptMap = hashMapOf<String, String>(
                                        "Terminal ID" to SharedPreferenceUtils.getTerminalId(this@Gotv),
                                        "Wallet ID" to wallet_id,
                                        "Ref" to response.ref,
                                        "Smart Card Number" to iuc
                                )
                                val receiptModel = ReceiptModel("", "GOTV", "", receiptMap, (amount).toString(), "APPROVED")

                                val intent = Intent(this@Gotv, PrintActivity::class.java)
                                intent.putExtra("print_map", receiptModel)
                                intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_VAS_TYPE, PrintActivity.VasType.GOTV)
                                startActivity(intent)
                                finish()
                            }
                        }.show()
                    }
                }
            }
        }


    }

    private fun addToBeneficiaries(bouquet : Data, iuc : String) {
        val db = (application as App).beneficiariesDatabase
        val dstvBeneficiariesDao = db.getDstvBeneficiariesDao()

        val beneficiary = DstvBeneficiariesModel(dstvNumber = iuc, name = beneficiaryName)

        async {
            dstvBeneficiariesDao.insert(beneficiary)
            launch(UI){
                toast("Beneficiary successfully added")
//                startActivity(Intent(this@Gotv, CableTVActivity::class.java))
//                finish()
            }
        }
    }

    private fun moveToHome() {
        finish()
        val intent = Intent(this, CableTVActivity::class.java)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode){
            KEYS.PURCHASE_INTENT_CODE -> when (resultCode){
                Activity.RESULT_OK -> {
                    val deviceState = data!!.getSerializableExtra("state") as DeviceState
                    val rrn = data!!.getStringExtra("rrn")
//                    val isApproved = data?.getBooleanExtra("isApproved", false)
//                    toast(isApproved.toString())
//                    isApproved?.let {
//                        if (it){
//                            launch(CommonPool){
//                                launch(UI){
//                                    mProgressDialog.show()
//                                }
//                                val cardResponse = DstvService.Factory.create().payWithCard(payDetails).await()
//
//                                launch(UI){
//                                    mProgressDialog.dismiss()
//                                    val result = TransactionResult()
//                                    alert {
//                                        title = "DSTV Payment Purchase"
//                                        message = "Message : ${cardResponse.message} \nReference number : ${cardResponse.ref}"
//                                        okButton { moveToHome() }
//                                    }.show()
//                                }
//
//                            }
//                        } else {
//                            toast("Transaction declined")
//                        }
//                    }
                    when (deviceState) {
                        DeviceState.APPROVED -> {
                            mProgressDialog.show()
                            (application as App).db.transactionResultDao.get(rrn).observe({ lifecycle }) {
                                it?.let { transactionResult ->
                                    launch(CommonPool) {
                                        launch(UI) {
                                            mProgressDialog.show()
                                        }
                                        val cardResponse = DstvService.Factory.create().payWithCard(payDetails).await()
                                        val jsonResponse = Gson().toJsonTree(cardResponse).asJsonObject
                                        val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                                        val response1 = gson.fromJson(jsonResponse.toString(), EnergyModel.PhPayFailedResponse::class.java)

                                        if (response1.error == "true") {
                                            launch(UI) {
                                                alert {
                                                    title = "Response"
                                                    message = cardResponse.message
                                                    positiveButton(buttonText = "Ok") {
                                                        moveToHome()
                                                    }
                                                }.show()
                                            }
                                        } else {
                                            launch(UI) {
                                                mProgressDialog.dismiss()
                                                val result = TransactionResult()
                                                alert {
                                                    title = "GOTV Payment Purchase"
                                                    message = "Message : ${cardResponse.message}"
                                                    positiveButton(buttonText = "Print") {
                                                        val receiptMap = hashMapOf<String, String>(
                                                                "Terminal ID" to SharedPreferenceUtils.getTerminalId(this@Gotv),
                                                                "Wallet ID" to wallet_id,
                                                                "RRN" to transactionResult.RRN,
                                                                "Card PAN" to transactionResult.PAN,
                                                                "CardHolder" to transactionResult.cardHolderName,
                                                                "Card Expiry" to transactionResult.cardExpiry,
                                                                "Auth ID" to transactionResult.authID,
                                                                "MID" to transactionResult.merchantID,
                                                                "STAN" to transactionResult.STAN,
                                                                "Ref" to cardResponse.ref,
                                                                "Smart Card Number" to smartCardNo

                                                        )

                                                        val receiptModel = ReceiptModel("", "GOTV", "", receiptMap, (amount).toString(), "APPROVED")

                                                        val intent = Intent(this@Gotv, PrintActivity::class.java)
                                                        intent.putExtra("print_map", receiptModel)
                                                        intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_VAS_TYPE, PrintActivity.VasType.GOTV)
                                                        startActivity(intent)

                                                        finish()
                                                    }
                                                }.show()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        DeviceState.DECLINED, DeviceState.FAILED -> {
//                            alert {
//                                title = "Transaction Failed"
//                                message = "Purchase transaction failed. Please try again"
//                            }.show()
                            (application as App).db.transactionResultDao.get(rrn).observe({ lifecycle }) {
                                it?.let { transactionResult ->
                                    launch(UI) {
                                        alert {
                                            title = "Response"
                                            message = "Purchase transaction failed. Please try again"
                                            positiveButton(buttonText = "Print") {
                                                val receiptMap = hashMapOf<String, String>(
                                                        "Terminal ID" to SharedPreferenceUtils.getTerminalId(this@Gotv),
                                                        "Wallet ID" to wallet_id,
                                                        "RRN" to transactionResult.RRN,
                                                        "Card PAN" to transactionResult.PAN,
                                                        "CardHolder" to transactionResult.cardHolderName,
                                                        "Card Expiry" to transactionResult.cardExpiry,
                                                        "Auth ID" to transactionResult.authID,
                                                        "MID" to transactionResult.merchantID,
                                                        "STAN" to transactionResult.STAN

                                                )

                                                val receiptModel = ReceiptModel("", "GOTV", "", receiptMap, (amount).toString(), "Declined")

                                                val intent = Intent(this@Gotv, PrintActivity::class.java)
                                                intent.putExtra("print_map", receiptModel)
                                                intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_VAS_TYPE, PrintActivity.VasType.GOTV)
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
        }
    }


    object KEYS {
        const val PURCHASE_INTENT_CODE = 23

    }
}
