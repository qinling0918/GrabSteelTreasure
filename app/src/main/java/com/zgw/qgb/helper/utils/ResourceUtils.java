package com.zgw.qgb.helper.utils;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.annotation.ArrayRes;

import com.zgw.qgb.App;

/**
 * Name:ReaourceUtils
 * Created by Tsinling on 2017/10/31 10:36.
 * description:
 */

public final class ResourceUtils {
    private ResourceUtils() {
        throw new IllegalArgumentException("u can not instaniste me");
    }

    public static Resources getResources() {
        return App.getContext().getResources();
    }

    public static int[] getDrawableId(@ArrayRes int id) {
        TypedArray ar = getResources().obtainTypedArray(id);
        final int len = ar.length();
        final int[] resIds = new int[len];
        for (int i = 0; i < len; i++){
            resIds[i] = ar.getResourceId(i, 0);
        }
        ar.recycle();
        return resIds;
    }
}
