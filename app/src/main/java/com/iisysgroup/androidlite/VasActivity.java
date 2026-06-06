package com.iisysgroup.androidlite;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.iisysgroup.androidlite.vas.activity.AllVasActivity;
import com.iisysgroup.androidlite.vas.activity.CableTVActivity;
import com.iisysgroup.androidlite.vas.activity.Genesis.MovieHouse;
//import com.iisysgroup.androidlite.vas.activity.education.EduActivity;
import com.iisysgroup.androidlite.vas.activity.EnergyActivity;
import com.iisysgroup.androidlite.vas.activity.EventsActivity;
import com.iisysgroup.androidlite.vas.activity.GamesActivity;
import com.iisysgroup.androidlite.vas.activity.Insurance;
import com.iisysgroup.androidlite.vas.internet.InternetSubscription;
import com.iisysgroup.androidlite.vas.airtime_and_data.SelectionActivity;

public class VasActivity extends AppCompatActivity implements View.OnClickListener{
    Toolbar toolbar;
    TextView all, energy,cableTV,airtime,internet_subsc, tickets;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home : onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vas);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //internet_subsc = findViewById(R.id.internet_subscription);
        energy = findViewById(R.id.energy);
        cableTV = findViewById(R.id.cableTV);
        airtime = findViewById(R.id.airtimeAndData);
        tickets = findViewById(R.id.tikets);

        energy.setOnClickListener(this);
        cableTV.setOnClickListener(this);
        airtime.setOnClickListener(this);
//        internet_subsc.setOnClickListener(this);

        tickets.setOnClickListener(this);
        //money_transfer = findViewById(R.id.money_transfer);money_transfer.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()){
//            case R.id.all:
//                intent = new Intent(this, AllVasActivity.class);
//                startActivity(intent);
//                break;
            case R.id.energy:
                intent = new Intent(this, EnergyActivity.class);
                startActivity(intent);
                break;
            case R.id.cableTV:
                intent = new Intent(this, CableTVActivity.class);
                startActivity(intent);
                break;
            case R.id.airtimeAndData:
                //Show user activity for selection of either data or airtime

                intent = new Intent(this, SelectionActivity.class);
                startActivity(intent);
                break;
//            case R.id.internet_subscription:
//                intent = new Intent(this, InternetSubscription.class);
//                startActivity(intent);
//                break;
            case R.id.tikets:
                intent = new Intent(this, MovieHouse.class);
                startActivity(intent);
                break;
            /*case R.id.money_transfer:
                intent = new Intent(this,MoneyTransferActivity.class);
                startActivity(intent);
                break;*/
        }
    }
}
