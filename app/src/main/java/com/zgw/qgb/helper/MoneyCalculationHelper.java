package com.zgw.qgb.helper;

import androidx.annotation.NonNull;
import android.text.TextUtils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/***
 *
 * 金额
 *
 * 如果需要精确计算，必须用String来够造BigDecimal！ ！！
 *
 * Java里面的商业计算，不能用float和double，因为他们无法 进行精确计算。
 * 但是Java的设计者给编程人员提供了一个很有用的类BigDecimal，
 * 他可以完善float和double类无法进行精确计算的缺憾。
 * BigDecimal类位于java.maths类包下。
 * 它的构造函数很多，最常用的:
 * BigDecimal(double val)
 * BigDecimal(String str)
 * BigDecimal(BigInteger val)
 * BigDecimal(BigInteger unscaledVal, int scale)
 *
 *
 *
 ROUND_UP： 远离零方向舍入。
 向远离0的方向舍入，也就是说，向绝对值最大的方向舍入，只要舍弃位非0即进位。

 ROUND_DOWN：趋向零方向舍入。向0方向靠拢，也就是说，向绝对值最小的方向输入，注意：所有的位都舍弃，不存在进位情况。

 ROUND_CEILING：向正无穷方向舍入。

 向正最大方向靠拢，如果是正数，舍入行为类似于ROUND_UP；如果为负数，则舍入行为类似于ROUND_DOWN。注意：Math.round方法使用的即为此模式。

 ROUND_FLOOR：向负无穷方向舍入。

 向负无穷方向靠拢，如果是正数，则舍入行为类似于 ROUND_DOWN；如果是负数，则舍入行为类似于 ROUND_UP。

 HALF_UP： 最近数字舍入（5进）。

 这就是我们最最经典的四舍五入模式。

 HALF_DOWN：最近数字舍入（5舍）。

 在四舍五入中，5是进位的，而在HALF_DOWN中却是舍弃不进位。

 HALF_EVEN ：银行家算法。
 *
 * @author wxw
 *
 */
public class MoneyCalculationHelper {

    //默认除法运算精度
    private static final int DEFAULT_DIV_SCALE = 10;



    /**
     * 提供精确的加法运算。
     * @param v1
     * @param v2
     * @return 两个参数的和
     */

    public static double add(double v1, double v2){

        BigDecimal b1 = double2bigDecimal(v1);
        BigDecimal b2 = double2bigDecimal(v2);

        return b1.add(b2).doubleValue();

    }

    @NonNull
    private static BigDecimal double2bigDecimal(double v1) {
        return new BigDecimal(Double.toString(v1));
    }

    /**

     * 提供精确的加法运算

     * @param v1

     * @param v2

     * @return 两个参数数学加和，以字符串格式返回

     */

    public static String add(String v1, String v2) {

        BigDecimal b1 = string2BigDecimal(v1);

        BigDecimal b2 = string2BigDecimal(v2);

        return b1.add(b2).toString();

    }



    /**

     * 提供精确的减法运算。

     * @param v1

     * @param v2

     * @return 两个参数的差

     */

    public static double subtract(double v1, double v2)

    {

        BigDecimal b1 = double2bigDecimal(v1);

        BigDecimal b2 = double2bigDecimal(v2);

        return b1.subtract(b2).doubleValue();

    }



    /**

     * 提供精确的减法运算

     * @param v1

     * @param v2

     * @return 两个参数数学差，以字符串格式返回

     */

    public static String subtract(String v1, String v2)

    {

        BigDecimal b1 = string2BigDecimal(v1);

        BigDecimal b2 = string2BigDecimal(v2);

        return b1.subtract(b2).toString();

    }





    /**

     * 提供精确的乘法运算。

     * @param v1

     * @param v2

     * @return 两个参数的积

     */

    public static double multiply(double v1, double v2)

    {

        BigDecimal b1 = double2bigDecimal(v1);

        BigDecimal b2 = double2bigDecimal(v2);

        return b1.multiply(b2).doubleValue();

    }



    /**

     * 提供精确的乘法运算

     * @param v1

     * @param v2

     * @return 两个参数的数学积，以字符串格式返回

     */

    public static String multiply(String v1, String v2)

    {

        BigDecimal b1 = string2BigDecimal(v1);

        BigDecimal b2 = string2BigDecimal(v2);

        return b1.multiply(b2).toString();

    }



    /**

     * 提供（相对）精确的除法运算，当发生除不尽的情况时，精确到

     * 小数点以后10位，以后的数字四舍五入,舍入模式采用ROUND_HALF_EVEN

     * @param v1

     * @param v2

     * @return 两个参数的商

     */

    public static double divide(double v1, double v2)

    {

        return divide(v1, v2, DEFAULT_DIV_SCALE);

    }



