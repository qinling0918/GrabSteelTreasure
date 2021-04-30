package com.zgw.qgb.hardware;

/**
 * created by tsinling on: 2020-03-13 11:01
 * description:
 */
public enum Code {
    // 蓝牙连接
    CODE_CONNECT_SUCCESS(0, "蓝牙连接成功"),
    CODE_CONNECT_FAILURE(1, "蓝牙连接失败"),
    CODE_CONNECT_TIMEOUT(2, "蓝牙连接超时"),
    CODE_CONNECT_NO_DEVICE(3, "蓝牙设备为空"),
    CODE_CONNECT_NOT_BOND(4, "蓝牙未进行配对"),
    CODE_CONNECT_BOND_FAILURE(5, "蓝牙配对失败"),
    CODE_DISCONNECT(6, "蓝牙连接已断开"),
    CODE_NOT_SUPPORT_BLUETOOTH(7, "该Android设备不支持蓝牙"),
    CODE_BLUETOOTH_ADDRESS_ERROR(8, "蓝牙设备 MAC地址错误"),
    CODE_BLUETOOTH_NOT_OPEN(9, "Android设备蓝牙未打开"),

    // 密钥协商
    CODE_KEY_INIT_FAILURE(10, "初始密钥获取失败"),
    CODE_KEY_AGREEMENT_FAILURE(11, "密钥协商失败"),
    CODE_KEY_AGREEMENT_CONFIRM_FAILURE(12, "密钥协商请求失败"),


    CODE_UNKONW(0xff, "未知错误"),
    ;

    int code;
    String msg;

    Code(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public static Code getCode(int codeNum){
        Code[] codes = Code.values();
        for (Code code : codes) {
            if (codeNum == code.code) {
                return code;
            }
        }
        return CODE_UNKONW;
    }
}
