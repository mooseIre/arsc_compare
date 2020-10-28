package com.android.systemui.miui.volume;

import android.os.SystemClock;
import android.util.Log;
import com.android.systemui.Constants;
import com.android.systemui.miui.analytics.OneTrackWrapper$Generic;
import java.util.HashMap;

public class VolumeEventTracker {
    private static final String TAG = "VolumeEventTracker";
    private static long sCallbackTime;

    private static String convertRingerModeToStr(int i) {
        return i != 0 ? i != 1 ? i != 4 ? "unknown" : "exit_silent" : "exit_dnd" : "enter_silent";
    }

    public static void recordAudioCallbackTime() {
        sCallbackTime = SystemClock.uptimeMillis();
    }

    public static void trackVolumeShowTimeCost() {
        HashMap hashMap = new HashMap();
        hashMap.put("time_cost", Long.valueOf(SystemClock.uptimeMillis() - sCallbackTime));
        OneTrackWrapper$Generic.track("event_volume_dialog_launch", hashMap);
        log("trackVolumeShowTimeCost event:%s, params:[%s, %s]", "event_volume_dialog_launch", "time_cost", String.valueOf(SystemClock.uptimeMillis() - sCallbackTime));
    }

    public static void trackVolumeShow(String str) {
        HashMap hashMap = new HashMap();
        hashMap.put("type", str);
        OneTrackWrapper$Generic.track("event_volume_dialog_show", hashMap);
        log("trackVolumeShow event:%s, params:[%s, %s]", "event_volume_dialog_show", "type", str);
    }

    public static void trackVolumeDismiss(String str) {
        HashMap hashMap = new HashMap();
        hashMap.put("reason", str);
        OneTrackWrapper$Generic.track("event_volume_dialog_dismiss", hashMap);
        log("trackVolumeDismiss event:%s, params:[%s, %s]", "event_volume_dialog_dismiss", "reason", str);
    }

    public static void trackClickRingerBtn(int i) {
        HashMap hashMap = new HashMap();
        hashMap.put("ringer_mode_after_click", convertRingerModeToStr(i));
        OneTrackWrapper$Generic.track("event_volume_collapse_click_btn", hashMap);
        log("trackClickRingerBtn event:%s, params:[%s, %s]", "event_volume_collapse_click_btn", "ringer_mode_after_click", convertRingerModeToStr(i));
    }

    public static void trackAdjustVolumeStream(String str) {
        HashMap hashMap = new HashMap();
        hashMap.put("stream_name", str);
        OneTrackWrapper$Generic.track("event_volume_adjust_stream", hashMap);
        log("trackAdjustVolumeStream event:%s, params:[%s, %s]", "event_volume_adjust_stream", "stream_name", str);
    }

    public static void trackTimerRingerMode(boolean z) {
        HashMap hashMap = new HashMap();
        String str = "silent_timer";
        hashMap.put("type", z ? str : "dnd_timer");
        OneTrackWrapper$Generic.track("event_volume_timer_ringer_mode", hashMap);
        Object[] objArr = new Object[3];
        objArr[0] = "event_volume_timer_ringer_mode";
        objArr[1] = "type";
        if (!z) {
            str = "dnd_timer";
        }
        objArr[2] = str;
        log("trackTimerRingerMode event:%s, params:[%s, %s]", objArr);
    }

    public static void trackTimerDuration(String str) {
        HashMap hashMap = new HashMap();
        hashMap.put("duration", str);
        OneTrackWrapper$Generic.track("event_volume_timer", hashMap);
        log("trackTimerDuration event:%s, params:[%s, %s]", "event_volume_timer", "duration", str);
    }

    public static void trackClickExpandRingerBtn(String str) {
        HashMap hashMap = new HashMap();
        hashMap.put("click_btn_type", str);
        OneTrackWrapper$Generic.track("event_volume_expand_click_btn", hashMap);
        log("trackClickExpandRingerBtn event:%s, params:[%s, %s]", "event_volume_expand_click_btn", "click_btn_type", str);
    }

    private static void log(String str, Object... objArr) {
        if (Constants.DEBUG) {
            Log.d(TAG, String.format(str, objArr));
        }
    }
}