    /**

     * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指

     * 定精度，以后的数字四舍五入。舍入模式采用ROUND_HALF_EVEN

     * @param v1

     * @param v2

     * @param scale 表示需要精确到小数点以后几位。

     * @return 两个参数的商

     */

    public static double divide(double v1,double v2, int scale)

    {

        return divide(v1, v2, scale, BigDecimal.ROUND_HALF_EVEN);

    }



    /**

     * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指

     * 定精度，以后的数字四舍五入。舍入模式采用用户指定舍入模式

     * @param v1

     * @param v2

     * @param scale 表示需要精确到小数点以后几位

     * @param round_mode 表示用户指定的舍入模式

     * @return 两个参数的商

     */

    public static double divide(double v1,double v2,int scale, int round_mode){

        checkScale(scale);
        BigDecimal b1 = double2bigDecimal(v1);

        BigDecimal b2 = double2bigDecimal(v2);



        return b1.divide(b2, scale, round_mode).doubleValue();

    }



    /**

     * 提供（相对）精确的除法运算，当发生除不尽的情况时，精确到

     * 小数点以后10位，以后的数字四舍五入,舍入模式采用ROUND_HALF_EVEN

     * @param v1

     * @param v2

     * @return 两个参数的商，以字符串格式返回

     */

    public static String divide(String v1, String v2)

    {

        return divide(v1, v2, DEFAULT_DIV_SCALE);

    }



    /**

     * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指

     * 定精度，以后的数字四舍五入。舍入模式采用ROUND_HALF_EVEN

     * @param v1

     * @param v2

     * @param scale 表示需要精确到小数点以后几位

     * @return 两个参数的商，以字符串格式返回

     */

    public static String divide(String v1, String v2, int scale)

    {

        return divide(v1, v2, DEFAULT_DIV_SCALE, BigDecimal.ROUND_HALF_EVEN);

    }



    /**

     * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指

     * 定精度，以后的数字四舍五入。舍入模式采用用户指定舍入模式

     * @param v1

     * @param v2

     * @param scale 表示需要精确到小数点以后几位

     * @param round_mode 表示用户指定的舍入模式

     * @return 两个参数的商，以字符串格式返回
     *
     *  Java中，如果int/int中除数为0，会抛出异常java.lang.ArithmeticException: / by zero，这个不容易理解！但如果是浮点型，就不会报异常了！
    总结如下：
    0.0/0.0 得到的结果是NaN(not an number的简称，即"不是数字")。通过Double.isNaN(double x)来判断。
    正数/0.0 得到的结果是正无穷大，即Infenity
    负数/0.0 得到的结果是负无穷大，即Infenity。通过Double.isInfinite(double x)来判断。

     */

    public static String divide(String v1, String v2, int scale, int round_mode){

        checkScale(scale);
        BigDecimal b1 = string2BigDecimal(v1);
        BigDecimal b2 = string2BigDecimal(v2);
        return b1.divide(b2, scale, round_mode).toString();

    }

    private static BigDecimal string2BigDecimal(String v1) {
        return new BigDecimal(null== v1 || TextUtils.isEmpty(v1)?"0":v1);
    }


    /**

     * 提供精确的小数位四舍五入处理,舍入模式采用ROUND_HALF_EVEN
     * @param v 需要四舍五入的数字
     * @param scale 小数点后保留几位
     * @return 四舍五入后的结果
     */

    public static double round(double v,int scale){
        return round(v, scale, BigDecimal.ROUND_HALF_EVEN);
    }

    /**
     * 提供精确的小数位四舍五入处理
     * @param v 需要四舍五入的数字
     * @param scale 小数点后保留几位
     * @param round_mode 指定的舍入模式
     * @return 四舍五入后的结果

     */

    public static double round(double v, int scale, int round_mode) {

        checkScale(scale);
        BigDecimal b = double2bigDecimal(v);
        return b.setScale(scale, round_mode).doubleValue();

    }



    /**
     * 提供精确的小数位四舍五入处理,舍入模式采用ROUND_HALF_EVEN
     * @param v 需要四舍五入的数字
     * @param scale 小数点后保留几位
     * @return 四舍五入后的结果，以字符串格式返回
     */

    public static String round(String v, int scale){
        return round(v, scale, BigDecimal.ROUND_HALF_EVEN);
    }

    /**
     * 提供精确的小数位四舍五入处理
     * @param v 需要四舍五入的数字
     * @param scale 小数点后保留几位
     * @param round_mode 指定的舍入模式
     * @return 四舍五入后的结果，以字符串格式返回

     */

    public static String round(String v, int scale, int round_mode) {
        checkScale(scale);
        BigDecimal b = new BigDecimal(v);
        return b.setScale(scale, round_mode).toString();
    }

