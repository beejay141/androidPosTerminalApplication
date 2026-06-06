package com.iisysgroup.androidlite.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

/**
 * Created by simileoluwaaluko on 16/02/2018.
 */

public class RefundViewModels extends ViewModel {
    private MutableLiveData<Boolean> goToAccountSelectionForReversal = new MutableLiveData<>();
    private MutableLiveData<Boolean> isUserInAccountSelectionForReversal = new MutableLiveData<>();

    private MutableLiveData<Boolean> goToAccountSelection = new MutableLiveData<>();
    private MutableLiveData<Boolean> isUserInAccountSelection = new MutableLiveData<>();
    private MutableLiveData<Boolean> goToSearch = new MutableLiveData<>();
    private MutableLiveData<Boolean> goToAmountEntry = new MutableLiveData<>();
    private MutableLiveData<Boolean> goToRefundResultDetail = new MutableLiveData<>();
    private MutableLiveData<Boolean> isUserInAmountEntry = new MutableLiveData<>();
    private MutableLiveData<Boolean> isUserInRefundResultDetail = new MutableLiveData<>();
    private MutableLiveData<Boolean> isUserInSearch = new MutableLiveData<>();

    public void setGoToAccountSelection(boolean value){
        goToAccountSelection.setValue(value);
    }

    public LiveData<Boolean> getGoToAccountSelection(){
        return goToAccountSelection;
    }

    public void setIsUserInAccountSelection(boolean value){
        isUserInAccountSelection.setValue(value);
    }

    public boolean getIsUserInAccountSelection(){
        if (isUserInAccountSelection.getValue() != null)
            return isUserInAccountSelection.getValue();

        return false;
    }



    public void setGoToAccountSelectionForReversal(boolean value){
        goToAccountSelectionForReversal.setValue(value);
    }

    public LiveData<Boolean> getGoToAccountSelectionForReversal(){
        return goToAccountSelectionForReversal;
    }

    public void setIsUserInAccountSelectionForReversal(boolean value){
        isUserInAccountSelectionForReversal.setValue(value);
    }

    public boolean getIsUserInAccountSelectionForReversal(){
        if (isUserInAccountSelection.getValue() != null)
            return isUserInAccountSelectionForReversal.getValue();

        return false;
    }



    public void setGoToAmountEntry(boolean value){goToAmountEntry.setValue(value);}

    public void setGoToRefundResultDetail(boolean value){goToRefundResultDetail.setValue(value);}

    public void setGoToSearch(boolean value){goToSearch.setValue(value);}

    public void setIsUserInSearch(boolean value){isUserInSearch.setValue(value);}

    public void setIsUserInAmountEntry(boolean value){isUserInAmountEntry.setValue(value);}

    public void setIsUserInRefundResultDetail(boolean value){isUserInRefundResultDetail.setValue(value);}

    public boolean getIsUserInAmountEntry(){
        if(isUserInAmountEntry.getValue() != null){
            return isUserInAmountEntry.getValue();
        }else{
            return false;
        }
    }

    public boolean getIsUserInRefundResultDetail(){
        if(isUserInRefundResultDetail.getValue() != null){
            return isUserInRefundResultDetail.getValue();
        }else{
            return false;
        }
    }

    public boolean getIsUserInSearch(){
        if(isUserInSearch.getValue() != null){
            return isUserInSearch.getValue();
        }else{
            return false;
        }
    }

    public LiveData<Boolean> getGoToSearch() { return goToSearch; }
    public LiveData<Boolean> getGoToAmountEntry(){return goToAmountEntry;}

    public LiveData<Boolean> getGoToRefundResultDetail() {
        return goToRefundResultDetail;
    }
}
