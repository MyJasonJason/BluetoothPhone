package com.autochips.bluetooth;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Bundle;
import android.os.OperationCanceledException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class BluetoothPhoneBookActivity extends Activity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener, CompoundButton.OnCheckedChangeListener, View.OnLongClickListener{

    public final static String BLUETOOTHPHONEBOOKTAG = "BluetoothPhoneBook";

    private final static int BLUETOOTH_PHONEBOOK_CALL_NUMBER_MAX = 17;
    private final static int KEY_CMD_DELETE = 0;
    private final static int KEY_CMD_SHOWNUMBER = 1;
    private final static int KEY_CMD_SHOW_KEYWOARD = 2;
    private final static int KEY_CMD_DELETE_ALL = 3;

    private final static int PHONEBOOK_CONTACT_TYPE = 1;
    private final static int PHONEBOOK_HISTROY_TYPE = 2;

    private final static int MSG_UPDATA_KEY_INPUT = 1;
    private final static int MSG_PHONEBOOK_DIALOG = 2;
    private final static int MSG_SHOW_FRAME_LAYOUT = 3;
    private final static int MSG_HIDE_FRAME_LAYOUT = 4;
    private final static int MSG_ADD_CONTACT = 5;
    private final static int MSG_LOAD_CONTACT = 6;
    private final static int MSG_ADD_PBLIST = 7;
    private final static int MSG_ADD_CALL_HISTORY = 8;
    private final static int MSG_LOAD_CALL_HISTORY = 9;
    private final static int MSG_ADD_HISTROYLIST = 10;
    private final static int MSG_STOP_DOWNLOAD = 11;
    private final static int MSG_START_DOWNLOAD = 12;
    private final static int MSG_CLEAN_PB_LIST = 13;
    private final static int MSG_CLEAN_CALL_LIST = 14;
    private final static int MSG_SEARCH = 15;
    private final static int MSG_UPDATA_SEARCH = 16;
    private final static int MSG_SHOW_SEARCH_LIST = 17;
    private final static int MSG_HIDE_SEARCH_LIST = 18;
    private final static int MSG_CLEAAR_SEARCH_LIST = 19;
    private final static int MSG_FINSH_ACTIVITY = 20;
    private final static int MSG_UPDATA_AUTO_ACEPT_CHECKBOX = 21;
    private final static int MSG_START_DOWNCANTACTS_ANIMATION = 22;
    private final static int MSG_STOP_DOWNCANTACTS_ANIMATION = 23;
    private final static int MSG_START_DOWNHISTROY_ANIMATION = 24;
    private final static int MSG_STOP_DOWNHISTROY_ANIMATION = 25;
    private final static int MSG_GET_DONWCONTACTS_STATE = 26;
    private final static int MSG_GET_DOWNHISTROY_STATE = 27;
    private final static int MSG_GET_DOWNCONTACTS_NUM = 28;
    private final static int MSG_GET_DOWNHISTROY_NUM = 29;


    private static final String AUTOHORITY = "com.autochips.bluetooth.contacts";
    private static final String TABLE_NAME = "bluetooth_contacts_table";
    private static final Uri CONTENT_URI = Uri.parse("content://" + AUTOHORITY + "/" + TABLE_NAME);
    private static final String DATA_AUTOHORITY = "com.autochips.bluetooth.contactsData";
    private static final String DATA_TABLE_NAME = "bluetooth_contacts_data_table";
    private static final Uri DATA_CONTENT_URI = Uri.parse("content://" + DATA_AUTOHORITY + "/" + DATA_TABLE_NAME);
    private static final String ID = "_id";
    private static final String DISPLAY_NAME = "display_name";
    private static final String REMOTE_PATH = "remote_path";
    private static final String REMOTE_ADDR = "remote_addr";
    private static final String CONTACT_SELECT_LIMIT = REMOTE_ADDR+"=?";
    private static final String CONTACT_SELECT_ALL = REMOTE_ADDR+"=?";
    private static final String MIME_TYPE = "mime_type";
    private static final String DATA1 = "data1";
    private static final String DATA2 = "data2";
    private static final String CONTACTS_ID = "contacts_id";
    private static final String CONTACTDATA_SELECT = CONTACTS_ID+"=?";
    private static final String CONTACT_SELECT_BY_NUMBER = DATA1+"=?";
    private static final String CONTACT_SELECT_BY_ID = ID+"=?";

    private static final String CALL_AUTOHORITY = "com.autochips.bluetooth.calllog";
    private static final String CALL_TABLE_NAME = "bluetooth_calllog_table";
    private static final Uri CALL_CONTENT_URI = Uri.parse("content://" + CALL_AUTOHORITY + "/" + CALL_TABLE_NAME);
    private static final String CALL_PHONE_NUMBER = "phone_number";
    private static final String CALL_DISPLAY_NAME = "display_name";
    private static final String CALL_CALL_TYPE = "call_type";
    private static final String CALL_CALL_TIME = "call_time";
    private static final String CALL_REMOTE_ADDR = "remote_addr";
    private static final String CALL_SELECT_LIMIT = CALL_REMOTE_ADDR+"=?";
    private static final String CALL_SELECT_ALL = CALL_REMOTE_ADDR+"=?";

    private static final String CONTACTDATA_SEARCH = DISPLAY_NAME+" LIKE ? or " + DATA1+" LIKE ?";
    private static final String CONTACT_SELECT_ID = ID+"=?";


    private final static String PHONEBOOK_NAME = "phonebook_name";
    private final static String PHONEBOOK_NUMBER = "phonebook_number";
    private final static String CALLHISTROY_NAME = "callhistroy_name";
    private final static String CALLHISTROY_NUMBER = "callhistroy_number";
    private final static String CALLHISTROY_TYPE = "callhistroy_type";
    private final static String CALLHISTROY_TYPE_INCOMING = "incoming";
    private final static String CALLHISTROY_TYPE_OUTGOING = "outgoing";
    private final static String CALLHISTROY_TYPE_MISSED = "missed";

    private final static int PHONEBOOK_STATE_START = 1;
    private final static int PHONEBOOK_STATE_STOP = 2;
    private final static int PHONEBOOK_STATE_FINSH = 4;
    private final static int PHONEBOOK_STATE_ERR = 3;

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
    private Button mBtnCall;
    private Button mBtnSetShow;
    private Button mBtnSetHide;
    private CheckBox mCheckAotoAcept;
    private ImageButton mBtnSet;
    private ImageButton mBtnDownLoader;
    private ListView mPBListView;
    private ListView mCallListView;
    private ListView mSearchView;
    private RadioButton mBtnContacts;
    private RadioButton mBtnCallHistroy;
    private RadioGroup mDownLoaderGroup;
    private ImageView mImgContacts;
    private ImageView mImgHistroy;
    private Animation mContactsAnimation;
    private Animation mHistroyAnimation;

    private RelativeLayout mLayoutSetFrame;

    private BluetoothPhoneBookClass mBluetoothPhoneBookClass;
    private BluetoothPhoneBookHandler mBluetoothPhoneBookHandler;
    private BluetoothPhoneBookAdapter mPBAdapter;
    private BluetoothCallHistroyAdapter mCallAdapter;
    private BluetoothSearchAdapter mSearchAdapter;

    private String mCallNumber;

    private int mCurrentIndex = 0;
    private int mCurrentCallIndex = 0;
    private int mContactsState;
    private int mCallHistroyState;
    private boolean isDown = false;
    private String mSearchKey;
    private boolean isContacts = true;

    private boolean isShowContacts = false;
    private boolean isShowHistroy = false;

    private ArrayList<HashMap<String, String>> mPBList;
    private ArrayList<HashMap<String, String>> mCallList;
    private ArrayList<HashMap<String, String>> mSearchList;
    private ArrayList<String> mPinyingList;

    private CancellationSignal mSearchCancellationSignal = null;


    private Handler mUpdateUIhandler = new Handler() {
        public void handleMessage(Message msg) {
            Log.d(BLUETOOTHPHONEBOOKTAG, "mUpdateUIhandler msg is " + msg.what);
            switch (msg.what) {
                case MSG_UPDATA_KEY_INPUT:
                    updataText(msg.arg1, (String)msg.obj);
                    break;

                case MSG_SHOW_FRAME_LAYOUT:
                    mLayoutSetFrame.setVisibility(View.VISIBLE);
                    mBtnSetShow.setVisibility(View.GONE);
                    mBtnSetHide.setVisibility(View.VISIBLE);
                    break;

                case MSG_HIDE_FRAME_LAYOUT:
                    mLayoutSetFrame.setVisibility(View.GONE);
                    mBtnSetHide.setVisibility(View.GONE);
                    mBtnSetShow.setVisibility(View.VISIBLE);
                    break;

                case MSG_ADD_PBLIST:
                    mPBAdapter.setCursor(getCursor(PHONEBOOK_CONTACT_TYPE));
                    break;

                case MSG_ADD_HISTROYLIST:
                    mCallAdapter.setCursor(getCursor(PHONEBOOK_HISTROY_TYPE));
                    break;

                case MSG_CLEAN_CALL_LIST:
                    mCallList.clear();
                    mCallAdapter.setCursor(null);
                    break;

                case MSG_CLEAN_PB_LIST:
                    mPBAdapter.setCursor(null);
                    break;

                case MSG_UPDATA_SEARCH:
                    SearchResult result  = (SearchResult) msg.obj;
                    if (mSearchKey.equals(result.searchKey)) {
                        mSearchList.addAll(result.pbList);
                        mSearchAdapter.notifyDataSetChanged();
                    }
                    break;

                case MSG_SHOW_SEARCH_LIST:
                    //mDownLoaderGroup.setVisibility(View.GONE);
                    mPBListView.setVisibility(View.GONE);
                    mCallListView.setVisibility(View.GONE);
                    mSearchView.setVisibility(View.VISIBLE);
                    break;

                case MSG_HIDE_SEARCH_LIST:
                    //mDownLoaderGroup.setVisibility(View.VISIBLE);
                    if (isContacts) {
                        mPBListView.setVisibility(View.VISIBLE);
                        mCallListView.setVisibility(View.GONE);
                    } else {
                        mPBListView.setVisibility(View.GONE);
                        mCallListView.setVisibility(View.VISIBLE);
                    }
                    mSearchView.setVisibility(View.GONE);
                    break;

                case MSG_CLEAAR_SEARCH_LIST:
                    mSearchList.clear();
                    mSearchAdapter.notifyDataSetChanged();
                    break;

                case MSG_FINSH_ACTIVITY:
                    finish();
                    break;

                case MSG_UPDATA_AUTO_ACEPT_CHECKBOX:
                    if (mBluetoothPhoneBookClass.getAutoAcept()) {
                        mCheckAotoAcept.setChecked(true);
                    } else {
                        mCheckAotoAcept.setChecked(false);
                    }
                    break;

                case MSG_START_DOWNCANTACTS_ANIMATION:
                    mImgContacts.startAnimation(mContactsAnimation);
                    mImgContacts.setVisibility(View.VISIBLE);
                    break;

                case MSG_STOP_DOWNCANTACTS_ANIMATION:
                    mImgContacts.clearAnimation();
                    mImgContacts.setVisibility(View.INVISIBLE);
                    break;

                case MSG_START_DOWNHISTROY_ANIMATION:
                    mImgHistroy.startAnimation(mHistroyAnimation);
                    mImgHistroy.setVisibility(View.VISIBLE);
                    break;

                case MSG_STOP_DOWNHISTROY_ANIMATION:
                    mImgHistroy.clearAnimation();
                    mImgHistroy.setVisibility(View.INVISIBLE);
                    break;
            }

        }
    };

    private AdapterView.OnItemClickListener mPBListListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Message msg = mUpdateUIhandler.obtainMessage();
            msg.what = MSG_UPDATA_KEY_INPUT;
            msg.arg1 = KEY_CMD_SHOWNUMBER;
            msg.obj = mPBAdapter.getNumber(position);
            mUpdateUIhandler.sendMessage(msg);
        }
    };

    private AdapterView.OnItemClickListener mSearchListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Message msg = mUpdateUIhandler.obtainMessage();
            msg.what = MSG_UPDATA_KEY_INPUT;
            msg.arg1 = KEY_CMD_SHOWNUMBER;
            msg.obj = mSearchList.get(position).get(PHONEBOOK_NUMBER);
            mUpdateUIhandler.sendMessage(msg);
        }
    };

    private AdapterView.OnItemClickListener mHistroyListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Message msg = mUpdateUIhandler.obtainMessage();
            msg.what = MSG_UPDATA_KEY_INPUT;
            msg.arg1 = KEY_CMD_SHOWNUMBER;
            msg.obj = mCallAdapter.getNumber(position);
            mUpdateUIhandler.sendMessage(msg);
        }
    };

    private BluetoothPhoneBookClass.onListChangeListener mBTPhoneBookListener = new BluetoothPhoneBookClass.onListChangeListener() {
        @Override
        public void onListener(int what, int arg1, int arg2) {
            Log.d(BLUETOOTHPHONEBOOKTAG, "mBTPhoneBookListener what is " + what + " arg1 is " + arg1);
            Message msg;
            switch (what) {
                case BluetoothPhoneBookClass.BLUETOOTH_SERVICE_READY:
                    mBluetoothPhoneBookHandler.sendEmptyMessage(what);
                    break;

                case BluetoothPhoneBookClass.BLUETOOTH_PHONEBOOK_DOWN_CONTACTS_INDEX:
                    mUpdateUIhandler.sendEmptyMessage(MSG_ADD_PBLIST);
                    break;

                case BluetoothPhoneBookClass.BLUETOOTH_PHONEBOOK_DOWN_CALLHISTROY_INDEX:
                    mUpdateUIhandler.sendEmptyMessage(MSG_ADD_HISTROYLIST);
                    break;

                case BluetoothPhoneBookClass.BLUETOOTH_PHONEBOOK_DOWN_CALLHISTROY_STATE:
                    mCallHistroyState = arg1;
                    if (PHONEBOOK_STATE_START == arg1) {
                        isShowHistroy = true;
                        mUpdateUIhandler.sendEmptyMessage(MSG_START_DOWNHISTROY_ANIMATION);
                        mUpdateUIhandler.sendEmptyMessage(MSG_ADD_HISTROYLIST);
                    } else if (PHONEBOOK_STATE_FINSH == arg1){
                        mUpdateUIhandler.sendEmptyMessage(MSG_STOP_DOWNHISTROY_ANIMATION);
                        mUpdateUIhandler.sendEmptyMessage(MSG_ADD_HISTROYLIST);
                    } else if (PHONEBOOK_STATE_STOP == arg1) {
                        mUpdateUIhandler.sendEmptyMessage(MSG_STOP_DOWNHISTROY_ANIMATION);
                        mUpdateUIhandler.sendEmptyMessage(MSG_CLEAN_CALL_LIST);
                    } else if (PHONEBOOK_STATE_ERR == arg1) {
                        mUpdateUIhandler.sendEmptyMessage(MSG_STOP_DOWNCANTACTS_ANIMATION);
                        mUpdateUIhandler.sendEmptyMessage(MSG_ADD_HISTROYLIST);
                    }
                    break;

                case BluetoothPhoneBookClass.BLUETOOTH_PHONEBOOK_DOWN_CONITICS_STATE:
                    mContactsState = arg1;
                    if (PHONEBOOK_STATE_START == arg1) {
                        isShowContacts = true;
                        mUpdateUIhandler.sendEmptyMessage(MSG_START_DOWNCANTACTS_ANIMATION);
                        mUpdateUIhandler.sendEmptyMessage(MSG_ADD_PBLIST);
                    } else if (PHONEBOOK_STATE_FINSH == arg1){
                        mUpdateUIhandler.sendEmptyMessage(MSG_STOP_DOWNCANTACTS_ANIMATION);
                        mUpdateUIhandler.sendEmptyMessage(MSG_ADD_PBLIST);
                    } else if (PHONEBOOK_STATE_STOP == arg1) {
                        mUpdateUIhandler.sendEmptyMessage(MSG_STOP_DOWNCANTACTS_ANIMATION);
                        if (isDown) {
                            mBluetoothPhoneBookHandler.sendEmptyMessage(MSG_START_DOWNLOAD);
                        }
                        mUpdateUIhandler.sendEmptyMessage(MSG_CLEAN_PB_LIST);
                    } else if (PHONEBOOK_STATE_ERR == arg1) {
                        mUpdateUIhandler.sendEmptyMessage(MSG_STOP_DOWNCANTACTS_ANIMATION);
                        mUpdateUIhandler.sendEmptyMessage(MSG_ADD_PBLIST);
                    }
                    break;

                case BluetoothPhoneBookClass.BLUETOOTH_MSG_DISCONNECT:
                    mUpdateUIhandler.sendEmptyMessage(MSG_FINSH_ACTIVITY);
                    break;
            }
        }

        @Override
        public void onIntent(Intent intent) {
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(BLUETOOTHPHONEBOOKTAG, "action = " + action);
            if (action.equals("com.autochips.bluetooth.phonebook")) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(CALLHISTROY_TYPE, intent.getStringExtra(CALLHISTROY_TYPE));
                map.put(CALLHISTROY_NUMBER, intent.getStringExtra(CALLHISTROY_NUMBER));
                map.put(CALLHISTROY_NAME, intent.getStringExtra(CALLHISTROY_NAME));
                mCallList.add(0, map);
                mUpdateUIhandler.sendEmptyMessage(MSG_ADD_HISTROYLIST);
            }
        }
    };

    private void initView() {
        mTextKeyboard = (TextView)findViewById(R.id.text_keyboard_input);
        mBtnKeyboardDelete = (Button)findViewById(R.id.btn_keyboard_delete);
        mBtnKeyboardDelete.setOnClickListener(this);
        mBtnKeyboardDelete.setOnLongClickListener(this);
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

        mBtnCall = (Button)findViewById(R.id.btn_phonebook_call);
        mBtnCall.setOnClickListener(this);
        mBtnSetShow = (Button)findViewById(R.id.btn_phonebook_set_up);
        mBtnSetShow.setOnClickListener(this);
        mBtnSetHide = (Button)findViewById(R.id.btn_phonebook_set_down);
        mBtnSetHide.setOnClickListener(this);
        mBtnSet = (ImageButton)findViewById(R.id.imgbtn_phonebook_frame_set);
        mBtnSet.setOnClickListener(this);
        mBtnDownLoader = (ImageButton)findViewById(R.id.imgbtn_phonebook_frame_download);
        mBtnDownLoader.setOnClickListener(this);
        mCheckAotoAcept = (CheckBox)findViewById(R.id.checkbox_phonebook_check);
        mCheckAotoAcept.setOnCheckedChangeListener(this);

        mLayoutSetFrame = (RelativeLayout)findViewById(R.id.layout_phonebook_frame);
        mPBListView = (ListView)findViewById(R.id.list_phonebook_contacts);
        mPBAdapter = new BluetoothPhoneBookAdapter(this);
        mPBListView.setAdapter(mPBAdapter);
        mPBListView.setOnItemClickListener(mPBListListener);

        mCallListView = (ListView)findViewById(R.id.list_phonebook_callhistroy);
        mCallAdapter = new BluetoothCallHistroyAdapter(this, mCallList);
        mCallListView.setAdapter(mCallAdapter);
        mCallListView.setOnItemClickListener(mHistroyListener);

        mSearchView = (ListView)findViewById(R.id.list_phonebook_search);
        mSearchAdapter = new BluetoothSearchAdapter(this, mSearchList);
        mSearchView.setAdapter(mSearchAdapter);
        mSearchView.setOnItemClickListener(mSearchListener);


        mDownLoaderGroup = (RadioGroup)findViewById(R.id.radiogrout_downlaoder);
        mDownLoaderGroup.setOnCheckedChangeListener(this);
        mBtnContacts = (RadioButton)findViewById(R.id.radiobutton_contacts);
        mBtnCallHistroy = (RadioButton)findViewById(R.id.radiobutton_call);

        mImgContacts = (ImageView)findViewById(R.id.img_phonebook_downcontacts);
        mImgContacts.setImageResource(R.drawable.bluetoothset_loading);
        mContactsAnimation = AnimationUtils.loadAnimation(this, R.anim.loading);
        mContactsAnimation.setInterpolator(new LinearInterpolator());
        mImgContacts.setVisibility(View.INVISIBLE);

        mImgHistroy = (ImageView)findViewById(R.id.img_phonebook_downhistory);
        mImgHistroy.setImageResource(R.drawable.bluetoothset_loading);
        mHistroyAnimation = AnimationUtils.loadAnimation(this, R.anim.loading);
        mHistroyAnimation.setInterpolator(new LinearInterpolator());
        mImgHistroy.setVisibility(View.INVISIBLE);
    }

    private void updataText(int cmd, String str) {
        Log.d(BLUETOOTHPHONEBOOKTAG, "updataText text = " + str);

        switch (cmd) {
            case KEY_CMD_DELETE:
                if (mCallNumber == null) {
                    return;
                }
                if (mCallNumber.length() > 1) {
                    mCallNumber = mCallNumber.substring(0, (mCallNumber.length() - 1));
                } else {
                    mCallNumber = null;
                }

                mTextKeyboard.setText(mCallNumber);
                break;

            case KEY_CMD_DELETE_ALL:
                if (mCallNumber == null) {
                    return;
                }

                mCallNumber = null;
                mTextKeyboard.setText(mCallNumber);
                break;

            case KEY_CMD_SHOWNUMBER:
                if (str == null) {
                    return;
                }
                mCallNumber = str;
                mTextKeyboard.setText(mCallNumber);
                break;

            case KEY_CMD_SHOW_KEYWOARD:
                if (str == null) {
                    return;
                }
                if (mCallNumber == null) {
                    mCallNumber = str;
                } else {
                    if (mCallNumber.length() >= BLUETOOTH_PHONEBOOK_CALL_NUMBER_MAX) {
                        return;
                    } else {
                        mCallNumber += str;
                    }
                }
                mTextKeyboard.setText(mCallNumber);
                break;
        }

        if (cmd != KEY_CMD_SHOWNUMBER) {
            if ((mCallNumber != null) && (mCallNumber.length() > 0)) {
                mSearchKey = mCallNumber;
                mUpdateUIhandler.sendEmptyMessage(MSG_CLEAAR_SEARCH_LIST);
                Message msg = mBluetoothPhoneBookHandler.obtainMessage();
                msg.what = MSG_SEARCH;
                msg.obj = mCallNumber;
                mBluetoothPhoneBookHandler.sendMessage(msg);
                synchronized (BluetoothPhoneBookActivity.this) {
                    if (mSearchCancellationSignal != null) {
                        mSearchCancellationSignal.cancel();
                    }
                }
            } else {
                mUpdateUIhandler.sendEmptyMessage(MSG_HIDE_SEARCH_LIST);
            }
        }

    }

    private void callPhone() {
        if (mCallNumber == null) {
            return;
        }

        mBluetoothPhoneBookClass.hfpCall(mCallNumber);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            mBluetoothPhoneBookClass.setAutoAcept(true);
        } else {
            mBluetoothPhoneBookClass.setAutoAcept(false);
        }

        mUpdateUIhandler.sendEmptyMessage(MSG_HIDE_FRAME_LAYOUT);
        
    }

    @Override
    public void  onCheckedChanged(RadioGroup group, int checkedId) {
        switch (group.getCheckedRadioButtonId()) {
            case R.id.radiobutton_contacts:
                isContacts = true;
                mSearchView.setVisibility(View.GONE);
                mCallListView.setVisibility(View.GONE);
                mPBListView.setVisibility(View.VISIBLE);
                break;

            case R.id.radiobutton_call:
                isContacts = false;
                mSearchView.setVisibility(View.GONE);
                mPBListView.setVisibility(View.GONE);
                mCallListView.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        Message msg;
        String str;
        msg = mUpdateUIhandler.obtainMessage();
        switch (v.getId()) {
            case R.id.imgbtn_keyboard_1:
                str = "1";
                msg.what = MSG_UPDATA_KEY_INPUT;
                msg.obj = str;
                msg.arg1 = KEY_CMD_SHOW_KEYWOARD;
                mUpdateUIhandler.sendMessage(msg);
                break;

            case R.id.imgbtn_keyboard_2:
                str = "2";
                msg.what = MSG_UPDATA_KEY_INPUT;
                msg.obj = str;
                msg.arg1 = KEY_CMD_SHOW_KEYWOARD;
                mUpdateUIhandler.sendMessage(msg);
                break;

            case R.id.imgbtn_keyboard_3:
                str = "3";
                msg.what = MSG_UPDATA_KEY_INPUT;
                msg.obj = str;
                msg.arg1 = KEY_CMD_SHOW_KEYWOARD;
                mUpdateUIhandler.sendMessage(msg);
                break;

            case R.id.imgbtn_keyboard_4:
                str = "4";
                msg.what = MSG_UPDATA_KEY_INPUT;
                msg.obj = str;
                msg.arg1 = KEY_CMD_SHOW_KEYWOARD;
                mUpdateUIhandler.sendMessage(msg);
                break;

            case R.id.imgbtn_keyboard_5:
                str = "5";
                msg.what = MSG_UPDATA_KEY_INPUT;
                msg.obj = str;
                msg.arg1 = KEY_CMD_SHOW_KEYWOARD;
                mUpdateUIhandler.sendMessage(msg);
                break;

            case R.id.imgbtn_keyboard_6:
                str = "6";
                msg.what = MSG_UPDATA_KEY_INPUT;
                msg.obj = str;
                msg.arg1 = KEY_CMD_SHOW_KEYWOARD;
                mUpdateUIhandler.sendMessage(msg);
                break;

            case R.id.imgbtn_keyboard_7:
                str = "7";
                msg.what = MSG_UPDATA_KEY_INPUT;
                msg.obj = str;
                msg.arg1 = KEY_CMD_SHOW_KEYWOARD;
                mUpdateUIhandler.sendMessage(msg);
                break;

            case R.id.imgbtn_keyboard_8:
                str = "8";
                msg.what = MSG_UPDATA_KEY_INPUT;
                msg.obj = str;
                msg.arg1 = KEY_CMD_SHOW_KEYWOARD;
                mUpdateUIhandler.sendMessage(msg);
                break;

            case R.id.imgbtn_keyboard_9:
                str = "9";
                msg.what = MSG_UPDATA_KEY_INPUT;
                msg.obj = str;
                msg.arg1 = KEY_CMD_SHOW_KEYWOARD;
                mUpdateUIhandler.sendMessage(msg);
                break;

            case R.id.imgbtn_keyboard_0:
                str = "0";
                msg.what = MSG_UPDATA_KEY_INPUT;
                msg.obj = str;
                msg.arg1 = KEY_CMD_SHOW_KEYWOARD;
                mUpdateUIhandler.sendMessage(msg);
                break;

            case R.id.imgbtn_keyboard_a:
                str = "*";
                msg.what = MSG_UPDATA_KEY_INPUT;
                msg.obj = str;
                msg.arg1 = KEY_CMD_SHOW_KEYWOARD;
                mUpdateUIhandler.sendMessage(msg);
                break;

            case R.id.imgbtn_keyboard_b:
                str = "#";
                msg.what = MSG_UPDATA_KEY_INPUT;
                msg.obj = str;
                msg.arg1 = KEY_CMD_SHOW_KEYWOARD;
                mUpdateUIhandler.sendMessage(msg);
                break;

            case R.id.btn_keyboard_delete:
                msg.what = MSG_UPDATA_KEY_INPUT;
                msg.arg1 = KEY_CMD_DELETE;
                mUpdateUIhandler.sendMessage(msg);
                break;

            case R.id.btn_phonebook_set_up:
                mUpdateUIhandler.sendEmptyMessage(MSG_SHOW_FRAME_LAYOUT);
                break;

            case R.id.btn_phonebook_set_down:
                mUpdateUIhandler.sendEmptyMessage(MSG_HIDE_FRAME_LAYOUT);
                break;

            case R.id.btn_phonebook_call:
                mBluetoothPhoneBookHandler.sendEmptyMessage(MSG_PHONEBOOK_DIALOG);
                break;

            case R.id.imgbtn_phonebook_frame_set:
                mUpdateUIhandler.sendEmptyMessage(MSG_HIDE_FRAME_LAYOUT);
                Intent intentActivity = new Intent();
                intentActivity.putExtra("KEY_DATA_a", 7);
                intentActivity.putExtra("KEY_DATA_b", 0);
                intentActivity.setClassName("com.autochips.bluetooth", "com.autochips.bluetooth.BluetoothSetActivity");
                startActivity(intentActivity);
                break;

            case R.id.imgbtn_phonebook_frame_download:
                mBluetoothPhoneBookHandler.removeMessages(MSG_ADD_CONTACT);
                mUpdateUIhandler.sendEmptyMessage(MSG_HIDE_FRAME_LAYOUT);
                mBluetoothPhoneBookHandler.sendEmptyMessage(MSG_STOP_DOWNLOAD);
                break;

        }
    }

    @Override
    public boolean onLongClick(View v) {
        Log.d(BLUETOOTHPHONEBOOKTAG, "onLongClick");
        Message msg;
        msg = mUpdateUIhandler.obtainMessage();
        switch (v.getId()) {
           case R.id.btn_keyboard_delete:
                msg.what = MSG_UPDATA_KEY_INPUT;
                msg.arg1 = KEY_CMD_DELETE_ALL;
                mUpdateUIhandler.sendMessage(msg);
                break;
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_phone_book);

        HandlerThread MsgThread = new HandlerThread("MSG_Thread");
        MsgThread.start();

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.autochips.bluetooth.phonebook");
        registerReceiver(mReceiver, filter);

        mCallList = new ArrayList<HashMap<String, String>>();
        mSearchList = new ArrayList<HashMap<String, String>>();
        mPinyingList = new ArrayList<String>();
        initView();
        mBluetoothPhoneBookClass = new BluetoothPhoneBookClass();
        mBluetoothPhoneBookClass.intiBluetoothPhoneBook(this, mBTPhoneBookListener);
        mBluetoothPhoneBookHandler = new BluetoothPhoneBookHandler(MsgThread.getLooper());
    }

    @Override
    protected void onDestroy() {
        Log.d(BLUETOOTHPHONEBOOKTAG, "onDestroy");
        super.onDestroy();
        unregisterReceiver(mReceiver);
        mPBAdapter.setCursor(null);
        mCallList.clear();
        mCallAdapter.setCursor(null);
    }

    private String getContactByNumber(String number) {
        String contactName = "";
        int contactID = -1;
        String remoteAddr = "";
        String recordAddr = "";

        remoteAddr = mBluetoothPhoneBookClass.getRemoteAddr();
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

    private final class SearchResult {
        public ArrayList<HashMap<String, String>> pbList;
        public String searchKey;
    }

    private Cursor getCursor (int type) {
        Cursor cursor = null;
        String remoteAddr = mBluetoothPhoneBookClass.getRemoteAddr();
        if (remoteAddr == null) {
            return cursor;
        }
        if ("" == remoteAddr) {
            return cursor;
        }

        ContentResolver resolver = getContentResolver();

        switch (type) {
            case PHONEBOOK_CONTACT_TYPE:
                cursor = resolver.query(CONTENT_URI, new String[]{ID, DISPLAY_NAME, REMOTE_PATH}, CONTACT_SELECT_LIMIT,
                        new String[]{remoteAddr},
                        null);
                break;

            case PHONEBOOK_HISTROY_TYPE:
                cursor = resolver.query(CALL_CONTENT_URI, new String[] {CALL_PHONE_NUMBER, CALL_DISPLAY_NAME, CALL_CALL_TYPE, CALL_CALL_TIME},
                        CALL_SELECT_LIMIT,
                        new String[]{remoteAddr},
                        "call_time DESC");
                break;
        }

        return cursor;
    }

    private String getNametoNumber (String name) {
        String number = "";
        if (name == null) {
            return number;
        }

        if (name.equals("")) {
            return number;
        }

        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if ((c >= 'a') && (c <= 'c')) {
                if (number.equals("")) {
                    number = "2";
                } else {
                    number = number + "2";
                }
            } else if ((c >= 'd') && (c <= 'f')) {
                if (number.equals("")) {
                    number = "3";
                } else {
                    number = number + "3";
                }
            } else if ((c >= 'g') && (c <= 'i')) {
                if (number.equals("")) {
                    number = "4";
                } else {
                    number = number + "4";
                }
            } else if ((c >= 'j') && (c <= 'l')) {
                if (number.equals("")) {
                    number = "5";
                } else {
                    number = number + "5";
                }
            } else if ((c >= 'm') && (c <= 'o')) {
                if (number.equals("")) {
                    number = "6";
                } else {
                    number = number + "6";
                }
            } else if ((c >= 'p') && (c <= 's')) {
                if (number.equals("")) {
                    number = "7";
                } else {
                    number = number + "7";
                }
            } else if ((c >= 't') && (c <= 'v')) {
                if (number.equals("")) {
                    number = "8";
                } else {
                    number = number + "8";
                }
            } else if ((c >= 'w') && (c <= 'z')) {
                if (number.equals("")) {
                    number = "9";
                } else {
                    number = number + "9";
                }
            }

        }
        return number;


    }

    class BluetoothPhoneBookHandler extends Handler {

        public BluetoothPhoneBookHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Message message;
            Log.d(BLUETOOTHPHONEBOOKTAG, "BluetoothPhoneBookHandler msg is " + msg.what);
            switch (msg.what) {
                case MSG_PHONEBOOK_DIALOG:
                    callPhone();
                    break;

                case BluetoothPhoneBookClass.BLUETOOTH_SERVICE_READY:
                    mUpdateUIhandler.sendEmptyMessage(MSG_UPDATA_AUTO_ACEPT_CHECKBOX);
                    sendEmptyMessage(MSG_GET_DONWCONTACTS_STATE);
                    break;

                case MSG_GET_DONWCONTACTS_STATE:
                    mContactsState = mBluetoothPhoneBookClass.getContactsDownState();
                    if (PHONEBOOK_STATE_START == mContactsState) {
                        mUpdateUIhandler.sendEmptyMessage(MSG_START_DOWNCANTACTS_ANIMATION);
                    } else {
                        mUpdateUIhandler.sendEmptyMessage(MSG_ADD_PBLIST);
                        sendEmptyMessage(MSG_GET_DOWNHISTROY_STATE);
                    }
                    break;

                case MSG_GET_DOWNHISTROY_STATE:
                    mCallHistroyState = mBluetoothPhoneBookClass.getContCallhistroyState();
                    if (PHONEBOOK_STATE_START == mCallHistroyState) {
                        mUpdateUIhandler.sendEmptyMessage(MSG_START_DOWNHISTROY_ANIMATION);
                    } else {
                        mUpdateUIhandler.sendEmptyMessage(MSG_ADD_HISTROYLIST);
                    }
                    break;


                case MSG_STOP_DOWNLOAD:
                    if ((mCallHistroyState != PHONEBOOK_STATE_START) && (mContactsState != PHONEBOOK_STATE_START)) {
                        sendEmptyMessage(MSG_START_DOWNLOAD);
                    } else {
                        isDown = true;
                        mBluetoothPhoneBookClass.stopDownloadContacts();
                    }
                    break;

                case MSG_START_DOWNLOAD:
                    mBluetoothPhoneBookClass.downloadContacts();
                    break;

                case MSG_SEARCH:
                    mUpdateUIhandler.sendEmptyMessage(MSG_SHOW_SEARCH_LIST);
                    try {
                        searchContact((String)msg.obj);
                    } catch (Exception e) {
                    // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
            }
        }

        private void searchContact(String key) throws Exception {
            Log.d(BLUETOOTHPHONEBOOKTAG, "searchContact key = " + key);
            SearchResult result;

            if (key == null) {
                return;
            }

            String remoteAddr = mBluetoothPhoneBookClass.getRemoteAddr();
            if (remoteAddr == null) {
                return;
            }
            if ("" == remoteAddr) {
                return;
            }

            ArrayList<HashMap<String, String>> pbList = new ArrayList<HashMap<String, String>>();
            
            ContentResolver resolver = getContentResolver();
            Cursor cursor = null;

            try {
                synchronized (BluetoothPhoneBookActivity.this) {
                    mSearchCancellationSignal = new CancellationSignal();
                }

                cursor = resolver.query(CONTENT_URI, new String[]{ID, DISPLAY_NAME, REMOTE_PATH}, CONTACT_SELECT_LIMIT,
                        new String[]{remoteAddr}, null,
                        mSearchCancellationSignal);
            } catch (OperationCanceledException e) {
                e.printStackTrace();
                if (cursor != null) {
                    cursor.close();
                }

                synchronized (BluetoothPhoneBookActivity.this) {
                    mSearchCancellationSignal = null;
                }
                return;
            } finally {
                synchronized (BluetoothPhoneBookActivity.this) {
                    mSearchCancellationSignal = null;
                }
            }
            while (cursor.moveToNext() && !mBluetoothPhoneBookHandler.hasMessages(MSG_SEARCH)) {
                int contactID = cursor.getInt(cursor.getColumnIndex(ID));
                Cursor dataCursor = resolver.query(DATA_CONTENT_URI, new String[]{MIME_TYPE, DATA1, DATA2}, CONTACTDATA_SELECT,
                        new String[]{String.valueOf(contactID)}, null);
                HashMap<String, String> map = null;
                while (dataCursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));
                    String firstLetters = FirstLetterUtil.getFirstLetter(name);
                    Log.d(BLUETOOTHPHONEBOOKTAG, "firstLetters key = " + firstLetters);
                    String pinying = getNametoNumber(firstLetters);
                    Log.d(BLUETOOTHPHONEBOOKTAG, "pinying key = " + pinying);
                    if (pinying.indexOf(key) != -1) {
                        map = new HashMap<String, String>();
                        map.put(PHONEBOOK_NAME, cursor.getString(cursor.getColumnIndex(DISPLAY_NAME)));
                        map.put(PHONEBOOK_NUMBER, dataCursor.getString(dataCursor.getColumnIndex(DATA1)));
                        pbList.add(map);
                    }
                }
                dataCursor.close();
                if (pbList.size() >= 10) {
                    result = new SearchResult();
                    result.searchKey = key;
                    result.pbList = pbList;
                    Message msg = Message.obtain(mUpdateUIhandler, MSG_UPDATA_SEARCH, result);
                    mUpdateUIhandler.sendMessage(msg);
                    pbList = new ArrayList<HashMap<String, String>>();
                }

            }
            cursor.close();
            if (mBluetoothPhoneBookHandler.hasMessages(MSG_SEARCH)) {
                return;
            }




            try {
                synchronized (BluetoothPhoneBookActivity.this) {
                    mSearchCancellationSignal = new CancellationSignal();
                }

                cursor = resolver.query(DATA_CONTENT_URI, new String[] {CONTACTS_ID, DISPLAY_NAME, MIME_TYPE, DATA1, DATA2},
                                        CONTACTDATA_SEARCH,
                                        new String[] {"%"+key+"%", "%"+key+"%"},
                                        null,
                                        mSearchCancellationSignal);
            } catch (OperationCanceledException e) {
                e.printStackTrace();
                if (cursor != null) {
                    cursor.close();
                }

                synchronized (BluetoothPhoneBookActivity.this) {
                    mSearchCancellationSignal = null;
                }
                return;
            } finally {
                synchronized (BluetoothPhoneBookActivity.this) {
                    mSearchCancellationSignal = null;
                }
            }

            while (cursor.moveToNext() && !mBluetoothPhoneBookHandler.hasMessages(MSG_SEARCH)) {
                HashMap<String, String> map = null;
                int contactID = cursor.getInt(cursor.getColumnIndex(CONTACTS_ID));
                Cursor contactsCursor = resolver.query(CONTENT_URI, new String[] {REMOTE_ADDR, REMOTE_PATH},
                                                       CONTACT_SELECT_ID,
                                                       new String[] {String.valueOf(contactID)},
                                                       null);
                if (contactsCursor.moveToNext()) {
                    String contactsBTAddr = contactsCursor.getString(contactsCursor.getColumnIndex(REMOTE_ADDR));
                    if (contactsBTAddr != null && contactsBTAddr.equals(remoteAddr)) {
                        map = new HashMap<String, String>();
                        map.put(PHONEBOOK_NAME, cursor.getString(cursor.getColumnIndex(DISPLAY_NAME)));
                        map.put(PHONEBOOK_NUMBER, cursor.getString(cursor.getColumnIndex(DATA1)));
                        pbList.add(map);
                    }
                }
                contactsCursor.close();
                if (pbList.size() >= 10) {
                    result = new SearchResult();
                    result.searchKey = key;
                    result.pbList = pbList;
                    Message msg = Message.obtain(mUpdateUIhandler, MSG_UPDATA_SEARCH, result);
                    mUpdateUIhandler.sendMessage(msg);
                    pbList = new ArrayList<HashMap<String, String>>();
                }
            }
            cursor.close();
            /*
            Log.d(BLUETOOTHPHONEBOOKTAG, "searchContact111 key = " + key);
            for(int i = 0; i < mPBList.size(); i++) {
                String firstLetters = FirstLetterUtil.getFirstLetter(mPBList.get(i).get(PHONEBOOK_NAME));
                Log.d(BLUETOOTHPHONEBOOKTAG, "firstLetters key = " + firstLetters);
                // 不区分大小
                Pattern firstLetterMatcher = Pattern.compile("bs",
                        Pattern.CASE_INSENSITIVE);
                if (firstLetterMatcher.matcher(firstLetters).find()) {
                    HashMap<String, String> map = null;
                    map = new HashMap<String, String>();
                    map.put(PHONEBOOK_NAME, mPBList.get(i).get(PHONEBOOK_NAME));
                    map.put(PHONEBOOK_NUMBER, mPBList.get(i).get(PHONEBOOK_NUMBER));
                    pbList.add(map);
                }
            }*/

            if (pbList.size() != 0) {
                result = new SearchResult();
                result.searchKey = key;
                result.pbList = pbList;
                Message msg = Message.obtain(mUpdateUIhandler, MSG_UPDATA_SEARCH, result);
                mUpdateUIhandler.sendMessage(msg);
            }

            return;
        }
    }

    class BluetoothSearchAdapter extends BaseAdapter {

        private ArrayList<HashMap<String, String>> mList;
        private Context mContext;

        public BluetoothSearchAdapter(Context context, ArrayList<HashMap<String, String>> data) {
            mList = data;
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
                convertView = LayoutInflater.from(mContext).inflate(R.layout.bluetoothrecord, null);
                holder.mContactsName = (TextView)convertView.findViewById(R.id.text_record_name);
                holder.mContactsNumber = (TextView)convertView.findViewById(R.id.text_record_number);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }

            holder.mContactsName.setText(mList.get(position).get(PHONEBOOK_NAME));
            holder.mContactsNumber.setText(mList.get(position).get(PHONEBOOK_NUMBER));

            ListView.LayoutParams params = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT,58);
            convertView.setLayoutParams(params);
            return convertView;
        }

        public final class ViewHolder {
            public TextView mContactsName;
            public TextView mContactsNumber;
        }

    }

    class BluetoothPhoneBookAdapter extends BaseAdapter {

        private Context mContext;
        Cursor mCursor;

        public BluetoothPhoneBookAdapter(Context context) {
            mContext = context;
        }

        public void setCursor (Cursor cursor) {
            if (mCursor != null) {
                mCursor.close();
            }
            mCursor = cursor;
            notifyDataSetChanged();
        }

        public String getNumber (int position) {
            String number = "";
            if (mCursor != null && mCursor.moveToFirst()) {
                mCursor.moveToPosition(position);
                ContentResolver resolver = getContentResolver();
                int contactID = mCursor.getInt(mCursor.getColumnIndex(ID));
                Cursor dataCursor = resolver.query(DATA_CONTENT_URI, new String[]{MIME_TYPE, DATA1, DATA2}, CONTACTDATA_SELECT,
                        new String[]{String.valueOf(contactID)}, null);
                while (dataCursor.moveToNext()) {
                    number = dataCursor.getString(dataCursor.getColumnIndex(DATA1));
                }
                dataCursor.close();
                return number;
            }
            return number;
        }

        @Override
        public int getCount() {
            if (mCursor != null) {
                return mCursor.getCount();
            }
            return 0;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            if (mCursor != null) {
                mCursor.moveToPosition(position);
                return  mCursor;
            }

            return null;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null ) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.bluetoothrecord, null);
                holder.mContactsName = (TextView)convertView.findViewById(R.id.text_record_name);
                holder.mContactsNumber = (TextView)convertView.findViewById(R.id.text_record_number);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            if (mCursor != null && mCursor.moveToFirst()) {
                mCursor.moveToPosition(position);
                ContentResolver resolver = getContentResolver();
                int contactID = mCursor.getInt(mCursor.getColumnIndex(ID));
                Cursor dataCursor = resolver.query(DATA_CONTENT_URI, new String[]{MIME_TYPE, DATA1, DATA2}, CONTACTDATA_SELECT,
                        new String[]{String.valueOf(contactID)}, null);
                HashMap<String, String> map = null;
                while (dataCursor.moveToNext()) {
                    map = new HashMap<String, String>();
                    map.put(PHONEBOOK_NAME, mCursor.getString(mCursor.getColumnIndex(DISPLAY_NAME)));
                    map.put(PHONEBOOK_NUMBER, dataCursor.getString(dataCursor.getColumnIndex(DATA1)));
                }
                if (map == null) {
                    map = new HashMap<String, String>();
                    map.put(PHONEBOOK_NAME, mCursor.getString(mCursor.getColumnIndex(DISPLAY_NAME)));
                    map.put(PHONEBOOK_NUMBER, "");
                }
                holder.mContactsName.setText(map.get(PHONEBOOK_NAME));
                holder.mContactsNumber.setText(map.get(PHONEBOOK_NUMBER));
                dataCursor.close();
            }
            ListView.LayoutParams params = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT,58);
            convertView.setLayoutParams(params);
            return convertView;
        }

        public final class ViewHolder {
            public TextView mContactsName;
            public TextView mContactsNumber;
        }

    }

    class BluetoothCallHistroyAdapter extends BaseAdapter {

        private ArrayList<HashMap<String, String>> mList;
        private Context mContext;
        private Cursor mCursor;

        public BluetoothCallHistroyAdapter(Context context, ArrayList<HashMap<String, String>> data) {
            mList = data;
            mContext = context;
        }

        public void setCursor (Cursor cursor) {
            if (mCursor != null) {
                mCursor.close();
            }
            mCursor = cursor;
            notifyDataSetChanged();
        }

        public String getNumber (int position) {
            String number = "";
            if (position < mList.size()) {
                number = mList.get(position).get(CALLHISTROY_NUMBER);
            } else {
                if (mCursor != null && mCursor.moveToFirst()) {
                    mCursor.moveToPosition(position - mList.size());
                    number = mCursor.getString(mCursor.getColumnIndex(CALL_PHONE_NUMBER));
                }
            }
            return number;
        }

        @Override
        public int getCount() {
            if (mCursor != null) {
                return (mCursor.getCount() + mList.size());
            }
            return mList.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            if (position < mList.size()) {
                return mList.get(position);
            } else {
                if (mCursor != null) {
                    mCursor.moveToPosition(position - mList.size());
                    return mCursor;
                }
            }
            return null;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d(BLUETOOTHPHONEBOOKTAG, "BluetoothCallHistroyAdapter getView position IS " + position);
            ViewHolder holder = null;
            String number = null;
            String type = null;
            String name = null;
            if (convertView == null ) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.bluetoothrecord, null);
                holder.mCallHistroyName = (TextView)convertView.findViewById(R.id.text_record_name);
                holder.mCallHistroyNumber = (TextView)convertView.findViewById(R.id.text_record_number);
                holder.mCallHistroyType = (ImageView)convertView.findViewById(R.id.iagem_call_type);
                holder.mCallHistroyType.setVisibility(View.VISIBLE);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }

           if (position < mList.size()) {
                name = mList.get(position).get(CALLHISTROY_NAME);
                type = mList.get(position).get(CALLHISTROY_TYPE);
                number = mList.get(position).get(CALLHISTROY_NUMBER);
            } else {
                if (mCursor != null && mCursor.moveToFirst()) {
                    mCursor.moveToPosition(position - mList.size());
                    number = mCursor.getString(mCursor.getColumnIndex(CALL_PHONE_NUMBER));
                    type = mCursor.getString(mCursor.getColumnIndex(CALL_CALL_TYPE));
                    name = mCursor.getString(mCursor.getColumnIndex(CALL_DISPLAY_NAME));
                    if (name.equals("")) {
                        name = getContactByNumber(number);
                    }
                }
            }

            if (name == null) {
                holder.mCallHistroyName.setText(R.string.phonebook_unknown);
            } else if ("".equals(name)) {
                holder.mCallHistroyName.setText(R.string.phonebook_unknown);
            } else {
                holder.mCallHistroyName.setText(name);
            }
            holder.mCallHistroyNumber.setText(number);
            if (type.equals(CALLHISTROY_TYPE_INCOMING)) {
                holder.mCallHistroyType.setImageDrawable(getResources().getDrawable(R.drawable.incoming));
            } else if (type.equals(CALLHISTROY_TYPE_OUTGOING)) {
                holder.mCallHistroyType.setImageDrawable(getResources().getDrawable(R.drawable.outgoing_calls));
            } else if (type.equals(CALLHISTROY_TYPE_MISSED)) {
                holder.mCallHistroyType.setImageDrawable(getResources().getDrawable(R.drawable.missed_call));
            }


            ListView.LayoutParams params = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT,58);
            convertView.setLayoutParams(params);
            return convertView;
        }

        public final class ViewHolder {
            public TextView mCallHistroyName;
            public TextView mCallHistroyNumber;
            public ImageView mCallHistroyType;
        }

    }
}
