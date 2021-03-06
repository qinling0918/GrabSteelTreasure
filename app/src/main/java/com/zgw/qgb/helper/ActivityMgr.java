package com.zgw.qgb.helper;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;


import com.zgw.qgb.interf.ActivityLifecycleCallbacks;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Stack;

import timber.log.Timber;

/**
 * Created by Tsinling on 2017/9/25 16:51.
 * description:
 */

public class ActivityMgr  extends ActivityLifecycleCallbacks{

    private static Stack<WeakReference<Activity>> mActivityStack;

    public Application getApplication() {
        return mApplication;
    }

/*    public void setApplication(Application mApplication) {
        this.mApplication = mApplication;
    }*/
    private static Application mApplication;
    private ActivityLifecycleCallbacks  lifecycleCallbacks;


    private ActivityMgr() {
         lifecycleCallbacks = new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                getInstance().addActivity(activity);
                // getInstance().printAllActivity();
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                getInstance().removeActivity(activity);
            }
        };
    }

    private void registerActivityLifecycleCallbacks(){
        if (mApplication==null){
            throw new NullPointerException("mApplication == null, please init first");
        }
        mApplication.registerActivityLifecycleCallbacks(lifecycleCallbacks);
    }

    private static class SingletonHolder {
        private static final ActivityMgr mInstance = new ActivityMgr();
    }

    public static ActivityMgr getInstance() {
        return SingletonHolder.mInstance;
    }

    public static void init(Context context) {
        // 注册退出广播
       // new AppExitReceiver(context).register();
        ActivityMgr.mApplication = (Application) context;
        ActivityMgr.getInstance().registerActivityLifecycleCallbacks();
    }

    public void addActivity(Activity activity) {
        if (mActivityStack == null) {
            mActivityStack = new Stack<>();
        }
        mActivityStack.add(new WeakReference<>(activity));
    }

    public void finishAllActivity() {
        if (mActivityStack == null || mActivityStack.size() == 0) {
            return;
        }
        for (WeakReference<Activity> activityWeakReference : mActivityStack) {
            Activity activity = activityWeakReference.get();
            if (activity != null) {
                activity.finish();
            }
        }
        mActivityStack.clear();
    }

    /**
     * 获取当前Activity（栈中最后一个压入的）
     *
     * @return 当前（栈顶）activity
     */
    public Activity currentActivity() {
        if (mActivityStack != null && !mActivityStack.isEmpty()) {
            return mActivityStack.lastElement().get();
        }
        return null;
    }

    public void printAllActivity() {
        if (mActivityStack == null) {
            return;
        }
        for (int i = 0; i < mActivityStack.size(); i++) {
            Log.e("TAG", "位置" + i + ": " + mActivityStack.get(i).get().getLocalClassName());
        }
    }


    public void removeActivity(Activity activity) {
        if (activity != null && mActivityStack != null) {
            for (Iterator<WeakReference<Activity>> it = mActivityStack.iterator(); it.hasNext(); ) {
                WeakReference<Activity> activityReference = it.next();
                Activity temp = activityReference.get();
                if (temp == null) {// 清理掉已经释放的activity
                    it.remove();
                    continue;
                }
                if (temp == activity) {
                    it.remove();
                }
            }
        }
    }

    /**
     * 结束指定的Activity
     *
     * @param activity 指定的activity实例
     */
    public void finishActivity(Activity activity) {
        if (activity != null) {
            removeActivity(activity);
            activity.finish();
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
            ActivityManager activityMgr = (ActivityManager) mApplication.getSystemService(Context.ACTIVITY_SERVICE);
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
