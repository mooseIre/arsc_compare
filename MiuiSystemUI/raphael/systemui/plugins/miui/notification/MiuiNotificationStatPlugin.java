package com.android.systemui.plugins.miui.notification;

import android.content.Context;
import com.android.systemui.plugins.Plugin;
import com.android.systemui.plugins.annotations.ProvidesInterface;
import java.util.HashMap;

@ProvidesInterface(action = MiuiNotificationStatPlugin.ACTION, version = 1)
public interface MiuiNotificationStatPlugin extends Plugin {
    public static final String ACTION = "miui.notification.action.PLUGIN_NOTIFICATION_STAT";
    public static final int VERSION = 1;

    default void onPluginEvent(Context context, String str, HashMap<String, Object> hashMap) {
    }
}
