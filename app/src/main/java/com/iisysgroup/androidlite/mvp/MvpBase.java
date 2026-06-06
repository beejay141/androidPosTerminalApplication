package com.iisysgroup.androidlite.mvp;

/**
 * Created by Agbede Samuel D on 2/2/2018.
 */

public class MvpBase {
    public interface View {

    }

    public interface Presenter<V extends MvpBase.View>{
        void addView(V v);
        void removeView();
    }
}
