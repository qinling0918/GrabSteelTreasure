package com.zgw.qgb.mvc_common.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;


import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import com.zgw.qgb.helper.utils.ViewBindingUtil;


/**
 * Created by qinling on 2020/8/11 11:25
 * Description:
 */
public abstract class BaseViewBindingActivity<VB extends ViewBinding> extends IViewActivity {
    protected final String TAG = this.getClass().getSimpleName();
    protected VB mBinding;



    protected Context mContext;


    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mBinding = ViewBindingUtil.create(getClass(), getLayoutInflater());
        setContentView(mBinding.getRoot());
    }


    public void setContentView(View contentView) {
        super.setContentView(contentView);

    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    public Context getContext() {
        return mContext;
    }
}