package com.zgw.qgb.base.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.appcompat.app.AppCompatActivity;

import com.zgw.qgb.model.bean.FragmentPagerAdapterModel;

import java.util.List;

/**
 *
 * Created by Tsinling on 2017/5/16 9:44.
 */

public class FragmentsPagerAdapter extends FragmentStatePagerAdapter {
    private List<FragmentPagerAdapterModel> fragments;
    //private AppCompatActivity activity;

    //private Fragment selected;

    public FragmentsPagerAdapter(AppCompatActivity activity) {
        super(activity.getSupportFragmentManager());
    }

    public FragmentsPagerAdapter(Fragment fragment) {
        super(fragment.getChildFragmentManager());
    }

    public FragmentsPagerAdapter(AppCompatActivity activity, List<FragmentPagerAdapterModel> fragments) {
        this(activity);
        //this.activity = activity;
        setData(fragments);
    }

    public FragmentsPagerAdapter(Fragment fragment, List<FragmentPagerAdapterModel> fragments) {
        this(fragment);
        //this.activity = (AppCompatActivity) fragment.getActivity();
        setData(fragments);
    }
    /**
     *  传入fragment数据源
     *
     * @param fragments
     */
    public void setData( List<FragmentPagerAdapterModel> fragments){
        this.fragments = fragments;
    }

    @Override public Fragment getItem(int position) {
        return fragments.get(position).getFragment();
    }

    @Override public int getCount() {
        return fragments.size();
    }

    @Override public CharSequence getPageTitle(int position) {
        return fragments.get(position).getTitle();
    }

    /*  @Override public float getPageWidth(int position) {
        return super.getPageWidth(position);
    }

  @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);

        boolean changed = false;
        if (object instanceof Fragment) {
            changed = object != selected;
            selected = (Fragment) object;
        } else {
            changed = object != null;
            selected = null;
        }

        if (changed) {
            activity.invalidateOptionsMenu();
        }
    }*/
}
