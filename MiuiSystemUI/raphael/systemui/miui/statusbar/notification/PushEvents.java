package com.android.systemui.miui.statusbar.notification;

import android.app.Notification;
import android.os.Bundle;

public class PushEvents {
    public static String getADId(Notification notification) {
        Bundle bundle;
        if (notification == null || (bundle = notification.extras) == null) {
            return null;
        }
        long j = bundle.getLong("adid");
        if (j == 0) {
            return null;
        }
        return j + "";
    }
}
