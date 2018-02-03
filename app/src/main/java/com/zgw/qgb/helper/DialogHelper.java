package com.zgw.qgb.helper;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;


public class DialogHelper {

    public static void showDialog(Dialog dialog) {
        if (!dialog.isShowing() && null != dialog) {
            dialog.show();
        }
    }

    public static void closeDialog(Dialog dialog) {
        if(null == dialog) return;
        if(!dialog.isShowing()){  dialog = null; return;}

        Context context = ((ContextWrapper)dialog.getContext()).getBaseContext();
        boolean isActivity = context instanceof Activity ;
        boolean canDismiss = !isActivity || activityIsNotFinish((Activity) context);

        if (canDismiss) dialog.dismiss();
        dialog = null;
    }

    /**
     *
     * @param context
     * @return 没有被销毁  返回 true
     */
    private static boolean activityIsNotFinish(Activity context) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
                ? !context.isFinishing() && !context.isDestroyed()
                : !context.isFinishing();
    }


}
