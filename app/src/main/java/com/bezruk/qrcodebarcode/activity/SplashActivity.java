package com.bezruk.qrcodebarcode.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.bezruk.qrcodebarcode.R;
import com.bezruk.qrcodebarcode.data.constant.Constants;
import com.bezruk.qrcodebarcode.utility.ActivityUtils;
import com.bezruk.qrcodebarcode.utility.AdManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SharedPreferences sharedPreferences=getSharedPreferences(Constants.SHARED_DATA_BASE,MODE_PRIVATE);
        sharedPreferences.edit().putInt(Constants.APP_RESUME,0).apply();

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.splashBody);
        AdManager.getInstance(this).loadFullScreenAd(this,getString(R.string.admobe_intertesial_splash));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                AdManager.getInstance(SplashActivity.this).showSplashScreenAd(SplashActivity.this, new AdManager.onAdShowListener() {
                    @Override
                    public void onAdShow() {

                        ActivityUtils.getInstance().invokeActivity(SplashActivity.this, MainActivity.class, true,0,0);

                    }
                });
            }
        },6000);



    }
}

