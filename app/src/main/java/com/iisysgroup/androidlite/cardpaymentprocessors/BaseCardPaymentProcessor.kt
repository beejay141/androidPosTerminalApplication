package com.iisysgroup.androidlite.cardpaymentprocessors

import android.app.Activity
import android.arch.lifecycle.LiveDataReactiveStreams
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.iisysgroup.androidlite.App
import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.utils.SharedPreferenceUtils
import com.iisysgroup.poslib.deviceinterface.DeviceState
import com.iisysgroup.poslib.deviceinterface.interactors.EmvInteractor
import com.iisysgroup.poslib.host.HostInteractor
import com.iisysgroup.poslib.host.entities.ConfigData
import com.iisysgroup.poslib.host.entities.ConnectionData
import com.iisysgroup.poslib.host.entities.KeyHolder
import com.pax.PaxDevice
import com.pax.app.Models.PaxConfigData
import com.pax.app.Models.ResultEvent
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.insert_card.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.contentView
import org.jetbrains.anko.toast
import com.pax.jemv.device.DeviceManager
import com.pax.tradepaypw.DeviceImplNeptune

abstract class BaseCardPaymentProcessor : AppCompatActivity() {


    //todo this should not be compulsory - users can decide to use default UI
    abstract fun initializeDefaultUI() : DefaultUIModel


    private lateinit var uiManager : DefaultUIManager

    lateinit var skDevice : PaxDevice
    lateinit var configData : ConfigData
    lateinit var keyHolder : KeyHolder
    lateinit var paxConfigData : PaxConfigData
    lateinit var mEmvInteractor : EmvInteractor
    lateinit var mConnectionData: ConnectionData
    lateinit var mHostInteractor: HostInteractor

    lateinit var mUIModel : DefaultUIModel

    private var mRrn : String = ""
    public var pinKey : String = ""
    public var sessionKey : String = ""
    var cardSeen: Boolean = false;

    public val mDb by lazy {
        (application as App).db
    }

    public val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_process)


        DeviceManager.getInstance().setIDevice(DeviceImplNeptune.getInstance())

        PaxDevice.register(this);

        //(application as App).db.configDataDao.get()


        mUIModel = initializeDefaultUI()
        setUpDeviceInteractor()
        setDeviceStatusObserver()

        setUpHostInteractor()
        setUpConnectionData()

    }

    private fun setUpDeviceInteractor() {

        skDevice = PaxDevice(this)
        mEmvInteractor = EmvInteractor.getInstance(skDevice)
    }

    private fun setUpHostInteractor() {
        mHostInteractor = (application as App).hostInteractor
    }

    private fun setUpConnectionData() {
        val terminalId = SharedPreferenceUtils.getTerminalId(this)
        val ipAddress = SharedPreferenceUtils.getIpAddress(this)
        val ipPort = SharedPreferenceUtils.getPort(this)
        val isSSL = SharedPreferenceUtils.isSsl(this)

        when {
            terminalId.isNullOrEmpty() -> {
                toast("Enter valid terminal Id")
                return
            }
            ipAddress.isNullOrEmpty() -> toast("Enter valid IP Address")
            ipPort.toString().isNullOrEmpty() -> toast("Enter valid IP Port")
        }

        mConnectionData = ConnectionData(terminalId, ipAddress, ipPort.toInt(), isSSL)
    }

    fun setTransactionRrn(rrn : String){
        this.mRrn = rrn
    }


    open fun setDeviceStatusObserver(){
        uiManager = DefaultUIManager(contentView!!, mUIModel)

//        LiveDataReactiveStreams.fromPublisher(mEmvInteractor.observeStatus()
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread()))
//                .observe({ lifecycle }){
//                    it?.let{
//                        val intent = Intent()
//                        intent.putExtra("state", it.state)
//
//                        when {
//                            it.state == DeviceState.DECLINED -> {
//                                mHostInteractor.rollBackTransaction().subscribe { transactionResult ->
//                                    Log.d("OkHRollback", transactionResult.toString())
//                                    intent.putExtra("rrn", transactionResult.RRN)
//                                    setResult(Activity.RESULT_OK, intent)
//                                    finish()
//                                }
//
//                            }
//                            it.state == DeviceState.FAILED -> {
//
//                                toast("Failed")
//                                intent.putExtra("rrn", mRrn)
//                                setResult(Activity.RESULT_OK, intent)
//                                finish()
//                            }
//
//                            it.state == DeviceState.REMOVE_CARD -> {
//
//                                toast("Removed Card")
//                                Log.d("skret","remove")
//                                intent.putExtra("rrn", mRrn)
//                                setResult(Activity.RESULT_OK, intent)
//                                finish()
//                            }
//
//                            it.state == DeviceState.AWAITING_ONLINE_RESPONSE -> {
//                                Log.d("OkH", "Awaiting online response")
//                            }
//
//                            it.state == DeviceState.APPROVED -> {
//                                intent.putExtra("rrn", mRrn)
//                                setResult(Activity.RESULT_OK, intent)
//                                finish()
//                            }
//                        }
//
//                        Log.d("UI_STATE", it.state.toString())
//                        uiManager.setState(it.state)
//                    }
//                }


    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReportEvent(event: ResultEvent) {
        val intent = Intent()

        when (event.status as ResultEvent.Status) {
            ResultEvent.Status.REMOVE_CARD -> {
                transaction_action.text = "PLEASE REMOVE CARD !!!"
                toast("PLEASE REMOVE CARD !!!")
                intent.putExtra("state", DeviceState.REMOVE_CARD)
                intent.putExtra("rrn", mRrn)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
            ResultEvent.Status.DECLINED -> {
//                mHostInteractor.rollBackTransaction().subscribe { transactionResult ->
//                    Log.d("OkHRollback-declined", transactionResult.toString())
//
//
//                }
                Log.d("OkHRollback-declined", mRrn)
                intent.putExtra("state", DeviceState.DECLINED)
                intent.putExtra("rrn", mRrn);
                setResult(Activity.RESULT_OK, intent)
                uiManager.setState(DeviceState.DECLINED)
                finish()
            }
            ResultEvent.Status.AWAITING_ONLINE_RESPONSE -> {
                Log.d("OkH", "Awaiting online response")
                intent.putExtra("state", DeviceState.AWAITING_ONLINE_RESPONSE)
                uiManager.setState(DeviceState.AWAITING_ONLINE_RESPONSE,"Awaiting online response")
            }
            ResultEvent.Status.APPROVED -> {
                Log.d("OkHRollback-approved", mRrn)
                intent.putExtra("state", DeviceState.APPROVED)
                intent.putExtra("rrn", mRrn)
                setResult(Activity.RESULT_OK, intent)
                uiManager.setState(DeviceState.APPROVED)
                finish()
            }
            ResultEvent.Status.INFO ->{
                intent.putExtra("state", DeviceState.INFO)
                uiManager.setState(DeviceState.INFO, event.data.toString())
            }
        }
    }

    override fun onStop() {
        stopService(skDevice.iDectect)
        super.onStop()
        Log.d("tag","finish")
//        paxDevice.stopDetector();
    }


    override fun onDestroy(){

        unregisterReceiver(skDevice.broadcastReceiver)
        super.onDestroy()
    }
}





