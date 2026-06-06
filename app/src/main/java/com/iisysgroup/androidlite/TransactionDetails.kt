package com.iisysgroup.androidlite

//import AmpEmvL2Android.AMPDevice
import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.iisysgroup.androidlite.utils.PrintUtils
import com.iisysgroup.androidlite.utils.TimeUtils
import com.iisysgroup.poslib.deviceinterface.interactors.EmvInteractor
import com.iisysgroup.poslib.deviceinterface.interactors.PrinterInteractor
import com.iisysgroup.poslib.host.entities.TransactionResult
import com.iisysgroup.poslib.utils.Utilities
import com.pax.PaxDevice
import kotlinx.android.synthetic.main.activity_transaction_details.*
import kotlinx.android.synthetic.main.content_transaction_details.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton


class TransactionDetails : AppCompatActivity(), View.OnClickListener {
    var transactionResult: TransactionResult? = null

    val device by lazy {
        PaxDevice(this)
    }

    val emvInteractor by lazy {
        EmvInteractor.getInstance(device)
    }


    val printer by lazy {
        PrinterInteractor.getInstance(device)
    }

    val db by lazy {

        (application as App).db
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_details)




        if (intent.hasExtra(Transactions.TRANSACTIONS_DETAILS_KEY)) {
            val RRN = intent.getStringExtra(Transactions.TRANSACTIONS_DETAILS_KEY)
            Thread(Runnable {
                db.transactionResultDao.get(RRN).observe(this@TransactionDetails, Observer { result ->
                    transactionResult = result


                    transactionResult?.let { result ->
                        if (result.isApproved){
                            fab.visibility = View.VISIBLE
                        }
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


                    }
                })
            }).start()
        } else {
            //todo show error
        }
        print_transaction.setOnClickListener(this)
        finish_transaction.setOnClickListener(this)
    }

    fun refund(transactionResult: TransactionResult){
        //val intent = Intent(this, )
    }

    fun revert(transactionResult: TransactionResult){

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.print_transaction -> {
                transactionResult?.let {result ->
                    PrintUtils.generatePrintableForCustomer(result, printer, this)

                    alert {
                        title = "Generate Merchant's copy"
                        message = "Click to O.K to print Merchant's copy"
                        okButton { PrintUtils.generatePrintableForMerchant(result, printer, this@TransactionDetails) }
                    }.show()
                }
            }
            R.id.finishButton -> {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
}
