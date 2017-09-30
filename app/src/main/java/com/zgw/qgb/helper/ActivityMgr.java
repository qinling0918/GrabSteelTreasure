package com.zgw.qgb.helper;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.zgw.qgb.interf.ActivityLifecycleCallbacks;

import java.util.Stack;

/**
 * Created by Tsinling on 2017/9/25 16:51.
 * description:
 */

public class ActivityMgr {

    private static Stack<Activity> mActivityStack;
    private Application mApplication;

    private ActivityMgr() {
    }

    private static class SingletonHolder {
        private static final ActivityMgr mInstance = new ActivityMgr();
    }

    public static ActivityMgr getInstance() {
        return SingletonHolder.mInstance;
    }

    public void init(Application application) {
        mApplication = application;
        mApplication.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                if (mActivityStack == null) {
                    mActivityStack = new Stack<>();
                }
                mActivityStack.add(activity);
            }


            @Override
            public void onActivityDestroyed(Activity activity) {
                mActivityStack.remove(activity);
            }
        });
    }

    public void finishAllActivity() {
        if (mActivityStack == null || mActivityStack.size() == 0) {
            return;
        }
        for (Activity activity : mActivityStack) {
            activity.finish();
        }
        mActivityStack.clear();
    }



    public void printAllActivity() {
        if (mActivityStack == null) {
            return;
        }
        for (int i = 0; i < mActivityStack.size(); i++) {
            Log.e("TAG", "位置" + i + ": " + mActivityStack.get(i));
        }
    }


    public void removeActivity(Activity activity) {
        if (mActivityStack != null) {
            mActivityStack.remove(activity);
        }
    }

    /**
     * 退出应用程序
     */
    public void appExit() {
        try {
            finishAllActivity();

            if (null == mApplication) {
                throw new NullPointerException("Please initialize in application ");
            }
            ActivityManager activityMgr =
                    (ActivityManager) mApplication.getSystemService(Context.ACTIVITY_SERVICE);
            if (activityMgr != null) {
                activityMgr.killBackgroundProcesses(mApplication.getPackageName());
            }
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mApplication = null;
    }

}
