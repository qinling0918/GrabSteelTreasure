package com.zgw.qgb.helper;

import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SimpleArrayMap;

/**
 * Created by tsinling on 2018-2
 */

public class PrefGetter {
    private static SimpleArrayMap<String, PrefGetter> SP_UTILS_MAP = new SimpleArrayMap<>();
    private SharedPreferences sp;

    private static final String TOKEN = "token";
    private static final String USER_INFO = "userInfo";
    private static final String APP_LANGUAGE = "app_language";
    private static final String NOTIFICATION_SOUND_PATH = "notification_sound_path";

    public static void setToken(@Nullable String token) {
        PrefHelper.set(TOKEN, token);
    }

    public static String getToken() {
        return PrefHelper.getString(TOKEN);
    }

    public static void clear() {
        PrefHelper.clearPrefs();
    }



    public static boolean isRVAnimationEnabled() {
        return PrefHelper.getBoolean("recylerViewAnimation");
    }


    public static boolean isTwiceBackButtonDisabled() {
        return PrefHelper.getBoolean("back_button");
    }


    @NonNull
    public static String getAppLanguage() {
        String appLanguage = PrefHelper.getString(APP_LANGUAGE);
        return appLanguage == null ? "en" : appLanguage;
    }

    public static void setAppLangauge(@Nullable String language) {
        PrefHelper.set(APP_LANGUAGE, language == null ? "en" : language);
    }


    @Nullable
    public static Uri getNotificationSound() {
        String nsp = PrefHelper.getString(NOTIFICATION_SOUND_PATH);
        return !InputHelper.isEmpty(nsp) ? Uri.parse(nsp) : RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    }

    public static void setNotificationSound(@NonNull Uri uri) {
        PrefHelper.set(NOTIFICATION_SOUND_PATH, uri.toString());
    }

   /* @Nullable
    public static UserInfo getUserInfo() {
        Gson gson = new Gson();
        String userJson = PrefHelper.getString(USER_INFO);
        if (null == userJson){
            return new UserInfo();
        }
        return gson.fromJson(userJson, UserInfo.class);
    }

    public static void setUserInfo(@NonNull UserInfo userInfo) {
        *//*if (null == userInfo )
            return;*//*

        Gson gson = new Gson();
        //转换成json数据，再保存
        String strJson = gson.toJson(userInfo);
        PrefHelper.set(USER_INFO, strJson);
    }*/
}
