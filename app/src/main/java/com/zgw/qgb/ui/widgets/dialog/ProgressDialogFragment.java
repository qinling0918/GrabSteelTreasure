package com.zgw.qgb.ui.widgets.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;

import com.zgw.qgb.helper.AnimHelper;
import com.zgw.qgb.helper.Bundler;


/**
 * Created by Kosh on 09 Dec 2016, 5:18 PM
 */

public class ProgressDialogFragment extends DialogFragment {

    public static final String TAG = ProgressDialogFragment.class.getSimpleName();

    @NonNull
    public static ProgressDialogFragment newInstance(@NonNull Resources resources, @StringRes int msgId, boolean isCancelable) {
        return newInstance(resources.getString(msgId), isCancelable);
    }

    @NonNull
    public static ProgressDialogFragment newInstance(@NonNull CharSequence msg, boolean isCancelable) {
        ProgressDialogFragment fragment = new ProgressDialogFragment();
        fragment.setArguments(Bundler.start()
                .put("msg", msg)
                .put("isCancelable", isCancelable)
                .end());
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
   /*     return new MaterialDialog.Builder(getContext())
                .title(getArguments().getString("msg"))
                .content(getArguments().getString("msg"))
                .progress(true, 0)
                .cancelable(getArguments().getBoolean("isCancelable"))
                .progressIndeterminateStyle(false).build();

    }*/
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage(getArguments().getString("msg"));
        boolean isCancelable = getArguments().getBoolean("isCancelable");
        progressDialog.setCancelable(isCancelable);
        setCancelable(isCancelable);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (getActivity() != null && !getActivity().isFinishing()) {
                progressDialog.setOnShowListener(dialog -> {
                    AnimHelper.revealDialog(progressDialog, 200);
                });
            }
        }
        return progressDialog;
    }
}
