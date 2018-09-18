package com.zgw.qgb.helper;

import android.content.Context;

/**
 * Name:FabricHelper
 * Created by Tsinling on 2017/10/21 9:26.
 * description:
 */

public class FabricHelper {
    private FabricHelper(){}
    private static class SingletonHolder {
        private static final FabricHelper mInstance = new FabricHelper();
    }

    public static FabricHelper getInstance() {
        return SingletonHolder.mInstance;
    }
    public void init(Context context){
        boolean debug = Utils.getInstance().isDebug();

       /* Fabric fabric = new Fabric.Builder(context)
                .kits(new Crashlytics.Builder()
                        .core(new CrashlyticsCore.Builder().disabled(debug).build())
                        .build())
                .debuggable(debug)
                .build();
        Fabric.with(fabric);*/
    }

}
