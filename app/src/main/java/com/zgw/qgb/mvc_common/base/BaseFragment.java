package com.zgw.qgb.mvc_common.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.trello.rxlifecycle4.components.support.RxFragment;
import com.zgw.qgb.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import icepick.Icepick;


/**
 * Created by Tsinling on 2017/8/12 14:22.
 * description:
 */

public abstract class BaseFragment extends RxFragment {
    protected final String TAG = this.getClass().getSimpleName();
    private Unbinder unbinder;

    @Nullable
    @BindView(R.id.toolbar)
    public Toolbar toolbar;
    @Nullable
    @BindView(R.id.tv_title)
    public TextView tv_title;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            Icepick.restoreInstanceState(this, savedInstanceState);
        }
    }

    /**
     * 用户是否登录
     *
     * @return
     */
    public boolean isLogin() {
       /* if (PrefGetter.isLogin()){//替换成用shrefreence 工具获取用户是否已经登录
            return true;
        }else{
            startActivity(new Intent(getContext(), LoginActivity.class));
            return false;
        }*/
        return false;

    }

    /**
     * 用来初始化数据,
     */
    protected void initData() {}

    @SuppressLint("RestrictedApi")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (fragmentLayout() != 0) {
            final Context contextThemeWrapper = new ContextThemeWrapper(getContext(), getContext().getTheme());
            LayoutInflater themeAwareInflater = inflater.cloneInContext(contextThemeWrapper);
            View view = themeAwareInflater.inflate(fragmentLayout(), container, false);
            unbinder = ButterKnife.bind(this, view);

            return view;
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    protected abstract int fragmentLayout();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null) unbinder.unbind();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    public void setTitle(CharSequence title) {
        if (tv_title != null){
            tv_title.setText(title);
        }
    }


}
