package com.autochips.bluetooth;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.media.AudioManager;


import java.util.Timer;
import java.util.TimerTask;

public class BluetoothPhoneActivity extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{

    public final static String BLUETOOTH_PHONE_ACTIVITY = "BluetoothPhoneActivity";

    private static final String DATA_AUTOHORITY = "com.autochips.bluetooth.contactsData";
    private static final String DATA_TABLE_NAME = "bluetooth_contacts_data_table";
    private static final Uri DATA_CONTENT_URI = Uri.parse("content://" + DATA_AUTOHORITY + "/" + DATA_TABLE_NAME);
    private static final String CONTACTS_ID = "contacts_id";
    private static final String DISPLAY_NAME = "display_name";
    private static final String DATA1 = "data1";
    private static final String CONTACT_SELECT_BY_NUMBER = DATA1+"=?";
    private static final String AUTOHORITY = "com.autochips.bluetooth.contacts";
    private static final String TABLE_NAME = "bluetooth_contacts_table";
    private static final Uri CONTENT_URI = Uri.parse("content://" + AUTOHORITY + "/" + TABLE_NAME);
    private static final String REMOTE_ADDR = "remote_addr";
    private static final String ID = "_id";
    private static final String CONTACT_SELECT_BY_ID = ID+"=?";

    public final static int MSG_UPDATA_VIEW = 2;
    public final static int MSG_UPDATA_CALL_TIME = 3;

    private final static String CALLHISTROY_TYPE_INCOMING = "incoming";
    private final static String CALLHISTROY_TYPE_OUTGOING = "outgoing";
    private final static String CALLHISTROY_TYPE_MISSED = "missed";
    private final static String CALLHISTROY_NAME = "callhistroy_name";
    private final static String CALLHISTROY_NUMBER = "callhistroy_number";
    private final static String CALLHISTROY_TYPE = "callhistroy_type";

    private RelativeLayout mLayoutKeyboard;
    private LinearLayout mLayoutControlAccept;
    private LinearLayout mLayoutControlReject;
    private LinearLayout mLayoutControlKeyboard;
    private LinearLayout mLayoutControlCar;
    private LinearLayout mLayoutControlPhone;
    private TextView mTextPhoneName;
    private TextView mTextPhoneNumber;
    private TextView mTextPhoneState;
    private TextView mTextControlAccept;
    private TextView mTextControlReject;
    private TextView mTextControlKeyBoard;
    private TextView mTextControlMute;
    private TextView mTextControlAudioCar;
    private TextView mTextControlAudioPhone;
    private Button mBtnControlAccept;
    private Button mBtnControlReject;
    private Button mBtnControlCar;
    private Button mBtnControlPhone;
    private CheckBox mCheckControlKeyboard;
    private CheckBox mCheckControlMute;

    private TextView mTextKeyboard;
    private Button mBtnKeyboardDelete;
    private ImageButton mBtnKeyboard_1;
    private ImageButton mBtnKeyboard_2;
    private ImageButton mBtnKeyboard_3;
    private ImageButton mBtnKeyboard_4;
    private ImageButton mBtnKeyboard_5;
    private ImageButton mBtnKeyboard_6;
    private ImageButton mBtnKeyboard_7;
    private ImageButton mBtnKeyboard_8;
    private ImageButton mBtnKeyboard_9;
    private ImageButton mBtnKeyboard_0;
    private ImageButton mBtnKeyboard_a;
    private ImageButton mBtnKeyboard_b;


    private int mCallType = BluetoothPhoneClass.BLUETOOTH_PHONE_CALL_STATE_NONE;
    private String mCallNumber;
    private String mCallName;
    private String mCallPhoneType;
    private int mCallScoState = BluetoothPhoneClass.BLUETOOTH_PHONE_SCO_CONNECT;
    private BluetoothPhoneClass mBluetoothPhoneClass;

    private String mStrKeyNum;

    private Timer mCallTimer;
    private int mCallTime;
    private boolean isMute = true;

