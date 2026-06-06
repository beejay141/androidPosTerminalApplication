package com.iisysgroup.androidlite.vas.internet.smile

import android.content.Context
import android.view.View
import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.utils.SharedPreferenceUtils
import com.iisysgroup.poslib.host.entities.TransactionResult
import com.itex.richard.payviceconnect.model.SmileModel
import kotlinx.android.synthetic.main.smile_card_successful.view.*
import kotlinx.android.synthetic.main.smile_cash_successful_receipt.view.*
import java.util.*

object SmileReceiptGenerator {

    fun generateCashSuccessful(context : Context, smileResponse : SmileModel.SmileSuccessResponse, lookup : SmileModel.SmileValidateCustomerResponse,  dataplan : SmileModel.Bundle, accountId : String) : View {
        val inflatedView = View.inflate(context, R.layout.smile_cash_successful_receipt, null)

        val terminalId = SharedPreferenceUtils.getTerminalId(context)
        val walletId = SharedPreferenceUtils.getPayviceWalletId(context)

        inflatedView.currentTime.text = Calendar.getInstance().time.toString()

        inflatedView.smileTransactionStatus.text = smileResponse.transactionStatus
        inflatedView.smilePaymentMethod.text = "CASH"
        inflatedView.smileTerminalId.text = terminalId
        inflatedView.smileWalletId.text = walletId
        inflatedView.smileAccountId.text = accountId
        inflatedView.smileRef.text = smileResponse.reference
        inflatedView.smileCustomerName.text = lookup.customerName
        inflatedView.smileDataPlan.text = dataplan.name
        inflatedView.smileDataValidity.text = dataplan.validity

        inflatedView.smileAmount.append(dataplan.displayPrice.toString())

        return inflatedView
    }

    fun generateCashDeclined(context : Context, lookup : SmileModel.SmileValidateCustomerResponse, dataplan : SmileModel.Bundle, accountId : String) : View {
        val inflatedView = View.inflate(context, R.layout.smile_cash_successful_receipt, null)

        val terminalId = SharedPreferenceUtils.getTerminalId(context)
        val walletId = SharedPreferenceUtils.getPayviceWalletId(context)

        inflatedView.currentTime.text = Calendar.getInstance().time.toString()

        inflatedView.smileTransactionStatus.text = "DECLINED"
        inflatedView.smilePaymentMethod.text = "CASH"
        inflatedView.smileTerminalId.text = terminalId
        inflatedView.smileWalletId.text = walletId
        inflatedView.smileAccountId.text = accountId
        inflatedView.smileRef.text = "Nil"
        inflatedView.smileCustomerName.text = lookup.customerName
        inflatedView.smileDataPlan.text = dataplan.name
        inflatedView.smileDataValidity.text = dataplan.validity

        inflatedView.smileAmount.append(dataplan.displayPrice.toString())

        return inflatedView
    }

    fun generateCardSuccessful(context: Context, transactionResult: TransactionResult, lookup : SmileModel.SmileValidateCustomerResponse, dataplan : SmileModel.Bundle, accountId : String) : View {
        val inflatedView = View.inflate(context, R.layout.smile_card_successful, null)

        val terminalId = SharedPreferenceUtils.getTerminalId(context)
        val walletId = SharedPreferenceUtils.getPayviceWalletId(context)

        inflatedView.cardCurrentTime.text = Calendar.getInstance().time.toString()
        inflatedView.smileCardTransactionStatus.text = "CARD"
        inflatedView.smileCardTerminalId.text = terminalId
        inflatedView.smileCardWalletId.text = walletId
        inflatedView.smileCardAccountId.text = accountId
        inflatedView.smileCardRrn.text = transactionResult.RRN

        inflatedView.smileCardCustomerName.text = lookup.customerName
        inflatedView.smileCardPan.text = transactionResult.PAN

        inflatedView.smileCardDataPlan.text = dataplan.name
        inflatedView.smileCardDataValidity.text = dataplan.validity

        inflatedView.smileAmount.text = dataplan.displayPrice.toString()



        return inflatedView


    }
}