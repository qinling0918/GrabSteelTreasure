package com.zgw.qgb.mvc_common.helper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by Administrator on 2018/3/14.
 */

public final class InputHelper {
    private InputHelper() {
        // No instances.
    }
    public static final String SPACE = "\u202F\u202F";

    private static boolean isWhiteSpaces(@Nullable String s) {
        return s != null && s.matches("\\s+");
    }

    public static boolean isEmpty(@Nullable String text) {
        return text == null || TextUtils.isEmpty(text) || isWhiteSpaces(text) || text.equalsIgnoreCase("null");
    }

    public static boolean isEmpty(@Nullable Object text) {
        return text == null || isEmpty(text.toString());
    }

    public static boolean isEmpty(@Nullable EditText text) {
        return text == null || isEmpty(text.getText().toString());
    }

    public static boolean isEmpty(@Nullable TextView text) {
        return text == null || isEmpty(text.getText().toString());
    }

    public static boolean isEmpty(@Nullable TextInputLayout txt) {
        return txt == null || isEmpty(txt.getEditText());
    }

    public static String toString(@NonNull EditText editText) {
        return editText.getText().toString();
    }

    public static String toString(@NonNull TextView editText) {
        return editText.getText().toString();
    }

    public static String toString(@NonNull TextInputLayout textInputLayout) {
        return textInputLayout.getEditText() != null ? toString(textInputLayout.getEditText()) : "";
    }

    @NonNull
    public static String toNA(@Nullable String value) {
        return valueOrDefault(value,"N/A");
    }

    public static String valueOrDefault(String string, String defaultString) {
        return isWhiteSpaces(string) ? defaultString : string;
    }

    /**
     * 若字符串
     * @param string
     * @param defaultString
     * @return
     */
    public static String emptyOrDefault(String string, String defaultString) {
        return isEmpty(string) ? defaultString : string;
    }
    @NonNull
    public static String toString(@Nullable Object object) {
        return !isEmpty(object) ? object.toString() : "";
    }

    public static long toLong(@NonNull TextView textView) {
        return toLong(toString(textView));
    }

    public static long toLong(@NonNull String text) {
        if (!isEmpty(text)) {
            try {
                return Long.valueOf(text.replace(".", "").replaceAll(",", ""));
            } catch (NumberFormatException ignored) {}
        }
        return 0;
    }


    public static int getSafeIntId(long id) {
        return id > Integer.MAX_VALUE ? (int) (id - Integer.MAX_VALUE) : (int) id;
    }

    /**
     * 将字符串首字母变成大写
     * @param s
     * @return
     */
    public static String upperFirstLetter(String s) {
        if (isEmpty(s)) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }


    /**
     * 转化为半角字符
     *
     * @param s 待转字符串
     * @return 半角字符串
     */
    public static String toDBC(final String s) {
        if (isEmpty(s)) return s;
        char[] chars = s.toCharArray();
        for (int i = 0, len = chars.length; i < len; i++) {
            if (chars[i] == 12288) {
                chars[i] = ' ';
            } else if (65281 <= chars[i] && chars[i] <= 65374) {
                chars[i] = (char) (chars[i] - 65248);
            } else {
                chars[i] = chars[i];
            }
        }
        return new String(chars);
    }

    /**
     * 转化为全角字符
     *
     * @param s 待转字符串
     * @return 全角字符串
     */
    public static String toSBC(final String s) {
        if (isEmpty(s)) return s;
        char[] chars = s.toCharArray();
        for (int i = 0, len = chars.length; i < len; i++) {
            if (chars[i] == ' ') {
                chars[i] = (char) 12288;
            } else if (33 <= chars[i] && chars[i] <= 126) {
                chars[i] = (char) (chars[i] + 65248);
            } else {
                chars[i] = chars[i];
            }
        }
        return new String(chars);
    }

    /**
     * 返回字符串长度
     *
     * @param s 字符串
     * @return null 返回 0，其他返回自身长度
     */
    public static int length(final CharSequence s) {
        return s == null ? 0 : s.length();
    }
}