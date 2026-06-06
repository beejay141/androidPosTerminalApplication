package com.iisysgroup.androidlite.history_summary

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.iisysgroup.androidlite.R
import kotlinx.android.synthetic.main.activity_transactions_menu.*

class TransactionsMenu : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions_menu)

        setSupportActionBar(toolbar)

        startActivity(Intent(this, TransactionHistory::class.java))
        finish()

        btn_transaction_history.setOnClickListener {
            startActivity(Intent(this, TransactionHistory::class.java))
        }

        btn_transaction_summary.setOnClickListener {
            startActivity(Intent(this, TransactionSummary::class.java))

        }
    }
}
