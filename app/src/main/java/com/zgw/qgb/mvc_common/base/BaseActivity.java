package com.zgw.qgb.mvc_common.base;

/**
 * created by tsinling on: 2018/5/1 13:43
 * description:
 */


import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.jaeger.library.StatusBarUtil;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;
import com.zgw.qgb.R;
import com.zgw.qgb.helper.ToastUtils;
import com.zgw.qgb.helper.ViewHelper;
import com.zgw.qgb.ui.widgets.dialog.ProgressDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;


public class BaseActivity extends RxAppCompatActivity {
    protected final String TAG = this.getClass().getSimpleName();
    protected Context mContext;
    protected Activity mActivity;
    private long backPressTimer;


    @Nullable
    @BindView(R.id.toolbar)
    public Toolbar toolbar;
    @Nullable
    @BindView(R.id.tv_title)
    public TextView tv_title;
    @State
    boolean isProgressShowing;

    /**
     * 用户是否登录
     *
     * @return
     */
    public boolean isLogin() {
       //替换成用shrefreence 工具获取用户是否已经登录
        return false;
    }



    //boolean isLogin;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        mActivity = this;

        if (layout() != 0) {
            setContentView(layout());
            ButterKnife.bind(this);
        }

        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            Icepick.restoreInstanceState(this, savedInstanceState);
        }
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);

        if (null != toolbar) {
            setupToolbarAndStatusBar(toolbar);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @LayoutRes
    protected int layout() {
        return 0;
    }

    /**
     * 该界面是否可以返回
     *
     * @return true 可以    false 类似MainActivity  没有上一级界面
     */
    protected boolean canBack() {
        return true;
    }

    protected boolean clickTwiceToExit() {
        return true;
    }

    /**
     * 状态栏需不需要透明,默认不透明
     *
     * @return
     */
    protected boolean isTransparent() {
        return false;
    }

    public void setupToolbarAndStatusBar(@Nullable Toolbar toolbar) {
        changeStatusBarColor(isTransparent());
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            resetTitle(getSupportActionBar().getTitle(), 0);

            if (canBack()) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }
            }
        }
    }

    private void resetTitle(CharSequence charSequence, int color) {
        if (tv_title != null) {
            if (color != 0) tv_title.setTextColor(color);
            tv_title.setText(charSequence);
            if (getSupportActionBar() != null) {

                getSupportActionBar().setTitle("");
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }

        }
    }


    protected void changeStatusBarColor(boolean isTransparent) {
        if (!isTransparent) {
            StatusBarUtil.setColor(this, ViewHelper.getPrimaryDarkColor(mContext), 0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (canBack()) {
            if (item.getItemId() == android.R.id.home) {
                onBackPressed();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 只有到MainActivity时才有两次点击退出逻辑
     */
    private void superOnBackPressed() {
    /*    if (this instanceof MainActivity) {
            if (canExit()) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }*/
        super.onBackPressed();
    }


    private boolean canExit() {
        if (backPressTimer + 2000 > System.currentTimeMillis()) {
            return true;
        } else {
            ToastUtils.showShort("两次点击");
        }
        backPressTimer = System.currentTimeMillis();
        return false;
    }

    @Override
    public void onBackPressed() {
        superOnBackPressed();
    }

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        resetTitle(title, color);
    }

    public void showProgress(@StringRes int resId) {
        showProgress(resId, true);
    }

    public void showProgress(CharSequence msg) {
        showProgress(msg, false);
    }

    public void showProgress(@StringRes int resId, boolean cancelable) {
        String msg = getString(R.string.in_progress);
        if (resId != 0) {
            msg = getString(resId);
        }
        showProgress(msg, cancelable);
    }

    public void showProgress(CharSequence msg, boolean cancelable) {
        if (!isProgressShowing && !isFinishing()) {
            ProgressDialogFragment fragment = (ProgressDialogFragment) getFragmentByTag(getSupportFragmentManager(),
                    ProgressDialogFragment.TAG);
            if (fragment == null) {
                isProgressShowing = true;
                fragment = ProgressDialogFragment.newInstance(msg, cancelable);
                fragment.show(getSupportFragmentManager(), ProgressDialogFragment.TAG);
            }
        }
    }

    public void hideProgress() {
        ProgressDialogFragment fragment = (ProgressDialogFragment) getFragmentByTag(getSupportFragmentManager(),
                ProgressDialogFragment.TAG);
        if (fragment != null) {
            isProgressShowing = false;
            fragment.dismiss();
        }
    }

    public Fragment getFragmentByTag(@NonNull FragmentManager fragmentManager, @NonNull String tag) {
        return fragmentManager.findFragmentByTag(tag);
    }


    public Context getContext() {
        return mContext;
    }

    public Activity getActivity() {
        return mActivity;
    }
}
