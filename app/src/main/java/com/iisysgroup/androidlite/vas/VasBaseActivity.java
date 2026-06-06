package com.iisysgroup.androidlite.vas;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.iisysgroup.androidlite.R;
import com.iisysgroup.androidlite.vas.adapter.DataAdapter;
import com.iisysgroup.androidlite.vas.airtime_and_data.DataModel;

import java.util.ArrayList;

/**
 * Created by Agbede on 3/21/2018.
 */

abstract public class VasBaseActivity extends AppCompatActivity {
    /** Base class that handles the toggle between grid view and linearlayout. This class also sets the adapter for the RecyclerView**/

    class RecyclerViewNotAttachedException extends RuntimeException{
        public RecyclerViewNotAttachedException(String message) {
            super(message);
            Log.e("Error", "Attach RecyclerView if you have attached it");
        }
    }

    class RecyclerListenerNotAttachedException extends RuntimeException {
        public RecyclerListenerNotAttachedException(String message){
            super(message);
            Log.d("Error", "Ensure your activity is implementing VasAdapater.VasClickListener");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.vas_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.toggle : toggleState();
                return true;
            case android.R.id.home : onBackPressed();
            return true;
        }
        return false;
    }
    public abstract RecyclerView setRecyclerView();

    GridLayoutManager layoutManager;

    VasAdapter.VasClickListener listener;

    public void setToolbar(Toolbar toolbar){
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }




    public void initializeRecyclerView(ArrayList<VasItems> vasItems, VasAdapter.VasClickListener listener){

        this.listener = listener;
        checkForNullValues();

        VasAdapter vasAdapter = new VasAdapter(vasItems, this, listener);

        setRecyclerView().setHasFixedSize(true);
        setRecyclerView().setLayoutManager(layoutManager);
        setRecyclerView().setAdapter(vasAdapter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layoutManager = new GridLayoutManager(this, 1, GridLayoutManager.VERTICAL, false);
    }

    private void checkForNullValues() throws RecyclerViewNotAttachedException {
        /*if (listener == null){
            throw new RecyclerListenerNotAttachedException("RecyclerView listener not attached");
        }*/
        if (setRecyclerView() == null){
            throw new RecyclerViewNotAttachedException("RecyclerView not attached");
        }
    }

    public void toggleState(){
        int spanCount = layoutManager.getSpanCount();
        if (spanCount == 1){
            //Current layout is linear
            layoutManager.setSpanCount(2);

        } else {
            layoutManager.setSpanCount(1);
        }

    }
}
