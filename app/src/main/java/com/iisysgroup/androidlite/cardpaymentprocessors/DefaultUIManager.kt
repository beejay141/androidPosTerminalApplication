package com.iisysgroup.androidlite.cardpaymentprocessors

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.payments_menu.PaymentsActivity
import com.iisysgroup.poslib.deviceinterface.DeviceState
import com.iisysgroup.poslib.utils.Utilities
import org.jetbrains.anko.indeterminateProgressDialog

class DefaultUIManager(private val parentContainerView : View, val uiModel: DefaultUIModel) {


    private val mProgressDialog by lazy {
        parentContainerView.context.indeterminateProgressDialog("Processing"){
            setCancelable(false)
        }
    }

    //Helper method to show UI details depending on the state
    fun setState(deviceState : DeviceState, msg : String = ""){

        setDetails(uiModel)

        when (deviceState){
            DeviceState.INSERT_CARD -> {
                Log.d("OkH" ,"Insert card")
                val insertCardView = parentContainerView.findViewById<View>(R.id.insert_card)
                showVisibility(insertCardView)
            }

            DeviceState.DECLINED, DeviceState.FAILED, DeviceState.APPROVED -> {
                if (mProgressDialog.isShowing)
                mProgressDialog.dismiss()

                handleTransactionCompletion(deviceState)
            }

            DeviceState.PROCESSING, DeviceState.AWAITING_ONLINE_RESPONSE -> {
                if (mProgressDialog.isShowing){
                    Log.d("Log", "Progress Showing")
                } else {
                    mProgressDialog.show()
                }
            }
            DeviceState.INFO ->{
                if(mProgressDialog.isShowing){
                    mProgressDialog.setMessage(msg)
                }
            }
        }
    }



    private fun setDetails(uiModel: DefaultUIModel){
        val insertCardView = parentContainerView.findViewById<View>(R.id.insert_card)

        val transactionType = insertCardView.findViewById<TextView>(R.id.transactionTypeText)
        transactionType.text = uiModel.transactionTitle



        val transactionAmount = insertCardView.findViewById<TextView>(R.id.transactionAmountText)
        transactionAmount.text =  "${Utilities.parseLongIntoNairaKoboString(uiModel.amount)}"

    }

    private fun showVisibility(view : View) {
        val layout_ids = intArrayOf(R.id.enter_amount, R.id.account_select_reversal,
                R.id.insert_card, R.id.transaction_status_layout)

        if (view.visibility == View.VISIBLE) {
            return
        }

        for (ids in layout_ids) {
            if (parentContainerView.findViewById<View>(ids) != null && ids != view.id)
                parentContainerView.findViewById<View>(ids).visibility = View.GONE
        }

        view.visibility = View.VISIBLE
    }

    private fun handleTransactionCompletion(deviceState : DeviceState){
        val transactionCompleteView = parentContainerView.findViewById<View>(R.id.transaction_status_layout)

        showVisibility(transactionCompleteView)

        val statusView = transactionCompleteView.findViewById<TextView>(R.id.transactionStatusText)
        val statusImageView = transactionCompleteView.findViewById<ImageView>(R.id.transactionStatusImage)

        val finishButton = transactionCompleteView.findViewById<Button>(R.id.finishButton)
0
        when (deviceState){
            DeviceState.APPROVED -> {
                statusView.text = "Transaction Approved"
                statusImageView.setImageDrawable(parentContainerView.context.getDrawable(R.drawable.transaction_approved))
            }

            DeviceState.DECLINED -> {
                statusView.text = "Transaction Declined"
                statusImageView.setImageDrawable(parentContainerView.context.getDrawable(R.drawable.transaction_declined))
            }

            DeviceState.FAILED -> {
                statusView.text = "Transaction Failed"
                statusImageView.setImageDrawable(parentContainerView.context.getDrawable(R.drawable.transaction_declined))
            }
        }

        finishButton.setOnClickListener {
            val context = parentContainerView.context
            val intent = Intent(context, PaymentsActivity::class.java)
            context.startActivity(intent)

            (context as AppCompatActivity).finish()
        }
    }


}