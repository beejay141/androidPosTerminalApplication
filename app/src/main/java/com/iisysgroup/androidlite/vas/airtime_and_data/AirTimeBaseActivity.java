package com.iisysgroup.androidlite.vas.airtime_and_data;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.iisysgroup.androidlite.R;

/**
 * Created by Agbede on 2/26/2018.
 */

public abstract class AirTimeBaseActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String AIRTIME_AMOUNT = "airtime_amount";
    public static final String AIRTIME_RECIPIENT = "airtime_recipient";
    public static final String AIRTIME_PROVIDER = "airtime_provider";


    abstract int getTextLayoutId();
    abstract int getMaxCount();
    TextView amount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        amount = findViewById(getTextLayoutId());
        initializeAmountEntryElements();

    }


    public void initializeAmountEntryElements() {
        //findViewById(R.id.back_to_enter_amount).setOnClickListener(this);
        findViewById(R.id.btn1).setOnClickListener(this);
        findViewById(R.id.btn2).setOnClickListener(this);
        findViewById(R.id.btn3).setOnClickListener(this);
        findViewById(R.id.btn4).setOnClickListener(this);
        findViewById(R.id.btn5).setOnClickListener(this);
        findViewById(R.id.btn6).setOnClickListener(this);
        findViewById(R.id.btn7).setOnClickListener(this);
        findViewById(R.id.btn8).setOnClickListener(this);
        findViewById(R.id.btn9).setOnClickListener(this);
        findViewById(R.id.btn0).setOnClickListener(this);
        findViewById(R.id.btn00).setOnClickListener(this);
        findViewById(R.id.btnclr).setOnClickListener(this);
        findViewById(R.id.btnenter).setOnClickListener(this);
        findViewById(R.id.btncancel).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        StringBuilder displayStr = new StringBuilder(amount.getText().toString());
        switch (view.getId()) {
            case R.id.btn1:
                fixAppend(displayStr, "1");
                break;
            case R.id.btn2:
                fixAppend(displayStr, "2");
                break;
            case R.id.btn3:
                fixAppend(displayStr, "3");
                break;
            case R.id.btn4:
                fixAppend(displayStr, "4");
                break;
            case R.id.btn5:
                fixAppend(displayStr, "5");
                break;
            case R.id.btn6:
                fixAppend(displayStr, "6");
                break;
            case R.id.btn7:
                fixAppend(displayStr, "7");
                break;
            case R.id.btn8:
                fixAppend(displayStr, "8");
                break;
            case R.id.btn9:
                fixAppend(displayStr, "9");
                break;
            case R.id.btn0:
                fixAppend(displayStr, "0");
                break;
            case R.id.btn00:
                fixAppend(displayStr, "00");
                break;
            case R.id.btnclr:
                if (displayStr.length() == 1){
                    displayStr.deleteCharAt(0);
                    fixDelete(displayStr);
                } else if (displayStr.length() > 1){
                    int index = displayStr.length() - 1;
                    displayStr.deleteCharAt(index);
                    fixDelete(displayStr);
                }
                break;
        }
    }

    protected void fixAppend(StringBuilder displayStr, String digit) {
        if(displayStr.length() < getMaxCount())
        {
            if (displayStr.length() == 4){
                displayStr.append(" ");
            }

            if (displayStr.length() == 8){
                displayStr.append(" ");
            }
            displayStr.append(digit);
            // fix new input
            amount.setText(displayStr.toString());
        }
    }

    protected void fixDelete(StringBuilder displayStr) {
        amount.setText(displayStr.toString());
    }

    public void showVisibility(View view)
    {
        int [] layout_ids = {R.id.enter_phone_number_or_amount, R.id.airtime_provider_select};

        if (view.getVisibility() == View.VISIBLE){
            Log.d("Visible", "Is Visible");
            return;
        }

        for (int ids : layout_ids)
        {
            if (findViewById(ids) != null && ids != view.getId())
                findViewById(ids).setVisibility(View.GONE);
        }

        view.setVisibility(View.VISIBLE);
    }
}
