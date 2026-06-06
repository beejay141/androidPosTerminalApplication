package com.iisysgroup.androidlite.utils;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;
import com.iisysgroup.androidlite.R;

public class PinAlertUtils {
    public interface PinEnteredListener {
        void onPinEntered(String pin);
    }
    public static void getPin(Context context, View view, final PinEnteredListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);


        builder.setView(view);

        PinLockView pinLockView = view.findViewById(R.id.pin_lock_view);
        IndicatorDots dots = view.findViewById(R.id.indicator_dots);
        pinLockView.attachIndicatorDots(dots);
        pinLockView.setPinLength(4);
        final Dialog dialog = builder.create();
        pinLockView.setPinLockListener(new PinLockListener() {
            @Override
            public void onComplete(String pin) {
                dialog.dismiss();
                listener.onPinEntered(pin);

            }

            @Override
            public void onEmpty() {

            }

            @Override
            public void onPinChange(int pinLength, String intermediatePin) {

            }
        });

        dialog.show();

    }
}
