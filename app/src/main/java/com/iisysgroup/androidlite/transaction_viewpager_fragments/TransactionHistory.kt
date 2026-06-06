package com.iisysgroup.androidlite.transaction_viewpager_fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.iisysgroup.androidlite.App
import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.history_summary.model.HistoryModel
import com.iisysgroup.androidlite.history_summary.service.HistoryService
import com.iisysgroup.androidlite.utils.SharedPreferenceUtils
import com.iisysgroup.androidlite.viewmodels.RecyclerClickListener
import com.iisysgroup.androidlite.utils.stringValue
import com.iisysgroup.poslib.host.entities.TransactionResult
import com.iisysgroup.poslib.utils.Utilities
import kotlinx.android.synthetic.main.activity_transaction_history.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.support.v4.alert
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class TransactionHistory : Fragment() {

    enum class HISTORY_TYPE {
        WALLET, CARD, ALL
    }

    private lateinit var walletHistory : HistoryModel

    internal lateinit var listener: RecyclerClickListener
    internal lateinit var mRecyclerView: RecyclerView
    internal lateinit var historyAdapter: HistoryAdapter

    internal lateinit var application: App
    internal lateinit var progressBar: ProgressBar


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.history_menu, menu)
    }





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

    private fun fetchData(type: TransactionHistory.HISTORY_TYPE) {
        when (type){
            TransactionHistory.HISTORY_TYPE.CARD -> {
                progressBar.visibility = View.VISIBLE

                historyAdapter = HistoryAdapter(HISTORY_TYPE.CARD)
                val transactionResultLiveData = application.db.transactionResultDao.findAll()
                transactionResultLiveData.observe(activity!!, Observer { transactionResults ->
                    historyAdapter.setTransactionResults(transactionResults)
                    progressBar.visibility = View.GONE

                    val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    mRecyclerView.layoutManager = linearLayoutManager
                    mRecyclerView.adapter = historyAdapter
                })
            }

            TransactionHistory.HISTORY_TYPE.WALLET -> {
                val adapter = HistoryAdapter(HISTORY_TYPE.WALLET)
                progressBar.visibility = View.VISIBLE

                launch {
                    val walletId = SharedPreferenceUtils.getPayviceWalletId(this@TransactionHistory.context!!)

                    walletHistory =  HistoryService.getInstance().getWalletHistory(walletId).await()

                    launch (UI){
                        progressBar.visibility = View.GONE
                    }
                    if (walletHistory.error){
                        launch(UI){
                            alert {
                                title = "Error"
                                message = "Error retrieving history for wallet Id $walletId"
                            }.show()
                        }

                    } else {
                        launch(UI){
                            historyProgressBar.visibility = View.GONE

                            adapter.setWalletResults(walletHistory)
                            transactionHistory.adapter = adapter
                        }
                    }
                }
            }
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        application = activity?.application as App
        val view = inflater.inflate(R.layout.fragment_transaction_history, container, false)
        listener = ViewModelProviders.of(activity!!).get(RecyclerClickListener::class.java)


        mRecyclerView = view.findViewById(R.id.recyclerView_transaction_history)
        progressBar = view.findViewById(R.id.progressBar)

        progressBar.visibility = View.VISIBLE


        fetchData(HISTORY_TYPE.CARD)


        return view
    }


    internal inner class HistoryAdapter(val historyType : TransactionHistory.HISTORY_TYPE) : RecyclerView.Adapter<HistoryViewHolder>() {
        private var transactionResults = ArrayList<TransactionResult>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.individual_transaction_history, parent, false)
            return HistoryViewHolder(view)
        }

        override fun onBindViewHolder(holder: TransactionHistory.HistoryViewHolder, position: Int) {
            when (historyType){
                TransactionHistory.HISTORY_TYPE.WALLET -> {
                    val result = walletHistory.data[position]

                    holder.transaction_amount.text = Utilities.parseLongIntoNairaKoboString(result.amount.toLong()).substring(1)
                    holder.beneficiary_name.text = result.date
                    holder.transaction_type.text = result.service
                    holder.transaction_id.text = result.ref

                    holder.itemView.tag = result

                }

                TransactionHistory.HISTORY_TYPE.CARD -> {
                    val result = transactionResults[position]

                    holder.transaction_amount.text = Utilities.parseLongIntoNairaKoboString(result.amount).substring(1)
                    holder.beneficiary_name.text = result.transactionStatus
                    holder.transaction_type.setText(result.transactionType.stringValue)
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

        fun setWalletResults(walletHistory: HistoryModel) {

            notifyDataSetChanged()
        }

        override fun getItemCount(): Int {
            return  transactionResults.size
        }
    }


    internal inner class HistoryViewHolder(view_that_is_passed: View) : RecyclerView.ViewHolder(view_that_is_passed), View.OnClickListener {
        internal var transaction_type: TextView
        internal var beneficiary_name: TextView
        internal var transaction_amount: TextView
        internal var transaction_id: TextView
        internal var view_details: Button

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

            listener.setTransactionResult(transactionResult.RRN)
        }
    }

}

