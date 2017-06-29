package com.king.weathertest.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.king.weathertest.db.City;
import com.king.weathertest.db.County;
import com.king.weathertest.db.Province;
import com.king.weathertest.db.WeatherInfo;
import com.king.weathertest.gson.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/5/20.
 */

public class Utility {
    private static final String TAG = "Utility:hua";

    /*
    * 解析和处理服务器返回的省级数据
    */
    public static boolean handleProvinceResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allProvinces = new JSONArray(response);
                for (int i = 0; i < allProvinces.length(); i++) {
                    JSONObject cityObject = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceCode(cityObject.getInt("id"));
                    province.setProvinceName(cityObject.getString("name"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /*
    * 解析和处理服务器返回的市级数据
    */
    public static boolean handleCityResponse(String response,int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCities = new JSONArray(response);
                for (int i = 0; i < allCities.length(); i++) {
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);

                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /*
    * 解析和处理服务器返回的县级数据
    */

    public static boolean handleCountyResponse(String response,int cityId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCounties = new JSONArray(response);
                for (int i = 0; i < allCounties.length(); i++) {
                    JSONObject cityObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setWeatherId(cityObject.getString("weather_id"));
                    Log.d(TAG, "handleCountyResponse: "+cityObject.getString("name"));
                    county.setCountyName(cityObject.getString("name"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    public static MyViewPager.WeatherName handleBasicResponse(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather5");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,MyViewPager.WeatherName.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static Weather handleWeatherResponse(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather5");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,Weather.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveShare(Context context ,String name, Boolean open){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(name,open);
        editor.apply();
    }

    public static Boolean getShareButton(Context context,String name){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(name,false);
    }


    public String getLocationCityName(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("city",null);
    }

    public void saveLocationCityName(Context context,String cityName){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString("city",cityName);
        editor.apply();
    }
}
