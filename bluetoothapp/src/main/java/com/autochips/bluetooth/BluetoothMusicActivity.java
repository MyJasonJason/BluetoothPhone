package com.autochips.bluetooth;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class BluetoothMusicActivity extends Activity implements View.OnClickListener{
    public final static String BLUETOOTH_MUSIC_ACTIVITY = "BluetoothMusicActivity";

    public final static int MSG_INIT_OK = 0;
    public final static int MSG_UPDATA_ALL = 1;
    public final static int MSG_UPDATA_METADATA = 2;
    public final static int MSG_UPDATA_PLAYSTATE = 3;
    public final static int MSG_CONTROL_PLAY = 4;
    public final static int MSG_CONTROL_PAUSE = 5;
    public final static int MSG_CONTROL_NEXT = 6;
    public final static int MSG_CONTROL_PRE = 7;


    private final static String AVRCP_METADATA_TITLE = "title";
    private final static String AVRCP_METADATA_ARTIST = "artist";
    private final static String AVRCP_MEIADATA_ALBUM = "album";
    private final static String AVRCP_PLAY_STATE = "play_state";
    private final static String AVRCP_PLAY_SONG_LEN = "song_len";
    private final static String AVRCP_PLAY_SONG_POS = "song_pos";
    private final static String BLUETOOTH_MUSIC_INTENT_TYPE = "intent_type";
    private final static String BLUETOOTH_MUSIC_INTEN_TYPE_METADATACHANGE = "type_metadatachange";
    private final static String BLUETOOTH_MUSIC_INTEN_TYPE_PLAYSTATECHANGE = "type_playstatechange";



    private TextView mTextSongname;
    private TextView mTextAlbum;
    private TextView mTextSinger;
    private Button mBtnPre;
    private Button mBtnPlay;
    private Button mBtnPause;
    private Button mBtnNext;
    private TextView mTextCurtime;
    private TextView mTextTotalTime;
    private SeekBar mSeekbar;

    private BluetoothMusicClass mBluetoothMusicClass;
    private String mStrTitle;
    private String mStrArtist;
    private String mStrAlbum;
    private byte mPlayState;
    private int mMusicLength;
    private int mMusicPlayTime;
    private byte mCurState = BluetoothMusicClass.MUSIC_STATE_STOP;
    private boolean isCmd = false;
    private boolean isStartPlay = true;


    private BluetoothMusicHandler mBluetoothMusicHandler;

    private BluetoothMusicClass.onListChangeListener mBTMusicListener = new BluetoothMusicClass.onListChangeListener() {
        @Override
        public void onListener(int what, int arg1, int arg2) {
            Log.d(BLUETOOTH_MUSIC_ACTIVITY, "what = " + what);
            switch (what) {
                case BluetoothMusicClass.BLUETOOTH_SERVICE_READY:
                    mBluetoothMusicClass.setPlayerState(true);
                    mBluetoothMusicHandler.sendEmptyMessage(MSG_INIT_OK);
                    break;

                case BluetoothMusicClass.BLUETOOTH_MSG_DISCONNECT:
                    mBluetoothMusicClass.setPlayerState(false);
                    finish();
                    break;

                case BluetoothMusicClass.BLUETOOTH_MSG_AVRCP_CONNECT:
                    if (!isStartPlay) {
                        break;
                    }
                    if (mPlayState != BluetoothMusicClass.MUSIC_STATE_PLAY) {
                        mBluetoothMusicClass.play();
                    }
                    break;
            }
        }

        @Override
        public void onIntent(Intent intent) {
            Log.d(BLUETOOTH_MUSIC_ACTIVITY, "onIntent");
            String intent_type = intent.getStringExtra(BLUETOOTH_MUSIC_INTENT_TYPE);
            Log.d(BLUETOOTH_MUSIC_ACTIVITY, "onIntent intent_type is " + intent_type);
            if (intent_type.equals(BLUETOOTH_MUSIC_INTEN_TYPE_METADATACHANGE)) {
                mStrTitle = intent.getStringExtra(AVRCP_METADATA_TITLE);
                mStrAlbum = intent.getStringExtra(AVRCP_MEIADATA_ALBUM);
                mStrArtist = intent.getStringExtra(AVRCP_METADATA_ARTIST);
                mUpdateUIhandler.sendEmptyMessage(MSG_UPDATA_METADATA);
            } else if (intent_type.equals(BLUETOOTH_MUSIC_INTEN_TYPE_PLAYSTATECHANGE)) {
                getPlayState();
                mUpdateUIhandler.sendEmptyMessage(MSG_UPDATA_PLAYSTATE);
            }
        }
    };

    private Handler mUpdateUIhandler = new Handler() {
        public void handleMessage(Message msg) {
            Log.d(BLUETOOTH_MUSIC_ACTIVITY, "mUpdateUIhandler handleMessage what is " + msg.what);
            switch (msg.what) {
                case MSG_UPDATA_ALL:
                    updataMetaData();
                    updataPlayState();
                    break;

                case MSG_UPDATA_METADATA:
                    updataMetaData();
                    break;

                case MSG_UPDATA_PLAYSTATE:
                    updataPlayState();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_music);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        HandlerThread MsgThread = new HandlerThread("MSG_Thread");
        MsgThread.start();
        isStartPlay = true;
        mBluetoothMusicHandler = new BluetoothMusicHandler(MsgThread.getLooper());
        mBluetoothMusicClass = new BluetoothMusicClass();
        mBluetoothMusicClass.intiBluetoothMusic(this, mBTMusicListener);
        initView();
    }

    @Override
    public void onResume() {
        Log.d(BLUETOOTH_MUSIC_ACTIVITY, "onResume");
        super.onResume();
        isStartPlay = true;
        mBluetoothMusicClass.setPlayerState(true);
    }

    @Override
    public void onPause() {
        Log.d(BLUETOOTH_MUSIC_ACTIVITY, "onPause");
        super.onPause();
        isStartPlay = false;
        mBluetoothMusicClass.setPlayerState(false);
    }

    @Override
    public void onDestroy() {
        Log.d(BLUETOOTH_MUSIC_ACTIVITY, "onDestroy");
        super.onDestroy();
        mBluetoothMusicClass.deinitBluetoothMusic();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_bluetoothmusic_pre:
                mBluetoothMusicHandler.sendEmptyMessage(MSG_CONTROL_PRE);
                break;
            case R.id.btn_bluetoothmusic_next:
                mBluetoothMusicHandler.sendEmptyMessage(MSG_CONTROL_NEXT);
                break;
            case R.id.btn_bluetoothmusic_pause:
                mBluetoothMusicHandler.sendEmptyMessage(MSG_CONTROL_PAUSE);
                break;
            case R.id.btn_bluetoothmusic_play:
                mBluetoothMusicHandler.sendEmptyMessage(MSG_CONTROL_PLAY);
                break;
        }
    }

    private void initView() {
        mTextAlbum = (TextView)findViewById(R.id.text_bluetoothmusic_album);
        mTextSinger = (TextView)findViewById(R.id.text_bluetoothmusic_singer);
        mTextSongname = (TextView)findViewById(R.id.text_bluetoothmusic_songname);
        mTextCurtime = (TextView)findViewById(R.id.text_btmusic_current_time);
        mTextTotalTime = (TextView)findViewById(R.id.text_btmusic_duration_time);

        mSeekbar = (SeekBar)findViewById(R.id.seekbar_btmusic);
        mSeekbar.setEnabled(false);

        mBtnPre = (Button)findViewById(R.id.btn_bluetoothmusic_pre);
        mBtnPre.setOnClickListener(this);
        mBtnPlay = (Button)findViewById(R.id.btn_bluetoothmusic_play);
        mBtnPlay.setOnClickListener(this);
        mBtnPause = (Button)findViewById(R.id.btn_bluetoothmusic_pause);
        mBtnPause.setOnClickListener(this);
        mBtnNext = (Button)findViewById(R.id.btn_bluetoothmusic_next);
        mBtnNext.setOnClickListener(this);
    }

    private void updataMetaData() {
        Log.d(BLUETOOTH_MUSIC_ACTIVITY, "updataMetaData");

        if (mStrTitle == null) {
            mTextSongname.setText(R.string.bluetoothmusic_unkonow);
        } else if(!mStrTitle.trim().isEmpty()){
            mTextSongname.setText(mStrTitle);
        } else {
            mTextSongname.setText(R.string.bluetoothmusic_unkonow);
        }

        if(mStrAlbum == null) {
            mTextAlbum.setText(R.string.bluetoothmusic_unkonow);
        } else if(!mStrAlbum.trim().isEmpty()) {
            mTextAlbum.setText(mStrAlbum);
        } else {
            mTextAlbum.setText(R.string.bluetoothmusic_unkonow);
        }

        if (mStrArtist == null) {
            mTextSinger.setText(R.string.bluetoothmusic_unkonow);
        } else if(!mStrArtist.trim().isEmpty()) {
            mTextSinger.setText(mStrArtist);
        } else {
            mTextSinger.setText(R.string.bluetoothmusic_unkonow);
        }
    }

    private void getMetaData() {
        Log.d(BLUETOOTH_MUSIC_ACTIVITY, "getMetaData");
        if (mBluetoothMusicClass == null) {
            Log.d(BLUETOOTH_MUSIC_ACTIVITY, "mBluetoothMusicClass is null");
            return;
        }

        mStrTitle = mBluetoothMusicClass.getTitle();
        mStrAlbum = mBluetoothMusicClass.getAlbum();
        mStrArtist = mBluetoothMusicClass.getArtist();
    }

    private void getPlayState() {
        if (mBluetoothMusicClass == null) {
            Log.d(BLUETOOTH_MUSIC_ACTIVITY, "mBluetoothMusicClass is null");
            return;
        }

        mPlayState = mBluetoothMusicClass.getPlayState();
        mMusicLength = mBluetoothMusicClass.getSongLen();
        mMusicPlayTime = mBluetoothMusicClass.getSongPos();
    }

    private void updataPlayPauseButton() {
        switch (mCurState) {
            case BluetoothMusicClass.MUSIC_STATE_PLAY:
                mBtnPause.setVisibility(View.VISIBLE);
                mBtnPlay.setVisibility(View.GONE);
                break;

            case BluetoothMusicClass.MUSIC_STATE_PAUSE:
            case BluetoothMusicClass.MUSIC_STATE_STOP:
                mBtnPlay.setVisibility(View.VISIBLE);
                mBtnPause.setVisibility(View.GONE);
                break;
        }
    }

    private void updataPlayState() {
        Log.d(BLUETOOTH_MUSIC_ACTIVITY, "mCurState = " + mCurState + " mPlayState = " + mPlayState);
        if (mCurState != mPlayState) {
            mCurState = mPlayState;
            updataPlayPauseButton();
        }
        updataCurPlayTime();
        updataTotalPlayTime();
        mSeekbar.setMax(mMusicLength);
        mSeekbar.setProgress(mMusicPlayTime);

    }

    private void updataCurPlayTime() {
        String str;
        if (mMusicLength == 0xFFFFFFFF) {
            mTextTotalTime.setText(R.string.bluetoothmusic_time);
            return;
        }
        int time = mMusicPlayTime / 1000;
        int hour = time / 3600;
        int min =  (time - hour * 3600) / 60;
        int sec = time % 60;

        Log.d(BLUETOOTH_MUSIC_ACTIVITY, "hour = " + hour + " min = " + min + " sec = " + sec);
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

        mTextCurtime.setText(str);
    }

    private void updataTotalPlayTime() {
        String str;
        if (mMusicLength == 0xFFFFFFFF) {
            mTextTotalTime.setText(R.string.bluetoothmusic_time);
            return;
        }
        int time = mMusicLength / 1000;
        int hour = time / 3600;
        int min =  (time - hour * 3600) / 60;
        int sec = time % 60;

        Log.d(BLUETOOTH_MUSIC_ACTIVITY, "hour = " + hour + " min = " + min + " sec = " + sec);
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

        mTextTotalTime.setText(str);
    }

    class BluetoothMusicHandler extends Handler {


        public BluetoothMusicHandler(Looper loop)
        {
            super(loop);
        }
        @Override
        public void handleMessage(Message msg) {
            Log.d(BLUETOOTH_MUSIC_ACTIVITY, "BluetoothMusicHandler handleMessage what = " + msg.what);
            switch (msg.what) {
                case MSG_INIT_OK:
                    getMetaData();
                    getPlayState();
                    mUpdateUIhandler.sendEmptyMessage(MSG_UPDATA_ALL);
                    if (mPlayState != BluetoothMusicClass.MUSIC_STATE_PLAY) {
                        mBluetoothMusicClass.play();
                    }
                   break;

               case MSG_CONTROL_NEXT:
                   if(!isCmd) {
                       isCmd = true;
                       mBluetoothMusicClass.next();
                       isCmd = false;
                   }
                   break;

               case MSG_CONTROL_PRE:
                   if(!isCmd) {
                       isCmd = true;
                       mBluetoothMusicClass.prev();
                       isCmd = false;
                   }
                   break;

               case MSG_CONTROL_PLAY:
                   if(!isCmd) {
                       isCmd = true;
                       mBluetoothMusicClass.play();
                       isCmd = false;
                   }
                   break;

               case MSG_CONTROL_PAUSE:
                   if(!isCmd) {
                       isCmd = true;
                       mBluetoothMusicClass.pause();
                       isCmd = false;
                   }
                   break;
            }
        }
    }
}
