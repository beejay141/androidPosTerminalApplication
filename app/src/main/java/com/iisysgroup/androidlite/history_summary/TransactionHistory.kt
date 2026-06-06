package com.iisysgroup.androidlite.history_summary

//import AmpEmvL2Android.AmpEmvCB.listener
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import com.iisysgroup.androidlite.App
import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.R.color.history
import com.iisysgroup.androidlite.history_summary.model.HistoryModel
import com.iisysgroup.androidlite.history_summary.service.HistoryService
import com.iisysgroup.androidlite.payments_menu.BasePaymentActivity
import com.iisysgroup.androidlite.payments_menu.RefundActivity
import com.iisysgroup.androidlite.transaction_viewpager_fragments.TransactionHistory
import com.iisysgroup.androidlite.utils.SharedPreferenceUtils
import com.iisysgroup.androidlite.utils.stringValue
import com.iisysgroup.androidlite.viewmodels.RecyclerClickListener
import com.iisysgroup.poslib.host.entities.TransactionResult
import com.iisysgroup.poslib.utils.Utilities
import kotlinx.android.synthetic.main.activity_transaction_history.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.alert
import java.util.ArrayList

class TransactionHistory : AppCompatActivity() {


    private lateinit var walletHistory : HistoryModel

    private lateinit var historyAdapter : HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_history)

        setSupportActionBar(toolbar)

        fetchData()

    }

    /*override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.history_menu, menu)
        return true
    }*/



    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId){
            R.id.historyCard -> {
                fetchData(TransactionHistory.HISTORY_TYPE.CARD)
                return true
            }

            R.id.historyWallet -> {
                fetchData(TransactionHistory.HISTORY_TYPE.WALLET)
                return true
            }
        }
        return false
    }

    private fun fetchData(historyType : TransactionHistory.HISTORY_TYPE = TransactionHistory.HISTORY_TYPE.CARD) {
        historyAdapter = HistoryAdapter(historyType)

        historyProgressBar.visibility = View.VISIBLE

        transactionHistory.layoutManager = GridLayoutManager(this, 1, GridLayoutManager.VERTICAL, false)

        when (historyType){
            TransactionHistory.HISTORY_TYPE.WALLET -> {
                transactionHistory.adapter = historyAdapter
                launch {
                    val walletId = SharedPreferenceUtils.getPayviceWalletId(this@TransactionHistory)

                    walletHistory =  HistoryService.getInstance().getWalletHistory(walletId).await()


                    if (walletHistory.error){
                        launch(UI){
                            alert {
                                title = "Error"
                                message = "Error retrieving history for wallet Id $walletId"
                            }.show()
                        }
                    }

                    launch(UI){
                        historyProgressBar.visibility = View.GONE
                        //historyAdapter.setTransactionResults(walletHistory)
                    }

                }
            }

            TransactionHistory.HISTORY_TYPE.CARD -> {
                val adapter = HistoryAdapter(TransactionHistory.HISTORY_TYPE.CARD)
                transactionHistory.adapter = adapter

                val result = (application as App).db.transactionResultDao.findAll()
                result.observe({lifecycle}){
                    historyProgressBar.visibility = View.GONE
                    adapter.setTransactionResults(it!!)
                }
            }

            TransactionHistory.HISTORY_TYPE.ALL -> {

            }
        }
    }


    internal inner class HistoryAdapter(val historyType : TransactionHistory.HISTORY_TYPE) : RecyclerView.Adapter<HistoryViewHolder>() {
        private var transactionResults = ArrayList<TransactionResult>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {

            val view = LayoutInflater.from(this@TransactionHistory).inflate(R.layout.individual_transaction_history, parent, false)
            return HistoryViewHolder(view)
        }

        override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
            when (historyType){
                TransactionHistory.HISTORY_TYPE.WALLET -> {
                    val result = walletHistory.data[position]

                    holder.transaction_amount.text = "\u20A6"+Utilities.parseLongIntoNairaKoboString(result.amount.toLong()).substring(1)
                    holder.beneficiary_name.text = result.date
                    holder.transaction_type.text = result.service
                    holder.transaction_id.text = result.ref

                    holder.itemView.tag = result

                }

                TransactionHistory.HISTORY_TYPE.CARD -> {
                    val result = transactionResults[position]

                    holder.transaction_amount.text = Utilities.parseLongIntoNairaKoboString(result.amount).substring(1)
                    holder.beneficiary_name.text = result.transactionStatus
                    holder.transaction_type.text = result.transactionType.stringValue
                    holder.transaction_id.text = result.PAN

                    holder.itemView.tag = result
                }
            }


        }

        fun setTransactionResults(transactionResults: List<TransactionResult>?) {
            transactionResults?.let {
                this.transactionResults.clear()
                this.transactionResults.addAll(it)
                notifyDataSetChanged()
            }
        }

        fun setWalletTransactionResults(transactionResults : List<HistoryModel>){

        }

        override fun getItemCount(): Int {
            return  transactionResults.size
        }
    }


    internal inner class HistoryViewHolder(view_that_is_passed: View) : RecyclerView.ViewHolder(view_that_is_passed), View.OnClickListener {
        var transaction_type: TextView
        var beneficiary_name: TextView
        var transaction_amount: TextView
        var transaction_id: TextView
        var view_details: Button

        init {
            view_details = itemView.findViewById(R.id.view_details_btn)
            transaction_type = itemView.findViewById(R.id.history_transaction_type)
            beneficiary_name = itemView.findViewById(R.id.history_beneficiary_name)
            transaction_amount = itemView.findViewById(R.id.history_transaction_amount)
            transaction_id = itemView.findViewById(R.id.history_transaction_id)

            view_details.setOnClickListener(this)
            view_that_is_passed.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val transactionResult = itemView.tag as TransactionResult

            val intent = Intent(this@TransactionHistory, RefundActivity::class.java)
            intent.putExtra(BasePaymentActivity.TRANSACTION_RRN, transactionResult.RRN)
            startActivity(intent)
        }
    }
}
