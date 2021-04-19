package com.zgw.qgb.helper.utils;

import android.os.Build;
import androidx.annotation.NonNull;
import androidx.collection.LongSparseArray;
import androidx.collection.SimpleArrayMap;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.SparseLongArray;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
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
        if (obj instanceof CharSequence && ((CharSequence)obj).length() == 0) {
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


    /**
     * 判断对象是否非空
     *
     * @param obj 对象
     * @return {@code true}: 非空<br>{@code false}: 空
     */
    public static boolean isNotEmpty(final Object obj) {
        return !isEmpty(obj);
    }

    /**
     * 全为空
     *
     * @param objs
     * @return
     */
    public static boolean isAllEmpty(final Object... objs) {
        boolean isEmpty = true;
        if (isEmpty(objs)) {
            return isEmpty;
        }
        for (Object obj : objs) {
            isEmpty = isEmpty && isEmpty(obj);
        }
        return isEmpty;
    }

    /**
     * 存在有为空的数据
     *
     * @param objs
     * @return
     */
    public static boolean isHaveEmpty(final Object... objs) {
        boolean isEmpty = false;
        if (isEmpty(objs)) {
            return isEmpty;
        }
        for (Object obj : objs) {
            isEmpty = isEmpty || isEmpty(obj);
        }
        return isEmpty;
    }

    /**
     * 不存在有空的数据
     * @param objs
     * @return
     */
    public static boolean isNotHaveEmpty(final Object... objs) {
        return !isHaveEmpty(objs);
    }
    /**
     * 不是全为空
     *
     * @param objs
     * @return
     */
    public static boolean isNotAllEmpty(final Object... objs) {
        return !isAllEmpty(objs);
    }

    public static <T> T checkNotNull(T value, String message) {
        if (value == null) {
            throw new NullPointerException(message);
        }
        return value;
    }
    /**
     * Returns 0 if the arguments are identical and {@code
     * c.compare(a, b)} otherwise.
     * Consequently, if both arguments are {@code null} 0
     * is returned.
     */
    public static <T> int compare(T a, T b, @NonNull Comparator<? super T> c) {
        return (a == b) ? 0 : c.compare(a, b);
    }

    /**
     * Checks that the specified object reference is not {@code null}.
     */
    public static <T> T requireNonNull(T obj) {
        if (obj == null) throw new NullPointerException();
        return obj;
    }

    /**
     * Checks that the specified object reference is not {@code null} and
     * throws a customized {@link NullPointerException} if it is.
     */
    public static <T> T requireNonNull(T obj, String ifNullTip) {
        if (obj == null) throw new NullPointerException(ifNullTip);
        return obj;
    }

    /**
     * Require the objects are not null.
     *
     * @param objects The object.
     * @throws NullPointerException if any object is null in objects
     */
    public static void requireNonNulls(final Object... objects) {
        if (objects == null) throw new NullPointerException();
        for (Object object : objects) {
            if (object == null) throw new NullPointerException();
        }
    }

    /**
     * Return the nonnull object or default object.
     *
     * @param object        The object.
     * @param defaultObject The default object to use with the object is null.
     * @param <T>           The value type.
     * @return the nonnull object or default object
     */
    public static <T> T getOrDefault(final T object, final T defaultObject) {
        if (object == null) {
            return defaultObject;
        }
        return object;
    }

    /**
     * Returns the result of calling {@code toString} for a non-{@code
     * null} argument and {@code "null"} for a {@code null} argument.
     */
    public static String toString(Object obj) {
        return String.valueOf(obj);
    }

    /**
     * Returns the result of calling {@code toString} on the first
     * argument if the first argument is not {@code null} and returns
     * the second argument otherwise.
     */
    public static String toString(Object o, String nullDefault) {
        return (o != null) ? o.toString() : nullDefault;
    }



    /**
     * Return the hash code of objects.
     */
    public static int hashCodes(Object... values) {
        return Arrays.hashCode(values);
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
