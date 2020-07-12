package com.zgw.qgb.mvc_common.helper;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SimpleArrayMap;
import android.util.Base64;

import com.zgw.qgb.mvc_common.Utils;
import com.zgw.qgb.mvc_common.download.FileUtils;
import com.zgw.qgb.mvc_common.utils.EmptyUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;


public class PrefHelper {
    private static SimpleArrayMap<String, PrefHelper> SP_MAP = new SimpleArrayMap<>();
    private SharedPreferences sp;

    private PrefHelper() {}


    private PrefHelper(String spName) {
        this.sp = getContext().getSharedPreferences(getSharedPreferencesName(spName),
                Context.MODE_PRIVATE);

    }


    /**
     * Return the single {@link PrefHelper} instance
     *
     * @param spName The name of sp.
     * @return the single {@link PrefHelper} instance
     */
    public static PrefHelper getInstance(String spName) {
        spName = InputHelper.emptyOrDefault(spName,"preferences");

        PrefHelper prefHelper = SP_MAP.get(spName);
        if (EmptyUtils.isEmpty(prefHelper)) {
            prefHelper = new PrefHelper(spName);
            SP_MAP.put(spName, prefHelper);
        }
        return prefHelper;
    }

    public static PrefHelper getInstance() {
        return getInstance(null);
    }

    public Editor edit() {
        return Editor.edit(sp);
    }


    private Application getContext() {
        return Utils.getApp();
    }

    private String getSharedPreferencesName(String spName) {
        return  getContext().getPackageName() + "_"+spName;
    }


