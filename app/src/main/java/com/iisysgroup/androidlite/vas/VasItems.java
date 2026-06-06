package com.iisysgroup.androidlite.vas;

/**
 * Created by Agbede on 3/20/2018.
 */

public class VasItems {
    String title;
    String ImageDrawable;

    public VasItems(String title, String ImageDrawable){
        this.title = title;
        this.ImageDrawable = ImageDrawable;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageDrawable() {
        return ImageDrawable;
    }

    public void setImageDrawable(String imageDrawable) {
        ImageDrawable = imageDrawable;
    }

}
