package com.iisysgroup.androidlite.vas;

import android.content.Context;

import com.iisysgroup.androidlite.R;

import java.util.ArrayList;

/**
 * Created by Agbede on 3/20/2018.
 */

public class VasEnergyDataGenerator {

    String[] arrays = {"1", "2", "3"};


    public static ArrayList<com.iisysgroup.androidlite.vas.VasItems> generateData(Context context){
        ArrayList<VasItems> vasItemsArrayList = new ArrayList<>();

        String[] title = context.getResources().getStringArray(R.array.vas_energy_title);
        String [] image = context.getResources().getStringArray(R.array.vas_energy_image);

        for (int i = 0; i < title.length; i++){
            VasItems items = new VasItems(title[i], image[i]);
            vasItemsArrayList.add(items);
        }

        return vasItemsArrayList;
    }

}
