package com.king.weathertest.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import com.google.gson.annotations.SerializedName;
import com.king.weathertest.WeatherFragment;
import com.king.weathertest.db.WeatherInfo;
import com.king.weathertest.gson.Basics;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2017/6/15.
 */

public class MyViewPager extends FragmentStatePagerAdapter {

    public MyViewPager(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public Fragment getItem(int position){
        return WeatherFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return DataSupport.findAll(WeatherInfo.class).size();
    }

    /**
     * Created by Administrator on 2017/5/26.
     */

    public static class WeatherName {

        @SerializedName("basic")
        public Basics basics;
        public String status;
    }
}
