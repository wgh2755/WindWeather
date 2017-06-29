package com.king.weathertest.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/5/26.
 */

public class Basics {
        @SerializedName("city")
    public String cityName;

    @SerializedName("cnty")
    public String country;

    @SerializedName("id")
    public String weatherId;

    public String lat;

    public String lon;

    @SerializedName("prov")
    public String provinceName;

}
