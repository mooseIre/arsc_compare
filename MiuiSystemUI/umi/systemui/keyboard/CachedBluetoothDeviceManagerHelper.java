package com.android.systemui.keyboard;

import android.bluetooth.BluetoothDevice;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.CachedBluetoothDeviceManager;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
import com.android.settingslib.bluetooth.LocalBluetoothProfileManager;

public class CachedBluetoothDeviceManagerHelper {
    public static CachedBluetoothDevice addDevice(CachedBluetoothDeviceManager cachedBluetoothDeviceManager, LocalBluetoothAdapter localBluetoothAdapter, LocalBluetoothProfileManager localBluetoothProfileManager, BluetoothDevice bluetoothDevice) {
        return cachedBluetoothDeviceManager.addDevice(bluetoothDevice);
    }
}
