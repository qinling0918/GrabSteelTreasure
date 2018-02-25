/**
 *  Copyright tsinling
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 */

package com.zgw.qgb.helper;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.zgw.qgb.interf.ActivityLifecycleCallbacks;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Stack;

/**
 * Created by tsinling on 2018/1/20.
 *
 */

public class ActivityStack extends ActivityLifecycleCallbacks {
    private static final String TAG = "ActivityStack";
    private Stack<WeakReference<Activity>> mActivityStack;
    private boolean init; //是否初始化过

    private ActivityStack(){}
    private static class SingletonHolder {
        private static final ActivityStack mInstance = new ActivityStack();
    }

    public static ActivityStack getInstance() {
        return ActivityStack.SingletonHolder.mInstance;
    }

    public boolean isInit() {
        return init;
    }

    public void init(Application mApplication) {
        init = true;
        mApplication.registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        addActivity(activity);
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        removeActivity(activity);
    }

    /***
     * 栈中Activity的数
     *
     * @return Activity的数
     */
    public int stackSize() {
        return null!= mActivityStack && !mActivityStack.empty() ? mActivityStack.size() : 0;
    }
    /***
     * 获得Activity栈
     *
     * @return Activity栈
     */
    public Stack<WeakReference<Activity>> getStack() {
        return mActivityStack;
    }
    /**
     * 添加Activity到堆栈
     */
    public void addActivity(Activity activity) {
        if (null == mActivityStack) {
            mActivityStack = new Stack<>();
        }
        mActivityStack.add(new WeakReference<>(activity));
    }
    /**
     * 从 堆栈移除
     *
     * @param activity
     */
    public void removeActivity(Activity activity) {
        if (mActivityStack != null) {
            Iterator<WeakReference<Activity>> iterator = mActivityStack.iterator();
            while (iterator.hasNext()) {
                WeakReference<Activity> stackActivity = iterator.next();
                if (null == stackActivity.get()) {
                    iterator.remove();
                    continue;
                }
                if (stackActivity.get().getClass().getName().equals(activity.getClass().getName())) {
                    iterator.remove();
                    break;
                }
            }
        }

    }
}
