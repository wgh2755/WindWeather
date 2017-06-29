package com.king.weathertest;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.king.weathertest.db.WeatherInfo;
import com.king.weathertest.gson.DailyForecast;
import com.king.weathertest.gson.HourlyForecast;
import com.king.weathertest.gson.Weather;
import com.king.weathertest.util.HttpUtil;
import com.king.weathertest.util.MyScrollView;
import com.king.weathertest.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class WeatherFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {
    private static final String TAG = "WeatherFragment:arthur";
    private static final int REFRESH_OK = 1;
    private static final int REFRESH_FALSE = 2;
    private TextView titleCity;
    private ImageView iv_nav, iv_del;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private LinearLayout hourlyLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView drsgText;
    private TextView fluText;
    private TextView travText;
    private TextView uvText;
    private TextView sportText;
    private TextView airText;
    private LinearLayout aqiLayout,now_layout,suggest_layout;
    private SwipeRefreshLayout refreshLayout;
    private Weather wea;
    private MainActivity activity;

    public static WeatherFragment newInstance(int pos) {
        WeatherFragment f = new WeatherFragment();
        Bundle args = new Bundle();
        String weatherText = DataSupport.findAll(WeatherInfo.class).get(pos).getWeathertext();
        if (weatherText != null) {
            args.putString("weatherinfo", weatherText);
            f.setArguments(args);
        }
        return f;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather, container, false);
        findById(view);
        Bundle bundle = getArguments();
        String weathertext = bundle.getString("weatherinfo");
        Log.d(TAG, "onCreateView: 获取到天气数据" + weathertext);
        if (weathertext != null) {
            wea = Utility.handleWeatherResponse(weathertext);
            Log.d(TAG, "onCreateView: 获取到数据" + weathertext);
            if (wea != null && wea.status.equals("ok")) {
                showWeatherInfo(wea);
            }
        }
        refreshLayout.setOnRefreshListener(this);
        iv_nav.setOnClickListener(this);
        iv_del.setOnClickListener(this);
        titleCity.setOnClickListener(this);
        if (activity.adapter.getCount() <= 1) iv_del.setVisibility(View.GONE);
        return view;
    }


    private void findById(View view) {
        activity = (MainActivity) getActivity();
        titleCity = view.findViewById(R.id.title_city);
        titleUpdateTime = view.findViewById(R.id.title_update_time);
        degreeText = view.findViewById(R.id.degree_text);
        weatherInfoText = view.findViewById(R.id.weather_info_text);
        hourlyLayout = view.findViewById(R.id.hourlycast_layout);
        forecastLayout = view.findViewById(R.id.forecast_layout);
        aqiText = view.findViewById(R.id.aqi_text);
        pm25Text = view.findViewById(R.id.pm25_text);
        comfortText = view.findViewById(R.id.comfort_text);
        fluText = view.findViewById(R.id.flu_text);
        carWashText = view.findViewById(R.id.car_wash_text);
        drsgText = view.findViewById(R.id.drsg_text);
        travText = view.findViewById(R.id.trav_text);
        uvText = view.findViewById(R.id.uv_text);
        sportText = view.findViewById(R.id.sport_text);
        airText = view.findViewById(R.id.air_text);
        aqiLayout = view.findViewById(R.id.aqi_layout);
        refreshLayout = view.findViewById(R.id.refresh_layout);
        iv_nav = view.findViewById(R.id.nav_image);
        iv_del = view.findViewById(R.id.dele_city);
        now_layout = view.findViewById(R.id.now_layout);
        suggest_layout = view.findViewById(R.id.suggest_layout);
    }


    public void showWeatherInfo(Weather weather) {
        titleCity.setText(weather.basic.cityName);
        Log.d(TAG, "showWeatherInfo: " + weather.status + weather.basic.cityName);
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.tmp + "℃";
        String weatherInfo = weather.now.cond.txt;
        titleUpdateTime.setTextSize(12);
        titleUpdateTime.setText("更新时间 : " + updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        Log.d(TAG, "showWeatherInfo: 2");
        hourlyLayout.removeAllViews();
        forecastLayout.removeAllViews();
        Date date = new Date();
        if (weather.hourlyForecastList.size()>0) {
            for (int i = 0; i < weather.hourlyForecastList.size(); i++) {
                HourlyForecast hourlyForecast = weather.hourlyForecastList.get(i);
                View view = LayoutInflater.from(getActivity()).inflate(R.layout.hourly_item, hourlyLayout, false);
                TextView timeText = view.findViewById(R.id.time_text);
                ImageView condImage = view.findViewById(R.id.cond_image);
                TextView hourlyText = view.findViewById(R.id.hourlyInfo_text);
                TextView hourlyTmp = view.findViewById(R.id.hourlytmp_text);
                TextView dirText = view.findViewById(R.id.dir_text);
                TextView scText = view.findViewById(R.id.sc_text);
                timeText.setText(hourlyForecast.date.split(" ")[1]);
                condImage.setMaxHeight(timeText.getLineHeight());
                String weatherImg = "https://cdn.heweather.com/cond_icon/" + hourlyForecast.cond.code + ".png";
                Glide.with(getActivity()).load(weatherImg).into(condImage);
                hourlyText.setText(hourlyForecast.cond.txt);
                hourlyTmp.setText(hourlyForecast.tmp + "℃");
                dirText.setText(hourlyForecast.wind.dir);
                scText.setText(hourlyForecast.wind.sc);
                hourlyLayout.addView(view);
            }
        }else {
            now_layout.setVisibility(View.GONE);
        }
        for (int i = 0; i < weather.dailyForecastList.size(); i++) {
            DailyForecast dailyForecast = weather.dailyForecastList.get(i);
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = view.findViewById(R.id.date_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);
            SimpleDateFormat sf = new SimpleDateFormat("MM-dd");
            if (i == 0) {
                String nowDate = sf.format(date);
                dateText.setText("今天" + "  " + nowDate);
            } else {
                long time = (date.getTime() / 1000) + 60 * 60 * 24;//秒
                date.setTime(time * 1000);
                String nextDate = sf.format(date);
                dateText.setText(getWeek(dailyForecast.date) + "  " + nextDate);
            }
            if (dailyForecast.cond.txt_d.equals(dailyForecast.cond.txt_n)) {
                infoText.setText(dailyForecast.cond.txt_d);
            } else {
                infoText.setText(dailyForecast.cond.txt_d + "转" + dailyForecast.cond.txt_n);
            }
            maxText.setText(dailyForecast.temperature.max);
            minText.setText(dailyForecast.temperature.min);
            forecastLayout.addView(view);
        }

        if (weather.aqi != null) {
            Log.d(TAG, "showWeatherInfo: aqi true");
            aqiLayout.setVisibility(View.VISIBLE);
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
            String air = "空气质量" + weather.aqi.city.quality;
            airText.setText(air);
        } else {
            aqiLayout.setVisibility(View.GONE);
        }
        Log.d(TAG, "showWeatherInfo: 0022");
        if (weather.suggestion != null) {
            Log.d(TAG, "showWeatherInfo:suggestion ");
            String comfort = "舒适度: " + weather.suggestion.comf.brf + "\n" + weather.suggestion.comf.txt;
            String carWash = "洗车指数: " + weather.suggestion.cw.brf + "\n" + weather.suggestion.cw.txt;
            String drsg = "穿衣指数: " + weather.suggestion.drsg.brf + "\n" + weather.suggestion.drsg.txt;
            String sport = "运动建议: " + weather.suggestion.sport.brf + "\n" + weather.suggestion.sport.txt;
            String uv = "紫外线指数: " + weather.suggestion.uv.brf + "\n" + weather.suggestion.uv.txt;
            String flu = "感冒指数: " + weather.suggestion.flu.brf + "\n" + weather.suggestion.flu.txt;
            String trav = "旅游指数: " + weather.suggestion.trav.brf + "\n" + weather.suggestion.trav.txt;
            comfortText.setText(comfort);
            carWashText.setText(carWash);
            drsgText.setText(drsg);
            fluText.setText(flu);
            sportText.setText(sport);
            travText.setText(trav);
            uvText.setText(uv);
        }else {
        suggest_layout.setVisibility(View.GONE);
        }
        Log.d(TAG, "showWeatherInfo: ");
    }


    public String getWeek(String sdate) {
        // 再转换为时间
        Date date = strToDate(sdate);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return new SimpleDateFormat("EEE").format(c.getTime());
    }

    public Date strToDate(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        ParsePosition pos = new ParsePosition(0);
        return formatter.parse(strDate, pos);
    }

    private Handler handler;
    @Override
    public void onRefresh() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case REFRESH_OK:
                        refreshLayout.setRefreshing(false);
                        Toast.makeText(getActivity(), "刷新成功", Toast.LENGTH_SHORT).show();
                        showWeatherInfo(wea);
                        break;
                    case REFRESH_FALSE:
                        refreshLayout.setRefreshing(false);
                        Toast.makeText(getActivity(), "刷新失败!请连接网络后重试", Toast.LENGTH_SHORT).show();
                }
            }
        };
        Log.d(TAG, "onRefresh: 开始刷新数据");
        String adress = "https://free-api.heweather.com/v5/weather?city=" + wea.basic.weatherId + "&key=885228adb2dd4b52b846c17fcf95e629";
        Log.d(TAG, "onRefresh: " + adress);
        HttpUtil.sendOkHttpRequest(adress, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.sendEmptyMessage(REFRESH_FALSE);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Log.d(TAG, "onResponse: " + responseText);
                WeatherInfo weatherInfo = DataSupport.where("cityName=?", wea.basic.cityName).findFirst(WeatherInfo.class);
                if (weatherInfo != null) {
                    weatherInfo.setWeathertext(responseText);
                    weatherInfo.save();
                }
                Log.d(TAG, "onResponse: 数据保存成功");
                wea = Utility.handleWeatherResponse(responseText);
                if (wea != null && wea.status.equals("ok")) {
                    handler.sendEmptyMessage(REFRESH_OK);
                } else {
                    handler.sendEmptyMessage(REFRESH_FALSE);
                }
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10000);
                    if (refreshLayout.isRefreshing()) {
                        handler.sendEmptyMessage(REFRESH_FALSE);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.nav_image:
                activity.drawerLayout.openDrawer(Gravity.LEFT);
                break;
            case R.id.dele_city:
                activity.showdelDialog();
                break;
            case R.id.title_city:
            default:
        }
    }

}
