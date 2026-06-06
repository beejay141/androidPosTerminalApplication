package com.iisysgroup.androidlite.history_summary

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.*
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.iisysgroup.androidlite.App
import com.iisysgroup.androidlite.PrintTesting.PRINTER_WIDTH
import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.history_summary.model.HistoryModel
import com.iisysgroup.androidlite.history_summary.service.HistoryService
import com.iisysgroup.androidlite.payments_menu.BasePaymentActivity
import com.iisysgroup.androidlite.payments_menu.RefundActivity
import com.iisysgroup.androidlite.transaction_viewpager_fragments.TransactionHistory
import com.iisysgroup.androidlite.utils.SharedPreferenceUtils
import com.iisysgroup.androidlite.utils.TimeUtils
import com.iisysgroup.poslib.host.entities.TransactionResult
import com.iisysgroup.poslib.utils.Utilities
//import com.pos.device.printer.PrintCanvas
//import com.pos.device.printer.PrintTask
//import com.pos.device.printer.Printer
//import com.pos.device.printer.PrinterCallback
import kotlinx.android.synthetic.main.activity_eod.*
import kotlinx.android.synthetic.main.eod_approved_history.*
import kotlinx.android.synthetic.main.eod_declined_history.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.alert
import java.text.SimpleDateFormat
import java.util.*

class EODActivity : AppCompatActivity() {


    private lateinit var walletHistory : HistoryModel

    private lateinit var historyAdapter : HistoryAdapter
    private lateinit var historyAdapter2 : HistoryAdapter

    private lateinit var toolbar : Toolbar
    private lateinit var viewEOD : ScrollView

//    private lateinit var printTask: PrintTask

    var approvedCount = 0
    var declinedCount = 0
    var approvedsumCount = 0
    var declinedsumCount = 0
    var image_url = "http://merchant.payvice.com/external-assets/logos/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eod)
        toolbar = findViewById(R.id.toolbar)

        val mTerminalId by lazy {
            SharedPreferenceUtils.getTerminalId(this)
        }
        val bankPrefix  = mTerminalId.substring(0,4)
        Glide.with(this).load(image_url+bankPrefix+".png").into(terminalOwnerLogo);

        Log.d("ss","saa")
        setSupportActionBar(toolbar)
        terminalID.text = mTerminalId.toString()
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        val date = Date()
        EODDate.text = dateFormat.format(date)
        fetchData()
        fetchData2()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //menuInflater.inflate(R.menu.eod_menu, menu)
        return true
    }



    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId){
//            R.id.historyCard -> {
//                fetchData(TransactionHistory.HISTORY_TYPE.CARD)
//
//                return true
//            }
//
//            R.id.historyWallet -> {
//                fetchData(TransactionHistory.HISTORY_TYPE.WALLET)
//                return true
//            }

              R.id.print -> {
                  val relEod : RelativeLayout = findViewById(R.id.relEod)
                  val bitmap = getBitmapFromView(relEod)
//                        printBitmap(bitmap, true)
                return true
            }

//            R.id.date -> {
//
//                return true
//            }
        }
        return false
    }


    fun getBitmapFromView(view: View): Bitmap {
        //Define a bitmap with the same size as the view
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        //Bind a canvas to it
        val canvas = Canvas(returnedBitmap)
        //Get the view's background
        val bgDrawable = view.background
        if (bgDrawable != null)
        //has background drawable , then draw it on the canvas
            bgDrawable.draw(canvas)
        else
        //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE)
        // draw the view on the canvas
        view.draw(canvas)
        //return the bitmap
        return returnedBitmap
    }

