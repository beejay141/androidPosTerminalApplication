package com.iisysgroup.androidlite.vas;

import android.content.Context;

import com.iisysgroup.androidlite.vas.VasCableGenerator;
import com.iisysgroup.androidlite.vas.VasDigiPaymentsGenerator;
import com.iisysgroup.androidlite.vas.VasEducationDataGenerator;
import com.iisysgroup.androidlite.vas.VasEnergyDataGenerator;
import com.iisysgroup.androidlite.vas.VasEventDataGenerator;
import com.iisysgroup.androidlite.vas.VasGamesGenerator;
import com.iisysgroup.androidlite.vas.VasInsuranceGenerator;
import com.iisysgroup.androidlite.vas.VasInternetDataGenerator;
import com.iisysgroup.androidlite.vas.activity.VasAirtimeGenerator;

import java.util.ArrayList;

public class VasAllGenerator {
    public static ArrayList<com.iisysgroup.androidlite.vas.VasItems> generateData(Context context){
        ArrayList<com.iisysgroup.androidlite.vas.VasItems> vasItemsArrayList = new ArrayList<>();

        //Add for airtime
        vasItemsArrayList.addAll(VasAirtimeGenerator.generateData(context));

        //Add for Cable TV
        vasItemsArrayList.addAll(VasCableGenerator.generateData(context));

        //Add for Education Vas
        vasItemsArrayList.addAll(VasEducationDataGenerator.generateData(context));

        //Add for Energy Vas
        vasItemsArrayList.addAll(VasEnergyDataGenerator.generateData(context));

        //Add for Event Vas
        vasItemsArrayList.addAll(VasEventDataGenerator.generateData(context));

        //Add for Games Vas
        vasItemsArrayList.addAll(VasGamesGenerator.generateData(context));

        //Add for Insurance Vas
        vasItemsArrayList.addAll(VasInsuranceGenerator.generateData(context));

        //Add for Internet Data Generator
        vasItemsArrayList.addAll(VasInternetDataGenerator.generateData(context));

        //Add for Digital payments
        vasItemsArrayList.addAll(VasDigiPaymentsGenerator.generateData(context));




        return vasItemsArrayList;
    }

    void accessor(){

    }
}
