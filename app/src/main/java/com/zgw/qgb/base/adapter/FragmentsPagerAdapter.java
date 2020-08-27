package com.zgw.qgb.base.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.appcompat.app.AppCompatActivity;

import com.zgw.qgb.model.bean.FragmentPagerAdapterModel;

import java.util.List;
/**
 * FragmentStatePagerAdapter /FragmentPagerAdapter使用中
 *
 *   FragmentStatePagerAdapter与FragmentPagerAdapter用法类似，
 *    区别在于，卸载不需要的Fragment时，各自的处理方法不同。
 *    FragmentStatePagerAdapter会销毁不需要的Fragment，事务提交后，
 *    FragmentManager中的Fragment会被彻底移除，销毁时可在onSaveInstanceState方法中保存信息；
 *    FragmentPagerAdapter对于不再需要的Fragment会调用事务的detach方法而非remove方法，
 *    仅仅是销毁Fragment的视图，而实例对象仍然保留。
 *    所以FragmentStatePagerAdapter更节省内存，当page页面较多时适合使用。
 *    如果界面只是少量固定页面，FragmentPagerAdapter更安全。
 *
 *
 * 1.viewpager.setOffscreenPageLimit(2);表示当前项(view)的左右两边的预加载的页面的个数, 超过此限制
 * FragmentStatePagerAdapter会回收Fragment (onDestroyView,onDestroy,onDetach)
 * FragmentPagerAdapter则会缓存在内存中（FragmentManager）执行onDestroyView
 * 2.无setOffscreenPageLimit(int limit)设置（viewpager中默认limit为1），默认加载左右页面；
 * 首先 getItem()是FragmentPagerAdapter/FragmentStatePagerAdapter 中的抽象方法
 * FragmentStatePagerAdapter 内部维持一个ArrayList，
 * 每次instantiateItem时，会先从List中取position位置的Fragment返回，如果没有，则会getItem()创建Fragment，存放在List中
 * destroyItem 时，则在List中set(position，null)；
 * FragmentPagerAdapter 则通过FragmentManager进行管理
 * 每次instantiateItem时，会先从FragmentManager中取Fragment，交给FragmentTransaction attach, 然后返回，如果没有，则会getItem(position)创建Fragment，由FragmentTransaction 添加，返回Fragment
 * destroyItem 时，则由FragmentTransaction detach 该Fragment；
 *
 * Created by Tsinling on 2017/5/16 9:44.
 * 由于此处需要使用 list 将fragment 保存。由于FragmentStatePagerAdapter 会回收Fragment (onDestroyView,onDestroy,onDetach)
 * 但 fragment实例被保存在list中，内存被占用。故无法完全被释放。
 * 所以为了避免内存泄漏，使用 FragmentPagerAdapter
 */

public class FragmentsPagerAdapter extends FragmentPagerAdapter {
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
