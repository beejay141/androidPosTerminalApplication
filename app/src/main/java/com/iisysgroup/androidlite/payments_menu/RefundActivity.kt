package com.iisysgroup.androidlite.payments_menu

//import AmpEmvL2Android.AMPDevice
import android.arch.lifecycle.Observer
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import com.iisysgroup.androidlite.App
import com.iisysgroup.androidlite.PrintActivity
import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.Transactions
import com.iisysgroup.androidlite.history_summary.TransactionHistory
import com.iisysgroup.androidlite.models.ReceiptModel
import com.iisysgroup.androidlite.utils.PrintUtils
import com.iisysgroup.androidlite.utils.TimeUtils
import com.iisysgroup.poslib.ISO.common.Constants.IsoTransactionType.REFUND
import com.iisysgroup.poslib.ISO.common.Constants.IsoTransactionType.REVERSAL
import com.iisysgroup.poslib.deviceinterface.interactors.EmvInteractor
import com.iisysgroup.poslib.deviceinterface.interactors.PrinterInteractor
import com.iisysgroup.poslib.host.Host
import com.iisysgroup.poslib.host.entities.TransactionResult
import com.iisysgroup.poslib.utils.AccountType
import com.iisysgroup.poslib.utils.Utilities
import com.pax.PaxDevice
import kotlinx.android.synthetic.main.activity_refund.*
import kotlinx.android.synthetic.main.content_transaction_details.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.anko.alert

class RefundActivity : BasePaymentActivity() {
    var TAG = javaClass.simpleName
    var transactionAmount: Long? = null
    lateinit var transactionType: Host.TransactionType
    var transactionResult: TransactionResult? = null
    var l_long_amount: Long = 0
    lateinit var accountType: AccountType
    lateinit var status: String
    lateinit var mRefund: Button
    lateinit var mReverse: Button
    lateinit var refund_search_btn: Button
    lateinit var moveToRefund: Button
    lateinit var moveToReverse: Button
    lateinit var coordinatorLayout: CoordinatorLayout
    lateinit var RRN: EditText
    lateinit var application: App

    var shouldPrinterShow = false

    private val device by lazy {
        PaxDevice(this)
    }

    private val emvInteractor by lazy {
      EmvInteractor.getInstance(device)
    }

    private val printerInteractor by lazy {
        PrinterInteractor.getInstance(device)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.let {
            menuInflater.inflate(R.menu.refund_activity, menu)
            menu.findItem(R.id.print).isVisible = shouldPrinterShow
            return true
        }
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {

            R.id.print -> {
                printTransaction()
                return true
            }

            android.R.id.home -> {
                onBackPressed()
                return false
            }

            else -> {}
        }

        return false
    }

    private fun printTransaction() {


        transactionResult?.let { transactionResult ->
            val map = HashMap<String, String>()
            map.put("RRN", transactionResult.RRN)
            map.put("STAN", transactionResult.STAN)
            map.put("MID", transactionResult.merchantID)
            map.put("Card PAN", transactionResult.PAN)
            map.put("Card Holder", transactionResult.cardHolderName)
            map.put("Card Expiry", transactionResult.cardExpiry)
            map.put("Auth ID", transactionResult.authID)
            map.put("Account Type", transactionResult.accountType)
            map.put("Terminal ID", transactionResult.terminalID)

            val convertedAmount = (transactionResult.amount.toDouble()/100).toString()

            val receiptModel = ReceiptModel(TimeUtils.convertLongToString(transactionResult.longDateTime), transactionResult.transactionType.toString(), transactionResult.transactionStatus, map, convertedAmount, transactionResult.transactionStatusReason)

            val intent = Intent(this@RefundActivity, PrintActivity::class.java)
            intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_MODEL_KEY, receiptModel)
            intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_VAS_TYPE, PrintActivity.VasType.PURCHASE)

