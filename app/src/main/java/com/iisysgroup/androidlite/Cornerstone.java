package com.iisysgroup.androidlite;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Cornerstone extends AppCompatActivity {
    LinearLayout vehicleclass, vehicleMan, vehicleModel, ManuYear, purchaseYr, regNum, chassisNum, engNum, insuranceType, policytype;
    TextView textView1, textView2, textView3, textView4, textView5, textView6, textView7, textView8, textView9, textView10;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cornerstone);

        vehicleclass = findViewById(R.id.vehicleClass);
        textView1 = vehicleclass.findViewById(R.id.textView);
        textView1.setText("Vehicle Class");

        vehicleMan = findViewById(R.id.vehicleBody);
        textView2 = vehicleMan.findViewById(R.id.textView);
        textView2.setText("Vehicle Manufacturer");

        vehicleModel = findViewById(R.id.vehicleModel);
        textView3 = vehicleModel.findViewById(R.id.textView);
        textView3.setText("Vehicle Model");

        ManuYear = findViewById(R.id.manufatureYear);
        textView4 = ManuYear.findViewById(R.id.textView);
        textView4.setText("Manufacture Year");

        purchaseYr = findViewById(R.id.purchaseYr);
        textView5 = purchaseYr.findViewById(R.id.textView);
        textView5.setText("Vehicle Value");

        regNum = findViewById(R.id.registrationNum);
        textView6 = regNum.findViewById(R.id.textView);
        textView6.setText("Registration Number");

        chassisNum = findViewById(R.id.chassisNumber);
        textView7 = chassisNum.findViewById(R.id.textView);
        textView7.setText("Chassis Number");

        engNum = findViewById(R.id.engineNumber);
        textView8 = engNum.findViewById(R.id.textView);
        textView8.setText("Engine Number");

        insuranceType = findViewById(R.id.insuranceType);
        textView9 = insuranceType.findViewById(R.id.textView);
        textView9.setText("Insurance Type");

        policytype = findViewById(R.id.policyType);
        textView10 = policytype.findViewById(R.id.textView);
        textView10.setText("Policy Holder Type");
    }
}
