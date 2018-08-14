// IBluetoothSetInterface.aidl
package com.autochips.bluetoothservice;

interface IBluetoothSetFunctionInterface {
    void openBluetooth();
    void closeBluetooth();
    boolean isBluetoothOn();
    void getBondDevices();
    void setLinstener(IBinder listener);
    void startSearch();
    void stopSearch();
    void connectDevice(String macAddr);
    void disconnectDevice(String macAddr);
    void removeDevice(String macAddr);
    void setPairRequest(boolean flag);
	void setAutoConnect(boolean flag);
    boolean getAutoConnect();
}
