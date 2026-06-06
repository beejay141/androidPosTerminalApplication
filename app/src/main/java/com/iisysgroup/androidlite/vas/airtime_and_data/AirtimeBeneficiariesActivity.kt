package com.iisysgroup.androidlite.vas.airtime_and_data

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.iisysgroup.androidlite.App
import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.db.AirtimeModel
import com.iisysgroup.androidlite.vas.adapter.AirtimeBeneficiariesAdapter
import kotlinx.android.synthetic.main.activity_beneficiaries.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton
import java.util.*

class AirtimeBeneficiariesActivity : AppCompatActivity(), AirtimeBeneficiariesAdapter.DataClickListener {
    override fun onDataItemClick(data: ArrayList<AirtimeModel>, position: Int) {
        alert {
            title = "Transaction"
            message = "Do you want to make an airtime transaction for ${data[position].phone_number}?"
            okButton { passDataToAirtimeActivity(data[position]) }
        }.show()
    }

    private fun passDataToAirtimeActivity(airtimeModel: AirtimeModel) {
        val intent = Intent(this@AirtimeBeneficiariesActivity, AirtimeActivity::class.java)
        intent.putExtra("airtimeModel", airtimeModel)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beneficiaries)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Airtime Beneficiaries"
        progressBar.visibility = View.VISIBLE

        setUpRecyclerView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return false
    }

    private fun setUpRecyclerView() {
        val layoutManager = GridLayoutManager(this@AirtimeBeneficiariesActivity, 1, GridLayoutManager.VERTICAL, false)
        airtime_beneficiaries_recyclerview.layoutManager = layoutManager

        launch(CommonPool){
            val db = (application as App).beneficiariesDatabase
            val airtimeDao = db.getAirtimeBeneficiariesDao()
            val beneficiaries = airtimeDao.getAllBeneficiaries()

            if (beneficiaries.isEmpty()){
                launch(UI){
                    progressBar.visibility = View.GONE
                    tv_empty_beneficiaries.visibility = View.VISIBLE
                    airtime_beneficiaries_recyclerview.visibility = View.GONE
                }
            } else {
                launch(UI){
                    progressBar.visibility = View.GONE
                    tv_empty_beneficiaries.visibility = View.GONE
                    val adapter = AirtimeBeneficiariesAdapter(beneficiaries as ArrayList<AirtimeModel>, this@AirtimeBeneficiariesActivity, this@AirtimeBeneficiariesActivity)
                    airtime_beneficiaries_recyclerview.adapter = adapter
                }
            }
        }
    }
}
