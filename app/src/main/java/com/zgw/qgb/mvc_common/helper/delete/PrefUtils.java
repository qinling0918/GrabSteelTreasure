package com.zgw.qgb.mvc_common.helper.delete;

import android.accounts.Account;
import android.support.annotation.Nullable;

import static com.zgw.qgb.mvc_common.helper.delete.PrefHelper.Editor.APPLY;


/**
 * created by tsinling on: 2018/4/29 08:42
 * description:
 */
public class PrefUtils {

    private static String accountSt = "";
    private static final String TOKEN = "token";
    private static final String[] USER_INFO = new String[]{"name","type"};
    private static final String IS_LOGIN = "is_login";


    public static void init(String account) {
        accountSt = account ;
    }
    private static PrefHelper getSp() {
        return PrefHelper.getInstance(accountSt);
    }

    public static void setToken(@Nullable String token) {
        getSp().edit(APPLY).put(TOKEN, token);
    }

    public static String getToken() {
        return getSp().getString(TOKEN);
    }

    public static void setUserInfo(@Nullable Account token) {
        getSp().edit(APPLY)
                .put(USER_INFO[0], token.name)
                .put(USER_INFO[1],token.type)
                .put(USER_INFO[0], token.name+token.type);
    }

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







}
