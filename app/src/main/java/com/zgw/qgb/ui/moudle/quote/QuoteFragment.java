package com.zgw.qgb.ui.moudle.quote;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.zgw.qgb.R;
import com.zgw.qgb.base.adapter.FragmentsPagerAdapter;
import com.zgw.qgb.helper.BundleConstant;
import com.zgw.qgb.model.FragmentPagerAdapterModel;
import com.zgw.qgb.ui.moudle.main.BaseMainFragment;
import com.zgw.qgb.ui.moudle.mine.SettingsActivity;
import com.zgw.qgb.ui.moudle.quote.contract.QuoteContract;
import com.zgw.qgb.ui.moudle.quote.presenter.QuotePresenter;
import com.zgw.qgb.ui.widgets.NoScrollViewPager;
import com.zgw.qgb.ui.widgets.SegmentControl;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnPageChange;

/**
 * 报价
 */
public class QuoteFragment extends BaseMainFragment<QuotePresenter> implements QuoteContract.IQuoteView {

    @BindView(R.id.segment_control)
    SegmentControl segmentControl;
    @BindView(R.id.tv_wide_nation)
    TextView tvWideNation;
    @BindView(R.id.vp_main_container)
    NoScrollViewPager vpMainContainer;


    public QuoteFragment() {
    }


    public static QuoteFragment newInstance() {
        QuoteFragment fragment = new QuoteFragment();
        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        segmentControl.setOnSegmentControlClickListener(this);
        setupViewPager();



    }

    @Override
    protected int fragmentLayout() {
        return R.layout.fragment_quote;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected QuotePresenter getPresenter() {
        return new QuotePresenter(this);
    }

    @Override
    public void onLazyLoad() {

    }

    private void setupViewPager() {
        vpMainContainer.setNoScroll(false);
        FragmentsPagerAdapter pagerAdapter = new FragmentsPagerAdapter(this, FragmentPagerAdapterModel.buildForQuote(getContext()));
        vpMainContainer.setAdapter(pagerAdapter);
        vpMainContainer.setCurrentItem(segmentControl.getSelectedIndex(), false);
    }

    @Override
    public void onSegmentControlClick(int index) {
        vpMainContainer.setCurrentItem(index, false);
    }

    @OnClick(R.id.tv_wide_nation)
    public void onViewClicked() {
       // startActivityForResult(new Intent(getContext(), CityActivity.class), BundleConstant.REQUEST_CODE);
        startActivityForResult(new Intent(getContext(), SettingsActivity.class), BundleConstant.REQUEST_CODE);
    }

    @OnPageChange(R.id.vp_main_container)
    public void onPageSelected(int position) {
        segmentControl.setSelectedIndex(position);
    }
}
