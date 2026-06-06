package com.iisysgroup.androidlite.models;

import com.google.gson.annotations.SerializedName;

public class Pfm
{
    @SerializedName("journal")
    public Journal journal;
    @SerializedName("state")
    public State state;

    public Pfm(State stateGenerator, Journal journal)
    {
        this.journal = journal;
        this.state = stateGenerator;
    }
}
