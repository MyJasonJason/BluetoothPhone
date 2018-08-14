package com.autochips.bluetoothservice;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadsetClient;
import android.bluetooth.BluetoothHeadsetClientCall;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.autochips.bluetooth.A2dpSinkProfile;
import com.autochips.bluetooth.AvrcpControllerProfile;
import com.autochips.bluetooth.AvrcpCtPlayerUtility;
import com.autochips.bluetooth.BluetoothCallback;
import com.autochips.bluetooth.BluetoothEventManager;
import com.autochips.bluetooth.BluetoothPBManager;
import com.autochips.bluetooth.BluetoothPbRecord;
import com.autochips.bluetooth.CachedBluetoothDevice;
import com.autochips.bluetooth.CachedBluetoothDeviceManager;
import com.autochips.bluetooth.HeadsetClientProfile;
import com.autochips.bluetooth.LocalBluetoothAdapter;
import com.autochips.bluetooth.LocalBluetoothManager;
import com.autochips.bluetooth.LocalBluetoothProfile;
import com.autochips.bluetooth.LocalBluetoothProfileManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ICManager.ICManager;
import ICommon.ICMessage;
import ICommon.ICSourceInfo;


public class BluetoothService extends Service {

    public final static String BLUETOOTH_SERVICE_TAG = "BluetoothService_App";
    public final static String BLUETOOTH_VERSION = "atc_bluetooth_20180615_01";
    public final static int BLUETOOTH_MSG_DISCONNECT = 100;
    public final static int BLUETOOTH_MSG_AVRCP_CONNECT = 101;

    private ICManager mICManager = null;

    public final static int BLUETOOTH_SERVICE_ERR = -1;

    public final static int BLUETOOTH_STATE_TURNING_ON = 0; // 蓝牙正在打开
    public final static int BLUETOOTH_STATE_ON = 1; // 蓝牙打开
    public final static int BLUETOOTH_STATE_TURNING_OFF = 2; // 蓝牙正在关闭
    public final static int BLUETOOTH_STATE_OFF = 3; // 蓝牙关闭
    public final static int BLUETOOTH_SCAN_START = 4; // 开始搜索
    public final static int BLUETOOTH_SCAN_END = 5; // 搜索完成
    public final static int BLUETOOTH_PAIR_REQUEST = 6; // 配对请求

    private final static int BLUETOOTH_PROFILE_AVRCPCT = 12; // avrcp profile
    private final static int BLUETOOTH_PROFILE_A2DP = 11; // a2dp profile
    private final static int BLUETOOTH_PROFILE_HEADSET_CLIENT = 16; // HFP profile

    private final static int BLUETOOTH_MSG_HANDLE_BONDED_DEVICE = 1; // 获取已配对的列表
    private final static int BLUETOOTH_MSG_HANDLE_ATTR_CHANGE_DEVICE = 2;
    private final static int BLUETOOTH_MSG_HANDLE_SERVICE_ATTACH = 3;
    private final static int BLUETOOTH_MSG_HANDLE_PROFILE_CHANGE = 4;
    private final static int BLUETOOTH_MSG_HANDLE_METADATA_UPDATA = 5;
    private final static int BLUETOOTH_MSG_HANDLE_PLAYSTATE_UPDATA = 6;
    private final static int BLUETOOTH_MSG_HANDLE_GEI_BONDDEVICE = 7;
    private final static int BLUETOOTH_MSG_HANDLE_DOWNLOADR_CONTACTS = 8;
    private final static int BLUETOOTH_MSG_HANDLE_DOWNLOADR_CALLHISTROY = 9;
    private final static int BLUETOOTH_MSG_HANDLE_STOPDOWN_CONTACIS = 10;
    private final static int BLUETOOTH_MSG_HANDLE_STOPDOWN_CALLHISTROY = 11;
    private final static int BLUETOOTH_MSG_AUTO_CONNECT = 12;
    private final static int BLUETOOTH_MSG_AUTO_ACEPT = 13;
    private final static int BLUETOOTH_MSG_START_SEARCH = 14;


    public final static String BLUETOOTH_DEVICE_ADDRESS = "device_address";
    public final static String BLUETOOTH_DEVICE_NAME = "device_name";
    public final static String BLUETOOTH_DEVICE_STATE = "device_state";
    public final static String BLUETOOTH_DEVICE_BONDED = "device_bonded";
    public final static String BLUETOOTH_DEVICE_NOBOND = "device_nobond";
    public final static String BLUETOOTH_DEVICE_STATE_DISCONNECT = "device_disconnect";
    public final static String BLUETOOTH_DEVICE_STATE_CONNECT = "device_connect";




    public LocalBluetoothManager mLocalBluetoothManager = null;
    private BluetoothEventManager mBluetoothEventManager = null;
    private LocalBluetoothProfileManager mLocalBluetoothProfileManager = null;
    private LocalBluetoothAdapter mLocalBluetoothAdapter = null;
    private A2dpSinkProfile mA2dpSinkProfile = null;
    private AvrcpControllerProfile mAvrcpCtProfile = null;
    private HeadsetClientProfile mHeadsetClientProfile = null;
    private CachedBluetoothDeviceManager mCachedBluetoothDevicesManager = null;

    private BluetoothSetFunction mIBluetoothSetFunction = null;
    private IBluetoothSetListenerInterface mIBluetoothSetListener = null;

    private ArrayList<HashMap<String, String>> mBluetoothDevices = new ArrayList<HashMap<String, String>>();
    private ArrayList<CachedBluetoothDevice> mBluetoothDeviceList = new ArrayList<CachedBluetoothDevice>();

    private BluetoothDevice mCurPairDevice = null;
    private boolean mIsBluetoothConnect = false;
    private String mStrBTName = null;
    private boolean mIsStartSearch = false;

    // BT Music
    private int mAvrcpCtProfileState = BluetoothProfile.STATE_DISCONNECTED;
    private int mA2dpSinkProfileState = BluetoothProfile.STATE_DISCONNECTED;
    private int mHfpProfileState = BluetoothProfile.STATE_DISCONNECTED;

    private final static String AVRCP_METADATA_TITLE = "title";
    private final static String AVRCP_METADATA_ARTIST = "artist";
    private final static String AVRCP_MEIADATA_ALBUM = "album";
    private final static String AVRCP_PLAY_STATE = "play_state";
    private final static String AVRCP_PLAY_SONG_LEN = "song_len";
    private final static String AVRCP_PLAY_SONG_POS = "song_pos";
    private final static String BLUETOOTH_MUSIC_INTENT_TYPE = "intent_type";
    private final static String BLUETOOTH_MUSIC_INTEN_TYPE_METADATACHANGE = "type_metadatachange";
    private final static String BLUETOOTH_MUSIC_INTEN_TYPE_PLAYSTATECHANGE = "type_playstatechange";

    private BluetoothA2dpFunction mIBluetoothA2dpFunction = null;
    private IBluetoothSetListenerInterface mIBluetoothA2dpListener = null;

    private String mStrTitle;
    private String mStrArtist;
    private String mStrAlbum;

    private byte mCurPlayState = AvrcpCtPlayerUtility.PLAYING;
    private byte mPlayState;
    private int mSonglen;
    private int mSongPos;
    private boolean mIsAudioFocus = false;



    //phone call
    private BluetoothPhoneCallFunction mIBluetoothPhoneCallFunction = null;

    private final static int BLUETOOTH_PHONECALL_CMD_CALL = 0;
    private final static int BLUETOOTH_PHONECALL_CMD_REJECT = 1;
    private final static int BLUETOOTH_PHONECALL_CMD_ACCEPT = 2;
    private final static int BLUETOOTH_PHONECALL_CMD_DTMFCODE = 3;
    private final static int BLUETOOTH_PHONECALL_CMD_SWITCHAUDIO = 4;
    private final static int BLUETOOTH_PHONECALL_CMD_TERMINATE = 6;

    private final static String BLUETOOTH_PHONECALL_CALL_TYPE = "phone_type";
    private final static String BLUETOOTH_PHONECALL_CALL_NUMBER = "phone_number";
    private final static String BLUETOOTH_PHONECALL_AUDIO_STATE = "phone_state";

    private final static int BLUETOOTH_PHONECALL_MSG_UPDATA_SCOSTATE = 1;

    private boolean mMuteMic = false;
    private boolean mIsShowActivity = false;
    private int mCallState = BluetoothHeadsetClientCall.CALL_STATE_ACTIVE;
    private String mCallNumber = null;
    private int mScoState = BluetoothHeadsetClient.STATE_AUDIO_CONNECTED;
    private boolean isPhoneCall = false;
    private ArrayList<HashMap<String, Object>> mCallList = new ArrayList<HashMap<String, Object>>();


    private IBluetoothSetListenerInterface mIBluetoothCallListener = null;

    // phonebook
    private final static int BLUETOOTHPHONEBOOK_MSG_DOWN_CONTATCS_INDEX = 0;
    private final static int BLUETOOTHPHONEBOOK_MSG_DOWN_CALLHISTROY_INDEX = 1;
    private final static int BLUETOOTHPHONEBOOK_MSG_DOWN_CONITICS_STATE = 2;
    private final static int BLUETOOTHPHONEBOOK_MSG_DOWN_CALLHISTROY_STATE = 3;

    private BluetoothPhoneBookFunction mIBluetoothPhoneBookFunction = null;
    private IBluetoothSetListenerInterface mIBluetoothPhoneBookListener = null;
    private BluetoothPBManager mBluetoothPBManager = null;

