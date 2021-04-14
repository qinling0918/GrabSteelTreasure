package com.zgw.qgb.base;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.jaeger.library.StatusBarUtil;
import com.trello.rxlifecycle4.LifecycleTransformer;
import com.trello.rxlifecycle4.components.support.RxAppCompatActivity;
import com.zgw.qgb.R;
import com.zgw.qgb.base.mvp.IPresenter;
import com.zgw.qgb.base.mvp.IView;
import com.zgw.qgb.helper.AppHelper;
import com.zgw.qgb.helper.PrefGetter;
import com.zgw.qgb.helper.ToastUtils;
import com.zgw.qgb.helper.ViewHelper;
import com.zgw.qgb.ui.moudle.main.MainActivity;
import com.zgw.qgb.ui.widgets.dialog.ProgressDialogFragment;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;

import static com.zgw.qgb.helper.utils.EmptyUtils.checkNotNull;


/**
 * Created by Tsinling on 2017/8/12 11:49.
 * description:
 */

public abstract class BaseActivity<P extends IPresenter<? extends IView>> extends RxAppCompatActivity implements IView {
    // 开启矢量图开关
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    protected final String TAG = this.getClass().getSimpleName();
    protected P mPresenter;
    protected Context mContext;
    @State
    boolean isProgressShowing;
    @State
    Bundle presenterStateBundle = new Bundle();
    //private Toast toast;
    private long backPressTimer;

    @Nullable
    @BindView(R.id.toolbar) public Toolbar toolbar;
    @Nullable @BindView(R.id.tv_title) public TextView tv_title;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        mPresenter = createPresenter();
        checkNotNull(mPresenter,"presenter can't be null");

        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            Icepick.restoreInstanceState(this, savedInstanceState);
            mPresenter.onRestoreInstanceState(savedInstanceState);
        }
    }


    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        if (layoutResID != 0) {
            ButterKnife.bind(this);
        }
        if (null!=toolbar){
            setupToolbarAndStatusBar(toolbar);
        }
    }


    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        mPresenter.detachView();
        super.onDestroy();
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
        mPresenter.onSaveInstanceState(outState);
    }

    protected abstract P createPresenter();


    protected  boolean canBack(){
        return true;
    }

    protected  boolean isTransparent(){
        return false;
        //return true;
    }

    public void setupToolbarAndStatusBar(@Nullable Toolbar toolbar) {
        changeStatusBarColor(isTransparent());
        if (toolbar != null) {
            setSupportActionBar(toolbar);

            resetTitle(getSupportActionBar().getTitle(),0);

            if (canBack()) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    if (canBack()) {
                        View navIcon = getToolbarNavigationIcon(toolbar);
                        if (navIcon != null) {
                            navIcon.setOnLongClickListener(v -> {
                                Intent intent = new Intent(this, MainActivity.class);
                                startActivity(intent);
                                finish();
                                return true;
                            });
                        }
                    }
                }
            }
        }
    }

    private void resetTitle(CharSequence charSequence, int color) {
        if (tv_title != null){
            if (color != 0) tv_title.setTextColor(color);
            tv_title.setText(charSequence);
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    @Nullable private View getToolbarNavigationIcon(Toolbar toolbar) {
        boolean hadContentDescription = TextUtils.isEmpty(toolbar.getNavigationContentDescription());
        String contentDescription = !hadContentDescription ? String.valueOf(toolbar.getNavigationContentDescription()) : "navigationIcon";
        toolbar.setNavigationContentDescription(contentDescription);
        ArrayList<View> potentialViews = new ArrayList<>();
        toolbar.findViewsWithText(potentialViews, contentDescription, View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
        View navIcon = null;
        if (potentialViews.size() > 0) {
            navIcon = potentialViews.get(0);
        }
        if (hadContentDescription) toolbar.setNavigationContentDescription(null);
        return navIcon;
    }

    protected void changeStatusBarColor(boolean isTransparent) {
        if (!isTransparent) {
            StatusBarUtil.setColor(this,ViewHelper.getPrimaryDarkColor(mContext),0);
        }
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (canBack()) {
            if (item.getItemId() == android.R.id.home) {
                onBackPressed();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void superOnBackPressed(boolean didClickTwice) {
        if (this instanceof MainActivity) {
            if (didClickTwice) {
                if (canExit()) {
                    super.onBackPressed();
                }
            } else {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }



    private boolean canExit() {
        if (backPressTimer + 2000 > System.currentTimeMillis()) {
            return true;
        } else {
            showMessage(R.string.press_again_to_exit,R.string.press_again_to_exit);
        }
        backPressTimer = System.currentTimeMillis();
        return false;
    }

    @Override public void onBackPressed() {
        boolean clickTwiceToExit = !PrefGetter.isTwiceBackButtonDisabled();
        superOnBackPressed(clickTwiceToExit);
    }


    @Override
    public void showProgress(@StringRes int resId) {
        showProgress(resId, true);
    }

    public void showProgress(@StringRes int resId, boolean cancelable) {
        String msg = getString(R.string.in_progress);
        if (resId != 0) {
            msg = getString(resId);
        }
        showProgress(msg, cancelable);
    }

    public void showProgress(CharSequence msg, boolean cancelable) {
        if (!isProgressShowing && !isFinishing()) {
            ProgressDialogFragment fragment = (ProgressDialogFragment) AppHelper.getFragmentByTag(getSupportFragmentManager(),
                    ProgressDialogFragment.TAG);
            if (fragment == null) {
                isProgressShowing = true;
                fragment = ProgressDialogFragment.newInstance(msg, cancelable);
                fragment.show(getSupportFragmentManager(), ProgressDialogFragment.TAG);
            }
        }
    }

    @Override
    public void hideProgress() {
        ProgressDialogFragment fragment = (ProgressDialogFragment) (getSupportFragmentManager().findFragmentByTag(
                ProgressDialogFragment.TAG));
        if (fragment != null) {
            isProgressShowing = false;
            fragment.dismiss();
        }
    }

    @Override
    public void showErrorMessage(@NonNull String msgRes) {
        showMessage(getString(R.string.error), msgRes);
    }

    @Override
    public void showMessage(int titleRes, int msgRes) {
        showMessage(getString(titleRes), getString(msgRes));
    }

    @Override
    public void showMessage(@NonNull String titleRes, @NonNull String msgRes) {
        hideProgress();
        if (titleRes.equals(getString(R.string.error))){
            ToastUtils.showError(msgRes);
        }else{


        }
       /* if (toast != null) toast.cancel();
        Context context = App.getInstance(); // WindowManager$BadTokenException

        toast = titleRes.equals(context.getString(R.string.error))
                ? Toasty.error(context, msgRes, Toast.LENGTH_LONG)//可以用Toasty.custom 修改颜色
                : Toasty.info(context, msgRes, Toast.LENGTH_LONG);
        toast.show();*/
    }

    @Override
    public boolean isLoggedIn() {
        return false;
    }

    public <T> LifecycleTransformer<T> bind2Lifecycle(){
        return bindToLifecycle();
    }

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        resetTitle(title,color);
    }



    /*  @Override
    public void onRequireLogin() {

    }

    @Override public void onThemeChanged() {
       if (this instanceof MainActivity) {
            recreate();
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtras(Bundler.start().put(BundleConstant.YES_NO_EXTRA, true).end());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }*/
}
