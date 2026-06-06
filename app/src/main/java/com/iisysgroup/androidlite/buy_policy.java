package com.iisysgroup.androidlite;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class buy_policy extends AppCompatActivity {

    LinearLayout vehicleMake,vehicleModel,vehicleBody,ManuYear,chassisNum,regNum,vehicleVal,covertype;
    TextView textView1, textView2, textView3, textView4, textView5, textView6, textView7, textView8;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_policy);

        vehicleMake = findViewById(R.id.vehicleMake);
        textView1 = vehicleMake.findViewById(R.id.textView);
        textView1.setText("Vehicle Make");

        vehicleModel = findViewById(R.id.vehicleModel);
        textView2 = vehicleModel.findViewById(R.id.textView);
        textView2.setText("Vehicle Model");

        vehicleBody = findViewById(R.id.vehicleBody);
        textView3 = vehicleBody.findViewById(R.id.textView);
        textView3.setText("Vehicle Body");

        ManuYear = findViewById(R.id.manufatureYear);
        textView4 = ManuYear.findViewById(R.id.textView);
        textView4.setText("Manufacture Year");

        chassisNum = findViewById(R.id.chassisNumber);
        textView5 = chassisNum.findViewById(R.id.textView);
        textView5.setText("Chassis Number");

        regNum = findViewById(R.id.registrationNum);
        textView6 = regNum.findViewById(R.id.textView);
        textView6.setText("Registration Number");

        vehicleVal = findViewById(R.id.vehicleValue);
        textView7 = vehicleVal.findViewById(R.id.textView);
        textView7.setText("Vehicle Value");

        covertype = findViewById(R.id.coverType);
        textView8 = covertype.findViewById(R.id.textView);
        textView8.setText("Type Of Cover");
    }
}
