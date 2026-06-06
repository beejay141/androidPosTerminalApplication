package com.iisysgroup.androidlite.vas.internet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.iisysgroup.androidlite.R;
import com.iisysgroup.androidlite.vas.VasAdapter;
import com.iisysgroup.androidlite.vas.VasBaseActivity;
import com.iisysgroup.androidlite.vas.VasInternetDataGenerator;
import com.iisysgroup.androidlite.vas.VasItems;
import com.iisysgroup.androidlite.vas.internet.smile.SmileActionSelection;

import java.util.ArrayList;

public class InternetSubscription extends VasBaseActivity implements VasAdapter.VasClickListener{
    RecyclerView recyclerView;
    ArrayList<VasItems> vasItems;
    Toolbar toolbar;

    @Override
    public RecyclerView setRecyclerView() {
        recyclerView = findViewById(R.id.rv_vas_internet_subs);
        return recyclerView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internet_subscription);
        vasItems = VasInternetDataGenerator.generateData(this);
        initializeRecyclerView(vasItems,this);

        toolbar = findViewById(R.id.toolbar);
        setToolbar(toolbar);
    }

    @Override
    public void onVasItemClick(ArrayList<VasItems> vasItemsArrayList, int position) {
        switch(position){
            case 0 : startActivity(new Intent(this, SmileActionSelection.class));
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home : onBackPressed();
            break;
        }
        return super.onOptionsItemSelected(item);
    }
}
