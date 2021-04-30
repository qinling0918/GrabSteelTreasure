package com.zgw.qgb.hardware;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;


import com.zgw.qgb.hardware.listener.OnBluetoothConnectListener;
import com.zgw.qgb.hardware.listener.OnBluetoothDiscoverListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

import static android.bluetooth.BluetoothDevice.ACTION_BOND_STATE_CHANGED;
import static android.bluetooth.BluetoothDevice.BOND_NONE;
import static com.zgw.qgb.hardware.Code.CODE_BLUETOOTH_ADDRESS_ERROR;
import static com.zgw.qgb.hardware.Code.CODE_BLUETOOTH_NOT_OPEN;
import static com.zgw.qgb.hardware.Code.CODE_CONNECT_BOND_FAILURE;
import static com.zgw.qgb.hardware.Code.CODE_CONNECT_FAILURE;
import static com.zgw.qgb.hardware.Code.CODE_CONNECT_NOT_BOND;
import static com.zgw.qgb.hardware.Code.CODE_CONNECT_NO_DEVICE;
import static com.zgw.qgb.hardware.Code.CODE_CONNECT_SUCCESS;
import static com.zgw.qgb.hardware.Code.CODE_DISCONNECT;
import static com.zgw.qgb.hardware.Code.CODE_NOT_SUPPORT_BLUETOOTH;
import static com.zgw.qgb.hardware.NumberConvert.bytesToHexString;


/**
 * Created by qinling on 2020/1/19 14:37
 * Description: 蓝牙服务,主要是蓝牙连接,数据收发
 */
public class BluetoothService {

    public static final int STATE_CONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_NONE = 2;

    public static final int READ_TIME_OUT = 7000;

    /**
     * 蓝牙串口服务 UUID
     */
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter mAdapter;

    private BluetoothDevice mDevice;
    private BluetoothSocket mSocket;
    private InputStream mmInStream;
    private OutputStream mmOutStream;

    private int mState;
    private ConnectThread mConnectThread;
    private ReadThread mReadThread;

    private Context app;


    public BluetoothDevice getBluetoothDevice() {
        return mDevice;
    }
    public String getBluetoothDeviceMacAddress() {
        return null == getBluetoothDevice()? null:getBluetoothDevice().getAddress();
    }
    public void onStart(String mac, OnBluetoothConnectListener listener) {
        connect(mac);
        this.mListener = listener;
    }

    public void onStart(BluetoothDevice device, OnBluetoothConnectListener listener) {
        connect(device);
        this.mListener = listener;
    }


    private OnBluetoothConnectListener mListener;


    public static BluetoothService getInstance() {
        return SingleTon.INSTANCE;
    }

    public BluetoothService init(Context app) {
        BluetoothService service = getInstance();
        service.app = app.getApplicationContext();
        return service;
    }


    private static class SingleTon {
        private static final BluetoothService INSTANCE = new BluetoothService();
    }

    private BluetoothService() {
        this.mAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mState = STATE_NONE;
    }

    private Handler mhandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            Code code = Code.getCode(msg.what);

