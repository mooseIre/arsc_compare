package com.android.systemui;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.android.systemui.statusbar.NotificationData;
import com.xiaomi.analytics.Actions;
import com.xiaomi.analytics.AdAction;
import com.xiaomi.analytics.Analytics;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AdTracker {
    public static void trackShow(Context context, NotificationData.Entry entry) {
        JSONObject jSONTag = getJSONTag(entry);
        trackEvent(context, "VIEW", getExtra(jSONTag), entry, gerMonitorUrl(jSONTag, "viewMonitorUrls"));
    }

    public static void trackRemove(Context context, NotificationData.Entry entry) {
        trackEvent(context, "NOTIFICATION_REMOVE", getExtra(getJSONTag(entry)), entry, (List<String>) null);
    }

    private static void trackEvent(Context context, String str, String str2, NotificationData.Entry entry, List<String> list) {
        if (!TextUtils.isEmpty(str2)) {
            AdAction newAdAction = Actions.newAdAction(str);
            newAdAction.addParam("ex", str2);
            newAdAction.addParam("v", "sdk_1.0");
            newAdAction.addParam("e", str);
            newAdAction.addParam("t", System.currentTimeMillis());
            if (list != null) {
                newAdAction.addAdMonitor(list);
            }
            try {
                String str3 = Constants.DEBUG ? "systemui_pushstaging" : "systemui_push";
                Log.d("adTracker", "config = " + str3);
                Analytics.trackSystem(context, str3, newAdAction);
            } catch (Exception e) {
                Log.e("adTracker", e.getLocalizedMessage());
            }
        }
    }

    public static String getExtra(JSONObject jSONObject) {
        if (jSONObject != null) {
            return jSONObject.optString("ex");
        }
        return null;
    }

    public static List<String> gerMonitorUrl(JSONObject jSONObject, String str) {
        if (jSONObject != null) {
            return JSONArrayToList(jSONObject.optJSONArray(str));
        }
        return null;
    }

    public static JSONObject getJSONTag(NotificationData.Entry entry) {
        String tag = entry.notification.getTag();
        if (TextUtils.isEmpty(tag)) {
            return null;
        }
        try {
            return new JSONObject(tag);
        } catch (JSONException e) {
            Log.e("adTracker", e.getLocalizedMessage());
            return null;
        }
    }

    public static List<String> JSONArrayToList(JSONArray jSONArray) {
        if (jSONArray == null) {
            return null;
        }
        ArrayList arrayList = new ArrayList(jSONArray.length());
        for (int i = 0; i < jSONArray.length(); i++) {
            try {
                arrayList.add(jSONArray.getString(i));
            } catch (JSONException e) {
                Log.e("adTracker", e.getLocalizedMessage());
            }
        }
        return arrayList;
    }
}
