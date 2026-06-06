package com.iisysgroup.androidlite.vas.activity.energy.Eko

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.iisysgroup.androidlite.App
import com.iisysgroup.androidlite.models.BaseReceiptDetails
import com.iisysgroup.androidlite.utils.PrintUtils

class EkoPrinter : AppCompatActivity() {

    enum class EKEDC_RECEIPT_TYPE {
        CASH_SUCCESSFUL, CASH_DECLINED, CARD_SUCCESSFUL, CARD_DECLINED
    }

    private val successfulResponse by lazy {
        intent.getParcelableExtra("values") as EkoModel.EkoPaySuccessResponse
    }

    private val declinedResponse by lazy {
        intent.getParcelableExtra("values") as EkoModel.EkoPayFailedResponse
    }

    private val ekedcType by lazy {
        intent.getSerializableExtra("ekedc_type") as EKEDC_RECEIPT_TYPE
    }


    private val baseReceiptDetails by lazy {
        intent.getParcelableExtra("extras") as BaseReceiptDetails
    }

    private val rrn by lazy {
        intent.getStringExtra("rrn")
    }

    private val lookupResult by lazy {
        intent.getParcelableExtra("lookupDetails") as EkoModel.EkoLookUpSuccessResponse
    }

    private val transactionAmount by lazy {
        intent.getStringExtra("transaction_amount")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


       (application as App).db.transactionResultDao.get(rrn).observe({lifecycle}){
           val view = when (ekedcType) {
               EKEDC_RECEIPT_TYPE.CARD_DECLINED -> {
                   EkoReceiptGenerator.generateCardDeclinedReceipt(this@EkoPrinter, declinedResponse, lookupResult, baseReceiptDetails, it!!)
               }
               EKEDC_RECEIPT_TYPE.CARD_SUCCESSFUL -> {
                    EkoReceiptGenerator.generateCardSuccessfulReceipt(this@EkoPrinter, successfulResponse, baseReceiptDetails, it!!)
               }
               EKEDC_RECEIPT_TYPE.CASH_DECLINED -> {
                   EkoReceiptGenerator.generateCashDeclinedReceipt(this@EkoPrinter, declinedResponse, lookupResult, baseReceiptDetails, amount = transactionAmount)
               }
               EKEDC_RECEIPT_TYPE.CASH_SUCCESSFUL -> {
                   EkoReceiptGenerator.generateCashSuccessfulReceipt(this@EkoPrinter, successfulResponse  , baseReceiptDetails)
               }

           }



           setContentView(view)

           view.post {

               PrintUtils.printBitmap(PrintUtils.getBitmapFromView(view), true)
           }
       }



    }


}
