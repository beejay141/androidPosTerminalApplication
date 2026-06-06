package com.iisysgroup.androidlite.vas.activity;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.iisysgroup.androidlite.R;
import com.iisysgroup.androidlite.vas.VasAdapter;
import com.iisysgroup.androidlite.vas.VasBaseActivity;
import com.iisysgroup.androidlite.vas.VasGamesGenerator;
import com.iisysgroup.androidlite.vas.VasItems;

import java.util.ArrayList;

public class GamesActivity extends VasBaseActivity implements VasAdapter.VasClickListener{

    Toolbar toolbar;
    RecyclerView recyclerView;
    VasAdapter vasAdapter;
    ArrayList<VasItems> vasItems;

    @Override
    public RecyclerView setRecyclerView() {
        recyclerView = findViewById(R.id.rv_vas_games);
        return recyclerView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_games);

        ArrayList<VasItems> vasItems = VasGamesGenerator.generateData(this);
        initializeRecyclerView(vasItems,this);


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
                break;
            case 1:
                break;
        }
    }

}
