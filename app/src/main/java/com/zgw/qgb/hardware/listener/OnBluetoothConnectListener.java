package com.zgw.qgb.hardware.listener;

/**
 * Created by qinling on 2020/1/19 14:39
 * Description:
 */
public interface  OnBluetoothConnectListener {
    /**
     * @param success 是否连接成功
     * @param code 响应码
     * @param msg 提示信息
     */
    void onConnect(boolean success, int code, String msg);

    /**
     *  蓝牙断开连接
     * @param code
     * @param msg
     */
    void onDisConnect(int code, String msg);
}
