package com.iisysgroup.androidlite

import android.app.Application
import android.arch.persistence.room.Room
import android.preference.PreferenceManager
import android.support.multidex.MultiDex
import android.util.Log
import android.view.View
//import com.google.firebase.analytics.FirebaseAnalytics
import com.iisysgroup.androidlite.db.BeneficiariesDatabase
import com.iisysgroup.androidlite.login.securestorage.SecureStorage
import com.iisysgroup.poslib.ISO.GTMS.GtmsHost
import com.iisysgroup.poslib.ISO.POSVAS.PosvasHost
import com.iisysgroup.poslib.TAMS.TamsHost
import com.iisysgroup.poslib.host.Host
import com.iisysgroup.poslib.host.HostInteractor
import com.iisysgroup.poslib.host.dao.PosLibDatabase
import com.iisysgroup.poslib.host.entities.ConfigData
import com.pax.PaxDevice
import com.pax.app.Models.ResultEvent
import com.pax.app.PaxModuleApplication
import com.pax.tradepaypw.utils.FileParse
import net.danlew.android.joda.JodaTimeAndroid
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.toast


/**
 * Created by Agbede on 2/26/2018.
 */
class App : Application() {


    init {
        System.loadLibrary("DeviceConfig")
        System.loadLibrary("DCL")
        System.loadLibrary("iconv")
        System.loadLibrary("IGLBarDecoder")
        System.loadLibrary("ZBarDecoder")

        System.loadLibrary("F_DEVICE_LIB_PayDroid")
        System.loadLibrary("F_PUBLIC_LIB_PayDroid")
        System.loadLibrary("F_EMV_LIB_PayDroid")
        System.loadLibrary("F_ENTRY_LIB_PayDroid")
        System.loadLibrary("F_MC_LIB_PayDroid")
        System.loadLibrary("F_WAVE_LIB_PayDroid")
        System.loadLibrary("F_AE_LIB_PayDroid")
        System.loadLibrary("F_QPBOC_LIB_PayDroid")
        System.loadLibrary("F_DPAS_LIB_PayDroid")
        System.loadLibrary("F_JCB_LIB_PayDroid")
        System.loadLibrary("F_PURE_LIB_PayDroid")
        System.loadLibrary("JNI_EMV_v100")
        System.loadLibrary("JNI_ENTRY_v100")
        System.loadLibrary("JNI_MC_v100")
        System.loadLibrary("JNI_WAVE_v100")
        System.loadLibrary("JNI_AE_v100")
        System.loadLibrary("JNI_QPBOC_v100")
        System.loadLibrary("JNI_DPAS_v100")
        System.loadLibrary("JNI_JCB_v100")
        System.loadLibrary("JNI_PURE_v100")
    }

      var  hostKey: String  = ""


    val TAG = "app_pit_ogl"

//    lateinit var skDevice : SKDevice

    val db by lazy {
        Room.databaseBuilder(this, PosLibDatabase::class.java, "poslib.db")
                .fallbackToDestructiveMigration()
                .build()
    }

    val beneficiariesDatabase by lazy {
        Room.databaseBuilder(this, BeneficiariesDatabase::class.java, "beneficiaries.db")
                .fallbackToDestructiveMigration()
                .build()
    }

    private val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

//    val hostInteractor: HostInteractor by lazy {
//        val  hostString = getString(R.string.key_host_type)
//
//       hostKey = sharedPreferences.getString(hostString, "")
//
//        Log.d("host","hostttt"+hostKey)
//
//
//        val host = when (hostKey) {
//
//            "TAMS" -> TamsHost(this)
//            "POSVAS" -> PosvasHost(this)
//            else -> GtmsHost(this)
//        }
//
//        HostInteractor.getInstance(host)
//    }

    lateinit var hostInteractor: HostInteractor


    override fun onCreate() {
        super.onCreate()

        MultiDex.install(this)

        PaxDevice.register(this);

        JodaTimeAndroid.init(this)

        SecureStorage.init(this)
                .setEncryptionMethod(SecureStorage.Builder.EncryptionMethod.ENCRYPTED)
                .setPassword("4321dcbA")
                .setStoreName(TAG)
                .build()


        PaxModuleApplication.initPaxModule(applicationContext);

        FileParse.parseAidFromAssets(this, "aid.ini")

        FileParse.parseCapkFromAssets(this, "capk.ini")


        hostInteractor = setupHost();

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReportEvent(event: ResultEvent) {
        when (event.status as ResultEvent.Status) {
            ResultEvent.Status.CONNECT_SUCCESS -> {
                toast("CONNECTED!!!")
//                SKDevice.downloadFile(this@App)
            }
            ResultEvent.Status.CONNECT_FAILED -> {
                toast("CONNECTED FAILED!!!")
            }
            ResultEvent.Status.DOWNLOAD_SUCCESS -> {
                toast("FILED DOWNLOADED!!!")
            }
            ResultEvent.Status.DOWNLOAD_FAILED -> {
                toast("FILED DOWNLOADED FAILED!!!")
            }
            else -> {
            }
        }
    }


    fun setupHost() : HostInteractor{
        val  hostString = getString(R.string.key_host_type)

        Log.d("interact","hostttt"+ hostString)

        hostKey = sharedPreferences.getString(hostString, "")

        Log.d("interact","hostttt" + hostKey)


        val host = when (hostKey) {

            "TAMS" -> TamsHost(this)
            "POSVAS" -> PosvasHost(this)
            else -> GtmsHost(this)
        }

        return HostInteractor.getInstance(host)
    }


}