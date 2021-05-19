package com.zgw.qgb.helper.utils;

import androidx.annotation.IntRange;

import com.afollestad.materialdialogs.internal.MDAdapter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Random;

public class Md5Util {
    /**
     * 获取一个文件的md5值(可处理大文件)
     *
     * @return md5 value
     */

    public static String getFileMD5(String filePath) {
        BigInteger bigInt = null;
        try {
            FileInputStream fis = new FileInputStream(filePath);
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[1024];
            int length = -1;
            while ((length = fis.read(buffer, 0, 1024)) != -1) {
                md.update(buffer, 0, length);
            }
            bigInt = new BigInteger(1, md.digest());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (bigInt != null) {
            String s = bigInt.toString(16);
            if (s.length() != 32) {
                int lack = 32 - s.length();
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < lack; i++) {
                    stringBuilder.append("0");
                }
                s = stringBuilder.toString() + s;
            }
            return s;
        }
        return "";
    }


    public static String generate(String password) {
        return generate(password, 0);
    }

    public static String generate(String password, @IntRange(from = 0, to = 65535) int validDays) {
        return generate(password, validDays, TimeUnit.DAY);
    }

    public static String generate(String password, @IntRange(from = 0, to = 65535) int validTime, TimeUnit timeUnit) {
        // 随机生成盐(16 位),或者当前时间与有效期的盐(16 位)
        String salt = validTime <= 0 ? randomSalt() : timeSalt(validTime, timeUnit);
        // 将盐以及明文分别做 MD5 计算,然后将两者的 MD5值交叉合并
        String passwordWithSaltMD5 = getPasswordAndSaltMD5(password, salt);
        // 将 交叉合并后的值(64 位)作为原文再次计算出一个新的 MD5(32 位)
        password = md5Hex(passwordWithSaltMD5);
        String md5WithSalt = saltMergeToMD5(password, salt);
        return md5WithSalt;
    }

    private static String getPasswordAndSaltMD5(String password, String salt) {
        String passwordMD5 = md5Hex(password);
        String saltMD5 = md5Hex(salt);
        return mergeMD5(passwordMD5, saltMD5);
    }


    public enum TimeUnit {

        YEAR('A', 1000L * 60L * 60L * 24 * 365),
        MONTH('B', 1000L * 60L * 60L * 24 * 30),
        DAY('C', 1000L * 60L * 60L * 24),
        HOUR('D', 1000L * 60L * 60L),
        MINUTE('E', 1000L * 60L),
        SECOND('F', 1000L);


        private char unitCode;
        /**
         * 转换到毫秒的转换率
         */
        private long convertRate;

        TimeUnit(char unitCode, long base) {
            this.unitCode = unitCode;
            this.convertRate = base;
        }

        public static TimeUnit matchTimeUnit(char code) {
            TimeUnit[] timeUnits = TimeUnit.values();
            for (TimeUnit timeUnit : timeUnits) {
                if (code == timeUnit.unitCode) return timeUnit;
            }
            return null;
        }
    }

    /**
     * @param validTime 由于范围限制只能输入 1-65535 之间的数
     * @param timeUnit  字节的限制导致只有有效时间,没有记录开始时间,手机时间设置到有效期内,将仍可以校验通过
     * @return
     */
    private static String timeSalt(int validTime, TimeUnit timeUnit) {
        if (validTime <= 0 || validTime > 0xffff) return randomSalt();
        // 13 位的当前时间(ms 级别) 转为 16 进制将占 11 位,故还有 5 位做其他处理.
        // 其中第一位用 f表示为时间,后四位表示有效时间,可以表示ffff(65535 天),
        // 当然也可以利用上第一位达到 fffff 天的时间范围

        long currentTime = System.currentTimeMillis();
        // System.out.println("currentTime: " + currentTime);
        // System.out.println("TimeUnit: " + timeUnit.name() + " code: " + timeUnit.unitCode);
        // 13位将转成 11 位
        String currentTimeHex = addZeroToStringLeft(BigInteger.valueOf(currentTime).toString(16), 11);
        String validDaysHex = addZeroToStringLeft(BigInteger.valueOf(validTime).toString(16), 4);
        // System.out.println("validDaysHex: " + validDaysHex);
        return timeUnit.unitCode + validDaysHex + currentTimeHex;


        //System.out.println(currentTime);
    }

    /**
     * 生成随机数作为盐
     *
     * @return
     */
    private static String randomSalt() {
        Random r = new Random();
        StringBuilder sb = new StringBuilder(16);
        sb.append(r.nextInt(99999999)).append(r.nextInt(99999999));
        // 16 位的 10 进制数转为 16 进制,最大占 14 位,故必会存在前方补 0 的情况.前两位必定为 0
        String randomHex = new BigInteger(sb.toString(), 10).toString(16);
        return addZeroToStringLeft(randomHex, 16);
    }

    public static String addZeroToStringLeft(String str, int strLength) {
        return addCharToString(str, strLength, true, '0');
    }

