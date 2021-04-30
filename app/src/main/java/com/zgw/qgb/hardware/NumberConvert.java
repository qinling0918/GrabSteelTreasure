package com.zgw.qgb.hardware;


import androidx.annotation.NonNull;
import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by qinling on 2018/5/5 14:58
 * Description: 数字进制转换，以及不同进制数字字符串的处理
 */
public final class NumberConvert {
    public static final String REGEX_HEX = "^[A-Fa-f0-9]+$";
    public static final String REGEX_BINARY = "^[0-1]+$";
    public static final String REGEX_OCTAL = "^[0-7]+$";
    public static final String REGEX_DECIMAL = "^[0-9]+$";

    @StringDef({yyyyMMddHHmmss, yyyyMMdd, HHmmss})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TimeFormater {
    }

    public static final String yyyyMMddHHmmss = "yyyyMMddHHmmss";
    public static final String yyyyMMdd = "yyyyMMdd";
    public static final String HHmmss = "HHmmss";


    @StringDef({HEX, BINARY, OCTAL, DECIMAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Radix {
    }

    public static final String HEX = "x";
    public static final String BINARY = "b";
    public static final String OCTAL = "o";
    public static final String DECIMAL = "d";

    /**
     * 是否是 16进制字符串
     *
     * @param hexStr 16进制字符串
     * @return boolean
     */
    public static boolean isHexStr(String hexStr) {
        hexStr = checkMinus(hexStr);
        return hexStr.matches(REGEX_HEX);
    }
    public static boolean isDecimalStr(String decimalStr) {
        decimalStr = checkMinus(decimalStr);
        return decimalStr.matches(REGEX_DECIMAL);
    }
    /**
     * 是否是 16进制字符串 不含有 符号
     *
     * @param hexStr 字符串
     * @return 是否是 16进制字符串
     */
    public static boolean isHexUnsignedStr(String hexStr) {
        return hexStr.matches(REGEX_HEX);
    }

    /**
     * 是否是 2进制字符串
     *
     * @param binaryStr 二进制字符串
     * @return boolean true 是， false 否
     */
    public static boolean isBinaryUnsignedStr(String binaryStr) {
        return binaryStr.matches(REGEX_BINARY);
    }

    public static boolean isBinaryStr(String binaryStr) {
        binaryStr = checkMinus(binaryStr);
        return binaryStr.matches(REGEX_BINARY);
    }

    /**
     * 是否是 8进制字符串
     *
     * @param octalStr 8进制字符串
     * @return true 是，false 否
     */
    public static boolean isOctalStr(String octalStr) {
        octalStr = checkMinus(octalStr);
        return octalStr.matches(REGEX_OCTAL);
    }

    /**
     * 检查是不是带有"-"号的负数
     *
     * @param hexStr 16进制字符串
     * @return 若带有"-"号,则截取后面的字符串加以判断
     */
    @NonNull
    private static String checkMinus(String hexStr) {
        hexStr = hexStr.trim();
        char firstChar = hexStr.charAt(0);
        if (firstChar == '-') {
            hexStr = hexStr.substring(1);
        }
        return hexStr;
    }
    /*private static void formatNum(int a, int shift,@Radix String radix) {
        StringBuilder builder = new StringBuilder("%0");
        builder.append(shift).append(radix).toString();
       // System.out.println(String.format(builder.toString(), a));
    }*/

    public static Integer parseInt(String codeStr) {
        return parseInt(codeStr, 10, null);
    }

    public static Integer parseInt(String codeStr, Integer defaultValue) {
        return parseInt(codeStr, 10, defaultValue);
    }

    public static Integer parseInt(String codeStr, int radix, Integer defaultValue) {
        try {
            return Integer.parseInt(codeStr, radix);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static int parseUnsignedInt(String s, int radix)
            throws NumberFormatException {
        if (s == null) {
            throw new NumberFormatException("null");
        }

        int len = s.length();
        if (len > 0) {
            char firstChar = s.charAt(0);
            if (firstChar == '-') {
                throw new
                        NumberFormatException(String.format("Illegal leading minus sign " +
                        "on unsigned string %s.", s));
            } else {
                if (len <= 5 || // Integer.MAX_VALUE in Character.MAX_RADIX is 6 digits
                        (radix == 10 && len <= 9)) { // Integer.MAX_VALUE in base 10 is 10 digits
                    return parseInt(s, radix);
                } else {
                    long ell = Long.parseLong(s, radix);
                    if ((ell & 0xffff_ffff_0000_0000L) == 0) {
                        return (int) ell;
                    } else {
                        throw new
                                NumberFormatException(String.format("String value %s exceeds " +
                                "range of unsigned int.", s));
                    }
                }
            }
        } else {
            throw new NumberFormatException("For input string: \"" + s + "\"");
        }
    }


    public static long parseUnsignedLong(String s, int radix)
            throws NumberFormatException {
        if (s == null) {
            throw new NumberFormatException("null");
        }

        int len = s.length();
        if (len > 0) {
            char firstChar = s.charAt(0);
            if (firstChar == '-') {
                throw new
                        NumberFormatException(String.format("Illegal leading minus sign " +
                        "on unsigned string %s.", s));
            } else {
                if (len <= 12 || // Long.MAX_VALUE in Character.MAX_RADIX is 13 digits
                        (radix == 10 && len <= 18)) { // Long.MAX_VALUE in base 10 is 19 digits
                    return Long.parseLong(s, radix);
                }

                // No need for range checks on len due to testing above.
                long first = Long.parseLong(s.substring(0, len - 1), radix);
                int second = Character.digit(s.charAt(len - 1), radix);
                if (second < 0) {
                    throw new NumberFormatException("Bad digit at end of " + s);
                }
                long result = first * radix + second;
                if (compareUnsigned(result, first) < 0) {

                    throw new NumberFormatException(String.format("String value %s exceeds " +
                            "range of unsigned long.", s));
                }
                return result;
            }
        } else {
            throw new NumberFormatException("For input string: \"" + s + "\"");
        }

    }

    public static int compare(long x, long y) {
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    /**
     * Compares two {@code long} values numerically treating the values
     * as unsigned.
     *
     * @param x the first {@code long} to compare
     * @param y the second {@code long} to compare
     * @return the value {@code 0} if {@code x == y}; a value less
     * than {@code 0} if {@code x < y} as unsigned values; and
     * a value greater than {@code 0} if {@code x > y} as
     * unsigned values
     * @since 1.8
     */
    public static int compareUnsigned(long x, long y) {
        return compare(x + Long.MIN_VALUE, y + Long.MIN_VALUE);
    }

    /**
     * 将十六进制的字符串转换成二进制的字符串
     *
     * @param hexString 十六进制的字符串
     * @return String 二进制的字符串
     */
    public static String hexStrToBinaryStr(String hexString) {
        //Frame698m.Parser parser = new Frame698m.Parser(hexString);


        if (hexString == null || hexString.equals("")) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        // 将每一个十六进制字符分别转换成一个四位的二进制字符
        for (int i = 0; i < hexString.length(); i++) {
            String indexStr = hexString.substring(i, i + 1);
            String binaryStr = Integer.toBinaryString(Integer.parseInt(indexStr, 16));
            while (binaryStr.length() < 4) {
                binaryStr = "0" + binaryStr;
            }
            sb.append(binaryStr);
        }

        return sb.toString();
    }

    /**
     * 例如  “11111”---> 00011111 --->“1f”
     * 例如  “111111111”---> 00000001 11111111 --->“01ff”
     * 不够的话将在前面补0
     * 二进制字符串转换为十六进制字符串, 一个字节的16进制字符串
     *
     * @param binaryStr 二进制字符串
     * @return 十六进制字符串
     */
    public static String binaryStrToHexStrWithFormat(String binaryStr) {
        StringBuilder stringBuffer = new StringBuilder();
        int len = binaryStr.length();
        int end = len % 8;
        for (int i = 0; i < 8 - end; i++) {
            stringBuffer.append(0);
        }
        stringBuffer.append(binaryStr);
        return binaryStrToHexStr(stringBuffer.toString());
    }

    /**
     * 二进制字符串转换为十六进制字符串, 不保证是一个字节的16进制字符串
     * <p>
     * 二进制字符串位数必须满足是4的倍数
     *
     * @param binaryStr 二进制字符串
     * @return 十六进制字符串
     */
    public static String binaryStrToHexStr(String binaryStr) {

        if (binaryStr == null || binaryStr.equals("") || binaryStr.length() % 4 != 0) {
            return null;
        }

        StringBuilder sbs = new StringBuilder();
        // 二进制字符串是4的倍数，所以四位二进制转换成一位十六进制
        for (int i = 0; i < binaryStr.length() / 4; i++) {
            String subStr = binaryStr.substring(i * 4, i * 4 + 4);
            String hexStr = Integer.toHexString(Integer.parseInt(subStr, 2));
            sbs.append(hexStr);
        }

        return sbs.toString().toUpperCase(Locale.CHINA);
    }


    public static String toBinary(int num, int digits) {
        String cover = Integer.toBinaryString(1 << digits).substring(1);
        String s = Integer.toBinaryString(num);
        return s.length() < digits ? cover.substring(s.length()) + s : s;
    }

    /***
     *  10进制 转成2进制字符串  并且有位数,不够则补零
     * @param num  10进制数字
     * @param strLength 字符串长度
     * @return 2进制字符串
     */
    public static String toBinaryStr(int num, int strLength) {
        String str = Integer.toBinaryString(num);
        return addZeroToStringLeft(str, strLength);

        /* String formater = "%0"+String.format(Locale.CHINA,"%d",strLength)+"d";
        String abcBinary = Integer.toBinaryString(1);
        int abcDec = Integer.parseInt(abcBinary);
        return String.format(Locale.CHINA,formater,abcDec);*/
    }


    /***
     *  10进制 转成16进制字符串  并且有位数,不够则补零
     * @param num 10进制数字
     * @param strLength  字符串长度
     * @return 16进制字符串
     */
    public static String toHexStr(int num, int strLength) {
        String str = Integer.toHexString(num).toUpperCase(Locale.CHINA);
        if (num < 0) {
            int len = str.length();
            if (len < strLength) {
                return addCharToString(str, strLength, true, 'F');
            } else {
                return str.substring(len - strLength, len);
            }
        }
        return addZeroToStringLeft(str, strLength);

       /* String formater = "%0"+String.format(Locale.CHINA,"%d",strLength)+"x";
        return String.format(Locale.CHINA,formater,num);*/
    }

    /**
     * -----ASCII码转换为16进制 -----
     *
     * @param str ASCII 字符串
     * @return String 16进制字符串
     * <p>
     * 字符串: efff
     * 转换为16进制 : 65666666
     */
    public static String asciiStringToHex(String str) {

        char[] chars = str.toCharArray();
        StringBuilder hex = new StringBuilder();
        for (char aChar : chars) {
            hex.append(Integer.toHexString((int) aChar));
        }

        return hex.toString();
    }

    /**
     * 16进制转换为ASCII
     *
     * @param hex 16进制字符串
     * @return ASCII码字符串
     */
    public static String hexStrToAsciiString(String hex) {

        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();

        //49204c6f7665204a617661 split into two characters 49, 20, 4c...
        for (int i = 0; i < hex.length() - 1; i += 2) {

            //grab the hex in pairs
            String output = hex.substring(i, (i + 2));
            //convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            //convert the decimal to character
            sb.append((char) decimal);
            temp.append(decimal);
        }

        return sb.toString();
    }

    /**
     * @param time     时间戳
     * @param formater 时间格式
     * @return 格式后的字符串
     */
    public static String getTimeHexStr(long time, @TimeFormater String formater) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return getTimeHexString(formater, calendar);

    }

    public static String getTimeHexStr(Date date, @TimeFormater String formater) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return getTimeHexString(formater, calendar);

    }

    @NonNull
    private static String getTimeHexString(@TimeFormater String formater, Calendar calendar) {
        String yearHexStr = toHexStr(calendar.get(Calendar.YEAR), 4);
        String monthHexStr = toHexStr(calendar.get(Calendar.MONTH) + 1, 2);
        String dayOfMonthHexStr = toHexStr(calendar.get(Calendar.DAY_OF_MONTH), 2);
        String hourOfDayHexStr = toHexStr(calendar.get(Calendar.HOUR_OF_DAY), 2);
        String minuteHexStr = toHexStr(calendar.get(Calendar.MINUTE), 2);
        String secondHexStr = toHexStr(calendar.get(Calendar.SECOND), 2);
        //String milliSecondHexStr = toHexStr(calendar.get(Calendar.MILLISECOND), 4);
        String timeHexStr = String.format(Locale.CHINA, "%s%s%s%s%s%s",
                yearHexStr, monthHexStr, dayOfMonthHexStr, hourOfDayHexStr, minuteHexStr, secondHexStr);
        int len = timeHexStr.length();
        switch (formater) {
            case yyyyMMddHHmmss:
                return timeHexStr;
            case yyyyMMdd:
                return timeHexStr.substring(0, 8);
            case HHmmss:
                return timeHexStr.substring(8, len);
            default:
                return "";
        }
    }

    /**
     * 字符串左边补零
     *
     * @param str       2进制,8进制 10进制,16进制 的字符串
     * @param strLength 期望字符串长度.
     * @return String 补0后的字符串
     */
    public static String addZeroToStringLeft(String str, int strLength) {
        return addZeroToString(str, strLength, true);
    }

    /**
     * @param str       原字符串
     * @param strLength 期望结果字符串的长度
     * @param isLeft    0是不是补在左侧
     * @return 补0后的字符串
     */
    public static String addZeroToString(String str, int strLength, boolean isLeft) {
        return addCharToString(str, strLength, isLeft, '0');
    }

    /**
     * @param str       原字符串
     * @param strLength 期望结果字符串的长度
     * @param isLeft    0是不是补在左侧
     * @param charStr   用来补位的字符
     * @return String
     */
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
     * HEX字符串依照byte数组方式倒序
     *
     * @param source HEX字符串
     * @return String
     */
    public static String hexStrReverse(String source) {
        return hexStrReverse(source, 0, source.length());
    }

    /**
     * HEX字符串依照byte数组方式部分倒序
     *
     * @param source 输入的原字符串
     * @param start  倒序开始位置
     * @param end    倒序结束位置
     * @return 倒序后的字符串
     */
    public static String hexStrReverse(String source, int start, int end) {
        if (start > end || source == null || source.length() % 2 == 1 || end > source.length() ||
                start % 2 == 1 || end % 2 == 1) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(source, 0, start);
        for (int i = end; i > start; i -= 2) {
            sb.append(source, i - 2, i);
        }
        sb.append(source.substring(end));

        return sb.toString();
    }

    /**
     * Java和一些windows编程语言如c、c++、delphi所写的网络程序进行通讯时，需要进行相应的转换
     * 高、低字节之间的转换
     * windows的字节序为低字节开头
     * linux,unix的字节序为高字节开头
     * java则无论平台变化，都是高字节开头
     * <p>
     * 将byte数组中的元素倒序排列
     */
    public static byte[] bytesReverse(byte[] b) {
        int length = b.length;
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) {
            result[length - i - 1] = b[i];
        }
        return result;
    }


    /**
     * 获取FCS 和 HCS
     *
     * @param hcsStr 用来计算hcs的16进制字符串
     * @return 2字节校验码
     */
    public static String getFcsOrHcs(String hcsStr) {

        hcsStr = hcsStr.trim().replaceAll(" ", "");
        byte[] bytes = hexStringToBytes(hcsStr + "0000");
        int length = bytes.length - 2;

        return CrcUtil.tryfcs16(bytes, length);
    }

    /**
     * 获取一段字符串的和校验值
     *
     * @param value 待计算字符串
     * @return 校验字符串-成功 null-失败
     */
    public static String getCs(String value) {

        if (value == null) {
            return null;
        }
        value = value.trim();
        if (value.length() == 0 || value.length() % 2 == 1) {
            return null;
        }
        int checkValue = 0;
        /*
        每两个字符当作一个byte值进行加法计算
        如果解析错误，则看作计算校验值错误
         */
        for (int i = 0; i < value.length() - 1; i += 2) {
            try {
                checkValue += Integer.parseInt(value.substring(i, i + 2), 16);
            } catch (Exception e) {
                return null;
            }
        }
        return toHexString(checkValue, 1);
    }
   /* public static byte[] toBytes(String hcsStr) {
        return DataConvert.toBytes(hcsStr);
    }*/

    /**
     * 将整型值转化为HEX字符串
     *
     * @param value       待转化整型值
     * @param bytesLength HEX数据长度，只能为1或2或4
     * @return 转化后HEX字符串-正确 null-错误
     */
    private static String toHexString(int value, int bytesLength) {
        if (bytesLength != 1 && bytesLength != 2 && bytesLength != 4) {
            return null;
        }
        int mask = 0;
        switch (bytesLength) {
            case 1:
                mask = 0x000000FF;
                break;
            case 2:
                mask = 0x0000FFFF;
                break;
            case 4:
                mask = 0xFFFFFFFF;
                break;
        }

        int length = 2 * bytesLength;

        String ret = Integer.toHexString(value & mask);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < (length - ret.length()); i++) {
            sb.append("0");
        }
        sb.append(ret);
        return sb.toString().toUpperCase();
    }

    /**
     * 把16进制字符串转换成字节数组
     *
     * @param hexString 16进制字符串
     * @return byte[]
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null) return null;
        hexString = hexString.trim().toUpperCase(Locale.CHINA);
        int len = (hexString.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hexString.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }

    /**
     * 注意使用时需要将字符串转为大写
     *
     * @param c 字符
     * @return int
     */
    private static int toByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     * 将字节数组转换为String (ASCII码)
     *
     * @param b byte[]
     * @return String
     * <p>
     * [48, 49, 97, 98] ---> 01ab
     * [37, 94, 38, 42] ---> %^&*
     */
    public static String bytesToString(byte[] b) {
        StringBuilder result = new StringBuilder();
        int length = b.length;
        for (byte aB : b) {
            result.append((char) (aB & 0xff));
        }
        return result.toString();
    }

    /**
     * /**
     * 数组转换成十六进制字符串
     *
     * @param src 字节数组
     * @return HexString
     */
    public static String bytesToHexString(byte[] src) {
        return src == null ? "" : bytesToHexString(src, 0, src.length);
    }

    /**
     * 将byte数组从指定索引位置fromindex截取len长度的数组转为字符串
     *
     * @param bytes     原始数组
     * @param fromIndex 索引位置
     * @param len       截取长度( 字节数)
     * @return String
     */
    public static String bytesToHexString(byte[] bytes, int fromIndex, int len) {
        StringBuilder stringBuilder = new StringBuilder();
        if (bytes == null || len <= 0) {
            return "";
        }
        for (int i = fromIndex; i < fromIndex + len; i++) {
            int v = bytes[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString().toUpperCase(Locale.CHINA);
    }

    public static int byteToInt(byte[] b, int offset, int len) {

        int mask = 0xff;
        int temp;
        int n = 0;
        for (int i = offset + len - 1; i >= offset; i--) {
            n <<= 8;
            temp = b[i] & mask;
            n |= temp;
        }
        return n;
    }

    // TODO : 此处用了 Object[] , 因为使用了基于协议中的数据类型外，又自行组合了一些数据类型
   /* public static String toString4Array(IData[] objs) {
        StringBuffer stringBuffer = new StringBuffer();
        if (objs != null && objs.length != 0) {
            int len = objs.length;
            stringBuffer.append(NumberConvert.toHexStr(len, 2));
            for (int i = 0; i < len; i++) {
                stringBuffer.append(objs[i].toString());
            }
        }
        return stringBuffer.toString().toUpperCase(Locale.CHINA);
    }*/

    public static String bytesToBinaryString(byte[] src) {
        return src == null ? "" : bytesToHexString(src, 0, src.length);
    }

    /**
     * 将byte数组从指定索引位置fromindex截取len长度的数组转为16进制字符串
     *
     * @param bytes     原始数组
     * @param fromIndex 索引位置
     * @param len       截取长度
     * @return String 16进制字符串
     */
    public static String bytesToBinaryString(byte[] bytes, int fromIndex, int len) {
        StringBuilder stringBuilder = new StringBuilder();
        if (bytes == null || len <= 0) {
            return "";
        }
        for (int i = fromIndex; i < fromIndex + len; i++) {
            stringBuilder.append(toBinaryStr(bytes[i] & 0xff, 8));
        }
        return stringBuilder.toString().toUpperCase(Locale.CHINA);
    }


    public static final class Crc16 {
        public static int calcCrc16(byte[] data)
        {
            return calcCrc16(data, 0, data.length);
        }
        public static int calcCrc16(byte[] data, int offset, int len)
        {
            return calCRC16(data, offset, len, 0xffff);
        }
        public static int calCRC16(byte[] data, int offset, int len, int preval)
        {
            int unCRCHi = (preval& 0xff00) >> 8;
            int ucCRCLo = preval& 0x00ff;
            int iIndex;
            for (int i = 0; i<len; i++)
            {
                iIndex = (ucCRCLo ^ data[offset + i]) & 0x00ff;
                ucCRCLo = unCRCHi ^ crc16_tab_h[iIndex];
                unCRCHi = crc16_tab_l[iIndex];
            }
            return ((unCRCHi& 0x00ff) << 8) | (ucCRCLo& 0x00ff) & 0xffff;
        }

        static byte[] crc16_tab_h = {
                (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41,
                (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40,
                (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40,
                (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41,
                (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40,
                (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41,
                (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41,
                (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40,
                (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40,
                (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41,
                (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41,
                (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40,
                (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41,
                (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40,
                (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40,
                (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41,
                (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40,
                (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41,
                (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41,
                (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40,
                (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41,
                (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40,
                (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40,
                (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41,
                (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41,
                (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40,
                (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40,
                (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41,
                (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40,
                (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41,
                (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41,
                (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40 };
        static byte[] crc16_tab_l = {
                (byte) 0x00, (byte) 0xC0, (byte) 0xC1, (byte) 0x01, (byte) 0xC3, (byte) 0x03, (byte) 0x02, (byte) 0xC2, (byte) 0xC6, (byte) 0x06, (byte) 0x07, (byte) 0xC7,
                (byte) 0x05, (byte) 0xC5, (byte) 0xC4, (byte) 0x04, (byte) 0xCC, (byte) 0x0C, (byte) 0x0D, (byte) 0xCD, (byte) 0x0F, (byte) 0xCF, (byte) 0xCE, (byte) 0x0E,
                (byte) 0x0A, (byte) 0xCA, (byte) 0xCB, (byte) 0x0B, (byte) 0xC9, (byte) 0x09, (byte) 0x08, (byte) 0xC8, (byte) 0xD8, (byte) 0x18, (byte) 0x19, (byte) 0xD9,
                (byte) 0x1B, (byte) 0xDB, (byte) 0xDA, (byte) 0x1A, (byte) 0x1E, (byte) 0xDE, (byte) 0xDF, (byte) 0x1F, (byte) 0xDD, (byte) 0x1D, (byte) 0x1C, (byte) 0xDC,
                (byte) 0x14, (byte) 0xD4, (byte) 0xD5, (byte) 0x15, (byte) 0xD7, (byte) 0x17, (byte) 0x16, (byte) 0xD6, (byte) 0xD2, (byte) 0x12, (byte) 0x13, (byte) 0xD3,
                (byte) 0x11, (byte) 0xD1, (byte) 0xD0, (byte) 0x10, (byte) 0xF0, (byte) 0x30, (byte) 0x31, (byte) 0xF1, (byte) 0x33, (byte) 0xF3, (byte) 0xF2, (byte) 0x32,
                (byte) 0x36, (byte) 0xF6, (byte) 0xF7, (byte) 0x37, (byte) 0xF5, (byte) 0x35, (byte) 0x34, (byte) 0xF4, (byte) 0x3C, (byte) 0xFC, (byte) 0xFD, (byte) 0x3D,
                (byte) 0xFF, (byte) 0x3F, (byte) 0x3E, (byte) 0xFE, (byte) 0xFA, (byte) 0x3A, (byte) 0x3B, (byte) 0xFB, (byte) 0x39, (byte) 0xF9, (byte) 0xF8, (byte) 0x38,
                (byte) 0x28, (byte) 0xE8, (byte) 0xE9, (byte) 0x29, (byte) 0xEB, (byte) 0x2B, (byte) 0x2A, (byte) 0xEA, (byte) 0xEE, (byte) 0x2E, (byte) 0x2F, (byte) 0xEF,
                (byte) 0x2D, (byte) 0xED, (byte) 0xEC, (byte) 0x2C, (byte) 0xE4, (byte) 0x24, (byte) 0x25, (byte) 0xE5, (byte) 0x27, (byte) 0xE7, (byte) 0xE6, (byte) 0x26,
                (byte) 0x22, (byte) 0xE2, (byte) 0xE3, (byte) 0x23, (byte) 0xE1, (byte) 0x21, (byte) 0x20, (byte) 0xE0, (byte) 0xA0, (byte) 0x60, (byte) 0x61, (byte) 0xA1,
                (byte) 0x63, (byte) 0xA3, (byte) 0xA2, (byte) 0x62, (byte) 0x66, (byte) 0xA6, (byte) 0xA7, (byte) 0x67, (byte) 0xA5, (byte) 0x65, (byte) 0x64, (byte) 0xA4,
                (byte) 0x6C, (byte) 0xAC, (byte) 0xAD, (byte) 0x6D, (byte) 0xAF, (byte) 0x6F, (byte) 0x6E, (byte) 0xAE, (byte) 0xAA, (byte) 0x6A, (byte) 0x6B, (byte) 0xAB,
                (byte) 0x69, (byte) 0xA9, (byte) 0xA8, (byte) 0x68, (byte) 0x78, (byte) 0xB8, (byte) 0xB9, (byte) 0x79, (byte) 0xBB, (byte) 0x7B, (byte) 0x7A, (byte) 0xBA,
                (byte) 0xBE, (byte) 0x7E, (byte) 0x7F, (byte) 0xBF, (byte) 0x7D, (byte) 0xBD, (byte) 0xBC, (byte) 0x7C, (byte) 0xB4, (byte) 0x74, (byte) 0x75, (byte) 0xB5,
                (byte) 0x77, (byte) 0xB7, (byte) 0xB6, (byte) 0x76, (byte) 0x72, (byte) 0xB2, (byte) 0xB3, (byte) 0x73, (byte) 0xB1, (byte) 0x71, (byte) 0x70, (byte) 0xB0,
                (byte) 0x50, (byte) 0x90, (byte) 0x91, (byte) 0x51, (byte) 0x93, (byte) 0x53, (byte) 0x52, (byte) 0x92, (byte) 0x96, (byte) 0x56, (byte) 0x57, (byte) 0x97,
                (byte) 0x55, (byte) 0x95, (byte) 0x94, (byte) 0x54, (byte) 0x9C, (byte) 0x5C, (byte) 0x5D, (byte) 0x9D, (byte) 0x5F, (byte) 0x9F, (byte) 0x9E, (byte) 0x5E,
                (byte) 0x5A, (byte) 0x9A, (byte) 0x9B, (byte) 0x5B, (byte) 0x99, (byte) 0x59, (byte) 0x58, (byte) 0x98, (byte) 0x88, (byte) 0x48, (byte) 0x49, (byte) 0x89,
                (byte) 0x4B, (byte) 0x8B, (byte) 0x8A, (byte) 0x4A, (byte) 0x4E, (byte) 0x8E, (byte) 0x8F, (byte) 0x4F, (byte) 0x8D, (byte) 0x4D, (byte) 0x4C, (byte) 0x8C,
                (byte) 0x44, (byte) 0x84, (byte) 0x85, (byte) 0x45, (byte) 0x87, (byte) 0x47, (byte) 0x46, (byte) 0x86, (byte) 0x82, (byte) 0x42, (byte) 0x43, (byte) 0x83,
                (byte) 0x41, (byte) 0x81, (byte) 0x80, (byte) 0x40 };

    }
}


