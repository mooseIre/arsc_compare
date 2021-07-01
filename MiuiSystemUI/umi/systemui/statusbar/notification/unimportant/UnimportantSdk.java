package com.android.systemui.statusbar.notification.unimportant;

import android.content.Context;
import android.service.notification.StatusBarNotification;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

public class UnimportantSdk extends UnimportantSdkWithPlugin {
    public UnimportantSdk(Context context) {
        super(context);
    }

    @Override // com.android.systemui.statusbar.notification.unimportant.UnimportantSdkWithPlugin, com.android.systemui.plugins.miui.notification.UnimportantSdkPlugin
    public int foldReason(StatusBarNotification statusBarNotification, String str, Map<String, Integer> map) {
        if (statusBarNotification == null || str == null || map == null) {
            return 313;
        }
        init();
        return super.foldReason(statusBarNotification, str, map);
    }

    @Override // com.android.systemui.statusbar.notification.unimportant.UnimportantSdkWithPlugin, com.android.systemui.plugins.miui.notification.UnimportantSdkPlugin
    public void updatePushFilter(List<JSONObject> list) {
        if (list != null && list.size() != 0) {
            init();
            super.updatePushFilter(list);
        }
    }
}
