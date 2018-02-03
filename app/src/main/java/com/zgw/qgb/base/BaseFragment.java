package com.zgw.qgb.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.trello.rxlifecycle2.LifecycleTransformer;
import com.trello.rxlifecycle2.components.support.RxFragment;
import com.zgw.qgb.R;
import com.zgw.qgb.base.mvp.IPresenter;
import com.zgw.qgb.base.mvp.IView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import icepick.Icepick;

import static com.trello.rxlifecycle2.internal.Preconditions.checkNotNull;


/**
 * Created by Tsinling on 2017/8/12 14:22.
 * description:
 */

public abstract class BaseFragment<P extends IPresenter> extends RxFragment implements IView {
    protected final String TAG = this.getClass().getSimpleName();
    protected IView callback;
    protected P mPresenter;
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
        if (context instanceof IView) {
            callback = (IView) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = getPresenter();
        checkNotNull(mPresenter, "presenter can't be null");
        initData();
        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            Icepick.restoreInstanceState(this, savedInstanceState);
            mPresenter.onRestoreInstanceState(savedInstanceState);
        }


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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null) unbinder.unbind();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
        mPresenter.onSaveInstanceState(outState);
    }


    /**
     * 获取注入Activity的Presenter对象
     */
    protected abstract P getPresenter();

    @Override
    public void showProgress(int resId) {
        callback.showProgress(resId);
    }

    @Override
    public void hideProgress() {
        callback.hideProgress();
    }

    @Override
    public void showErrorMessage(@NonNull String msgRes) {
        callback.showErrorMessage(msgRes);
    }

    @Override
    public void showMessage(int titleRes, int msgRes) {
        callback.showMessage(titleRes, msgRes);
    }

    @Override
    public void showMessage(@NonNull String titleRes, @NonNull String msgRes) {
        callback.showMessage(titleRes, msgRes);
    }

    @Override
    public boolean isLoggedIn() {
        return callback.isLoggedIn();
    }

    @Override
    public <T> LifecycleTransformer<T> bind2Lifecycle() {
        return bindToLifecycle();
    }

    public void setTitle(CharSequence title) {
        if (tv_title != null){
            tv_title.setText(title);
        }

    }


}
