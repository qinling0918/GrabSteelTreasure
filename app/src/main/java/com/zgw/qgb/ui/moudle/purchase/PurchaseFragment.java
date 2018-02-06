package com.zgw.qgb.ui.moudle.purchase;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.zgw.qgb.R;
import com.zgw.qgb.base.adapter.BindableAdapter;
import com.zgw.qgb.helper.Bundler;
import com.zgw.qgb.ui.moudle.main.BaseMainFragment;
import com.zgw.qgb.ui.moudle.purchase.contract.PurchaseContract;
import com.zgw.qgb.ui.moudle.purchase.presenter.PurchasePresenter;

import java.util.ArrayList;
import java.util.List;

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

        getAdapter();
        lv.setAdapter(adapter);
        debugNetworkEndpoint.setAdapter(adapter);

        View view1 = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, null);


        //adapter.bindDropDownView("drop", 5 , view1);
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

    private BindableAdapter getAdapter() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            list.add(i + "");
        }

        return adapter = new BindableAdapter<String>(getContext()) {
            @Override
            public String getItem(int position) {
                return list.get(position);
            }

            @Override
            public View newView(LayoutInflater inflater, int position, ViewGroup container) {
                return inflater.inflate(android.R.layout.simple_list_item_2, null);
            }

            @Override
            public void bindView(String item, int position, View view) {
                ((TextView) view.findViewById(android.R.id.text1)).setText(item);
                //((TextView)view.findViewById(android.R.id.text2)).setText(item+"//");
            }

            @Override
            public int getCount() {
                return list.size();
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

           /* @Override
            public View newDropDownView(LayoutInflater inflater, int position, ViewGroup container) {
                return inflater.inflate(android.R.layout.simple_list_item_checked, container, false);
            }

            @Override
            public void bindDropDownView(String item, int position, View view) {
                super.bindDropDownView(item, position, view);
                ((TextView) view.findViewById(android.R.id.text1)).setText(item + "drop");
            }*/
        };
    }


}
