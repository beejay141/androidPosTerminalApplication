package com.iisysgroup.androidlite

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.iisysgroup.androidlite.db.VasTerminalService
import com.iisysgroup.androidlite.utils.SharedPreferenceUtils
import com.iisysgroup.poslib.ISO.GTMS.GtmsKeyProcessor
import com.iisysgroup.poslib.ISO.POSVAS.PosvasKeyProcessor
import com.iisysgroup.poslib.host.Host
import com.iisysgroup.poslib.host.HostInteractor
import com.iisysgroup.poslib.host.dao.PosLibDatabase
import com.iisysgroup.poslib.host.entities.ConfigData
import com.iisysgroup.poslib.host.entities.ConnectionData
import com.iisysgroup.poslib.host.entities.KeyHolder
import com.iisysgroup.poslib.host.entities.VasTerminalData
import com.pax.PaxDevice
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_term_magm.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.okButton
import org.jetbrains.anko.toast


class TermMagmActivity : AppCompatActivity() {


    object TerminalUtils {

        fun isTerminalPrepped(context: Context, application : App) : Boolean {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

            val isValidSettings = when {
                !sharedPreferences.contains(context.getString(R.string.key_terminal_id)) ||
                        !sharedPreferences.contains(context.getString(R.string.key_ip_address)) ||
                        !sharedPreferences.contains(context.getString(R.string.key_ip_address)) ||
                        !sharedPreferences.contains(context.getString(R.string.key_pref_port_type)) ||
                        !sharedPreferences.contains(context.getString(R.string.key_host_type)) -> false
                else -> true
            }

            if (!isValidSettings){
                return false
            } else {
                (application).db.configDataDao.get() ?: return false
            }

            return true

        }
    }

    private fun SharedPreferences.isSSL() : Boolean {
        return when (this.getString(getString(R.string.key_host_type), "")){
            "" -> false
            "SSL" -> true
            else -> true
        }
    }

    private val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    private val connectionData by lazy {
        val terminal_id = sharedPreferences.getString(getString(R.string.key_terminal_id), null)
        val ip_address = sharedPreferences.getString(getString(R.string.key_ip_address), null)
        val ip_port = Integer.parseInt(sharedPreferences.getString(getString(R.string.key_pref_port), null))
        val isSSL = sharedPreferences.isSSL()

        ConnectionData(terminal_id, ip_address, ip_port, isSSL)
    }

    val hostInteractor: HostInteractor by lazy {
        (application as App).hostInteractor
    }

//

    fun displayError(e: Exception){
        progressDialog.dismiss()
        alert.setTitle("Error")
        alert.setMessage(e.message)
        alert.show()

    }

    val alert by lazy{
        AlertDialog.Builder(this)
                .setTitle(null)
                .setMessage(null)
                .create()
    }

    val db by lazy {
        (application as App).db
    }

    val progressDialog by lazy {
        ProgressDialog(this).apply { setCancelable(false) }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_term_magm)



