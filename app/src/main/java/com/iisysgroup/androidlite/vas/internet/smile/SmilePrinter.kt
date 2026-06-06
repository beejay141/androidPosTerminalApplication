package com.iisysgroup.androidlite.vas.internet.smile

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.iisysgroup.androidlite.App
import com.itex.richard.payviceconnect.model.SmileModel

class SmilePrinter : AppCompatActivity() {

    enum class SMILE_TRANSACTION_TYPE {
        CASH_APPROVED, CASH_DECLINED, CARD_APPROVED
    }

    private val smileSuccessBundle by lazy {
        intent.getSerializableExtra("smile_response") as SmileModel.SmileSuccessResponse
    }

    private val smileLookupDetails by lazy {
        intent.getSerializableExtra("smile_lookup") as SmileModel.SmileValidateCustomerResponse
    }


    private val transactionType by lazy {
        intent.getSerializableExtra("transaction_type") as SMILE_TRANSACTION_TYPE
    }

    private val smileData by lazy {
        intent.getParcelableExtra("smile_extra") as SmileModel.Bundle
    }

    private val accountId by lazy {
        intent.getStringExtra("accountId")
    }

    private val rrn by lazy {
        intent.getStringExtra("rrn")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        (application as App).db.transactionResultDao.get(rrn).observe({lifecycle}){
            val view = when (transactionType){
                SMILE_TRANSACTION_TYPE.CASH_DECLINED -> {
                    SmileReceiptGenerator.generateCashDeclined(this, smileLookupDetails, smileData, accountId)
                }

                SMILE_TRANSACTION_TYPE.CASH_APPROVED -> {
                    SmileReceiptGenerator.generateCashDeclined(this, smileLookupDetails, smileData, accountId)
                }

                SMILE_TRANSACTION_TYPE.CARD_APPROVED -> {
                        SmileReceiptGenerator.generateCardSuccessful(this, it!!, smileLookupDetails, smileData, accountId)
                }
            }

            setContentView(view)

        }
    }
}
