package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.content.Intent;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.systemui.Dumpable;
import java.util.Collection;

public interface BluetoothController extends CallbackController<Callback>, Dumpable {

    public interface Callback {
        default void onBluetoothBatteryChange(Intent intent) {
        }

        void onBluetoothDevicesChanged();

        default void onBluetoothInoutStateChange(String str) {
        }

        void onBluetoothStateChange(boolean z);
    }

    boolean canConfigBluetooth();

    void connect(CachedBluetoothDevice cachedBluetoothDevice);

    void disconnect(CachedBluetoothDevice cachedBluetoothDevice);

    int getBluetoothState();

    int getBondState(CachedBluetoothDevice cachedBluetoothDevice);

    Collection<CachedBluetoothDevice> getDevices();

    String getLastDeviceName();

    int getMaxConnectionState(CachedBluetoothDevice cachedBluetoothDevice);

    boolean isBleAudioDevice(Context context, CachedBluetoothDevice cachedBluetoothDevice);

    boolean isBluetoothConnected();

    boolean isBluetoothConnecting();

    boolean isBluetoothEnabled();

    boolean isBluetoothReady();

    boolean isBluetoothSupported();

    void setBluetoothEnabled(boolean z);
}
