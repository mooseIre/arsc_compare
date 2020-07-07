package com.android.systemui.statusbar.policy;

import android.os.Build;
import android.os.SystemProperties;

public class EncryptionHelper {
    public static final boolean IS_DATA_ENCRYPTED = isDataEncrypted();

    private static boolean isDataEncrypted() {
        String str = SystemProperties.get("vold.decrypt");
        return "1".equals(str) || "trigger_restart_min_framework".equals(str);
    }

    public static boolean systemNotReady() {
        return Build.VERSION.SDK_INT >= 28 && isDataEncrypted();
    }
}