    private SharedPreferences getDefaultSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(getContext());
    }



    public static class Editor {

        private final SharedPreferences.Editor sp_editor;


        private Editor(SharedPreferences sp) {
            sp_editor = sp.edit();

        }

        public static Editor edit(SharedPreferences sp) {
            return new Editor(sp);
        }

        public Editor put(String key, @Nullable String value) {
            sp_editor.putString(key,value);
            return this;
        }

        public Editor put(String key, @Nullable Set<String> values) {
            sp_editor.putStringSet(key,values);
            return this;
        }

        public Editor put(String key, int value) {
            sp_editor.putInt(key,value);
            return this;
        }

        public Editor put(String key, long value) {
            sp_editor.putLong(key,value);
            return this;
        }

        public Editor put(String key, float value) {
            sp_editor.putFloat(key,value);
            return this;
        }

        public Editor put(String key, boolean value) {
            sp_editor.putBoolean(key,value);
            return this;
        }
        /**
         * 将对象储存到sharepreference
         *
         * @param key
         * @param <T>
         */
        public <T extends Serializable> Editor put(String key, T serializable) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = null;
            try {   //Device为自定义类
                // 创建对象输出流，并封装字节流
                oos = new ObjectOutputStream(baos);
                // 将对象写入字节流
                oos.writeObject(serializable);
                // 将字节流编码成base64的字符串
                String oAuth_Base64 = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
                put(key, oAuth_Base64);
                return this;
            } catch (Exception e) {
                e.printStackTrace();
                FileUtils.close(oos,baos);
                return this;
            }
        }

        public Editor remove(String key) {
            sp_editor.remove(key);
            return this;
        }

        public Editor clear() {
            sp_editor.clear();
            return this;
        }
        public boolean commit() {
            return sp_editor.commit();
        }

        public void apply() {
             sp_editor.apply();

        }
    }


    /**
     * Return the string value in sp.
     *
     * @param key The key of sp.
     * @return the string value if sp exists or {@code ""} otherwise
     */
    public String getString(@NonNull final String key) {
        return getString(key, "");
    }

    /**
     * Return the string value in sp.
     *
     * @param key          The key of sp.
     * @param defaultValue The default value if the sp doesn't exist.
     * @return the string value if sp exists or {@code defaultValue} otherwise
     */
    public String getString(@NonNull final String key, final String defaultValue) {
        return sp.getString(key, defaultValue);
    }


    /**
     * Return the int value in sp.
     *
     * @param key The key of sp.
     * @return the int value if sp exists or {@code -1} otherwise
     */
    public int getInt(@NonNull final String key) {
        return getInt(key, -1);
    }

    /**
     * Return the int value in sp.
     *
     * @param key          The key of sp.
     * @param defaultValue The default value if the sp doesn't exist.
     * @return the int value if sp exists or {@code defaultValue} otherwise
     */
    public int getInt(@NonNull final String key, final int defaultValue) {
        return sp.getInt(key, defaultValue);
    }


    /**
     * Return the long value in sp.
     *
     * @param key The key of sp.
     * @return the long value if sp exists or {@code -1} otherwise
     */
    public long getLong(@NonNull final String key) {
        return getLong(key, -1L);
    }

    /**
     * Return the long value in sp.
     *
     * @param key          The key of sp.
     * @param defaultValue The default value if the sp doesn't exist.
     * @return the long value if sp exists or {@code defaultValue} otherwise
     */
    public long getLong(@NonNull final String key, final long defaultValue) {
        return sp.getLong(key, defaultValue);
    }


    /**
     * Return the float value in sp.
     *
     * @param key The key of sp.
     * @return the float value if sp exists or {@code -1f} otherwise
     */
    public float getFloat(@NonNull final String key) {
        return getFloat(key, -1f);
    }

    /**
     * Return the float value in sp.
     *
     * @param key          The key of sp.
     * @param defaultValue The default value if the sp doesn't exist.
     * @return the float value if sp exists or {@code defaultValue} otherwise
     */
    public float getFloat(@NonNull final String key, final float defaultValue) {
        return sp.getFloat(key, defaultValue);
    }



    /**
     * Return the boolean value in sp.
     *
     * @param key The key of sp.
     * @return the boolean value if sp exists or {@code false} otherwise
     */
    public boolean getBoolean(@NonNull final String key) {
        return getBoolean(key, false);
    }

    /**
     * Return the boolean value in sp.
     *
     * @param key          The key of sp.
     * @param defaultValue The default value if the sp doesn't exist.
     * @return the boolean value if sp exists or {@code defaultValue} otherwise
     */
    public boolean getBoolean(@NonNull final String key, final boolean defaultValue) {
        return sp.getBoolean(key, defaultValue);
    }


    /**
     * Return the set of string value in sp.
     *
     * @param key The key of sp.
     * @return the set of string value if sp exists
     * or {@code Collections.<String>emptySet()} otherwise
     */
    public Set<String> getStringSet(@NonNull final String key) {
        return getStringSet(key, Collections.<String>emptySet());
    }

    /**
     * Return the set of string value in sp.
     *
     * @param key          The key of sp.
     * @param defaultValue The default value if the sp doesn't exist.
     * @return the set of string value if sp exists or {@code defaultValue} otherwise
     */
    public Set<String> getStringSet(@NonNull final String key,
                                    final Set<String> defaultValue) {
        return sp.getStringSet(key, defaultValue);
    }
    /**
     * 将对象从shareprerence中取出来
     *
     * @param key
     * @param <T>
     * @return
     */
    public <T extends Serializable> T getSerializable(final String key) {
        return getSerializable(key,null);
    }
    public <T extends Serializable> T getSerializable(final String key,final T defaultValue) {
        T serializable = defaultValue;
        String base64Str = getString(key, null);
        if (base64Str == null) {
            return defaultValue;
        }
        // 读取字节
        byte[] base64 = Base64.decode(base64Str.getBytes(), Base64.DEFAULT);
        // 封装到字节流
        ByteArrayInputStream bais = new ByteArrayInputStream(base64);
        ObjectInputStream bis = null;
        try {
            bis = new ObjectInputStream(bais);
            serializable = (T) bis.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            FileUtils.close(bais,bis);
        }
        return serializable;
    }

    /**
     * Return all values in sp.
     *
     * @return all values in sp
     */
    public Map<String, ?> getAll() {
        return sp.getAll();
    }

    /**
     * Return whether the sp contains the preference.
     *
     * @param key The key of sp.
     * @return {@code true}: yes<br>{@code false}: no
     */
    public boolean contains(@NonNull final String key) {
        return sp.contains(key);
    }



 /*   private static boolean isSpace(final String s) {
        if (s == null) return true;
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }*/
}
