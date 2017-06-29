package com.king.weathertest.db;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2017/5/27.
 */

public class WeatherInfo extends DataSupport{
    private int id;
    private String cityName;
    private String weathertext;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getWeathertext() {
        return weathertext;
    }

    public void setWeathertext(String weathertext) {
        this.weathertext = weathertext;
    }
}
