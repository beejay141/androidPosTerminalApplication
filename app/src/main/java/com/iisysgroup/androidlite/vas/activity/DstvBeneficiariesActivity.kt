package com.iisysgroup.androidlite.vas.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.MenuItem
import com.iisysgroup.androidlite.App
import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.vas.adapter.DstvBeneficiariesAdapter
import com.iisysgroup.androidlite.vas.airtime_and_data.AirtimeActivity
import com.iisysgroup.androidlite.vas.cable.DstvBeneficiariesModel
import kotlinx.android.synthetic.main.activity_beneficiaries.*
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton
import java.util.*

class DstvBeneficiariesActivity : AppCompatActivity(), DstvBeneficiariesAdapter.DataClickListener{
    override fun onDataItemClick(data: ArrayList<DstvBeneficiariesModel>, position: Int) {
        alert {
            title = "Transaction"
            message = "Do you want to make an airtime transaction for ${data[position].name}?"
            okButton { passDataToActivity(data[position]) }
        }.show()
    }

    private fun passDataToActivity(dstvBeneficiariesModel: DstvBeneficiariesModel) {
        val intent = Intent(this@DstvBeneficiariesActivity, AirtimeActivity::class.java)
        intent.putExtra("dstvModel", dstvBeneficiariesModel)
        startActivity(intent)
    }

    private val airtimeBeneficiariesDao by lazy {
        (application as App).beneficiariesDatabase.getAirtimeBeneficiariesDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beneficiaries)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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
        val layoutManager = GridLayoutManager(this@DstvBeneficiariesActivity, 1, GridLayoutManager.VERTICAL, false)
        airtime_beneficiaries_recyclerview.layoutManager = layoutManager

        async {
            val beneficiaries = airtimeBeneficiariesDao.getAllBeneficiaries()
            if (beneficiaries == null){

            } else {
                val adapter = DstvBeneficiariesAdapter(beneficiaries as ArrayList<DstvBeneficiariesModel>, this@DstvBeneficiariesActivity, this@DstvBeneficiariesActivity)
                airtime_beneficiaries_recyclerview.adapter = adapter
            }
            Log.d("Beneficiaries", beneficiaries.toString())
        }


    }
}