    public static String addCharToString(String str, int strLength, boolean isLeft, char charStr) {
        int strLen = str.length();
        StringBuffer sb;
        while (strLen < strLength) {
            sb = new StringBuffer();
            if (isLeft) {
                sb.append(charStr).append(str);// 左补0
            } else {
                sb.append(str).append(charStr);//右补0
            }
            str = sb.toString();
            strLen = str.length();
        }
        return str;
    }

    /**
     * 将 md5 与盐值合成一个新的值,该值可以根据合并方法逆推出 md5 以及盐值
     *
     * @param md5  通过 MD5 算法计算出的 MD5字符串
     * @param salt 此处为 16 位的 16 进制字符串.
     * @return 携带 MD5 以及盐信息的48 位的字符串.
     */
    private static String saltMergeToMD5(String md5, String salt) {
        // System.out.println("MD5: " + md5);
        // System.out.println("salt: " + salt);
        char[] cs = new char[48];
        // MD5 值逢 2 插 1
        for (int i = 0; i < 48; i += 3) {
            cs[i] = md5.charAt(i / 3 * 2);
            cs[i + 1] = md5.charAt(i / 3 * 2 + 1);
            cs[i + 2] = salt.charAt(i / 3);
        }
        return new String(cs);
    }


    /**
     * 将两个 MD5交叉,然后生成一个 64 位的字符串,在基于该字符串生成一个 md5 值
     *
     * @param MD5     原文的 md5
     * @param saltMD5 盐值对应的 md5
     * @return
     */
    private static String mergeMD5(String MD5, String saltMD5) {
        // System.out.println("MD5: " + MD5);
        //  System.out.println("saltMD5: " + saltMD5);
        char[] cs = new char[64];
        for (int i = 0; i < 64; i += 4) {
            cs[i] = MD5.charAt(i / 4 * 2);
            cs[i + 1] = saltMD5.charAt(i / 4 * 2);
            cs[i + 2] = MD5.charAt(i / 4 * 2 + 1);
            cs[i + 3] = saltMD5.charAt(i / 4 * 2 + 1);
        }
        return new String(cs);
    }


    /**
     * 校验加盐后是否和原文一致
     *
     * @param password
     * @param md5
     * @return
     */
    public static boolean verify(String password, String md5) {
        if (md5 == null || md5.isEmpty()) return false;
        if (md5.length() == 32) return md5Hex(password).equals(md5);
        if (md5.length() != 48) return false;

        char[] cs1 = new char[32];
        char[] cs2 = new char[16];
        for (int i = 0; i < 48; i += 3) {
            cs1[i / 3 * 2] = md5.charAt(i);
            cs1[i / 3 * 2 + 1] = md5.charAt(i + 1);
            cs2[i / 3] = md5.charAt(i + 2);
        }
        String salt = new String(cs2);
        //  System.out.println("salt form code: " + salt);
        //  System.out.println("md5 form code: " + new String(cs1));
        boolean inValidTime = verifyTime(salt);

        return inValidTime && md5Hex(getPasswordAndSaltMD5(password, salt)).equals(new String(cs1));
    }

    /**
     * 判断是否在有效期内,盐值可能是生成编码时的时间与有效期组成的,所以需要判断一下有效时间
     *
     * @param salt
     * @return
     */
    private static boolean verifyTime(String salt) {
        salt = salt.toUpperCase(Locale.CHINA);
        if (salt.startsWith("00")) {
            return true;
        }
        // 若是第一位是a-f,则从第二位开始截取,否则直接从第一位开始截取
        TimeUnit timeUnit = TimeUnit.matchTimeUnit(salt.charAt(0));
        // 若是第一位是 A-F 表示为时间
        BigInteger validTime = new BigInteger(salt.substring(1, 5), 16);
        BigInteger validMillSecond = validTime.multiply(BigInteger.valueOf(timeUnit.convertRate));
        BigInteger startTime = new BigInteger(salt.substring(5, 16), 16);
        BigInteger endTime = startTime.add(validMillSecond);
        BigInteger currentTime = BigInteger.valueOf(System.currentTimeMillis());
        // 截止时间大于等于当前时间
        return endTime.compareTo(currentTime) >= 0;

    }

    /**
     * 获取十六进制字符串形式的MD5摘要
     */

    public final static String md5Hex(String s) {
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F'};
        try {
            byte[] btInput = s.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }


    public static void main(String[] args) {
        // 原文
        String plaintext = "我是明文";


        String code = generate(plaintext);
        String code_minute = generate(plaintext, 1, TimeUnit.MINUTE);
        String code_second = generate(plaintext, 1, TimeUnit.SECOND);

        System.out.println(code);
        System.out.println(code_minute);
        System.out.println(code_second);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(verify(plaintext, code));
        System.out.println(verify(plaintext, code_minute));
        System.out.println(verify(plaintext, code_second));


    }
}