    private Handler mUpdateUIhandler = new Handler() {
        public void handleMessage(Message msg) {
            Log.d(BLUETOOTH_PHONE_ACTIVITY, "mUpdateUIhandler handleMessage what is " + msg.what);
            switch (msg.what) {
                case MSG_UPDATA_VIEW:
                    int callstate = msg.arg1;
                    String callNumber = (String)msg.obj;
                    updataView(callstate, callNumber);
                    break;

                case BluetoothPhoneClass.BLUETOOTH_UPDATA_SCO_STATE:
                    int scoState = msg.arg1;
                    updataScoState(scoState);
                    break;

                case MSG_UPDATA_CALL_TIME:
                    updataCallTime(mCallTime);
                    break;

                case BluetoothPhoneClass.BLUETOOTH_MSG_DISCONNECT:
                    finish();
                    break;

                case BluetoothPhoneClass.BLUETOOTH_SERVICE_READY:
                    isMute = mBluetoothPhoneClass.isMicMute();
                    mCheckControlMute.setChecked(isMute);
                    updataView(mCallType, mCallNumber);
                    break;
            }
        }
    };

    private BluetoothPhoneClass.onListChangeListener mBTPhoneListener = new BluetoothPhoneClass.onListChangeListener() {
        @Override
        public void onListener(int what, int arg1, int arg2) {
            Log.d(BLUETOOTH_PHONE_ACTIVITY, "what = " + what);
            Message msg = mUpdateUIhandler.obtainMessage();
            msg.what = what;
            msg.arg1 = arg1;
            mUpdateUIhandler.sendMessage(msg);

        }

        @Override
        public void onIntent(Intent intent) {
            Log.d(BLUETOOTH_PHONE_ACTIVITY, "onIntent");
            int callstate = intent.getIntExtra("phone_type", 0);
            String callNumber = intent.getStringExtra("phone_number");
            Message msg = mUpdateUIhandler.obtainMessage();
            msg.what = MSG_UPDATA_VIEW;
            msg.obj = callNumber;
            msg.arg1 = callstate;
            mUpdateUIhandler.sendMessage(msg);
        }
    };

