package com.bezruk.qrcodebarcode.utility;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import com.appbrain.AdId;
import com.appbrain.InterstitialBuilder;
import com.bezruk.qrcodebarcode.R;
import com.bezruk.qrcodebarcode.data.preference.AppPreference;
import com.bezruk.qrcodebarcode.data.preference.PrefKey;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;


public class AdManager {

    private static AdManager adUtils;

    private InterstitialAd mInterstitialAd;
    private Boolean isAdsVisibility;
    String Banner, Full, BannerPr, FullPr;
    public static InterstitialBuilder interstitialBuilder;

    private AdManager(Context context) {
        MobileAds.initialize(context);
        isAdsVisibility = AppPreference.getInstance(context).getBoolean(PrefKey.ADS_VISIBILITY,true);
    }

    public static AdManager getInstance(Context context) {
        if (adUtils == null) {
            adUtils = new AdManager(context);
        }
        return adUtils;
    }

    public void showBannerAd(final AdView mAdView) {
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                if(isAdsVisibility){
                    mAdView.setVisibility(View.VISIBLE);
                } else {
                    mAdView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                mAdView.setVisibility(View.GONE);
            }
        });

    }

    public void loadFullScreenAd(Activity activity,String adID) {
        interstitialBuilder = InterstitialBuilder.create()
                .setAdId(AdId.LEVEL_COMPLETE)
                .setOnDoneCallback(() -> {
                    if (interstitialBuilder != null) {
                        interstitialBuilder.preload(activity);
                    }
                })
                .preload(activity);

        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(activity, adID, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                mInterstitialAd=null;
            }

            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                super.onAdLoaded(interstitialAd);
                mInterstitialAd=interstitialAd;
            }
        });





    }

    public boolean showFullScreenAd(Activity activity, onAdShowListener adShowListener) {

        if(isAdsVisibility) {

            if (mInterstitialAd!=null) {
                mInterstitialAd.show(activity);
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent();
                        adShowListener.onAdShow();
                        mInterstitialAd=null;
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        super.onAdFailedToShowFullScreenContent(adError);
                        adShowListener.onAdShow();
                        mInterstitialAd=null;
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        super.onAdShowedFullScreenContent();
                    }
                });

                return true;
            }else {
                if (interstitialBuilder != null) {
                    interstitialBuilder.show(activity);
                }
                adShowListener.onAdShow();
            }
        }else {

            adShowListener.onAdShow();
        }
        return false;
    }
    public boolean showSplashScreenAd(Activity activity, onAdShowListener adShowListener) {

            if (mInterstitialAd!=null) {
                mInterstitialAd.show(activity);
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent();
                        adShowListener.onAdShow();
                        mInterstitialAd=null;
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        super.onAdFailedToShowFullScreenContent(adError);
                        adShowListener.onAdShow();
                        mInterstitialAd=null;
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        super.onAdShowedFullScreenContent();
                    }
                });

                return true;
            }else {
                if (interstitialBuilder != null) {
                    interstitialBuilder.show(activity);
                }
                adShowListener.onAdShow();
            }

        return false;
    }


    public interface onAdShowListener{
        public void onAdShow();
    }

}
