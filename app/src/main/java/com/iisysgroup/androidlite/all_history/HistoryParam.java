package com.iisysgroup.androidlite.all_history;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class HistoryParam {
    String wallet;
    String username;
    String password;
    String product;
    String productName;
    String startDate;
    String endDates;
    String viewWallet;
    @NonNull
    int limit;
    @Nullable
    int currentPage;

    public HistoryParam(String wallet, String username, String password, String viewWallet, String product, String productName, String startDate, String endDates, int limit, int currentPage) {
        this.wallet = wallet;
        this.username = username;
        this.password = password;
        this.product =product;
        this.productName =productName;
        this.startDate = startDate;
        this.viewWallet =viewWallet;
        this.endDates =endDates;
        this.limit =limit;
        this.currentPage=currentPage;
    }

    public HistoryParam(String wallet, String username, String password, int limit, int currentPage) {
        this.wallet = wallet;
        this.username = username;
        this.password = password;
        this.limit=limit;
        this.currentPage=currentPage;

    }
}
