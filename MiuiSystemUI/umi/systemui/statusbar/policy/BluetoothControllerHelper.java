package com.android.systemui.statusbar.policy;

import android.os.Handler;
import com.android.settingslib.bluetooth.BluetoothEventManager;
import com.android.settingslib.bluetooth.LocalBluetoothProfileManager;

public class BluetoothControllerHelper {
    public static void setReceiverHandler(BluetoothEventManager bluetoothEventManager, Handler handler) {
    }

    public static void addServiceListener(LocalBluetoothProfileManager localBluetoothProfileManager, LocalBluetoothProfileManager.ServiceListener serviceListener) {
        localBluetoothProfileManager.addServiceListener(serviceListener);
    }
}
