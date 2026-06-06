package com.iisysgroup.androidlite;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.iisysgroup.androidlite.viewmodels.RecyclerClickListener;
import com.iisysgroup.androidlite.payments_menu.BasePaymentActivity;
import com.iisysgroup.androidlite.payments_menu.RefundActivity;
import com.iisysgroup.androidlite.transaction_viewpager_fragments.TransactionHistory;
import com.iisysgroup.androidlite.transaction_viewpager_fragments.TransactionSummary;
import com.iisysgroup.poslib.host.entities.TransactionResult;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import java.util.ArrayList;
import java.util.List;

public class Transactions extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, ViewPager.OnPageChangeListener, MaterialSearchView.OnQueryTextListener, MaterialSearchView.SearchViewListener {

    public static final String TRANSACTIONS_DETAILS_KEY = "transaction_details_key";

    ViewPager viewPager;
    TransactionsAdapter adapter;
    BottomNavigationView navigation;
    MaterialSearchView materialSearchView;
    Toolbar toolbar;

    App application;

    TransactionHistory transactionHistory;
    TransactionSummary transactionSummary;
    List<TransactionResult> transactionResultList;




    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.transactions, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        materialSearchView.setMenuItem(item);
        materialSearchView.setOnQueryTextListener(this);
        materialSearchView.setOnSearchViewListener(this);

        return true;
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        transactionResultList = new ArrayList<>();
        setContentView(R.layout.activity_transactions);
        application = (App)getApplication();

        application.getDb().getTransactionResultDao().findAll().observe(this, new Observer<List<TransactionResult>>() {
            @Override
            public void onChanged(@Nullable List<TransactionResult> transactionResults) {
                transactionResultList = transactionResults;
            }
        });


        RecyclerClickListener listener = ViewModelProviders.of(this).get(RecyclerClickListener.class);
        listener.getTransactionResult().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String RRN) {

                Intent intent = new Intent(Transactions.this, RefundActivity.class);
                intent.putExtra(BasePaymentActivity.TRANSACTION_RRN, RRN);
                startActivity(intent);
            }
        });


        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Transactions");
        toolbar.setTitleTextColor(getResources().getColor(R.color.recyclerview_light));
        setSupportActionBar(toolbar);

        materialSearchView = findViewById(R.id.search_view);
        materialSearchView.setOnSearchViewListener(this);
        materialSearchView.setOnQueryTextListener(this);
        viewPager = findViewById(R.id.viewpager_transactions);

        adapter = new TransactionsAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);
        viewPager.addOnPageChangeListener(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_history: viewPager.setCurrentItem(0);
                return true;
            case R.id.navigation_summary: viewPager.setCurrentItem(1);
                return true;
        }
        return false;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (position == 0){
            navigation.setSelectedItemId(R.id.navigation_history);
        }
        else if (position == 1){
            navigation.setSelectedItemId(R.id.navigation_summary);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        List<TransactionResult> results = new ArrayList<>();
        for (TransactionResult result : transactionResultList){
             if (result.RRN.contains(newText)){
                 results.add(result);
                 //transactionHistory.setNewData(results);
             }
        }
        return true;
    }

    @Override
    public void onSearchViewShown() {

    }

    @Override
    public void onSearchViewClosed() {

    }


    class TransactionsAdapter extends FragmentPagerAdapter {

        public TransactionsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0: transactionHistory = new TransactionHistory();
                    return transactionHistory;
                case 1: transactionSummary = new TransactionSummary();
                    return transactionSummary;
            }
            return transactionHistory;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
