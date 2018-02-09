package com.zgw.qgb.ui.moudle.purchase;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ListView;
import android.widget.Spinner;

import com.zgw.qgb.R;
import com.zgw.qgb.base.adapter.BindableAdapter;
import com.zgw.qgb.helper.Bundler;
import com.zgw.qgb.ui.moudle.main.BaseMainFragment;
import com.zgw.qgb.ui.moudle.purchase.contract.PurchaseContract;
import com.zgw.qgb.ui.moudle.purchase.presenter.PurchasePresenter;

import butterknife.BindView;

import static com.zgw.qgb.helper.BundleConstant.EXTRA;


public class PurchaseFragment extends BaseMainFragment<PurchasePresenter> implements PurchaseContract.IPurchaseView {

    @BindView(R.id.lv)
    ListView lv;
    @BindView(R.id.debug_network_endpoint)
    Spinner debugNetworkEndpoint;

    private String title;
    private BindableAdapter<String> adapter;

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
        setBadgeCount(2, 2);

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

    @Override
    protected void initData() {
        super.initData();


    }



}