    private int mCurContactsDown = 0;
    private int mContactsDownState = BluetoothPbRecord.PB_IND_FINISH;
    private int mCurCallhistroyDown = 0;
    private int mCallhistroyDownState = BluetoothPbRecord.PB_IND_FINISH;
    private boolean isDownCallHistroy = true;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(BLUETOOTH_SERVICE_TAG, "onReceive = " + action);
            if (action.equals(BluetoothDevice.ACTION_PAIRING_REQUEST)) {
                onBluetoothPairRequest(context, intent);
            } else if (action.equals(intent.ACTION_BOOT_COMPLETED)) {
                setBTName();;
            } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                onBluetoothAclDisconnected(intent);
            }
        }
    };


    private Handler mServiceHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BLUETOOTH_MSG_HANDLE_BONDED_DEVICE:
                    break;

                case BLUETOOTH_MSG_HANDLE_ATTR_CHANGE_DEVICE:
                    CachedBluetoothDevice device = (CachedBluetoothDevice) msg.obj;
                    if (device != null) {
                        handleDeviceNameChange(device);
                    }
                    break;

                case BLUETOOTH_MSG_HANDLE_SERVICE_ATTACH:
                    regMetadataCallback();
                    regPhoneBookCallback();
                    break;

                case BLUETOOTH_MSG_HANDLE_PROFILE_CHANGE:
                    int profile_id = msg.arg1;
                    int profile_state = msg.arg2;
                    BluetoothDevice bluetootDevice = (BluetoothDevice)msg.obj;
                    if (BLUETOOTH_PROFILE_A2DP == profile_id) {
                        handleA2dpSinkStateChanged(bluetootDevice, profile_state);
                    } else if (BLUETOOTH_PROFILE_AVRCPCT == profile_id) {
                        handleAvrcpCtStateChanged(bluetootDevice, profile_state);
                    } else if (BLUETOOTH_PROFILE_HEADSET_CLIENT == profile_id) {
                        handleHfpStateChanged(bluetootDevice, profile_state);
                    }
                    break;

                case BLUETOOTH_MSG_HANDLE_GEI_BONDDEVICE:
                    List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
                    devices.addAll(mLocalBluetoothAdapter.getBondedDevices());
                    for (BluetoothDevice pairDevice : devices) {
                        CachedBluetoothDevice cacheDevice = mCachedBluetoothDevicesManager.findDevice(pairDevice);
                        if (cacheDevice != null) {
                            cacheDevice.registerCallback(mCacheDeviceCallback);
                        }

                        Intent intent = new Intent();
                        intent.putExtra(BLUETOOTH_DEVICE_ADDRESS, pairDevice.getAddress());
                        intent.putExtra(BLUETOOTH_DEVICE_NAME, pairDevice.getName());
                        intent.putExtra(BLUETOOTH_DEVICE_STATE, getBondDeviceState(cacheDevice));
                        handleIntentCallBack(intent);

                    }
                    break;

                case BLUETOOTH_MSG_HANDLE_DOWNLOADR_CONTACTS:
                    startDownLoadContacts();
                    break;

                case BLUETOOTH_MSG_HANDLE_DOWNLOADR_CALLHISTROY:
                    startDownLoadCallHistroy();
                    break;

                case BLUETOOTH_MSG_HANDLE_STOPDOWN_CALLHISTROY:
                    stopDownLoad();
                    break;

                case BLUETOOTH_MSG_HANDLE_STOPDOWN_CONTACIS:
                    stopDownLoad();
                    break;

                case BLUETOOTH_MSG_AUTO_CONNECT:
                    synchronized (getApplicationContext()) {
                        if (mLocalBluetoothManager != null) {
                            LocalBluetoothAdapter adapter = mLocalBluetoothManager.getBluetoothAdapter();
                            String addr = (String)msg.obj;
                            BluetoothDevice connectDevice = adapter.getRemoteDevice(addr);
                            CachedBluetoothDevice connectCacheDevice = mCachedBluetoothDevicesManager.findDevice(connectDevice);
                            if (connectCacheDevice != null && !connectCacheDevice.isConnected()) {
                                connectCacheDevice.connect();
                            }
                        } else {
                            this.removeMessages(BLUETOOTH_MSG_AUTO_CONNECT);
                            break;
                        }
                        Message delayMsg = Message.obtain();
                        delayMsg.what = BLUETOOTH_MSG_AUTO_CONNECT;
                        delayMsg.obj = msg.obj;
                        this.sendMessageDelayed(delayMsg, 10000);

                    }
                    break;

                case BLUETOOTH_MSG_AUTO_ACEPT:
                    bluetoothPhoneCallCmd(BLUETOOTH_PHONECALL_CMD_ACCEPT, null, (byte)0, false);
                    break;

                case BLUETOOTH_MSG_START_SEARCH:
                    mLocalBluetoothAdapter.startScanning(true);
                    break;
            }
        }
    };

    class ICManagerListener implements ICManager.ServiceListener {
        @Override
        public void onStateChange(int state) {
            switch (state) {
                case ICManager.SERVICE_CONNECTED:
                    ICMessage msg = ICMessage.obtain();
                    msg.setCMD(ICMessage.CMD_REGISTER_APP).setInteger(ICMessage.APP_BT);
                    mICManager.talkWithService(msg);

                    msg = ICMessage.obtain().setCMD(ICMessage.CMD_REQUEST_SET_APP_VERSION)
                                            .setStrings(BLUETOOTH_VERSION);
                    mICManager.talkWithService(msg);
                    break;

                case ICManager.SERVICE_DISCONNECTED:
                    break;
            }
        }

        @Override
        public ICMessage handleMessage(ICMessage icMessage) {
            ICMessage ret = new ICMessage();
            int cmd = icMessage.getCMD();
            Log.d(BLUETOOTH_SERVICE_TAG, "ICManagerListener handleMessage cmd = " + cmd);
            if (ICMessage.CMD_REQUEST_Get_STATES == cmd) {
                if (mIsBluetoothConnect) {
                    return icMessage.setInteger(ICSourceInfo.Connected);
                } else {
                    return icMessage.setInteger(ICSourceInfo.Disconnect);
                }
            } else if (ICMessage.CMD_SERVICE_FROM_OTHER_NOTIFY == cmd) {
                Bundle mData = icMessage.getData();
                Log.d(BLUETOOTH_SERVICE_TAG, "ICManagerListener handleMessage mData.getInt(\"KEY_DATA_a\") " + mData.getInt("KEY_DATA_a"));
                onCmdICMessage(mData.getInt("KEY_DATA_a"));
            } else if (ICMessage.CMD_REQUEST_GET_MEDIA_INFORMATION == cmd) {
                Bundle data = ret.getData();
                data.putInt(ICMessage.KEY_DATA_a, 0);
                data.putInt(ICMessage.KEY_DATA_b, 0);
                data.putInt(ICMessage.KEY_DATA_c, (mSongPos / 1000));
                data.putInt(ICMessage.KEY_DATA_d, (mSonglen / 1000));
                if(mStrTitle == null) {
                    data.putString(ICMessage.KEY_DATA_e, "unKnow");
                } else {
                    data.putString(ICMessage.KEY_DATA_e, mStrTitle);
                }

                if(mStrAlbum == null) {
                    data.putString(ICMessage.KEY_DATA_f, "unKnow");
                } else {
                    data.putString(ICMessage.KEY_DATA_f, mStrAlbum);
                }

                if(mStrArtist == null) {
                    data.putString(ICMessage.KEY_DATA_g, "unKnow");
                } else {
                    data.putString(ICMessage.KEY_DATA_g, mStrArtist);
                }
            }

            return ret;
        }
    }

    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChange = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            Log.d(BLUETOOTH_SERVICE_TAG, "mOnAudioFocusChange focusChange IS " + focusChange);
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    mIsAudioFocus = true;

                    if (mCurPlayState == AvrcpCtPlayerUtility.PLAYING) {
                        if(AvrcpCtPlayerUtility.PLAYING != mPlayState) {
                            sendAvrcpCommand(AvrcpCtPlayerUtility.CMD_PLAY);
                        }
                    }
                    Log.d(BLUETOOTH_SERVICE_TAG, "AUDIOFOCUS_GAIN mIsAudioFocus IS " + mIsAudioFocus);
                    break;

                case AudioManager.AUDIOFOCUS_LOSS:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    mIsAudioFocus = false;

                    sendAvrcpCommand(AvrcpCtPlayerUtility.CMD_PAUSE);

                    Log.d(BLUETOOTH_SERVICE_TAG, "AUDIOFOCUS_LOSS mIsAudioFocus IS " + mIsAudioFocus);
                    break;

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    mIsAudioFocus = false;
                    break;
            }

            AvrcpControllerProfile profile = (AvrcpControllerProfile)getProfile(BLUETOOTH_PROFILE_AVRCPCT);
            if (null == profile) {
                Log.d(BLUETOOTH_SERVICE_TAG, "Avrcp Profile is null");
                return;
            }
            profile.setAudioFocusState(focusChange);
            
        }
    };

    // Callback
    private BluetoothCallback mBluetoothCallback = new BluetoothCallback() {
        @Override
        public void onBluetoothStateChanged(int i) {
            Log.d(BLUETOOTH_SERVICE_TAG, "onBluetoothStateChanged state = " + i);
            if (BluetoothAdapter.STATE_ON == i) {
                handleMsgCallback(BLUETOOTH_STATE_ON, 0, 0);


                setBTName();
                if (mLocalBluetoothAdapter != null) {
                    mLocalBluetoothAdapter.setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, -1);
                }
            } else if (BluetoothAdapter.STATE_OFF == i) {
                handleMsgCallback(BLUETOOTH_STATE_OFF, 0, 0);
            }
        }

        @Override
        public void onScanningStateChanged(boolean b) {
            Log.d(BLUETOOTH_SERVICE_TAG, "onScanningStateChanged");
            if (!b) {
                // 搜索结束
                handleMsgCallback(BLUETOOTH_SCAN_END, 0, 0);
                if (mIsStartSearch) {
                    mIsStartSearch = false;
                    mServiceHandler.sendEmptyMessageDelayed(BLUETOOTH_MSG_START_SEARCH, 200);
                }

            } else {
                handleMsgCallback(BLUETOOTH_SCAN_START, 0, 0);
            }
        }

        @Override
        public void onDeviceAdded(CachedBluetoothDevice cachedBluetoothDevice) {
            Log.d(BLUETOOTH_SERVICE_TAG, "onDeviceAdded");
            handleDeviceFound(cachedBluetoothDevice);
        }

        @Override
        public void onDeviceDeleted(CachedBluetoothDevice cachedBluetoothDevice) {
            Log.d(BLUETOOTH_SERVICE_TAG, "onDeviceDeleted");

        }

        @Override
        public void onDeviceBondStateChanged(CachedBluetoothDevice cachedBluetoothDevice, int i) {
            Log.d(BLUETOOTH_SERVICE_TAG, "onDeviceBondStateChanged");
            if (cachedBluetoothDevice == null) {
                return;
            }

            Intent intent = new Intent();
            intent.putExtra(BLUETOOTH_DEVICE_ADDRESS, cachedBluetoothDevice.getDevice().getAddress());
            intent.putExtra(BLUETOOTH_DEVICE_NAME, cachedBluetoothDevice.getDevice().getName());
            if (BluetoothDevice.BOND_BONDED == i) {
                LocalBluetoothProfile profile = getProfile(BLUETOOTH_PROFILE_HEADSET_CLIENT);
                if ((profile != null) && (cachedBluetoothDevice.isConnectedProfile(profile))) {
                    intent.putExtra(BLUETOOTH_DEVICE_STATE, BLUETOOTH_DEVICE_STATE_CONNECT);
                } else {
                    intent.putExtra(BLUETOOTH_DEVICE_STATE, BLUETOOTH_DEVICE_BONDED);
                }
            } else if (BluetoothDevice.BOND_NONE == i) {
                intent.putExtra(BLUETOOTH_DEVICE_STATE, BLUETOOTH_DEVICE_NOBOND);
                if(readLastConnectedDeviceAddress(getApplicationContext()).equals(cachedBluetoothDevice.getDevice().getAddress()))
                {
                    writeLastConnectedDeviceAddress(getApplicationContext(), "");
                }
            }
            handleIntentCallBack(intent);

        }

        @Override
        public void onConnectionStateChanged(CachedBluetoothDevice cachedBluetoothDevice, int i) {
            Log.d(BLUETOOTH_SERVICE_TAG, "onConnectionStateChanged");
            if (cachedBluetoothDevice == null) {
                return;
            }
            if (BluetoothProfile.STATE_DISCONNECTED == i) {
                mIsBluetoothConnect = false;
                Intent intent = new Intent();
                intent.putExtra(BLUETOOTH_DEVICE_ADDRESS, cachedBluetoothDevice.getDevice().getAddress());
                intent.putExtra(BLUETOOTH_DEVICE_NAME, cachedBluetoothDevice.getDevice().getName());
                intent.putExtra(BLUETOOTH_DEVICE_STATE, BLUETOOTH_DEVICE_STATE_DISCONNECT);
                handleIntentCallBack(intent);
                handleBluetoothPhoneBokkMsgCallback(BLUETOOTH_MSG_DISCONNECT, 0, 0);
                handleBluetoothCallMsgCallback(BLUETOOTH_MSG_DISCONNECT, 0, 0);
                handleBluetoothMusicMsgCallback(BLUETOOTH_MSG_DISCONNECT, 0, 0);
            } else if (BluetoothProfile.STATE_CONNECTED == i) {
                writeLastConnectedDeviceAddress(getApplicationContext(), cachedBluetoothDevice.getDevice().getAddress());
                stopAutoConnect();
            }

        }
    };
    private LocalBluetoothProfileManager.ProfileCallback  mBluetoothProfileCallback = new LocalBluetoothProfileManager.ProfileCallback() {
        @Override
        public void onProfileStateChanged(BluetoothDevice bluetoothDevice, int profile, int state) {
            Log.d(BLUETOOTH_SERVICE_TAG, "onProfileStateChanged");
            dispatchMessage(BLUETOOTH_MSG_HANDLE_PROFILE_CHANGE, bluetoothDevice, profile, state);
        }
    };
    private LocalBluetoothProfileManager.ServiceListener mBluetoothServiceListener = new LocalBluetoothProfileManager.ServiceListener() {
        @Override
        public void onServiceConnected() {
            Log.d(BLUETOOTH_SERVICE_TAG, "onServiceConnected");
            mA2dpSinkProfile = mLocalBluetoothProfileManager.getA2dpSinkProfile();
            mAvrcpCtProfile = mLocalBluetoothProfileManager.getAvrcpCtProfile();
            mHeadsetClientProfile = mLocalBluetoothProfileManager.getHeadsetClientProfile();
            mBluetoothPBManager = BluetoothPBManager.getInstance(getApplicationContext());

            if (mAvrcpCtProfile != null) {
                mAvrcpCtProfile.regPlayerUtility();
            }


            if (mHeadsetClientProfile != null) {
                mHeadsetClientProfile.registerHeadsetClientCallCallback(mHeadsetClientProfileCallback);
            }

            dispatchMessage(BLUETOOTH_MSG_HANDLE_SERVICE_ATTACH, null, 0, 0);
            if(readAutoConnect(getApplicationContext())) {
                startAutoConnect();
            }

        }

        @Override
        public void onServiceDisconnected() {
            Log.d(BLUETOOTH_SERVICE_TAG, "onServiceConnected");
            mA2dpSinkProfile = null;
            mAvrcpCtProfile = null;
            mHeadsetClientProfile = null;
            stopAutoConnect();
        }
    };

    private CachedBluetoothDevice.Callback mCacheDeviceCallback = new CachedBluetoothDevice.Callback() {
        @Override
        public void onDeviceAttributesChanged(CachedBluetoothDevice cachedBluetoothDevice) {
            Log.d(BLUETOOTH_SERVICE_TAG, "onDeviceAttributesChanged");
            dispatchMessage(BLUETOOTH_MSG_HANDLE_ATTR_CHANGE_DEVICE, cachedBluetoothDevice, 0, 0);

        }
    };

    private HeadsetClientProfile.HeadsetClientCallCallback mHeadsetClientProfileCallback = new HeadsetClientProfile.HeadsetClientCallCallback() {
        @Override
        public void onCallStateChanged(Intent intent) {
            onActionCallStateChange(intent);
        }

        @Override
        public void onAudioStatusChanged(Intent intent) {
            onAudioStateChange(intent);
        }
    };

    private AvrcpControllerProfile.MetadataCallback mAvrcpMetadataCallback = new AvrcpControllerProfile.MetadataCallback() {
        @Override
        public void onMetadataChanged(String title, String artist, String album) {
            Log.d(BLUETOOTH_SERVICE_TAG, "onMetadataChanged");
            mStrTitle = title;
            mStrAlbum = album;
            mStrArtist = artist;
            Intent intent = new Intent();
            intent.putExtra(BLUETOOTH_MUSIC_INTENT_TYPE, BLUETOOTH_MUSIC_INTEN_TYPE_METADATACHANGE);
            intent.putExtra(AVRCP_METADATA_TITLE, title);
            intent.putExtra(AVRCP_METADATA_ARTIST, artist);
            intent.putExtra(AVRCP_MEIADATA_ALBUM, album);
            handleBluetoothMusicIntentCallBack(intent);
            Log.d(BLUETOOTH_SERVICE_TAG, "mIsAudioFocus IS " + mIsAudioFocus);
            if(mIsAudioFocus) {
                ICMessage msg = ICMessage.obtain();
                msg.setCMD(ICMessage.CMD_REQUEST_NOTIFY_OTHER_APP).setInteger(ICMessage.APP_Launch);
                Bundle mBundle = msg.getData();
                mBundle.putInt("KEY_DATA_b", 1003);
                mBundle.putString("KEY_DATA_c", mStrTitle);
                if (mPlayState == AvrcpCtPlayerUtility.PLAYING) {
                    mBundle.putInt("KEY_DATA_d", 2001);
                } else {
                    mBundle.putInt("KEY_DATA_d", 2002);
                }
                mICManager.talkWithService(msg);
            }

        }

        @Override
        public void onPlayStatusChanged(byte play_state, int song_len, int song_pos) {
            Log.d(BLUETOOTH_SERVICE_TAG, "onPlayStatusChanged");
            mPlayState = play_state;
            mSonglen = song_len;
            mSongPos = song_pos;
            Log.d(BLUETOOTH_SERVICE_TAG, "onPlayStatusChanged mPlayState = " + mPlayState + " mSonglen = " + mSonglen + " mSongPos = " + mSongPos);
            Intent intent = new Intent();
            intent.putExtra(BLUETOOTH_MUSIC_INTENT_TYPE, BLUETOOTH_MUSIC_INTEN_TYPE_PLAYSTATECHANGE);
            intent.putExtra(AVRCP_PLAY_STATE, play_state);
            intent.putExtra(AVRCP_PLAY_SONG_LEN, song_len);
            intent.putExtra(AVRCP_PLAY_SONG_POS, song_pos);
            handleBluetoothMusicIntentCallBack(intent);
            Log.d(BLUETOOTH_SERVICE_TAG, "mIsAudioFocus IS " + mIsAudioFocus);
            if (mIsAudioFocus) {
                ICMessage msg = ICMessage.obtain();
                msg.setCMD(ICMessage.CMD_REQUEST_NOTIFY_OTHER_APP).setInteger(ICMessage.APP_Launch);
                Bundle mBundle = msg.getData();
                mBundle.putInt("KEY_DATA_b", 1003);
                mBundle.putString("KEY_DATA_c", mStrTitle);
                if (mPlayState == AvrcpCtPlayerUtility.PLAYING) {
                    mBundle.putInt("KEY_DATA_d", 2001);
                } else {
                    mBundle.putInt("KEY_DATA_d", 2002);
                }
                mICManager.talkWithService(msg);
            }
        }
    };

    private BluetoothPBManager.BluetoothPBCallback mBluetoothPBCallback = new BluetoothPBManager.BluetoothPBCallback() {
        @Override
        public void onPBDownloadStateChanged(Intent intent) {
            int path = intent.getIntExtra(BluetoothPbRecord.EXTRA_PATH, 0);
            int state  = intent.getIntExtra(BluetoothPbRecord.EXTRA_STATE, 0);
            Log.d(BLUETOOTH_SERVICE_TAG, "download state change, state:" + state + ", path:" + path);
            if ((path & (BluetoothPbRecord.PBMGR_SIM_PHONEBOOK | BluetoothPbRecord.PBMGR_PHONEBOOK |
                         BluetoothPbRecord.PBMGR_COMBINED_CALLED_HISTORY)) == 0) {
                return;
            }
            if ((path & (BluetoothPbRecord.PBMGR_SIM_PHONEBOOK | BluetoothPbRecord.PBMGR_PHONEBOOK)) != 0) {
                switch (state) {
                    case BluetoothPbRecord.PB_IND_START:
                        mCurContactsDown = 0;
                        mContactsDownState = state;
                        handleBluetoothPhoneBokkMsgCallback(BLUETOOTHPHONEBOOK_MSG_DOWN_CONITICS_STATE, mContactsDownState, 0);
                        break;

                    case BluetoothPbRecord.PB_IND_ERROR:
                    case BluetoothPbRecord.PB_IND_FINISH:
                    case BluetoothPbRecord.PB_IND_STOP:
                        mContactsDownState = state;
                        handleBluetoothPhoneBokkMsgCallback(BLUETOOTHPHONEBOOK_MSG_DOWN_CONITICS_STATE, mContactsDownState, 0);
                        if (isDownCallHistroy) {
                            dispatchMessage(BLUETOOTH_MSG_HANDLE_DOWNLOADR_CALLHISTROY, null, 0, 0);
                        }
                        isDownCallHistroy = true;
                        break;
                }
            } else if ((path & BluetoothPbRecord.PBMGR_COMBINED_CALLED_HISTORY) != 0) {
                switch (state) {
                    case BluetoothPbRecord.PB_IND_START:
                        mCurCallhistroyDown = 0;
                        mCallhistroyDownState = state;
                        handleBluetoothPhoneBokkMsgCallback(BLUETOOTHPHONEBOOK_MSG_DOWN_CALLHISTROY_STATE, mCallhistroyDownState, 0);
                        break;

                    case BluetoothPbRecord.PB_IND_ERROR:
                    case BluetoothPbRecord.PB_IND_FINISH:
                    case BluetoothPbRecord.PB_IND_STOP:
                        mCallhistroyDownState = state;
                        handleBluetoothPhoneBokkMsgCallback(BLUETOOTHPHONEBOOK_MSG_DOWN_CALLHISTROY_STATE, mCallhistroyDownState, 0);
                        break;
                }
            }


        }

        @Override
        public void onPBDownloadOnestep(Intent intent) {
            Log.d(BLUETOOTH_SERVICE_TAG, "onPBDownloadOnestep");
            int index = intent.getIntExtra(BluetoothPbRecord.EXTRA_INDEX, 0);
            int path = intent.getIntExtra(BluetoothPbRecord.EXTRA_PATH, 0);
            Log.d(BLUETOOTH_SERVICE_TAG, "onPBDownloadOnestep, index:" + index + ", path:" + path);
            if ((path & (BluetoothPbRecord.PBMGR_SIM_PHONEBOOK | BluetoothPbRecord.PBMGR_PHONEBOOK |
                         BluetoothPbRecord.PBMGR_COMBINED_CALLED_HISTORY)) == 0) {
                return;
            }
            if ((path & (BluetoothPbRecord.PBMGR_SIM_PHONEBOOK | BluetoothPbRecord.PBMGR_PHONEBOOK)) != 0) {
                mCurContactsDown = mCurContactsDown + 1;
                if (mCurContactsDown >= 10) {
                    mCurContactsDown = 0;
                    handleBluetoothPhoneBokkMsgCallback(BLUETOOTHPHONEBOOK_MSG_DOWN_CONTATCS_INDEX, index, 0);
                }
            } else if ((path & BluetoothPbRecord.PBMGR_COMBINED_CALLED_HISTORY) != 0) {
                mCurCallhistroyDown = mCurCallhistroyDown + 1;
                if (mCurCallhistroyDown >= 10) {
                    mCurCallhistroyDown = 0;
                    handleBluetoothPhoneBokkMsgCallback(BLUETOOTHPHONEBOOK_MSG_DOWN_CALLHISTROY_INDEX, index, 0);
                }
            }
        }
    };

    // AIDL FUN
    class BluetoothSetFunction extends IBluetoothSetFunctionInterface.Stub {
        @Override
        public void openBluetooth() {
            Log.d(BLUETOOTH_SERVICE_TAG, "openBluetooth start");

            if (mLocalBluetoothAdapter != null) {
                mLocalBluetoothAdapter.setBluetoothEnabled(true);
            }

            Log.d(BLUETOOTH_SERVICE_TAG, "openBluetooth end");
        }

        @Override
        public void closeBluetooth() {
            Log.d(BLUETOOTH_SERVICE_TAG, "closeBluetooth start");

            if (mLocalBluetoothAdapter != null) {
                mLocalBluetoothAdapter.setBluetoothEnabled(false);
            }

            Log.d(BLUETOOTH_SERVICE_TAG, "closeBluetooth end");
        }

        @Override
        public boolean isBluetoothOn() {
            Log.d(BLUETOOTH_SERVICE_TAG, "isBluetoothOn start");

            if (null == mLocalBluetoothAdapter) {
                Log.d(BLUETOOTH_SERVICE_TAG, "mLocalBluetoothAdapter is null");
                return false;
            }
            Log.d(BLUETOOTH_SERVICE_TAG, "isBluetoothOn is = " + mLocalBluetoothAdapter.isEnabled());
            return mLocalBluetoothAdapter.isEnabled();
        }

        @Override
        public void getBondDevices() {
            Log.d(BLUETOOTH_SERVICE_TAG, "getBondDevices start");

            if (null == mLocalBluetoothAdapter) {
                Log.d(BLUETOOTH_SERVICE_TAG, "mLocalBluetoothAdapter is null");
                return ;
            }
            dispatchMessage(BLUETOOTH_MSG_HANDLE_GEI_BONDDEVICE, null, 0, 0);
            Log.d(BLUETOOTH_SERVICE_TAG, "getBondDevices start");
        }

        @Override
        public void setLinstener(IBinder listener) {
            Log.d(BLUETOOTH_SERVICE_TAG, "setLinstener start");

            mIBluetoothSetListener = (IBluetoothSetListenerInterface)IBluetoothSetListenerInterface.Stub.asInterface(listener);

            Log.d(BLUETOOTH_SERVICE_TAG, "setLinstener end");
        }

        @Override
        public void startSearch() {
            Log.d(BLUETOOTH_SERVICE_TAG, "startSearch start");

            if (null == mLocalBluetoothAdapter) {
                Log.d(BLUETOOTH_SERVICE_TAG, "mLocalBluetoothAdapter is null");
                return;
            }
            if (mLocalBluetoothAdapter.isDiscovering()) {
                // 蓝牙正在搜索，停止搜索
                mIsStartSearch = true;
                mLocalBluetoothAdapter.cancelDiscovery();
            } else {
                mLocalBluetoothAdapter.startScanning(true);
            }

            Log.d(BLUETOOTH_SERVICE_TAG, "startSearch end");
        }

        @Override
        public void stopSearch() {
            Log.d(BLUETOOTH_SERVICE_TAG, "stopSearch start");

            if (null == mLocalBluetoothAdapter) {
                Log.d(BLUETOOTH_SERVICE_TAG, "mLocalBluetoothAdapter is null");
                return;
            }
            mLocalBluetoothAdapter.cancelDiscovery();

            Log.d(BLUETOOTH_SERVICE_TAG, "stopSearch end");
        }

        private int getDevice(String macAddr) {
            int index = 0;
            for (int i = 0; i < mBluetoothDevices.size(); i++) {
                if (macAddr.equals(mBluetoothDevices.get(i).get(BLUETOOTH_DEVICE_ADDRESS))) {
                    index = i;
                    break;
                }
            }
            if (index >= mBluetoothDevices.size()) {
                index = BLUETOOTH_SERVICE_ERR;
            }
            return index;
        }

        @Override
        public void connectDevice(String macAddr) {
            Log.d(BLUETOOTH_SERVICE_TAG, "connectDevice macAddr = " + macAddr);
            if (BLUETOOTH_SERVICE_ERR == getDevice(macAddr)) {
                Log.d(BLUETOOTH_SERVICE_TAG, "connectDevice macAddr = " + macAddr + " no device");
                return;
            }

            stopSearch();
            stopAutoConnect();
            CachedBluetoothDevice device = mBluetoothDeviceList.get(getDevice(macAddr));
            device.connect();
        }

        @Override
        public void disconnectDevice(String macAddr) {
            Log.d(BLUETOOTH_SERVICE_TAG, "disconnectDevice macAddr = " + macAddr);
            if (BLUETOOTH_SERVICE_ERR == getDevice(macAddr)) {
                Log.d(BLUETOOTH_SERVICE_TAG, "disconnectDevice macAddr = " + macAddr + " no device");
                return;
            }

            stopSearch();
            CachedBluetoothDevice device = mBluetoothDeviceList.get(getDevice(macAddr));
            device.disconnect();
        }

        @Override
        public void removeDevice(String macAddr) {
            Log.d(BLUETOOTH_SERVICE_TAG, "removeDevice macAddr = " + macAddr);
            if (BLUETOOTH_SERVICE_ERR == getDevice(macAddr)) {
                Log.d(BLUETOOTH_SERVICE_TAG, "removeDevice macAddr = " + macAddr + " no device");
                return;
            }

            stopSearch();
            BluetoothDevice device = mLocalBluetoothAdapter.getRemoteDevice(macAddr);
            if (device != null) {
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    device.removeBond();
                }

            }
        }

        @Override
        public void setPairRequest(boolean flag) {
            Log.d(BLUETOOTH_SERVICE_TAG, "setPairRequest");
            if (mCurPairDevice != null) {
                mCurPairDevice.setPairingConfirmation(flag);
            }
        }

        @Override
        public void setAutoConnect(boolean flag) {
            writeAutoConnect(getApplicationContext(), flag);
        }

        @Override
        public boolean getAutoConnect() {
            return readAutoConnect(getApplicationContext());
        }
    }

    class BluetoothA2dpFunction extends IBluetoothA2dpFunctionInterface.Stub {
        @Override
        public void play() {
            Log.d(BLUETOOTH_SERVICE_TAG, "play start");

            sendAvrcpCommand(AvrcpCtPlayerUtility.CMD_PLAY);
            mCurPlayState = AvrcpCtPlayerUtility.PLAYING;
            Log.d(BLUETOOTH_SERVICE_TAG, "play end");
        }

        @Override
        public void pause() {
            Log.d(BLUETOOTH_SERVICE_TAG, "pause start");

            sendAvrcpCommand(AvrcpCtPlayerUtility.CMD_PAUSE);
            mCurPlayState = AvrcpCtPlayerUtility.PAUSED;
            Log.d(BLUETOOTH_SERVICE_TAG, "pause end");
        }

        @Override
        public void prev() {
            Log.d(BLUETOOTH_SERVICE_TAG, "prev start");

            sendAvrcpCommand(AvrcpCtPlayerUtility.CMD_PREV);

            Log.d(BLUETOOTH_SERVICE_TAG, "prev end");
        }

        @Override
        public void next() {
            Log.d(BLUETOOTH_SERVICE_TAG, "next start");

            sendAvrcpCommand(AvrcpCtPlayerUtility.CMD_NEXT);

            Log.d(BLUETOOTH_SERVICE_TAG, "next end");
        }

        @Override
        public void setPlayerState(boolean flag) {
            if(mAvrcpCtProfile != null) {
                mAvrcpCtProfile.setPlayerState(flag);
                if (flag) {
                    requestAudioFocus();
                }
            }
        }

        @Override
        public void setLinstener(IBinder listener) {
            Log.d(BLUETOOTH_SERVICE_TAG, "setLinstener start");

            mIBluetoothA2dpListener = (IBluetoothSetListenerInterface)IBluetoothSetListenerInterface.Stub.asInterface(listener);

            Log.d(BLUETOOTH_SERVICE_TAG, "setLinstener end");
        }

        @Override
        public String getTitle() {
            Log.d(BLUETOOTH_SERVICE_TAG, "gettitle title is " + mStrTitle);
            return mStrTitle;
        }

        @Override
        public String getArtist() {
            Log.d(BLUETOOTH_SERVICE_TAG, "getArtist artist is " + mStrArtist);
            return mStrArtist;
        }

        @Override
        public String getAlbum() {
            Log.d(BLUETOOTH_SERVICE_TAG, "getAlbum albun is " + mStrAlbum);
            return mStrAlbum;
        }

        @Override
        public byte getPlayState() {
            Log.d(BLUETOOTH_SERVICE_TAG, "getPlayState state is " + mPlayState);
            return mPlayState;
        }

        @Override
        public int getSongLen() {
            return mSonglen;
        }

        @Override
        public int getSongPos() {
            return mSongPos;
        }


        @Override
        public int a2dpsinkstate() {
            Log.d(BLUETOOTH_SERVICE_TAG, "a2dpsinkstate");
            return mA2dpSinkProfileState;
        }

        @Override
        public int avrcpctstate() {
            Log.d(BLUETOOTH_SERVICE_TAG, "avrcpctstate");
            return mAvrcpCtProfileState;
        }

        @Override
        public int playbackstate() {
            return 1;
        }
    }

    class BluetoothPhoneCallFunction extends IBluetoothPhoneCallInterface.Stub {
        @Override
        public void hfpCall( String number) {
            Log.d(BLUETOOTH_SERVICE_TAG, "hfpCall");
            if (!isPhoneCall) {
                isPhoneCall = false;
                bluetoothPhoneCallCmd(BLUETOOTH_PHONECALL_CMD_CALL, number, (byte) 0, false);
            }
        }

        @Override
        public void acceptCall() {
            Log.d(BLUETOOTH_SERVICE_TAG, "acceptIncomingCall");
            bluetoothPhoneCallCmd(BLUETOOTH_PHONECALL_CMD_ACCEPT, null, (byte)0, false);
        }

        @Override
        public void rejectCall() {
            Log.d(BLUETOOTH_SERVICE_TAG, "rejectIncomingCall");
            bluetoothPhoneCallCmd(BLUETOOTH_PHONECALL_CMD_REJECT, null, (byte)0, false);
        }

        @Override
        public void terminateCall() {
            Log.d(BLUETOOTH_SERVICE_TAG, "rejectIncomingCall");
            bluetoothPhoneCallCmd(BLUETOOTH_PHONECALL_CMD_TERMINATE, null, (byte)0, false);
        }

        @Override
        public void sendDTMFCode(byte dtmfCode) {
            Log.d(BLUETOOTH_SERVICE_TAG, "sendDTMFCode");
            bluetoothPhoneCallCmd(BLUETOOTH_PHONECALL_CMD_DTMFCODE, null, dtmfCode, false);
        }

        @Override
        public void setLinstener(IBinder listener) {
            Log.d(BLUETOOTH_SERVICE_TAG, "setLinstener start");

            mIBluetoothCallListener = (IBluetoothSetListenerInterface)IBluetoothSetListenerInterface.Stub.asInterface(listener);

            Log.d(BLUETOOTH_SERVICE_TAG, "setLinstener end");
        }

        @Override
        public void muteMic(boolean mute) {
            Log.d(BLUETOOTH_SERVICE_TAG, "muteMic mute = " + mute);
            AudioManager manager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            if (manager == null) {
                return;
            }
            if (mute) {
                mMuteMic = true;
                manager.setMicrophoneMute(true);
            } else {
                mMuteMic = false;
                manager.setMicrophoneMute(false);
            }
        }

        @Override
        public boolean isMicMute() {
            return mMuteMic;
        }

        @Override
        public  void switchAudioMode(boolean towardsAG) {
            Log.d(BLUETOOTH_SERVICE_TAG, "switchAudioMode");
            bluetoothPhoneCallCmd(BLUETOOTH_PHONECALL_CMD_SWITCHAUDIO, null, (byte)0, towardsAG);
        }

        @Override
        public boolean isAudioTowardsAG() {
            return false;
        }

        @Override
        public String getIncomingNumber() {
            return mCallNumber;
        }

        @Override
        public String getRemoteAddr() {
            HeadsetClientProfile profile = (HeadsetClientProfile)getProfile(BLUETOOTH_PROFILE_HEADSET_CLIENT);
            if (null == profile) {
                Log.d(BLUETOOTH_SERVICE_TAG, "hfp profile is null");
                return null;
            }

            List<BluetoothDevice> devicelist = profile.getConnectedDevices();
            if ((null == devicelist) || (0 == devicelist.size())) {
                Log.d(BLUETOOTH_SERVICE_TAG, "hfp no device connect");
                return null;
            }

            String address = devicelist.get(0).getAddress();
            return address;
        }

    }

    class BluetoothPhoneBookFunction extends IBluetoothPhoneBookInterface.Stub {
        @Override
        public void downloadContacts() {
            Log.d(BLUETOOTH_SERVICE_TAG, "downloadContacts");
            dispatchMessage(BLUETOOTH_MSG_HANDLE_DOWNLOADR_CONTACTS, null, 0, 0);
        }

        @Override
        public void stopDownloadContacts() {
            if (BluetoothPbRecord.PB_IND_START == mContactsDownState) {
                isDownCallHistroy = false;
                dispatchMessage(BLUETOOTH_MSG_HANDLE_STOPDOWN_CONTACIS, null, 0, 0);
            }
            if (BluetoothPbRecord.PB_IND_START == mCallhistroyDownState) {
                dispatchMessage(BLUETOOTH_MSG_HANDLE_STOPDOWN_CALLHISTROY, null, 0, 0);
            }
        }

        @Override
        public void hfpCall(String number) {
            Log.d(BLUETOOTH_SERVICE_TAG, "hfpCall");
            if (!isPhoneCall) {
                isPhoneCall = false;
                bluetoothPhoneCallCmd(BLUETOOTH_PHONECALL_CMD_CALL, number, (byte) 0, false);
            }
        }

        @Override
        public int getContactsDownNumber() {
            return mCurContactsDown;
        }

        @Override
        public int getCallhistroyNumber() {
            return mCurCallhistroyDown;
        }

        @Override
        public int getContactsDownState() {
            return mContactsDownState;
        }

        @Override
        public int getContCallhistroyState() {
            return mCallhistroyDownState;
        }
        @Override
        public String getRemoteAddr() {
            HeadsetClientProfile profile = (HeadsetClientProfile)getProfile(BLUETOOTH_PROFILE_HEADSET_CLIENT);
            if (null == profile) {
                Log.d(BLUETOOTH_SERVICE_TAG, "hfp profile is null");
                return null;
            }

            List<BluetoothDevice> devicelist = profile.getConnectedDevices();
            if ((null == devicelist) || (0 == devicelist.size())) {
                Log.d(BLUETOOTH_SERVICE_TAG, "hfp no device connect");
                return null;
            }

            String address = devicelist.get(0).getAddress();
            return address;
        }

        @Override
        public void setAutoAcept(boolean flag) {
            writeAutoAcept(getApplicationContext(), flag);
        }

        @Override
        public boolean getAutoAcept() {
            return readAutoAcept(getApplicationContext());
        }

        @Override
        public void setLinstener(IBinder listener) {
            Log.d(BLUETOOTH_SERVICE_TAG, "setLinstener start");

            mIBluetoothPhoneBookListener = (IBluetoothSetListenerInterface)IBluetoothSetListenerInterface.Stub.asInterface(listener);

            Log.d(BLUETOOTH_SERVICE_TAG, "setLinstener end");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(BLUETOOTH_SERVICE_TAG, "onBind start");

        String action = intent.getAction();
        Log.d(BLUETOOTH_SERVICE_TAG, "intent action = " + action);
        if (IBluetoothSetFunctionInterface.class.getName().equals(action)) {
            return mIBluetoothSetFunction;
        } else if (IBluetoothA2dpFunctionInterface.class.getName().equals(action)) {
            return mIBluetoothA2dpFunction;
        } else if (IBluetoothPhoneCallInterface.class.getName().equals(action)) {
            return mIBluetoothPhoneCallFunction;
        } else if (IBluetoothPhoneBookInterface.class.getName().equals(action)) {
            return mIBluetoothPhoneBookFunction;
        } else {
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    @Override
    public void onCreate() {
        Log.d(BLUETOOTH_SERVICE_TAG, "onCreate start");

        initServiceStub();
        initBluetooth();

        mICManager = ICManager.getICManager();
        mICManager.connect(this, new ICManagerListener());
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        filter.addAction("android.intent.action.BOOT_COMPLETED");
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(mReceiver, filter);

        Log.d(BLUETOOTH_SERVICE_TAG, "onCreate end");
    }

    @Override
    public void onDestroy() {
        Log.d(BLUETOOTH_SERVICE_TAG, "onDestroy start");

        deinitBluetooth();

        Log.d(BLUETOOTH_SERVICE_TAG, "onDestroy end");
    }

    private void initServiceStub() {
        Log.d(BLUETOOTH_SERVICE_TAG, "initServiceStub start");

        if (null == mIBluetoothSetFunction) {
            mIBluetoothSetFunction = new BluetoothSetFunction();
        }

        if (null == mIBluetoothA2dpFunction) {
            mIBluetoothA2dpFunction = new BluetoothA2dpFunction();
        }

        if (null == mIBluetoothPhoneCallFunction) {
            mIBluetoothPhoneCallFunction = new BluetoothPhoneCallFunction();
        }

        if (null == mIBluetoothPhoneBookFunction) {
            mIBluetoothPhoneBookFunction = new BluetoothPhoneBookFunction();
        }
        Log.d(BLUETOOTH_SERVICE_TAG, "initServiceStub end");
    }

    private void initBluetooth() {
        Log.d(BLUETOOTH_SERVICE_TAG, "initBluetooth start");

        if (null == mLocalBluetoothManager) {
            mLocalBluetoothManager = LocalBluetoothManager.getInstance(this);
        }
        mBluetoothEventManager = mLocalBluetoothManager.getEventManager();
        if (mBluetoothEventManager != null) {
            mBluetoothEventManager.registerCallback(mBluetoothCallback);
        }
        mLocalBluetoothProfileManager = mLocalBluetoothManager.getProfileManager();
        if (mLocalBluetoothProfileManager != null) {
            mLocalBluetoothProfileManager.addProfileCallback(mBluetoothProfileCallback);
            mLocalBluetoothProfileManager.addServiceListener(mBluetoothServiceListener);
        }

        mLocalBluetoothAdapter = LocalBluetoothAdapter.getInstance();
        mCachedBluetoothDevicesManager = mLocalBluetoothManager.getCachedDeviceManager();

        Log.d(BLUETOOTH_SERVICE_TAG, "initBluetooth end");
    }

    private void deinitBluetooth() {
        Log.d(BLUETOOTH_SERVICE_TAG, "deinitBluetooth start");

        if (mLocalBluetoothProfileManager != null) {
            mLocalBluetoothProfileManager.removeProfileCallback(mBluetoothProfileCallback);
            mLocalBluetoothProfileManager.removeServiceListener(mBluetoothServiceListener);
            mLocalBluetoothProfileManager = null;
        }
        if (mBluetoothEventManager != null) {
            mBluetoothEventManager.unregisterCallback(mBluetoothCallback);
            mBluetoothEventManager = null;
        }

        mLocalBluetoothAdapter = null;
        mLocalBluetoothManager = null;
        mCachedBluetoothDevicesManager = null;

        Log.d(BLUETOOTH_SERVICE_TAG, "deinitBluetooth end");
    }

    private LocalBluetoothProfile getProfile(int profile) {
        Log.d(BLUETOOTH_SERVICE_TAG, "getProfile profile is " + profile);
        switch (profile) {
            case BLUETOOTH_PROFILE_AVRCPCT:
                if ((mAvrcpCtProfile != null) && (mAvrcpCtProfile.isProfileReady())) {
                    return mAvrcpCtProfile;
                }
                break;

            case BLUETOOTH_PROFILE_A2DP:
                if ((mA2dpSinkProfile != null) && (mA2dpSinkProfile.isProfileReady())) {
                    return mA2dpSinkProfile;
                }
                break;

            case BLUETOOTH_PROFILE_HEADSET_CLIENT:
                if ((mHeadsetClientProfile != null) && (mHeadsetClientProfile.isProfileReady())) {
                    return mHeadsetClientProfile;
                }
                break;
        }

        return null;
    }

    public void writeAutoConnect(Context context, boolean flag) {
        if (context != null) {
            SharedPreferences.Editor sharedata = context.getSharedPreferences("autoconnect_data", Context.MODE_PRIVATE).edit();
            sharedata.putBoolean("bt_autoconnect", flag);
            SharedPreferencesCommitor commitor = new SharedPreferencesCommitor(sharedata);
            new Thread(commitor).start();
        }
    }

    public boolean readAutoConnect(Context context) {
        boolean flag = false;
        if (context != null) {
            SharedPreferences sharedata = context.getSharedPreferences("autoconnect_data", Context.MODE_PRIVATE);
            flag = sharedata.getBoolean("bt_autoconnect", true);
        }
        return flag;
    }

    public void writeAutoAcept(Context context, boolean flag) {
        if (context != null) {
            SharedPreferences.Editor sharedata = context.getSharedPreferences("autoacept_data", Context.MODE_PRIVATE).edit();
            sharedata.putBoolean("bt_autoacept", flag);
            SharedPreferencesCommitor commitor = new SharedPreferencesCommitor(sharedata);
            new Thread(commitor).start();
        }
    }

    public boolean readAutoAcept(Context context) {
        boolean flag = false;
        if (context != null) {
            SharedPreferences sharedata = context.getSharedPreferences("autoacept_data", Context.MODE_PRIVATE);
            flag = sharedata.getBoolean("bt_autoacept", false);
        }
        return flag;
    }

    public void startAutoAcept() {
        Message msg = Message.obtain();
        msg.what = BLUETOOTH_MSG_AUTO_ACEPT;
        mServiceHandler.sendMessageDelayed(msg, 5000);
    }

    public void stopAutoAcept() {
        if (mServiceHandler.hasMessages(BLUETOOTH_MSG_AUTO_ACEPT)) {
            mServiceHandler.removeMessages(BLUETOOTH_MSG_AUTO_ACEPT);
        }
    }

    public void writeLastConnectedDeviceAddress(Context context, String addr) {
        Log.d(BLUETOOTH_SERVICE_TAG, "writeLastConnectedDeviceAddress: addr = " + addr);
        if (context != null) {
            SharedPreferences.Editor sharedata = context.getSharedPreferences("connectedDevice_data", Context.MODE_PRIVATE).edit();
            sharedata.putString("bt_address", addr);
            SharedPreferencesCommitor commitor = new SharedPreferencesCommitor(sharedata);
            new Thread(commitor).start();
        }
    }

    public String readLastConnectedDeviceAddress(Context context) {
        String addr = "";
        if (context != null) {
            SharedPreferences sharedata = context.getSharedPreferences("connectedDevice_data", Context.MODE_PRIVATE);
            addr = sharedata.getString("bt_address", "");
        }

        Log.d(BLUETOOTH_SERVICE_TAG, "readLastConnectedDeviceAddress addr = " + addr);
        return addr;
    }

    public void startAutoConnect() {
        if (readLastConnectedDeviceAddress(getApplicationContext()) != "") {
            Message msg = Message.obtain();
            msg.what = BLUETOOTH_MSG_AUTO_CONNECT;
            msg.obj = readLastConnectedDeviceAddress(getApplicationContext());
            mServiceHandler.sendMessage(msg);
        }
        return;
    }

    public void stopAutoConnect() {
        if (mServiceHandler.hasMessages(BLUETOOTH_MSG_AUTO_CONNECT)) {
            mServiceHandler.removeMessages(BLUETOOTH_MSG_AUTO_CONNECT);
        }
    }

    public class SharedPreferencesCommitor implements Runnable {
        private SharedPreferences.Editor mSharedata;
        public SharedPreferencesCommitor(SharedPreferences.Editor sharedata) {
            mSharedata = sharedata;
        }

        public void run() {
            if (mSharedata != null) {
                mSharedata.commit();
            }
        }
    }

    private void requestAudioFocus() {
        Log.d(BLUETOOTH_SERVICE_TAG, "requestAudioFocus");
        int ret = mICManager.requestAudioFocus(mOnAudioFocusChange, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN,
                                     ICMessage.SourceIndex_BTMusic);
        if (AudioManager.AUDIOFOCUS_REQUEST_GRANTED == ret) {
            if (AvrcpCtPlayerUtility.PLAYING == mCurPlayState) {
                if(AvrcpCtPlayerUtility.PLAYING != mPlayState) {
                    sendAvrcpCommand(AvrcpCtPlayerUtility.CMD_PLAY);
                }
            }
        }
        mIsAudioFocus = true;
    }
    // Bluetooth Setting
    private void onBluetoothAclDisconnected(Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        int reason = intent.getIntExtra(LocalBluetoothProfile.EXTRA_HCI_REASON, 0);
        Log.d(BLUETOOTH_SERVICE_TAG, "onBluetoothAclDisconnected device = " + device + " reason = " + reason);
        if (LocalBluetoothProfile.HCI_ERR_CONNECTION_TIMEOUT == reason) {
            startAutoConnect();
        }
    }

    private void setBTName() {
        mStrBTName = getBTName();
        if ((mStrBTName == null) && (mStrBTName.equals(""))) {
            mStrBTName = "AC8227L";
        }
        Log.d(BLUETOOTH_SERVICE_TAG, "setBTName = " + mStrBTName);
        mLocalBluetoothAdapter.setName(mStrBTName);
    }
    private String getBTName() {
        String name = "";
        Uri uri = Uri.parse("content://com.autochips.settings.display.provider/Settings");
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(uri, null, null, null, null);
        cursor.moveToFirst();
        name = cursor.getString(1);
        cursor.close();
        return name;
    }
    private String getBondDeviceState (CachedBluetoothDevice device) {
        HeadsetClientProfile hfpProfile = (HeadsetClientProfile)getProfile(BluetoothProfile.HEADSET_CLIENT);
        if ((hfpProfile == null)) {
            return BLUETOOTH_DEVICE_BONDED;
        }

        if (device.isConnectedProfile(hfpProfile)) {
            return BLUETOOTH_DEVICE_STATE_CONNECT;
        }

        return BLUETOOTH_DEVICE_BONDED;
    }
    private void handleDeviceFound(CachedBluetoothDevice cachedBluetoothDevice) {
        Log.d(BLUETOOTH_SERVICE_TAG, "handleDeviceFound start");

        if ((null == cachedBluetoothDevice) || (null == cachedBluetoothDevice.getDevice())) {
            Log.d(BLUETOOTH_SERVICE_TAG, "no found device");
            return;
        }
        cachedBluetoothDevice.registerCallback(mCacheDeviceCallback);

        BluetoothDevice device = cachedBluetoothDevice.getDevice();
        Log.d(BLUETOOTH_SERVICE_TAG, "handleDeviceFound device address " + device.getAddress());
        for (int i = 0; i < mBluetoothDevices.size(); i++) {
            if (device.getAddress().equals(mBluetoothDevices.get(i).get(BLUETOOTH_DEVICE_ADDRESS))) {
                mBluetoothDevices.get(i).put(BLUETOOTH_DEVICE_NAME, device.getName());
                if (BluetoothDevice.BOND_BONDED == device.getBondState()) {
                    mBluetoothDevices.get(i).put(BLUETOOTH_DEVICE_STATE, getBondDeviceState(cachedBluetoothDevice));
                } else {
                    mBluetoothDevices.get(i).put(BLUETOOTH_DEVICE_STATE, BLUETOOTH_DEVICE_NOBOND);
                }
                Intent intent = new Intent();
                intent.putExtra(BLUETOOTH_DEVICE_ADDRESS, mBluetoothDevices.get(i).get(BLUETOOTH_DEVICE_ADDRESS));
                intent.putExtra(BLUETOOTH_DEVICE_NAME, mBluetoothDevices.get(i).get(BLUETOOTH_DEVICE_NAME));
                intent.putExtra(BLUETOOTH_DEVICE_STATE, mBluetoothDevices.get(i).get(BLUETOOTH_DEVICE_STATE));
                handleIntentCallBack(intent);
                return;
            }
        }

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(BLUETOOTH_DEVICE_ADDRESS, device.getAddress());
        map.put(BLUETOOTH_DEVICE_NAME, device.getName());
        if (BluetoothDevice.BOND_BONDED == device.getBondState()) {
            map.put(BLUETOOTH_DEVICE_STATE, BLUETOOTH_DEVICE_BONDED);
        } else {
            map.put(BLUETOOTH_DEVICE_STATE, BLUETOOTH_DEVICE_NOBOND);
        }
        mBluetoothDevices.add(map);
        mBluetoothDeviceList.add(cachedBluetoothDevice);
        Intent intent = new Intent();
        intent.putExtra(BLUETOOTH_DEVICE_ADDRESS,map.get(BLUETOOTH_DEVICE_ADDRESS));
        intent.putExtra(BLUETOOTH_DEVICE_NAME, map.get(BLUETOOTH_DEVICE_NAME));
        intent.putExtra(BLUETOOTH_DEVICE_STATE, map.get(BLUETOOTH_DEVICE_STATE));
        handleIntentCallBack(intent);
        Log.d(BLUETOOTH_SERVICE_TAG, "handleDeviceFound end");

    }

    private void handleDeviceNameChange(CachedBluetoothDevice cacheDevice) {
        Log.d(BLUETOOTH_SERVICE_TAG, "handleDeviceNameChange start");

        if ((null == cacheDevice) || (null == cacheDevice.getDevice())) {
            Log.d(BLUETOOTH_SERVICE_TAG, "no found device");
            return;
        }
        BluetoothDevice device = cacheDevice.getDevice();
        if(device.getName() == null) {
            return;
        }
        Log.d(BLUETOOTH_SERVICE_TAG, "device name = " + device.getName() + "cacheDevice name = " + cacheDevice.getName());
        for (int i = 0; i < mBluetoothDevices.size(); i++) {
            if (device.getAddress().equals(mBluetoothDevices.get(i).get(BLUETOOTH_DEVICE_ADDRESS))) {
                Log.d(BLUETOOTH_SERVICE_TAG, "device address = " + device.getAddress() + "mBluetoothDevices address = " + mBluetoothDevices.get(i).get(BLUETOOTH_DEVICE_ADDRESS));
                if (mBluetoothDevices.get(i).get(BLUETOOTH_DEVICE_NAME) == null) {
                    return;
                }
                if (!device.getName().equals(mBluetoothDevices.get(i).get(BLUETOOTH_DEVICE_NAME))) {
                    mBluetoothDevices.get(i).put(BLUETOOTH_DEVICE_NAME, device.getName());
                    Intent intent = new Intent();
                    intent.putExtra(BLUETOOTH_DEVICE_ADDRESS, mBluetoothDevices.get(i).get(BLUETOOTH_DEVICE_ADDRESS));
                    intent.putExtra(BLUETOOTH_DEVICE_NAME, mBluetoothDevices.get(i).get(BLUETOOTH_DEVICE_NAME));
                    intent.putExtra(BLUETOOTH_DEVICE_STATE, mBluetoothDevices.get(i).get(BLUETOOTH_DEVICE_STATE));
                    handleIntentCallBack(intent);
                    return;
                } else {
                    return;
                }
                
            }
        }
        Log.d(BLUETOOTH_SERVICE_TAG, "handleDeviceNameChange end");
    }

    private void handleHfpStateChanged(BluetoothDevice device, int state) {
        Log.d(BLUETOOTH_SERVICE_TAG, "handleHfpStateChanged state = " + state);
        switch (state) {
            case BluetoothProfile.STATE_CONNECTED:
                mHfpProfileState = BluetoothProfile.STATE_CONNECTED;
                mIsBluetoothConnect = true;
                Intent intent = new Intent();
                intent.putExtra(BLUETOOTH_DEVICE_ADDRESS, device.getAddress());
                intent.putExtra(BLUETOOTH_DEVICE_NAME, device.getName());
                intent.putExtra(BLUETOOTH_DEVICE_STATE, BLUETOOTH_DEVICE_STATE_CONNECT);
                handleIntentCallBack(intent);
                dispatchMessage(BLUETOOTH_MSG_HANDLE_DOWNLOADR_CONTACTS, null, 0, 0);
                break;

            case BluetoothProfile.STATE_DISCONNECTED:
                mHfpProfileState = BluetoothProfile.STATE_DISCONNECTED;
                mIsShowActivity = false;
                mIsAudioFocus = false;
                isPhoneCall = false;
                mCurPlayState = AvrcpCtPlayerUtility.PLAYING;

                mICManager.abandonAudioFocus(mOnAudioFocusChange, ICMessage.SourceIndex_BTMusic);
                break;
        }
    }


    private void dispatchMessage(int what, Object obj, int arg1, int arg2) {
        Log.d(BLUETOOTH_SERVICE_TAG, "dispatchMessage what = " + what);
        if (mServiceHandler != null) {
            Message msg = mServiceHandler.obtainMessage();
            msg.what = what;
            msg.obj = obj;
            msg.arg1 = arg1;
            msg.arg2 = arg2;
            mServiceHandler.sendMessage(msg);
        }
    }

    public void handleMsgCallback(int what,int arg1,int arg2) {
        Log.d(BLUETOOTH_SERVICE_TAG, "handleMsgCallback what = " + what + " arg1 = " + arg1 + " arg2 = " + arg2);
        if (null == mIBluetoothSetListener) {
            Log.d(BLUETOOTH_SERVICE_TAG, "handleMsgCallback: mIBluetoothSetListener is null");
            return;
        }

        try {
            mIBluetoothSetListener.onMessage(what, arg1, arg2);
        } catch (RemoteException e) {
            Log.i(BLUETOOTH_SERVICE_TAG, "BluetoothService handleMessage err");
            e.printStackTrace();
        }
    }

    public void handleIntentCallBack(Intent intent) {
        Log.i(BLUETOOTH_SERVICE_TAG,"handleIntentCallBack intent =:" + intent.toString());
        if (null == mIBluetoothSetListener) {
            Log.d(BLUETOOTH_SERVICE_TAG, "handleIntentCallBack: mBluetoothSetListener is null");
            return;
        }
        try {
            mIBluetoothSetListener.onIntent(intent);
        } catch (RemoteException e) {
            Log.i(BLUETOOTH_SERVICE_TAG, "BluetoothService handleMessage err");
            e.printStackTrace();
        }
    }

    private void onBluetoothPairRequest(Context context, Intent intent) {
        Log.d(BLUETOOTH_SERVICE_TAG, "onBluetoothPairRequest");
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        int type = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, BluetoothDevice.ERROR);
        Log.d(BLUETOOTH_SERVICE_TAG, "onBluetoothPairRequest type is " + type);
        switch(type) {
            case BluetoothDevice.PAIRING_VARIANT_DISPLAY_PASSKEY:
            case BluetoothDevice.PAIRING_VARIANT_PASSKEY_CONFIRMATION:
                int passkey = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_KEY, BluetoothDevice.ERROR);
                if (BluetoothDevice.ERROR == passkey) {
                    return;
                }
                mCurPairDevice = device;
                handleMsgCallback(BLUETOOTH_PAIR_REQUEST, passkey, 0);
                //device.setPairingConfirmation(true);
                break;
        }
    }

    // BT Music
    private void regMetadataCallback() {
        AvrcpControllerProfile profile = (AvrcpControllerProfile)getProfile(BLUETOOTH_PROFILE_AVRCPCT);
        if (profile != null) {
            profile.regMetaCallback(mAvrcpMetadataCallback);
            profile.flushMetadata();
        }
    }

    private void onCmdICMessage(int cmd) {
        Log.d(BLUETOOTH_SERVICE_TAG, "onCmdICMessage cmd = " + cmd);
        switch (cmd) {
            case 3001:
                sendAvrcpCommand(AvrcpCtPlayerUtility.CMD_PLAY);
                mCurPlayState = AvrcpCtPlayerUtility.PLAYING;
                break;

            case 3002:
                sendAvrcpCommand(AvrcpCtPlayerUtility.CMD_PAUSE);
                mCurPlayState = AvrcpCtPlayerUtility.PAUSED;
                break;

            case 3003:
                sendAvrcpCommand(AvrcpCtPlayerUtility.CMD_PREV);
                break;

            case 3004:
                sendAvrcpCommand(AvrcpCtPlayerUtility.CMD_NEXT);
                break;
        }
    }

    private void handleA2dpSinkStateChanged(BluetoothDevice device, int state) {
        Log.d(BLUETOOTH_SERVICE_TAG, "handleA2dpSinkStateChanged");
        switch (state) {
            case BluetoothProfile.STATE_CONNECTED:
                mA2dpSinkProfileState = BluetoothProfile.STATE_CONNECTED;
                break;

            case BluetoothProfile.STATE_DISCONNECTED:
                mA2dpSinkProfileState = BluetoothProfile.STATE_DISCONNECTED;
                break;
        }
    }

    private void handleAvrcpCtStateChanged(BluetoothDevice device, int state) {
        Log.d(BLUETOOTH_SERVICE_TAG, "handleAvrcpCtStateChanged");
        switch (state) {
            case BluetoothProfile.STATE_CONNECTED:
                mAvrcpCtProfileState = BluetoothProfile.STATE_CONNECTED;
                handleBluetoothMusicMsgCallback(BLUETOOTH_MSG_AVRCP_CONNECT, 0, 0);
                break;

            case BluetoothProfile.STATE_DISCONNECTED:
                mAvrcpCtProfileState = BluetoothProfile.STATE_DISCONNECTED;
                break;
        }
    }

    private void sendAvrcpCommand(int cmd) {
        Log.d(BLUETOOTH_SERVICE_TAG, "sendAvrcpCommand cmd = " + cmd);
        AvrcpControllerProfile profile = (AvrcpControllerProfile)getProfile(BLUETOOTH_PROFILE_AVRCPCT);
        if (null == profile) {
            Log.d(BLUETOOTH_SERVICE_TAG, "Avrcp Profile is null");
            return;
        }

        if (mAvrcpCtProfileState != BluetoothProfile.STATE_CONNECTED) {
            Log.d(BLUETOOTH_SERVICE_TAG, "Avrcp Profile not connect");
            return;
        }

        List<BluetoothDevice> deviceList = profile.getConnectedDevices();
        if ((deviceList != null) && (deviceList.size() > 0)) {
            profile.sendAvrcpCommand(deviceList.get(0), cmd);
        }
    }

    public void handleBluetoothMusicIntentCallBack(Intent intent) {
        Log.i(BLUETOOTH_SERVICE_TAG,"handleBluetoothMusicIntentCallBack intent =:" + intent.toString());
        if (null == mIBluetoothA2dpListener) {
            Log.d(BLUETOOTH_SERVICE_TAG, "handleBluetoothMusicIntentCallBack: mBluetoothSetListener is null");
            return;
        }
        try {
            mIBluetoothA2dpListener.onIntent(intent);
        } catch (RemoteException e) {
            Log.i(BLUETOOTH_SERVICE_TAG, "BluetoothService handleMessage err");
            e.printStackTrace();
        }
    }

    public void handleBluetoothMusicMsgCallback(int what,int arg1,int arg2) {
        Log.d(BLUETOOTH_SERVICE_TAG, "handleBluetoothMusicMsgCallback what = " + what + " arg1 = " + arg1 + " arg2 = " + arg2);
        if (null == mIBluetoothA2dpListener) {
            Log.d(BLUETOOTH_SERVICE_TAG, "handleBluetoothPhoneBokkMsgCallback: mIBluetoothPhoneBookListener is null");
            return;
        }

        try {
            mIBluetoothA2dpListener.onMessage(what, arg1, arg2);
        } catch (RemoteException e) {
            Log.i(BLUETOOTH_SERVICE_TAG, "BluetoothService handleMessage err");
            e.printStackTrace();
        }
    }

    // phonecall
    private void bluetoothPhoneCallCmd(int cmd, String number, byte code, boolean audioState) {
        Log.d(BLUETOOTH_SERVICE_TAG, "bluetoothPhoneCallCmd cmd = " + cmd);
        HeadsetClientProfile profile = (HeadsetClientProfile)getProfile(BLUETOOTH_PROFILE_HEADSET_CLIENT);
        if (null == profile) {
            Log.d(BLUETOOTH_SERVICE_TAG, "hfp profile is null");
            return;
        }

        List<BluetoothDevice> devicelist = profile.getConnectedDevices();
        if ((null == devicelist) || (0 == devicelist.size())) {
            Log.d(BLUETOOTH_SERVICE_TAG, "hfp no device connect");
            return;
        }

        switch (cmd) {
            case BLUETOOTH_PHONECALL_CMD_CALL:
                profile.dial(devicelist.get(0), number);
                break;

            case BLUETOOTH_PHONECALL_CMD_ACCEPT:
                profile.acceptCall(devicelist.get(0), 0);
                break;

            case BLUETOOTH_PHONECALL_CMD_REJECT:
                profile.rejectCall(devicelist.get(0));
                break;

            case BLUETOOTH_PHONECALL_CMD_DTMFCODE:
                profile.sendDTMF(devicelist.get(0), code);
                break;

            case BLUETOOTH_PHONECALL_CMD_SWITCHAUDIO:
                if (audioState) {
                    profile.disconnectAudio();
                } else {
                    profile.connectAudio();
                }
                break;

            case BLUETOOTH_PHONECALL_CMD_TERMINATE:
                profile.terminateCall(devicelist.get(0), 0);
                break;
        }
    }

    private void onActionCallStateChange(Intent intent) {
        Log.d(BLUETOOTH_SERVICE_TAG, "onActionCallStateChange");
        HashMap<String, Object> map;
        BluetoothHeadsetClientCall call = intent.getParcelableExtra(BluetoothHeadsetClient.EXTRA_CALL);
        mCallState = call.getState();
        mCallNumber = call.getNumber();
        Log.d(BLUETOOTH_SERVICE_TAG, "callstate = " + mCallState);
        if (BluetoothHeadsetClientCall.CALL_STATE_INCOMING == mCallState) {
            if (readAutoAcept(getApplicationContext())) {
                startAutoAcept();
            }
        } else {
            stopAutoAcept();
        }
        if (!mIsShowActivity) {
            mIsShowActivity = true;
            map = new HashMap<String, Object>();
            map.put("callnumber", mCallNumber);
            mCallList.add(map);
            Intent intentActivity = new Intent();
            intentActivity.setClassName("com.autochips.bluetooth", "com.autochips.bluetooth.BluetoothPhoneActivity");
            intentActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intentActivity.putExtra(BLUETOOTH_PHONECALL_CALL_TYPE, mCallState);
            intentActivity.putExtra(BLUETOOTH_PHONECALL_CALL_NUMBER, mCallNumber);
            startActivity(intentActivity);
        } else {
            if (mCallList.size() > 0) {
                if (!mCallNumber.equals(mCallList.get(0).get("callnumber"))) {
                    return;
                }
            }
            Intent intentCallBack = new Intent();
            intentCallBack.putExtra(BLUETOOTH_PHONECALL_CALL_TYPE, mCallState);
            intentCallBack.putExtra(BLUETOOTH_PHONECALL_CALL_NUMBER, mCallNumber);
            handleBluetoothCallIntentCallBack(intentCallBack);
            if (BluetoothHeadsetClientCall.CALL_STATE_TERMINATED == mCallState) {
                isPhoneCall = false;
                mIsShowActivity = false;
                mCallList.clear();
            }
        }
    }

    private void onAudioStateChange(Intent intent) {
        Log.d(BLUETOOTH_SERVICE_TAG, "onActionCallStateChange");
        mScoState = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_DISCONNECTED);
        handleBluetoothCallMsgCallback(BLUETOOTH_PHONECALL_MSG_UPDATA_SCOSTATE, mScoState, 0);
    }

    public void handleBluetoothCallMsgCallback(int what,int arg1,int arg2) {
        Log.d(BLUETOOTH_SERVICE_TAG, "handleBluetoothCallMsgCallback what = " + what + " arg1 = " + arg1 + " arg2 = " + arg2);
        if (null == mIBluetoothCallListener) {
            Log.d(BLUETOOTH_SERVICE_TAG, "handleMsgCallback: mIBluetoothCallListener is null");
            return;
        }

        try {
            mIBluetoothCallListener.onMessage(what, arg1, arg2);
        } catch (RemoteException e) {
            Log.i(BLUETOOTH_SERVICE_TAG, "BluetoothService handleMessage err");
            e.printStackTrace();
        }
    }

    public void handleBluetoothCallIntentCallBack(Intent intent) {
        Log.i(BLUETOOTH_SERVICE_TAG,"handleBluetoothCallIntentCallBack intent =:" + intent.toString());
        if (null == mIBluetoothCallListener) {
            Log.d(BLUETOOTH_SERVICE_TAG, "mIBluetoothCallListener: mBluetoothSetListener is null");
            return;
        }
        try {
            mIBluetoothCallListener.onIntent(intent);
        } catch (RemoteException e) {
            Log.i(BLUETOOTH_SERVICE_TAG, "BluetoothService handleMessage err");
            e.printStackTrace();
        }
    }

    // bt phonebook
    private void regPhoneBookCallback() {
        Log.d(BLUETOOTH_SERVICE_TAG, "regPhoneBookCallback");
        if (mBluetoothPBManager == null) {
            Log.d(BLUETOOTH_SERVICE_TAG, "regPhoneBookCallback mBluetoothPBManager is null");
            return;
        }

        mBluetoothPBManager.regPBCallback(mBluetoothPBCallback);
    }

    private void startDownLoadContacts() {
        HeadsetClientProfile profile = (HeadsetClientProfile)getProfile(BLUETOOTH_PROFILE_HEADSET_CLIENT);
        if (null == profile) {
            Log.d(BLUETOOTH_SERVICE_TAG, "hfp profile is null");
            return;
        }

        List<BluetoothDevice> devicelist = profile.getConnectedDevices();
        if ((null == devicelist) || (0 == devicelist.size())) {
            Log.d(BLUETOOTH_SERVICE_TAG, "hfp no device connect");
            return;
        }

        if (mBluetoothPBManager != null) {
            Log.d(BLUETOOTH_SERVICE_TAG, "startDownLoadContacts");
            mBluetoothPBManager.startDownload(devicelist.get(0), (BluetoothPbRecord.PBMGR_PHONEBOOK | BluetoothPbRecord.PBMGR_SIM_PHONEBOOK));
        }
    }

    private void startDownLoadCallHistroy() {
        HeadsetClientProfile profile = (HeadsetClientProfile)getProfile(BLUETOOTH_PROFILE_HEADSET_CLIENT);
        if (null == profile) {
            Log.d(BLUETOOTH_SERVICE_TAG, "hfp profile is null");
            return;
        }

        List<BluetoothDevice> devicelist = profile.getConnectedDevices();
        if ((null == devicelist) || (0 == devicelist.size())) {
            Log.d(BLUETOOTH_SERVICE_TAG, "hfp no device connect");
            return;
        }

        if (mBluetoothPBManager != null) {
            Log.d(BLUETOOTH_SERVICE_TAG, "startDownLoadCallHistroy");
            mBluetoothPBManager.startDownload(devicelist.get(0), BluetoothPbRecord.PBMGR_COMBINED_CALLED_HISTORY);
        }
    }

    private void stopDownLoad() {
        HeadsetClientProfile profile = (HeadsetClientProfile)getProfile(BLUETOOTH_PROFILE_HEADSET_CLIENT);
        if (null == profile) {
            Log.d(BLUETOOTH_SERVICE_TAG, "hfp profile is null");
            return;
        }

        List<BluetoothDevice> devicelist = profile.getConnectedDevices();
        if ((null == devicelist) || (0 == devicelist.size())) {
            Log.d(BLUETOOTH_SERVICE_TAG, "hfp no device connect");
            return;
        }

        if (mBluetoothPBManager != null) {
            mBluetoothPBManager.stopDownload(devicelist.get(0));
        }
    }



    public void handleBluetoothPhoneBokkMsgCallback(int what,int arg1,int arg2) {
        Log.d(BLUETOOTH_SERVICE_TAG, "handleBluetoothPhoneBokkMsgCallback what = " + what + " arg1 = " + arg1 + " arg2 = " + arg2);
        if (null == mIBluetoothPhoneBookListener) {
            Log.d(BLUETOOTH_SERVICE_TAG, "handleBluetoothPhoneBokkMsgCallback: mIBluetoothPhoneBookListener is null");
            return;
        }

        try {
            mIBluetoothPhoneBookListener.onMessage(what, arg1, arg2);
        } catch (RemoteException e) {
            Log.i(BLUETOOTH_SERVICE_TAG, "BluetoothService handleMessage err");
            e.printStackTrace();
        }
    }

}
