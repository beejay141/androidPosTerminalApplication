package com.iisysgroup.androidlite.transaction_viewpager_fragments

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.*
import android.widget.Toast
import com.iisysgroup.androidlite.App
import com.iisysgroup.androidlite.DataHolder
import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.SummaryAnalysis
import com.iisysgroup.poslib.host.Host
import com.iisysgroup.poslib.host.entities.TransactionResult
//import com.kizitonwose.time.days
import kotlinx.android.synthetic.main.fragment_transaction_summary.*
import kotlinx.android.synthetic.main.fragment_transaction_summary.view.*
import org.jetbrains.anko.doAsync
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class TransactionSummary : Fragment(){
    internal lateinit var application: App

    internal lateinit var view : View

    internal lateinit var result : List<TransactionResult>

    val endTimeHolder = Calendar.getInstance()
    var endTimeInMillis = endTimeHolder.timeInMillis

//    val interval = (30.days.inMilliseconds).longValue

    val interval =0L

    var startTimeInMillis = endTimeInMillis - interval

    val day = endTimeHolder.time.day
    val month = endTimeHolder.time.month
    val year = endTimeHolder.time.year

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.let {
            it.inflate(R.menu.transaction_summary, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let {
            when(it.itemId){
                R.id.action_graph -> {
                    val intent = Intent(context, SummaryAnalysis::class.java)
                    val values = sortOutTransactions(result)
                    intent.putExtra(Intent.EXTRA_COMPONENT_NAME, values)
                    startActivity(intent)
                }
            }
        }
        return true
    }

    fun setDataToText(result: List<TransactionResult>){
        this.result = result
        val total_amount = numberOfSuccessfulTransactions(result) + numberOfFailedTransactions(result)
        total_transactions_made.text = "Total transactions: $total_amount"
        total_approved_transactions_made.text = "Total approved : ${numberOfSuccessfulTransactions(result)}"
        total_declined_transactions_made.text = "Total declined : ${numberOfFailedTransactions(result)}"
        total_amount_approved_transactions_made.text = "Total approved amount : \u20A6${amountTotalApproved(result)}"
        total_amount_declined_transactions_made.text  = "Total declined amount : \u20A6${amountTotalDeclined(result)}"

    }

    private fun retrieveTransactions(startDateLong: Long = startTimeInMillis, endDateLong: Long = endTimeInMillis) {

        view.transaction_time_width.text = "Showing transactions from ${view.startDate.text} to ${view.endDate.text}"

        if (startDateLong <= endDateLong){
            doAsync {
                application.db.transactionResultDao.findInDateRange(startDateLong, endDateLong).observe({lifecycle}){

                    DataHolder.transactionResults = it as ArrayList<TransactionResult>
                    setDataToText(it as List<TransactionResult>)
                }
            }
        } else {
            Toast.makeText(context, "Start date can not be later than end date", Toast.LENGTH_LONG).show()
        }
    }

    private fun showDialog(isStartDate : Boolean){
        val onDateSetListener = OnDateSetListener { datePicker, i, i1, i2 ->
            val newCal = Calendar.getInstance()
            newCal.set(i, i1, i2)
            Log.d("i", "$i")
            Log.d("i1", "$i1")
            Log.d("i2", "$i2")

            val format = "dd/MM/yyyy"
            val dateFormat = SimpleDateFormat(format)

            if (isStartDate){
                startTimeInMillis = newCal.timeInMillis
                retrieveTransactions(startDateLong = newCal.timeInMillis)
                startDate.setText(dateFormat.format(newCal.time))
            } else {
                retrieveTransactions(endDateLong = newCal.timeInMillis)
                endDate.setText(dateFormat.format(newCal.time))
            }
        }
        val datePickerDialog = DatePickerDialog(this@TransactionSummary.context, onDateSetListener, year, month, day)
        datePickerDialog.show()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        view = inflater.inflate(R.layout.fragment_transaction_summary, container, false)

        val dateFormat = SimpleDateFormat("dd/MM")

        view.startDate.setText(dateFormat.format(startTimeInMillis))
        view.endDate.setText(dateFormat.format(endTimeInMillis))

        view.startDate.setOnClickListener { showDialog(true) }
        view.endDate.setOnClickListener { showDialog(false) }


        activity?.let {
            application = it.application as App
            retrieveTransactions()
        }

        return view
    }

    data class TransactionsStore(val count : Int, val amount : Long) : Serializable

    fun sortOutTransactions(transactionResults: List<TransactionResult>?) : HashMap<Host.TransactionType, TransactionsStore> {
        var cashAdvanceCount = 0
        var cashAdvanceAmount = 0L
        var cashBackCount = 0
        var cashBackAmount = 0L
        var purchaseCount = 0
        var purchaseAmount = 0L
        var transferCount = 0
        var transferAmount = 0L
        Log.d("Got called", "Before results")
        for (result in transactionResults!!){
            when (result.transactionType){
                Host.TransactionType.PURCHASE_WITH_CASH_ADVANCE -> {
                    cashAdvanceCount++
                    cashAdvanceAmount += result.amount
                }
                Host.TransactionType.PURCHASE_WITH_CASH_BACK -> {
                    cashBackCount++
                    cashBackAmount += result.amount
                }
                Host.TransactionType.PURCHASE -> {
                    purchaseCount++
                    purchaseAmount += result.amount
                    Log.d("Got called", "In purchasE")
                    Log.d("Got called", "$purchaseAmount is equals to")
                }
                Host.TransactionType.FUND_TRANSFER -> {
                    transferCount++
                    transferAmount += result.amount
                }
            }
        }

        val map = HashMap<Host.TransactionType, TransactionsStore>()
        map[Host.TransactionType.PURCHASE_WITH_CASH_ADVANCE] = TransactionsStore(cashAdvanceCount, cashAdvanceAmount)
        map[Host.TransactionType.PURCHASE_WITH_CASH_BACK] = TransactionsStore(cashBackCount, cashBackAmount)
        map[Host.TransactionType.PURCHASE] = TransactionsStore(purchaseCount, purchaseAmount)
        map[Host.TransactionType.FUND_TRANSFER] = TransactionsStore(transferCount, transferAmount)

        return map
    }

    fun numberOfSuccessfulTransactions(transactionResults: List<TransactionResult>?): Long {

        var successful_count = 0
        for (result in transactionResults!!) {
            if (result.isApproved) {
                successful_count++
            }
        }
        return successful_count.toLong()
    }

    fun numberOfFailedTransactions(transactionResults: List<TransactionResult>?): Long {
        var failed_count = 0
        for (result in transactionResults!!) {
            if (!result.isApproved) {
                failed_count++
            }
        }
        return failed_count.toLong()
    }

    fun amountTotalApproved(transactionResults: List<TransactionResult>?): Long {
        var amount: Long = 0
        transactionResults?.let {
            for (result in transactionResults) {
                if (result.isApproved) {
                    amount += result.amount
                }
            }
            return amount
        }
        return 0L
    }

    fun amountTotalDeclined(transactionResults: List<TransactionResult>?): Long {
        var amount: Long = 0
        for (result in transactionResults!!) {
            if (!result.isApproved) {
                amount += result.amount
            }
        }
        return amount
    }

    /*internal fun netTotalAmount(transactionResults: ArrayList<TransactionResult>): HashMap<String, Long> {
        val map = HashMap<String, Long>()
        val amount: Long = 0
        var vas_amount: Long = 0
        //var purchase_amount: Long = 0
        var transfer_amount: Long = 0
        for (result in transactionResults) {
            if (result.isApproved) {
                when (result.transactionType) {
                    *//*ost.TransactionType.PURCHASE -> {
                        vas_amount += result.amount
                        map["vas"] = vas_amount
                    }

                    Host.TransactionType.PURCHASE -> {
                       // purchase_amount += result.amount
                        //map["purchase"] = purchase_amount
                    }

                    Host.TransactionType.PURCHASE -> {
                        transfer_amount += result.amount
                        map["transfer"] = transfer_amount
                    }*//*
                }
            }
        }
        return map
    }*/
}
