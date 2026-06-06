package com.iisysgroup.androidlite.vas.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.iisysgroup.androidlite.Cornerstone;
import com.iisysgroup.androidlite.Leadway;
import com.iisysgroup.androidlite.R;
import com.iisysgroup.androidlite.vas.VasAdapter;
import com.iisysgroup.androidlite.vas.VasBaseActivity;
import com.iisysgroup.androidlite.vas.VasInsuranceGenerator;
import com.iisysgroup.androidlite.vas.VasItems;

import java.util.ArrayList;

public class Insurance extends VasBaseActivity implements VasAdapter.VasClickListener{

    RecyclerView recyclerView;
    Toolbar toolbar;
    @Override
    public RecyclerView setRecyclerView() {
        return recyclerView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insurance);
        recyclerView = findViewById(R.id.recyclerView);
        toolbar = findViewById(R.id.toolbar);
        setToolbar(toolbar);



        ArrayList<VasItems> items = VasInsuranceGenerator.generateData(this);
        initializeRecyclerView(items, this);
    }

    @Override
    public void onVasItemClick(ArrayList<VasItems> vasItemsArrayList, int position) {
        Intent intent;
        switch (position){
            case 0 : intent = new Intent(this,Leadway.class);
                startActivity(intent);
                break;
            case 1 : intent = new Intent(this, Cornerstone.class);
                startActivity(intent);
                break;
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
