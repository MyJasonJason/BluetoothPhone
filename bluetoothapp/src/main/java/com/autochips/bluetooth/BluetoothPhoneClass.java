package com.autochips.bluetooth;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.autochips.bluetoothservice.IBluetoothPhoneCallInterface;
import com.autochips.bluetoothservice.IBluetoothSetListenerInterface;

public class BluetoothPhoneClass {

    public static final String BLUETOOTHPHONE_CLASS_TAG = "BluetoothPhoneClass";

    public final static int BLUETOOTH_MSG_DISCONNECT = 100;

    public final static int BLUETOOTH_SERVICE_READY = 0;
    public final static int BLUETOOTH_UPDATA_SCO_STATE = 1;

    public final static int BLUETOOTH_PHONE_CALL_STATE_ACTIVE = 0;
    public final static int BLUETOOTH_PHONE_CALL_STATE_DIALING = 2;
    public final static int BLUETOOTH_PHONE_CALL_STATE_ALERTING = 3;
    public final static int BLUETOOTH_PHONE_CALL_STATE_INCOMING = 4;
    public final static int BLUETOOTH_PHONE_CALL_STATE_TERMINATED= 7;
    public final static int BLUETOOTH_PHONE_CALL_STATE_NONE = 255;

    public final static int BLUETOOTH_PHONE_SCO_CONNECT = 2;
    public final static int BLUETOOTH_PHONE_SCO_DISCONNECT = 0;

    private static Context mContext;

    public interface onListChangeListener {
        void onListener(int what, int arg1, int arg2);
        void onIntent(Intent intent);
    }

    private static onListChangeListener mListener;
    private static IBluetoothPhoneCallInterface mIBluetoothPhoneCallInterface;

