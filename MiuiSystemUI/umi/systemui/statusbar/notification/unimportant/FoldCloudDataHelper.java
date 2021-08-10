package com.android.systemui.statusbar.notification.unimportant;

import android.content.Context;
import android.util.Log;
import com.android.systemui.C0008R$array;
import com.android.systemui.Prefs;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import kotlin.collections.ArraysKt___ArraysJvmKt;
import kotlin.collections.IntIterator;
import kotlin.jvm.internal.Intrinsics;
import kotlin.random.Random;
import kotlin.ranges.IntRange;
import kotlin.ranges.RangesKt___RangesKt;
import kotlinx.coroutines.BuildersKt__Builders_commonKt;
import kotlinx.coroutines.GlobalScope;
import kotlinx.coroutines.Job;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class FoldCloudDataHelper {
    public static final FoldCloudDataHelper INSTANCE = new FoldCloudDataHelper();
    private static List<String> localWhitelist;
    private static Context mContext;
    private static List<String> xmsfNotificationChannel;

    private FoldCloudDataHelper() {
    }

    public final List<String> getXmsfNotificationChannel() {
        return xmsfNotificationChannel;
    }

    public final List<String> getLocalWhitelist() {
        return localWhitelist;
    }

    public final void init(Context context) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        mContext = context;
        String[] stringArray = context.getResources().getStringArray(C0008R$array.config_xmsfNotificationChannel);
        Intrinsics.checkExpressionValueIsNotNull(stringArray, "context.resources\n      …_xmsfNotificationChannel)");
        xmsfNotificationChannel = ArraysKt___ArraysJvmKt.asList(stringArray);
        String[] stringArray2 = context.getResources().getStringArray(C0008R$array.local_white_list);
        Intrinsics.checkExpressionValueIsNotNull(stringArray2, "context.resources\n      …R.array.local_white_list)");
        localWhitelist = ArraysKt___ArraysJvmKt.asList(stringArray2);
    }

    public final void updateAll(Context context) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Job unused = BuildersKt__Builders_commonKt.launch$default(GlobalScope.INSTANCE, null, null, new FoldCloudDataHelper$updateAll$1(context, null), 3, null);
    }

    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x0158  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final void updateAllCore(android.content.Context r14) {
        /*
        // Method dump skipped, instructions count: 408
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.unimportant.FoldCloudDataHelper.updateAllCore(android.content.Context):void");
    }

    private final int getBucket() {
        int i = Prefs.getNotif(mContext).getInt("user_bucket", RangesKt___RangesKt.random(new IntRange(0, 1000), Random.Default));
        Prefs.getNotif(mContext).edit().putInt("user_bucket", i).apply();
        return i;
    }

    private final void setNeedTrackingViaBucket(int i, String str) {
        try {
            JSONObject jSONObject = new JSONObject(str);
            Iterator<String> keys = jSONObject.keys();
            Intrinsics.checkExpressionValueIsNotNull(keys, "cloudBucket.keys()");
            while (true) {
                boolean z = false;
                while (keys.hasNext()) {
                    String next = keys.next();
                    if (!z) {
                        Intrinsics.checkExpressionValueIsNotNull(next, "it");
                        int parseInt = Integer.parseInt(next);
                        int i2 = jSONObject.getInt(next);
                        if (parseInt <= i) {
                            if (i2 >= i) {
                            }
                        }
                    }
                    z = true;
                }
                Log.i("CloudDataHelper", "user bucket: " + i + ", cloudData: " + jSONObject);
                return;
            }
        } catch (JSONException unused) {
            Log.e("CloudDataHelper", "sampling cloud data error");
        }
    }

    private final List<String> jsonArrayStr2StrList(String str) {
        JSONArray jSONArray;
        try {
            jSONArray = new JSONArray(str);
        } catch (JSONException unused) {
            jSONArray = new JSONArray();
        }
        ArrayList arrayList = new ArrayList(jSONArray.length());
        Iterator it = RangesKt___RangesKt.until(0, jSONArray.length()).iterator();
        while (it.hasNext()) {
            arrayList.add(jSONArray.optString(((IntIterator) it).nextInt()));
        }
        return arrayList;
    }
}
