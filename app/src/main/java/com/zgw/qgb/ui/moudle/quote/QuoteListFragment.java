package com.zgw.qgb.ui.moudle.quote;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import com.zgw.qgb.R;
import com.zgw.qgb.base.BaseFragment;
import com.zgw.qgb.helper.Bundler;
import com.zgw.qgb.helper.ToastUtils;
import com.zgw.qgb.ui.moudle.quote.contract.QuoteListContract;
import com.zgw.qgb.ui.moudle.quote.presenter.QuoteListPresenter;

import butterknife.BindView;
import butterknife.OnClick;

import static com.zgw.qgb.helper.BundleConstant.EXTRA;


/**
 * Comment://报价列表
 * Created by Tsinling on 2017/5/24 17:34.
 */

public class QuoteListFragment extends BaseFragment<QuoteListPresenter> implements QuoteListContract.IQuoteListView {

    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tv_wide_nation)
    TextView tvWideNation;

    private String title;

    public QuoteListFragment() {

    }

    public static QuoteListFragment newInstance(String title) {
        QuoteListFragment fragment = new QuoteListFragment();
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
    }

    @Override
    protected int fragmentLayout() {
        return R.layout.fragment_quote_list;
    }


    @Override
    protected QuoteListPresenter getPresenter() {
        return new QuoteListPresenter(this);
    }


    int num = 0;

    @SuppressLint("ResourceType")
    @OnClick({R.id.tv_title, R.id.tv_wide_nation})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_title:
               /* ToastUtils.setBgColor(getResources().getColor(R.color.colorPrimary));
                ToastUtils.setMsgColor(getResources().getColor(android.R.color.white));
                ToastUtils.showLong("Toast" + num++);*/
             /*   LayoutInflater inflate = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View toastView = inflate != null ? inflate.inflate(R.layout.fragment_message, null) : null;
                toastView.setBackgroundColor(R.color.colorPrimary);*/
                //((TextView)toastView.findViewById(R.id.toast_text)).setText(R.string.message);
           /*     TextView tv = new TextView(getContext());
                tv.setText("123123");
                ToastUtils.setGravity(Gravity.CENTER,0,0);
                ToastUtils.showCustomShort(tv);*/
                showMessage(R.string.error,R.string.error);
                //ToastUtils.showLong(getString(R.string.message));
                break;
            case R.id.tv_wide_nation:
                //showMessage(R.string.message,R.string.error);


                Snackbar.make(tvTitle,"1213123",Snackbar.LENGTH_LONG).setText("123").setAction("123123123123123", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ToastUtils.showNormal("1231");
                    }
                }).show();
              /*  ToastUtils.setBgColor(getResources().getColor(R.color.colorAccent));
                ToastUtils.setMsgColor(getResources().getColor(android.R.color.white));
                ToastUtils.showLong("Toast" + num++);*/
             /*   ToastUtils.showLong("Toast" + num++);
                Drawable drawable = getResources().getDrawable(R.drawable.ic_main_message);//获取图片资源
                drawable.setBounds(0, 0, 72, 72);
                tvWideNation.setError("Toast", drawable);
                tvWideNation.requestFocus();*/
                break;
        }
    }


}
