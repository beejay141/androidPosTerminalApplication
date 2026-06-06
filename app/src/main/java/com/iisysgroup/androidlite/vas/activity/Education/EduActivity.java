package com.iisysgroup.androidlite.vas.activity.education;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.iisysgroup.androidlite.R;
import com.iisysgroup.androidlite.vas.VasAdapter;
import com.iisysgroup.androidlite.vas.VasBaseActivity;
import com.iisysgroup.androidlite.vas.VasEducationDataGenerator;
import com.iisysgroup.androidlite.vas.VasItems;

import java.util.ArrayList;

public class EduActivity extends VasBaseActivity implements VasAdapter.VasClickListener {

    Toolbar toolbar;
    RecyclerView recyclerView;


    @Override
    public RecyclerView setRecyclerView() {
        recyclerView = findViewById(R.id.rv_vas_education);
        return recyclerView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edu);
        ArrayList<VasItems> vasItems = VasEducationDataGenerator.generateData(this);
        initializeRecyclerView(vasItems, this);

        toolbar = findViewById(R.id.toolbar);
        setToolbar(toolbar);
    }

    @Override
    public void onVasItemClick(ArrayList<VasItems> vasItemsArrayList, int position) {

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
