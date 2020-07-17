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
        super(activity.getSupportFragmentManager(),BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    public FragmentsPagerAdapter(Fragment fragment) {
        super(fragment.getChildFragmentManager(),BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
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

   /*
 使用add()加入fragment时将触发onAttach(),使用attach()不会触发onAttach()

使用replace()替换后会将之前的fragment的view从viewtree中删除

触发顺序:

detach()->onPause()->onStop()->onDestroyView()

attach()->onCreateView()->onActivityCreated()->onStart()->onResume()

使用hide()方法只是隐藏了fragment的view并没有将view从viewtree中删除,随后可用show()方法将view设置为显示

而使用detach()会将view从viewtree中删除,和remove()不同,此时fragment的状态依然保持着,在使用 attach()时会再次调用onCreateView()来重绘视图,注意使用detach()后fragment.isAdded()方法将返回 false,在使用attach()还原fragment后isAdded()会依然返回false(需要再次确认)

执行detach()和replace()后要还原视图的话, 可以在相应的fragment中保持相应的view,并在onCreateView()方法中通过view的parent的removeView()方法将view和parent的关联删除后返回
   * */
}
