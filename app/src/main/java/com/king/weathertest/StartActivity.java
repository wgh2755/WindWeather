package com.king.weathertest;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.security.Permission;
import java.util.ArrayList;
import java.util.List;

public class StartActivity extends Activity {
    private static final int USER_OK = 1;
    private static final int USER_FALSE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        ImageView imageView = findViewById(R.id.start_img);
        Glide.with(this).load(R.drawable.time1).into(imageView);
        permissionsend();
    }

    private void permissionsend() {
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(StartActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(StartActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(StartActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(StartActivity.this, permissions, 1);
        } else {
            handler.sendEmptyMessageAtTime(USER_OK,2000);
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == USER_OK) {
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else if (msg.what == USER_FALSE) {
                permissionsend();
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==1){
            int i = 0;
            for (int result:grantResults){
                if (result!=PackageManager.PERMISSION_GRANTED){
                    showDialog();
                }else {
                    i++;
                }
                if (i  == grantResults.length){
                    handler.sendEmptyMessageAtTime(USER_OK,2000);
                }else {
                    handler.sendEmptyMessage(USER_FALSE);
                }
            }
        }
    }

    private void showDialog() {
        new AlertDialog.Builder(this).setMessage("您必须同意所有权限才能正常使用哦")
                .setNegativeButton("同意", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }).setNeutralButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        }).show();
    }
}
