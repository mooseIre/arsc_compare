package com.android.systemui.screenshot;

import android.content.Context;
import android.content.Intent;
import com.xiaomi.stat.MiStat;
import java.util.HashMap;
import java.util.Map;

public class StatHelper {
    public static void recordCountEvent(Context context, String str) {
        recordCountEvent(context, str, (Map<String, String>) null);
    }

    public static void recordCountEvent(Context context, String str, String str2) {
        HashMap hashMap = new HashMap();
        hashMap.put("category", str2);
        recordCountEvent(context, str, (Map<String, String>) hashMap);
    }

    public static void recordCountEvent(Context context, String str, Map<String, String> map) {
        Intent intent = new Intent("com.miui.gallery.intent.action.SEND_STAT");
        intent.setPackage("com.miui.gallery");
        intent.putExtra("stat_type", "count_event");
        intent.putExtra("category", "new_screenshot");
        intent.putExtra("event", str);
        if (map != null && map.size() > 0) {
            intent.putExtra("param_keys", (String[]) map.keySet().toArray(new String[0]));
            intent.putExtra("param_values", (String[]) map.values().toArray(new String[0]));
        }
        context.sendBroadcast(intent);
    }

    public static void recordNewScreenshotEvent(Context context, String str, Map<String, String> map) {
        Intent intent = new Intent("com.miui.gallery.intent.action.SEND_STAT");
        intent.setPackage("com.miui.gallery");
        intent.putExtra("stat_type", "count_event");
        intent.putExtra("category", "new_screenshot");
        intent.putExtra("event", str);
        if (map != null && map.size() > 0) {
            intent.putExtra("param_keys", (String[]) map.keySet().toArray(new String[0]));
            intent.putExtra("param_values", (String[]) map.values().toArray(new String[0]));
        }
        context.sendBroadcast(intent);
    }

    public static void recordNumericPropertyEvent(Context context, String str, long j) {
        Intent intent = new Intent("com.miui.gallery.intent.action.SEND_STAT");
        intent.setPackage("com.miui.gallery");
        intent.putExtra("stat_type", "numeric_event");
        intent.putExtra("category", "new_screenshot");
        intent.putExtra("event", str);
        intent.putExtra(MiStat.Param.VALUE, j);
        context.sendBroadcast(intent);
    }
}
