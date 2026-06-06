package com.iisysgroup.androidlite;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class ConsatTVOptions extends AppCompatActivity implements View.OnClickListener{
    Toolbar toolbar;
    Button consatTvBuyPolicy,consatTvActivateVoucher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consat_tvoptions);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        consatTvBuyPolicy = findViewById(R.id.consat_buy_policy_btn);
        consatTvActivateVoucher = findViewById(R.id.consat_activate_voucher_btn);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.consat_buy_policy_btn:
                startActivity(new Intent(this,ConsatBuyVoucher.class));
                break;
            case R.id.consat_activate_voucher_btn:
                startActivity(new Intent(this,ConsatVAS.class));
                break;
        }
    }
}
