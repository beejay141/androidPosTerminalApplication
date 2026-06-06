package com.iisysgroup.androidlite.payments_menu.transfer

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import com.iisysgroup.androidlite.R
import kotlinx.android.synthetic.main.activity_transfer_bank_selection.*

class TransferBankSelection : AppCompatActivity() {

    private val mTransactionType by lazy {
        intent.getSerializableExtra("transfer_type") as TransferAmountEntry.TRANSACTION_TYPE
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer_bank_selection)

        val bankVendorCodes = resources.getStringArray(R.array.bank_codes)
        val bankNames = resources.getStringArray(R.array.bank_names)

        initializeBankList()

        if (mTransactionType == TransferAmountEntry.TRANSACTION_TYPE.WITHDRAWAL){
            finish()
            val enterAmountIntent = Intent(this@TransferBankSelection, TransferAmountEntry::class.java)

            val bankCode = bankVendorCodes[spinner_banks_list.selectedItemPosition]
            val bankName = bankNames[spinner_banks_list.selectedItemPosition]
            val accountNumber = tv_account_number.text.toString()

            enterAmountIntent.putExtra(BANK_CODE, bankCode)
            enterAmountIntent.putExtra(ACCOUNT_NUMBER, accountNumber)
            enterAmountIntent.putExtra(BANK_NAME, bankName)
            enterAmountIntent.putExtra(TRANSACTION_TYPE, mTransactionType)


            startActivity(enterAmountIntent)
        }


        action_next.setOnClickListener { 
            if (validateInputs()){
                val enterAmountIntent = Intent(this@TransferBankSelection, TransferAmountEntry::class.java)

                val bankCode = bankVendorCodes[spinner_banks_list.selectedItemPosition]
                val bankName = bankNames[spinner_banks_list.selectedItemPosition]
                val accountNumber = tv_account_number.text.toString()

                enterAmountIntent.putExtra(BANK_CODE, bankCode)
                enterAmountIntent.putExtra(ACCOUNT_NUMBER, accountNumber)
                enterAmountIntent.putExtra(BANK_NAME, bankName)
                enterAmountIntent.putExtra(TRANSACTION_TYPE, mTransactionType)


                startActivity(enterAmountIntent)

            }
        }
    }

    private fun initializeBankList() {
        // DROP DOWN TO SELECT OTHER BANKS
        val spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.bank_names, R.layout.support_simple_spinner_dropdown_item)
        spinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        spinner_banks_list.adapter = spinnerAdapter

        //Set default bank selection for GTBank
        spinner_banks_list.setSelection(23)
    }

    private fun validateInputs(): Boolean {
        if (tv_account_number.text.toString().length != 10){
            tv_account_number.error = "Enter valid account number"
            return false
        }

        return true
    }



    companion object {
        const val BANK_CODE = "bank_code"
        const val ACCOUNT_NUMBER = "account_number"
        const val TRANSACTION_TYPE = "transaction_type"
        const val BANK_NAME = "bank_name"
    }
}
