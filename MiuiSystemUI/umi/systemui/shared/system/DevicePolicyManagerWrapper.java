package com.android.systemui.shared.system;

import android.app.AppGlobals;
import android.app.admin.DevicePolicyManager;

public class DevicePolicyManagerWrapper {
    private static final DevicePolicyManager sDevicePolicyManager = ((DevicePolicyManager) AppGlobals.getInitialApplication().getSystemService(DevicePolicyManager.class));
    private static final DevicePolicyManagerWrapper sInstance = new DevicePolicyManagerWrapper();

    private DevicePolicyManagerWrapper() {
    }

    public static DevicePolicyManagerWrapper getInstance() {
        return sInstance;
    }

    public boolean isLockTaskPermitted(String str) {
        return sDevicePolicyManager.isLockTaskPermitted(str);
    }
}
