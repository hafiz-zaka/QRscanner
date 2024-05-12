package com.bezruk.qrcodebarcode.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.bezruk.qrcodebarcode.R;
import com.bezruk.qrcodebarcode.data.preference.AppPreference;
import com.bezruk.qrcodebarcode.data.preference.PrefKey;
import com.bezruk.qrcodebarcode.utility.AppUtils;

public class SettingsFragment extends Fragment implements View.OnTouchListener  {

    private Activity mActivity;
    private Context mContext;
    private Switch switch_vibrate, switch_autofocus, switch_auto_url;
    private LinearLayout rate_us, share_app;
    private ImageView rate_icon, share_icon;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initVar();



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        initView(rootView);
        initListener();

        return rootView;
    }

    private void initVar() {
        mActivity = getActivity();
        if (mActivity != null) {
            mContext = mActivity.getApplicationContext();
        }
    }

    private void initView(View rootView) {
        switch_vibrate = (Switch) rootView.findViewById(R.id.switch_vibrate);
        switch_autofocus = (Switch) rootView.findViewById(R.id.switch_autofocus);
        switch_auto_url = (Switch) rootView.findViewById(R.id.switch_auto_url);

        rate_us = rootView.findViewById(R.id.rate_us);
        share_app = rootView.findViewById(R.id.share_app);

        rate_icon = rootView.findViewById(R.id.rate__icon);
        share_icon = rootView.findViewById(R.id.share_icon);

        switch_vibrate.setChecked(AppPreference.getInstance(mContext).getBoolean(PrefKey.SETTINGS_VIBRATE, true));
        switch_autofocus.setChecked(AppPreference.getInstance(mContext).getBoolean(PrefKey.SETTINGS_AUTOFOCUS, true));
        switch_auto_url.setChecked(AppPreference.getInstance(mContext).getBoolean(PrefKey.SETTINGS_AUTO_URL, false));

    }

    @SuppressLint("ClickableViewAccessibility")
    private void initListener() {
        switch_vibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.d("SSSSS1",String.valueOf(b));
                AppPreference.getInstance(mContext).setBoolean(PrefKey.SETTINGS_VIBRATE, b);
            }
        });

        switch_autofocus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.d("SSSSS2",String.valueOf(b));
                AppPreference.getInstance(mContext).setBoolean(PrefKey.SETTINGS_AUTOFOCUS, b);
            }
        });

        switch_auto_url.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.d("SSSSS3",String.valueOf(b));
                AppPreference.getInstance(mContext).setBoolean(PrefKey.SETTINGS_AUTO_URL, b);
            }
        });

        rate_us.setOnTouchListener(this);
        share_app.setOnTouchListener(this);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            if (v.getId() == R.id.rate_us) {
                rate_icon.setColorFilter(ContextCompat.getColor(mContext,
                        R.color.colorPrimary));
            } else if (v.getId() == R.id.share_app) {
                share_icon.setColorFilter(ContextCompat.getColor(mContext,
                        R.color.colorPrimary));
            }
        } else if (action == MotionEvent.ACTION_UP) {
            if (v.getId() == R.id.rate_us) {
//                rate_icon.setColorFilter(ContextCompat.getColor(mContext,
//                        R.color.materialcolorpicker__lightgrey));
                AppUtils.rateThisApp(mActivity);
            } else if (v.getId() == R.id.share_app) {
//                share_icon.setColorFilter(ContextCompat.getColor(mContext,
//                        R.color.materialcolorpicker__lightgrey));
                AppUtils.shareApp(mActivity);
            }
        }
        return true;
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {
//            rate_icon.setColorFilter(ContextCompat.getColor(mContext,
//                    R.color.materialcolorpicker__lightgrey));
//            share_icon.setColorFilter(ContextCompat.getColor(mContext,
//                    R.color.materialcolorpicker__lightgrey));
        }
    }


}
