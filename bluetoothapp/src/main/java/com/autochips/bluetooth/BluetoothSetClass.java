package com.autochips.bluetooth;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.autochips.bluetoothservice.IBluetoothSetFunctionInterface;
import com.autochips.bluetoothservice.IBluetoothSetListenerInterface;


public class BluetoothSetClass {

    public static final String BLUETOOTHSET_CLASS_TAG = "BluetoothSetClass";

    public static final int BLUETOOTH_STATE_ON = 1;
    public static final int BLUETOOTH_STATE_OFF = 3;
    public final static int BLUETOOTH_SCAN_START = 4; // 开始搜索
    public final static int BLUETOOTH_SCAN_END = 5; // 搜索完成
    public final static int BLUETOOTH_PAIR_REQUEST = 6; // 配对请求

    public final static int BLUETOOTH_SERVICE_READY = 0; // binder service OK

    private static Context mContext;

    public interface onListChangeListener {
        void onListener(int what, int arg1, int arg2);
        void onIntent(Intent intent);
    }

    private static onListChangeListener mListener;
    private static IBluetoothSetFunctionInterface mIBluetoothSetFunction;

    private static ServiceConnection mServiceConnect = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(BLUETOOTHSET_CLASS_TAG, "onServiceConnected");
            mIBluetoothSetFunction = IBluetoothSetFunctionInterface.Stub.asInterface(service);
            try {
                mIBluetoothSetFunction.setLinstener(mIBluetoothSetListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            if (mListener != null) {
                mListener.onListener(BLUETOOTH_SERVICE_READY, 0, 0);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(BLUETOOTHSET_CLASS_TAG, "onServiceDisconnected");
            mIBluetoothSetFunction = null;
        }
    };

    private static IBluetoothSetListenerInterface.Stub mIBluetoothSetListener = new IBluetoothSetListenerInterface.Stub() {
        @Override
        public void onMessage(int what, int arg1, int arg2) throws RemoteException {
            Log.d(BLUETOOTHSET_CLASS_TAG, "onMessage what = " + what);
            
            if (mListener != null) {
                mListener.onListener(what, arg1, arg2);
            }
        }

        @Override
        public void onIntent(Intent intent) throws RemoteException {
            Log.d(BLUETOOTHSET_CLASS_TAG, "onIntent");
            if (mListener != null) {
                mListener.onIntent(intent);
            }
        }
    };

    public static void intiBluetoothSet(Context context, onListChangeListener lintener) {
        Log.d(BLUETOOTHSET_CLASS_TAG, "intiBluetoothSet start");

        mListener = lintener;
        mContext = context;
        Intent intent = new Intent();
        intent.setPackage("com.autochips.bluetoothservice");
        intent.setAction(IBluetoothSetFunctionInterface.class.getName());
        mContext.bindService(intent, mServiceConnect, Context.BIND_AUTO_CREATE);

        Log.d(BLUETOOTHSET_CLASS_TAG, "intiBluetoothSet end");
    }

    public static void deinitBluetoothSet() {
        Log.d(BLUETOOTHSET_CLASS_TAG, "deinitBluetoothSet start");

        mContext.unbindService(mServiceConnect);
        mListener = null;

        Log.d(BLUETOOTHSET_CLASS_TAG, "deinitBluetoothSet end");
    }

    public void openBluetooth() {
        Log.d(BLUETOOTHSET_CLASS_TAG, "openBluetooth start");

        if (null == mIBluetoothSetFunction) {
            Log.d(BLUETOOTHSET_CLASS_TAG, "mIBluetoothSetFunction is null");
            return;
        }
        try {
            mIBluetoothSetFunction.openBluetooth();
        }catch (RemoteException e) {
            e.printStackTrace();
        }

        Log.d(BLUETOOTHSET_CLASS_TAG, "openBluetooth start");
    }

    public void closeBluetooth() {
        Log.d(BLUETOOTHSET_CLASS_TAG, "closeBluetooth start");

        if (null == mIBluetoothSetFunction) {
            Log.d(BLUETOOTHSET_CLASS_TAG, "mIBluetoothSetFunction is null");
            return;
        }
        try {
            mIBluetoothSetFunction.closeBluetooth();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        Log.d(BLUETOOTHSET_CLASS_TAG, "closeBluetooth end");
    }

    public boolean isBluetoothOn() {
        Log.d(BLUETOOTHSET_CLASS_TAG, "isBluetoothOn start");

        if (null == mIBluetoothSetFunction) {
            Log.d(BLUETOOTHSET_CLASS_TAG, "mIBluetoothSetFunction is null");
            return false;
        }
        try {
            return mIBluetoothSetFunction.isBluetoothOn();
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void startSearch() {
        Log.d(BLUETOOTHSET_CLASS_TAG, "startSearch start");

        if (null == mIBluetoothSetFunction) {
            Log.d(BLUETOOTHSET_CLASS_TAG, "mIBluetoothSetFunction is null");
            return;
        }
        try {
            mIBluetoothSetFunction.startSearch();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        Log.d(BLUETOOTHSET_CLASS_TAG, "startSearch end");
    }

    public void stopSearch() {
        Log.d(BLUETOOTHSET_CLASS_TAG, "stopSearch start");

        if (null == mIBluetoothSetFunction) {
            Log.d(BLUETOOTHSET_CLASS_TAG, "mIBluetoothSetFunction is null");
            return;
        }
        try {
            mIBluetoothSetFunction.stopSearch();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        Log.d(BLUETOOTHSET_CLASS_TAG, "stopSearch end");
    }

    public void connectDevice(String address) {
        Log.d(BLUETOOTHSET_CLASS_TAG, "connectDevice start");

        if (null == mIBluetoothSetFunction) {
            Log.d(BLUETOOTHSET_CLASS_TAG, "mIBluetoothSetFunction is null");
            return;
        }
        try {
            mIBluetoothSetFunction.connectDevice(address);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        Log.d(BLUETOOTHSET_CLASS_TAG, "connectDevice end");
    }

    public void disConnectDevice(String address) {
        Log.d(BLUETOOTHSET_CLASS_TAG, "disConnectDevice start");

        if (null == mIBluetoothSetFunction) {
            Log.d(BLUETOOTHSET_CLASS_TAG, "mIBluetoothSetFunction is null");
            return;
        }
        try {
            mIBluetoothSetFunction.disconnectDevice(address);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        Log.d(BLUETOOTHSET_CLASS_TAG, "disConnectDevice end");
    }

    public void removeDevice(String address) {
        Log.d(BLUETOOTHSET_CLASS_TAG, "removeDevice start");

        if (null == mIBluetoothSetFunction) {
            Log.d(BLUETOOTHSET_CLASS_TAG, "mIBluetoothSetFunction is null");
            return;
        }
        try {
            mIBluetoothSetFunction.removeDevice(address);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        Log.d(BLUETOOTHSET_CLASS_TAG, "removeDevice end");
    }

    public void getBondDevice() {
        Log.d(BLUETOOTHSET_CLASS_TAG, "getBondDevice start");

        if (null == mIBluetoothSetFunction) {
            Log.d(BLUETOOTHSET_CLASS_TAG, "mIBluetoothSetFunction is null");
            return;
        }
        try {
            mIBluetoothSetFunction.getBondDevices();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        Log.d(BLUETOOTHSET_CLASS_TAG, "getBondDevice end");
    }

    public void setPairRequest(boolean flag) {
        Log.d(BLUETOOTHSET_CLASS_TAG, "setPairRequest start");

        if (null == mIBluetoothSetFunction) {
            Log.d(BLUETOOTHSET_CLASS_TAG, "mIBluetoothSetFunction is null");
            return;
        }
        try {
            mIBluetoothSetFunction.setPairRequest(flag);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        Log.d(BLUETOOTHSET_CLASS_TAG, "setPairRequest end");
    }

    public void setAutoConnect(boolean flag) {
        Log.d(BLUETOOTHSET_CLASS_TAG, "setAutoConnect start");

        if (null == mIBluetoothSetFunction) {
            Log.d(BLUETOOTHSET_CLASS_TAG, "mIBluetoothSetFunction is null");
            return;
        }
        try {
            mIBluetoothSetFunction.setAutoConnect(flag);
        }catch (RemoteException e) {
            e.printStackTrace();
        }
        Log.d(BLUETOOTHSET_CLASS_TAG, "setAutoConnect end");
        return;
    }

    public boolean getAutoConnect() {
        Log.d(BLUETOOTHSET_CLASS_TAG, "getAutoConnect start");
        boolean flag = false;
        if (null == mIBluetoothSetFunction) {
            Log.d(BLUETOOTHSET_CLASS_TAG, "mIBluetoothSetFunction is null");
            return flag;
        }
        try {
            flag = mIBluetoothSetFunction.getAutoConnect();
        }catch (RemoteException e) {
            e.printStackTrace();
        }
        Log.d(BLUETOOTHSET_CLASS_TAG, "getAutoConnect end");
        return  flag;
    }
}
