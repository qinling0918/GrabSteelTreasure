package com.zgw.qgb.hardware.listener;

import android.bluetooth.BluetoothDevice;

import java.util.Set;

/**
 * Created by qinling on 2020/1/19 14:39
 * Description: 蓝牙设备搜索回调
 */
public interface OnBluetoothDiscoverListener {
    /**
     *  已经配过对的
     * @param pairedBluetoothDevices
     */
    void onBondedDevices(Set<BluetoothDevice> pairedBluetoothDevices);

    /**
     * 新发现单位
     * @param newDevice
     */
    void onFound(BluetoothDevice newDevice);

    /**
     *  蓝牙搜索开始
     */
    void onDiscoveryStarted();

    /**
     *  蓝牙搜索结束
     */
    void onDiscoveryfinished();
}
