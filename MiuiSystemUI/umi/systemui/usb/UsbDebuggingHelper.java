package com.android.systemui.usb;

import android.debug.IAdbManager;
import android.os.RemoteException;
import android.os.ServiceManager;

public class UsbDebuggingHelper {
    public static IAdbManager getService() {
        return IAdbManager.Stub.asInterface(ServiceManager.getService("adb"));
    }

    public static void allowDebugging(boolean z, String str) throws RemoteException {
        getService().allowDebugging(z, str);
    }

    public static void denyDebugging() throws RemoteException {
        getService().denyDebugging();
    }
}
