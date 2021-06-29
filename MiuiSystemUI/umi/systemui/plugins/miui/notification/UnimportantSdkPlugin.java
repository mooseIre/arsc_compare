package com.android.systemui.plugins.miui.notification;

import android.service.notification.StatusBarNotification;
import com.android.systemui.plugins.Plugin;
import com.android.systemui.plugins.annotations.ProvidesInterface;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

@ProvidesInterface(action = UnimportantSdkPlugin.ACTION, version = 1)
public interface UnimportantSdkPlugin extends Plugin {
    public static final String ACTION = "miui.notification.action.PLUGIN_SDK_UNIMPORTANT";
    public static final int VERSION = 1;

    default int foldReason(StatusBarNotification statusBarNotification, String str, Map<String, Integer> map) {
        return 0;
    }

    default void init() {
    }

    default void updatePushFilter(List<JSONObject> list) {
    }
}
