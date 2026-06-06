package com.iisysgroup.androidlite.vas.activity.Genesis

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnCheckedChanged
import butterknife.OnClick
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.iisysgroup.androidlite.App
import com.iisysgroup.androidlite.PrintActivity
import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.TermMagmActivity
import com.iisysgroup.androidlite.cardpaymentprocessors.VasPurchaseProcessor
import com.iisysgroup.androidlite.login.Helper
import com.iisysgroup.androidlite.login.securestorage.SecureStorage
import com.iisysgroup.androidlite.models.ReceiptModel
import com.iisysgroup.androidlite.payments_menu.BasePaymentActivity
import com.iisysgroup.androidlite.utils.PinAlertUtils
import com.iisysgroup.androidlite.utils.SharedPreferenceUtils
import com.iisysgroup.androidlite.utils.StringUtils
import com.iisysgroup.androidlite.utils.TimeUtils
import com.iisysgroup.androidlite.vas.activity.Genesis.adapters.MovieListAdapter
import com.iisysgroup.androidlite.vas.activity.energy.model.EnergyModel
//import com.iisysgroup.androidlite.vas.cable.startimes.StartimesModel
import com.iisysgroup.payvice.securestorage.SecureStorageUtils
import com.iisysgroup.poslib.deviceinterface.DeviceState
import com.iisysgroup.poslib.host.Host
import com.iisysgroup.poslib.host.entities.TransactionResult
import com.iisysgroup.poslib.utils.AccountType
import com.itex.richard.payviceconnect.model.Genesis
import com.itex.richard.payviceconnect.wrapper.PayviceServices
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_genesis_movile_list.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.okButton
import org.jetbrains.anko.toast
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class GenesisMovieListActivity : AppCompatActivity()  {
    private lateinit var encryptedUserPin: String
    private lateinit var mov : Genesis.Movies
    private lateinit var mRrn : String

    // id of the selected cinema from cinema house list
    private lateinit var cinemaId : String
    private lateinit var amountw: String
    private lateinit var mTransactionResult : TransactionResult


    var isCard = false

    private var ticketType : String? = null

    private val mProgressDialog by lazy {
        indeterminateProgressDialog(message = "Please wait while we purchase your ticket.", title = "Processing") {
            setCancelable(true)
        }
    }

    private val mWaitDialog by lazy {
        indeterminateProgressDialog(message = "Please wait..", title = "Fetching Movies") {
            setCancelable(false)
        }
    }

    private val payviceServices by lazy {
        PayviceServices.getInstance(this)
    }

    private val terminalId by lazy {
        SecureStorage.retrieve(Helper.TERMINAL_ID, "")
    }

    private val userId by lazy {
        SecureStorage.retrieve(Helper.USER_ID, "")
    }

    @BindView(R.id.d_radio_regular)
    lateinit var rdRegular : RadioButton
    @BindView(R.id.d_radio_premium)
    lateinit var rdPremium : RadioButton
    @BindView(R.id.d_radio_combo)
    lateinit var rdCombo : RadioButton
    @BindView(R.id.d_radio_vip)
    lateinit var rdVip : RadioButton
    @BindView(R.id._3d_with_glasses_radio)
    lateinit var rd3DwithGlasses : RadioButton
    @BindView(R.id._3d_without_glasses_radio)
    lateinit var rd3DwithoutGlasses : RadioButton
    @BindView(R.id.d_btn_buy)
    lateinit var btnBuy : Button
    lateinit var dialog : Dialog
    private lateinit var pin : String

    var amount : Double? = null
    lateinit var movieId : String

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId){
            android.R.id.home -> {
               // startActivity(Intent(this@GenesisMovieListActivity, GenesisMovieActivity::class.java))
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_genesis_movile_list)


        supportActionBar?.title = "Movies"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        gencin_title.setText("Movies")
        back_button.setOnClickListener {
            onBackPressed()
        }

        cinemaId = intent.getStringExtra("cima");
        Log.d("genesis",cinemaId)

        mWaitDialog.show()
        payviceServices.GenesisGetMovies(Genesis.GetMoviesRequest(cinemaId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Genesis.GenesisResponses> {
                    override fun onComplete() {
                        mWaitDialog.dismiss()
                    }

                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onNext(t: Genesis.GenesisResponses) {
                        setUpRecyclerView(t.movies!!)
                        mWaitDialog.dismiss()
                    }

                    override fun onError(e: Throwable) {
                        Log.d("genesis",e.message);
                        mWaitDialog.dismiss()
                        alert{
                            title = "Error"
                            message = "An error has occurred, please try again."
                            isCancelable =false

                            okButton {  finish()}
                        }.show()

//
                    }

                })
    }

    private fun setUpRecyclerView(movies : List<Genesis.Movies>){
        val adapter = MovieListAdapter(movies, this, object : MovieListAdapter.OnMovieSelectedListener{
            override fun startPayment(movie: Genesis.Movies) {
                mov = movie
                showMovie(movie)
            }

        })
        recyclerView.adapter = adapter
        val mLinearLayoutManagerVertical = GridLayoutManager(this,2) // (Context context, int spanCount)
        recyclerView.layoutManager = mLinearLayoutManagerVertical
        recyclerView.itemAnimator = DefaultItemAnimator()
    }

    private fun showMovie(movie : Genesis.Movies)
    {
        dialog = Dialog(this)
        dialog.setContentView(R.layout.movie_details)
        ButterKnife.bind(this, dialog);
        var otherMovies = dialog.findViewById<LinearLayout>(R.id.other_movies)
        var _3DMovies  = dialog.findViewById<LinearLayout>(R.id._3d_movies)
        var poster = dialog.findViewById<ImageView>(R.id.d_movieimg)
        var txtMovieName  = dialog.findViewById<TextView>(R.id.d_name)
        var txttime  = dialog.findViewById<TextView>(R.id.d_movieTime)
        var txtGenre  = dialog.findViewById<TextView>(R.id.d_genre)
        var txtDuration  = dialog.findViewById<TextView>(R.id.d_duration)
        var txtSynosis  = dialog.findViewById<TextView>(R.id.d_synopsis)
        var txtRegular  = dialog.findViewById<TextView>(R.id.d_regularPrice)
        var txtPremium  = dialog.findViewById<TextView>(R.id.d_premiumPrice)
        var txtCombo  = dialog.findViewById<TextView>(R.id.d_comboPrice)
        var txtVip  = dialog.findViewById<TextView>(R.id.d_vipPrice)

        var with3d = dialog.findViewById<TextView>(R.id._3d_with_glasses_price)
        var without3d = dialog.findViewById<TextView>(R.id._3d_without_glasses_price)


//        btnBuy.isEnabled = false

        Glide.with(this)
                .load(movie.poster)
                .apply(RequestOptions.placeholderOf(R.drawable.ic_genesis))
                .into(poster)
        movieId = movie.movie_id
        txtMovieName.text = movie.title
        txttime.text = formatDate(movie.start_date + " " + movie.start_time)
        txtGenre.text = movie.genre
        txtDuration.text = movie.duration +"min"
        var synopsis = movie.synopsis;
        if(movie.synopsis.length > 110)
            synopsis = movie.synopsis.substring(0,110)
        txtSynosis.text = synopsis + "..."
        if( movie.amount.threeDWithGlasses== null || movie.amount.threeDWithoutGlasses == null ){

            otherMovies.visibility = View.VISIBLE
            _3DMovies.visibility = View.GONE
            txtRegular.text = setPrice(movie.amount.regular)
            txtPremium.text = setPrice(movie.amount.premium)
            txtCombo.text = setPrice(movie.amount.combo)
            txtVip.text = setPrice(movie.amount.vip)
        }else {
            otherMovies.visibility = View.GONE
            _3DMovies.visibility = View.VISIBLE

            with3d.text = setPrice(movie.amount.threeDWithGlasses)
            without3d.text = setPrice(movie.amount.threeDWithoutGlasses)

        }

        var btnClose = dialog.findViewById<Button>(R.id.d_btn_close)
        btnClose.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                dialog.dismiss()
            }
        })

        dialog.show()
    }


    @OnCheckedChanged(R.id.d_radio_regular)
    fun regularPriceChecked()
    {
        ticketType = "regular"
        Log.d("radio","here");
        if(rdRegular.isChecked)
        {
            amount = mov.amount.regular;
            rdPremium.setChecked(false)
            rdCombo.setChecked(false)
            rdVip.isChecked = false;
            title = "regular"
        }
    }

    @OnCheckedChanged(R.id.d_radio_premium)
    fun premiumPriceChecked()
    {
        if(rdPremium.isChecked)
        {
            ticketType = "premium"
            amount = mov.amount.premium;
            rdRegular.isChecked = false;
            rdCombo.isChecked = false;
            rdVip.isChecked = false;
            title = "premium"
        }
    }

    @OnCheckedChanged(R.id.d_radio_combo)
    fun comboPriceChecked()
    {
        if(rdCombo.isChecked)
        {
            ticketType = "combo"
            amount = mov.amount.combo
            rdRegular.isChecked = false;
            rdPremium.isChecked = false;
            rdVip.isChecked = false;
            title = "combo"
        }
    }

    @OnCheckedChanged(R.id.d_radio_vip)
    fun vipPriceChecked()
    {
        if(rdVip.isChecked)
        {
            ticketType = "vip"
            amount = mov.amount.vip
            rdRegular.isChecked = false
            rdPremium.isChecked = false
            rdCombo.isChecked = false
            title = "vip"
        }
    }

    @OnCheckedChanged(R.id._3d_with_glasses_radio)
    fun with3DGlassesPriceChecked()
    {
        if(rd3DwithGlasses.isChecked)
        {
            amount = mov.amount.threeDWithGlasses
            rd3DwithoutGlasses.isChecked = false;
            ticketType = "threeDWithGlasses"

        }
    }

    @OnCheckedChanged(R.id._3d_without_glasses_radio)
    fun without3DGlassesPriceChecked()
    {
        if(rd3DwithoutGlasses.isChecked)
        {
            amount = mov.amount.threeDWithoutGlasses
            rd3DwithGlasses.isChecked = false;
            ticketType = "threeDWithoutGlasses"
        }
    }
    @OnClick(R.id.d_btn_buy)
    fun buyClicked()
    {
        Log.d("buybutton","clicked")
        if(enableBuyButton()) {
           // showPinEntry()
            selectTransactionType(amount)
            dialog.dismiss()
        }
        else
            toast("Price not set").show()
    }


    private fun enableBuyButton(): Boolean
    {
        return amount != null
    }

    private fun selectTransactionType(amount : Double?){
        this.amount = amount

        val view = LayoutInflater.from(this@GenesisMovieListActivity).inflate(R.layout.activity_enter_pin, null, false)
        alert {
            title = "Transaction Type"
            message = "Select the type of transaction you want to make"
            positiveButton(buttonText = "Card") { _ ->
                isCard = true

                PinAlertUtils.getPin(this@GenesisMovieListActivity, view){
                    val encryptedPassword = SecureStorage.retrieve(Helper.STORED_PASSWORD, "")
                    val pin = SecureStorageUtils.hashIt(it, encryptedPassword)
                    payWithCard( amount!!, pin!!)
                }
            }
            negativeButton(buttonText = "Wallet") {_ ->
                isCard = false
                PinAlertUtils.getPin(this@GenesisMovieListActivity, view){
                    val encryptedPassword = SecureStorage.retrieve(Helper.STORED_PASSWORD, "")
                    val pin = SecureStorageUtils.hashIt(it, encryptedPassword)
                    payWithWallet(amount!!, false, pin!!)
                }
            }
        }.show()
    }

    private fun payWithWallet( amount: Double, isCard : Boolean, pin : String) {
        mProgressDialog.show()
        val clientReference = StringUtils.getClientRef(this@GenesisMovieListActivity, "")
        var payDetails : GenesisModel.payDetail


        launch(CommonPool) {
            val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
            payDetails = if (isCard){
               GenesisModel.payDetail(amount = amount, terminal_id = terminalId, user_id = userId,
                        clientreference = clientReference, movie_id = movieId, pin = pin)
            } else {
                GenesisModel.payDetail(amount = amount, terminal_id = terminalId, user_id = userId,
                        clientreference = clientReference, movie_id = movieId, pin = pin)
            }

            try {
                val request =GenesisService.create().buyTicket(payDetails).await()

                val jsonResponse = Gson().toJsonTree(request).asJsonObject

                launch(UI){
                    mProgressDialog.dismiss()
                }
                if (jsonResponse.toString().contains("\"error\"=true")){
                    val response = gson.fromJson(jsonResponse.toString(), GenesisModel.moviesSuccesfulResponse::class.java)

                    launch(UI){
                        alert {
                            title = "Response"
                            message = "Error : Ticket Purchase Failed"

                            okButton { }
                        }.show()
                    }

                } else {
                    val response = gson.fromJson(jsonResponse.toString(), GenesisModel.moviesSuccesfulResponse::class.java)
                    launch(UI){
                        alert {
                            title = "Response"
                            message = "Ticket Purchase Successful"
                            positiveButton(buttonText = "Print"){

                                if (isCard){
//                                    val intent = Intent(this@PHElectric, EkoPrinter::class.java)
//                                    intent.putExtra("values", response)
//                                    intent.putExtra("lookupDetails",lookupResponse)
//                                    intent.putExtra("ekedc_type", EkoPrinter.EKEDC_RECEIPT_TYPE.CARD_SUCCESSFUL)
//                                    startActivity(intent)
//                                    finish()

                                } else {
                                    val receiptMap = hashMapOf<String, String>(
                                            "Terminal ID" to SharedPreferenceUtils.getTerminalId(this@GenesisMovieListActivity),
                                            "Terminal ID" to SharedPreferenceUtils.getTerminalId(this@GenesisMovieListActivity),
                                            "Ref" to response.ref,
                                            "Title" to response.title,
                                            "Ticket Id" to response.ticket_id,
                                            "Time" to response.start_time,
                                            "Screen" to response.screen
                                    )
                                    val receiptModel = ReceiptModel("", "GENESIS TICKET", "APPROVED", receiptMap, amount.toString(), "Approved")

                                    val intent = Intent(this@GenesisMovieListActivity, PrintActivity::class.java)
                                    intent.putExtra("print_map", receiptModel)
                                    intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_VAS_TYPE, PrintActivity.VasType.GENESIS)
                                    startActivity(intent)
                                    finish()
                                }
                            }

                        }.show()
                    }
                }
            }
            catch (exception : ConnectException){
                launch(UI){
                    mProgressDialog.dismiss()
                    alert {
                        title = "Response"
                        message = "Error in connection. Please check your internet connection"
                        okButton { }
                    }.show()
                }

            }
            catch (exception : SocketTimeoutException){
                launch(UI){
                    mProgressDialog.dismiss()
                    alert {
                        title = "Response"
                        message = "This connection is taking too long. Please try again"
                    }.show()
                }
            }
            catch (e : retrofit2.HttpException){
                launch(UI){
                    mProgressDialog.dismiss()
                    alert {
                        title = "Error"
                        message = "Error from server. Please try again"
                        okButton {  }
                    }.show()
                }
            }
            catch (e : IllegalStateException){
                launch(UI){
                    mProgressDialog.dismiss()
                    alert {
                        title = "Error"
                        message = "Error from server. Please try again"
                        okButton {  }
                    }.show()
                }
            }

        }
    }
    private fun payWithCard(amount: Double, pin: String) {
        this.amount = amount

        this.pin = pin
        val intent = Intent(this, VasPurchaseProcessor::class.java)
        intent.putExtra(BasePaymentActivity.TRANSACTION_ACCOUNT_TYPE, AccountType.DEFAULT_UNSPECIFIED)
        intent.putExtra(BasePaymentActivity.TRANSACTION_TYPE, Host.TransactionType.BILL_PAYMENT)

        //amount * 100 to convert the amount to long
        intent.putExtra(BasePaymentActivity.TRANSACTION_AMOUNT, (amount.toLong() * 100))
        intent.putExtra(BasePaymentActivity.TRANSACTION_ADDITIONAL_AMOUNT, 0L)

        if (SharedPreferenceUtils.getIsTerminalPrepped(this)) {
            startActivityForResult(intent, 9090)
        } else {
            alert {
                isCancelable = false
                title = "Terminal not configured"
                message = "Click O.K to go to configuration page"
                okButton {
                    startActivity(Intent(this@GenesisMovieListActivity, TermMagmActivity::class.java))
                    //this@EkoPostpaid.finish()
                }
            }.show()
        }

    }
    private fun getServices(transactionResult: TransactionResult){
        launch(CommonPool) {
            val clientReference = StringUtils.getClientRef(this@GenesisMovieListActivity, "")
            val payDetails = GenesisModel.payDetail(amount = amount!!, terminal_id = terminalId, user_id = userId,
                    clientreference = clientReference, movie_id = movieId, pin = pin)

            val cardResponse = GenesisService.Factory.create().buyTicket(payDetails).await()

            val jsonResponse = Gson().toJsonTree(cardResponse).asJsonObject
            val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
            if (cardResponse.toString().contains("\"error\":true")) {
                val response = gson.fromJson(jsonResponse.toString(), EnergyModel.PhPayFailedResponse::class.java)
                launch(UI) {
                    alert {
                        title = "Response"
                        message = response.message
                        positiveButton(buttonText = "Ok") {

                        }
                    }.show()
                }

            } else {
                val response = gson.fromJson(jsonResponse.toString(), GenesisModel.moviesSuccesfulResponse::class.java)
                launch(UI) {
                    alert {
                        title = "Response"
                        message = "Ticket Purchase Succesful"
                        positiveButton(buttonText = "Print") {
                            val receiptMap = hashMapOf<String, String>(
                                    "Terminal ID" to SharedPreferenceUtils.getTerminalId(this@GenesisMovieListActivity),
                                    "RRN" to transactionResult.RRN,
                                    "Card PAN" to transactionResult.PAN,
                                    "CardHolder" to transactionResult.cardHolderName,
                                    "Card Expiry" to transactionResult.cardExpiry,
                                    "Auth ID" to transactionResult.authID,
                                    "MID" to transactionResult.merchantID,
                                    "STAN" to transactionResult.STAN
                            )

                            val receiptModel = ReceiptModel("", "IKEJA POSTPAID PURCHASE", transactionResult.transactionStatus, receiptMap, amount.toString(), transactionResult.transactionStatusReason)

                            val intent = Intent(this@GenesisMovieListActivity, PrintActivity::class.java)
                            intent.putExtra("print_map", receiptModel)
                            intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_VAS_TYPE, PrintActivity.VasType.GENESIS)
                            startActivity(intent)

                            finish()
                        }
                    }.show()
                }
            }
        }
    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 9090){
            if (resultCode == Activity.RESULT_OK) {

                val state = data?.getSerializableExtra("state") as DeviceState
                mRrn = data.getStringExtra("rrn")

                when (state){
                    DeviceState.DECLINED, DeviceState.FAILED -> {
                        alert {
                            title = "Transaction Result"
                            message = "Transaction declined. Please try again later"
                            positiveButton(buttonText = "Print"){
                                (application as App).db.transactionResultDao.get(mRrn).observe({lifecycle}){

                                    it?.let {
                                        transactionResult ->

                                        val map = hashMapOf<String, String>(
                                                "MID" to transactionResult.merchantID,
                                                "RRN" to transactionResult.RRN,
                                                "Transaction approved" to "False",
                                                "Card Holder" to transactionResult.cardHolderName,
                                                "Card Expiry" to transactionResult.cardExpiry,
                                                "PAN" to transactionResult.PAN,
                                                "STAN" to transactionResult.STAN,
                                                "Auth ID" to transactionResult.authID
                                        )
                                        val date = TimeUtils.convertLongToString(transactionResult.longDateTime)
                                        val receiptModel = ReceiptModel(date, "Transfer", transactionResult.transactionStatus, map,  amount.toString(), transactionResult.transactionStatusReason)

                                        val intent = Intent(this@GenesisMovieListActivity, PrintActivity::class.java)
                                        intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_MODEL_KEY, receiptModel)
                                        intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_VAS_TYPE, PrintActivity.VasType.NOT_INCLUDED)
                                        //finish()
                                        startActivity(intent)
                                    }

                                }
                            }
                        }.show()

                    }

                    DeviceState.APPROVED -> {
                        // completeCardPayment()
                        (application as App).db.transactionResultDao.get(mRrn).observe({lifecycle}){

                            it?.let { transactionResult ->
                                getServices(transactionResult)
                            }
                            }

//                        alert {
//                            title = "Transaction Result"
//                            message = "Transaction approved."
//                            positiveButton(buttonText = "Print"){
//                                (application as App).db.transactionResultDao.get(mRrn).observe({lifecycle}){
//
//                                    it?.let {
//                                        transactionResult ->
//                                        val map = hashMapOf<String, String>(
//                                                "MID" to transactionResult.merchantID,
//                                                "RRN" to transactionResult.RRN,
//                                                "Transaction approved" to "True",
//                                                "Card Holder" to transactionResult.cardHolderName,
//                                                "Card Expiry" to transactionResult.cardExpiry,
//                                                "PAN" to transactionResult.PAN,
//                                                "STAN" to transactionResult.STAN,
//                                                "Amount" to transactionResult.amount.toString(),
//                                                "Auth ID" to transactionResult.authID
//                                        )
//                                        val date = TimeUtils.convertLongToString(transactionResult.longDateTime)
//                                        val receiptModel = ReceiptModel(date, "Transfer", transactionResult.transactionStatus, map,  amount.toString(), transactionResult.transactionStatusReason)
//
//                                        val intent = Intent(this@GenesisMovieListActivity, PrintActivity::class.java)
//                                        intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_MODEL_KEY, receiptModel)
//                                        intent.putExtra(PrintActivity.KEYS.PRINT_RECEIPT_VAS_TYPE, PrintActivity.VasType.NOT_INCLUDED)
//                                        startActivity(intent)
//                                    }
//
//                                }
//                            }
//                        }.show()
                    } else -> {
                    toast("No data!")
                }
                }
            }
        }
    }



