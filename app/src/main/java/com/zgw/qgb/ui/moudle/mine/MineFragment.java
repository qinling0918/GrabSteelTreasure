package com.zgw.qgb.ui.moudle.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.zgw.qgb.R;
import com.zgw.qgb.helper.Bundler;
import com.zgw.qgb.ui.moudle.main.BaseMainFragment;
import com.zgw.qgb.ui.moudle.mine.contract.MineContract;
import com.zgw.qgb.ui.moudle.mine.presenter.MinePresenter;

import static com.zgw.qgb.helper.BundleConstant.EXTRA;


public class MineFragment extends BaseMainFragment<MinePresenter> implements MineContract.IMineView{

    private String title;

    public MineFragment() {

    }

    public static MineFragment newInstance(String title) {
        MineFragment fragment = new MineFragment();
        fragment.setArguments(Bundler.start()
                .put(EXTRA, title).end());
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            title = getArguments().getString(EXTRA);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(title);
       // setBadgeCount(4,4);
    }


    @Override
    protected int fragmentLayout() {
        return R.layout.fragment_mine;
    }

    @Override
    protected MinePresenter getPresenter() {
        return new MinePresenter(this);
    }

    @Override
    public void onLazyLoad() {
        
    }
}
