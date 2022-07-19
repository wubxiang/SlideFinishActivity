package com.example.slidefinish;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Author: wbx
 * Date: 2020/7/22
 * Description:
 */

public class ViewpagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> mFragmentList;

    public ViewpagerAdapter(@NonNull FragmentManager fm, List<Fragment> list) {
        super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mFragmentList = list;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

}
