package com.iisysgroup.androidlite.utils;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.google.gson.Gson;
import com.iisysgroup.androidlite.SessionKey;
import com.iisysgroup.androidlite.SessionKeyProcess;
import com.itex.richard.payviceconnect.wrapper.PayviceServices;

import org.apache.commons.codec.binary.Base64;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Agbede on 3/14/2018.
 */

public class StringUtils {
//    Gson gson = new Gson();
    public static String getPrintableLine(String a, String b){
        int length = 32;
        int a_length = a.length();
        int b_length = b.length();

        int total_length = a_length + b_length;
        int space_length = length - total_length;

        StringBuilder stringBuilder = new StringBuilder(32);
        stringBuilder.append(a);
        for (int i = 0; i < space_length; i++){
            stringBuilder.append(" ");
        }
        stringBuilder.append(b);

        Log.d("Final string", stringBuilder.toString());

        return stringBuilder.toString();
    }

    public static  String getClientRef(final Context context, String rrn){
        if(SharedPreferenceUtils.INSTANCE.getSessionKey(context).isEmpty()){
            getSsKey(context);
        }
        String paylad = "{" +
                "    \"sessionKey\": \""+SharedPreferenceUtils.INSTANCE.getSessionKey(context)+"\"," +
                "    \"timestamp\": \"Y-m-d H:i:s.u\"," +
                "    \"rrn\": \""+rrn+"\"," +
                "    \"randomString\":\""+getRandom(30)+"\"\n" +
                "}";

        return new String(Base64.encodeBase64(paylad.getBytes()));
    }

    public static  String getRandom(int length){
        if(length <= 0){
            length = 11;
        }
        String er = "";
        for(int i = 0; i < length; i++){
            double c = Math.random();
            int d = Math.getExponent(c);
            er += d;
        }
        return er;
    }
    public static void getSsKey(final Context context){
       /* final AlertDialogBuilder alertDialogBuilder = new AlertDialogBuilder(context);
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Getting Session key...");
        progressDialog.setCancelable(false);
        progressDialog.show();*/
        Log.i("okh", "GettingSesionKey");
        SharedPreferenceUtils preferenceUtils = SharedPreferenceUtils.INSTANCE;

        GetSesionParam(new SessionKey.Request(preferenceUtils.getPayviceWalletId(context),preferenceUtils.getTerminalId(context),
                preferenceUtils.getPayviceUsername(context),preferenceUtils.getPayvicePassword(context),"ANDROIDPOS", Build.ID))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<SessionKey.Response>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(SessionKey.Response response) {
//                        progressDialog.dismiss();
                        if(!response.getError()){
                            SharedPreferenceUtils.INSTANCE.saveSessionKey(context, response.getSessionKey());
                            Log.i("okh","SeesionKEySaved");
                           /* alertDialogBuilder.message("Completed"+ response.getMessage());
                            alertDialogBuilder.positiveButton("Ok", new Function1<DialogInterface, Unit>() {
                                @Override
                                public Unit invoke(DialogInterface dialogInterface) {
                                    dialogInterface.dismiss();
                                    return null;
                                }
                            });
                            alertDialogBuilder.show();*/
                        }else {
                           /* alertDialogBuilder.message("Error occured with error message "+ response.getMessage());
                            alertDialogBuilder.positiveButton("Ok", new Function1<DialogInterface, Unit>() {
                                @Override
                                public Unit invoke(DialogInterface dialogInterface) {
                                    dialogInterface.dismiss();
                                    return null;
                                }
                            });
                            alertDialogBuilder.show();*/
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        /*progressDialog.dismiss();
                        alertDialogBuilder.message("Error occured ");
                        alertDialogBuilder.positiveButton("Ok", new Function1<DialogInterface, Unit>() {
                            @Override
                            public Unit invoke(DialogInterface dialogInterface) {
                                dialogInterface.dismiss();
                                return null;
                            }
                        });

                        alertDialogBuilder.show();*/
                        Log.i("okh","Session key Failed");
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public static Observable<SessionKey.Response> GetSesionParam(SessionKey.Request request) {
        Gson gson = new Gson();
        SessionKeyProcess process = new SessionKeyProcess();
        String body = gson.toJson(request, SessionKey.Request.class);
        return process.getKey("http://197.253.19.75:8029/api/v1/account/generate-session-key", body);
    }


}
