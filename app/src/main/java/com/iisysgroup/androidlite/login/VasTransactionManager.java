package com.iisysgroup.androidlite.login;

import android.content.Context;

import java.io.IOException;
import java.util.Map;

import static com.iisysgroup.androidlite.login.Parsers.getTamsResult;

/**
 * Created by Bamitale @Itex on 2/8/2016.
 */
public class VasTransactionManager {

    Context context;
    VasTransactionData vasTransactionData;
    String url, action, terminalID, userId;
    Map<String, String> params;





    /**
     * @param url
     * @param params
     */
    public VasTransactionManager(String url, Map<String, String> params) {
        this.url = url;
        this.params = params;
    }


    public VasResult processTransaction() {
        RestWrapper restWrapper = new RestWrapper(url);
        VasResult tamsResult = new VasResult();

        restWrapper.setParams(params);
        String responseString = null;


        try {
            responseString = restWrapper.processRequest(RestWrapper.Request.GET);

            if (!responseString.isEmpty()) {
                tamsResult = getTamsResult(responseString);
            }
            else
                tamsResult.message = "Server communication error. Please try again later.";

        } catch (IOException e) {
        }




        return tamsResult;
    }


}
