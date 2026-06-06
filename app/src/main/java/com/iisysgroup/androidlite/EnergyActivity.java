package com.iisysgroup.androidlite;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.iisysgroup.androidlite.vas.VasAdapter;
import com.iisysgroup.androidlite.vas.VasBaseActivity;
import com.iisysgroup.androidlite.vas.VasEnergyDataGenerator;
import com.iisysgroup.androidlite.vas.VasItems;

import java.util.ArrayList;

public class EnergyActivity extends VasBaseActivity implements VasAdapter.VasClickListener {


    Toolbar toolbar;
    RecyclerView recyclerView;
    ArrayList<VasItems> vasItems;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home : onBackPressed();
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public RecyclerView setRecyclerView() {
        recyclerView = findViewById(R.id.rv_vas_energy);
        return recyclerView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_energy);
        vasItems = VasEnergyDataGenerator.generateData(this);
        initializeRecyclerView(vasItems, this);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public void onVasItemClick(ArrayList<VasItems> vasItemsArrayList, int position) {

    }
}
