package com.zgw.qgb.mvc_common.base;



import java.util.Observable;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.zgw.qgb.R;
import com.zgw.qgb.helper.ToastUtils;

/**
 * Created by qinling on 2020/6/5 11:17
 * Description: 实现了IView 的公共部分，主要有显示隐藏进度框，以及提示语句的显示。
 */
public class IViewActivity extends AppCompatActivity implements IView {


    @Override
    public void showProgress() {
        showProgress(R.string.in_progress);
    }

    @Override
    public void showProgress(@StringRes int resId) {
        showProgress(resId, false);
    }

    @Override
    public void showProgress(@StringRes int resId, boolean cancelable) {
        String msg = getString(R.string.in_progress);
        if (resId != 0) {
            msg = getString(resId);
        }
        showProgress(msg, cancelable);
    }

    @Override
    public void showProgress(CharSequence msg, boolean cancelable) {
        if (isFinishing()) {
            return;
        }
        ProgressDialogFragment fragment = getFragmentByTag();
        // 避免重复添加，
        if (null!=fragment){
            getSupportFragmentManager().beginTransaction().remove(fragment);
        }
        fragment = null == fragment ? ProgressDialogFragment.newInstance(msg, cancelable) : fragment;
        if (!fragment.isShowing()) {

            fragment.showNow(getSupportFragmentManager(), ProgressDialogFragment.TAG);
           // LogPrintUtils.d("dialog:"+ this.getClass().getName()+"   showProgress: "+ getFragmentByTag());
        }
    }

    /**
     * 通过tag查找 fragment  此处注意 ProgressDialogFragment 的Tag 建议不要用
     * ProgressDialogFragment.class.getSimpleName() 来获取，
     * 避免出现包名不一致，但类名相同的fragment，尤其是打包混淆后
     * 例如 混淆后的 包类格式可能存在 a.b.c 以及a.b.c.c 这种格式，用simpleName 得到的tag 重复率较高。
     * @return
     */
    private ProgressDialogFragment getFragmentByTag() {
        return (ProgressDialogFragment) (getSupportFragmentManager().findFragmentByTag(ProgressDialogFragment.TAG));
    }

    @Override
    public void hideProgress() {
        ProgressDialogFragment fragment = getFragmentByTag();
        if (fragment != null) {
            fragment.dismiss();
        }
    }

    @Override
    public void showMessage(@StringRes int msgRes) {
        showMessage(getString(msgRes));
    }


    @Override
    public void showMessage(@NonNull CharSequence msg) {

        ToastUtils.showNormal(msg);
    }

    @Override
    public void update(Observable o, Object arg) {

    }
}