    private static ServiceConnection mServiceConnect = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(BLUETOOTHPHONE_CLASS_TAG, "onServiceConnected");
            mIBluetoothPhoneCallInterface = IBluetoothPhoneCallInterface.Stub.asInterface(service);
            try {
                mIBluetoothPhoneCallInterface.setLinstener(mIBluetoothPhoneCallListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            if (mListener != null) {
                mListener.onListener(BLUETOOTH_SERVICE_READY, 0, 0);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(BLUETOOTHPHONE_CLASS_TAG, "onServiceDisconnected");
            mIBluetoothPhoneCallInterface = null;
        }
    };

    private static IBluetoothSetListenerInterface.Stub mIBluetoothPhoneCallListener = new IBluetoothSetListenerInterface.Stub() {
        @Override
        public void onMessage(int what, int arg1, int arg2) throws RemoteException {
            Log.d(BLUETOOTHPHONE_CLASS_TAG, "onMessage what = " + what);
            if (mListener != null) {
                mListener.onListener(what, arg1, arg2);
            }
        }

        @Override
        public void onIntent(Intent intent) throws RemoteException {
            Log.d(BLUETOOTHPHONE_CLASS_TAG, "onIntent");
            if (mListener != null) {
                mListener.onIntent(intent);
            }
        }
    };

    public static void intiBluetoothPhoneCall(Context context, onListChangeListener lintener) {
        Log.d(BLUETOOTHPHONE_CLASS_TAG, "intiBluetoothSet start");

        mListener = lintener;
        mContext = context;
        Intent intent = new Intent();
        intent.setPackage("com.autochips.bluetoothservice");
        intent.setAction(IBluetoothPhoneCallInterface.class.getName());
        mContext.bindService(intent, mServiceConnect, Context.BIND_AUTO_CREATE);

        Log.d(BLUETOOTHPHONE_CLASS_TAG, "intiBluetoothSet end");
    }

    public static void deinitBluetoothPhoneCall() {
        mContext.unbindService(mServiceConnect);
    }

    public void acceptCall() {
        Log.d(BLUETOOTHPHONE_CLASS_TAG, "acceptCall start");

        if (null == mIBluetoothPhoneCallInterface) {
            Log.d(BLUETOOTHPHONE_CLASS_TAG, "mIBluetoothPhoneCallInterface is null");
            return;
        }
        try {
            mIBluetoothPhoneCallInterface.acceptCall();
        }catch (RemoteException e) {
            e.printStackTrace();
        }
        Log.d(BLUETOOTHPHONE_CLASS_TAG, "acceptCall end");
    }

    public void rejectCall() {
        Log.d(BLUETOOTHPHONE_CLASS_TAG, "rejectCall start");

        if (null == mIBluetoothPhoneCallInterface) {
            Log.d(BLUETOOTHPHONE_CLASS_TAG, "mIBluetoothPhoneCallInterface is null");
            return;
        }
        try {
            mIBluetoothPhoneCallInterface.rejectCall();
        }catch (RemoteException e) {
            e.printStackTrace();
        }
        Log.d(BLUETOOTHPHONE_CLASS_TAG, "rejectCall end");
    }

    public void terminateCall() {
        Log.d(BLUETOOTHPHONE_CLASS_TAG, "terminateCall start");

        if (null == mIBluetoothPhoneCallInterface) {
            Log.d(BLUETOOTHPHONE_CLASS_TAG, "mIBluetoothPhoneCallInterface is null");
            return;
        }
        try {
            mIBluetoothPhoneCallInterface.terminateCall();
        }catch (RemoteException e) {
            e.printStackTrace();
        }
        Log.d(BLUETOOTHPHONE_CLASS_TAG, "terminateCall end");
    }

    public void sendDTMFCode(byte dtmfCode) {
        Log.d(BLUETOOTHPHONE_CLASS_TAG, "sendDTMFCode start");

        if (null == mIBluetoothPhoneCallInterface) {
            Log.d(BLUETOOTHPHONE_CLASS_TAG, "mIBluetoothPhoneCallInterface is null");
            return;
        }
        try {
            mIBluetoothPhoneCallInterface.sendDTMFCode(dtmfCode);
        }catch (RemoteException e) {
            e.printStackTrace();
        }
        Log.d(BLUETOOTHPHONE_CLASS_TAG, "sendDTMFCode end");
    }

    public void switchAudioMode(boolean towardsAG) {
        Log.d(BLUETOOTHPHONE_CLASS_TAG, "switchAudioMode start");

        if (null == mIBluetoothPhoneCallInterface) {
            Log.d(BLUETOOTHPHONE_CLASS_TAG, "mIBluetoothPhoneCallInterface is null");
            return;
        }
        try {
            mIBluetoothPhoneCallInterface.switchAudioMode(towardsAG);
        }catch (RemoteException e) {
            e.printStackTrace();
        }
        Log.d(BLUETOOTHPHONE_CLASS_TAG, "switchAudioMode end");
    }

    public boolean isMicMute() {
        Log.d(BLUETOOTHPHONE_CLASS_TAG, "isMicMute start");
        
        boolean flag = false;
        if (null == mIBluetoothPhoneCallInterface) {
            Log.d(BLUETOOTHPHONE_CLASS_TAG, "mIBluetoothPhoneCallInterface is null");
            return flag;
        }
        try {
            flag = mIBluetoothPhoneCallInterface.isMicMute();
        }catch (RemoteException e) {
            e.printStackTrace();
        }
        Log.d(BLUETOOTHPHONE_CLASS_TAG, "muteMic end");

        return flag;
    }

    public void muteMic(boolean mute) {
        Log.d(BLUETOOTHPHONE_CLASS_TAG, "muteMic start");

        if (null == mIBluetoothPhoneCallInterface) {
            Log.d(BLUETOOTHPHONE_CLASS_TAG, "mIBluetoothPhoneCallInterface is null");
            return;
        }
        try {
            mIBluetoothPhoneCallInterface.muteMic(mute);
        }catch (RemoteException e) {
            e.printStackTrace();
        }
        Log.d(BLUETOOTHPHONE_CLASS_TAG, "muteMic end");
    }

    public String getRemoteAddr() {
        Log.d(BLUETOOTHPHONE_CLASS_TAG, "getRemoteAddr start");
        String addr = null;
        if (null == mIBluetoothPhoneCallInterface) {
            Log.d(BLUETOOTHPHONE_CLASS_TAG, "mIBluetoothPhoneCallInterface is null");
            return addr;
        }
        try {
            addr = mIBluetoothPhoneCallInterface.getRemoteAddr();
        }catch (RemoteException e) {
            e.printStackTrace();
        }
        Log.d(BLUETOOTHPHONE_CLASS_TAG, "getRemoteAddr end");
        return  addr;
    }

}
