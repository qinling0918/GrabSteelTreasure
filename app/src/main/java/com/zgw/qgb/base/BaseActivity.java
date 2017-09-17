package com.zgw.qgb.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.jaeger.library.StatusBarUtil;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;
import com.zgw.qgb.App;
import com.zgw.qgb.R;
import com.zgw.qgb.base.mvp.IPresenter;
import com.zgw.qgb.base.mvp.IView;
import com.zgw.qgb.helper.AppHelper;
import com.zgw.qgb.helper.PrefGetter;
import com.zgw.qgb.helper.ViewHelper;
import com.zgw.qgb.ui.moudle.main.MainActivity;
import com.zgw.qgb.ui.widgets.dialog.ProgressDialogFragment;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import icepick.Icepick;
import icepick.State;

import static com.trello.rxlifecycle2.internal.Preconditions.checkNotNull;

/**
 * Created by Tsinling on 2017/8/12 11:49.
 * description:
 */

public abstract class BaseActivity<P extends IPresenter> extends RxAppCompatActivity implements IView {
    protected final String TAG = this.getClass().getSimpleName();
    protected P mPresenter;
    protected Context mContext;
    @State boolean isProgressShowing;
    @State Bundle presenterStateBundle = new Bundle();
    private Toast toast;
    private long backPressTimer;

    @Nullable @BindView(R.id.toolbar) public Toolbar toolbar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        if (layout() != 0) {
            setContentView(layout());
            ButterKnife.bind(this);
        }

        mPresenter = createPresenter();
        checkNotNull(mPresenter,"presenter can't be null");

        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            Icepick.restoreInstanceState(this, savedInstanceState);
            mPresenter.onRestoreInstanceState(savedInstanceState);
            //mPresenter.onRestoreInstanceState(presenterStateBundle);
        }

        if (null!=toolbar){
            setupToolbarAndStatusBar(toolbar);
        }

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
        //mPresenter.onSaveInstanceState(presenterStateBundle);
    }



    protected abstract P createPresenter();

    @LayoutRes
    protected abstract int layout();

    protected  boolean canBack(){
        return true;
    }

    protected  boolean isTransparent(){
        return false;
        //return true;
    }

    private void setupToolbarAndStatusBar(@Nullable Toolbar toolbar) {
        changeStatusBarColor(isTransparent());
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (canBack()) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher);
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
            Toast.makeText(App.getInstance(), R.string.press_again_to_exit, Toast.LENGTH_SHORT).show();
        }
        backPressTimer = System.currentTimeMillis();
        return false;
    }

    @Override public void onBackPressed() {
        boolean clickTwiceToExit = !PrefGetter.isTwiceBackButtonDisabled();
        superOnBackPressed(clickTwiceToExit);
    }




  /*  private void showProgress() {
        showProgress(0,true);
    }*/

    @Override
    public void showProgress(int resId) {
        showProgress(resId, true);
    }
    private void showProgress(int resId, boolean cancelable) {
        String msg = getString(R.string.in_progress);
        if (resId != 0) {
            msg = getString(resId);
        }
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
        ProgressDialogFragment fragment = (ProgressDialogFragment) AppHelper.getFragmentByTag(getSupportFragmentManager(),
                ProgressDialogFragment.TAG);
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
        if (toast != null) toast.cancel();
        Context context = App.getInstance(); // WindowManager$BadTokenException

        toast = titleRes.equals(context.getString(R.string.error))
                ? Toasty.error(context, msgRes, Toast.LENGTH_LONG)//可以用Toasty.custom 修改颜色
                : Toasty.info(context, msgRes, Toast.LENGTH_LONG);
        toast.show();
    }

    @Override
    public boolean isLoggedIn() {
        return false;
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
