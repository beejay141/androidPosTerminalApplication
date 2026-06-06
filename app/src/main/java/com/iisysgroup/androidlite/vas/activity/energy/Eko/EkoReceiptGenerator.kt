package com.iisysgroup.androidlite.vas.activity.energy.Eko

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.models.BaseReceiptDetails
import com.iisysgroup.poslib.host.entities.TransactionResult
import kotlinx.android.synthetic.main.activity_print.view.*
import kotlinx.android.synthetic.main.eko_successful_card_receipt.view.*
import kotlinx.android.synthetic.main.eko_successful_receipt.view.*

object EkoReceiptGenerator {

    fun generateCashSuccessfulReceipt(context : Context, ekoModel : EkoModel.EkoPaySuccessResponse, baseTransactionReceipt : BaseReceiptDetails) : View {



        val ekoSuccessReceiptLayout = View.inflate(context, R.layout.eko_successful_receipt, null)
        ekoSuccessReceiptLayout.tokenHolder.visibility = View.VISIBLE


        ekoSuccessReceiptLayout.currentTime.text = ekoModel.date

        ekoSuccessReceiptLayout.ekoTerminalId.text = baseTransactionReceipt.terminalID

        ekoSuccessReceiptLayout.ekoCustomerName.text = ekoModel.payer

        ekoSuccessReceiptLayout.ekoCustomerAddress.text = ekoModel.address

        ekoSuccessReceiptLayout.ekoCustomerNumber.text = ekoModel.customerMeterNumber

        ekoSuccessReceiptLayout.ekoTransactionId.text = ekoModel.ref

        ekoSuccessReceiptLayout.ekoCustomerAccountType.text = ekoModel.account_type

        ekoSuccessReceiptLayout.ekoCustomerId.text = ekoModel.customerId

        ekoSuccessReceiptLayout.ekoCustomerBusiness.text = ekoModel.customerBusinessUnit

        ekoSuccessReceiptLayout.ekoAmount.append(ekoModel.amount)

        if(ekoModel.token != null){
            ekoSuccessReceiptLayout.token.text = ekoModel.token
        }


        return ekoSuccessReceiptLayout
    }

    fun generateCashDeclinedReceipt(context : Context, ekoModel : EkoModel.EkoPayFailedResponse, lookupResult : EkoModel.EkoLookUpSuccessResponse, baseTransactionReceipt : BaseReceiptDetails, amount : String) : View {

        val ekoSuccessReceiptLayout = View.inflate(context, R.layout.eko_successful_receipt, null)


        ekoSuccessReceiptLayout.ekoTransactionStatus.text = "DECLINED"

        ekoSuccessReceiptLayout.currentTime.text = ekoModel.date

        ekoSuccessReceiptLayout.ekoTerminalId.text = baseTransactionReceipt.terminalID

        ekoSuccessReceiptLayout.ekoCustomerName.text = lookupResult.name

        ekoSuccessReceiptLayout.ekoCustomerAddress.text = lookupResult.address

        ekoSuccessReceiptLayout.ekoCustomerNumber.text = "Nil"

        ekoSuccessReceiptLayout.ekoTransactionId.text = ekoModel.ref

        ekoSuccessReceiptLayout.ekoCustomerAccountType.text = lookupResult.account_type

        ekoSuccessReceiptLayout.ekoCustomerId.text = "Nil"

        ekoSuccessReceiptLayout.ekoCustomerBusiness.text = lookupResult.businessDistrict

        ekoSuccessReceiptLayout.ekoAmount.append(amount)


        return ekoSuccessReceiptLayout
    }

    fun generateCardSuccessfulReceipt(context : Context, ekoModel : EkoModel.EkoPaySuccessResponse, baseTransactionReceipt : BaseReceiptDetails, transactionResult: TransactionResult) : View {

        val ekoSuccessReceiptLayout = LayoutInflater.from(context).inflate(R.layout.eko_successful_card_receipt, null, false)



        ekoSuccessReceiptLayout.ekoCardPan.text = transactionResult.PAN

        ekoSuccessReceiptLayout.ekoCardRrn.text = transactionResult.RRN

        ekoSuccessReceiptLayout.ekoCardAuthId.text = transactionResult.authID

        ekoSuccessReceiptLayout.ekoCardCardExpiry.text = transactionResult.cardExpiry

        ekoSuccessReceiptLayout.ekoCardCardName.text = transactionResult.cardHolderName

        ekoSuccessReceiptLayout.currentTime.text = ekoModel.date

        ekoSuccessReceiptLayout.ekoCardTerminalId.text = baseTransactionReceipt.terminalID

        ekoSuccessReceiptLayout.ekoCardCustomerName.text = ekoModel.payer

        ekoSuccessReceiptLayout.ekoCardCustomerAddress.text = ekoModel.address

        ekoSuccessReceiptLayout.ekoCardCustomerNumber.text = ekoModel.customerMeterNumber

        ekoSuccessReceiptLayout.ekoCardTransactionId.text = ekoModel.ref

        ekoSuccessReceiptLayout.ekoCardCustomerAccountType.text = ekoModel.account_type

        ekoSuccessReceiptLayout.ekoCardCustomerId.text = ekoModel.customerId

        ekoSuccessReceiptLayout.ekoCardCustomerBusiness.text = ekoModel.customerBusinessUnit

        ekoSuccessReceiptLayout.ekoCardAmount.text = ekoModel.amount

        return ekoSuccessReceiptLayout
    }

    fun generateCardDeclinedReceipt(context : Context, ekoModel : EkoModel.EkoPayFailedResponse, lookupResult : EkoModel.EkoLookUpSuccessResponse,  baseTransactionReceipt : BaseReceiptDetails, transactionResult: TransactionResult) : View {

        val ekoCardFailedDeclinedReceipt = LayoutInflater.from(context).inflate(R.layout.eko_declined_card_receipt, null, false)

        ekoCardFailedDeclinedReceipt.transactionStatusReason.text = transactionResult.transactionStatusReason

        ekoCardFailedDeclinedReceipt.currentTime.text = ekoModel.date

        ekoCardFailedDeclinedReceipt.ekoTerminalId.text = baseTransactionReceipt.terminalID

        ekoCardFailedDeclinedReceipt.ekoCustomerName.text = lookupResult.name

        ekoCardFailedDeclinedReceipt.ekoCustomerAddress.text = lookupResult.address

        ekoCardFailedDeclinedReceipt.ekoCustomerNumber.text = "Nil"

        ekoCardFailedDeclinedReceipt.ekoTransactionId.text = ekoModel.ref

        ekoCardFailedDeclinedReceipt.ekoCustomerAccountType.text = lookupResult.account_type

        ekoCardFailedDeclinedReceipt.ekoCustomerId.text = "Nil"

        ekoCardFailedDeclinedReceipt.ekoCustomerBusiness.text = lookupResult.businessDistrict

        ekoCardFailedDeclinedReceipt.ekoAmount.text = transactionResult.amount.toString()

        return ekoCardFailedDeclinedReceipt
    }


    fun getBitmapFromView(view: View): Bitmap {
        //Define a bitmap with the same size as the view
        val returnedBitmap = Bitmap.createBitmap(view.width, view.width, Bitmap.Config.ARGB_8888)
        //Bind a canvas to it
        val canvas = Canvas(returnedBitmap)
        //Get the view's background
        val bgDrawable = view.getBackground()
        if (bgDrawable != null)
        //has background drawable, then draw it on the canvas
            bgDrawable!!.draw(canvas)
        else
        //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE)
        // draw the view on the canvas
        view.draw(canvas)
        //return the bitmap
        return returnedBitmap
    }
}