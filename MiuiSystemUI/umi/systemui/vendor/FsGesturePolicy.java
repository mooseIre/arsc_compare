package com.android.systemui.vendor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.UserHandle;
import android.util.Log;
import com.android.systemui.Dependency;
import com.android.systemui.miui.statusbar.analytics.SystemUIStat;
import java.util.ArrayList;
import java.util.HashMap;

public class FsGesturePolicy {
    private static BroadcastReceiver mAnalyticsReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("FsGesturePolicy", "mAnalyticsReceiver onReceive(): " + action);
            if ("com.android.systemui.action_track_fullscreen_event".equals(action)) {
                String stringExtra = intent.getStringExtra("event_name");
                ArrayList<String> stringArrayListExtra = intent.getStringArrayListExtra("event_param");
                ArrayList<String> stringArrayListExtra2 = intent.getStringArrayListExtra("event_value");
                HashMap hashMap = null;
                if (stringArrayListExtra != null && stringArrayListExtra2 != null && stringArrayListExtra.size() == stringArrayListExtra2.size() && stringArrayListExtra.size() > 0) {
                    hashMap = new HashMap(stringArrayListExtra.size());
                    for (int i = 0; i < stringArrayListExtra.size(); i++) {
                        hashMap.put(stringArrayListExtra.get(i), stringArrayListExtra2.get(i));
                    }
                }
                ((SystemUIStat) Dependency.get(SystemUIStat.class)).reportFullScreenEventAnonymous(stringExtra, hashMap);
            }
        }
    };
    private Context mContext;

    public FsGesturePolicy(Context context) {
        this.mContext = context;
    }

    public void start() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.systemui.action_track_fullscreen_event");
        this.mContext.registerReceiverAsUser(mAnalyticsReceiver, UserHandle.ALL, intentFilter, (String) null, (Handler) null);
    }
}
