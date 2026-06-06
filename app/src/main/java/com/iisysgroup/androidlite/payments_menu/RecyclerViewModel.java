package com.iisysgroup.androidlite.payments_menu;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

/**
 * Created by Agbede on 2/8/2018.
 */

public class RecyclerViewModel extends ViewModel {
    private final MutableLiveData<Integer> itemSelected = new MutableLiveData<>();

    void setItemSelected(int position){
        itemSelected.setValue(position);
    }

    LiveData<Integer> getItemSelected(){
        if (itemSelected != null)
            return itemSelected;

        return null;
    }
}
