package com.iisysgroup.androidlite

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import com.iisysgroup.androidlite.history_summary.EODActivity
import com.iisysgroup.androidlite.history_summary.TransactionsMenu
import com.iisysgroup.androidlite.login.LoginActivity
import com.iisysgroup.androidlite.login.securestorage.SecureStorage
import com.iisysgroup.androidlite.payments_menu.PaymentsActivity
import com.iisysgroup.androidlite.utils.SharedPreferenceUtils
import com.iisysgroup.payvice.securestorage.SecureStorageUtils
import com.iisysgroup.poslib.host.entities.ConnectionData
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var btn_payment: Button
    private lateinit var btn_settings: Button
    private lateinit var btn_termManagement: Button
    private lateinit var btn_report: Button
    private lateinit var btn_eod: Button
    private lateinit var btn_signOut: Button
    private lateinit var application: App

    private lateinit var toolbar: Toolbar

    private lateinit var button : Button



    private val mUsername by lazy {
        SharedPreferenceUtils.getPayviceUsername(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)


        initializeConnData()
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        application = App()

        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false)

        btn_payment = findViewById(R.id.btn_payment)
        btn_settings = findViewById(R.id.btn_settings)
        btn_termManagement = findViewById(R.id.btn_termManagement)
        btn_report = findViewById(R.id.btn_report)
        btn_eod = findViewById(R.id.btn_eod)
        btn_signOut = findViewById(R.id.btn_signOut)
        btn_signOut.text = "Sign-out - $mUsername"

        btn_settings.setOnClickListener(this)
        btn_payment.setOnClickListener(this)
        btn_termManagement.setOnClickListener(this)
        btn_report.setOnClickListener(this)
        btn_signOut.setOnClickListener(this)
        btn_eod.setOnClickListener(this)

    }

    private fun initializeConnData() {

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn_payment -> startActivity(Intent(this, PaymentsActivity::class.java))
            R.id.btn_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.btn_termManagement -> startActivity(Intent(this, TermMagmActivity::class.java))
            R.id.btn_report -> startActivity(Intent(this, TransactionsMenu::class.java))
            R.id.btn_eod -> startActivity(Intent(this, EODActivity::class.java))
            R.id.btn_signOut -> signOut()
        }
    }

    private fun signOut() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.edit().clear().apply()
        SecureStorage.deleteAll()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
