package com.zgw.qgb.hardware;

import android.util.SparseArray;


import androidx.annotation.IntRange;

import java.util.Arrays;


/**
 * Created by qinling on 2020/1/20 11:55
 * Description: 数据格式匹配
 */
public class DataMatcher {
    public static int FRAME_WS = 0x00;
    /**
     * 非报文格式的数据
     */
    public static int UNKNOW = 0x01;
    /**
     * 安全单元报文
     */
    public static int FRAME_SAFE_UNIT = 0x02;
    /**
     * 645报文
     */
    public static int FRAME_645 = 0x03;
    /**
     * 698报文（面向对象报文）
     */
    public static int FRAME_698 = 0x04;
    /**
     * 1376.1报文
     */
    public static int FRAME_1376_1 = 0x05;
    /**
     * 电子封印数据
     */
    public static int SEAL = 0x06;


   /* public static int FRAME_WS = 0;
    public static int FRAME_SAFE_UNIT = 1;
    public static int FRAME_645 = 2;
    public static int FRAME_698 = 3;
    public static int FRAME_1376_1 = 4;
    public static int FRAME_SCANNER = 7;
    public static int GET_INFRARAD_MODE = 8;
    public static int RFID = 5;
    public static int TYPE_A_CPU = 6;
    public static int TYPE_GETCURRENTMODEL = 9;*/

    private SparseArray<Matcher> dataMatchers = new SparseArray<>();
    private Matcher matcher;
    private byte[] lastMatchedBytes;

    public static DataMatcher getInstance() {
        return SingleTon.INSTANCE;
    }

    private DataMatcher() {
        dataMatchers.put(FRAME_WS, new FrameWSMatcher());
        dataMatchers.put(FRAME_SAFE_UNIT, new FrameSafeUnitMatcher());
        dataMatchers.put(FRAME_645, new Frame645Matcher());
        dataMatchers.put(FRAME_698, new Frame698Matcher());
        dataMatchers.put(FRAME_1376_1, new Frame376Matcher());
        dataMatchers.put(UNKNOW, new DefaultMatcher());
    }

    private static class SingleTon {
        private static final DataMatcher INSTANCE = new DataMatcher();
    }

    /**
     * 方便扩展 报文解析匹配器,若现有匹配器不满足,可以基于此进行扩展,
     *
     * @param key   数据格式,尽量不要与现有 key 冲突,当然若是需要替换也可以 重复
     * @param value 自定义好的匹配器.需要为  Matcher的实现类
     * @return 数据格式匹配器
     */
    public DataMatcher put(int key, Matcher value) {
        dataMatchers.put(key, value);
        return this;
    }

    public DataMatcher clear() {
        dataMatchers.clear();
        return this;
    }

    public Matcher match(byte[] bytes) {
        for (int i = 0; i < dataMatchers.size(); i++) {
            Matcher matcher = dataMatchers.valueAt(i);
            lastMatchedBytes = matcher.readFrom(bytes);
            if (null != lastMatchedBytes) {
                this.matcher = matcher;
                return matcher;
            }
        }
        return null;
    }

    public Matcher match(int cammandType) {
        this.matcher = dataMatchers.get(cammandType);
        return this.matcher;
    }

    public boolean matched() {
        return null != this.matcher;
    }

    public byte[] getLastMatchedBytes() {
        return lastMatchedBytes;
    }
    public byte[] readFrom(byte[] bytes) {
        return this.matcher.readFrom(bytes);
    }
    public byte[] readFrom(byte[] bytes, @IntRange(from = 0) int offset) {
        return this.matcher.readFrom(bytes,offset);
    }

    public static abstract class Matcher {

        public abstract byte[] readFrom(byte[] bytes, @IntRange(from = 0) int offset);
        public byte[] readFrom(byte[] bytes) {
            return readFrom(bytes, 0);
        }
        public String name(){
            return getClass().getSimpleName();
        }

    }

    public static final class Frame698Matcher extends Matcher {

