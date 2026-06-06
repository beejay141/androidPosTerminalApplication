package com.iisysgroup.androidlite.vas.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.iisysgroup.androidlite.ConsatTVOptions;
import com.iisysgroup.androidlite.vas.cable.Dstv;
import com.iisysgroup.androidlite.vas.cable.Gotv;
import com.iisysgroup.androidlite.R;
import com.iisysgroup.androidlite.vas.VasAdapter;
import com.iisysgroup.androidlite.vas.VasBaseActivity;
import com.iisysgroup.androidlite.vas.VasCableGenerator;
import com.iisysgroup.androidlite.vas.VasItems;
import com.iisysgroup.androidlite.vas.cable.startimes.Startimes;

import java.util.ArrayList;

public class CableTVActivity extends VasBaseActivity implements VasAdapter.VasClickListener {
    Toolbar toolbar;
    RecyclerView recyclerView;
    ArrayList<VasItems> vasItems;



    @Override
    public RecyclerView setRecyclerView() {
        recyclerView = findViewById(R.id.rv_vas_cable);
        return recyclerView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cable_tv);


        vasItems = VasCableGenerator.generateData(this);
        initializeRecyclerView(vasItems, this);

        toolbar = findViewById(R.id.toolbar);
        setToolbar(toolbar);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home : onBackPressed();
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onVasItemClick(ArrayList<VasItems> vasItemsArrayList, int position) {
        switch (position){
            case 0:
                startActivity(new Intent(this,Dstv.class));
                break;
            case 1:
                startActivity(new Intent(this, Gotv.class));
                break;
            case 2:
                startActivity(new Intent(this, Startimes.class));
                break;
//            case 3:
//                startActivity(new Intent(this,ConsatTVOptions.class));
//                break;
        }
    }
}