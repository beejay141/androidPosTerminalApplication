package com.iisysgroup.androidlite.login;

/**
 * Created by Bamitale@Itex on 5/4/2016.
 */
public class VasResult {
    public Result result = Result.DECLINED;
    public String balance = "";
    public String message = "";
    public String macrosTID = "";
    public String key = "";
    public String commission = "";


    public enum Result {APPROVED, DECLINED}
}