    private static void checkScale(int scale) {

        if(scale<0) throw new IllegalArgumentException("The scale must be a positive integer or zero");
    }

    //获取整数数值格式
    public  static String getNumberInstance(String str){
        checkNumber(str);

        BigDecimal bd = new BigDecimal(str);
        NumberFormat currency = NumberFormat.getNumberInstance() ;
        currency.setMinimumFractionDigits(2);//设置数的小数部分所允许的最小位数(如果不足后面补0)
        currency.setMaximumFractionDigits(3);//设置数的小数部分所允许的最大位数(如果超过会四舍五入)
        return currency.format(bd);
    }

    //获取货币数值格式
    public  static String getCurrencyInstance(String str){
        checkNumber(str);
        BigDecimal bd=new BigDecimal(str);
        NumberFormat  currency = NumberFormat.getCurrencyInstance() ;
        currency.setMinimumFractionDigits(2);//设置数的小数部分所允许的最小位数(如果不足后面补0)
        currency.setMaximumFractionDigits(2);//设置数的小数部分所允许的最大位数(如果超过会四舍五入)
        return currency.format(bd);
    }

    public  static String getPercentInstance(String str){
        checkNumber(str);
        BigDecimal bd=new BigDecimal(str);
        NumberFormat  currency = NumberFormat.getPercentInstance();
        currency.setMinimumFractionDigits(2);//设置数的小数部分所允许的最小位数(如果不足后面补0)
        currency.setMaximumFractionDigits(3);//设置数的小数部分所允许的最大位数(如果超过会四舍五入)
        return currency.format(bd);
    }

    private static void checkNumber(String str) {
        if (!isNumeric(str)){
            throw new NumberFormatException("is not a number format");
        }
    }

    public  static boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("-?[0-9]+.?[0-9]+");
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();

    }



    /**
     * 万亿位 精确到分  推荐使用
     */
    public static String digitUppercase(double v) {

        String UNIT = "万仟佰拾亿仟佰拾万仟佰拾元角分";
        String DIGIT = "零壹贰叁肆伍陆柒捌玖";
        double MAX_VALUE = 9999999999999.99D;

        if (v < 0 || v > MAX_VALUE) {
            return "参数非法(超出范围)!";
        }
        long l = Math.round(v * 100);
        if (l == 0) {
            return "零元整";
        }
        String strValue = l + "";
        // i用来控制数
        int i = 0;
        // j用来控制单位
        int j = UNIT.length() - strValue.length();
        String rs = "";
        boolean isZero = false;
        for (; i < strValue.length(); i++, j++) {
            char ch = strValue.charAt(i);
            if (ch == '0') {
                isZero = true;
                if (UNIT.charAt(j) == '亿' || UNIT.charAt(j) == '万' || UNIT.charAt(j) == '元') {
                    rs = rs + UNIT.charAt(j);
                    isZero = false;
                }
            } else {
                if (isZero) {
                    rs = rs + "零";
                    isZero = false;
                }
                rs = rs + DIGIT.charAt(ch - '0') + UNIT.charAt(j);
            }
        }
        if (!rs.endsWith("分")) {
            rs = rs + "整";
        }
        rs = "人民币" + rs.replaceAll("亿万", "亿");
        return rs;
    }




    private static final String[] CN_UPPER_NUMBER = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
    private static final String[] RADICES = {"", "拾", "佰", "仟"};
    private static final String[] BIG_RADICES = {"", "万", "亿", "兆"};

    /**
     * 获取大写的人名币的金额，单位精确到分
     * Create by andy on 2016-11-22 14:26
     *
     * @param money 人民币，单位：元
     * @return 人民币大写的金额
     */

    public static String getRMB(double money) {
        return getRMB(Math.round(money * 100));
    }
    /**
     *
     *  万兆? 单位精确到分  ,输入参数  以分为单位
     * 获取大写的人名币的金额，单位精确到分
     * Create by andy on 2016-11-22 14:26
     *
     * @param money 人民币，单位：分
     * @return 人民币大写的金额
     */
    public static String getRMB(long money) {
        StringBuilder result = new StringBuilder("");
        if (money == 0) {
            return "零元整";
        }
        if (money> 999999999999999999L){
            return "参数非法(超出范围)!";
        }
        long integral = money / 100;//整数部分
        int integralLen = (integral + "").length();
        int decimal = (int) (money % 100);//小数部分
        if (integral > 0) {
            int zeroCount = 0;
            for (int i = 0; i < integralLen; i++) {
                int unitLen = integralLen - i - 1;
                int d = Integer.parseInt((integral + "").substring(i, i + 1));//当前数字的值
                int quotient = unitLen / 4;//大单位的下标{"", "万", "亿"}
                int modulus = unitLen % 4;//获取单位的下标（整数部分都是以4个数字一个大单位，比如：个、十、百、千、个万、十万、百万、千万、个亿、十亿、百亿、千亿）
                if (d == 0) {
                    zeroCount++;
                } else {
                    if (zeroCount > 0) {
                        result.append(CN_UPPER_NUMBER[0]);
                    }
                    zeroCount = 0;
                    result.append(CN_UPPER_NUMBER[d]).append(RADICES[modulus]);
                }
                if (modulus == 0 && zeroCount < 4) {
                    result.append(BIG_RADICES[quotient]);
                }
            }
            result.append("元");
        }
        if (decimal > 0) {
            int j = decimal / 10;
            if (j > 0) {
                result.append(CN_UPPER_NUMBER[j]).append("角");
            }
            j = decimal % 10;
            if (j > 0) {
                result.append(CN_UPPER_NUMBER[j]).append("分");
            }
        } else {
            result.append("整");
        }
        return result.toString();
    }

   /* NumberFormat number = NumberFormat.getNumberInstance();
    String str = number.format(format);//12,343,171.6

    //获取整数数值格式
    NumberFormat integer = NumberFormat.getIntegerInstance();
    str = integer.format(format);//如果带小数会四舍五入到整数12,343,172

    //获取货币数值格式
    NumberFormat currency = NumberFormat.getCurrencyInstance();
    currency.setMinimumFractionDigits(2);//设置数的小数部分所允许的最小位数(如果不足后面补0)
    currency.setMaximumFractionDigits(4);//设置数的小数部分所允许的最大位数(如果超过会四舍五入)
    str = currency.format(format);//￥12,343,171.60

    //获取显示百分比的格式
    NumberFormat percent = NumberFormat.getPercentInstance();
    percent.setMinimumFractionDigits(2);//设置数的小数部分所允许的最小位数(如果不足后面补0)
    percent.setMaximumFractionDigits(3);//设置数的小数部分所允许的最大位数(如果超过会四舍五入)
    str = percent.format(format);//1,234,317,160.00% */
