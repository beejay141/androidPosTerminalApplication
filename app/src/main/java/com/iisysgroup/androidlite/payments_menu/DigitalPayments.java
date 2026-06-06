package com.iisysgroup.androidlite.payments_menu;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.iisysgroup.androidlite.QRScannerActivity;
import com.iisysgroup.androidlite.R;

public class DigitalPayments extends AppCompatActivity implements View.OnClickListener{

    Toolbar toolbar;
    LinearLayout qrCode, remita, mastercard;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_digital_payments);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        qrCode = findViewById(R.id.qr_scanner);
        remita = findViewById(R.id.remita);
        mastercard = findViewById(R.id.mastercard);

        qrCode.setOnClickListener(this);
        remita.setOnClickListener(this);
        mastercard.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.qr_scanner:
                startActivity(new Intent(this, QRScannerActivity.class));
                break;
            case R.id.remita:
                Toast.makeText(this, "Remita digital payment coming soon...", Toast.LENGTH_SHORT).show();
                break;
            case R.id.mastercard:
                Toast.makeText(this, "Mastercard digital payment coming soon...", Toast.LENGTH_SHORT).show();
                break;
            case R.id.home : onBackPressed();
                break;

        }
    }


}
