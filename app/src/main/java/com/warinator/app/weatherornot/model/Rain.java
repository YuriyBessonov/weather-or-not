package com.warinator.app.weatherornot.model;

/**
 * Created by Warinator on 10.07.2017.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Rain {

    @SerializedName("3h")
    @Expose
    private int _3h;

    public int get3h() {
        return _3h;
    }

    public void set3h(int _3h) {
        this._3h = _3h;
    }

}