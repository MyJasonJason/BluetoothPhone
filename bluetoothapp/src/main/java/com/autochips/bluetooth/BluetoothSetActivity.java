package com.autochips.bluetooth;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;


public class BluetoothSetActivity extends Activity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    public final static String BLUETOOTH_SET_ACTIVITY = "BluetoothSetActivity";

    public final static String BLUETOOTH_DEVICE_NAME = "device_name";
    public final static String BLUETOOTH_DEVICE_ADDRESS = "device_address";
    public final static String BLUETOOTH_DEVICE_STATE = "device_state";
    public final static String BLUETOOTH_DEVICE_BONDED = "device_bonded";
    public final static String BLUETOOTH_DEVICE_NOBOND = "device_nobond";
    public final static String BLUETOOTH_DEVICE_CONNECT = "device_connect";

    public final static int SourceIndex_BTMusic = 3;
    public final static int SourceIndex_BTPhoneBook = 7;
    public final static int SourceIndex_BTSetting = 8;

    public final static int MSG_INIT_OK = 0;
    public final static int MSG_SERVICE_CONNCET = 1;
    public final static int MSG_OPEN_BLUETOOTH = 2;
    public final static int MSG_CLOSE_BLUETOOTH = 3;
    public final static int MSG_BLUETOOTH_STATE_ON = 4;
    public final static int MSG_BLUETOOTH_STATE_OFF = 5;
    public final static int MSG_BLUETOOTH_REFRESH = 6;
    public final static int MSG_BLUETOOTH_UPDATA_PAIR_LIST = 7;
    public final static int MSG_BLUETOOTH_CONNECT_DEVICE = 8;
    public final static int MSG_BLUETOOTH_SCAN_START = 9;
    public final static int MSG_BLUETOOTH_SCAN_END = 10;
    public final static int MSG_BLUETOOTH_DISCONNECT_DEVICE = 11;
    public final static int MSG_BLUETOOTH_DELETE_DEVICE = 12;
    public final static int MSG_BLUETOOTH_UPDATA_DEVICE_LIST = 13;
    public final static int MSG_BLUETOOTH_OPEN_DEVICE_DIALOG = 14;
    public final static int MSG_BLUETOOTH_OPEN_PAIR_DIALOG = 15;
    public final static int MSG_BLUETOOTH_DELETE_UNPAIR_DEVICE = 16;
    public final static int MSG_BLUETOOTH_GET_BOND_DEVICE = 17;
    public final static int MSG_BLUETOOTH_DISCONNECT_CONNECT_DEVICE = 18;
    public final static int MSG_BLUETOOTH_HIDE_LIST = 19;
    public final static int MSG_BLUETOOTH_TIMER = 20;
    public final static int MSG_BLUETOOTH_STARTSOURCE = 21;

    private ToggleButton mBtnBluetoothOnOff;
    private ToggleButton mBtnBluetoothAutoConnect;
    private TextView mTextBluetoothName;
    private RelativeLayout mRelativeLayoutPair;
    private RelativeLayout mRelativeLayoutUnpair;
    private ListView mListViewPair;
    private ListView mListViewDevice;
    private Button mBtnRefresh;
    private ImageView mImgLoading;
    private Animation mAlphaAnimation;
    private myDialog.Builder mUnPairDialog;
    private myDialog.Builder mPairDialog;

    private boolean mIsBTOn = false;
    private boolean mIsAutoConnect = true;
    private boolean mIsBTOperation = false; // true 蓝牙正在连接，不可在操作 false 蓝牙可操作
    private boolean mIsConnect = false;
    private boolean mIsNeedConnet = false;
    private int mStartSource = SourceIndex_BTSetting;
    private int mAutoStartPhoneBook = 1; // 0 no auto start  > 0  auto start

    private BluetoothSetClass mBluetoothSet = null;
    private BluetoothSetHandler mBluetoothHandler;
    private BluetoothPairDeviceAdapter mPairDeviceAdapter;
    private BluetoothUnPairDeviceAdapter mUnPairDevicesAdapter;

    private String mStrCurSelDeviceName;
    private String mStrCurSelDeviceAddress;
    private String mStrCurSelDeviceState;
    private String mStrConnectDeviceAddress;

    private Timer mCallTimer;
    private int mTime = 0;



    private BluetoothSetClass.onListChangeListener mBTListener = new BluetoothSetClass.onListChangeListener() {
        @Override
        public void onListener(int what, int arg1, int arg2) {
            Log.d(BLUETOOTH_SET_ACTIVITY, "onListener what = " + what + " arg1 = " + arg1 + " arg2 = " + arg2);
            Message msg;
            switch (what) {
                case BluetoothSetClass.BLUETOOTH_SERVICE_READY:
                    mBluetoothHandler.sendEmptyMessage(MSG_INIT_OK);
                    break;

                case BluetoothSetClass.BLUETOOTH_STATE_ON:
                    mIsBTOn = true;
                    mUpdateUIhandler.sendEmptyMessage(MSG_BLUETOOTH_STATE_ON);
                    mBluetoothHandler.sendEmptyMessage(MSG_BLUETOOTH_GET_BOND_DEVICE);
                    mBluetoothHandler.sendEmptyMessage(MSG_BLUETOOTH_REFRESH);
                    break;

                case BluetoothSetClass.BLUETOOTH_STATE_OFF:
                    mIsBTOn = false;
                    mUpdateUIhandler.sendEmptyMessage(MSG_BLUETOOTH_STATE_OFF);
                    break;

                case BluetoothSetClass.BLUETOOTH_SCAN_START:
                    mUpdateUIhandler.sendEmptyMessage(MSG_BLUETOOTH_SCAN_START);
                    break;

                case BluetoothSetClass.BLUETOOTH_SCAN_END:
                    mUpdateUIhandler.sendEmptyMessage(MSG_BLUETOOTH_SCAN_END);
                    break;

                case BluetoothSetClass.BLUETOOTH_PAIR_REQUEST:
                    msg = mUpdateUIhandler.obtainMessage();
                    msg.what = MSG_BLUETOOTH_OPEN_DEVICE_DIALOG;
                    msg.arg1 = arg1;
                    mUpdateUIhandler.sendMessage(msg);
                    break;
            }
        }

        @Override
        public void onIntent(Intent intent) {
            Log.d(BLUETOOTH_SET_ACTIVITY, "onIntent");
            String address = intent.getStringExtra(BLUETOOTH_DEVICE_ADDRESS);
            String name = intent.getStringExtra(BLUETOOTH_DEVICE_NAME);
            String state = intent.getStringExtra(BLUETOOTH_DEVICE_STATE);
            Log.d(BLUETOOTH_SET_ACTIVITY, "state = " + state + " mStrDeviceName is " + name + " mStrDeviceAddress is " + address);
            if (null == state) {
                return;
            }
            if (state.equals(BLUETOOTH_DEVICE_NOBOND)) {
                sendMessageToUIHandle(MSG_BLUETOOTH_DELETE_DEVICE, name, address, state);
                sendMessageToUIHandle(MSG_BLUETOOTH_UPDATA_DEVICE_LIST, name, address, state);
                mIsBTOperation = false;
                stopTimer();
            } else if(state.equals(BLUETOOTH_DEVICE_BONDED)){
                sendMessageToUIHandle(MSG_BLUETOOTH_UPDATA_PAIR_LIST, name, address, state);
                sendMessageToUIHandle(MSG_BLUETOOTH_DELETE_UNPAIR_DEVICE, name, address, state);
            } else if (state.equals(BLUETOOTH_DEVICE_CONNECT)) {
                mIsConnect = true;
                mIsBTOperation = false;
                stopTimer();
                mStrConnectDeviceAddress = address;
                sendMessageToUIHandle(MSG_BLUETOOTH_UPDATA_PAIR_LIST, name, address, state);
                sendMessageToUIHandle(MSG_BLUETOOTH_DELETE_UNPAIR_DEVICE, name, address, state);
                mBluetoothHandler.sendEmptyMessage(MSG_BLUETOOTH_STARTSOURCE);
            } else {
                sendMessageToUIHandle(MSG_BLUETOOTH_UPDATA_PAIR_LIST, name, address, state);
                mIsBTOperation = false;
                mAutoStartPhoneBook = 1;
                stopTimer();
                mIsConnect = false;
                mStrConnectDeviceAddress = "";
                if (mIsNeedConnet) {
                    mBluetoothHandler.sendEmptyMessage(MSG_BLUETOOTH_CONNECT_DEVICE);
                    mIsNeedConnet = false;
                }
            }
        }
    };

    private Handler mUpdateUIhandler = new Handler() {
        public void handleMessage(Message msg) {
            Log.d(BLUETOOTH_SET_ACTIVITY, "mUpdateUIhandler handleMessage what is " + msg.what);
            String name;
            String address;
            String state;
            HashMap<String, String> map;
            switch (msg.what) {
                case MSG_SERVICE_CONNCET:
                    mBtnBluetoothOnOff.setChecked(mIsBTOn);
                    mBtnBluetoothOnOff.setEnabled(true);
                    mBtnBluetoothAutoConnect.setChecked(mIsAutoConnect);
                    if (mIsBTOn) {
                        mBluetoothHandler.sendEmptyMessage(MSG_BLUETOOTH_GET_BOND_DEVICE);
                        mRelativeLayoutPair.setVisibility(View.VISIBLE);
                        mRelativeLayoutUnpair.setVisibility(View.VISIBLE);
                        mBluetoothHandler.sendEmptyMessage(MSG_BLUETOOTH_REFRESH);
                    }
                    break;

                case MSG_BLUETOOTH_STATE_ON:
                    mBluetoothHandler.sendEmptyMessage(MSG_BLUETOOTH_GET_BOND_DEVICE);
                    mRelativeLayoutPair.setVisibility(View.VISIBLE);
                    mRelativeLayoutUnpair.setVisibility(View.VISIBLE);
                    mBtnBluetoothOnOff.setEnabled(true);
                    mBtnBluetoothOnOff.setChecked(true);
                    break;

                case MSG_BLUETOOTH_STATE_OFF:
                    mRelativeLayoutPair.setVisibility(View.INVISIBLE);
                    mRelativeLayoutUnpair.setVisibility(View.INVISIBLE);
                    mBtnBluetoothOnOff.setEnabled(true);
                    mBtnBluetoothOnOff.setChecked(false);
                    break;

                case MSG_BLUETOOTH_HIDE_LIST:
                    mUnPairDevicesAdapter.clear();
                    mPairDeviceAdapter.clear();
                    mPairDeviceAdapter.notifyDataSetChanged();
                    mUnPairDevicesAdapter.notifyDataSetChanged();
                    mRelativeLayoutPair.setVisibility(View.INVISIBLE);
                    mRelativeLayoutUnpair.setVisibility(View.INVISIBLE);
                    break;

                case MSG_BLUETOOTH_UPDATA_PAIR_LIST:
                    map = (HashMap<String, String>)msg.obj;
                    name = map.get(BLUETOOTH_DEVICE_NAME);
                    address = map.get(BLUETOOTH_DEVICE_ADDRESS);
                    state = map.get(BLUETOOTH_DEVICE_STATE);
                    if (!mPairDeviceAdapter.isHaveDevice(address)) {
                        mPairDeviceAdapter.addDevice(address, name, state);
                    } else {
                        mPairDeviceAdapter.updataDeviceName(address, name, state);
                    }
                    mPairDeviceAdapter.notifyDataSetChanged();
                    break;

                case MSG_BLUETOOTH_UPDATA_DEVICE_LIST:
                    map = (HashMap<String, String>)msg.obj;
                    name = map.get(BLUETOOTH_DEVICE_NAME);
                    address = map.get(BLUETOOTH_DEVICE_ADDRESS);
                    state = map.get(BLUETOOTH_DEVICE_STATE);
                    if (!mUnPairDevicesAdapter.isHaveDevice(address)) {
                        mUnPairDevicesAdapter.addDevice(address, name);
                    } else {
                        mUnPairDevicesAdapter.updataDeviceName(address, name);
                    }
                    mUnPairDevicesAdapter.notifyDataSetChanged();
                    break;

                case MSG_BLUETOOTH_REFRESH:
                    mUnPairDevicesAdapter.clear();
                    mUnPairDevicesAdapter.notifyDataSetChanged();
                    break;

                case MSG_BLUETOOTH_SCAN_START:
                    mImgLoading.startAnimation(mAlphaAnimation);
                    mImgLoading.setVisibility(View.VISIBLE);
                    break;

                case MSG_BLUETOOTH_SCAN_END:
                    mImgLoading.clearAnimation();
                    mImgLoading.setVisibility(View.INVISIBLE);
                    break;

                case MSG_BLUETOOTH_OPEN_DEVICE_DIALOG:
                    openDeviceDialog(msg.arg1);
                    break;

                case MSG_BLUETOOTH_OPEN_PAIR_DIALOG:
                    openPairDialog();
                    break;

                case MSG_BLUETOOTH_DELETE_UNPAIR_DEVICE:
                    map = (HashMap<String, String>)msg.obj;
                    address = map.get(BLUETOOTH_DEVICE_ADDRESS);
                    mUnPairDevicesAdapter.deleteDevice(address);
                    mUnPairDevicesAdapter.notifyDataSetChanged();
                    break;

                case MSG_BLUETOOTH_DELETE_DEVICE:
                    map = (HashMap<String, String>)msg.obj;
                    address = map.get(BLUETOOTH_DEVICE_ADDRESS);
                    if (mPairDeviceAdapter.isHaveDevice(address)) {
                        mPairDeviceAdapter.deleteDevice(address);
                        mPairDeviceAdapter.notifyDataSetChanged();
                    }
                    break;

                case MSG_BLUETOOTH_TIMER:
                    Log.d(BLUETOOTH_SET_ACTIVITY, "MSG_BLUETOOTH_TIMER mTime = " + mTime);
                    if (mTime >= 10) {
                        stopTimer();
                        mTime = 0;
                        mIsBTOperation = false;
                    }
                    break;
            }
        }
    };

    private AdapterView.OnItemClickListener mUnpairClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (!mIsBTOperation) {
                mIsBTOperation = true;
                startTimer();
                mStrCurSelDeviceName = mUnPairDevicesAdapter.getName(position);
                mStrCurSelDeviceAddress = mUnPairDevicesAdapter.getAddress(position);
                if (mIsConnect) {
                    mBluetoothHandler.sendEmptyMessage(MSG_BLUETOOTH_DISCONNECT_CONNECT_DEVICE);
                } else {
                    mBluetoothHandler.sendEmptyMessage(MSG_BLUETOOTH_CONNECT_DEVICE);
                }
            }
        }
    };

    private AdapterView.OnItemClickListener mPairClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
            if (!mIsBTOperation) {
                mStrCurSelDeviceState = mPairDeviceAdapter.getState(position);
                mStrCurSelDeviceAddress = mPairDeviceAdapter.getAddress(position);
                mStrCurSelDeviceName = mPairDeviceAdapter.getName(position);
                mUpdateUIhandler.sendEmptyMessage(MSG_BLUETOOTH_OPEN_PAIR_DIALOG);
            }
        }
    };

    private void initView() {
        mBtnBluetoothOnOff = (ToggleButton)findViewById(R.id.btn_bluetooth_onoff);
        mBtnBluetoothOnOff.setOnCheckedChangeListener(this);
        mBtnBluetoothOnOff.setEnabled(false);

        mBtnBluetoothAutoConnect = (ToggleButton)findViewById(R.id.btn_bluetooth_autoconnect_onoff);
        mBtnBluetoothAutoConnect.setOnCheckedChangeListener(this);
        mBtnBluetoothAutoConnect.setEnabled(true);

        mTextBluetoothName = (TextView)findViewById(R.id.text_bluetooth_name);
        String name = getBTName();
        mTextBluetoothName.setText(name);

        mRelativeLayoutPair = (RelativeLayout)findViewById(R.id.layout_pair_device);
        mRelativeLayoutPair.setVisibility(View.INVISIBLE);

        mRelativeLayoutUnpair = (RelativeLayout)findViewById(R.id.layout_unpair_device);
        mRelativeLayoutUnpair.setVisibility(View.INVISIBLE);

        mListViewPair = (ListView)findViewById(R.id.list_bluetooth_pair);
        mPairDeviceAdapter = new BluetoothPairDeviceAdapter(this);
        mListViewPair.setAdapter(mPairDeviceAdapter);
        mListViewPair.setOnItemClickListener(mPairClickListener);

        mListViewDevice = (ListView)findViewById(R.id.list_bluetooth_device);
        mUnPairDevicesAdapter = new BluetoothUnPairDeviceAdapter(this);
        mListViewDevice.setAdapter(mUnPairDevicesAdapter);
        mListViewDevice.setOnItemClickListener(mUnpairClickListener);


        mBtnRefresh = (Button)findViewById(R.id.btn_bluetooth_refresh);
        mBtnRefresh.setOnClickListener(this);

        mImgLoading = (ImageView)findViewById(R.id.img_bluetooth_loading);
        mImgLoading.setImageResource(R.drawable.bluetoothset_loading);
        mAlphaAnimation = AnimationUtils.loadAnimation(this, R.anim.loading);
        mAlphaAnimation.setInterpolator(new LinearInterpolator());
        mImgLoading.setVisibility(View.INVISIBLE);


    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!buttonView.isPressed()) {
            return;
        }

        if (R.id.btn_bluetooth_onoff == buttonView.getId()) {
            Log.d(BLUETOOTH_SET_ACTIVITY, "mIsBTOn is " + mIsBTOn);
            mBtnBluetoothOnOff.setEnabled(false);
            if (mIsBTOn) {
                // 蓝牙打开状态，关闭蓝牙
                mUpdateUIhandler.sendEmptyMessage(MSG_BLUETOOTH_HIDE_LIST);
                mBluetoothHandler.sendEmptyMessage(MSG_CLOSE_BLUETOOTH);
            } else {
                // 蓝牙关闭状态，打开蓝牙
                mBluetoothHandler.sendEmptyMessage(MSG_OPEN_BLUETOOTH);
            }
        } else if (R.id.btn_bluetooth_autoconnect_onoff == buttonView.getId()) {
            if (mIsAutoConnect) {
                mBluetoothSet.setAutoConnect(false);
            } else {
                mBluetoothSet.setAutoConnect(true);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_bluetooth_refresh:
                mBluetoothHandler.sendEmptyMessage(MSG_BLUETOOTH_REFRESH);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(BLUETOOTH_SET_ACTIVITY, "onCreate");
        setContentView(R.layout.activity_bluetooth_set);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},  1);
        }

        Intent intent = getIntent();
        mStartSource = intent.getIntExtra("KEY_DATA_a", SourceIndex_BTSetting);
        mAutoStartPhoneBook = intent.getIntExtra("KEY_DATA_b", 1);
        Log.d(BLUETOOTH_SET_ACTIVITY, "onCreat1e mStartSource = " + mStartSource);
        

        HandlerThread MsgThread = new HandlerThread("MSG_Thread");
        MsgThread.start();

        mBluetoothHandler = new BluetoothSetHandler(MsgThread.getLooper());
        mBluetoothSet = new BluetoothSetClass();
        mBluetoothSet.intiBluetoothSet(this, mBTListener);
        mUnPairDialog = new myDialog.Builder(this, myDialog.Builder.DIALOG_TWO_BUTTON);
        mPairDialog = new myDialog.Builder(this, myDialog.Builder.DIALOG_THREE_BUTTON);

        initView();
    }

    @Override
    public void onPause() {
        Log.d(BLUETOOTH_SET_ACTIVITY, "onPause");
        super.onPause();
        mStartSource = SourceIndex_BTSetting;
        mAutoStartPhoneBook = 1;
        mBluetoothSet.stopSearch();
        mBluetoothSet.deinitBluetoothSet();
        finish();
    }

    @Override
    public void onDestroy() {
        Log.d(BLUETOOTH_SET_ACTIVITY, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private String getBTName() {
        String name = "";
        Uri uri = Uri.parse("content://com.autochips.settings.display.provider/Settings");
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(uri, null, null, null, null);
        cursor.moveToFirst();
        name = cursor.getString(1);
        /*
        while (cursor.moveToNext()) {
            name = cursor.getString(1);
        }*/
        Log.d(BLUETOOTH_SET_ACTIVITY, "getBTName name = " + name);
        cursor.close();
        return name;
    }


    class BluetoothSetHandler extends Handler {
        public BluetoothSetHandler(Looper loop)
        {
            super(loop);
        }
        @Override
        public void handleMessage(Message msg) {
            Log.d(BLUETOOTH_SET_ACTIVITY, "BluetoothSetHandler handleMessage what = " + msg.what);
            switch (msg.what) {
                case MSG_INIT_OK:
                    mIsBTOn = mBluetoothSet.isBluetoothOn();
                    mIsAutoConnect = mBluetoothSet.getAutoConnect();
                    mUpdateUIhandler.sendEmptyMessage(MSG_SERVICE_CONNCET);
                    break;

                case MSG_BLUETOOTH_GET_BOND_DEVICE:
                    mBluetoothSet.getBondDevice();
                    break;

                case MSG_OPEN_BLUETOOTH:
                    mBluetoothSet.openBluetooth();
                    break;

                case MSG_CLOSE_BLUETOOTH:
                    mBluetoothSet.closeBluetooth();
                    break;

                case MSG_BLUETOOTH_REFRESH:
                    mUpdateUIhandler.sendEmptyMessage(MSG_BLUETOOTH_REFRESH);
                    mBluetoothSet.startSearch();
                    break;

                case MSG_BLUETOOTH_CONNECT_DEVICE:
                    mIsBTOperation = true;
                    startTimer();
                    mBluetoothSet.connectDevice(mStrCurSelDeviceAddress);
                    break;

                case MSG_BLUETOOTH_DISCONNECT_DEVICE:
                    mIsBTOperation = true;
                    startTimer();
                    mBluetoothSet.disConnectDevice(mStrCurSelDeviceAddress);
                    break;

                case MSG_BLUETOOTH_DELETE_DEVICE:
                    mIsBTOperation = true;
                    startTimer();
                    mBluetoothSet.removeDevice(mStrCurSelDeviceAddress);
                    break;

                case MSG_BLUETOOTH_DISCONNECT_CONNECT_DEVICE:
                    mIsBTOperation = true;
                    startTimer();
                    mIsNeedConnet = true;
                    mBluetoothSet.disConnectDevice(mStrConnectDeviceAddress);
                    break;

                case MSG_BLUETOOTH_STARTSOURCE:
                    statrtBTSourceActivity();
                    break;

            }
        }
    }

    class BluetoothPairDeviceAdapter extends BaseAdapter {

        private ArrayList<HashMap<String, String>> mList;
        private Context mContext;

        public BluetoothPairDeviceAdapter(Context context) {
            mList = new ArrayList<HashMap<String, String>>();
            mContext = context;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null ) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.bluetoothpairdevice, null);
                holder.mDeviceName = (TextView)convertView.findViewById(R.id.text_device_name);
                holder.mDeviceState = (TextView)convertView.findViewById(R.id.text_device_state);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }

            holder.mDeviceName.setText(mList.get(position).get(BLUETOOTH_DEVICE_NAME));
            holder.mDeviceState.setText(mList.get(position).get(BLUETOOTH_DEVICE_STATE));
            ListView.LayoutParams params = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT,57);
            convertView.setLayoutParams(params);
            return convertView;
        }

        public boolean isHaveDevice(String address) {
            Log.d(BLUETOOTH_SET_ACTIVITY, "isHaveDevice");
            if (mList.size() == 0) {
                return false;
            }
            for (int i = 0; i < mList.size(); i++) {
                if (address.equals(mList.get(i).get(BLUETOOTH_DEVICE_ADDRESS))) {
                    return true;
                }
            }

            return false;
        }

        public void addDevice(String address, String name, String state) {
            Log.d(BLUETOOTH_SET_ACTIVITY, "addDevice");
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(BLUETOOTH_DEVICE_ADDRESS, address);
            if (name == null) {
                map.put(BLUETOOTH_DEVICE_NAME, address);
            } else {
                map.put(BLUETOOTH_DEVICE_NAME, name);
            }
            if (state.equals(BLUETOOTH_DEVICE_CONNECT)) {
                map.put(BLUETOOTH_DEVICE_STATE, getResources().getString(R.string.bluetooth_connect));
            } else {
                map.put(BLUETOOTH_DEVICE_STATE, getResources().getString(R.string.bluetooth_bond));
            }
            mList.add(map);
            Log.d(BLUETOOTH_SET_ACTIVITY, "addDevice end");
        }

        public void updataDeviceName(String address, String name, String state) {
            if (mList.size() == 0) {
                return;
            }
            for (int i = 0; i < mList.size(); i++) {
                if (address.equals(mList.get(i).get(BLUETOOTH_DEVICE_ADDRESS))) {
                    if (name != null) {
                        mList.get(i).put(BLUETOOTH_DEVICE_NAME, name);
                    }
                    if (state != null) {
                        if (state.equals(BLUETOOTH_DEVICE_CONNECT)) {
                            mList.get(i).put(BLUETOOTH_DEVICE_STATE, getResources().getString(R.string.bluetooth_connect));
                        } else {
                            mList.get(i).put(BLUETOOTH_DEVICE_STATE, getResources().getString(R.string.bluetooth_bond));
                        }
                    }
                }
            }

        }

        public String getAddress(int index) {
            return mList.get(index).get(BLUETOOTH_DEVICE_ADDRESS);
        }

        public String getName(int index) {
            return mList.get(index).get(BLUETOOTH_DEVICE_NAME);
        }

        public String getState(int index) {
            return mList.get(index).get(BLUETOOTH_DEVICE_STATE);
        }

        public void clear() {
            mList.clear();
        }

        public void deleteDevice(String address) {
            Log.d(BLUETOOTH_SET_ACTIVITY, "deleteDevice");
            if (mList.size() == 0) {
                return ;
            }
            for (int i = 0; i < mList.size(); i++) {
                if (address.equals(mList.get(i).get(BLUETOOTH_DEVICE_ADDRESS))) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map = mList.get(i);
                    mList.remove(map);
                }
            }

        }


        public final class ViewHolder {
            public TextView mDeviceName;
            public TextView mDeviceState;
        }

    }

    class BluetoothUnPairDeviceAdapter extends BaseAdapter {

        private ArrayList<HashMap<String, String>> mList;
        private Context mContext;

        public BluetoothUnPairDeviceAdapter(Context context) {
            mList = new ArrayList<HashMap<String, String>>();
            mContext = context;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null ) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.bluetoothpairdevice, null);
                holder.mDeviceName = (TextView)convertView.findViewById(R.id.text_device_name);
                holder.mDeviceState = (TextView)convertView.findViewById(R.id.text_device_state);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }

            holder.mDeviceName.setText(mList.get(position).get(BLUETOOTH_DEVICE_NAME));
            holder.mDeviceState.setText(mList.get(position).get(BLUETOOTH_DEVICE_STATE));

            ListView.LayoutParams params = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT,57);
            convertView.setLayoutParams(params);
            return convertView;
        }

        public boolean isHaveDevice(String address) {
            Log.d(BLUETOOTH_SET_ACTIVITY, "isHaveDevice");
            if (mList.size() == 0) {
                return false;
            }
            for (int i = 0; i < mList.size(); i++) {
                if (address.equals(mList.get(i).get(BLUETOOTH_DEVICE_ADDRESS))) {
                    return true;
                }
            }

            return false;
        }

        public void addDevice(String address, String name) {
            Log.d(BLUETOOTH_SET_ACTIVITY, "addDevice");
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(BLUETOOTH_DEVICE_ADDRESS, address);
            if (name == null) {
                map.put(BLUETOOTH_DEVICE_NAME, address);
            } else {
                map.put(BLUETOOTH_DEVICE_NAME, name);
            }
            map.put(BLUETOOTH_DEVICE_STATE, getResources().getString(R.string.bluetooth_unbond));
            mList.add(map);
            Log.d(BLUETOOTH_SET_ACTIVITY, " BluetoothUnPairDeviceAdapter addDevice end");
        }

        public void updataDeviceName(String address, String name) {
            if (mList.size() == 0) {
                return;
            }
            for (int i = 0; i < mList.size(); i++) {
                if (address.equals(mList.get(i).get(BLUETOOTH_DEVICE_ADDRESS))) {
                    if (name != null) {
                        mList.get(i).put(BLUETOOTH_DEVICE_NAME, name);
                        return;
                    }
                }
            }

        }

        public String getAddress(int index) {
            return mList.get(index).get(BLUETOOTH_DEVICE_ADDRESS);
        }

        public String getName(int index) {
            return mList.get(index).get(BLUETOOTH_DEVICE_NAME);
        }

        public void clear() {
            mList.clear();
        }

        public void deleteDevice(String address) {
            Log.d(BLUETOOTH_SET_ACTIVITY, "deleteDevice address = " + address);
            if (mList.size() == 0) {
                return ;
            }
            if (address == null) {
                return;
            }
            for (int i = 0; i < mList.size(); i++) {
                if (address.equals(mList.get(i).get(BLUETOOTH_DEVICE_ADDRESS))) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map = mList.get(i);
                    mList.remove(map);
                }
            }

        }

        public final class ViewHolder {
            public TextView mDeviceName;
            public TextView mDeviceState;
        }

    }

    private void openDeviceDialog(int key) {
        Log.d(BLUETOOTH_SET_ACTIVITY, "openDeviceDialog ket = " + key + " mStrCurSelDeviceAddress + " + mStrCurSelDeviceAddress + " mStrCurSelDeviceName = " + mStrCurSelDeviceName);
        mUnPairDialog.setBTName(mStrCurSelDeviceName);
        mUnPairDialog.setBTPincode(Integer.toString(key));
        mUnPairDialog.setPositiveButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mUnPairDialog.dismiss();
                mBluetoothSet.setPairRequest(true);

            }
        });

        mUnPairDialog.setNegativeButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mUnPairDialog.dismiss();
                mBluetoothSet.setPairRequest(false);
                mIsBTOperation = false;
                stopTimer();
            }
        });
        myDialog dialog = mUnPairDialog.create();
        Window windowDialog = dialog.getWindow();
        WindowManager.LayoutParams lp = windowDialog.getAttributes();
        windowDialog.setGravity(Gravity.LEFT | Gravity.TOP);
        lp.x = 290;
        lp.y = 120;
        lp.width = 412;
        lp.height = 294;
        windowDialog.setAttributes(lp);
        dialog.show();
    }

    private void openPairDialog() {
        mPairDialog.setBTName(mStrCurSelDeviceName);
        if (getResources().getString(R.string.bluetooth_bond).equals(mStrCurSelDeviceState)) {
            mPairDialog.setPositiveButtonText(getResources().getString(R.string.bluetooth_dialog_connect));
            mPairDialog.setPositiveButton(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mPairDialog.dismiss();
                    if (mIsConnect) {
                        mBluetoothHandler.sendEmptyMessage(MSG_BLUETOOTH_DISCONNECT_CONNECT_DEVICE);
                    } else {
                        mBluetoothHandler.sendEmptyMessage(MSG_BLUETOOTH_CONNECT_DEVICE);
                    }
                }
            });

            mPairDialog.setNeutralButton(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mPairDialog.dismiss();
                    mBluetoothHandler.sendEmptyMessage(MSG_BLUETOOTH_DELETE_DEVICE);

                }
            });

            mPairDialog.setNegativeButton(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mPairDialog.dismiss();
                }
            });

            myDialog dialog = mPairDialog.create();
            Window windowDialog = dialog.getWindow();
            WindowManager.LayoutParams lp = windowDialog.getAttributes();
            windowDialog.setGravity(Gravity.LEFT | Gravity.TOP);
            lp.x = 290;
            lp.y = 120;
            lp.width = 412;
            lp.height = 294;
            windowDialog.setAttributes(lp);
            dialog.show();
        } else {
            mPairDialog.setPositiveButtonText(getResources().getString(R.string.bluetooth_dialog_disconnect));
            mPairDialog.setPositiveButton(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mPairDialog.dismiss();
                    mBluetoothHandler.sendEmptyMessage(MSG_BLUETOOTH_DISCONNECT_DEVICE);
                }
            });

            mPairDialog.setNeutralButton(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mPairDialog.dismiss();
                    mBluetoothHandler.sendEmptyMessage(MSG_BLUETOOTH_DELETE_DEVICE);
                }
            });

            mPairDialog.setNegativeButton(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mPairDialog.dismiss();
                }
            });

            myDialog dialog = mPairDialog.create();
            Window windowDialog = dialog.getWindow();
            WindowManager.LayoutParams lp = windowDialog.getAttributes();
            windowDialog.setGravity(Gravity.LEFT | Gravity.TOP);
            lp.x = 290;
            lp.y = 120;
            lp.width = 412;
            lp.height = 294;
            windowDialog.setAttributes(lp);
            dialog.show();

        }
    }

    private void sendMessageToUIHandle(int what, String name, String address, String state) {
        Message msg = mUpdateUIhandler.obtainMessage();
        HashMap<String, String> map = new HashMap<String, String>();

        map.put(BLUETOOTH_DEVICE_NAME, name);
        map.put(BLUETOOTH_DEVICE_ADDRESS, address);
        map.put(BLUETOOTH_DEVICE_STATE, state);
        msg.what = what;
        msg.obj = map;
        mUpdateUIhandler.sendMessage(msg);
    }

    private void startTimer() {
        mCallTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                mTime = mTime + 1;
                mUpdateUIhandler.sendEmptyMessage(MSG_BLUETOOTH_TIMER);
            }
        };
        mTime = -1;
        mCallTimer.schedule(task, 0, 1000);
    }

    private void stopTimer() {
        if (mCallTimer != null) {
            mCallTimer.cancel();
            mCallTimer = null;
        }
    }

    private void statrtBTSourceActivity() {
        Intent intentActivity = new Intent();
        switch (mStartSource) {
            case SourceIndex_BTMusic:
                intentActivity.setClassName("com.autochips.bluetooth", "com.autochips.bluetooth.BluetoothMusicActivity");
                intentActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentActivity);
                break;

            case SourceIndex_BTPhoneBook:
                if (mAutoStartPhoneBook > 0) {
                    intentActivity.setClassName("com.autochips.bluetooth", "com.autochips.bluetooth.BluetoothPhoneBookActivity");
                    intentActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intentActivity);
                }
                break;
        }
    }

}
