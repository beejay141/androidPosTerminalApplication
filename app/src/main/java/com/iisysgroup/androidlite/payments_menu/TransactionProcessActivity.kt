package com.iisysgroup.androidlite.payments_menu

//import AmpEmvL2Android.AMPDevice
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.Transformations
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.iisysgroup.androidlite.App
import com.iisysgroup.androidlite.MainActivity
import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.payments_menu.handlers.Balance
import com.iisysgroup.androidlite.payments_menu.handlers.Purchase
import com.iisysgroup.androidlite.payments_menu.handlers.Refund
import com.iisysgroup.androidlite.payments_menu.handlers.Revert
import com.iisysgroup.androidlite.utils.PrintUtils
import com.iisysgroup.poslib.commons.emv.EmvTransactionType
import com.iisysgroup.poslib.deviceinterface.DeviceState
import com.iisysgroup.poslib.deviceinterface.interactors.EmvInteractor
import com.iisysgroup.poslib.deviceinterface.interactors.PrinterInteractor
import com.iisysgroup.poslib.host.Host
import com.iisysgroup.poslib.host.entities.ConnectionData
import com.iisysgroup.poslib.host.entities.TransactionResult
import com.iisysgroup.poslib.utils.AccountType
import com.iisysgroup.poslib.utils.InputData
import com.iisysgroup.poslib.utils.TransactionData
import com.iisysgroup.poslib.utils.Utilities.parseLongIntoNairaKoboString
import com.pax.PaxDevice
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.insert_card.*
import kotlinx.android.synthetic.main.transaction_status.*
import kotlinx.android.synthetic.main.view_wallet_enter.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.anko.alert
import org.jetbrains.anko.cancelButton
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.okButton
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@SuppressLint("MissingPermission")
class TransactionProcessActivity : AppCompatActivity(), Callback<Any>{

    override fun onFailure(call: Call<Any>?, t: Throwable?) {
        Log.d("Payvice lookup Error", t.toString())
    }

    override fun onResponse(call: Call<Any>?, response: Response<Any>?) {
        Log.d("Payvice lookup Response", response?.body().toString())
    }

    private val printerInteractor by lazy {
        PrinterInteractor.getInstance(ampDevice)
    }

    private val TAG = "TransactionProcess"

    private val isCard by lazy {
        intent.getBooleanExtra(BasePaymentActivity.PROCESSING_CARD_OR_WALLET, false)
    }

    private var currentTransactionResult: TransactionResult? = null

    private fun SharedPreferences.isSSL(): Boolean{
        return when (this.getString(getString(R.string.key_pref_port_type), "").toLowerCase()){
            "open" -> false
            else -> true
        }
    }

    private fun displayInfo(message: String, title: String? = null) {
        alert.setTitle(title)
        alert.setMessage(message)
        alert.show()
    }


    private val additionalTransactionType by lazy {
        intent.getSerializableExtra(BasePaymentActivity.ADDITIONAL_TRANSACTION_TYPE) as Host.TransactionType
    }

    private val alert by lazy {
        AlertDialog.Builder(this)
                .setTitle(null)
                .setMessage(null)
                .create()
    }