            //Optional - the reason you're passing in the terminal ID is because you want to create a receipt for the Vas terminal ID which is not what is stored in the Shared Preferences. If you do not send in this terminal ID, the receipt would print the terminal ID receipt for the Device and not the one for Itex.
            intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_TERMINAL_ID, transactionResult.terminalID)

            alert {
                title = "Receipt reprint"
                message = "Whose receipt would you want to print?"
                positiveButton(buttonText = "Customer"){
                    intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_RECEIPT_OWNER, "Customer's receipt reprint")
                    startActivity(intent)
                }

                negativeButton(buttonText = "Merchant"){
                    intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_RECEIPT_OWNER, "Merchant's receipt reprint")
                    startActivity(intent)
                }
            }.show()



        }
    }

    fun getBitmapFromView(view: View): Bitmap {
        //Define a bitmap with the same size as the view
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        //Bind a canvas to it
        val canvas = Canvas(returnedBitmap)
        //Get the view's background
        val bgDrawable = view.background
        if (bgDrawable != null)
        //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas)
        else
        //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE)
        // draw the view on the canvas
        view.draw(canvas)
        //return the bitmap
        return returnedBitmap
    }

    internal override fun getMaxCount(): Int {
        return 8
    }

    fun getRRN() : String ?{
        if (intent.hasExtra(BasePaymentActivity.TRANSACTION_RRN)){
            val rrn = intent.getStringExtra(BasePaymentActivity.TRANSACTION_RRN)
           return rrn
        }
        return null
    }

    internal override fun getTextLayoutId(): Int {
        return R.id.txtAmount
    }

    internal override fun getNumberOfPages(): Int {
        return 4
    }

    internal override fun getPageLayout(): Int {
        return BasePaymentActivity.INT_REFUND_REVERSAL
    }

    internal override fun onEnterPressed() {
        coordinatorLayout.tag = 4
        showVisibility(findViewById(R.id.account_select_refund))
    }

    internal fun getAccountType(): AccountType {
        val radioGroup = findViewById<RadioGroup>(R.id.radio_payments_account_group)
        val text_amt = pageTitle.text.toString().replace(".", "")
        l_long_amount = java.lang.Long.parseLong(text_amt)

        when (radioGroup.checkedRadioButtonId) {
            R.id.radio_default -> accountType = AccountType.DEFAULT_UNSPECIFIED
            R.id.radio_credit -> accountType = AccountType.CREDIT
            R.id.radio_savings -> accountType = AccountType.SAVINGS
            R.id.radio_current -> accountType = AccountType.CURRENT
            else -> accountType = AccountType.DEFAULT_UNSPECIFIED
        }
        return accountType
    }

    fun processRRN(rrn_ : String) = runBlocking {
        Thread(Runnable {
            //rrn_ is the string. rrn is the TextView reference
            application.db.transactionResultDao.get(rrn_).observe(this@RefundActivity, Observer { result ->
                if (result == null) {
                    Toast.makeText(this@RefundActivity, "No RRN matched", Toast.LENGTH_LONG).show()
                    return@Observer
                }
                showVisibility(findViewById(R.id.refund_details))
                shouldPrinterShow = true
                invalidateOptionsMenu()
                transactionResult = result
                transaction_date.text = TimeUtils.convertLongToString(result.longDateTime)
                transaction_type.text = result.transactionType.toString()
                transaction_status.text = result.transactionStatus
                rrn.text = result.RRN
                name.text = result.cardHolderName
                account_type.text = result.accountType
                card_expiry.text = result.cardExpiry
                mid.text = result.merchantID
                aid.text = result.authID
                amount.text = Utilities.parseLongIntoNairaKoboString(result.amount)
                transactionAmount = result.amount
                status = result.transactionStatus
                coordinatorLayout.tag = 1
            })
        }).start()
    }

    /*fun screenShot(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width,
                view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        coordinatorLayout = findViewById(R.id.coordinator_layout)
        coordinatorLayout.tag = 0
        application = getApplication() as App
        RRN = findViewById(R.id.searchRRN)
        mReverse = findViewById(R.id.submit_button)
        mRefund = findViewById(R.id.refund_submit_button)

        //Todo Turn this visibility on after customization for eko bank


        refund_search_btn = findViewById(R.id.refund_search_button)
        moveToRefund = findViewById(R.id.refund)




        getRRN()?.let {
            processRRN(it)
        }?: refund_search_btn.setOnClickListener(View.OnClickListener {
            val rrn_ = RRN.text.toString()
            if (rrn_ == null || rrn_.length < 12) {
                RRN.error = "Enter a valid RRN number please"
                return@OnClickListener
            }
            processRRN(rrn_)
        })


        mReverse.setOnClickListener {
            getAccountType()
            shouldPrinterShow = false
            invalidateOptionsMenu()
            transactionType = Host.TransactionType.REVERSAL
            coordinatorLayout.tag = 2
            if (!status.equals("approved", ignoreCase = true)) {
                Toast.makeText(this@RefundActivity, "Refund can not be done on a declined transaction", Toast.LENGTH_LONG).show()
            }

            reverse(accountType)
        }

        mRefund.setOnClickListener {
            shouldPrinterShow = false
            invalidateOptionsMenu()
            coordinatorLayout.tag = 3
            getAccountType()
            transactionType = Host.TransactionType.REFUND
            refund(l_long_amount, accountType)
        }

        moveToReverse = findViewById(R.id.reversal)
        findViewById<View>(R.id.search_refund).visibility = View.VISIBLE

        moveToRefund.setOnClickListener {
            shouldPrinterShow = true
            invalidateOptionsMenu()

            coordinatorLayout.tag = 2
            showVisibility(findViewById(R.id.enter_amount))
            transactionType = Host.TransactionType.REFUND
        }

        moveToReverse.setOnClickListener {
            shouldPrinterShow = true
            invalidateOptionsMenu()

            coordinatorLayout.tag = 3
            showVisibility(findViewById(R.id.account_select_reversal))
            transactionType = Host.TransactionType.REVERSAL
        }


        Log.d("OkH", "Transaction Details")

        launch(CommonPool){

            Log.d("OkH", "${coordinatorLayout.width} and height is ${coordinatorLayout.height}")
           /* val bitmap = screenShot(window.decorView.rootView)*/
            //PrintUtils.printBitmap(bitmap, true)
    }
    }


    override fun onBackPressed() {
        when (coordinatorLayout.tag.toString()) {
        // we use a composite layout here - one activity, about 5 layouts. Each layout has a tag associated to it in the order it should be shown. The first layout to be shown is 0, the next one is 1 etc. Here I override onBackPressed to properly handle the flow backwards when the user clicks on back
            "0" -> {
                val intent = Intent(this@RefundActivity, PaymentsActivity::class.java)
                startActivity(intent)
                finish()
                showVisibility(findViewById(R.id.search_refund))
                coordinatorLayout.tag = 0
            }
            "1" -> {
                getRRN()?.let {
                    startActivity(Intent(this, TransactionHistory::class.java))
                    finish()
                } ?: showVisibility(findViewById(R.id.search_refund))

                coordinatorLayout.tag = 0
            }
            "2" -> {
                showVisibility(findViewById(R.id.refund_details))
                shouldPrinterShow = true
                invalidateOptionsMenu()
                coordinatorLayout.tag = 1
            }
            "3" -> if (transactionType == Host.TransactionType.REFUND) {
                shouldPrinterShow = false
                invalidateOptionsMenu()
                showVisibility(findViewById(R.id.enter_amount))
                coordinatorLayout.tag = 2
            } else if (transactionType == Host.TransactionType.REVERSAL) {
                shouldPrinterShow = false
                invalidateOptionsMenu()
                showVisibility(findViewById(R.id.refund_details))
                coordinatorLayout.tag = 1
            }
        }
    }

    internal override fun onAccountTypeSet(account_type: AccountType) {
        this.accountType = account_type

        if (transactionType == Host.TransactionType.REFUND) {
            refund(l_long_amount, account_type)
            return
        }

        if (transactionType == Host.TransactionType.REVERSAL) {
            reverse(account_type)
            return
        }

        refund(l_long_amount, account_type)
    }

    private fun reverse(accountType: AccountType) {
        val intent = Intent(this, TransactionProcessActivity::class.java)
        intent.putExtra(BasePaymentActivity.TRANSACTION_ACCOUNT_TYPE, accountType)
        intent.putExtra(BasePaymentActivity.TRANSACTION_TYPE, REVERSAL)
        intent.putExtra(BasePaymentActivity.TRANSACTION_AMOUNT, transactionAmount)


        intent.putExtra(BasePaymentActivity.TRANSACTION_RRN, RRN.text.toString() )
        startActivity(intent)
    }

    fun refund(amount: Long, accountType: AccountType?) {
        if (amount == 0L || accountType == null) {
            Toast.makeText(this, "Amount must be valid and Account Type must be selected", Toast.LENGTH_LONG).show()
            return
        }

        transactionAmount?.let {
            if (l_long_amount > it) {
                Toast.makeText(this, "Refund amount can not be greater than original amount", Toast.LENGTH_LONG).show()
                return
            }
        }

        val rrn = RRN.text.toString()

        val intent = Intent(this, TransactionProcessActivity::class.java)
        intent.putExtra(BasePaymentActivity.TRANSACTION_ACCOUNT_TYPE, accountType)
        intent.putExtra(BasePaymentActivity.TRANSACTION_AMOUNT, amount)
        intent.putExtra(BasePaymentActivity.TRANSACTION_TYPE, REFUND)
        Toast.makeText(this, rrn, Toast.LENGTH_LONG).show()
        intent.putExtra(BasePaymentActivity.TRANSACTION_RRN, rrn)
        startActivity(intent)

    }
}