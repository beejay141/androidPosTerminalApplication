package com.iisysgroup.androidlite.login;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.zxing.common.BitMatrix;
import com.iisysgroup.androidlite.login.securestorage.SecureStorage;
import com.iisysgroup.payvice.securestorage.SecureStorageUtils;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by Bamitale @Itex on 11/4/2015.
 */
public class Helper {
    public static final String USER_TRANSACTIONS = "user_transactions" ;
    public static String PLAIN_PASSWORD = "plain_password";
    public static String COMMISSION_BALANCE = "commission_balance";
    public static String TOKEN = "token";
    public static String TERMINAL_ID = "terminal id";
    public static String STORED_PASSWORD = "password_";
    public static String PIN = "pin_";
    public static String USER_ID = "user_id";
    public static String USER_EMAIL = "user_email";
    public static String USERNAME = "username";
    public static String USER_PHONE = "phone_number";
    public static String LOGGED_IN = "is_logged_in";
    public static String STAY_LOGGED_IN = "stay_logged_in";
    public static String BALANCE = "balance";
    public static String VERIFYING = "verifying";
    public static String USER_KEY = "user_key";
    public static String LAST_LOGGED_IN = "last_logged_in";
    public static String LOG_IN_TIME = "log_in_time";
    public static String PASSWORD_IS_RESET = "password is reset";
    public static String ID_TO_RESET = "id to reset";
    public static String DOWNLOAD_BALANCE = "dbll";
    public static String REFERRAL_CODE = "referral_code";
    public static String TIME_OUT_TIME = "time_out_time";

    public static String HISTORY_SERIAL = "history_serial";
    public static String COMMISSION_KEY = "commission key";
    public static int REQUEST_INVITE = 121;
    public static int default_commission = 0;
    public static String TAG = "Payvice";



    public static String KEY_HOLDER = "keyholder";
    public static String TRANSACTION_DATA = "transaction_data";
    public static String CONNECTION_DATA = "connection_data";



    public static String HAS_USER_SIGNED_IN_BEFORE = "has_user_signed_in_before";

    public static String KEY_SESSION_EXPIRY_TIME = "session_expiry_time";
    public static String KEY_SHOULD_AUTO_LOG_OUT = "auto_log_user_out";

    static Handler handler = new Handler(Looper.getMainLooper());
    static SharedPreferences prefs;
    static SharedPreferences.Editor prefEditor;