    private val terminalId by lazy {
        PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.key_terminal_id), "")
    }

    private val payviceUserNameAlertDialog by lazy {
        val v = LayoutInflater.from(this).inflate(R.layout.view_wallet_enter, null, false)
        AlertDialog.Builder(this).setPositiveButton("Enter", {_, _ ->  processPassword()} ).setView(v).setTitle("Enter the password for $payviceUsername").create() as AlertDialog
    }

    private val ampDevice by lazy {
        PaxDevice(this)
    }

    private val device by lazy {
        EmvInteractor.getInstance(ampDevice)
    }

    private val hostInteractor by lazy {
        (application as App).hostInteractor
    }

    private val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }
    private val db by lazy {
        (application as App).db
    }
    private val progressDialog by lazy {
        ProgressDialog(this).apply {
            setCancelable(false)
        }
    }
    private val connectionData by lazy {
        val terminal_id = sharedPreferences.getString(getString(R.string.key_terminal_id), null)
        val ip_address = sharedPreferences.getString(getString(R.string.key_ip_address), null)
        val ip_port = Integer.parseInt(sharedPreferences.getString(getString(R.string.key_pref_port), null))
        val isSSL = sharedPreferences.isSSL()

        ConnectionData(terminal_id, ip_address, ip_port, isSSL)
    }

    private val transactionType: Host.TransactionType? by lazy {
        if (!intent.hasExtra(BasePaymentActivity.TRANSACTION_TYPE)) {
            Toast.makeText(this, "Transaction type is not specified", Toast.LENGTH_LONG).show()
        }

        intent.getSerializableExtra(BasePaymentActivity.TRANSACTION_TYPE) as Host.TransactionType
    }

    private val accountNumber by lazy {
       intent.getStringExtra(BasePaymentActivity.TRANSACTION_ACCOUNT_NUMBER)
    }

    private val bankCode by lazy {
        //todo sort this out well
        intent.getSerializableExtra(BasePaymentActivity.TRANSACTION_BANK_NAME)
    }

    private val accountType by lazy {
        intent.getSerializableExtra(BasePaymentActivity.TRANSACTION_ACCOUNT_TYPE) as AccountType
    }

    private val amount by lazy {
        intent.getLongExtra(BasePaymentActivity.TRANSACTION_AMOUNT, 0)
    }

    private val additionalAmount by lazy {
        intent.getLongExtra(BasePaymentActivity.TRANSACTION_ADDITIONAL_AMOUNT, 0)
    }

    private val inputData by lazy {
        InputData(amount, additionalAmount, accountType)
    }

    private val keyHolder by lazy {
        db.keyHolderDao.get()
    }

    private val configData by lazy {
        db.configDataDao.get()
    }

    private val payviceUsername by lazy {
        PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.key_payvice_username), "")
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_process)

        transactionType?.let {

            when (it) {
                Host.TransactionType.PURCHASE -> {
                    if (intent.getSerializableExtra(BasePaymentActivity.ADDITIONAL_TRANSACTION_TYPE) == Host.TransactionType.FUND_TRANSFER){
                       performTransfer()
                    } else {
                        performPurchase()
                    }
                }
                Host.TransactionType.BILL_PAYMENT -> performBillPayment()
                Host.TransactionType.PURCHASE_WITH_CASH_ADVANCE -> performCashAdvance()
                Host.TransactionType.PURCHASE_WITH_CASH_BACK -> performCashBack()
                Host.TransactionType.BALANCE_INQUIRY -> checkBalance()
                Host.TransactionType.REFUND -> performRefund()
                Host.TransactionType.REVERSAL -> performReversal()
                else -> {}
            }
        } ?: kotlin.run {
            finishButton.setOnClickListener {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        device.observeStatus()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({status ->
                    when (status.state) {
                        DeviceState.INSERT_CARD -> {
                            handleInsertCard()
                        }

                        DeviceState.FAILED -> {
                            handleStatusDisplay(false)
                            transactionStatusText.text = "Transaction Canceled"
                            print_transaction.visibility = View.GONE

                            currentTransactionResult?.let {
                                //If device fails transaction offline after the transaction had been sent to the remote host
                                //then the transaction needs to be rolled back.
                                handleRollBack(it)
                            }
                        }

                        DeviceState.DECLINED -> {
                            handleStatusDisplay(false)
                            currentTransactionResult?.let {
                                if(it.isApproved){
                                    //If device declines an host-approved transaction,
                                    // then the transaction needs a roll back
                                    handleRollBack(it)
                                }else{
                                    it.let(this::handlePrinting)
                                }
                            }
                        }

                        DeviceState.APPROVED -> {
                            handleStatusDisplay(true)
                            if (intent.hasExtra(BasePaymentActivity.ADDITIONAL_TRANSACTION_TYPE) && intent.getSerializableExtra(BasePaymentActivity.ADDITIONAL_TRANSACTION_TYPE) == Host.TransactionType.FUND_TRANSFER){
                                //todo handle the push to the new APIs
                            }
                            currentTransactionResult?.let(this::handlePrinting)
                        }

                        DeviceState.PROCESSING, DeviceState.AWAITING_ONLINE_RESPONSE -> {
                            alert.dismiss()
                            progressDialog.setMessage(status.message)
                            progressDialog.show()
                        }

                        else -> {
                            alert.dismiss()
                            displayInfo(status.message)
                            progressDialog.dismiss()
                        }
                    }

                },{onError -> Log.d("Error", onError.message)} )

    }

    private fun performBillPayment() {
        performPurchase()
    }

    private fun showVisibility(view: View) {
        val layout_ids = intArrayOf(R.id.enter_amount, R.id.account_select_reversal,
                R.id.insert_card, R.id.search_refund, R.id.refund_details,
                R.id.account_select_refund, R.id.account_select, R.id.purchase_account_select, R.id.transaction_status_layout)

        if (view.visibility == View.VISIBLE) {
            return
        }

        for (ids in layout_ids) {
            if ((findViewById<View>(ids)) != null && ids != view.id)
                findViewById<View>(ids).visibility = View.GONE
        }

        view.visibility = View.VISIBLE
    }

    private fun initializeApproveDeclinedState() {
        progressDialog.dismiss()
        alert.dismiss()
        doAsync {
            db.transactionResultDao.save(currentTransactionResult)
        }

    }

    private fun handleInsertCard(){
        showVisibility(findViewById(R.id.insert_card))

        if(transactionType != Host.TransactionType.BALANCE_INQUIRY){
            transactionAmountText.text = "${parseLongIntoNairaKoboString(amount)}"
        }else {
            transactionAmountText.visibility = View.GONE
        }

        if (intent.hasExtra(BasePaymentActivity.ADDITIONAL_TRANSACTION_TYPE) && intent.getSerializableExtra(BasePaymentActivity.ADDITIONAL_TRANSACTION_TYPE)== Host.TransactionType.FUND_TRANSFER){
            transactionTypeText.text = "Funds Transfer"
        } else {
            transactionTypeText.text =  transactionType?.name?.replace("_"," ")
        }
        alert.dismiss()
    }

    private fun handleStatusDisplay(isApproved: Boolean){
        initializeApproveDeclinedState()
        showVisibility(findViewById(R.id.transaction_status_layout))

        if(isApproved){
            transactionStatusText.text = getString(R.string.state_transaction_approved)
            transactionStatusImage.setImageDrawable(getDrawable(R.drawable.transaction_approved))
        }else{
            transactionStatusText.text = getString(R.string.state_transaction_declined)
            transactionStatusImage.setImageDrawable(getDrawable(R.drawable.transaction_declined))
        }

        finishButton.setOnClickListener {
            finish()
        }
    }

    fun performTransfer(){
        //showVisibility(findViewById(R.id.insert_card))

        if(transactionType != Host.TransactionType.BALANCE_INQUIRY){
            transactionAmountText.text = "${parseLongIntoNairaKoboString(amount)}"
        } else {
            transactionAmountText.visibility = View.GONE
        }

        //todo show dialogBox asking user to enter password

        if (isCard)
            handleCardTransfer()
        else
            handleWalletTransfer()

    }

    private fun lookup(accountNumber : String, bank_code : String){
        val bankCode = bank_code.substring(0, 3)
        /*TransferService.create().lookUpAccountNumber(terminalID = terminalId, toAccount = accountNumber, bankCode = bankCode).enqueue(this)*/
    }



    private fun handleCardTransfer() = runBlocking{

        performPurchase()
    }

    fun processPassword(){
        val password = payviceUserNameAlertDialog.et_payvice_password.text.toString()

        //todo show processing and show the user's details for client to confirm.
        //todo if user confirms, debit user's wallets and then debit our position with GT and credit client
        if (terminalId.isNullOrEmpty())
        {
            return
        }

        lookup(accountNumber = accountNumber, bank_code = accountNumber)
    }

    private fun handleWalletTransfer() {
        progressDialog.setMessage("Processing wallet transfer")
        progressDialog.show()
    }

    private fun handlePrinting(result: TransactionResult){
        tranStatusReasonText.text = result.transactionStatusReason
        PrintUtils.generatePrintableForCustomer(result, printerInteractor, this@TransactionProcessActivity)

        alert {
            title = "Print Merchant Copy?"
            isCancelable = false
            message = "Press OK to print merchant's copy"
            okButton {  PrintUtils.generatePrintableForMerchant(result, printerInteractor, this@TransactionProcessActivity) }
            cancelButton {  }
        }.show()
    }

    private fun handleRollBack(lastTransactionResult: TransactionResult){

        when(transactionType){
            null,
            Host.TransactionType.BALANCE_INQUIRY,
            Host.TransactionType.REVERSAL,
            Host.TransactionType.REFUND -> return
            else -> ""
        };
        progressDialog.setMessage("Rolling back transaction")
        progressDialog.show()

        Log.d(TAG,"Rolling back")
        hostInteractor.rollBackTransaction().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { transactionResult , throwable ->
                    transactionResult?.let {
                        Log.d(TAG, "Offline Failed transaction roll back status:  " + it.transactionStatus)
                        Log.d(TAG, it.toString())

                        progressDialog.dismiss()

                        if(lastTransactionResult.isApproved && !it.isRolledBack){
                            alert {
                                title = "Rollback Failed"
                                message = "Could not roll back transaction"
                                isCancelable = false
                                positiveButton("Retry"){
                                    handleRollBack(lastTransactionResult)
                                }
                                cancelButton {  }
                            }.show()
                        }else{
                            doAsync {
                                db.transactionResultDao.save(it)
                            }
                            handlePrinting(it)
                        }
                    }
                }

    }


    private fun performPurchase() {
        launch(CommonPool) {

            val purchase = Purchase(this@TransactionProcessActivity, db, inputData,
                    hostInteractor, connectionData, device)
            val cardLiveData = purchase.getTransactionResult()
            cardLiveData.observe({ lifecycle }) {
                it?.let {
                    /*val pfmJournal = PfmJournalGenerator(it).generateJournal()
                    val pfmState = PfmStateGenerator(this@TransactionProcessActivity, ampDevice).generateState()
                    Log.d("Pfm", pfmState.toString())
                    Log.d("Pfm", pfmJournal.toString())*/
                    currentTransactionResult = it

                    progressDialog.dismiss()
                    device.processOnlineResponse(it.responseCode, it.issuerAuthData91, it.issuerScript71, it.issuerScript72)


                    if (intent.getSerializableExtra(BasePaymentActivity.TRANSACTION_TYPE) == Host.TransactionType.BILL_PAYMENT){
                        //if transaction type is a bill payment - return a transaction status to the calling activity
                        val intent = Intent()
                        intent.putExtra("isApproved", it.isApproved)
                        setResult(Activity.RESULT_OK, intent)

                    }
                }
            }
        }
    }

    fun checkBalance() {
        launch(CommonPool){
            val balance = Balance(this@TransactionProcessActivity, db, hostInteractor,
                    connectionData, device, accountType)
            balance.getTransactionResult().observe({lifecycle}){
                progressDialog.dismiss()
                alert.dismiss()
                it?.let {
                    if(it.isApproved){
                        val balanceJson =  it.transactionStatusReason
                        val accountBalance =  JSONObject(balanceJson).getString(Host.KEY_BALANCE_ACCOUNT_BALANCE)
                        it.transactionStatusReason = "Account Balance: $accountBalance"
                    }
                    currentTransactionResult = it
                    device.processOnlineResponse(it.responseCode, it.issuerAuthData91, it.issuerScript71, it.issuerScript72)
                }
            }
        }
    }

    private fun performCashBack() {
        doAsync {
            val inputData = InputData(amount, additionalAmount, accountType)
            val cardLiveData = device.startEmvTransaction(inputData.amount, inputData.additionalAmount,
                    EmvTransactionType.EMV_CASHBACK)

            cardLiveData.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { emvCard, throwable ->
                        emvCard?.let {
                            val transactionData = TransactionData(inputData, it, configData, keyHolder)
                            val transactionLiveData = hostInteractor.getTransactionResult(Host.TransactionType.PURCHASE_WITH_CASH_BACK, connectionData, transactionData, null, null)
                            transactionLiveData.subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe { transactionResult, throwable ->
                                        transactionResult?.let {

                                        }
                                    }
                        }
                    }


        }
    }

    private fun performCashAdvance() {
        doAsync {
            val configData = db.configDataDao.get()
            val keyholder = db.keyHolderDao.get()

            if (amount == 0L || transactionType == null){
                Toast.makeText(this@TransactionProcessActivity, "Check out values", Toast.LENGTH_LONG).show()
                return@doAsync
            }












            val inputData = InputData(amount, 0, accountType)

            val cardObserver = device.startEmvTransaction(inputData.amount, inputData.additionalAmount, EmvTransactionType.EMV_PURCHASE)

            val liveData = LiveDataReactiveStreams.fromPublisher(cardObserver.toFlowable())


            Transformations.switchMap(liveData) {it ->
                val transactionData = TransactionData(inputData, it, configData, keyholder)

                val single = hostInteractor.getTransactionResult(Host.TransactionType.PURCHASE_WITH_CASH_ADVANCE, connectionData, transactionData, null, null)

                LiveDataReactiveStreams.fromPublisher(single.toFlowable()).observe({ lifecycle }) {

                    Log.d("Status", it?.transactionStatus)
                    Log.d("Status reason", it?.transactionStatusReason)
                    device.processOnlineResponse(it?.responseCode, it?.issuerAuthData91, it?.issuerScript71, it?.issuerScript72)
                }

                LiveDataReactiveStreams.fromPublisher(single.toFlowable())
            }


        }
    }

    private fun performReversal() {
        val rrn = intent.getStringExtra(BasePaymentActivity.TRANSACTION_RRN)
        doAsync {

            db.transactionResultDao.get(rrn).observe({lifecycle}){
                it?.let {
                    val revert = Revert(this@TransactionProcessActivity, db, inputData, hostInteractor, connectionData, device, rrn, it)
                    val transactionResult = revert.getTransactionResult()

                    transactionResult.observe({lifecycle}){
                        it?.let {
                            currentTransactionResult = it
                            progressDialog.dismiss()
                            device.processOnlineResponse(it.responseCode, it.issuerAuthData91, it.issuerScript71, it.issuerScript72)
                            Log.d("Status", it.transactionStatus)
                            Log.d("status reason", it.transactionStatusReason)
                        }
                    }
                }
            }
        }
    }

    private fun performRefund() {
        val rrn = intent.getStringExtra(BasePaymentActivity.TRANSACTION_RRN)
        val refundAmount = intent.getLongExtra(BasePaymentActivity.TRANSACTION_AMOUNT, 0L)
        if (refundAmount == 0L && rrn == null){
            Toast.makeText(this, "Enter a valid refund amount", Toast.LENGTH_LONG).show()
            return
        }
        doAsync {
            db.transactionResultDao.get(rrn).observe({lifecycle}){
                it?.let {
                    val refund = Refund(this@TransactionProcessActivity, db, inputData, hostInteractor, connectionData, device, rrn, it)
                    val transactionResult = refund.getTransactionResult()

                    transactionResult.observe({lifecycle}){
                        it?.let {
                            currentTransactionResult = it
                            progressDialog.dismiss()
                            device.processOnlineResponse(it.responseCode, it.issuerAuthData91, it.issuerScript71, it.issuerScript72)
                            Log.d("Status", it.transactionStatus)
                            Log.d("Status Reason", it.transactionStatusReason)
                        }
                    }
                }
            }
        }
    }
}






