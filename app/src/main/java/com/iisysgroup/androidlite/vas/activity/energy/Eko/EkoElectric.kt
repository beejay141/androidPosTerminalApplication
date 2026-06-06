package com.iisysgroup.androidlite.vas.activity.energy.Eko

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View

import com.iisysgroup.androidlite.R
import kotlinx.android.synthetic.main.activity_eko_electric.*
import kotlinx.android.synthetic.main.content_energy.*

class EkoElectric : AppCompatActivity() {

    val view by lazy {
        View(this)
    }
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId){
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eko_electric)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        prepaid.setOnClickListener { startActivity(Intent(this@EkoElectric, EkoPrepaid::class.java)) }

        postpaid.setOnClickListener { startActivity(Intent(this@EkoElectric, EkoPostpaid::class.java)) }

    }
}