//
//    private fun printBitmap(bitmap: Bitmap?, fitToPage: Boolean) {
//        if (bitmap == null || bitmap.width == 0 || bitmap.height == 0) {
//            return
//        }
//
//        Log.d("viewEOD", "original Bitmap Width:" + bitmap.width + "   Height:" + bitmap.height + "  FitToPage=" + fitToPage)
//
//        // PrintTask constructor
////        printTask = PrintTask()
//
//
//
//        // PrintCanvas constructor
////        val printCanvas = PrintCanvas()
////        val paint = Paint()
//
//
//
//
//        if (fitToPage && bitmap.width != PRINTER_WIDTH) {
//            val scaledHeight = PRINTER_WIDTH * bitmap.height / bitmap.width
//            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, PRINTER_WIDTH, scaledHeight, false)
//            Log.d("viewEOD", "scaled Bitmap Width:" + scaledBitmap.width + "   Height:" + scaledBitmap.height)
//            // Draw the bitmap
//            printCanvas.drawBitmap(scaledBitmap, paint)
//        } else {
//            // Draw the bitmap
//            printCanvas.drawBitmap(bitmap, paint)
//        }
//        // Set print canvas
//        printTask.setPrintCanvas(printCanvas)
//        // Set the amount of feed paper
//        printTask.addFeedPaper(100)
//        // Get the gray value of the task
//        printTask.setGray(130)
//
//        // Start print task
//        Printer.getInstance().startPrint(printTask, printerCallback)
//    }
//
//    private val printerCallback = PrinterCallback { arg0, arg1 ->
//
//    }

    private fun fetchData(historyType : TransactionHistory.HISTORY_TYPE = TransactionHistory.HISTORY_TYPE.CARD) {

        historyAdapter = HistoryAdapter(historyType)
      //  historyAdapter2 = HistoryAdapter(historyType)

        historyProgressBar.visibility = View.VISIBLE

        transactionHistory.layoutManager = GridLayoutManager(this, 1, GridLayoutManager.VERTICAL, false)
      //  transactionHistory2.layoutManager = GridLayoutManager(this, 1, GridLayoutManager.VERTICAL, false)

        when (historyType){
            TransactionHistory.HISTORY_TYPE.WALLET -> {
                transactionHistory.adapter = historyAdapter
                launch {
                    val walletId = SharedPreferenceUtils.getPayviceWalletId(this@EODActivity)

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
                Log.d("here", "here")
                val adapter = HistoryAdapter(TransactionHistory.HISTORY_TYPE.CARD)
                transactionHistory.adapter = adapter
               // transactionHistory2.adapter = adapter

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

            val view = LayoutInflater.from(this@EODActivity).inflate(R.layout.individual_eod_history, parent, false)
            return HistoryViewHolder(view)
        }

        override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
            when (historyType){
                TransactionHistory.HISTORY_TYPE.WALLET -> {
                    val result = walletHistory.data[position]

                    holder.transaction_amount.text = Utilities.parseLongIntoNairaKoboString(result.amount.toLong()).substring(1)
//                    holder.beneficiary_name.text = result.date
//                    holder.transaction_type.text = result.service
//                    holder.transaction_id.text = result.ref

                    holder.itemView.tag = result

                }

                TransactionHistory.HISTORY_TYPE.CARD -> {

                    val result = transactionResults[position]
                    Log.d("amount", result.amount.toString())
                    Log.d("position", position.toString())
                    if (result.isApproved){
                        approvedCount++
                        approvedsumCount = (approvedsumCount+result.amount).toInt()
                        holder.transaction_amount.text = "\u20A6"+Utilities.parseLongIntoNairaKoboString(result.amount).substring(1)
                        holder.transaction_time.text = TimeUtils.convertLongToTime(result.longDateTime)
                        // holder.transaction_amount.text = result.amount.toString()
                        holder.beneficiary_RRN.text = result.RRN
                        Log.d("count", approvedCount.toString())
                        //holder.transaction_id.text = result.PAN
                        approvedValue.text = approvedCount.toString()
                        approvedValue2.text = "\u20A6"+Utilities.parseLongIntoNairaKoboString(approvedsumCount.toLong()).substring(1)
                        holder.itemView.tag = result
                    }
                    else{
                        holder.transaction_amount.visibility = View.GONE
                        holder.transaction_time.visibility = View.GONE
                        holder.beneficiary_RRN.visibility = View.GONE
                        holder.eodrel.visibility = View.GONE
                    }
//                    else{
//                        declinedCount++
//                        declinedsumCount = (declinedsumCount+result.amount).toInt()
//                        holder.transaction_amount.text = "\u20A6"+Utilities.parseLongIntoNairaKoboString(result.amount).substring(1)
//                        holder.transaction_time.text = TimeUtils.convertLongToTime(result.longDateTime)
//                        // holder.transaction_amount.text = result.amount.toString()
//                        holder.beneficiary_RRN.text = result.RRN
//                        Log.d("count", declinedCount.toString())
//                        //holder.transaction_id.text = result.PAN
//                        declinedValue.text = declinedCount.toString()
//                        declinedValue2.text = "\u20A6"+Utilities.parseLongIntoNairaKoboString(declinedsumCount.toLong()).substring(1)
//                        holder.itemView.tag = result
//                    }


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
        var transaction_time: TextView
        var beneficiary_RRN: TextView
        var transaction_amount: TextView
        var eodrel: RelativeLayout

       // var transaction_id: TextView

        init {
            transaction_time = itemView.findViewById(R.id.time)
            beneficiary_RRN = itemView.findViewById(R.id.RRN)
            transaction_amount = itemView.findViewById(R.id.amount)
            eodrel = itemView.findViewById(R.id.eodrel)

           // transaction_id = itemView.findViewById(R.id.history_transaction_id)

            view_that_is_passed.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val transactionResult = itemView.tag as TransactionResult

            val intent = Intent(this@EODActivity, RefundActivity::class.java)
            intent.putExtra(BasePaymentActivity.TRANSACTION_RRN, transactionResult.RRN)
            startActivity(intent)
        }
    }


    private fun fetchData2(historyType2 : TransactionHistory.HISTORY_TYPE = TransactionHistory.HISTORY_TYPE.CARD) {

        historyAdapter2 = HistoryAdapter(historyType2)
        //  historyAdapter2 = HistoryAdapter(historyType)

        historyProgressBar.visibility = View.VISIBLE

        transactionHistory2.layoutManager = GridLayoutManager(this, 1, GridLayoutManager.VERTICAL, false)
        //  transactionHistory2.layoutManager = GridLayoutManager(this, 1, GridLayoutManager.VERTICAL, false)

        when (historyType2){
            TransactionHistory.HISTORY_TYPE.WALLET -> {
                transactionHistory2.adapter = historyAdapter2
                launch {
                    val walletId = SharedPreferenceUtils.getPayviceWalletId(this@EODActivity)

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
                Log.d("here", "here")
                val adapter2 = HistoryAdapter2(TransactionHistory.HISTORY_TYPE.CARD)
                transactionHistory2.adapter = adapter2
                // transactionHistory2.adapter = adapter

                val result2 = (application as App).db.transactionResultDao.findAll()
                result2.observe({lifecycle}){
                    historyProgressBar.visibility = View.GONE
                    adapter2.setTransactionResults2(it!!)
                }
            }

            TransactionHistory.HISTORY_TYPE.ALL -> {

            }
        }
    }


    internal inner class HistoryAdapter2(val historyType2 : TransactionHistory.HISTORY_TYPE) : RecyclerView.Adapter<HistoryViewHolder2>() {
        private var transactionResults2 = ArrayList<TransactionResult>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder2 {

            val view2 = LayoutInflater.from(this@EODActivity).inflate(R.layout.individual_eod_history2, parent, false)
            return HistoryViewHolder2(view2)
        }

        override fun onBindViewHolder(holder2: HistoryViewHolder2, position2: Int) {
            when (historyType2){
                TransactionHistory.HISTORY_TYPE.WALLET -> {
                    val result2 = walletHistory.data[position2]

                    holder2.transaction_amount2.text = Utilities.parseLongIntoNairaKoboString(result2.amount.toLong()).substring(1)
//                    holder.beneficiary_name.text = result.date
//                    holder.transaction_type.text = result.service
//                    holder.transaction_id.text = result.ref

                    holder2.itemView.tag = result2

                }

                TransactionHistory.HISTORY_TYPE.CARD -> {

                    val result2 = transactionResults2[position2]
                    Log.d("amount", result2.amount.toString())
                    Log.d("position", position2.toString())
                    if (!result2.isApproved){
//                        approvedCount++
//                        approvedsumCount = (approvedsumCount+result.amount).toInt()
//                        holder2.transaction_amount2.text = "\u20A6"+Utilities.parseLongIntoNairaKoboString(result.amount).substring(1)
//                        holder2.transaction_time2.text = TimeUtils.convertLongToTime(result.longDateTime)
//                        // holder.transaction_amount.text = result.amount.toString()
//                        holder2.beneficiary_RRN2.text = result.RRN
//                        Log.d("count", approvedCount.toString())
//                        //holder.transaction_id.text = result.PAN
//                        approvedValue.text = approvedCount.toString()
//                        approvedValue2.text = "\u20A6"+Utilities.parseLongIntoNairaKoboString(approvedsumCount.toLong()).substring(1)
//                        holder2.itemView.tag = result
//                    }
//                    else{
                        declinedCount++
                        declinedsumCount = (declinedsumCount+result2.amount).toInt()
                        holder2.transaction_amount2.text = "\u20A6"+Utilities.parseLongIntoNairaKoboString(result2.amount).substring(1)
                        holder2.transaction_time2.text = TimeUtils.convertLongToTime(result2.longDateTime)
                        // holder.transaction_amount.text = result.amount.toString()
                        holder2.beneficiary_RRN2.text = result2.RRN
                        Log.d("count", declinedCount.toString())
                        //holder.transaction_id.text = result.PAN
                        declinedValue.text = declinedCount.toString()
                        declinedValue2.text = "\u20A6"+Utilities.parseLongIntoNairaKoboString(declinedsumCount.toLong()).substring(1)
                        holder2.itemView.tag = result2
                    }
                    else{
                        holder2.transaction_amount2.visibility = View.GONE
                        holder2.transaction_time2.visibility = View.GONE
                        holder2.beneficiary_RRN2.visibility = View.GONE
                        holder2.eodrel2.visibility = View.GONE
                    }


                }
            }


        }

        fun setTransactionResults2(transactionResults2: List<TransactionResult>?) {
            transactionResults2?.let {
                this.transactionResults2.clear()
                this.transactionResults2.addAll(it)
                notifyDataSetChanged()
            }
        }

        fun setWalletTransactionResults(transactionResults : List<HistoryModel>){

        }

        override fun getItemCount(): Int {
            return  transactionResults2.size
        }
    }

    internal inner class HistoryViewHolder2(view_that_is_passed: View) : RecyclerView.ViewHolder(view_that_is_passed), View.OnClickListener {
        var transaction_time2: TextView
        var beneficiary_RRN2: TextView
        var transaction_amount2: TextView
        var eodrel2: RelativeLayout

        // var transaction_id: TextView

        init {
            transaction_time2 = itemView.findViewById(R.id.time2)
            beneficiary_RRN2 = itemView.findViewById(R.id.RRN2)
            transaction_amount2 = itemView.findViewById(R.id.amount2)
            eodrel2 = itemView.findViewById(R.id.eodrel2)

            // transaction_id = itemView.findViewById(R.id.history_transaction_id)

            view_that_is_passed.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val transactionResult = itemView.tag as TransactionResult

            val intent = Intent(this@EODActivity, RefundActivity::class.java)
            intent.putExtra(BasePaymentActivity.TRANSACTION_RRN, transactionResult.RRN)
            startActivity(intent)
        }
    }



}