//        Single.fromCallable<ConfigData>{
//            mDb.configDataDao.get()
//        }.subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({
//
//                    when{
//                        it.equals(null) ->{
//                            toast("Download POS Parmeter failed")
//                        }
//                    }
//                    configData = it
//                    paxConfigData = PaxConfigData()
//                    paxConfigData.TerminalId = terminalId
//                    paxConfigData.key03015 = configData.getConfigData("03015").toString()
//                    paxConfigData.key05003 = configData.getConfigData("05003").toString()
//                    paxConfigData.key06003 = configData.getConfigData("06003").toString()
//                    paxConfigData.key08004 = configData.getConfigData("08004").toString()
//                    paxConfigData.key52040 = configData.getConfigData("52040").toString()
//
//                    skDevice.configData = paxConfigData;
//
//                },{
//                    Log.d("PaxDevice",it.message)
//                })
//
//        Single.fromCallable<KeyHolder>{
//            mDb.keyHolderDao.get()
//        }.subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({
//
//                    when{
//                        it.equals(null) ->{
//                            toast("Download POS Parmeter failed")
//                        }
//                    }
//                    keyHolder = it
//                    //do pin injection here
//                    val  hostString = getString(R.string.key_host_type)
//
//                    val hostKey = sharedPreferences.getString(hostString, "")
//                    val host = when (hostKey) {
//
//                        "POSVAS" -> {
//                            var clearMk = PosvasKeyProcessor.getMasterKey(it.masterKey,it.isTestPlatform);
//                            var clearPk = PosvasKeyProcessor.decryptKey(it.pinKey,clearMk);
//                            skDevice.clearPinkey = clearPk;
//                            SKDevice.WriteKey(clearMk,clearPk);
//                        }
//                        else -> {
//                            var clearMk = GtmsKeyProcessor.getMasterKey(it.masterKey,it.isTestPlatform);
//                            var clearPk = GtmsKeyProcessor.decryptKey(it.pinKey,clearMk);
//                            skDevice.clearPinkey = clearPk;
//                            SKDevice.WriteKey(clearMk,clearPk);
//                        }
//                    }
//
//                },{
//                    Log.d("PaxDevice",it.message)
//                })
