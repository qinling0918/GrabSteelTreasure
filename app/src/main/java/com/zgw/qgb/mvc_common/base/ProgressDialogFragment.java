package com.zgw.qgb.mvc_common.base;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.zgw.qgb.R;
import com.zgw.qgb.helper.Bundler;


/**
 * Created by qinling on 2020/6/4 18:25
 * Description: 基于 公共组件中 LoadingDialog 实现的 DialogFragment
 */
public class ProgressDialogFragment extends DialogFragment {
    private static final String MSG = "msg";
    private static final String IS_CANCELABLE = "isCancelable";
    public static final String TAG = /*"ProgressDialogFragment"*/ProgressDialogFragment.class.getCanonicalName();
    private boolean isShowing;
    private Context mContext;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @NonNull
    public static ProgressDialogFragment newInstance(@NonNull Resources resources, @StringRes int msgId, boolean isCancelable) {
        return newInstance(resources.getString(msgId), isCancelable);
    }

    @NonNull
    public static ProgressDialogFragment newInstance(@NonNull CharSequence msg, boolean isCancelable) {
        ProgressDialogFragment fragment = new ProgressDialogFragment();
        fragment.setArguments(Bundler.start()
                .put(MSG, msg)
                .put(IS_CANCELABLE, isCancelable)
                .end());
        return fragment;
    }

    @NonNull
    @Override
    public Context getContext() {
        return mContext;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        String msg = null == arguments ? getString(R.string.in_progress) : arguments.getString(MSG);
        boolean isCancelable = null != arguments && arguments.getBoolean(IS_CANCELABLE);
        return new AlertDialog.Builder(getContext())
                .setCancelable(isCancelable)
                .setMessage(msg)
                .create();

    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        isShowing = false;
    }

    @Override
    public int show(@NonNull FragmentTransaction transaction, @Nullable String tag) {
        isShowing = true;
        return super.show(transaction, tag);
    }

    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        isShowing = true;
        super.show(manager, tag);
    }

    @Override
    public void showNow(@NonNull FragmentManager manager, @Nullable String tag) {
        isShowing = true;
        super.showNow(manager, tag);
    }

    public boolean isShowing() {
        return isShowing;
    }
}
