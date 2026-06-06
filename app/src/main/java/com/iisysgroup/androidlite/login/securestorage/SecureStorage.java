package com.iisysgroup.androidlite.login.securestorage;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.iisysgroup.payvice.securestorage.SecureStorageUtils;

import java.util.Arrays;

/**
 * Created by Bamitale @Itex on 3/1/2016.
 */
public class SecureStorage {
    private static SecureStorage secureStorage;
    private static Builder builder;
    private static Gson gson = new Gson();
    private Context context;
    private SharedPreferences prefs;
    private SharedPreferences.Editor prefEditor;
    private KeyCrypto keyCrypto;

    private boolean secure;

    private String password;

    private SecureStorage(Context context) {
        this.context = context;
    }

    public static boolean store(String key, String value) {

        return secureStorage.put(key, value);
    }

    public static boolean store(String key, Object object) {
        return secureStorage.put(key, object);
    }

    public static String retrieve(String key, String defaultVal) {
        return secureStorage.get(key, defaultVal);
    }

    public static <T> T retrieve(String key, T defaultObj) {
        return secureStorage.get(key, defaultObj);
    }

    public static void delete(String key) {
        secureStorage.remove(key);
    }

    public static void deleteAll() {
        secureStorage.clear();
    }

    public static Builder init(Context context) {
        builder = new Builder(context);
        return builder;
    }

    private static void checkNullBuilder() {
        if (builder == null)
            throw new IllegalStateException("Invalid builder state. Did you call init()?");
    }

    private boolean put(String key, String value) {
        try {
            if (secure)
                value = KeyCrypto.hex(keyCrypto.encryptData(value));

            if (key != null) {
                prefEditor = prefs.edit();
                prefEditor.putString(key, value);
                prefEditor.apply();
                return true;
            }
        } catch (Exception e) {
            // e.printStackTrace();
        }

        return false;
    }

    private boolean put(String key, Object object) {
        String obj = gson.toJson(object);
        return put(key, obj);
    }

    private String get(String key, String defaultVal) {
        if (key != null) {
            try {
                String value = prefs.getString(key, defaultVal);

                if (secure)
                    value = new String(keyCrypto.decryptData(value));

                return value;
            } catch (Exception e) {
                // e.printStackTrace();
            }
        }

        return defaultVal;
    }

    private <T> T get(String key, T defaultObj) {
        if (key != null) {
            String temp = get(key, gson.toJson(defaultObj));
            return (T) gson.fromJson(temp, defaultObj.getClass());
        }

        return defaultObj;
    }

    private void remove(String key) {
        prefs.edit().remove(key).apply();
    }

    private void clear() {
        prefs.edit().clear().apply();
    }

    public static class Builder {
        static final String defaultPassword = "p@55w0rd$";
        Context context;
        String TAG = "SecureStorage", password = defaultPassword;
        EncryptionMethod encryptionMethod;


        public Builder(Context context) {
            this.context = context;
        }

        public Builder setStoreName(String name) {
            checkNullBuilder();
            builder.TAG = name;

            return builder;
        }

        public Builder setPassword(String password) {
            checkNullBuilder();
            builder.password = password;

            return builder;
        }

        public Builder setEncryptionMethod(Builder.EncryptionMethod encryptionMethod) {
            checkNullBuilder();
            builder.encryptionMethod = encryptionMethod;

            return builder;
        }

        public void build() throws Exception {
            checkNullBuilder();
            secureStorage = new SecureStorage(context);
            switch (builder.encryptionMethod) {
                case NONE:
                    secureStorage.prefs = PreferenceManager.getDefaultSharedPreferences(builder.context);
                    break;
                case ENCRYPTED:
                    String tag = builder.TAG;
                    secureStorage.prefs = builder.context.getSharedPreferences(tag, Context.MODE_PRIVATE);
                    secureStorage.secure = true;
                    break;
            }

            if (builder.password != null) {
                secureStorage.password = builder.password;
            }


            if (secureStorage.secure) {
                byte[] key = SecureStorageUtils.hash(password, "SHA-512");

                key = Arrays.copyOfRange(key, 11, 11 + KeyCrypto.DESEDE_KEY_LENGTH);
                secureStorage.keyCrypto = new KeyCrypto(key, "DESede", "DESede");
            }


        }

        public enum EncryptionMethod {NONE, ENCRYPTED}
    }


}
