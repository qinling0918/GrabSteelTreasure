package com.zgw.qgb.helper;

import android.content.Context;
import android.content.pm.ApplicationInfo;

/**
 * Created by Tsinling on 2017/9/26 13:58.
 * description:
 */

public final class DebugHelper {
    private DebugHelper(){}
    private static class SingletonHolder {
        private static final DebugHelper mInstance = new DebugHelper();
    }

    public static DebugHelper getInstance() {
        return SingletonHolder.mInstance;
    }

    private    Boolean isDebug = null;

    public boolean isDebug(){
        return isDebug!=null&&isDebug;
    }
    /**
     * 同步lib调试与应用程序的调试值。 应在模块Application中调用
     * 若是項目中有多個lib,moudle等,拿被依賴的buildConfig.debug的值可能一直為false
     * Sync lib debug with app's debug value. Should be called in module Application
     * @param context
     */
    public void syscIsDebug(Context context){
        if (isDebug ==null){
            isDebug =context.getApplicationInfo() !=null && (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)!=0;
        }
    }
}