    private void initView() {
        mLayoutKeyboard = (RelativeLayout)findViewById(R.id.layout_bluetoothphone_keyboard);
        mLayoutControlAccept = (LinearLayout)findViewById(R.id.layout_btphone_control_accept);
        mLayoutControlReject = (LinearLayout)findViewById(R.id.layout_btphone_control_reject);
        mLayoutControlKeyboard = (LinearLayout)findViewById(R.id.layout_btphone_control_keyboard);
        mLayoutControlCar = (LinearLayout)findViewById(R.id.layout_btphone_control_car);
        mLayoutControlCar.setVisibility(View.VISIBLE);
        mLayoutControlPhone = (LinearLayout)findViewById(R.id.layout_btphone_control_phone);

        mTextPhoneName = (TextView)findViewById(R.id.text_btphone_name);
        mTextPhoneNumber = (TextView)findViewById(R.id.text_btphone_number);
        mTextPhoneState = (TextView)findViewById(R.id.text_btphone_state);
        mTextControlAccept = (TextView)findViewById(R.id.text_btphone_accept);
        mTextControlAccept.setText(R.string.bluetoothphone_control_accept);
        mTextControlReject = (TextView)findViewById(R.id.text_btphone_reject);
        mTextControlReject.setText(R.string.bluetoothphone_control_reject);
        mTextControlKeyBoard = (TextView)findViewById(R.id.text_btphone_keyboard);
        mTextControlKeyBoard.setText(R.string.bluetoothphone_control_keyboard);
        mTextControlMute = (TextView)findViewById(R.id.text_btphone_mute);
        mTextControlMute.setText(R.string.bluetoothphone_control_mutemic);
        mTextControlAudioCar = (TextView)findViewById(R.id.text_btphone_audio_car);
        mTextControlAudioCar.setText(R.string.bluetoothphone_control_audio_car);
        mTextControlAudioPhone = (TextView)findViewById(R.id.text_btphone_audio_phone) ;
        mTextControlAudioPhone.setText(R.string.bluetoothphone_control_audio_phone);

        mBtnControlAccept = (Button)findViewById(R.id.btn_btphone_accept);
        mBtnControlAccept.setOnClickListener(this);
        mBtnControlReject = (Button)findViewById(R.id.btn_btphone_reject);
        mBtnControlReject.setOnClickListener(this);
        mBtnControlCar = (Button)findViewById(R.id.btn_btphone_audio_car);
        mBtnControlCar.setOnClickListener(this);
        mBtnControlPhone = (Button)findViewById(R.id.btn_btphone_audio_phone);
        mBtnControlPhone.setOnClickListener(this);
        mCheckControlKeyboard = (CheckBox)findViewById(R.id.checkbox_btphone_keyboard);
        mCheckControlKeyboard.setOnCheckedChangeListener(this);
        mCheckControlMute = (CheckBox)findViewById(R.id.checkbox_btphone_mute);
        mCheckControlMute.setOnCheckedChangeListener(this);

        mTextKeyboard = (TextView)findViewById(R.id.text_keyboard_input);
        mBtnKeyboardDelete = (Button)findViewById(R.id.btn_keyboard_delete);
        mBtnKeyboardDelete.setOnClickListener(this);
        mBtnKeyboard_1 = (ImageButton) findViewById(R.id.imgbtn_keyboard_1);
        mBtnKeyboard_1.setOnClickListener(this);
        mBtnKeyboard_2 = (ImageButton) findViewById(R.id.imgbtn_keyboard_2);
        mBtnKeyboard_2.setOnClickListener(this);
        mBtnKeyboard_3 = (ImageButton) findViewById(R.id.imgbtn_keyboard_3);
        mBtnKeyboard_3.setOnClickListener(this);
        mBtnKeyboard_4 = (ImageButton) findViewById(R.id.imgbtn_keyboard_4);
        mBtnKeyboard_4.setOnClickListener(this);
        mBtnKeyboard_5 = (ImageButton) findViewById(R.id.imgbtn_keyboard_5);
        mBtnKeyboard_5.setOnClickListener(this);
        mBtnKeyboard_6 = (ImageButton) findViewById(R.id.imgbtn_keyboard_6);
        mBtnKeyboard_6.setOnClickListener(this);
        mBtnKeyboard_7 = (ImageButton) findViewById(R.id.imgbtn_keyboard_7);
        mBtnKeyboard_7.setOnClickListener(this);
        mBtnKeyboard_8 = (ImageButton) findViewById(R.id.imgbtn_keyboard_8);
        mBtnKeyboard_8.setOnClickListener(this);
        mBtnKeyboard_9 = (ImageButton) findViewById(R.id.imgbtn_keyboard_9);
        mBtnKeyboard_9.setOnClickListener(this);
        mBtnKeyboard_0 = (ImageButton) findViewById(R.id.imgbtn_keyboard_0);
        mBtnKeyboard_0.setOnClickListener(this);
        mBtnKeyboard_a = (ImageButton) findViewById(R.id.imgbtn_keyboard_a);
        mBtnKeyboard_a.setOnClickListener(this);
        mBtnKeyboard_b = (ImageButton) findViewById(R.id.imgbtn_keyboard_b);
        mBtnKeyboard_b.setOnClickListener(this);
    }

    private void updataCallTime(int time) {
        String str;
        if (time < 0) {
            return;
        }
        int hour = time / 3600;
        int min =  (time - hour * 3600) / 60;
        int sec = time % 60;

        Log.d(BLUETOOTH_PHONE_ACTIVITY, "hour = " + hour + " min = " + min + " sec = " + sec);
        if (hour < 10) {
            str = "0";
            str += Integer.toString(hour);
        } else {
            str = Integer.toString(hour);
        }
        str += ":";
        if (min < 10) {
            str += "0";
            str += Integer.toString(min);
        } else {
            str += Integer.toString(min);
        }
        str += ":";
        if (sec < 10) {
            str += "0";
            str += Integer.toString(sec);
        } else {
            str += Integer.toString(sec);
        }
        mTextPhoneState.setText(str);
    }

