package com.zgw.qgb.helper;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.zgw.qgb.App;

import java.util.Map;

/**
 * Created by kosh 20111 on 19 Feb 2017, 2:01 AM
 */
public class PrefHelper {
    /**
     * @param key ( the Key to used to retrieve this data later  )
     * @param value ( any kind of primitive values  )  non can be null!!!
     */

    @SuppressLint("ApplySharedPref") public static <T> void set(@NonNull String key, @Nullable T value) {
        set(key, value, true);
    }

    /**
     * @param key ( the Key to used to retrieve this data later  )
     * @param value ( any kind of primitive values  )
     * @param isCommit  {@code true}: {@link SharedPreferences.Editor#commit()}<br>
     *                  {@code false}: {@link SharedPreferences.Editor#apply()}
     */
    @SuppressLint("ApplySharedPref") public static <T> void set(@NonNull String key, @Nullable T value, @Nullable boolean isCommit) {
        if (InputHelper.isEmpty(key)) {
            throw new NullPointerException("Key must not be null! (key = " + key + "), (value = " + value + ")");
        }
        SharedPreferences.Editor edit = getSharedPreferences().edit();
        if (InputHelper.isEmpty(value)) {
            clearKey(key);
            return;
        }
        if (value instanceof String) {
            edit.putString(key, (String) value);
        } else if (value instanceof Integer) {
            edit.putInt(key, (Integer) value);
        } else if (value instanceof Long) {
            edit.putLong(key, (Long) value);
        } else if (value instanceof Boolean) {
            edit.putBoolean(key, (Boolean) value);
        } else if (value instanceof Float) {
            edit.putFloat(key, (Float) value);
        } else {
            edit.putString(key, value.toString());
        }
        if (isCommit){
            edit.commit(); //apply on UI
        }else{
            edit.apply();
        }

    }

    private static SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(App.getInstance());
    }

    @Nullable
    public static String getString(@NonNull String key) {
        return getSharedPreferences().getString(key, null);
    }

    public static boolean getBoolean(@NonNull String key) {
        return getSharedPreferences().getBoolean(key, false);
    }

    public static int getInt(@NonNull String key) {
        return getSharedPreferences().getInt(key, 0);
    }

    public static long getLong(@NonNull String key) {
        return getSharedPreferences().getLong(key, 0);
    }

    public static float getFloat(@NonNull String key) {
        return getSharedPreferences().getFloat(key, 0);
    }

    public static void clearKey(@NonNull String key) {
        getSharedPreferences().edit().remove(key).apply();
    }

    public static boolean isExist(@NonNull String key) {
        return getSharedPreferences().contains(key);
    }

    public static void clearPrefs() {
        getSharedPreferences().edit().clear().apply();
    }

    public static Map<String, ?> getAll() {
        return getSharedPreferences().getAll();
    }
}
