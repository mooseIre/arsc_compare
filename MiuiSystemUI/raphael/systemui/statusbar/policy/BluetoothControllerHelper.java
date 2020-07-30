package com.android.systemui.statusbar.policy;

import com.android.settingslib.bluetooth.LocalBluetoothProfileManager;

public class BluetoothControllerHelper {
    public static void addServiceListener(LocalBluetoothProfileManager localBluetoothProfileManager, LocalBluetoothProfileManager.ServiceListener serviceListener) {
        localBluetoothProfileManager.addServiceListener(serviceListener);
    }
}
