package com.iisysgroup.androidlite.mvp;

/**
 * Created by Agbede Samuel D on 2/2/2018.
 */

public class BasePresenter<V extends MvpBase.View> implements MvpBase.Presenter<V>{

    V view;

    public V getView(){
        return this.view;
    }

    public void checkIfViewIsAttached(){
        if (view == null){
            throw new MVPException();
        }
    }

    @Override
    public void addView(V v) {
        this.view = v;
    }

    @Override
    public void removeView() {
        this.view = null;
    }

    public static class MVPException extends RuntimeException{
        public MVPException(){
            super("View is null");
        }
    }
}
