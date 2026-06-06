package com.iisysgroup.androidlite.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Agbede on 3/14/2018.
 */

public class TimeUtils {
    public static String convertLongToString(long time){
        Date date = new Date(time);
        return date.toString();
    }
    public static String convertLongToTime(long time){
        Date date = new Date(time);
        String newtime = new SimpleDateFormat("H:mm").format(date);
        return newtime;
    }
}