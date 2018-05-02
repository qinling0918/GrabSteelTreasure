package com.zgw.qgb.mvc_common.utils;

import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;
import android.support.v4.util.SimpleArrayMap;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.SparseLongArray;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

/**
 * <pre>
 *     author: Blankj
 *     blog  : http://blankj.com
 *     time  : 2016/09/28
 *     desc  : 判空相关工具类
 * </pre>
 */
public final class EmptyUtils {

    private EmptyUtils() {
        throw new AssertionError("No instances.");
    }

    /**
     * 判断对象是否为空
     *
     * @param obj 对象
     * @return {@code true}: 为空<br>{@code false}: 不为空
     */
    public static boolean isEmpty(final Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof CharSequence && obj.toString().length() == 0) {
            return true;
        }
        if (obj.getClass().isArray() && Array.getLength(obj) == 0) {
            return true;
        }
        if (obj instanceof Collection && ((Collection) obj).isEmpty()) {
            return true;
        }
        if (obj instanceof Map && ((Map) obj).isEmpty()) {
            return true;
        }
        if (obj instanceof SimpleArrayMap && ((SimpleArrayMap) obj).isEmpty()) {
            return true;
        }
        if (obj instanceof SparseArray && ((SparseArray) obj).size() == 0) {
            return true;
        }
        if (obj instanceof SparseBooleanArray && ((SparseBooleanArray) obj).size() == 0) {
            return true;
        }
        if (obj instanceof SparseIntArray && ((SparseIntArray) obj).size() == 0) {
            return true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (obj instanceof SparseLongArray && ((SparseLongArray) obj).size() == 0) {
                return true;
            }
        }
        if (obj instanceof LongSparseArray && ((LongSparseArray) obj).size() == 0) {
            return true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (obj instanceof android.util.LongSparseArray && ((android.util.LongSparseArray) obj).size() == 0) {
                return true;
            }
        }
        return false;
    }



    private static boolean isWhiteSpaces(@Nullable String s) {
        return s != null && s.matches("\\s+");
    }
    public static boolean isEmpty(@Nullable String text) {
        return text == null || TextUtils.isEmpty(text) || isWhiteSpaces(text) || text.equalsIgnoreCase("null");
    }
    /**
     * 判断对象是否非空
     *
     * @param obj 对象
     * @return {@code true}: 非空<br>{@code false}: 空
     */
    public static boolean isNotEmpty(final Object obj) {
        return !isEmpty(obj);
    }


    public static <T> T checkNotNull(T value, String message) {
        if (value == null) {
            throw new NullPointerException(message);
        }
        return value;
    }

    /**
     * 判断对象是否相等
     *
     * @param o1 对象1
     * @param o2 对象2
     * @return {@code true}: 相等<br>{@code false}: 不相等
     */
    public static boolean equals(Object o1, Object o2) {
        return o1 == o2 || (o1 != null && o1.equals(o2));
    }

    /**
     * 获取对象哈希值
     *
     * @param o 对象
     * @return 哈希值
     */
    public static int hashCode(Object o) {
        return o != null ? o.hashCode() : 0;
    }
}
