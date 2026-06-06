package com.iisysgroup.androidlite.payments_menu;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.iisysgroup.androidlite.R;
import com.iisysgroup.androidlite.viewmodels.PurchaseViewModels;
import com.iisysgroup.poslib.host.Host;
import com.iisysgroup.poslib.utils.AccountType;
import com.iisysgroup.poslib.utils.Utilities;

public class CashAdvance extends BasePaymentActivity {

    public String TAG = getClass().getSimpleName();




    private PurchaseViewModels purchaseViewModels;

    long l_long_amount;

    @Override
    int getMaxCount() {
        return 8;
    }

    @Override
    int getTextLayoutId() {
        return R.id.txtAmount;
    }

    @Override
    int getNumberOfPages() {
        return 2;
    }

    @Override
    int getPageLayout() {
        return BasePaymentActivity.INT_CASH_ADVANCE;
    }

    @Override
    void onEnterPressed() {
        purchaseViewModels.setGoToAccountSelection(true);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findViewById(R.id.submit_button).setOnClickListener(this);
        purchaseViewModels = ViewModelProviders.of(this).get(PurchaseViewModels.class);
        purchaseViewModels.setIsUserInAmount(true);

        purchaseViewModels.getGoToAccountSelection().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                if (aBoolean){
                    showVisibility(findViewById(R.id.purchase_account_select));
                    purchaseViewModels.setIsUserInAccountSelection(true);
                    purchaseViewModels.setIsUserInAmount(false);
                }
                else {
                    showVisibility(findViewById(R.id.enter_amount));
                    purchaseViewModels.setIsUserInAmount(true);
                    purchaseViewModels.setIsUserInAccountSelection(false);
                }
            }
        });

    }

    public void purchase(long amount, AccountType radio_selected)
    {
        if (amount == 0 || radio_selected == null)
        {
            Toast.makeText(this, "Enter valid amount and account type", Toast.LENGTH_LONG).show();
            return;
        }


        Intent intent = new Intent(this, TransactionProcessActivity.class);
        intent.putExtra(BasePaymentActivity.TRANSACTION_AMOUNT, amount);
        intent.putExtra(BasePaymentActivity.TRANSACTION_TYPE, Host.TransactionType.PURCHASE_WITH_CASH_ADVANCE);
        intent.putExtra(BasePaymentActivity.TRANSACTION_ACCOUNT_TYPE, radio_selected);
        startActivity(intent);

    }



    @Override
    public void onBackPressed() {

        boolean isUserInAmount = purchaseViewModels.getIsUserInAmount();
        boolean isUserInAccount = purchaseViewModels.isUserInAccountSelection();

        if (isUserInAccount){
            purchaseViewModels.setGoToAccountSelection(false);
        }
        else if (isUserInAmount)
        {
            startActivity(new Intent(this, PaymentsActivity.class));
            finish();
        }
    }

    @Override
    void onAccountTypeSet(AccountType account_type)
    {
        String text_amt = pageTitle.getText().toString();
        try {
            l_long_amount = Utilities.parseAmountInDecimalToLong(text_amt);
            purchase(l_long_amount, account_type);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
