package com.android.systemui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils;

public class OTAGuideReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (TextUtils.equals(intent.getAction(), "com.miui.miservice.action.SWITCH_UPDATE")) {
            String stringExtra = intent.getStringExtra("switchState");
            String stringExtra2 = intent.getStringExtra("switchFlag");
            Logger.d("MiSrv:OTAGuideReceiver", "flag: " + stringExtra2 + " state: " + stringExtra);
            Settings.System.putIntForUser(context.getContentResolver(), "use_control_panel", TextUtils.equals(stringExtra, "1") ? 1 : 0, 0);
        }
    }
}