        setSupportActionBar(toolbar)

        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
        }


        configureTerminalBtn.setOnClickListener {
            configureTerminal()
        }
        downloadParameterBtn.setOnClickListener {
            downloadParameters()
        }





        if (!isValidConnectionSettings())
        {
            alert {
                title = getString(R.string.app_name)
                message = "No settings set. Press OK to update your settings"
                isCancelable = false
                okButton {
                    startActivity(Intent(this@TermMagmActivity, SettingsActivity::class.java))
                }
            }.show()
        }
    }

    private fun isValidConnectionSettings() = when {
        !sharedPreferences.contains(getString(R.string.key_terminal_id)) ||
                !sharedPreferences.contains(getString(R.string.key_ip_address)) ||
                !sharedPreferences.contains(getString(R.string.key_ip_address)) ||
                !sharedPreferences.contains(getString(R.string.key_pref_port_type)) ||
                !sharedPreferences.contains(getString(R.string.key_host_type)) -> false
        else -> true
    }


    private fun downloadParameters()
    {
        launch(CommonPool){
            val keyHolder = db.keyHolderDao.get()

            if (keyHolder == null)
            {
                launch(UI){
                    alert {
                        title = "Error"
                        message = "Invalid keys. Press OK to reconfigure terminal"
                        isCancelable = false
                        okButton { configureTerminal()
                        }
                    }.show()
                }
            }
            else {
                launch(UI){
                    progressDialog.setMessage("Now downloading parameters")
                    progressDialog.show()
                }


                hostInteractor.getConfigData(connectionData, keyHolder)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {configData: ConfigData?, throwable: Throwable? ->
                            throwable?.let {

                                launch(UI){
                                    alert {
                                        title = "Error"
                                        message = "Invalid keys. Press OK to reconfigure terminal"
                                        isCancelable = false
                                        okButton { configureTerminal()
                                        }
                                    }.show()

                                }
                                return@let
                            }

                            configData?.let {
                                doAsync {
                                    db.configDataDao.save(it)
                                    Log.d("OkH", it.data.toString())
                                    SharedPreferenceUtils.setIsTerminalPrepped(this@TermMagmActivity, true)
                                }
                                launch(UI){
                                    progressDialog.dismiss()
                                    alert {
                                        title = "Success"
                                        message = "Device configured successfully"
                                        okButton { }
                                    }.show()
                                }

                            }
                        }
            }
        }
    }

    private fun getVasTerminalService() : Single<VasTerminalData>{
        return VasTerminalService.Factory.getService().getVasTerminalDetails()
    }



    private fun configureTerminal() {
        progressDialog.setMessage("Getting keys")
        progressDialog.show()













        getVasTerminalService()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe {
                    terminalData, error ->

                    terminalData?.let {
                        launch {
                            (application as App).db.vasTerminalDataDao.save(it)
                        }


                        val liveKeyHolder = hostInteractor.getKeyHolder(connectionData)
                        liveKeyHolder.subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe {keyHolder: KeyHolder?, throwable: Throwable? ->
                                    keyHolder?.let {
                                        progressDialog.setMessage("Configuring terminal")
                                        Log.d("keyHolder",keyHolder.toString())

                                        val isTest = when(sharedPreferences.getString(getString(R.string.key_platform), "").toLowerCase()){
                                            "test" -> true
                                            else -> false
                                        }

                                        if (isTest)
                                            it.isTestPlatform = true

                                        async {
                                            db.keyHolderDao.save(it)
                                        }

                                        val  hostString = getString(R.string.key_host_type)
                                        val hostKey = sharedPreferences.getString(hostString, "")
                                        val host = when (hostKey) {
                                            "POSVAS" -> {

                                                Log.d("pinkey-tmk",it.masterKey);
                                                var clearMk = PosvasKeyProcessor.getMasterKey(keyHolder.masterKey,it.isTestPlatform);

                                                Log.d("pinkey",it.isTestPlatform.toString());
                                                Log.d("pinkey",clearMk);

                                                var writeKey = PaxDevice.InjectKeys(clearMk, it.pinKey)
                                                Log.d("write-key",writeKey.toString())

                                            }
                                            else -> {
                                                var clearMk = GtmsKeyProcessor.getMasterKey(it.masterKey,it.isTestPlatform);

                                                var writeKey = PaxDevice.InjectKeys(clearMk, it.pinKey)
                                                Log.d("write-key",writeKey.toString())

                                            }
                                        }

                                         Log.d("connectionData",connectionData.toString())
                                        hostInteractor.getConfigData(connectionData, it)
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribeOn(Schedulers.io())
                                                .subscribe { configData , throwable ->
                                                    configData?.let {

                                                        doAsync {
                                                            db.configDataDao.save(configData)
                                                            SharedPreferenceUtils.setIsTerminalPrepped(this@TermMagmActivity, true)
                                                        }

                                                        progressDialog.dismiss()
                                                        alert{
                                                            title = "Success"
                                                            message = "Device configured successfully"
                                                        }.show()
                                                    }

                                                    throwable?.let {
                                                        progressDialog.dismiss()
                                                        displayError(it as Exception)
                                                    }
                                                }
                                    }


                                    throwable?.let {
                                        toast("Error")
                                        displayError(it as Exception)
                                    }
                                }

                    }

                    error?.let {
                        alert {
                            title = "Error"
                            message = it.message.toString()
                            okButton {  }
                        }.show()
                    }
                }





        /*
        *
       Single.fromCallable {
            var client = OkHttpClient()
            val mediaType = MediaType.parse("application/json")
            val body = RequestBody.create(mediaType, "{\"oem\" : \"AMP\",\"model\" : \"AMP8000\",\"sn\" : \"${deviceManager.device.deviceInfo.sn}\",\"version\" : \"1.0.2\"}");
            Log.i("okh",body.toString())
            var request = Request.Builder()
                   // .url("https://197.253.19.75/tams/eftpos/op/pos.php")
                    .url("http://197.253.19.75/tams/eftpos/op/pos.php")
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();
              var res = client.newCall(request).execute()
              var resString = res.body()!!.string()
              Gson().fromJson(resString, GetTidModel.MainModel::class.java)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.i("okh", it.toString())
                    Log.i("okh", it.getTid()+"")
                    Log.i("okh seriel", deviceManager.device.deviceInfo.sn)
                    if(it.getError()!!){
                        progressDialog.dismiss()
                        alert{
                            title = "Error"
                            message  = "TID was not found for the device with Seriel Number: ${deviceManager.device.deviceInfo.sn} \n Please call customer service"
                            okButton {
                                this@TermMagmActivity.finish()
                            }
                        }.show()
                    }else{
                        var editor = sharedPreferences.edit()
                        editor.putString(getString(R.string.key_terminal_id), it.getTid())
                        editor.apply()
                        progressDialog.setMessage("Getting keys")
                        getVasTerminalService()
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
                                .subscribe {
                                    terminalData, error ->

                                    terminalData?.let {
                                        GlobalScope.launch {
                                            (application as App).db.vasTerminalDataDao.save(it)
                                        }


                                        val liveKeyHolder = hostInteractor.getKeyHolder(connectionData)
                                        liveKeyHolder.subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe {keyHolder: KeyHolder?, throwable: Throwable? ->
                                                    keyHolder?.let {
                                                        progressDialog.setMessage("Configuring terminal")

                                                        val isTest = when(sharedPreferences.getString(getString(R.string.key_platform), "").toLowerCase()){
                                                            "test" -> true
                                                            else -> false
                                                        }

                                                        if (isTest)
                                                            it.isTestPlatform = true

                                                        GlobalScope.async {
                                                            db.keyHolderDao.save(it)
                                                            SecureStorage.store("mainkey", keyHolder.masterKey)
                                                            SecureStorage.store("pinkey", keyHolder.pinKey)
                                                            Log.d("keys", keyHolder.masterKey + " "+ keyHolder.pinKey)
                                                        }

                                                        hostInteractor.getConfigData(connectionData, it)
                                                                .observeOn(AndroidSchedulers.mainThread())
                                                                .subscribeOn(Schedulers.io())
                                                                .subscribe { configData , throwable ->
                                                                    configData?.let {

                                                                        doAsync {
                                                                            db.configDataDao.save(configData)
                                                                            SharedPreferenceUtils.setIsTerminalPrepped(this@TermMagmActivity, true)
                                                                        }

                                                                        progressDialog.dismiss()
                                                                        alert{
                                                                            title = "Success"
                                                                            message = "Device configured successfully"
                                                                        }.show()
                                                                    }

                                                                    throwable?.let {
                                                                        progressDialog.dismiss()
                                                                        displayError(it as Exception)
                                                                    }
                                                                }
                                                    }


                                                    throwable?.let {
                                                        toast("Error")
                                                        displayError(it as Exception)
                                                    }
                                                }

                                    }

                                    error?.let {
                                        progressDialog.dismiss()
                                        alert {
                                            title = "Error"
                                            message = it.message.toString()
                                            okButton {  }
                                        }.show()
                                    }
                                }
                    }
                },{
                    it.printStackTrace()
                    progressDialog.dismiss()
                    alert{
                        message = "Unknown Error occured"
                        okButton {
                            progressDialog.dismiss()
                            it.dismiss()
                        }
                    }.show()
                })*/

        }
    }