//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == UserPinEntryActivity.PIN_REQUEST_CODE) {
//            if (resultCode == Activity.RESULT_OK) {
//                encryptedUserPin = data!!.getStringExtra(UserPinEntryActivity.PIN_RESPONSE_DATA)
//                showPaymentOption(PaymentOption.Mode.PAY, amount!!.roundToInt(), "")
//            }
//        }
//
//        if (requestCode == PaymentOptionActivity.REQUEST_CODE) {
//            if (resultCode == Activity.RESULT_OK) {
//                val paymentOption = data?.getSerializableExtra(PaymentOptionActivity.RESULT_OPTION) as PaymentOption
//                val card = data.getSerializableExtra(PaymentOptionActivity.RESULT_CARD) as? Card
//                // continuePayment(paymentOption, card)
//            }
//        }
//    }


    private fun moveToHome() {
        finish()
        val intent = Intent(this, GenesisMovieListActivity::class.java)
        startActivity(intent)
    }


    object KEYS {
        const val GENESIS_INTENT_CODE = 3432
    }

    private fun formatDate(date : String) : String
    {
        val d = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date)
        val cal = Calendar.getInstance()
        cal.setTime(d)
        return SimpleDateFormat("EEE, d MMM 'at' hh:mm aaa").format(cal.getTime())
    }

    private fun setPrice(amount : Double?) : String
    {
        var price = "₦ ";
        if (amount != null)
            price += amount.toString()
        else price = "Price not available"

        return price;
    }

}
