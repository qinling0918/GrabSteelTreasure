package com.zgw.qgb.ui.moudle.purchase;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.zgw.qgb.R;
import com.zgw.qgb.helper.Bundler;
import com.zgw.qgb.ui.moudle.main.BaseMainFragment;
import com.zgw.qgb.ui.moudle.purchase.contract.PurchaseContract;
import com.zgw.qgb.ui.moudle.purchase.presenter.PurchasePresenter;

import static com.zgw.qgb.helper.BundleConstant.EXTRA;


public class PurchaseFragment extends BaseMainFragment<PurchasePresenter> implements PurchaseContract.IPurchaseView{
   
    private String title;

    public PurchaseFragment() {

    }

    public static PurchaseFragment newInstance(String title) {
        PurchaseFragment fragment = new PurchaseFragment();
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
        setBadgeCount(2,2);
    }

    @Override
    protected int fragmentLayout() {
        return R.layout.fragment_purchase;
    }

    @Override
    protected PurchasePresenter getPresenter() {
        return new PurchasePresenter(this);
    }

    @Override
    public void onLazyLoad() {
        
    }
}
