package com.zgw.qgb.ui.moudle.quote;

import android.os.Bundle;

import com.zgw.qgb.R;
import com.zgw.qgb.base.BaseFragment;
import com.zgw.qgb.ui.moudle.quote.contract.QuoteMapContract;
import com.zgw.qgb.ui.moudle.quote.presenter.QuoteMapPresenter;


/**
 * Name:MapFragment
 * Comment://todo
 * Created by Tsinling on 2017/5/24 17:35.
 */

public class QuoteMapFragment extends BaseFragment<QuoteMapPresenter> implements QuoteMapContract.IQuoteMapView {
    private static final String ARG_PARAM1 = "param1";
    private String mParam1;

    public QuoteMapFragment() {
    }

    public static QuoteMapFragment newInstance(String param1) {
        QuoteMapFragment fragment = new QuoteMapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    protected int fragmentLayout() {
        return R.layout.fragment_quote_map;
    }

    @Override
    protected QuoteMapPresenter getPresenter() {
        return new QuoteMapPresenter(this);
    }
}
