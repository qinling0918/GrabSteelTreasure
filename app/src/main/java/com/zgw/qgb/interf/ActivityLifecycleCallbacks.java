package com.zgw.qgb.interf;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * Created by Tsinling on 2017/9/25 15:47.
 * description:
 */

public class ActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
    public static ActivityLifecycleCallbacks newInstance() {
        return new ActivityLifecycleCallbacks();
    }

    /**
     * 此处用 onActivityStarted 和 onActivityStopped 来计frontActivities数，而非 onActivityResumed和onActivityPaused
     * 是因为生命周期的影响。
     * 例如 两个Acitivty A、B
     * 在A的onResume中开始跳转到B,则生命周期将会是
     * A.onCreate --> A.onStart --> A.onResume --> A.onPause
     * --> B.onCreate --> B.onStart --> B.onResume --> A.onStop
     * 在该过程中就会发现  若用 onResume和 onPause 来计数
     * 即在A到B 跳转时，从 A.onPause到 B.onResume 将会存在 frontActivities 为0的情况
     * 故 用onResume和 onPause 并不严谨，此处选择用  onStart与 onStop 来计数。
     * <p>
     * <p>
     * 1)、onStart()是activity界面被显示出来的时候执行的，用户可见，包括有一个activity在他上面，
     * 但没有将它完全覆盖，用户可以看到部分activity但不能与它交互！！！
     * 2)、onResume()是当该activity与用户能进行交互时被执行，用户可以获得activity的焦点，能够与用户交互！！！
     * 3)、onStart()通常就是onStop()（也就是用户按下了home键，activity变为后台后），
     * 之后用户再切换回这个activity就会调用onRestart()而后调用onStart()
     * 4)、onResume()是onPause()（通常是当前的acitivty被暂停了，比如被另一个透明或者Dialog样式的Activity
     * 覆盖了），之后dialog取消，activity回到可交互状态，调用onResume()
     * <p>
     * <p>
     * <p>
     * 若是 frontActivities == 0 则表示在后台，
     */
    protected int frontActivities = 0;

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        ++frontActivities;
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        --frontActivities;
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

}
