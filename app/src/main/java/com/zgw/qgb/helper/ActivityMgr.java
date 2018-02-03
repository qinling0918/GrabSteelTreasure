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
/*
public class ActivityStackManager {
    private static final String TAG = "ActivityStackManager";
    */
/**
     * Activity栈
     *//*

    private Stack<WeakReference<Activity>> mActivityStack;

    private static ActivityStackManager activityStackManager = new ActivityStackManager();


    private ActivityStackManager() {
    }

    */
/***
     * 获得AppManager的实例
     *
     * @return AppManager实例
     *//*

    public static ActivityStackManager getInstance() {
        if (activityStackManager == null) {
            activityStackManager = new ActivityStackManager();
        }
        return activityStackManager;
    }


    */
/***
     * 栈中Activity的数
     *
     * @return Activity的数
     *//*

    public int stackSize() {
        return mActivityStack.size();
    }

    */
/***
     * 获得Activity栈
     *
     * @return Activity栈
     *//*

    public Stack<WeakReference<Activity>> getStack() {
        return mActivityStack;
    }


    */
/**
     * 添加Activity到堆栈
     *//*

    public void addActivity(WeakReference<Activity> activity) {
        if (mActivityStack == null) {
            mActivityStack = new Stack<>();
        }
        mActivityStack.add(activity);
    }

    */
/**
     * 删除ac
     *
     * @param activity 弱引用的ac
     *//*

    public void removeActivity(WeakReference<Activity> activity) {
        if (mActivityStack != null) {
            mActivityStack.remove(activity);
        }
    }

    */
/***
     * 获取栈顶Activity（堆栈中最后一个压入的）
     *
     * @return Activity
     *//*

    public Activity getTopActivity() {
        Activity activity = mActivityStack.lastElement().get();
        if (null == activity) {
            return null;
        } else {
            return mActivityStack.lastElement().get();
        }
    }

    */
/***
     * 通过class 获取栈顶Activity
     *
     * @param cls
     * @return Activity
     *//*

    public Activity getActivityByClass(Class<?> cls) {
        Activity return_activity = null;
        for (WeakReference<Activity> activity : mActivityStack) {
            if (activity.get().getClass().equals(cls)) {
                return_activity = activity.get();
                break;
            }
        }
        return return_activity;
    }

    */
/**
     * 结束栈顶Activity（堆栈中最后一个压入的）
     *//*

    public void killTopActivity() {
        try {
            WeakReference<Activity> activity = mActivityStack.lastElement();
            killActivity(activity);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    */
/***
     * 结束指定的Activity
     *
     * @param activity
     *//*

    public void killActivity(WeakReference<Activity> activity) {
        try {
            Iterator<WeakReference<Activity>> iterator = mActivityStack.iterator();
            while (iterator.hasNext()) {
                WeakReference<Activity> stackActivity = iterator.next();
                if (stackActivity.get() == null) {
                    iterator.remove();
                    continue;
                }
                if (stackActivity.get().getClass().getName().equals(activity.get().getClass().getName())) {
                    iterator.remove();
                    stackActivity.get().finish();
                    break;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }


    */
/***
     * 结束指定类名的Activity
     *
     * @param cls
     *//*

    public void killActivity(Class<?> cls) {
        try {

            ListIterator<WeakReference<Activity>> listIterator = mActivityStack.listIterator();
            while (listIterator.hasNext()) {
                Activity activity = listIterator.next().get();
                if (activity == null) {
                    listIterator.remove();
                    continue;
                }
                if (activity.getClass() == cls) {
                    listIterator.remove();
                    if (activity != null) {
                        activity.finish();
                    }
                    break;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    */
/**
     * 结束所有Activity
     *//*

    public void killAllActivity() {
        try {
            ListIterator<WeakReference<Activity>> listIterator = mActivityStack.listIterator();
            while (listIterator.hasNext()) {
                Activity activity = listIterator.next().get();
                if (activity != null) {
                    activity.finish();
                }
                listIterator.remove();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }


    */
/**
     * 移除除了某个activity的其他所有activity
     *
     * @param cls 界面
     *//*

    public void killAllActivityExceptOne(Class cls) {
        try {
            for (int i = 0; i < mActivityStack.size(); i++) {
                WeakReference<Activity> activity = mActivityStack.get(i);
                if (activity.getClass().equals(cls)) {
                    break;
                }
                if (mActivityStack.get(i) != null) {
                    killActivity(activity);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
*/