    private void updataScoState(int state) {
        Log.d(BLUETOOTH_PHONE_ACTIVITY, "updataScoState state is " + state);
        if (state == mCallScoState) {
            return;
        }

        mCallScoState = state;
        if (BluetoothPhoneClass.BLUETOOTH_PHONE_SCO_CONNECT == mCallScoState) {
            mLayoutControlPhone.setVisibility(View.GONE);
            mLayoutControlCar.setVisibility(View.VISIBLE);
        } else {
            mLayoutControlCar.setVisibility(View.GONE);
            mLayoutControlPhone.setVisibility(View.VISIBLE);
        }

    }

    private void updataView(int callType, String callNumber) {
        Log.d(BLUETOOTH_PHONE_ACTIVITY, "callType = " + callType + " callNumber = " + callNumber);
        String name = null;
        if (callNumber == null) {
            mTextPhoneNumber.setText(R.string.bluetoothphone_unkonow);
        } else {
            if ((mCallNumber != null) && (!callNumber.equals(mCallNumber))) {
                mCallNumber = callNumber;
                mTextPhoneNumber.setText(mCallNumber);
            } else if(mCallNumber == null) {
                mCallNumber = callNumber;
                mTextPhoneNumber.setText(mCallNumber);
            }
        }
        if (mCallNumber != null) {
            try {
                name = getContactByNumber(mCallNumber);
            } catch (Exception e) {
                e.printStackTrace();
                name = null;
            }
            if (name == null) {
                mCallName = getResources().getString(R.string.phonebook_unknown);
                mTextPhoneName.setText(R.string.bluetoothphone_unkonow);
            } else if (name.equals("")) {
                mCallName = getResources().getString(R.string.phonebook_unknown);
                mTextPhoneName.setText(R.string.bluetoothphone_unkonow);
            } else {
                mCallName = name;
                mTextPhoneName.setText(name);
            }
        }

        switch (callType) {
            case BluetoothPhoneClass.BLUETOOTH_PHONE_CALL_STATE_DIALING:
            case BluetoothPhoneClass.BLUETOOTH_PHONE_CALL_STATE_ALERTING:
                mCallPhoneType = CALLHISTROY_TYPE_OUTGOING;
                if (callType != mCallType) {
                    mCallType = callType;
                    mTextPhoneState.setText(R.string.bluetoothphone_state_out);
                    mLayoutControlAccept.setVisibility(View.GONE);
                    mLayoutControlReject.setVisibility(View.VISIBLE);
                    mLayoutControlKeyboard.setVisibility(View.VISIBLE);
                }
                break;

            case BluetoothPhoneClass.BLUETOOTH_PHONE_CALL_STATE_INCOMING:
                mCallPhoneType = CALLHISTROY_TYPE_MISSED;
                if (callType != mCallType) {
                    mCallType = callType;
                    mTextPhoneState.setText(R.string.bluetoothphone_state_in);
                    mLayoutControlAccept.setVisibility(View.VISIBLE);
                    mLayoutControlReject.setVisibility(View.VISIBLE);
                    mLayoutControlKeyboard.setVisibility(View.GONE);
                }
                break;

            case BluetoothPhoneClass.BLUETOOTH_PHONE_CALL_STATE_ACTIVE:
                if (mCallPhoneType == null) {
                    mCallPhoneType = CALLHISTROY_TYPE_INCOMING;
                } else {
                    if (mCallPhoneType.equals(CALLHISTROY_TYPE_MISSED)) {
                        mCallPhoneType = CALLHISTROY_TYPE_INCOMING;
                    }
                }
                if (callType != mCallType) {
                    mCallType = callType;
                    mLayoutControlAccept.setVisibility(View.GONE);
                    mLayoutControlReject.setVisibility(View.VISIBLE);
                    mLayoutControlKeyboard.setVisibility(View.VISIBLE);
                    startTimer();
                }
                break;

            case BluetoothPhoneClass.BLUETOOTH_PHONE_CALL_STATE_TERMINATED:
                Intent intent = new Intent("com.autochips.bluetooth.phonebook");
                intent.putExtra(CALLHISTROY_NAME, mCallName);
                intent.putExtra(CALLHISTROY_NUMBER, mCallNumber);
                intent.putExtra(CALLHISTROY_TYPE, mCallPhoneType);
                sendBroadcast(intent);
                stopTimer();
                this.finish();
                break;
        }
    }

