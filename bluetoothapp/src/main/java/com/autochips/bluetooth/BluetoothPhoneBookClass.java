package com.autochips.bluetooth;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.autochips.bluetoothservice.IBluetoothPhoneBookInterface;
import com.autochips.bluetoothservice.IBluetoothSetListenerInterface;

public class BluetoothPhoneBookClass {
    public static final String BLUETOOTHPHONEBOOK_CLASS_TAG = "BluetoothPhoneBookClass";

    public final static int BLUETOOTH_SERVICE_READY = 255;
    public final static int BLUETOOTH_MSG_DISCONNECT = 100;
    public final static int BLUETOOTH_PHONEBOOK_DOWN_CONTACTS_INDEX = 0;
    public final static int BLUETOOTH_PHONEBOOK_DOWN_CALLHISTROY_INDEX = 1;
    public final static int BLUETOOTH_PHONEBOOK_DOWN_CONITICS_STATE = 2;
    public final static int BLUETOOTH_PHONEBOOK_DOWN_CALLHISTROY_STATE = 3;


    public interface onListChangeListener {
        void onListener(int what, int arg1, int arg2);
        void onIntent(Intent intent);
    }

    private static onListChangeListener mListener;
    private static IBluetoothPhoneBookInterface mIBluetoothPhoneBookFunction;

    private static IBluetoothSetListenerInterface.Stub mIBluetoothPhoneBookListener = new IBluetoothSetListenerInterface.Stub() {
        @Override
        public void onMessage(int what, int arg1, int arg2) throws RemoteException {
            Log.d(BLUETOOTHPHONEBOOK_CLASS_TAG, "onMessage what = " + what);
            if (mListener != null) {
                mListener.onListener(what, arg1, arg2);
            }
        }

        @Override
        public void onIntent(Intent intent) throws RemoteException {
            Log.d(BLUETOOTHPHONEBOOK_CLASS_TAG, "onIntent");
            if (mListener != null) {
                mListener.onIntent(intent);
            }
        }
    };

