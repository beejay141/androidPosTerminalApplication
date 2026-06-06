package com.iisysgroup.androidlite.vas.activity;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.iisysgroup.androidlite.R;
import com.iisysgroup.androidlite.vas.VasAdapter;
import com.iisysgroup.androidlite.vas.VasBaseActivity;
import com.iisysgroup.androidlite.vas.VasEventDataGenerator;
import com.iisysgroup.androidlite.vas.VasItems;

import java.util.ArrayList;

public class EventsActivity extends VasBaseActivity implements VasAdapter.VasClickListener {

    Toolbar toolbar;
    RecyclerView recyclerView;
    ArrayList<VasItems> vasItems;

    @Override
    public RecyclerView setRecyclerView() {
        recyclerView = findViewById(R.id.rv_events);
        return recyclerView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_activity);

        vasItems = VasEventDataGenerator.generateData(this);
        initializeRecyclerView(vasItems,this);


        toolbar = findViewById(R.id.toolbar);
        setToolbar(toolbar);

    }

    @Override
    public void onVasItemClick(ArrayList<VasItems> vasItemsArrayList, int position) {
        switch(position){
            case 0:
                Toast.makeText(this, "Service coming soon.", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home : onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
