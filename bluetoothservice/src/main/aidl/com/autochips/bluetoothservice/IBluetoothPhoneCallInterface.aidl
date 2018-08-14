// IBluetoothPhoneInterface.aidl
package com.autochips.bluetoothservice;

// Declare any non-default types here with import statements

interface IBluetoothPhoneCallInterface {
    // 拨号
    void hfpCall(String number);
    // 接听来电
    void acceptCall();
    // 挂断电话
    void rejectCall();
    void terminateCall();
    void sendDTMFCode(byte dtmfCode);
    void setLinstener(IBinder listener);
    void muteMic(boolean mute);
    boolean isMicMute();
    /**
    * 切换通话声音输出端
    * towardsAG: true  输出到手机端
    *            false 输出到车机端
    **/
    void switchAudioMode(boolean towardsAG);
    boolean isAudioTowardsAG();
    // 获取来电的电话号码
    String getIncomingNumber();
    String getRemoteAddr();
}
