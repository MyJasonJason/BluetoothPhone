package com.autochips.bluetooth;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.autochips.bluetoothservice.IBluetoothA2dpFunctionInterface;
import com.autochips.bluetoothservice.IBluetoothSetListenerInterface;


public class BluetoothMusicClass {

    public static final String BLUETOOTHMUSIC_CLASS_TAG = "BluetoothMusicClass";

    public final static int BLUETOOTH_SERVICE_READY = 0;
    public final static int BLUETOOTH_MSG_DISCONNECT = 100;
    public final static int BLUETOOTH_MSG_AVRCP_CONNECT = 101;

    public final static int MUSIC_STATE_STOP = 0;
    public final static int MUSIC_STATE_PLAY = 1;
    public final static int MUSIC_STATE_PAUSE = 2;

    private static Context mContext;

    public interface onListChangeListener {
        void onListener(int what, int arg1, int arg2);
        void onIntent(Intent intent);
    }

    private static onListChangeListener mListener;
    private static IBluetoothA2dpFunctionInterface mIBluetoothMusicFunction;

    private static ServiceConnection mServiceConnect = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(BLUETOOTHMUSIC_CLASS_TAG, "onServiceConnected");
            mIBluetoothMusicFunction = IBluetoothA2dpFunctionInterface.Stub.asInterface(service);
            try {
                mIBluetoothMusicFunction.setLinstener(mIBluetoothMusicListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            if (mListener != null) {
                mListener.onListener(BLUETOOTH_SERVICE_READY, 0, 0);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(BLUETOOTHMUSIC_CLASS_TAG, "onServiceDisconnected");
            mIBluetoothMusicFunction = null;
        }
    };

    private static IBluetoothSetListenerInterface.Stub mIBluetoothMusicListener = new IBluetoothSetListenerInterface.Stub() {
        @Override
        public void onMessage(int what, int arg1, int arg2) throws RemoteException {
            Log.d(BLUETOOTHMUSIC_CLASS_TAG, "onMessage what = " + what);
            if (mListener != null) {
                mListener.onListener(what, arg1, arg2);
            }
        }

        @Override
        public void onIntent(Intent intent) throws RemoteException {
            Log.d(BLUETOOTHMUSIC_CLASS_TAG, "onIntent");
            if (mListener != null) {
                mListener.onIntent(intent);
            }
        }
    };

    public static void intiBluetoothMusic(Context context, onListChangeListener lintener) {
        Log.d(BLUETOOTHMUSIC_CLASS_TAG, "intiBluetoothSet start");

        mListener = lintener;
        mContext = context;
        Intent intent = new Intent();
        intent.setPackage("com.autochips.bluetoothservice");
        intent.setAction(IBluetoothA2dpFunctionInterface.class.getName());
        mContext.bindService(intent, mServiceConnect, Context.BIND_AUTO_CREATE);

        Log.d(BLUETOOTHMUSIC_CLASS_TAG, "intiBluetoothSet end");
    }

    public static void deinitBluetoothMusic() {
        Log.d(BLUETOOTHMUSIC_CLASS_TAG, "deinitBluetoothMusic start");

        mContext.unbindService(mServiceConnect);
        mListener = null;

        Log.d(BLUETOOTHMUSIC_CLASS_TAG, "deinitBluetoothMusic end");
    }

    public void play() {
        Log.d(BLUETOOTHMUSIC_CLASS_TAG, "play start");

        if (null == mIBluetoothMusicFunction) {
            Log.d(BLUETOOTHMUSIC_CLASS_TAG, "mIBluetoothMusicFunction is null");
            return;
        }
        try {
            mIBluetoothMusicFunction.play();
        }catch (RemoteException e) {
            e.printStackTrace();
        }

        Log.d(BLUETOOTHMUSIC_CLASS_TAG, "play start");
    }

    public void pause() {
        Log.d(BLUETOOTHMUSIC_CLASS_TAG, "pause start");

        if (null == mIBluetoothMusicFunction) {
            Log.d(BLUETOOTHMUSIC_CLASS_TAG, "mIBluetoothMusicFunction is null");
            return;
        }
        try {
            mIBluetoothMusicFunction.pause();
        }catch (RemoteException e) {
            e.printStackTrace();
        }

        Log.d(BLUETOOTHMUSIC_CLASS_TAG, "pause start");
    }

    public void next() {
        Log.d(BLUETOOTHMUSIC_CLASS_TAG, "next start");

        if (null == mIBluetoothMusicFunction) {
            Log.d(BLUETOOTHMUSIC_CLASS_TAG, "mIBluetoothMusicFunction is null");
            return;
        }
        try {
            mIBluetoothMusicFunction.next();
        }catch (RemoteException e) {
            e.printStackTrace();
        }

        Log.d(BLUETOOTHMUSIC_CLASS_TAG, "next start");
    }

    public void prev() {
        Log.d(BLUETOOTHMUSIC_CLASS_TAG, "prev start");

        if (null == mIBluetoothMusicFunction) {
            Log.d(BLUETOOTHMUSIC_CLASS_TAG, "mIBluetoothMusicFunction is null");
            return;
        }
        try {
            mIBluetoothMusicFunction.prev();
        }catch (RemoteException e) {
            e.printStackTrace();
        }

        Log.d(BLUETOOTHMUSIC_CLASS_TAG, "prev start");
    }

    public String getTitle() {
        Log.d(BLUETOOTHMUSIC_CLASS_TAG, "getTitle start");

        String title = null;

        if (null == mIBluetoothMusicFunction) {
            Log.d(BLUETOOTHMUSIC_CLASS_TAG, "mIBluetoothMusicFunction is null");
            return title;
        }
        try {
            title = mIBluetoothMusicFunction.getTitle();
        }catch (RemoteException e) {
            e.printStackTrace();
        }

        Log.d(BLUETOOTHMUSIC_CLASS_TAG, "getTitle end title is " + title);
        return title;
    }

    public String getArtist() {
        Log.d(BLUETOOTHMUSIC_CLASS_TAG, "getArtist start");

        String artist = null;

        if (null == mIBluetoothMusicFunction) {
            Log.d(BLUETOOTHMUSIC_CLASS_TAG, "mIBluetoothMusicFunction is null");
            return artist;
        }
        try {
            artist = mIBluetoothMusicFunction.getArtist();
        }catch (RemoteException e) {
            e.printStackTrace();
        }

        Log.d(BLUETOOTHMUSIC_CLASS_TAG, "getArtist end artist is " + artist);
        return artist;
    }

    public String getAlbum() {
        Log.d(BLUETOOTHMUSIC_CLASS_TAG, "getAlbum start");

        String album = null;

        if (null == mIBluetoothMusicFunction) {
            Log.d(BLUETOOTHMUSIC_CLASS_TAG, "mIBluetoothMusicFunction is null");
            return album;
        }
        try {
            album = mIBluetoothMusicFunction.getAlbum();
        }catch (RemoteException e) {
            e.printStackTrace();
        }

        Log.d(BLUETOOTHMUSIC_CLASS_TAG, "getAlbum end artist is " + album);
        return album;
    }

    public byte getPlayState() {
        Log.d(BLUETOOTHMUSIC_CLASS_TAG, "getPlayState start");

        byte state = 0;

        if (null == mIBluetoothMusicFunction) {
            Log.d(BLUETOOTHMUSIC_CLASS_TAG, "mIBluetoothMusicFunction is null");
            return state;
        }
        try {
            state = mIBluetoothMusicFunction.getPlayState();
        }catch (RemoteException e) {
            e.printStackTrace();
        }

        Log.d(BLUETOOTHMUSIC_CLASS_TAG, "getPlayState end state is " + state);
        return state;
    }

    public int getSongLen() {
        Log.d(BLUETOOTHMUSIC_CLASS_TAG, "getSongLen start");

        int length = 0;

        if (null == mIBluetoothMusicFunction) {
            Log.d(BLUETOOTHMUSIC_CLASS_TAG, "mIBluetoothMusicFunction is null");
            return length;
        }
        try {
            length = mIBluetoothMusicFunction.getSongLen();
        }catch (RemoteException e) {
            e.printStackTrace();
        }

        Log.d(BLUETOOTHMUSIC_CLASS_TAG, "getSongLen end state is " + length);
        return length;
    }

    public int getSongPos() {
        Log.d(BLUETOOTHMUSIC_CLASS_TAG, "getSongPos start");

        int pos = 0;

        if (null == mIBluetoothMusicFunction) {
            Log.d(BLUETOOTHMUSIC_CLASS_TAG, "mIBluetoothMusicFunction is null");
            return pos;
        }
        try {
            pos = mIBluetoothMusicFunction.getSongPos();
        }catch (RemoteException e) {
            e.printStackTrace();
        }

        Log.d(BLUETOOTHMUSIC_CLASS_TAG, "getSongPos end state is " + pos);
        return pos;
    }

    public void setPlayerState(boolean flag) {
        Log.d(BLUETOOTHMUSIC_CLASS_TAG, "setPlayerState start");


        if (null == mIBluetoothMusicFunction) {
            Log.d(BLUETOOTHMUSIC_CLASS_TAG, "mIBluetoothMusicFunction is null");
            return;
        }
        try {
            mIBluetoothMusicFunction.setPlayerState(flag);
        }catch (RemoteException e) {
            e.printStackTrace();
        }

        Log.d(BLUETOOTHMUSIC_CLASS_TAG, "setPlayerState end");
    }


}
