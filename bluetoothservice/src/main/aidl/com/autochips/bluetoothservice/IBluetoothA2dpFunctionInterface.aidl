// BluetoothA2dpFunctionInterface.aidl
package com.autochips.bluetoothservice;

// Declare any non-default types here with import statements

interface IBluetoothA2dpFunctionInterface {
    void play();
    void pause();
    void prev();
    void next();
    void setPlayerState(boolean flag);

    void setLinstener(IBinder listener);

    String getTitle();
    String getArtist();
    String getAlbum();

    byte getPlayState();
    int getSongLen();
    int getSongPos();

    int a2dpsinkstate();
    int avrcpctstate();
    int playbackstate();
}
