package com.iisysgroup.androidlite.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

/**
 * Created by Agbede on 2/8/2018.
 */

public class PurchaseViewModels extends ViewModel {
    MutableLiveData<Boolean> goToAccountSelection = new MutableLiveData<>();
    MutableLiveData<Boolean> goToCashAdvanceEnter = new MutableLiveData<>();
    MutableLiveData<Boolean> isUserInAccountSelection = new MutableLiveData<>();
    MutableLiveData<Boolean> isUserInAmount = new MutableLiveData<>();

    public void setGoToAccountSelection(boolean value){
        goToAccountSelection.setValue(value);
    }

    public void setGoToCashAdvanceEnter(boolean value){goToCashAdvanceEnter.setValue(value);}

    public LiveData<Boolean> getGoToAccountSelection(){
        return goToAccountSelection;
    }

    public LiveData<Boolean> getGoToCashAdvanceEnter(){
        return goToCashAdvanceEnter;
    }

    public boolean isUserInAccountSelection(){
        if (isUserInAccountSelection.getValue() != null)
        return isUserInAccountSelection.getValue();

        return false;
    }
    public void setIsUserInAccountSelection(boolean value){
        isUserInAccountSelection.setValue(value);
    }

    public void setIsUserInAmount(boolean value){
        isUserInAmount.setValue(value);
    }

    public boolean getIsUserInAmount(){
        if (isUserInAmount.getValue() != null)
        return isUserInAmount.getValue();

        return false;
    }
}