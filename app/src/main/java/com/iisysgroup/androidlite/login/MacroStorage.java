package com.iisysgroup.androidlite.login;

import android.content.Context;

import com.iisysgroup.androidlite.login.securestorage.SecureStorage;

/**
 * Created by Agbede on 3/26/2018.
 */

public class MacroStorage {
    public void store(Context context, String message, String userId, String walletId, String ePassword, String key, String balance) {
        String userName = message.substring(0, message.indexOf("macro") - 1);

        SecureStorage.store(Helper.USERNAME, userName);
        SecureStorage.store(Helper.USER_ID, userId);
        SecureStorage.store(Helper.LOGGED_IN, true);
        SecureStorage.store(Helper.TERMINAL_ID, walletId);
        SecureStorage.store(Helper.STORED_PASSWORD, ePassword);
        SecureStorage.store(Helper.USER_KEY, key);

        Helper.savePreference(context.getApplicationContext(), Helper.BALANCE, balance);
        Helper.savePreference(context.getApplicationContext(), Helper.DOWNLOAD_BALANCE, true);


        String logInTime = Helper.getLogInTime();
        Helper.savePreference(context.getApplicationContext(), Helper.LOG_IN_TIME, logInTime);

        if (Helper.getPreference(context.getApplicationContext(), Helper.LAST_LOGGED_IN, null) == null)
            Helper.savePreference(context.getApplicationContext(), Helper.LAST_LOGGED_IN, logInTime);

        Helper.savePreference(context.getApplicationContext(), Helper.HISTORY_SERIAL, "");

    }
}
