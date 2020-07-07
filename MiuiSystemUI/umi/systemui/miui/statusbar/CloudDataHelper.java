package com.android.systemui.miui.statusbar;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.android.systemui.Dependency;
import com.android.systemui.miui.statusbar.notification.NotificationSettingsManager;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import miui.util.NotificationFilterHelper;
import org.json.JSONArray;
import org.json.JSONException;

public class CloudDataHelper {
    public static final Uri URI_CLOUD_ALL_DATA_NOTIFY = Uri.parse("content://com.android.settings.cloud.CloudSettings/cloud_all_data/notify");

    public static void updateAll(Context context) {
        Log.d("CloudDataHelper", "CloudData updateAll");
        ContentResolver contentResolver = context.getContentResolver();
        String cloudDataString = getCloudDataString(contentResolver, "systemui_float_whitelist", "whitelist");
        if (!TextUtils.isEmpty(cloudDataString)) {
            int hashCode = cloudDataString.hashCode();
            int readHashCode = readHashCode(context, "systemui_float_whitelist");
            Log.d("CloudDataHelper", String.format("updateFloatWhitelist thisHashCode=%d lastHashCode=%d", new Object[]{Integer.valueOf(hashCode), Integer.valueOf(readHashCode)}));
            if (hashCode != readHashCode) {
                updateFloatWhitelist(context, cloudDataString);
                writeHashCode(context, "systemui_float_whitelist", hashCode);
            }
        }
        String cloudDataString2 = getCloudDataString(contentResolver, "systemui_keyguard_whitelist", "whitelist");
        if (!TextUtils.isEmpty(cloudDataString2)) {
            int hashCode2 = cloudDataString2.hashCode();
            int readHashCode2 = readHashCode(context, "systemui_keyguard_whitelist");
            Log.d("CloudDataHelper", String.format("updateKeyguardWhitelist thisHashCode=%d lastHashCode=%d", new Object[]{Integer.valueOf(hashCode2), Integer.valueOf(readHashCode2)}));
            if (hashCode2 != readHashCode2) {
                updateKeyguardWhitelist(context, cloudDataString2);
                writeHashCode(context, "systemui_keyguard_whitelist", hashCode2);
            }
        }
        ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).onCloudDataUpdated();
    }

    public static List<String> getFloatWhitelist(Context context) {
        return jsonArray2List(createJSONArray(getCloudDataString(context.getContentResolver(), "systemui_float_whitelist", "whitelist")));
    }

    public static List<String> getKeyguardWhitelist(Context context) {
        return jsonArray2List(createJSONArray(getCloudDataString(context.getContentResolver(), "systemui_keyguard_whitelist", "whitelist")));
    }

    public static List<String> getFloatBlacklist(Context context) {
        return jsonArray2List(createJSONArray(getCloudDataString(context.getContentResolver(), "systemui_float_blacklist", "whitelist")));
    }

    public static List<String> getKeyguardBlacklist(Context context) {
        return jsonArray2List(createJSONArray(getCloudDataString(context.getContentResolver(), "systemui_keyguard_blacklist", "whitelist")));
    }

    public static List<String> getBadgeWhitelist(Context context) {
        return jsonArray2List(createJSONArray(getCloudDataString(context.getContentResolver(), "systemui_badge_whitelist", "whitelist")));
    }

    public static List<String> getSlideWhiteList(Context context) {
        return jsonArray2List(createJSONArray(getCloudDataString(context.getContentResolver(), "small_window", "small_window_notification_whitelist")));
    }

    private static String getCloudDataString(ContentResolver contentResolver, String str, String str2) {
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            return null;
        }
        return MiuiSettings.SettingsCloudData.getCloudDataString(contentResolver, str, str2, "");
    }

    private static void updateFloatWhitelist(Context context, String str) {
        List<String> jsonArray2List = jsonArray2List(createJSONArray(str));
        if (jsonArray2List != null && !jsonArray2List.isEmpty()) {
            NotificationFilterHelper.updateFloatWhiteList(context, jsonArray2List);
        }
    }

    private static void updateKeyguardWhitelist(Context context, String str) {
        List<String> jsonArray2List = jsonArray2List(createJSONArray(str));
        if (jsonArray2List != null && !jsonArray2List.isEmpty()) {
            NotificationFilterHelper.updateKeyguardWhitelist(context, jsonArray2List);
        }
    }

    private static JSONArray createJSONArray(String str) {
        if (!TextUtils.isEmpty(str)) {
            try {
                return new JSONArray(str);
            } catch (JSONException unused) {
                Log.d("CloudDataHelper", "createJSONArray exception json=" + str);
            }
        }
        return null;
    }

    private static List<String> jsonArray2List(JSONArray jSONArray) {
        if (jSONArray == null) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < jSONArray.length(); i++) {
            try {
                arrayList.add(jSONArray.getString(i));
            } catch (JSONException unused) {
                Log.d("CloudDataHelper", "jsonArray2List exception i=" + i);
            }
        }
        return arrayList;
    }

    private static void writeHashCode(Context context, String str, int i) {
        Settings.Global.putInt(context.getContentResolver(), str, i);
    }

    private static int readHashCode(Context context, String str) {
        return Settings.Global.getInt(context.getContentResolver(), str, -1);
    }

    public static void dump(Context context, PrintWriter printWriter) {
        printWriter.println("CloudData:");
        printWriter.println("  float_whitelist" + getFloatWhitelist(context));
        printWriter.println("  keyguard_whitelist" + getKeyguardWhitelist(context));
        printWriter.println("  badge_whitelist" + getBadgeWhitelist(context));
    }
}
