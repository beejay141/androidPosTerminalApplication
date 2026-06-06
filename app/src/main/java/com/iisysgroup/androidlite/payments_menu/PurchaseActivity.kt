package com.iisysgroup.androidlite.payments_menu


import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.TermMagmActivity
import com.iisysgroup.androidlite.cardpaymentprocessors.PurchaseProcessor
import com.iisysgroup.androidlite.cardpaymentprocessors.TransactionCompleteDisplay
import com.iisysgroup.androidlite.cardpaymentprocessors.VasPurchaseProcessor
import com.iisysgroup.androidlite.payments_menu.handlers.Purchase
import com.iisysgroup.androidlite.utils.SharedPreferenceUtils
import com.iisysgroup.androidlite.viewmodels.PurchaseViewModels
import com.iisysgroup.poslib.deviceinterface.DeviceState
import com.iisysgroup.poslib.utils.AccountType
import kotlinx.android.synthetic.main.account_select.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton
import org.jetbrains.anko.toast


class PurchaseActivity : BasePaymentActivity() {

    private val purchaseViewModels by lazy{
        ViewModelProviders.of(this).get(PurchaseViewModels::class.java)
    }

    internal var l_long_amount: Long = 0

    internal override fun getMaxCount(): Int {
        return 11
    }

    internal override fun getTextLayoutId(): Int {
        return R.id.txtAmount
    }

    internal override fun getNumberOfPages(): Int {
        return 2
    }

    internal override fun getPageLayout(): Int {
        return BasePaymentActivity.INT_PURCHASE_ACTIVITY
    }

    internal override fun onEnterPressed() {
        purchaseViewModels.setGoToAccountSelection(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        submit_button.setOnClickListener(this)
        purchaseViewModels.isUserInAmount = true

        purchaseViewModels.goToAccountSelection.observe(this, Observer { aBoolean ->
            if (aBoolean!!) {
                val amount = pageTitle.text.toString().replace(".", "")
                l_long_amount = java.lang.Long.parseLong(amount)
                if (l_long_amount == 0L){
                    toast("Enter valid amount")
                    return@Observer
                }

                showVisibility(findViewById(R.id.purchase_account_select))
                purchaseViewModels.setIsUserInAccountSelection(true)
                purchaseViewModels.isUserInAmount = false
            } else {
                showVisibility(findViewById(R.id.enter_amount))
                purchaseViewModels.isUserInAmount = true
                purchaseViewModels.setIsUserInAccountSelection(false)
            }
        })
    }

    fun purchase(amount: Long, accountType : AccountType) {

        val intent = Intent(this@PurchaseActivity, PurchaseProcessor::class.java)


        intent.putExtra(BasePaymentActivity.TRANSACTION_AMOUNT, amount)
        intent.putExtra(BasePaymentActivity.TRANSACTION_ADDITIONAL_AMOUNT, 0L)
        intent.putExtra(BasePaymentActivity.TRANSACTION_ACCOUNT_TYPE, accountType)

        if (SharedPreferenceUtils.getIsTerminalPrepped(this)){
            startActivityForResult(intent, 124)
        } else {
            alert {
                isCancelable = false
                title = "Terminal not configured"
                message = "Click O.K to go to configuration page"
                okButton {
                    startActivity(Intent(this@PurchaseActivity, TermMagmActivity::class.java))
                    //this@PurchaseActivity.finish()
                }
            }.show()
        }

    }

    override fun onBackPressed() {

        val isUserInAmount = purchaseViewModels.isUserInAmount
        val isUserInAccount = purchaseViewModels.isUserInAccountSelection

        if (isUserInAccount) {
            purchaseViewModels.setGoToAccountSelection(false)
        } else if (isUserInAmount) {
            startActivity(Intent(this, PaymentsActivity::class.java))
            finish()
        }
    }

    internal override fun onAccountTypeSet(account_type: AccountType) {
        if (account_type == null){
            toast("Select a valid account")
            return
        }
        val text_amt = pageTitle.text.toString().replace(".", "")
        l_long_amount = java.lang.Long.parseLong(text_amt)

        purchase(l_long_amount, account_type)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_CANCELED){
            return
        } else if (resultCode == Activity.RESULT_OK && requestCode == 124){
                if (data != null){
                    val state = data.getSerializableExtra("state") as DeviceState
                    val rrn = data.getStringExtra("rrn")
                    Log.d("rrn",rrn)


                    if(state == DeviceState.REMOVE_CARD){
                        finish()
                    }
                    else{

                        val intent = Intent(this@PurchaseActivity, TransactionCompleteDisplay::class.java)
                        intent.putExtra("state", state)
                        intent.putExtra("rrn", rrn)

                        //whether or not to print the value
                        intent.putExtra("print", true)
                        startActivity(intent)
                        finish()

                    }

                }

        }
    }
}