    private static ServiceConnection mServiceConnect = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(BLUETOOTHPHONEBOOK_CLASS_TAG, "onServiceConnected");
            mIBluetoothPhoneBookFunction = IBluetoothPhoneBookInterface.Stub.asInterface(service);
            try {
                mIBluetoothPhoneBookFunction.setLinstener(mIBluetoothPhoneBookListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            if (mListener != null) {
                mListener.onListener(BLUETOOTH_SERVICE_READY, 0, 0);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(BLUETOOTHPHONEBOOK_CLASS_TAG, "onServiceDisconnected");
            mIBluetoothPhoneBookFunction = null;
        }
    };

    public static void intiBluetoothPhoneBook(Context context, onListChangeListener lintener) {
        Log.d(BLUETOOTHPHONEBOOK_CLASS_TAG, "intiBluetoothSet start");

        mListener = lintener;
        Intent intent = new Intent();
        intent.setPackage("com.autochips.bluetoothservice");
        intent.setAction(IBluetoothPhoneBookInterface.class.getName());
        context.bindService(intent, mServiceConnect, Context.BIND_AUTO_CREATE);

        Log.d(BLUETOOTHPHONEBOOK_CLASS_TAG, "intiBluetoothSet end");
    }

    public void downloadContacts() {
        Log.d(BLUETOOTHPHONEBOOK_CLASS_TAG, "downloadContacts start");

        if (null == mIBluetoothPhoneBookFunction) {
            Log.d(BLUETOOTHPHONEBOOK_CLASS_TAG, "mIBluetoothPhoneCallInterface is null");
            return;
        }
        try {
            mIBluetoothPhoneBookFunction.downloadContacts();
        }catch (RemoteException e) {
            e.printStackTrace();
        }
        Log.d(BLUETOOTHPHONEBOOK_CLASS_TAG, "downloadContacts end");
    }

    public void stopDownloadContacts() {
        Log.d(BLUETOOTHPHONEBOOK_CLASS_TAG, "stopDownloadContacts start");

        if (null == mIBluetoothPhoneBookFunction) {
            Log.d(BLUETOOTHPHONEBOOK_CLASS_TAG, "mIBluetoothPhoneCallInterface is null");
            return;
        }
        try {
            mIBluetoothPhoneBookFunction.stopDownloadContacts();
        }catch (RemoteException e) {
            e.printStackTrace();
        }
        Log.d(BLUETOOTHPHONEBOOK_CLASS_TAG, "stopDownloadContacts end");
    }

    public void hfpCall(String number) {
        Log.d(BLUETOOTHPHONEBOOK_CLASS_TAG, "hfpCall start");

        if (null == mIBluetoothPhoneBookFunction) {
            Log.d(BLUETOOTHPHONEBOOK_CLASS_TAG, "mIBluetoothPhoneCallInterface is null");
            return;
        }
        try {
            mIBluetoothPhoneBookFunction.hfpCall(number);
        }catch (RemoteException e) {
            e.printStackTrace();
        }
        Log.d(BLUETOOTHPHONEBOOK_CLASS_TAG, "hfpCall end");
    }

    public int getContactsDownState() {
        Log.d(BLUETOOTHPHONEBOOK_CLASS_TAG, "getContactsDownFinsh start");
        int state = 1;
        if (null == mIBluetoothPhoneBookFunction) {
            Log.d(BLUETOOTHPHONEBOOK_CLASS_TAG, "mIBluetoothPhoneCallInterface is null");
            return state;
        }
        try {
            state = mIBluetoothPhoneBookFunction.getContactsDownState();
        }catch (RemoteException e) {
            e.printStackTrace();
        }
        Log.d(BLUETOOTHPHONEBOOK_CLASS_TAG, "getContactsDownFinsh end");
        return  state;
    }

    public int getContCallhistroyState() {
        Log.d(BLUETOOTHPHONEBOOK_CLASS_TAG, "getContCallhistroyState start");
        int state = 1;
        if (null == mIBluetoothPhoneBookFunction) {
            Log.d(BLUETOOTHPHONEBOOK_CLASS_TAG, "mIBluetoothPhoneCallInterface is null");
            return state;
        }
        try {
            state = mIBluetoothPhoneBookFunction.getContCallhistroyState();
        }catch (RemoteException e) {
            e.printStackTrace();
        }
        Log.d(BLUETOOTHPHONEBOOK_CLASS_TAG, "getContCallhistroyState end");
        return  state;
    }

    public int getContactsDownNumber() {
        Log.d(BLUETOOTHPHONEBOOK_CLASS_TAG, "getContactsDownNumber start");
        int count  = 0;
        if (null == mIBluetoothPhoneBookFunction) {
            Log.d(BLUETOOTHPHONEBOOK_CLASS_TAG, "mIBluetoothPhoneCallInterface is null");
            return count;
        }
        try {
            count = mIBluetoothPhoneBookFunction.getContactsDownNumber();
        }catch (RemoteException e) {
            e.printStackTrace();
        }
        Log.d(BLUETOOTHPHONEBOOK_CLASS_TAG, "getContactsDownNumber end");
        return  count;
    }

    public int getCallhistroyNumber() {
        Log.d(BLUETOOTHPHONEBOOK_CLASS_TAG, "getCallhistroyNumber start");
        int count  = 0;
        if (null == mIBluetoothPhoneBookFunction) {
            Log.d(BLUETOOTHPHONEBOOK_CLASS_TAG, "mIBluetoothPhoneCallInterface is null");
            return count;
        }
        try {
            count = mIBluetoothPhoneBookFunction.getCallhistroyNumber();
        }catch (RemoteException e) {
            e.printStackTrace();
        }
        Log.d(BLUETOOTHPHONEBOOK_CLASS_TAG, "getCallhistroyNumber end");
        return  count;
    }

    public String getRemoteAddr() {
        Log.d(BLUETOOTHPHONEBOOK_CLASS_TAG, "getRemoteAddr start");
        String addr = null;
        if (null == mIBluetoothPhoneBookFunction) {
            Log.d(BLUETOOTHPHONEBOOK_CLASS_TAG, "mIBluetoothPhoneCallInterface is null");
            return addr;
        }
        try {
            addr = mIBluetoothPhoneBookFunction.getRemoteAddr();
        }catch (RemoteException e) {
            e.printStackTrace();
        }
        Log.d(BLUETOOTHPHONEBOOK_CLASS_TAG, "getRemoteAddr end");
        return  addr;
    }

    public void setAutoAcept(boolean flag) {
        Log.d(BLUETOOTHPHONEBOOK_CLASS_TAG, "setAutoAcept start");

        if (null == mIBluetoothPhoneBookFunction) {
            Log.d(BLUETOOTHPHONEBOOK_CLASS_TAG, "mIBluetoothPhoneCallInterface is null");
            return;
        }
        try {
            mIBluetoothPhoneBookFunction.setAutoAcept(flag);
        }catch (RemoteException e) {
            e.printStackTrace();
        }
        Log.d(BLUETOOTHPHONEBOOK_CLASS_TAG, "setAutoAcept end");
        return;
    }

    public boolean getAutoAcept() {
        Log.d(BLUETOOTHPHONEBOOK_CLASS_TAG, "getAutoAcept start");
        boolean flag = false;
        if (null == mIBluetoothPhoneBookFunction) {
            Log.d(BLUETOOTHPHONEBOOK_CLASS_TAG, "mIBluetoothPhoneCallInterface is null");
            return flag;
        }
        try {
            flag = mIBluetoothPhoneBookFunction.getAutoAcept();
        }catch (RemoteException e) {
            e.printStackTrace();
        }
        Log.d(BLUETOOTHPHONEBOOK_CLASS_TAG, "getAutoAcept end");
        return  flag;
    }
}