            switch (code) {
                // 断开连接
                case CODE_DISCONNECT: {
                    onDisConnect(code.code, code.msg);
                    clearHandler();
                    break;
                }
                // 连接成功
                case CODE_CONNECT_SUCCESS: {
                    onConnect(true, code.code, code.msg);
                    break;
                }
                // 其他的连接错误原因
                default:
                    onConnect(false, code.code, code.msg);
                    clearHandler();
                    break;
            }
        }
    };

    /**
     *  若是连接失败或者蓝牙断开则清出handler引用。
     */
    private void clearHandler() {
        if(mhandler!=null){
            mhandler.removeCallbacksAndMessages(null);
            mhandler = null;
        }
    }

    private OnBluetoothDiscoverListener onBluetoothDiscoverListener;

    public void startDiscovery(OnBluetoothDiscoverListener listener) {
        startDiscovery(0, listener);
    }

    public void startDiscovery(int searchTime, OnBluetoothDiscoverListener listener) {
        this.onBluetoothDiscoverListener = listener;
        if (searchTime > 0) {
            setDiscoverableTimeout(searchTime);
        }
        if (null == mAdapter) {
            sendEmptyMessage(CODE_NOT_SUPPORT_BLUETOOTH);
            return;
        }
        if (null != listener) {
            listener.onBondedDevices(mAdapter.getBondedDevices());
        }
        if (!checkBluetoothEnable(null)) {
            return;
        }
        if (null == this.app) {
            sendEmptyMessage(CODE_NOT_SUPPORT_BLUETOOTH);
            return;
        }
        registerReceiverAndstartDiscovery(listener);
    }

    /**
     * 注册蓝牙搜索广播监听
     *
     * @param listener 用于回调蓝牙搜索结果
     */
    private void registerReceiverAndstartDiscovery(final OnBluetoothDiscoverListener listener) {

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    // 发现设备
                    case BluetoothDevice.ACTION_FOUND:
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if (null != listener) {
                            listener.onFound(device);
                        }
                        break;
                    // 蓝牙开始搜索
                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                        // 记录搜索开始时间

                        if (null != listener) {
                            listener.onDiscoveryStarted();
                        }

                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        if (null != listener) {
                            listener.onDiscoveryfinished();
                        }
                        unregisterReceiver(this);
                        break;
                }
            }
        };
        // 避免由于多次调用导致报错，先解除
        unregisterReceiver(receiver);


        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);
        mAdapter.startDiscovery();


    }


    /**
     * 设置蓝牙搜索时间,
     *
     * @param timeout
     */
    @SuppressLint("uncheck")
    public void setDiscoverableTimeout(int timeout) {
        try {
            Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", int.class);
            setDiscoverableTimeout.setAccessible(true);
            Method setScanMode = BluetoothAdapter.class.getMethod("setScanMode", int.class, int.class);
            setScanMode.setAccessible(true);
            setDiscoverableTimeout.invoke(mAdapter, timeout);
            setScanMode.invoke(mAdapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, timeout);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void unregisterReceiver(BroadcastReceiver receiver) {
        // 避免由于多次调用导致报错，先解除
        try {
            app.unregisterReceiver(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        try {
            app.registerReceiver(receiver, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean cancelDiscovery() {
        if (mAdapter.isDiscovering()) {
            return mAdapter.cancelDiscovery();
        }
        return true;
    }

    private void onConnect(boolean success, int code, String msg) {
        this.mState = success ? STATE_CONNECTED : STATE_NONE;
        if (this.mListener != null) {
            this.mListener.onConnect(success, code, msg);
        }
    }

    private void onDisConnect(int code, String msg) {
        this.mState = STATE_NONE;
        if (this.mListener != null) {
            this.mListener.onDisConnect(code, msg);
        }
    }


    private void sendEmptyMessage(Code what) {
        if (this.mhandler != null) {
            this.mhandler.sendEmptyMessage(what.code);
        }
    }

    public synchronized void connect(String macAddress) {
        if (null == mAdapter) {
            sendEmptyMessage(CODE_NOT_SUPPORT_BLUETOOTH);
            return;
        }
        if (!BluetoothAdapter.checkBluetoothAddress(macAddress)) {
            sendEmptyMessage(CODE_BLUETOOTH_ADDRESS_ERROR);
            return;
        }
        connect(mAdapter.getRemoteDevice(macAddress));
    }

    public synchronized void connect(BluetoothDevice device) {
        this.mDevice = device;
        if (null == mAdapter) {
            sendEmptyMessage(CODE_NOT_SUPPORT_BLUETOOTH);
            return;
        }
        if (null == mDevice) {
            sendEmptyMessage(CODE_CONNECT_NO_DEVICE);
            return;
        }
        if (!checkBluetoothEnable(device)) return;

        checkDeviceBonded(device);
    }

    /**
     * 检查蓝牙设备是否已进行配对,若没有配对的则进行配对.
     *
     * @param device
     */
    private void checkDeviceBonded(BluetoothDevice device) {
        if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
            // 弹出配对框，成功后会触发 广播BOND_BONDED ，失败会触发BOND_NONE
            if (this.app != null) {
                createBond(app, device);
            } else {
                sendEmptyMessage(CODE_CONNECT_NOT_BOND);
                return;
            }
        } else {
            connectDevice(device);
        }
    }

    /**
     * 检查蓝牙是否打开,若是已经打开 则进行下一步.若是未打开则设置广播接收器,在接收到蓝牙被打开的广播时,解除监听,
     * 开始校验配对关系
     *
     * @param device
     * @return
     */
    private boolean checkBluetoothEnable(BluetoothDevice device) {
        if (mAdapter.isEnabled()) {
            return true;
        }
        if (null == this.app) {
            // 若是蓝牙未打开
            sendEmptyMessage(CODE_BLUETOOTH_NOT_OPEN);
            return false;
        }
        openBluetooth(device);
        return false;
    }

    /**
     * 打开蓝牙, 通过注册广播接收监听
     *
     * @param device 若 device 为 null ,蓝牙打开后执行搜索, 不为 null 则执行配对.
     */
    private void openBluetooth(final BluetoothDevice device) {
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    // 监听到蓝牙被打开,则解除监听,并开始校验配对关系以及进行蓝牙连接.
                    if (BluetoothAdapter.STATE_ON == state) {
                        unregisterReceiver(this);

                        if (null == device) {
                            registerReceiverAndstartDiscovery(onBluetoothDiscoverListener);
                        } else {
                            // device 在蓝牙搜索时传入的为null，故不会执行。只有在连接蓝牙时不为null
                            checkDeviceBonded(device);
                        }

                    }
                }
            }
        };
        // 避免由于多次，先解除
        unregisterReceiver(receiver);
        registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        mAdapter.enable();
    }

    /**
     * 开始蓝牙配对,并在收到蓝牙配对成功或者失败的广播后,进行蓝牙连接
     *
     * @param app
     * @param device
     */
    private void createBond(final Context app, final BluetoothDevice device) {
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (ACTION_BOND_STATE_CHANGED.equals(intent.getAction())) {
                    int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                    if (state == BluetoothDevice.BOND_NONE) {
                        unregisterReceiver(this);
                        sendEmptyMessage(CODE_CONNECT_BOND_FAILURE);
                    } else if (state == BluetoothDevice.BOND_BONDED) {
                        unregisterReceiver(this);
                        // 配对成功
                        connectDevice(device);
                    } else if (state == BluetoothDevice.BOND_BONDING) {
                    }
                }
            }
        };
        // 避免由于多次，先解除
        unregisterReceiver(receiver);
        registerReceiver(receiver, new IntentFilter(ACTION_BOND_STATE_CHANGED));
        createBond(device);
    }

    private void createBond(BluetoothDevice bluetoothDevice) {
        if (Build.VERSION.SDK_INT >= 19) {
            bluetoothDevice.createBond();
        } else {
            try {
                Method m = bluetoothDevice.getClass().getMethod("createBond");
                m.invoke(bluetoothDevice);
            } catch (Exception var3) {
                var3.printStackTrace();
            }
        }

    }

    /**
     * 创建蓝牙 socket 连接,进行数据交互.
     *
     * @param device
     */
    private void connectDevice(BluetoothDevice device) {
        try {
            if (this.mAdapter.isDiscovering()) {
                this.mAdapter.cancelDiscovery();
            }
            if (this.mState == STATE_CONNECTED && isConnected(device)) {
                sendEmptyMessage(CODE_CONNECT_SUCCESS);
                return;
            }
            if (this.mState == STATE_CONNECTING && null != this.mSocket) {
                this.mSocket.close();
                this.mSocket = null;
            }

            resetBtThread();
            mConnectThread = new ConnectThread(device);
            mConnectThread.start();

        } catch (IOException var6) {
            var6.printStackTrace();

            try {
                this.mSocket.close();
            } catch (IOException var4) {
                // LOG.e("BluetoothService", "unable to closed() " + device.getAddress() + " socket during connection failure", var4);
            }

            sendEmptyMessage(CODE_CONNECT_FAILURE);
        }

        // this.removeMessages();
    }


    private void resetBtThread() {
        try {
            if (mConnectThread != null && mConnectThread.isAlive()) {
                mConnectThread.stopThread();
                mConnectThread.interrupt();
                mConnectThread = null;
            }
            synchronized (SPP_UUID) {
                if (mReadThread != null) {
                    if (mReadThread.isAlive()) {
                        mReadThread.interrupt();
                    }
                    mReadThread = null;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 指定蓝牙设备是否连接
     *
     * @param device
     * @return
     */
    public boolean isConnected(BluetoothDevice device) {
        return isSocketConnected() && this.mSocket.getRemoteDevice().equals(device);
    }

    /**
     * socket 是否是已经连接状态
     *
     * @return
     */
    public boolean isSocketConnected() {
        return this.mSocket != null && this.mSocket.isConnected();

    }

    /**
     * 是否已经有蓝牙设备建立了连接
     *
     * @return
     */
    public boolean isConnected() {
        try {
            return mDevice != null
                    && mDevice.getBondState() != BOND_NONE
                    && isConnected(mDevice)
                    && this.mState == 0;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean write(byte[] buffer) {
        // 记录此次发送的命令类型,为紧接着返回的接收校验做准备
        // 通用 SDK 基本上是外设协议
        DataMatcher.Matcher matcher = DataMatcher.getInstance().match(buffer);
        // 由于上面匹配过，获取刚刚匹配到的结果，该结果可以过滤 fefefefe,
        // 背夹内部程序可能有问题，需要无fefefefe的数据。
        byte[] bytes = DataMatcher.getInstance().getLastMatchedBytes();

        return send(bytes);
    }

   /* public boolean write(byte[] buffer, int cammandType) {
        DataMatcher.getInstance().match(cammandType);
        return send(buffer);
    }*/

    private boolean send(byte[] buffer) {
        if (!isConnected()) {
            return false;
        }
        clearReceivePool();
        try {
            this.mmOutStream.write(buffer);
            this.mmOutStream.flush();
            return true;
        } catch (Exception var3) {
            var3.printStackTrace();
            return false;
        }
    }

    public byte[] sendCommand(byte[] cmds) {
        return sendCommand(cmds, READ_TIME_OUT);
    }

    public byte[] sendCommand(byte[] cmds, int readTimeout) {
            boolean writeSuccess = write(cmds);
            if (!writeSuccess) {
                return null;
            }
            byte[] read = read(readTimeout);
            return read;
    }

    /**
     * 清空缓存池
     */
    private void clearReceivePool() {
        if (null != mReadThread) {
            mReadThread.byteArrayOutputStream.reset();
        }
    }

    public byte[] read(long timeOut) {
        byte[] bytes = receive(timeOut <= 0 ? READ_TIME_OUT : timeOut);
        clearReceivePool();
        return bytes;
    }

    private byte[] read() {
        if (null == mReadThread) {
            return null;
        }
        return mReadThread.byteArrayOutputStream.toByteArray();
    }

    private byte[] receive(long timeOut) {
        synchronized (this) {
            if (isConnected()) {
                boolean isCompeted = false;
                long startTime = System.currentTimeMillis();
                while (!isCompeted) {
                    byte[] result = DataMatcher.getInstance().matched()
                            ? DataMatcher.getInstance().readFrom(read())
                            : readWait();
                    if (result != null) {
                        return result;
                    }
                    isCompeted = System.currentTimeMillis() - startTime > timeOut;
                }
            }
            return null;
        }
    }


    /**
     * 不知道读取的是什么类型的报文，就默认等待3s再获取数据。
     * 通常等待时间越短越适合。但是经过测试，698执行时 会话协商验证 耗时较长，故此处设置为3s
     * 详情参见 read(InputStream stream ,byte[] bytes) 注释
     *
     * @return
     */
    private byte[] readWait() {
        return readWait(3000);
    }

    private byte[] readWait(long waitTime) {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return read();

    }


    public boolean close() {
        try {
            if (null != this.mSocket) {
                this.mSocket.close();
                // 主动触发蓝牙断开。
                sendEmptyMessage(CODE_DISCONNECT);
            }
        } catch (Exception ex) {
            ex.printStackTrace();

        }
        return true;
    }

    public class ReadThread extends Thread {
        /**
         * 作为缓冲区，用来接收蓝牙收到的字节流
         */
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();


        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            while (isSocketConnected()) {
                try {
                    int lenth = mmInStream.read(buffer);
                    if (lenth > 0) {
                        this.byteArrayOutputStream.write(buffer, 0, lenth);
                        this.byteArrayOutputStream.flush();
                    }
                    Log.d("BluetoothService", "len " + lenth + ", total_len = " + this.byteArrayOutputStream.size() + " 报文：" +
                            bytesToHexString(this.byteArrayOutputStream.toByteArray()));
                } catch (Exception var5) {
                    //   LOG.e("BluetoothService", "disconn!!", var5);
                    if (mSocket != null) {
                        try {
                            mSocket.close();
                        } catch (Exception var4) {
                            var4.printStackTrace();
                        }
                    }
                    sendEmptyMessage(CODE_DISCONNECT);
                    break;
                }
            }
        }

        public void reset() {
            // LOG.i("BluetoothService", "buffer reset");
            this.byteArrayOutputStream.reset();
        }


    }

    private class ConnectThread extends Thread {
        private final BluetoothDevice mmDevice;
        private volatile boolean stop;

        public ConnectThread(BluetoothDevice device) {
            this(device, true);
        }

        public ConnectThread(BluetoothDevice device, boolean isDeamon) {
            mmDevice = device;
            setDaemon(isDeamon);
        }

        public void stopThread() {
            stop = true;
            mState = STATE_NONE;
        }


        @Override
        public void run() {
            mState = STATE_CONNECTING;
            try {
                if (stop) {
                    return;
                }
                mSocket = Build.VERSION.SDK_INT > 10
                        ? mmDevice.createInsecureRfcommSocketToServiceRecord(SPP_UUID)
                        : mmDevice.createRfcommSocketToServiceRecord(SPP_UUID);

                if (stop) {
                    return;
                }
                // 2.尝试第二种方式
                if (mSocket == null) {
                    Method m = mmDevice.getClass().getMethod("createRfcommSocket", int.class);
                    for (int i = 1; i < 36; i++) {
                        mSocket = (BluetoothSocket) m.invoke(mmDevice, i);
                        if (mSocket != null || stop) {
                            break;
                        }
                    }
                }
                if (stop) {
                    return;
                }
                if (mSocket != null) {
                    mSocket.connect();
                }
                if (mSocket == null || stop) {
                    sendEmptyMessage(CODE_CONNECT_FAILURE);
                    return;
                }
                connected(mSocket);
            } catch (Exception e) {
                e.printStackTrace();
                sendEmptyMessage(CODE_CONNECT_FAILURE);
            }
        }
    }

    private void connected(BluetoothSocket mSocket) {
        synchronized (SPP_UUID) {
            try {
                mmInStream = mSocket.getInputStream();
                mmOutStream = mSocket.getOutputStream();
                this.mReadThread = new ReadThread();
                this.mReadThread.start();
                sendEmptyMessage(CODE_CONNECT_SUCCESS);
            } catch (IOException e) {
                //KLog.e(e);
                sendEmptyMessage(CODE_CONNECT_FAILURE);
            }
        }
    }


}
