package com.android.systemui.statusbar.phone;

import android.os.Build;
import com.android.internal.os.BackgroundThread;
import java.util.ArrayList;
import java.util.List;
import miui.mqsas.sdk.MQSEventManagerDelegate;
import org.json.JSONException;
import org.json.JSONObject;

public class NavStubJankyFrameReporter {
    private static long sCurrentTime = -1;
    private static int sJankyFrameCount;
    private static final List<String> sJankyFramesInfoList = new ArrayList();

    static void resetAnimationFrameIntervalParams(String str) {
        if ("whyred".equals(Build.DEVICE)) {
            sCurrentTime = -1;
            sJankyFrameCount = 0;
        }
    }

    static void caculateAnimationFrameInterval(String str) {
        if ("whyred".equals(Build.DEVICE)) {
            long j = sCurrentTime;
            long currentTimeMillis = System.currentTimeMillis();
            sCurrentTime = currentTimeMillis;
            if (j > 0 && currentTimeMillis - j > 100) {
                sJankyFrameCount++;
            }
        }
    }

    static void recordJankyFrames(String str) {
        if ("whyred".equals(Build.DEVICE)) {
            try {
                sJankyFramesInfoList.add(frameInfoToJson(str).toString());
            } catch (JSONException e) {
                e.printStackTrace();
                sJankyFramesInfoList.add(String.format("{\"fullScreenVersion\":\"\",\"action\":\"\",\"jankyFramesCount\":\"\",\"extraKey1\":\"\",\"extraKey2\":\" %s\"}", new Object[]{e.toString()}));
            }
            if (sJankyFramesInfoList.size() == 10) {
                final ArrayList arrayList = new ArrayList(sJankyFramesInfoList);
                sJankyFramesInfoList.clear();
                BackgroundThread.getHandler().post(new Runnable() {
                    public void run() {
                        MQSEventManagerDelegate.getInstance().reportEvents("fsJankyFrames", arrayList, true);
                    }
                });
            }
        }
    }

    private static JSONObject frameInfoToJson(String str) throws JSONException {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("fullScreenVersion", "1");
        jSONObject.put("action", str);
        jSONObject.put("jankyFramesCount", String.valueOf(sJankyFrameCount));
        jSONObject.put("extraKey1", String.valueOf(System.currentTimeMillis()));
        jSONObject.put("extraKey2", "");
        return jSONObject;
    }
}
