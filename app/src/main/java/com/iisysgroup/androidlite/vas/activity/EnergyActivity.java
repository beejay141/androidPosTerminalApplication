package com.iisysgroup.androidlite.vas.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.iisysgroup.androidlite.R;
import com.iisysgroup.androidlite.vas.VasAdapter;
import com.iisysgroup.androidlite.vas.VasBaseActivity;
import com.iisysgroup.androidlite.vas.VasEnergyDataGenerator;
import com.iisysgroup.androidlite.vas.VasItems;
import com.iisysgroup.androidlite.vas.activity.energy.Abuja.AbujaElectric;
import com.iisysgroup.androidlite.vas.activity.energy.Eko.EkoElectric;
import com.iisysgroup.androidlite.vas.activity.energy.Enugu.EnuguElectric;
import com.iisysgroup.androidlite.vas.activity.energy.Ibadan.Ibedc;
import com.iisysgroup.androidlite.vas.activity.energy.Ikeja.IkejaElectric;
import com.iisysgroup.androidlite.vas.activity.energy.Kaduna.KadunaElectric;
import com.iisysgroup.androidlite.vas.activity.energy.Kano.KanoElectric;
import com.iisysgroup.androidlite.vas.activity.energy.PortHarcourt.PHElectric;

import java.util.ArrayList;

public class EnergyActivity  extends VasBaseActivity implements VasAdapter.VasClickListener {

    public static String ENERGY_ENERGY = "energy_prepaid_type";
    public static String ENERGY_NO_ENERGY = "energy_postpaid_type";

    public static String ENERGY_TYPE = "energy_type";

    Toolbar toolbar;
    RecyclerView recyclerView;
    ArrayList<VasItems> vasItems;


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
        setToolbar(toolbar);
    }


    @Override
    public void onVasItemClick(ArrayList<VasItems> vasItemsArrayList, int position) {
        switch (position){
            case 0 : startActivity(new Intent(this, EkoElectric.class));
                break;
            case 1 : startActivity(new Intent(this, Ibedc.class));
                break;
            case 2 : startActivity(new Intent(this, IkejaElectric.class));
                break;
            case 3 : startActivity(new Intent(this, AbujaElectric.class));
                break;
            case 4 : startActivity(new Intent(this, EnuguElectric.class));
                break;
//            case 5 : startActivity(new Intent(this, KadunaElectric.class));
//                break;
//            case 6 : startActivity(new Intent(this, KanoElectric.class));
//                break;
            case 5 : startActivity(new Intent(this, PHElectric.class));
                break;
        }
    }
}
