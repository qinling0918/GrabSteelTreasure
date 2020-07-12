package com.zgw.qgb.ui.moudle.find;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;

import com.zgw.qgb.R;
import com.zgw.qgb.helper.Bundler;
import com.zgw.qgb.ui.moudle.main.BaseMainFragment;

import static com.zgw.qgb.helper.BundleConstant.EXTRA;

/**
 */
public class FindFragment extends BaseMainFragment<FindPresenter> implements FindContract.IFindView {

    private String title;

    public static FindFragment newInstance(String title) {
        FindFragment fragment = new FindFragment();
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
        setBadgeCount(1,1);
    }

    @Override
    protected int fragmentLayout() {
        return R.layout.fragment_find;
    }

    @Override
    protected FindPresenter getPresenter() {
        return new FindPresenter(this);
    }

    @Override
    public void onLazyLoad() {

    }


}
