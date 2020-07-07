package com.android.systemui.fsgesture;

import android.content.Context;
import android.content.Intent;

public class GestureDemoBroadcastUtils {
    public static void sendBroadcast(Context context, boolean z) {
        sendBroadcast(context, z, "typefrom_demo");
    }

    public static void sendBroadcast(Context context, boolean z, String str) {
        Intent intent = new Intent();
        intent.setAction("com.android.systemui.fsgesture");
        intent.putExtra("typeFrom", str);
        intent.putExtra("isEnter", z);
        context.sendBroadcast(intent, "miui.permission.USE_INTERNAL_GENERAL_API");
    }
}
