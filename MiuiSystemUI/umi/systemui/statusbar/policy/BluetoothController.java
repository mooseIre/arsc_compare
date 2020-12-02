package com.android.systemui.statusbar.policy;

import android.content.Intent;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.systemui.Dumpable;
import java.util.Collection;

public interface BluetoothController extends CallbackController<Callback>, Dumpable {

    public interface Callback {
        void onBluetoothBatteryChange(Intent intent) {
        }

        void onBluetoothDevicesChanged();

        void onBluetoothInoutStateChange(String str) {
        }

        void onBluetoothStateChange(boolean z);
    }

    boolean canConfigBluetooth();

    void connect(CachedBluetoothDevice cachedBluetoothDevice);

    void disconnect(CachedBluetoothDevice cachedBluetoothDevice);

    int getBluetoothState();

    Collection<CachedBluetoothDevice> getDevices();

    String getLastDeviceName();

    int getMaxConnectionState(CachedBluetoothDevice cachedBluetoothDevice);

    boolean isBluetoothConnected();

    boolean isBluetoothConnecting();

    boolean isBluetoothEnabled();

    boolean isBluetoothReady();

    boolean isBluetoothSupported();

    void setBluetoothEnabled(boolean z);
}
