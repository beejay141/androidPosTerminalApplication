package com.iisysgroup.androidlite.viewmodels;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

/**
 * Created by OFURE on 3/6/2018.
 */

public class TransferViewModel extends ViewModel{

    MutableLiveData<Boolean> goToAccNumber = new MutableLiveData<>();
    MutableLiveData<Boolean> goToAmount = new MutableLiveData<>();
    MutableLiveData<Boolean> goToBankSelection  = new MutableLiveData<>();
    MutableLiveData<Boolean> IsUserInAccNumber = new MutableLiveData<>();
    MutableLiveData<Boolean> IsUserInAmount = new MutableLiveData<>();
    MutableLiveData<Boolean> IsUserInBankSelection = new MutableLiveData<>();




    public void setGoToAccNumber(boolean value) {goToAccNumber.setValue(value);}

    public void setGoToAmount(boolean value) {goToAmount.setValue(value);}

    public void setGoToBankSelection(boolean value) {goToBankSelection.setValue(value);}

    public LiveData<Boolean> getGoToAccNumber() {return goToAccNumber;}

    public LiveData<Boolean> getGoToAmount() {return goToAmount;}

    public LiveData<Boolean>  getGoToBankSelection(){return goToBankSelection;}
    public boolean getIsUserInAccNumber()
    {
        if (IsUserInAccNumber.getValue() != null)
            return IsUserInAccNumber.getValue();

        return false;
    }

    public void setIsUserInAccNumber(boolean value)
    {
        IsUserInAccNumber.setValue(value);
    }

    public boolean getIsUserInAmount()
    {
        if (IsUserInAmount.getValue() != null)
            return IsUserInAmount.getValue();
        return false;
    }

    public void setIsUserInAmount(boolean value)
    {
        IsUserInAmount.setValue(value);
    }

    public boolean getIsUserInBankSelection()
    {
        if (IsUserInBankSelection.getValue() != null)
            return IsUserInBankSelection.getValue();

        return false;
    }
    public void setIsUserInBankSelection(boolean value){IsUserInBankSelection.setValue(value);}


}