    private void startTimer() {
        mCallTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                mCallTime += 1;
                mUpdateUIhandler.sendEmptyMessage(MSG_UPDATA_CALL_TIME);
            }
        };
        mCallTime = -1;
        mCallTimer.schedule(task, 0, 1000);
    }

    private void stopTimer() {
        if (mCallTimer != null) {
            mCallTimer.cancel();
            mCallTimer = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_phone);

        mBluetoothPhoneClass = new BluetoothPhoneClass();
        mBluetoothPhoneClass.intiBluetoothPhoneCall(this, mBTPhoneListener);
        Intent intent = getIntent();
        initView();
        updataView(intent.getIntExtra("phone_type", 0), intent.getStringExtra("phone_number"));


    }

    @Override
    protected void onDestroy() {
        Log.d(BLUETOOTH_PHONE_ACTIVITY, "onDestroy");
        super.onDestroy();
        stopTimer();
        mBluetoothPhoneClass.deinitBluetoothPhoneCall();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_btphone_accept:
                mBluetoothPhoneClass.acceptCall();
                break;

            case R.id.btn_btphone_reject:

                if (mCallType == BluetoothPhoneClass.BLUETOOTH_PHONE_CALL_STATE_INCOMING) {
                    mBluetoothPhoneClass.rejectCall();
                } else {
                    mBluetoothPhoneClass.terminateCall();
                }
                break;

            case R.id.btn_btphone_audio_car:
                if (mCallScoState == BluetoothPhoneClass.BLUETOOTH_PHONE_SCO_CONNECT) {
                    mBluetoothPhoneClass.switchAudioMode(true);
                }
                break;

            case R.id.btn_btphone_audio_phone:
                if (mCallScoState != BluetoothPhoneClass.BLUETOOTH_PHONE_SCO_CONNECT) {
                    mBluetoothPhoneClass.switchAudioMode(false);
                }
                break;

            case R.id.imgbtn_keyboard_1:
                if (mStrKeyNum == null) {
                    mStrKeyNum = "1";
                } else {
                    mStrKeyNum += "1";
                }
                mTextKeyboard.setText(mStrKeyNum);
                mBluetoothPhoneClass.sendDTMFCode((byte)'1');
                break;

            case R.id.imgbtn_keyboard_2:
                if (mStrKeyNum == null) {
                    mStrKeyNum = "2";
                } else {
                    mStrKeyNum += "2";
                }
                mTextKeyboard.setText(mStrKeyNum);
                mBluetoothPhoneClass.sendDTMFCode((byte)'2');
                break;

            case R.id.imgbtn_keyboard_3:
                if (mStrKeyNum == null) {
                    mStrKeyNum = "3";
                } else {
                    mStrKeyNum += "3";
                }
                mTextKeyboard.setText(mStrKeyNum);
                mBluetoothPhoneClass.sendDTMFCode((byte)'3');
                break;

            case R.id.imgbtn_keyboard_4:
                if (mStrKeyNum == null) {
                    mStrKeyNum = "4";
                } else {
                    mStrKeyNum += "4";
                }
                mTextKeyboard.setText(mStrKeyNum);
                mBluetoothPhoneClass.sendDTMFCode((byte)'4');
                break;

            case R.id.imgbtn_keyboard_5:
                if (mStrKeyNum == null) {
                    mStrKeyNum = "5";
                } else {
                    mStrKeyNum += "5";
                }
                mTextKeyboard.setText(mStrKeyNum);
                mBluetoothPhoneClass.sendDTMFCode((byte)'5');
                break;

            case R.id.imgbtn_keyboard_6:
                if (mStrKeyNum == null) {
                    mStrKeyNum = "6";
                } else {
                    mStrKeyNum += "6";
                }
                mTextKeyboard.setText(mStrKeyNum);
                mBluetoothPhoneClass.sendDTMFCode((byte)'6');
                break;

            case R.id.imgbtn_keyboard_7:
                if (mStrKeyNum == null) {
                    mStrKeyNum = "7";
                } else {
                    mStrKeyNum += "7";
                }
                mTextKeyboard.setText(mStrKeyNum);
                mBluetoothPhoneClass.sendDTMFCode((byte)'7');
                break;

            case R.id.imgbtn_keyboard_8:
                if (mStrKeyNum == null) {
                    mStrKeyNum = "8";
                } else {
                    mStrKeyNum += "8";
                }
                mTextKeyboard.setText(mStrKeyNum);
                mBluetoothPhoneClass.sendDTMFCode((byte)'8');
                break;

            case R.id.imgbtn_keyboard_9:
                if (mStrKeyNum == null) {
                    mStrKeyNum = "9";
                } else {
                    mStrKeyNum += "9";
                }
                mTextKeyboard.setText(mStrKeyNum);
                mBluetoothPhoneClass.sendDTMFCode((byte)'9');
                break;

            case R.id.imgbtn_keyboard_0:
                if (mStrKeyNum == null) {
                    mStrKeyNum = "0";
                } else {
                    mStrKeyNum += "0";
                }
                mTextKeyboard.setText(mStrKeyNum);
                mBluetoothPhoneClass.sendDTMFCode((byte)'0');
                break;

            case R.id.imgbtn_keyboard_a:
                if (mStrKeyNum == null) {
                    mStrKeyNum = "*";
                } else {
                    mStrKeyNum += "*";
                }
                mTextKeyboard.setText(mStrKeyNum);
                mBluetoothPhoneClass.sendDTMFCode((byte)'*');
                break;

            case R.id.imgbtn_keyboard_b:
                if (mStrKeyNum == null) {
                    mStrKeyNum = "#";
                } else {
                    mStrKeyNum += "#";
                }
                mTextKeyboard.setText(mStrKeyNum);
                mBluetoothPhoneClass.sendDTMFCode((byte)'#');
                break;

            case R.id.btn_keyboard_delete:
                if (mStrKeyNum != null) {
                    mStrKeyNum = mStrKeyNum.substring(0, (mStrKeyNum.length() - 1));
                }
                mTextKeyboard.setText(mStrKeyNum);
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.checkbox_btphone_keyboard:
                if (isChecked) {
                    mLayoutKeyboard.setVisibility(View.VISIBLE);
                } else {
                    mLayoutKeyboard.setVisibility(View.GONE);
                }
                break;

            case R.id.checkbox_btphone_mute:
                if (isChecked) {
                    mBluetoothPhoneClass.muteMic(true);
                } else {
                    mBluetoothPhoneClass.muteMic(false);
                }
                break;
        }
    }

    private String getContactByNumber(String number) throws Exception{
        String contactName = "";
        int contactID = -1;
        String remoteAddr = "";
        String recordAddr = "";

        remoteAddr = mBluetoothPhoneClass.getRemoteAddr();
        if (remoteAddr == null) {
            return remoteAddr;
        }
        if ("" == remoteAddr) {
            return remoteAddr;
        }

        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(DATA_CONTENT_URI, new String[] {CONTACTS_ID, DISPLAY_NAME},
                CONTACT_SELECT_BY_NUMBER, new String[] {number},
                null);
        if (cursor.moveToNext()) {
            contactName = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));
            contactID = cursor.getInt(cursor.getColumnIndex(CONTACTS_ID));
        }
        cursor.close();

        cursor = resolver.query(CONTENT_URI, new String[] {REMOTE_ADDR}, CONTACT_SELECT_BY_ID,
                new String[] {String.valueOf(contactID)}, null);
        if (cursor.moveToNext()) {
            recordAddr = cursor.getString(cursor.getColumnIndex(REMOTE_ADDR));
        }
        cursor.close();

        if (!recordAddr.equals(remoteAddr)) {
            contactName = "";
        }

        return contactName;

    }
}
