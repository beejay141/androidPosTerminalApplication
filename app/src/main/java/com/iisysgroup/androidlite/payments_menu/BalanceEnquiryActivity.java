package com.iisysgroup.androidlite.payments_menu;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.iisysgroup.androidlite.R;
import com.iisysgroup.poslib.host.Host;
import com.iisysgroup.poslib.utils.AccountType;

public class BalanceEnquiryActivity extends AppCompatActivity{
    Button mCheckBalance;
    RadioGroup radioGroup;

    Toolbar toolbar;


    private void balance_enquiry(AccountType radio_selected) {
        if (radio_selected != null){
            Intent intent = new Intent(this, TransactionProcessActivity.class);
            intent.putExtra(BasePaymentActivity.TRANSACTION_TYPE, Host.TransactionType.BALANCE_INQUIRY);
            intent.putExtra(BasePaymentActivity.TRANSACTION_ACCOUNT_TYPE, radio_selected);
            startActivity(intent);
            finish();
            return;
        }

        Toast.makeText(this, "Account not selected", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(BalanceEnquiryActivity.this, PaymentsActivity.class));
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home : onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance_inq);

        radioGroup = findViewById(R.id.radio_payments_account_group);
        mCheckBalance = findViewById(R.id.btn_balance_enquiry);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCheckBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AccountType radio_selected = null;
                int selectedId = radioGroup.getCheckedRadioButtonId();

                switch (selectedId)
                {
                    case R.id.radio_savings: radio_selected = AccountType.SAVINGS;
                        break;
                    case R.id.radio_current: radio_selected = AccountType.CURRENT;
                        break;
                    case R.id.radio_default: radio_selected = AccountType.DEFAULT_UNSPECIFIED;
                        break;
                    case R.id.radio_credit: radio_selected = AccountType.CREDIT;
                        break;
                }
                balance_enquiry(radio_selected);
            }
        });
    }
}
