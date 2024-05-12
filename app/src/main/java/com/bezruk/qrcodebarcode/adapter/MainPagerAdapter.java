package com.bezruk.qrcodebarcode.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.bezruk.qrcodebarcode.fragment.GenerateFragment;
import com.bezruk.qrcodebarcode.fragment.HistoryFragment;
import com.bezruk.qrcodebarcode.fragment.ScanFragment;
import com.bezruk.qrcodebarcode.fragment.SettingsFragment;

import java.util.ArrayList;

/**
 * Created by Bezruk on 16/10/18.
 */
public class MainPagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<String> mFragmentItems;

    public MainPagerAdapter(FragmentManager fm, ArrayList<String> fragmentItems) {
        super(fm);
        this.mFragmentItems = fragmentItems;
    }

    @Override
    public Fragment getItem(int i) {

        Fragment fragment = null;

        if(i == 0) {
            fragment = new ScanFragment();
        } else if(i == 1){
            fragment = new GenerateFragment();
        } else if(i == 2){
            fragment = new HistoryFragment();
        } else if(i == 3){
            fragment = new SettingsFragment();
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return mFragmentItems.size();
    }

}
