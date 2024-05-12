package com.bezruk.qrcodebarcode.ADS;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.bezruk.qrcodebarcode.R;
import com.bezruk.qrcodebarcode.data.constant.Constants;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;

import java.util.Date;

public class MyApplication extends Application implements Application.ActivityLifecycleCallbacks, LifecycleObserver {


    private AppOpenAdManager appOpenAdManager;
    private Activity currentActivity;
    SharedPreferences sharedPreferences;
    private static final String TAG = "MyApplication";
    SharedPreferences.Editor editor;
    int resume_check;

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = getSharedPreferences(Constants.SHARED_DATA_BASE, MODE_PRIVATE);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        registerActivityLifecycleCallbacks(this);
        appOpenAdManager = new AppOpenAdManager();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    void oncreate() {
        editor = sharedPreferences.edit();
        editor.putInt(Constants.APP_RESUME, 0);
        editor.apply();

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void onDestroy() {
        editor = sharedPreferences.edit();
        editor.putInt(Constants.APP_RESUME, 0);
        editor.apply();



    }
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    void onPause(){


    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    void onResume() {
        resume_check = sharedPreferences.getInt(Constants.APP_RESUME, 0);
        if (resume_check == 1) {
            appOpenAdManager.showAdIfAvailable(currentActivity,getString(R.string.admobe_app_open));
        }



    }

    @Override



    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (!appOpenAdManager.isShowingAd) {
            currentActivity = activity;
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }
    private class AppOpenAdManager {

        private static final String LOG_TAG = "AppOpenAdManager";


        private AppOpenAd appOpenAd = null;
        private boolean isLoadingAd = false;
        private boolean isShowingAd = false;

        /**
         * Keep track of the time an app open ad is loaded to ensure you don't show an expired ad.
         */
        private long loadTime = 0;

        /**
         * Constructor.
         */
        public AppOpenAdManager() {
        }


        private void loadAd(Context context, String adId) {
            // Do not load ad if there is an unused ad or one is already loading.
            if (isLoadingAd || isAdAvailable()) {
                return;
            }

            isLoadingAd = true;


            AdRequest request = new AdRequest.Builder().build();


            try {
                AppOpenAd.load(
                        context,
                        adId,
                        request,
                        new AppOpenAd.AppOpenAdLoadCallback() {
                            /**
                             * Called when an app open ad has loaded.
                             *
                             * @param ad the loaded app open ad.
                             */
                            @Override
                            public void onAdLoaded(@NonNull AppOpenAd ad) {
                                appOpenAd = ad;
                                isLoadingAd = false;
                                loadTime = (new Date()).getTime();

                                Log.d(LOG_TAG, "onAdLoaded.");
                                //  Toast.makeText(context, "onAdLoaded", Toast.LENGTH_SHORT).show();
                            }

                            /**
                             * Called when an app open ad has failed to load.
                             *
                             * @param loadAdError the error.
                             */
                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                isLoadingAd = false;
                                Log.d(LOG_TAG, "onAdFailedToLoad: " + loadAdError.getMessage());
                                // Toast.makeText(context, "onAdFailedToLoad", Toast.LENGTH_SHORT).show();
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }




        }

        /** Check if ad was loaded more than n hours ago. */
        private boolean wasLoadTimeLessThanNHoursAgo() {
            long dateDifference = (new Date()).getTime() - loadTime;
            long numMilliSecondsPerHour = 3600000;
            return (dateDifference < (numMilliSecondsPerHour * (long) 4));
        }

        /** Check if ad exists and can be shown. */
        private boolean isAdAvailable() {
            // Ad references in the app open beta will time out after four hours, but this time limit
            // may change in future beta versions. For details, see:
            // https://support.google.com/admob/answer/9341964?hl=en
            return appOpenAd != null && wasLoadTimeLessThanNHoursAgo();
        }

        /**
         * Show the ad if one isn't already showing.
         *
         * @param activity the activity that shows the app open ad
         */
        private void showAdIfAvailable(@NonNull final Activity activity, String adId) {
            showAdIfAvailable(activity,adId,
                    new OnShowAdCompleteListener() {
                        @Override
                        public void onShowAdComplete() {
                            // Empty because the user will go back to the activity that shows the ad.
                        }
                    });
        }


        private void showAdIfAvailable(
                @NonNull final Activity activity,String adId,
                @NonNull OnShowAdCompleteListener onShowAdCompleteListener) {
            // If the app open ad is already showing, do not show the ad again.
            if (isShowingAd) {
                Log.d(LOG_TAG, "The app open ad is already showing.");
                return;
            }

            // If the app open ad is not available yet, invoke the callback then load the ad.
            if (!isAdAvailable()) {
                Log.d(LOG_TAG, "The app open ad is not ready yet.");
                onShowAdCompleteListener.onShowAdComplete();
                loadAd(activity,adId);
                return;
            }

            Log.d(LOG_TAG, "Will show ad.");

            appOpenAd.setFullScreenContentCallback(
                    new FullScreenContentCallback() {
                        /** Called when full screen content is dismissed. */
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            // Set the reference to null so isAdAvailable() returns false.
                            appOpenAd = null;
                            isShowingAd = false;

                            Log.d(LOG_TAG, "onAdDismissedFullScreenContent.");
                            //  Toast.makeText(activity, "onAdDismissedFullScreenContent", Toast.LENGTH_SHORT).show();

                            onShowAdCompleteListener.onShowAdComplete();
                            loadAd(activity, adId);
                        }

                        /** Called when fullscreen content failed to show. */
                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            appOpenAd = null;
                            isShowingAd = false;

                            Log.d(LOG_TAG, "onAdFailedToShowFullScreenContent: " + adError.getMessage());
                            // Toast.makeText(activity, "onAdFailedToShowFullScreenContent", Toast.LENGTH_SHORT)
                            //   .show();

                            onShowAdCompleteListener.onShowAdComplete();
                            loadAd(activity, adId);
                        }

                        /** Called when fullscreen content is shown. */
                        @Override
                        public void onAdShowedFullScreenContent() {
                            Log.d(LOG_TAG, "onAdShowedFullScreenContent.");
                            //  Toast.makeText(activity, "onAdShowedFullScreenContent", Toast.LENGTH_SHORT).show();
                        }
                    });

            isShowingAd = true;
            appOpenAd.show(activity);
        }
    }
    public interface OnShowAdCompleteListener {
        void onShowAdComplete();
    }
}
