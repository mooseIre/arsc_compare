package com.android.systemui.statusbar.notification.unimportant;

import android.service.notification.StatusBarNotification;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

public class UnimportantSdk extends UnimportantSdkWithPlugin {
    @Override // com.android.systemui.statusbar.notification.unimportant.UnimportantSdkWithPlugin, com.android.systemui.plugins.miui.notification.UnimportantSdkPlugin
    public int foldReason(StatusBarNotification statusBarNotification, String str, Map<String, Integer> map) {
        init();
        return super.foldReason(statusBarNotification, str, map);
    }

    @Override // com.android.systemui.statusbar.notification.unimportant.UnimportantSdkWithPlugin, com.android.systemui.plugins.miui.notification.UnimportantSdkPlugin
    public void updatePushFilter(List<JSONObject> list) {
        init();
        super.updatePushFilter(list);
    }
}
