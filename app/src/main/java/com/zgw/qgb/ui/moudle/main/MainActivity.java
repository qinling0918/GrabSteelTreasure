package com.zgw.qgb.ui.moudle.main;

import android.os.Bundle;
import android.widget.TextView;

import com.zgw.qgb.R;
import com.zgw.qgb.base.BaseActivity;

import butterknife.BindView;


/**
 * Created by Tsinling on 2017/9/16 16:04.
 * description:
 */

public class MainActivity extends BaseActivity<MainPresenter> implements MainContract.IMainView {
    @BindView(R.id.tv_main)
    TextView tvMain;

    @Override
    protected boolean canBack() {
        return true;
    }

    @Override
    protected MainPresenter createPresenter() {
        return new MainPresenter(this);
    }

    @Override
    protected int layout() {
        return R.layout.activity_main;
    }

    @Override
    public void setText(String str) {
        tvMain.setText(str);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPresenter.login();
    }
}
