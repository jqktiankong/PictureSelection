package com.jqk.pictureselectorlibrary.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.jqk.pictureselectorlibrary.view.show.ShowImgFragment;

import java.util.ArrayList;

/**
 * Created by Administrator on 2018/4/12 0012.
 */

public class MyViewPagerAdapter extends FragmentPagerAdapter {

    private ArrayList<ShowImgFragment> fragments;

    public MyViewPagerAdapter(FragmentManager fm, ArrayList<ShowImgFragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
