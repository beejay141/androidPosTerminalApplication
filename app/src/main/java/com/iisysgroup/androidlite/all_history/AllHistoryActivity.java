package com.iisysgroup.androidlite.all_history;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.iisysgroup.androidlite.R;
import com.iisysgroup.androidlite.login.Helper;
import com.iisysgroup.androidlite.login.securestorage.SecureStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AllHistoryActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    HistoryAdapter historyAdapter;
    ProgressBar progressBar;
    Boolean isLoading = false;

    List<Transaction> historyList = new ArrayList<>();
    Handler handler = new Handler();

    View errorLayout, emptyLayout;

    String userId = SecureStorage.retrieve(Helper.USER_ID, "");
    String terminalId = SecureStorage.retrieve(Helper.TERMINAL_ID, "");
    String password = SecureStorage.retrieve(Helper.STORED_PASSWORD, "");
    private int currentPage = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_transaction_history);

        recyclerView = findViewById(android.R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        historyAdapter = new HistoryAdapter(this, historyList);
        recyclerView.setAdapter(historyAdapter);

        swipeRefreshLayout = findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        progressBar = findViewById(R.id.progrssbar);

        errorLayout = findViewById(R.id.error_layout);
        emptyLayout = findViewById(R.id.empty_layout);
        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                Log.i(" dx",  "" +dx);
                Log.i("dy", "" + dy);

                int totalItemCount = linearLayoutManager.getItemCount();
                int visibleItemCount = linearLayoutManager.getChildCount();
                int firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();

                if ((visibleItemCount + firstVisibleItem) >= totalItemCount && firstVisibleItem >= 0 && progressBar.getVisibility() == View.GONE) {
                    if(!swipeRefreshLayout.isRefreshing()){
                        currentPage++;
                        getTransactionHistories();
                        progressBar.setVisibility(View.VISIBLE);
                        isLoading = true;
                    }
                }
            }
        });

        downloadRecords();
    }

    @Override
    public void onRefresh() {
        downloadRecords();
        progressBar.setVisibility(View.GONE);
    }

    void downloadRecords() {

        if (!swipeRefreshLayout.isRefreshing()) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
        }

        errorLayout.setVisibility(View.INVISIBLE);
        emptyLayout.setVisibility(View.INVISIBLE);

        if(!isLoading){
            getTransactionHistories();
        }
    }

    public void getTransactionHistories() {

        Log.i("RetrofitResponse", "Here");

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor)
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://vas.itexapp.com")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        Log.i("RetrofitResponse", "Here");

        HistoryApi transactionHistoryApi = retrofit.create(HistoryApi.class);

//        Call<TransactionHistory> transactionHistoryCall = transactionHistoryApi.fetchHistoty( new HistoryParam(terminalId,userId,password,limit,currentPage), terminalId);

        int limit = 15;
        Call<TransactionHistory> transactionHistoryCall = transactionHistoryApi.fetchHistoty(new HistoryParam(terminalId, userId, password, limit, currentPage));
        Log.i("RetrofitResponse", "Here");
        transactionHistoryCall.enqueue(new Callback<TransactionHistory>() {
            @Override
            public void onResponse(@NonNull Call<TransactionHistory> call, @NonNull Response<TransactionHistory> response) {
                isLoading = false;
                Log.i("RetrofitResponse", "Here");

                try {
                    assert response.body() != null;
                    final UserTransactions data = response.body().getUserTransactions();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            swipeRefreshLayout.setRefreshing(false);
                            swipeRefreshLayout.setVisibility(View.VISIBLE);
                            if (data != null) {
                                if (data.getError()) {
                                    Snackbar snackbar = Snackbar.make(errorLayout,
                                            "Could not retrieve transaction history", Snackbar.LENGTH_INDEFINITE);
                                    snackbar.setAction("Refresh", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            downloadRecords();
                                        }
                                    });
                                    snackbar.show();
                                    progressBar.setVisibility(View.GONE);
                                    return;
                                }

                                historyList.addAll(data.getTransactions());
                                historyAdapter.notifyDataSetChanged();

                                //Needed to populate the filters on the filters menu of transaction history
                                Helper.savePreference(AllHistoryActivity.this, Helper.USER_TRANSACTIONS, new Gson().toJson(data, UserTransactions.class));
//                                Helper.savePreference(getContext(), Helper.DOWNLOAD_BALANCE, false);

//
                            } else {
                                Helper.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Snackbar snackbar = Snackbar.make(errorLayout,
                                                "Could not retrieve transaction history", Snackbar.LENGTH_INDEFINITE);
                                        snackbar.setAction("Refresh", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                downloadRecords();
                                            }
                                        });
                                        snackbar.show();
                                    }
                                });
                                swipeRefreshLayout.setVisibility(View.INVISIBLE);
                                progressBar.setVisibility(View.GONE);
                            }
                        }

                    });

                } catch (Exception e) {

                }
            }

            @Override
            public void onFailure(Call<TransactionHistory> call, Throwable t) {
                // Log.i("Response", t.getMessage().toString());
                isLoading = false;
                Snackbar snackbar = Snackbar.make(errorLayout,
                        "Could not retrieve transaction history", Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("Refresh", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        downloadRecords();
                    }
                });
                snackbar.show();
                swipeRefreshLayout.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        });

    }
}
