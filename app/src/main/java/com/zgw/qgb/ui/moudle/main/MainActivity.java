package com.zgw.qgb.ui.moudle.main;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zgw.qgb.R;
import com.zgw.qgb.base.BaseActivity;
import com.zgw.qgb.base.adapter.BaseRecyclerAdapter;
import com.zgw.qgb.base.adapter.BaseViewHolder;

import butterknife.BindView;
import butterknife.OnClick;


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

    @OnClick(R.id.tv_main)
    public void onViewClicked(View view) {
        //SettingsActivity.startActivity(mContext);
            //throw new RuntimeException("This is a crash");
        BaseRecyclerAdapter adapter = new BaseRecyclerAdapter() {
            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            }

            @Override
            protected BaseViewHolder viewHolder(ViewGroup parent, int viewType) {
                return null;
            }

            @Override
            protected void onBindView(BaseViewHolder holder, int position) {

            }
        };
    }
}