    public static String getPreference(Context context, String key,
                                       String defaultValue) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);

        return prefs.getString(key, defaultValue);
    }


    public static void savePreference(Context context,
                                      String key, String value) {
        if (key != null && value != null) {
            prefs = PreferenceManager.getDefaultSharedPreferences(context);
            prefEditor = prefs.edit();
            prefEditor.putString(key, value);
            prefEditor.apply();
        }

    }

    public static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }

    public static boolean getPreference(Context context, String key,
                                        boolean defaultValue) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);

        return prefs.getBoolean(key, defaultValue);
    }


    public static void savePreference(Context context,
                                      String key, boolean value) {
        if (key != null) {
            prefs = PreferenceManager.getDefaultSharedPreferences(context);
            prefEditor = prefs.edit();
            prefEditor.putBoolean(key, value);
            prefEditor.apply();
        }
    }

    public static int getPreference(Context context, String key,
                                    int defaultValue) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);

        return prefs.getInt(key, defaultValue);
    }


    public static void savePreference(Context context, String key, int value) {
        if (key != null) {
            prefs = PreferenceManager.getDefaultSharedPreferences(context);
            prefEditor = prefs.edit();
            prefEditor.putInt(key, value);
            prefEditor.apply();
        }
    }

    public static long getLongPreference(Context context, String key,
                                         long defaultValue) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);

        return prefs.getLong(key, defaultValue);
    }


    public static void saveLongPreference(Context context, String key, long value) {
        if (key != null) {
            prefs = PreferenceManager.getDefaultSharedPreferences(context);
            prefEditor = prefs.edit();
            prefEditor.putLong(key, value);
            prefEditor.commit();
        }
    }


    public static AlertDialog.Builder getAlertDialogBuilder(Activity context, String title, String message) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setTitle(title);
        alertBuilder.setMessage(message);
        alertBuilder.setCancelable(false);
        return alertBuilder;
    }


    public static void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    public static void runAfterDuration(Runnable runnable, long expiry) {
        handler.postDelayed(runnable, expiry);
    }

    public static void showInfoDialog(final Context context, final String title, final String message) {
        showInfoDialogWithAction(context, title, message, null);
    }

    public static void showInfoDialogWithAction(final Context context, final String title, final String message,
                                                final DialogInterface.OnClickListener action) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(context)
                        .setTitle(title)
                        .setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, action).show();
            }
        });
    }


    public static String getCharacterDataFromElement(Element e) {
        if (e != null) {
            Node child = e.getFirstChild();
            if (child instanceof CharacterData) {
                CharacterData cd = (CharacterData) child;
                return cd.getData();
            }
        }
        return "";
    }

    public static String getElementLine(Element element, String elementTagName) {
        if (element != null) {
            NodeList tempNodeList = element.getElementsByTagName(elementTagName);
            Element line = (Element) tempNodeList.item(0);
            return getCharacterDataFromElement(line);
        }
        return "";
    }

    public static Document getXMLDocument(String xmlRecords) throws Exception {
        DocumentBuilder xmlDocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource inputSource = new InputSource();
        inputSource.setCharacterStream(new StringReader(xmlRecords));
        return xmlDocumentBuilder.parse(inputSource);
    }

    public static String getServerErrorMessage(NodeList xmlNodes, Document xmlDocument) {
        xmlNodes = xmlDocument.getElementsByTagName("error");
        Element element = (Element) xmlNodes.item(0);
        return getElementLine(element, "errmsg");
    }

    public static String generateVerificationKey(String input) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");

            byte[] output = messageDigest.digest(input.getBytes("UTF-8"));

            String key = TripleDES.bytesToHexString(output).trim().toUpperCase();

            return key;
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * @param key
     * @param input
     * @param algorithm e.g "SHA-512" "SHA-256"
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static byte[] hash(String key, String input, String algorithm) throws
            NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
        messageDigest.update(TripleDES.hexStringToBytes(key));

        return messageDigest.digest(input.getBytes("UTF-8"));

    }


    public static boolean checkPasswordStrength(String password) {
        String passwordRegex = ".*[A-Z]+.*.*[0-9]+.*|.*[0-9]+.*.*[A-Z]+.*";
        Pattern pattern = Pattern.compile(passwordRegex);

        return pattern.matcher(password).matches();
    }

    public static boolean validateEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static void showDefaultMessage(Activity context) {
        Helper.showInfoDialog(context, "Coming soon", "Feature not currently available.");
    }

    public static void showSnackBar(Activity context,
                                    String message) {
        showSnackBar(context.getCurrentFocus().getRootView(), message);
    }


    public static void showSnackBar(View view,
                                    String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
//        snackbar.setAction("OK",action);
        snackbar.show();
    }

    public static String decryptUserKey(String terminalID, final String key) {

        System.out.println("TID: " + terminalID);

        String[] xters = terminalID.split("");

        System.out.println("Xter: " + Arrays.deepToString(xters));


        String[] keys = key.split("\\|");
        String masterKey = keys[0];
        String sessionKey = keys[1];

        //System.out.println("Key: " + sessionKey);

        String clrKey = "";

        for (int i = 0; i < xters.length; i++) {
            String x = xters[i].trim();
            //  System.out.println(String.format("index %d, TID Xter %s", i, x));
            if (x.isEmpty())
                continue;

            int pos = 7;
            if (Character.isDigit(x.toCharArray()[0]))
                pos = new Integer(x);

            char keyChar = sessionKey.charAt(pos);
            clrKey += keyChar;
            //  System.out.println(String.format("index %d, Key Position %d,  Key Xter %c", i, pos, keyChar));
        }

        return clrKey;
    }

    public static void checkNullResponse(String response) {
        if (response.isEmpty())
            throw new IllegalStateException("Server communication error");
    }

    public static String getClearKey() {
        String terminalID = SecureStorage.retrieve(Helper.TERMINAL_ID, "");
        String key = SecureStorage.retrieve(Helper.USER_KEY, "");
        return Helper.decryptUserKey(terminalID, key);
    }

    public static String preparePassword(String clrPassword) {
        String key = getClearKey();
//        System.out.println("Clear Key: " + key);
        return SecureStorageUtils.hashIt(clrPassword, key);
    }

    public static String preparePin(String clrPin) {
        String password = SecureStorage.retrieve(Helper.STORED_PASSWORD, "");
        return SecureStorageUtils.hashIt(clrPin, password);
    }

    public static void clearData() {
  //      SecureStorage.delete(STORED_PASSWORD);
 //       SecureStorage.delete(USER_KEY);
//        SecureStorage.delete(USER_ID);
 //       SecureStorage.delete(BALANCE);
        SecureStorage.delete(USERNAME);
  //      SecureStorage.dele
        //
        // te(STORED_PASSWORD);
        SecureStorage.delete(PIN);
 //       SecureStorage.delete(TERMINAL_ID);
        SecureStorage.delete(USER_PHONE);

    }


    public static String getDeviceID(Context context) {

        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);


        final String tmDevice = tm.getDeviceId() != null ? tm.getDeviceId() : "",

                tmSerial = tm.getSimSerialNumber() != null ? tm.getSimSerialNumber() : "",

                androidId = android.provider.Settings.Secure.getString(context.getContentResolver(),
                        android.provider.Settings.Secure.ANDROID_ID);

        long idHashCode = androidId.hashCode();
        long tmHashCode = tmDevice.hashCode();
        long tmSHashCode = tmSerial.hashCode();

        UUID deviceUuid = new UUID(idHashCode, (tmHashCode << 32) | tmSHashCode);

        String deviceId = deviceUuid.toString();
        return deviceId;
    }


    public static String getLogInTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM, yyyy hh:mm:ss");

        return sdf.format(new Date());
    }


    public static void setTypeFace(TextView textView, String typeFaceFileName) {
        if (textView != null && typeFaceFileName != null) {
            textView.setTypeface(getTypeFace(textView.getContext(), typeFaceFileName));
        }
    }

    public static Typeface getTypeFace(Context context, String typeFaceFileName) {
        return Typeface.createFromAsset(context.getAssets(), typeFaceFileName);
    }

    public static void gotoActivitySettingScreen(Activity activity) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivity(intent);
    }

    public static void saveTimeOutTime(Context context) {
        saveLongPreference(context, Helper.TIME_OUT_TIME, System.currentTimeMillis());
    }

    public static long getSessionDuration(Context context) {
        long duration = Long.parseLong(getPreference(context, Helper.KEY_SESSION_EXPIRY_TIME, "5"));
        long durationInMillis = 1000 * 60 * duration;
        return durationInMillis;
    }

    public static boolean isExpiredSession(Context context) {
        if (!getPreference(context, Helper.KEY_SHOULD_AUTO_LOG_OUT, true))
            return false;

        long sessionLength = getSessionDuration(context);
        if (sessionLength < 0) return false;

        long expiredtime = getLongPreference(context, Helper.TIME_OUT_TIME, System.currentTimeMillis());

        long currenttime = System.currentTimeMillis();
        expiredtime += sessionLength;

        return currenttime >= expiredtime;

    }

    public static String processPinResult(String message) {

        String resp = "";

        for (Map.Entry<String, String> pinSerial : getPinSerialMap(message).entrySet()) {
            resp += "Pin: " + pinSerial.getKey() + " Serial: " + pinSerial.getValue() + "\n";
        }

        return resp;
    }

    public static Map<String, String> getPinSerialMap(String message) {
        //4513365854147996|5797863093440779,8303696829113577|8366527920012664

        Map<String, String> map = new HashMap<>();
        String[] vals = message.split("\\,"), temp = null;

        for (String value : vals) {
            temp = value.split("\\|");
            String pin = temp[0];
            String serial = temp[1];
            map.put(pin, serial);
        }

        return map;
    }

    public static Bitmap toBitmap(BitMatrix matrix) {
        int height = matrix.getHeight();
        int width = matrix.getWidth();
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bmp.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bmp;
    }

    public static File getReferrerQRBitmapFile() {
        File imageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!imageDir.exists()) imageDir.mkdirs();

        File imageFile = new File(imageDir.getAbsolutePath(), "payvice_ref_qr.bmp");

        return imageFile;
    }

    public static String sanitizeStringAmount(String dirtyAmount) {
        if (dirtyAmount == null) return null;
        return dirtyAmount.toUpperCase().replaceAll("₦", "").replaceAll("N", "").replaceAll(",", "").trim();
    }



    public static class ThreadService {
        static Thread thread;

        public static void execute(Runnable runnable) {
            thread = new Thread(runnable);
            thread.start();
        }
    }


}
