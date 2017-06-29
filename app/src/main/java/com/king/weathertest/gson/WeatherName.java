package com.king.weathertest.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/5/26.
 */

public class WeatherName {

    @SerializedName("basic")
    public Basics basics;
    public String status;
}
