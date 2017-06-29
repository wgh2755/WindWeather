package com.king.weathertest;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.king.weathertest.db.City;
import com.king.weathertest.db.County;
import com.king.weathertest.db.Province;
import com.king.weathertest.db.WeatherInfo;
import com.king.weathertest.gson.Weather;
import com.king.weathertest.util.HttpUtil;
import com.king.weathertest.util.MyViewPager;
import com.king.weathertest.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements DrawerLayout.DrawerListener, View.OnClickListener,CompoundButton.OnCheckedChangeListener {
    private static final int LEVEL_PROVINCE = 0;
    private static final int LEVEL_CITY = 1;
    private static final int LEVEL_COUNTY = 2;
    private TextView noti_city,noti_info,noti_tmp,noti_mix_max;
    private Switch switch_desktop,switch_notification;
    public List<WeatherFragment> weatherFragmentList;
    private List<String> dataList;
    private ArrayAdapter<String> cityAdapter;
    public MyViewPager adapter;
    private ListView navList;
    public ViewPager viewPager;
    private FragmentManager fm;
    private ImageView bingPicImg;
    public DrawerLayout drawerLayout;
    private TextView title_query;
    private ImageView image_back;
    private static final String TAG = "MainActivity:arthur";
    //省列表
    private List<Province> provinceList;
    //市列表
    private List<City> cityList;
    //县列表
    private List<County> countyList;
    //选中的省份
    private Province selectedProvince;
    //选中的城市
    private City selectedCity;

    //当前选中的级别
    public int currentLevel;

    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLocationClient = new LocationClient(getApplicationContext());//声明LocationClient类
        mLocationClient.registerLocationListener(myListener);//注册监听函数
        initLocation();
        permissionSend();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        weatherFragmentList = new ArrayList<>();
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navList = (ListView) findViewById(R.id.left_drawer);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        image_back = (ImageView) findViewById(R.id.nav_back);
        title_query = (TextView) findViewById(R.id.title_query);
        image_back.setOnClickListener(this);
        fm = getSupportFragmentManager();
        initListView();
        initFramgent();
        loadImg();
        drawerLayout.addDrawerListener(this);
        viewPager.setOffscreenPageLimit(4);
    }

    private void initFramgent() {
        List<WeatherInfo> weatherInfos = DataSupport.findAll(WeatherInfo.class);
        Log.d(TAG, "initFramgent: 获取到的长度" + weatherInfos.size());
            adapter = new MyViewPager(fm);
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(0);
    }

    private void initListView() {
        dataList = new ArrayList<>();
        cityAdapter = new ArrayAdapter<>(this, R.layout.nav_list_item, dataList);
        navList.setAdapter(cityAdapter);
        navList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (currentLevel) {
                    case LEVEL_PROVINCE:
                        selectedProvince = provinceList.get(position);
                        queryCities();
                        break;
                    case LEVEL_CITY:
                        selectedCity = cityList.get(position);
                        queryCounties();
                        break;
                    case LEVEL_COUNTY:
                        County county = countyList.get(position);
                        addWeatherInfo(county.getWeatherId(), county.getCountyName());
                        drawerLayout.closeDrawers();
                        break;
                    default:
                }
            }
        });
        queryProvinces();
    }


    public void initWeatherInfo(String cityName) {
        String cityAdress = "https://api.heweather.com/v5/search?city=" + cityName + "&key=885228adb2dd4b52b846c17fcf95e629";
        Log.d(TAG, "weatherinfotext: 1" + cityAdress);
        HttpUtil.sendOkHttpRequest(cityAdress, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String contentText = response.body().string();
                MyViewPager.WeatherName weatherName = Utility.handleBasicResponse(contentText);
                if (weatherName != null && weatherName.status.equals("ok")) {
                    Log.d(TAG, "onResponse: 2" + weatherName.basics.weatherId);
                    String weatherId = weatherName.basics.weatherId;
                    String adress = "https://free-api.heweather.com/v5/weather?city=" + weatherId + "&key=885228adb2dd4b52b846c17fcf95e629";
                    HttpUtil.sendOkHttpRequest(adress, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                        }
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String responseText = response.body().string();
                            Weather weather = Utility.handleWeatherResponse(responseText);
                            if (weather != null && weather.status.equals("ok")) {
                                WeatherInfo weatherInfo = DataSupport.where("cityName=?", weather.basic.cityName).findFirst(WeatherInfo.class);
                                if (weatherInfo == null) {
                                    weatherInfo = new WeatherInfo();
                                    weatherInfo.setWeathertext(responseText);
                                    weatherInfo.setCityName(weather.basic.cityName);
                                    weatherInfo.save();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            adapter.notifyDataSetChanged();
                                            viewPager.setCurrentItem(adapter.getCount()-1);
                                        }
                                    });
                                } else {
                                    List<WeatherInfo> weatherInfos = DataSupport.findAll(WeatherInfo.class);
                                    for (int i = 0; i < weatherInfos.size(); i++) {
                                        WeatherInfo wi = weatherInfos.get(i);
                                        if (wi.getCityName().equals(weather.basic.cityName)) {
                                            final int pos = i;
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    viewPager.setCurrentItem(pos);
                                                }
                                            });

                                        }
                                    }
                                }
                            }
                        }
                    });
                } else {
                    Log.d(TAG, "onResponse: 获取的天气ID无效");
                }
            }
        });
    }

    public void addWeatherInfo(String weatherId,String weatherName) {
        String adress = "https://free-api.heweather.com/v5/weather?city=" + weatherId + "&key=885228adb2dd4b52b846c17fcf95e629";
        HttpUtil.sendOkHttpRequest(adress, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                Weather weather = Utility.handleWeatherResponse(responseText);
                if (weather != null && weather.status.equals("ok")) {
                    WeatherInfo weatherInfo = DataSupport.where("cityName=?", weather.basic.cityName).findFirst(WeatherInfo.class);
                    if (weatherInfo == null) {
                        weatherInfo = new WeatherInfo();
                        weatherInfo.setWeathertext(responseText);
                        weatherInfo.setCityName(weather.basic.cityName);
                        weatherInfo.save();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                                viewPager.setCurrentItem(adapter.getCount() - 1);
                            }
                        });
                    } else {
                        List<WeatherInfo> weatherInfos = DataSupport.findAll(WeatherInfo.class);
                        for (int i = 0; i < weatherInfos.size(); i++) {
                            WeatherInfo wi = weatherInfos.get(i);
                            if (wi.getCityName().equals(weather.basic.cityName)) {
                                final int pos = i;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        viewPager.setCurrentItem(pos);
                                    }
                                });

                            }
                        }
                    }
                } else {
                    Log.d(TAG, "onResponse: 获取天气数据失败");
                }
            }
        });
    }

    private void loadBingPic() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String bingPic = prefs.getString("bingPic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop().into(bingPicImg);
        } else {
            String requestBingPic = "http://guolin.tech/api/bing_pic";
            HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String bingPic = response.body().string();
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
                    editor.putString("bing_pic", bingPic);
                    editor.putInt("date",Calendar.getInstance().get(Calendar.DAY_OF_YEAR));
                    editor.apply();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(MainActivity.this).load(bingPic).into(bingPicImg);
                        }
                    });
                }
            });
        }
    }

    private static final String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/bj.jpg";
    private void loadImg() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int date = prefs.getInt("date", 0);
        Boolean isDown = prefs.getBoolean("path", false);
        Log.d(TAG, "loadImg: ---"+path);
        if (isDown&& date == Calendar.getInstance().get(Calendar.DAY_OF_YEAR)) {
            Glide.with(this).load(path).diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop().into(bingPicImg);
        } else {
            String requestBingPic = "http://guolin.tech/api/bing_pic";
            HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String imgbic = response.body().string();
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(imgbic).build();
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {

                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            InputStream is = null;
                            byte[] buf = new byte[2048];
                            int len;
                            FileOutputStream fos = null;
                            Log.d(TAG, "onResponse: 需要下载的文件"+path);
                            try {
                                long total = response.body().contentLength();
                                Log.e(TAG, "total------>" + total);
                                long current = 0;
                                is = response.body().byteStream();
                                fos = new FileOutputStream(path);
                                while ((len = is.read(buf)) != -1) {
                                    current += len;
                                    fos.write(buf, 0, len);
                                    Log.e(TAG, "current------>" + current);
                                }
                                fos.flush();
                                Log.d(TAG, "onResponse: 下载成功");
                                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
                                editor.putBoolean("path",true);
                                editor.putInt("date",Calendar.getInstance().get(Calendar.DAY_OF_YEAR));
                                editor.apply();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Glide.with(MainActivity.this).load(path).diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop().into(bingPicImg);
                                    }
                                });
                            } catch (IOException e) {
                                Log.d(TAG, "onResponse: 下载失败");
                            } finally {
                                try {
                                    if (is != null) {
                                        is.close();
                                    }
                                    if (fos != null) {
                                        fos.close();
                                    }
                                } catch (IOException e) {
                                    Log.e(TAG, e.toString());
                                }
                            }
                        }
                    });
                }
            });
        }
    }

    //查询全国所有的省,优先从数据库查询,如果没有查询到再去服务器上获取
    public void queryProvinces() {
        title_query.setText("全国");
        image_back.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        Log.d(TAG, "queryProvinces:准备查询数据 ");
        if (provinceList.size() > 0) {
            if (dataList.size() > 0) {
                dataList.clear();
            }
            Log.d(TAG, "queryProvinces: " + provinceList.size());
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            Log.d(TAG, "queryProvinces: " + dataList.size());
            cityAdapter.notifyDataSetChanged();
            navList.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            String address = "http://guolin.tech/api/china";
            Log.d(TAG, "queryProvinces: 请求省份数据");
            queryFromServer(address, LEVEL_PROVINCE);
        }
    }

    //查询选中的省份的所有城市,优先读取本地数据库,如果没有查询到再去服务器上查询
    private void queryCities() {
        title_query.setText(selectedProvince.getProvinceName());
        image_back.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid = ?", String.valueOf(selectedProvince.getId())).find(City.class);
        Log.d(TAG, "queryLeaders:--- " + "--leaderList size-" + cityList.size());
        if (cityList.size() > 0) {
            if (dataList != null) {
                dataList.clear();
            }
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            cityAdapter.notifyDataSetChanged();
            navList.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            String adress = "http://guolin.tech/api/china/" + provinceCode;
            Log.d(TAG, "queryCities: 请求地级市数据");
            queryFromServer(adress, LEVEL_CITY);
        }
    }

    //查询选中的市内的所有县数据,优先查数据库,没有就网上下载
    private void queryCounties() {
        title_query.setText(selectedCity.getCityName());
        image_back.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?", String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size() > 0) {
            Log.d(TAG, "querycounties: " + countyList.size());
            if (dataList != null) {
                dataList.clear();
            }
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            cityAdapter.notifyDataSetChanged();
            navList.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectedCity.getProvinceId();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            Log.d(TAG, "queryCounties: 请求县级数据");
            queryFromServer(address, LEVEL_COUNTY);
        }
    }

    //根据传入的地址以及类型,从服务器上查询所有省市县数据
    private void queryFromServer(String adress, final int type) {
        Log.d(TAG, "queryFromServer: " + adress + type);
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(adress, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Log.d(TAG, "onResponse: " + responseText);
                Boolean result = false;
                switch (type) {
                    case LEVEL_PROVINCE:
                        result = Utility.handleProvinceResponse(responseText);
                        Log.d(TAG, "onResponse: 展示省级界面");
                        break;
                    case LEVEL_CITY:
                        result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                        Log.d(TAG, "onResponse: 展示市级界面");
                        break;
                    case LEVEL_COUNTY:
                        result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                        Log.d(TAG, "onResponse: 展示县级界面");
                        break;
                    default:
                }
                if (result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            switch (type) {
                                case LEVEL_PROVINCE:
                                    queryProvinces();
                                    break;
                                case LEVEL_CITY:
                                    queryCities();
                                    break;
                                case LEVEL_COUNTY:
                                    queryCounties();
                                    break;
                                default:
                            }
                        }
                    });
                }

            }
        });
    }

    private ProgressDialog dialog;
    private void showProgressDialog() {
        dialog = new ProgressDialog(this);
        dialog.setMessage("正在加载中");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
    }

    private void closeProgressDialog() {
        dialog.dismiss();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                switch (currentLevel) {
                    case LEVEL_PROVINCE:
                        drawerLayout.closeDrawers();
                        break;
                    case LEVEL_CITY:
                        queryProvinces();
                        break;
                    case LEVEL_COUNTY:
                        queryCities();
                        break;
                    default:
                }
            } else {
                exitTime();
            }
        }
        return true;
    }

    long now = 0;
    private void exitTime() {
        long time = System.currentTimeMillis();
        if (time - now > 2000) {
            Toast.makeText(this, "再点一次退出软件", Toast.LENGTH_SHORT).show();
            now = time;
        } else {
            finish();
        }
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
    }

    @Override
    public void onDrawerOpened(View drawerView) {
    }

    @Override
    public void onDrawerClosed(View drawerView) {
        queryProvinces();
    }

    @Override
    public void onDrawerStateChanged(int newState) {
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.nav_image:
                drawerLayout.openDrawer(Gravity.LEFT);
                break;
            case R.id.dele_city:
                showdelDialog();
                break;
            case R.id.nav_back:
                if (currentLevel==LEVEL_CITY){
                    queryProvinces();
                }else if (currentLevel==LEVEL_COUNTY){
                    queryCities();
                }
        }
    }

    public void showdelDialog() {
        final List<WeatherInfo> wis = DataSupport.findAll(WeatherInfo.class);
        if (wis.size() > 1) {
            new AlertDialog.Builder(this).setTitle("删除城市").setMessage("你确定要删除吗?")
                    .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            int select = viewPager.getCurrentItem();
                            WeatherInfo info = wis.get(select);
                            info.delete();
                            adapter.notifyDataSetChanged();
                        }
                    }).setNeutralButton("取消", null).show();
        }
    }

    public void showSetDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("系统设置").create();
        dialog.show();
        dialog.getWindow().setContentView(R.layout.diagset_layout);
        switch_desktop = dialog.findViewById(R.id.switch_desktop);
        switch_notification = dialog.findViewById(R.id.switch_notification);
        switch_desktop.setChecked(Utility.getShareButton(this,"desktop"));
        switch_notification.setChecked(Utility.getShareButton(this,"notification"));
        switch_desktop.setOnCheckedChangeListener(this);
        switch_notification.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()){
            case R.id.switch_desktop:
                if (b){
                    showDesktop();//桌面展示
                }else {
                    closeDesktop();//关闭桌面展示
                }
                Utility.saveShare(this,"desktop",b);
                break;
            case R.id.switch_notification:
                if (b){
                    sendNotification();//显示天气通知

                }else {
                    closeNotification();//关闭天气通知
                }
                Utility.saveShare(this,"notification",b);
                break;
        }
    }

    private void closeDesktop() {

    }

    private void showDesktop() {
    }


    NotificationCompat.Builder builder;
    private void closeNotification() {
        builder.setVisibility(View.GONE);
    }

    private void sendNotification() {
        builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.weather);
        builder.setAutoCancel(false);
        RemoteViews views = new RemoteViews(getPackageName(),R.layout.notification_layout);
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(1,builder.build());
        builder.setContent(views);
        builder.build();
    }

    private class MyLocationListener implements BDLocationListener {
        private StringBuffer sb;

        @Override
        public void onReceiveLocation(BDLocation location) {
            //获取定位结果
            if (location != null) {
                sb = new StringBuffer(256);
                sb.append(location.getDistrict());
                Log.d(TAG, "onReceiveLocation: " + location.getAddrStr());
                initWeatherInfo(sb.toString().replaceAll("市辖区", ""));
            } else {
                Log.d(TAG, "onReceiveLocation: 定位失败");
            }
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {
        }
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备

        option.setCoorType("bd09ll");
        //可选，默认gcj02，设置返回的定位结果坐标系

        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要

        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps

        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”

        option.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到

        option.setIgnoreKillProcess(true);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死

        option.SetIgnoreCacheException(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集
        mLocationClient.setLocOption(option);
//        mLocationClient.start();
        Log.d(TAG, "initLocation: 定位设置完毕");
    }

    private void permissionSend() {
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        } else {
            mLocationClient.start();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须开通相关权限", Toast.LENGTH_SHORT).show();
                        }
                    }
                    mLocationClient.start();
                }
                break;
            default:
        }
    }


}
