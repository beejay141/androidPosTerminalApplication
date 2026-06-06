package com.iisysgroup.androidlite.vas;

import android.content.Context;

import com.iisysgroup.androidlite.R;

import java.util.ArrayList;

public class VasDigiPaymentsGenerator {
    public static ArrayList<VasItems> generateData(Context context){
        ArrayList<VasItems> vasItemsArrayList = new ArrayList<>();

        String[] title = context.getResources().getStringArray(R.array.vas_digipayments_title);
        String [] image = context.getResources().getStringArray(R.array.vas_digipayments_image);

        for (int i = 0; i < title.length; i++){
            VasItems items = new VasItems(title[i], image[i]);
            vasItemsArrayList.add(items);
        }

        return vasItemsArrayList;
    }
}
