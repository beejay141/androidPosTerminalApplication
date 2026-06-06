//package com.iisysgroup.androidlite.vas.activity.Genesis
//
//import android.app.Activity
//import android.content.Intent
//import android.os.Bundle
//import android.preference.PreferenceManager
//import android.support.v4.app.ActivityCompat
//import android.support.v7.app.AppCompatActivity
//import android.support.v7.widget.GridLayoutManager
//import android.util.Log
//import com.iisysgroup.androidlite.*
//import com.iisysgroup.androidlite.cardpaymentprocessors.PurchaseProcessor
//import com.iisysgroup.androidlite.cardpaymentprocessors.VasPurchaseProcessor
//import com.iisysgroup.androidlite.models.ReceiptModel
//import com.iisysgroup.androidlite.payments_menu.BasePaymentActivity
//import com.iisysgroup.androidlite.utils.SharedPreferenceUtils
//import com.iisysgroup.androidlite.vas.activity.Genesis.adapters.MovieListAdapter
//import com.iisysgroup.poslib.deviceinterface.DeviceState
//import com.iisysgroup.poslib.host.entities.TransactionResult
//import com.iisysgroup.poslib.utils.AccountType
//import com.itex.richard.payviceconnect.model.Genesis
//import com.itex.richard.payviceconnect.wrapper.PayviceServices
//import io.reactivex.Observer
//import io.reactivex.android.schedulers.AndroidSchedulers
//import io.reactivex.disposables.Disposable
//import io.reactivex.schedulers.Schedulers
//import kotlinx.android.synthetic.main.activity_movies_list.*
//import org.jetbrains.anko.alert
//import org.jetbrains.anko.indeterminateProgressDialog
//import org.jetbrains.anko.okButton
//import org.jetbrains.anko.toast
//import java.util.*
//
//
//class MoviesList : AppCompatActivity() {
//    object KEY : Intent() {
//        var INTENT_RESULT_CODE = 20334
//    }
//
//    var isCard: Boolean = false
//    lateinit var transactionResult: TransactionResult
//    lateinit var mRrn: String
//
//    lateinit var Ac_movie: Genesis.Movies
//
//    private val mProgressDialog by lazy {
//        indeterminateProgressDialog("Processing")
//    }
//    private val mPayvicePin by lazy {
//        PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.key_payvice_wallet_pin), "")
//    }
//
//    private val mWalletUsername by lazy {
//        SharedPreferenceUtils.getPayviceUsername(this)
//    }
//
//    private val mWalletId by lazy {
//        SharedPreferenceUtils.getPayviceWalletId(this)
//    }
//
//    private val mWalletPassword by lazy {
//        SharedPreferenceUtils.getPayvicePassword(this)
//    }
//
//    private val payviceServices by lazy {
//        PayviceServices.getInstance(this)
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_movies_list)
//
//        val payviceServices = PayviceServices.getInstance(this)
//        payviceServices.GenesisGetMovies(Genesis.GetMoviesRequest("${intent.getStringExtra("house_no")}"))
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(object : Observer<Genesis.GenesisResponses> {
//                    override fun onComplete() {
//                    }
//
//                    override fun onSubscribe(d: Disposable) {
//                    }
//
//                    override fun onNext(t: Genesis.GenesisResponses) {
//                        setUpRecyclerView(t.movies!!)
//                    }
//
//                    override fun onError(e: Throwable) {
//                        Log.i("okh", e.message)
//                    }
//                })
//    }
//
//    private fun setUpRecyclerView(movies: List<Genesis.Movies>) {
//        val adapter = MovieListAdapter(movies, this, object : MovieListAdapter.OnMovieSelectedListener {
//            override fun startWalletPayment(movies: Genesis.Movies) {
//                payWithWallet(movies)
//            }
//
//            override fun startCardPayment(movies: Genesis.Movies) {
//                payWithCard(movies)
//            }
//
//        })
//        recyclerView.adapter = adapter
//        val mLinearLayoutManagerVertical = GridLayoutManager(this, 2) // (Context context, int spanCount)
//        recyclerView.layoutManager = mLinearLayoutManagerVertical
//    }
//
//    private fun payWithWallet(movie: Genesis.Movies) {
//        isCard = false
//        mProgressDialog.show()
//        payviceServices.GenesisBuyTickets(Genesis.BuyTicketRequest(movie.movie_id, mWalletUsername, mWalletId, mPayvicePin))
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(object : Observer<Genesis.GenesisResponses> {
//                    override fun onComplete() {
//                    }
//
//                    override fun onSubscribe(d: Disposable) {
//                    }
//
//                    override fun onNext(t: Genesis.GenesisResponses) {
//                        completeTransaction(t, movie)
//                    }
//
//                    override fun onError(e: Throwable) {
//                        mProgressDialog.dismiss()
//                        val map = HashMap<String, String>()
//                        map["Error Message"] = e.message!!
//                        alert {
//                            title = "Transaction Status"
//                            message = "Error : " + e.message
//                            positiveButton("Print") { _ -> print(map, "Declined", movie.amount.toString()) }
//                        }
//                    }
//
//                })
//    }
//
//    private fun completeTransaction(t: Genesis.GenesisResponses, movie: Genesis.Movies) {
//        val map = HashMap<String, String>()
//
//        if (isCard) {
//            map["PAN"] = transactionResult.PAN.toString()
//            map["Card Holder"] = transactionResult.cardHolderName.toString()
//            map["Expiry Date"] = transactionResult.cardExpiry.toString()
//            map["Authorizaion Code"] = transactionResult.authID
//            map["RRN"] = transactionResult.RRN
//        }
//
//
//        mProgressDialog.dismiss()
//        if (!t.error) {
//            map["Movie Title"] = t.title!!
//            map["Tickets No"] = t.ticket_id!!
//            map["Screen"] = t.screen!!
//            map["Movie Date"] = movie.start_date
//            map["Movie Time"] = movie.start_time
//            map["Movie Duration"] = movie.duration
//            if(isCard){
//                map["Payment Method"] = "Card"
//            }
//            else{
//                map["Payment Method"] = "Cash"
//
//            }
//            alert {
//                title = "Transaction Status"
//                message = "Transaction Approved"
//                positiveButton(buttonText = "Print") { _ -> print(map, "Approved", movie.amount) }
//            }.show()
//        } else {
//            alert {
//                title = "Transaction Status"
//                message = "Transaction Declined"
//                positiveButton(buttonText = "Finish") { _ -> print(map, "Declined", movie.amount) }
//            }.show()
//        }
//
//    }
//
//    private fun payWithCard(movie: Genesis.Movies) {
//        isCard = true
//        Ac_movie = movie
//        val intent = Intent(this, VasPurchaseProcessor::class.java)
//        intent.putExtra(BasePaymentActivity.TRANSACTION_ACCOUNT_TYPE, AccountType.DEFAULT_UNSPECIFIED)
//        //times 100 because of the conversion to kobo
//        val amount = movie.amount.toLong() * 100
//        intent.putExtra(BasePaymentActivity.TRANSACTION_AMOUNT, 200L)
//        intent.putExtra(BasePaymentActivity.TRANSACTION_ADDITIONAL_AMOUNT, 0L)
//
//        if (SharedPreferenceUtils.getIsTerminalPrepped(this)){
//            ActivityCompat.startActivityForResult(this, intent, KEY.INTENT_RESULT_CODE, null)
//        } else {
//            alert {
//                isCancelable = false
//                title = "Terminal not configured"
//                message = "Click O.K to go to configuration page"
//                okButton {
//                    startActivity(Intent(this@MoviesList, TermMagmActivity::class.java))
//
//                }
//            }.show()
//        }
//
//
//    }
//
//    private fun print(map: HashMap<String, String>, status: String, amount: String) {
//
//        val date = Calendar.getInstance().time.toString()
//        val receiptModel = ReceiptModel(date, "Genesis Movie Purchase", status, map, amount, status)
//        val intent = Intent(this@MoviesList, PrintActivity::class.java)
//        intent.putExtra("print_map", receiptModel)
//        startActivity(intent)
//        finish()
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        when (requestCode) {
//            KEY.INTENT_RESULT_CODE -> when (resultCode) {
//                Activity.RESULT_OK -> {
//                    val state = data?.getSerializableExtra("state") as DeviceState
//                    mRrn = data?.getStringExtra("rrn")
//
//                    when (state) {
//                        DeviceState.APPROVED -> {
//                            (application as App).db.transactionResultDao.get(mRrn).observe({ lifecycle }) {
//                                transactionResult = it!!
//
//                                mProgressDialog.show()
//                                payviceServices.GenesisBuyTickets(Genesis.BuyTicketRequest(Ac_movie.movie_id, mWalletUsername, mWalletId, mPayvicePin, amo))
//                                        .subscribeOn(Schedulers.io())
//                                        .observeOn(AndroidSchedulers.mainThread())
//                                        .subscribe(object : Observer<Genesis.GenesisResponses> {
//                                            override fun onComplete() {
//                                            }
//
//                                            override fun onSubscribe(d: Disposable) {
//                                            }
//
//                                            override fun onNext(t: Genesis.GenesisResponses) {
//                                                completeTransaction(t, Ac_movie)
//                                            }
//
//                                            override fun onError(e: Throwable) {
//                                                mProgressDialog.dismiss()
//                                                val map = HashMap<String, String>()
//                                                map["Error Message"] = e.message!!
//                                                alert {
//                                                    title = "Transaction Status"
//                                                    message = "Error : " + e.message
//                                                    positiveButton("Print") { _ -> print(map, "Declined", Ac_movie.amount) }
//                                                }
//                                            }
//
//                                        })
//                            }}
//                            DeviceState.DECLINED, DeviceState.FAILED -> {
//                                gotoHome()
//                                toast("Transaction declined")
//                            }
//                        }
//
//                    }
//                }
//            }
//        }
//
//    private fun gotoHome() {
//        val intent = Intent(this@MoviesList, VasActivity::class.java)
//        finish()
//        startActivity(intent)
//    }
//
//    }
//
//
