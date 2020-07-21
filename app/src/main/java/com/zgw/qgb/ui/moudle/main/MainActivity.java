package com.zgw.qgb.ui.moudle.main;

import android.content.Context;
import android.os.Bundle;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.ashokvarma.bottomnavigation.ShapeBadgeItem;
import com.ashokvarma.bottomnavigation.TextBadgeItem;
import com.zgw.qgb.R;
import com.zgw.qgb.base.BaseActivity;
import com.zgw.qgb.base.adapter.FragmentsPagerAdapter;
import com.zgw.qgb.helper.ConfigContextWrapper;
import com.zgw.qgb.helper.utils.ResourceUtils;
import com.zgw.qgb.model.bean.FragmentPagerAdapterModel;
import com.zgw.qgb.ui.widgets.NoScrollViewPager;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.OnPageChange;
import icepick.State;


/**
 * Created by Tsinling on 2017/9/16 16:04.
 * description:
 */

public class MainActivity extends BaseActivity<MainPresenter> implements MainContract.IMainView {


    @BindView(R.id.bottom_navigation_bar)
    BottomNavigationBar bottomNavigationBar;
    @BindView(R.id.vp_main_container)
    NoScrollViewPager vpMainContainer;
    @BindArray(R.array.bottom_tab_title)
    String[] tabStrArr;

    @State
    int lastSelectedPosition = 0;
    @State int [] countArr ;

    private int[] tabIconArr;
    private FragmentsPagerAdapter pagerAdapter;

    @Override
    protected boolean canBack() {
        return false;
    }

    @Override
    protected MainPresenter createPresenter() {
        return new MainPresenter(this);
    }

    @Override
    public void setText(String str) {
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ConfigContextWrapper.create(newBase, 360, new ConfigContextWrapper.IConfig() {
            @Override
            public float getFontScaleSize() {
                return 0;
            }

            @Override
            public int getDensityDpi() {
                return ConfigContextWrapper.getDefaultDisplayDensity();
            }
        }));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPresenter.login();



        setupViewPager();
        setupBottomNavigationBar();



    }

    protected void initData() {
        countArr = new int[tabStrArr.length];
        tabIconArr = ResourceUtils.getDrawableId(R.array.bottom_tab_icon_id);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setupViewPager() {
        vpMainContainer.setNoScroll(false);
        pagerAdapter = new FragmentsPagerAdapter(this, FragmentPagerAdapterModel.buildForMain(mContext));
        vpMainContainer.setAdapter(pagerAdapter);
        vpMainContainer.setCurrentItem(lastSelectedPosition, false);

    }


    private void setupBottomNavigationBar() {
        initData();

        bottomNavigationBar.clearAll();
        bottomNavigationBar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC);
        bottomNavigationBar.setMode(BottomNavigationBar.MODE_FIXED);

        for (int i = 0; i < tabStrArr.length; i++) {
            BottomNavigationItem bottomNavigationItem = new BottomNavigationItem(tabIconArr[i], tabStrArr[i])
                    .setActiveColorResource(R.color.colorPrimary)
                    .setInActiveColorResource(R.color.textColorPrimary);

            setBadge(i, bottomNavigationItem);
            bottomNavigationBar.addItem(bottomNavigationItem);
        }

        bottomNavigationBar.setFirstSelectedPosition(lastSelectedPosition).initialise();
        bottomNavigationBar.setTabSelectedListener(this);
    }


    /**
     * countArr
     * 0 没有badge  大于0 显示小红球加数字  小于0显示小红点
     */
    private void setBadge(int i, BottomNavigationItem bottomNavigationItem) {
        if (countArr[i] > 0) {
            TextBadgeItem numberBadgeItem = new TextBadgeItem()
                    .setText(countArr[i] + "")
                    .setBackgroundColorResource(R.color.colorPrimary);

            bottomNavigationItem.setBadgeItem(numberBadgeItem);
        } else if (countArr[i] < 0) {
            ShapeBadgeItem shapeBadgeItem = new ShapeBadgeItem()
                    .setSizeInDp(mContext, 8, 8)
                    .setShapeColorResource(R.color.colorPrimary)
                    .setShape(ShapeBadgeItem.SHAPE_OVAL);
            bottomNavigationItem.setBadgeItem(shapeBadgeItem);
        }
    }

    @Override
    public void onTabSelected(int position) {
        lastSelectedPosition = position;
        vpMainContainer.setCurrentItem(position, false);

    }

    @Override
    public void onTabUnselected(int position) {}

    @Override
    public void onTabReselected(int position) {

    }

    @OnPageChange(R.id.vp_main_container)
    public void onPageSelected(int position) {
        lastSelectedPosition = position;
        setupBottomNavigationBar();
    }

    /**
     * 设置小红点
     * @param fromIndex  需要显示在bottomBar的哪一个位置,即当前页面的position
     * @param count 小红点的数量
     */
    @Override
    public void onBadgeCountChange(int fromIndex, int count) {
        countArr[fromIndex] = count;
        setupBottomNavigationBar();
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
