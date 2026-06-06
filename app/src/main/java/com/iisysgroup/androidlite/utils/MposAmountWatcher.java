package com.iisysgroup.androidlite.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.widget.EditText;

import java.text.NumberFormat;

public class MposAmountWatcher implements TextWatcher{
    private EditText editText;
    Formatter formatter =  new Formatter();
    NumberFormat numberFormat = NumberFormat.getNumberInstance();
    String temp;

    public MposAmountWatcher(EditText editText){
        this.editText = editText;
        numberFormat.setMinimumFractionDigits(2);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        temp =  s.toString();

        if(temp.length() > 0){
            temp = temp.replaceAll(",","");

            if(temp.length() ==  1){
                temp =  Double.parseDouble(temp)/100.0 + "";
            }
            if(temp.length() > 1){
                if(temp.contains(".")) {
                    int pow = temp.length() -  temp.indexOf(".") - 1;
                    temp =  Math.pow(10, pow) * Double.parseDouble(temp) + "";
                }
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if(!temp.isEmpty()){
            String    text =   numberFormat.format(Double.parseDouble(temp)/100.0);
            editText.removeTextChangedListener(this);
            editText.setText(text);
            editText.addTextChangedListener(this);
            editText.setSelection(editText.length());
        }
    }
}