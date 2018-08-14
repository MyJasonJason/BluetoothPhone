// IBluetoothPhoneBookInterface.aidl
package com.autochips.bluetoothservice;

// Declare any non-default types here with import statements

interface IBluetoothPhoneBookInterface {
    void downloadContacts();
    void stopDownloadContacts();
    void setLinstener(IBinder listener);
    void hfpCall(String number);
    int getContactsDownNumber();
    int getCallhistroyNumber();
    int getContactsDownState();
    int getContCallhistroyState();
    String getRemoteAddr();
    void setAutoAcept(boolean flag);
    boolean getAutoAcept();
}
