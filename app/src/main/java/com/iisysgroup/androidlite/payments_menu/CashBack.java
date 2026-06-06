package com.iisysgroup.androidlite.payments_menu;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.iisysgroup.androidlite.R;
import com.iisysgroup.androidlite.viewmodels.PurchaseViewModels;
import com.iisysgroup.poslib.host.Host;
import com.iisysgroup.poslib.utils.AccountType;
import com.iisysgroup.poslib.utils.Utilities;


public class CashBack extends BasePaymentActivity{
    public String TAG = getClass().getSimpleName();

    String cash_entered;
    String cash_advance_entered = "0.00";
    AccountType account_type;

    View enter_amount;
    View choose_account_type;
    private PurchaseViewModels purchaseViewModels;


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
        return 3;
    }

    @Override
    int getPageLayout() {
        return BasePaymentActivity.INT_CASH_BACK;
    }


    @Override
    public void onBackPressed() {
        if (choose_account_type.getVisibility() == View.VISIBLE){
            showVisibility(enter_amount);
            enter_amount.setTag(1);
            return;
        }

        if (enter_amount.getVisibility() == View.VISIBLE){
            if (enter_amount.getTag().equals(0)){
                startActivity(new Intent(this, PaymentsActivity.class));
                finish();
            } else if (enter_amount.getTag().equals(1)){
                setUpCashEnter(enter_amount);
                enter_amount.setTag(0);
            }
        }
    }

    private void setUpCashEnter(View view) {
        TextView dashBoardTitle = view.findViewById(R.id.dashboard_title);
        dashBoardTitle.setText("Enter cash");

        pageTitle.setText(cash_entered);
    }

    public void setUpCashAdvance(View view){
        TextView dashBoardTitle = view.findViewById(R.id.dashboard_title);
        dashBoardTitle.setText("Enter cash back amount");
        //setTag to 1 means we are now in cashAdvanceCashEnteringScreen
        view.setTag(1);


        pageTitle.setText(cash_advance_entered);
    }


    @Override
    void onEnterPressed() {
        if (enter_amount.getTag().equals(0)){
            purchaseViewModels.setGoToCashAdvanceEnter(true);
            return;
        }

        if (enter_amount.getTag().equals(1))
            purchaseViewModels.setGoToAccountSelection(true);

    }

    @Override
    void onAccountTypeSet(AccountType account_type) {
        this.account_type = account_type;

        try {
            long cash = Utilities.parseAmountInDecimalToLong(cash_entered);
            long cash_advance = Utilities.parseAmountInDecimalToLong(cash_advance_entered);
            Log.d("Cash advance", cash + " " + cash_advance + " " + account_type.toString());
            purchase(cash, cash_advance, account_type);
        } catch (Exception e) {
            Log.d("Error", e.toString());
            e.printStackTrace();
        }
    }

    public void purchase(long amount, long cashback_amount, AccountType radio_selected)
    {
        if (amount == 0 || cashback_amount == 0 || radio_selected == null)
        {
            Toast.makeText(this, "Please enter valid amount and account", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(this, TransactionProcessActivity.class);
        intent.putExtra(BasePaymentActivity.TRANSACTION_TYPE, Host.TransactionType.PURCHASE_WITH_CASH_BACK);
        intent.putExtra(BasePaymentActivity.TRANSACTION_ACCOUNT_TYPE, radio_selected);
        intent.putExtra(BasePaymentActivity.TRANSACTION_AMOUNT, amount);
        intent.putExtra(BasePaymentActivity.TRANSACTION_ADDITIONAL_AMOUNT, cashback_amount);
        startActivity(intent);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        purchaseViewModels = ViewModelProviders.of(this).get(PurchaseViewModels.class);

        enter_amount = findViewById(R.id.enter_amount);
        choose_account_type = findViewById(R.id.account_select);

        showVisibility(enter_amount);
        enter_amount.setTag(0);

        purchaseViewModels.getGoToCashAdvanceEnter().observe(this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(@Nullable Boolean aBoolean) {
                        if (aBoolean){
                            cash_entered = pageTitle.getText().toString();
                            enter_amount.setTag(1);
                            setUpCashAdvance(enter_amount);
                        }
                    }
                });

        purchaseViewModels.getGoToAccountSelection().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                if (aBoolean){
                    cash_advance_entered = pageTitle.getText().toString();
                    showVisibility(choose_account_type);
                }

            }
        });


    }


}