        @Override
        public byte[] readFrom(byte[] bytes, int offset) {
            if (bytes == null || bytes.length - offset < 12) {
                return null;
            }

            try {
                for (int i = offset; i < bytes.length; i++) {
                    //帧头68
                    if (bytes[i] == (byte) 0x68) {
                        //获取698数据帧的长度域
                        int length = (NumberConvert.byteToInt(bytes, i + 1, 2));
                        if (length > bytes.length) {
                            return null;
                        }

                        //计算结束符16在当前缓冲区数组中的位置
                        if (bytes[(i + length + 1)] == (byte) 0x16) {
                            int frame698Len = length + 2;
                            byte[] temp = new byte[frame698Len];
                            System.arraycopy(bytes, i, temp, 0, frame698Len);
                            //计算校验码
                            if (checkFCS(temp)) {
                                return temp;
                            }
                            return null;
                        }
                        return null;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return null;
        }


        private boolean checkFCS(byte[] frame) {
            String fcsHex = NumberConvert.bytesToHexString(frame, 1, frame.length - 4);
            String fcs = NumberConvert.getFcsOrHcs(fcsHex);
            return fcs != null && fcs.equalsIgnoreCase(NumberConvert.bytesToHexString(frame, frame.length - 3, 2));
        }
    }

    public static final class FrameWSMatcher extends Matcher {

        @Override
        public byte[] readFrom(byte[] bytes, int offset) {
            // 外设协议最少为20字节
            if (null == bytes || bytes.length - offset < 20) {
                return null;
            }
            try {
                for (int start = offset; start < bytes.length; start++) {
                    // 起始符和标识符正确
                    if (bytes[start] == (byte) 0x97 && bytes[start + 11] == (byte) 0x97) {
                        // 数据域长度
                        int lenArea = NumberConvert.byteToInt(bytes, start + 12, 2);
                        int frameLen = lenArea + 20;
                        int stopIndex = start + frameLen - 1;

                        if (frameLen> bytes.length ) {
                            return null;
                        }
                        // 结束符正确
                        if (bytes[stopIndex] == (byte) 0xE9) {
                            byte checkCode = bytes[stopIndex - 1];
                            byte tempCode = 0;
                            for (int i = start; i <= stopIndex - 2; i++) {
                                tempCode = (byte) (tempCode + bytes[i]);
                            }
                            // 检验码正确
                            if (tempCode == checkCode) {
                                // 是正常完整报文，返回
                                byte[] temp = new byte[frameLen];
                                // 将整个报文读出来
                                System.arraycopy(bytes, start, temp, 0, frameLen);
                                return temp;
                            } else {
                                return null;
                            }
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            return null;
        }


    }

    public static final class Frame645Matcher extends Matcher {

        private boolean checkCrc(byte[] bytes) {
            String crc = NumberConvert.bytesToHexString(bytes, bytes.length - 2, 1);
            String crcSource = NumberConvert.bytesToHexString(bytes, 0, bytes.length - 2);
            return crc.equals(NumberConvert.getCs(crcSource));
        }

        @Override
        public byte[] readFrom(byte[] bytes, int offset) {
            // 无数据域时 645报文为12字节
            if (bytes == null || bytes.length - offset < 12) {
                return null;
            }
            try {
                for (int i = offset; i < bytes.length; i++) {
                    //帧头68
                    if (bytes[i] == (byte) 0x68 && bytes[i + 7] == (byte) 0x68) {
                        //获取645数据帧的长度 +12 数据域之外的数据为12字节
                        int length = (NumberConvert.byteToInt(bytes, 9 + i, 1)) + 12;
                        // 帧长度 大于现有的有效字节数组长度
                        if (length > bytes.length - i) {
                            return null;
                        }
                        //计算结束符16在当前缓冲区数组中的位置
                        if (bytes[(i + length - 1)] == (byte) 0x16) {
                            byte[] temp = new byte[length];
                            System.arraycopy(bytes, i, temp, 0, length);
                            //计算校验码
                            if (checkCrc(temp)) {
                                return temp;
                            }
                        }
                        return null;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return null;
        }


    }

    public static final class FrameSafeUnitMatcher extends Matcher {

        @Override
        public byte[] readFrom(byte[] bytes, int offset) {
            if (bytes == null || bytes.length - offset < 7) {
                return null;
            }
            try {
                for (int i = offset; i < bytes.length; i++) {
                    //帧头68
                    if (bytes[i] == (byte) 0xe9) {
                        //获取698数据帧的长度
                        byte[] lenArr = new byte[]{bytes[i + 2], bytes[i + 1]};

                        // 取第2个和第3个 表示长度域  +5 主功能标识开始到数据域最后1字节结束之外的5个字节
                        int len = (NumberConvert.byteToInt(lenArr, 0, 2)) + 5;
                        // int len = ByteUtils.byteToInt(bytes, 1, 2) + 5;
                        if (len > bytes.length) {
                            return null;
                        }
                        if (bytes[(i + len - 1)] == (byte) 0xE6) {
                            byte[] result = new byte[len];
                            System.arraycopy(bytes, i, result, 0, len);

                            byte[] crcBytes = new byte[len - 2];
                            System.arraycopy(bytes, i, crcBytes, 0, len - 2);
                            byte crc = CrcUtil.getCrc(crcBytes);
                            if (bytes[(i + len - 2)] == crc) {
                                return result;
                            }
                            return null;
                        }
                        return null;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return null;
        }
    }


    public static final class Frame376Matcher extends Matcher {

        @Override
        public byte[] readFrom(byte[] bytes, int offset) {
            // 无数据域时 1376_1报文为14字节
            if (null == bytes || bytes.length - offset < 14) {
                return null;
            }

            int start_index;// 报文开始的下标
            int stop_index;// 报文结束的下标

            for (int start = offset; start < bytes.length; start++) {
                // 起始符和标识符正确
                if (bytes[start] == (byte) 0x68 && bytes[start + 5] == (byte) 0x68) {
                    start_index = start;
                    int length = (NumberConvert.byteToInt(bytes, start_index + 1, 2)) >> 2;
                    //加2是校验位和结尾标识16
                    stop_index = start_index + 5 + length + 2;

                    if (stop_index > bytes.length) {
                        return null;
                    }
                    // 结束符正确
                    if (bytes[stop_index] == (byte) 0x16) {
                        byte check_code = bytes[stop_index - 1];
                        //计算校验和
                        byte temp_code = 0;
                        for (int i = (start_index + 6); i <= stop_index - 2; i++) {
                            temp_code += bytes[i];
                        }
                        byte cs = (byte) (temp_code % 256);
                        if (cs == check_code) {
                            // 检验码正确   是正常完整报文，返回
                            int len = stop_index - start_index + 1;
                            byte[] result = new byte[len];

                            // 将整个报复制给out_data数组
                            System.arraycopy(bytes, start_index, result, 0, len);
                            return result;

                        }
                    }
                    return null;
                }
            }
            return null;
        }
    }
    private static final class DefaultMatcher extends Matcher {
        @Override
        public byte[] readFrom(byte[] bytes, int offset) {
            return Arrays.copyOfRange(bytes,offset,bytes.length);
        }
    }

    public static void main(String[] args) {

        /*System.out.println(DataConvert.getSumValue("68 99 99 99 99 99 99 68 05 01 61".replaceAll(" ","")));
        System.out.println(String.format("%02x",0x4cd%0x100));
        System.out.println(0x000000FF&0x4cd);
        System.out.println(0x0000ffFF&0x4cd);
        System.out.println(0x00ffffFF&0x4cd);*/

      /*  try {

            System.out.println(Hex.toHexString(matcher.readFrom(Hex.decode("970080000798D3346000349708000180B005180400010801FFFF004AE9"))));
        }catch (Exception e){
            System.out.println(Hex.toHexString(matcher.readFrom(Hex.decode("970080000f98d3346000349701000140b00518ffe9"))));
            System.out.println(Hex.toHexString(matcher.readFrom(Hex.decode("970080000F98D331F34A8D9709000D80B005190400010801001000A5E9"))));
        }

        System.out.println(Hex.toHexString(matcher.readFrom(Hex.decode("970080000798D3346000349702000180B00517FA31E9"))));*/
    }

}
