package com.zgw.qgb.mvc_common.helper;

import android.accounts.Account;
import android.content.Context;

import androidx.annotation.Nullable;

import com.zgw.qgb.helper.Utils;

import java.util.ArrayList;


/**
 * created by tsinling on: 2018/4/29 08:42
 * description:
 */
public class PrefUtils {

    //private static String accountSt = "";
    private static final String TOKEN = "token";
    private static final String[] USER_INFO = new String[]{"name","type"};
    private static final String IS_LOGIN = "is_login";



   /* public static void init(String account) {
        accountSt = account ;
    }*/
   public static PrefHelper.PrefGetter getSp() {
        return PrefHelper.getInstance("");
    }
    public static PrefHelper.PrefGetter getSp(String spName) {
        return PrefHelper.getInstance(spName);
    }
    public static void setToken(@Nullable String token) {
        getSp().edit().put(TOKEN, token).apply();
    }

    public static String getToken() {
        return getSp().getString(TOKEN);
    }

    public static void setUserInfo(@Nullable Account token) {
        getSp().edit()
                .put(USER_INFO[0], token.name)
                .put(USER_INFO[1],token.type)
                .put(USER_INFO[0], token.name+token.type)
                .commit();
    }
    /*public static void setbean(String frame) {
        getSp().edit()
                .put(, frame.substring(0,8))
                .put(USER_INFO[1],token.type)
                .put(USER_INFO[0], token.name+token.type)
                .commit();
    }*/
    public static String[] getUserInfo() {
        int len = USER_INFO.length;
        String[] userInfo = new String[len];
        for (int i = 0; i < len ; i++) {
            userInfo[i] = getSp().getString(USER_INFO[i]);
        }
        return userInfo;
    }

    public static void setIsLogin(@Nullable boolean token) {
        getSp().edit().put(IS_LOGIN, token);
    }

    public static boolean isLogin() {
        return getSp().getBoolean(IS_LOGIN);
    }

    public static void clear() {
        getSp().edit().clear();
    }
    public static void remove(String key) {
        getSp().edit().remove(key);
    }
    public static boolean isTwiceBackButtonDisabled() {
        return  getSp().getBoolean("back_button");
    }



    public static final String SP_NAME_HISTORY = "history";

    public static final String FACTORY_NAME = "factory_name";
    public static final String FACTORY_CODE = "factory_code";



    public static PrefHelper.PrefGetter history() {
        return getSp(SP_NAME_HISTORY);
    }

    public static void setFactoryNameHistory(ArrayList<String> factory_name) {
        saveHistories( FACTORY_NAME, factory_name);
    }

    public static ArrayList<String> getFactoryNameHistory() {
        return getHistories(FACTORY_NAME);
    }

    public static void setFactoryCodeHistory(ArrayList<String> factory_code) {
        saveHistories( FACTORY_CODE, factory_code);
    }

    public static ArrayList<String> getFactoryCodeHistory() {
        return getHistories(FACTORY_CODE);
    }


    public static Context getContext() {
        return Utils.getContext();
    }

    public static ArrayList<String> getHistories(String key) {
        return PrefHelper.getInstance(SP_NAME_HISTORY).getSerializable(key, new ArrayList<String>());
    }

    public static void saveHistories(String key, ArrayList<String> history) {
        PrefHelper.getInstance(SP_NAME_HISTORY).edit().put(key, history).commit();
    }



    public static void clearHistories(String key) {
        saveHistories(key,new ArrayList<String>());
    }



}
