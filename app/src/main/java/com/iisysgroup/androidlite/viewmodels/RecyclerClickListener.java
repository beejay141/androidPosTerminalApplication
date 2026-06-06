package com.iisysgroup.androidlite.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

/**
 * Created by Agbede on 2/28/2018.
 */

public class RecyclerClickListener extends ViewModel {
    private MutableLiveData<String> passingHolderLiveData = new MutableLiveData<>();

    public void setTransactionResult(String RRN){
        passingHolderLiveData.setValue(RRN);
    }

    public LiveData<String> getTransactionResult(){
        return passingHolderLiveData;
    }
}
