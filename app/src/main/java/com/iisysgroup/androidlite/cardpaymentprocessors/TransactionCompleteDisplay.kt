package com.iisysgroup.androidlite.cardpaymentprocessors

//import AmpEmvL2Android.AMPDevice
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.iisysgroup.androidlite.App
import com.iisysgroup.androidlite.PrintActivity
import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.models.ReceiptModel
import com.iisysgroup.androidlite.utils.PrintUtils
import com.iisysgroup.androidlite.utils.SharedPreferenceUtils
import com.iisysgroup.androidlite.utils.StringUtils
import com.iisysgroup.androidlite.utils.TimeUtils
import com.iisysgroup.poslib.deviceinterface.DeviceState
import com.iisysgroup.poslib.deviceinterface.interactors.EmvInteractor
import com.iisysgroup.poslib.deviceinterface.interactors.PrinterInteractor
import com.iisysgroup.poslib.utils.Utilities
import com.pax.PaxDevice
import kotlinx.android.synthetic.main.transaction_status.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.cancelButton
import org.jetbrains.anko.okButton
import org.jetbrains.anko.toast
import java.util.*

//This class only gets called if the transaction is a card transaction. This is why the transaction can be retrieved from the database
class TransactionCompleteDisplay : AppCompatActivity() {

    private val db by lazy {
        (application as App).db
    }

    private val device by lazy {
        PaxDevice(this)
    }

    private val emvInteractor by lazy {
        EmvInteractor.getInstance(device)
    }

    private val printerInteractor by lazy {
        PrinterInteractor.getInstance(device)
    }

    private val transactionRef by lazy {
        intent.getStringExtra("rrn")
    }

    private val terminalId by lazy {
        SharedPreferenceUtils.getTerminalId(this)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_complete_display)

        val state = intent.getSerializableExtra("state") as DeviceState

        when (state) {
            DeviceState.DECLINED -> {
                toast("Declined")
                handleStatusDisplay(false)
                printTransaction()
            }

            DeviceState.APPROVED -> {
                toast("Approved")
                handleStatusDisplay(true)
                printTransaction()
            }

            DeviceState.FAILED -> {
                toast("Failed")
                handleStatusDisplay(false)
                //printTransaction()
            }

            DeviceState.REMOVE_CARD -> {
                toast("REMOVE CARD")
                handleStatusDisplay(false)
            }
            else -> {

            }
        }
    }

    private fun printTransaction() {
        db.transactionResultDao.get(transactionRef).observe({lifecycle}){ result ->


            result?.let {

                val intent = Intent(this@TransactionCompleteDisplay, PrintActivity::class.java)
                val map = HashMap<String, String>()

                val transactionType = "PURCHASE"
                val amount = result.amount
                val additionalAmount = result.additionalAmount
                val cardHolderName = result.cardHolderName
                val cardExpiry  = result.cardExpiry
                val merchantID = result.merchantID
                val authID = result.authID
                val terminalID = result.terminalID
                val PAN = result.PAN
                val RRN = result.RRN
                val responseCode = result.responseCode
                val STAN = result.STAN
                val accountType = result.accountType
                val transactionStatus = result.transactionStatus
                val calendar = Calendar.getInstance()
                val date = calendar.time
                val AID = result.AID
                val TVR = result.TVR
                val TSI = result.TSI

                map.put("ADDITIONAL AMOUNT", additionalAmount.toString())
                map.put("NAME", cardHolderName)
                map.put("EXPIRY DATE", cardExpiry)
                map.put("MID", merchantID)
                map.put("AID", AID)
                map.put("TERMINAL ID", terminalID)
                map.put("PAN", PAN)
                map.put("LABEL", responseCode)
                map.put("SEO NO", STAN)
                map.put("RRN", RRN)
                map.put("AUTH ID", authID)
                map.put("ACCOUNT TYPE", accountType)
                map.put("TSI", TSI)
                map.put("TVR", TVR)



                val convertedAmount = (amount.toDouble()/100).toString()

                val receiptModel = ReceiptModel(date.toString(), transactionType, transactionStatus, map, convertedAmount, result.transactionStatusReason)
                intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_VAS_TYPE, PrintActivity.VasType.PURCHASE)
                intent.putExtra("print_map", receiptModel)
                startActivity(intent)

            }
        }
    }

    private fun handleStatusDisplay(isApproved: Boolean){


        if(isApproved){
            transactionStatusText.text = getString(R.string.state_transaction_approved)
            transactionStatusImage.setImageDrawable(getDrawable(R.drawable.transaction_approved))
        }else{
            transactionStatusText.text = getString(R.string.state_transaction_declined)
            transactionStatusImage.setImageDrawable(getDrawable(R.drawable.transaction_declined))
        }

        alert {
            message = "Please remove your card"
        }.show()

        finishButton.setOnClickListener {
            finish()
        }
    }
}