/**
 * 1、ROUND_UP

 舍入远离零的舍入模式。

 在丢弃非零部分之前始终增加数字(始终对非零舍弃部分前面的数字加1)。

 注意,此舍入模式始终不会减少计算值的大小。

 2、ROUND_DOWN

 接近零的舍入模式。

 在丢弃某部分之前始终不增加数字(从不对舍弃部分前面的数字加1,即截短)。

 注意,此舍入模式始终不会增加计算值的大小。

 3、ROUND_CEILING

 接近正无穷大的舍入模式。

 如果 BigDecimal 为正,则舍入行为与 ROUND_UP 相同;

 如果为负,则舍入行为与 ROUND_DOWN 相同。

 注意,此舍入模式始终不会减少计算值。

 4、ROUND_FLOOR

 接近负无穷大的舍入模式。

 如果 BigDecimal 为正,则舍入行为与 ROUND_DOWN 相同;

 如果为负,则舍入行为与 ROUND_UP 相同。

 注意,此舍入模式始终不会增加计算值。

 5、ROUND_HALF_UP

 向“最接近的”数字舍入,如果与两个相邻数字的距离相等,则为向上舍入的舍入模式。

 如果舍弃部分 >= 0.5,则舍入行为与 ROUND_UP 相同;否则舍入行为与 ROUND_DOWN 相同。

 注意,这是我们大多数人在小学时就学过的舍入模式(四舍五入)。

 6、ROUND_HALF_DOWN

 向“最接近的”数字舍入,如果与两个相邻数字的距离相等,则为上舍入的舍入模式。

 如果舍弃部分 > 0.5,则舍入行为与 ROUND_UP 相同;否则舍入行为与 ROUND_DOWN 相同(五舍六入)。

 7、ROUND_HALF_EVEN

 向“最接近的”数字舍入,如果与两个相邻数字的距离相等,则向相邻的偶数舍入。

 如果舍弃部分左边的数字为奇数,则舍入行为与 ROUND_HALF_UP 相同;

 如果为偶数,则舍入行为与 ROUND_HALF_DOWN 相同。

 注意,在重复进行一系列计算时,此舍入模式可以将累加错误减到最小。

 此舍入模式也称为“银行家舍入法”,主要在美国使用。四舍六入,五分两种情况。

 如果前一位为奇数,则入位,否则舍去。

 以下例子为保留小数点1位,那么这种舍入方式下的结果。

 1.15>1.2 1.25>1.2

 8、ROUND_UNNECESSARY

 断言请求的操作具有精确的结果,因此不需要舍入。

 如果对获得精确结果的操作指定此舍入模式,则抛出ArithmeticException。
 */
}