// IBluetoothSetListenerInterface.aidl
package com.autochips.bluetoothservice;

import android.content.Intent;
interface IBluetoothSetListenerInterface {
    void onMessage(int what,int arg1,int arg2);
    void onIntent(in Intent intent);
}
