package com.cheng.movies.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.cheng.movies.PageFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private final int PAGE_COUNT = 3;
    private String[] pageTitles = new String[]{"热门", "排行榜", "未来"};
    private Context context;

    public ViewPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        return PageFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return pageTitles[position];
    }
}