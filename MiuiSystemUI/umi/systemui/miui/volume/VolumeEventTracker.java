package com.android.systemui.miui.volume;

import android.os.SystemClock;
import android.util.Log;
import com.android.systemui.Constants;
import com.android.systemui.miui.analytics.AnalyticsWrapper;
import com.xiaomi.stat.MiStatParams;

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
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putLong("time_cost", SystemClock.uptimeMillis() - sCallbackTime);
        AnalyticsWrapper.trackEvent("event_volume_dialog_launch", miStatParams);
        log("trackVolumeShowTimeCost event:%s, params:[%s, %s]", "event_volume_dialog_launch", "time_cost", String.valueOf(SystemClock.uptimeMillis() - sCallbackTime));
    }

    public static void trackVolumeShow(String str) {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putString("type", str);
        AnalyticsWrapper.trackEvent("event_volume_dialog_show", miStatParams);
        log("trackVolumeShow event:%s, params:[%s, %s]", "event_volume_dialog_show", "type", str);
    }

    public static void trackVolumeDismiss(String str) {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putString("reason", str);
        AnalyticsWrapper.trackEvent("event_volume_dialog_dismiss", miStatParams);
        log("trackVolumeDismiss event:%s, params:[%s, %s]", "event_volume_dialog_dismiss", "reason", str);
    }

    public static void trackClickRingerBtn(int i) {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putString("ringer_mode_after_click", convertRingerModeToStr(i));
        AnalyticsWrapper.trackEvent("event_volume_collapse_click_btn", miStatParams);
        log("trackClickRingerBtn event:%s, params:[%s, %s]", "event_volume_collapse_click_btn", "ringer_mode_after_click", convertRingerModeToStr(i));
    }

    public static void trackAdjustVolumeStream(String str) {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putString("stream_name", str);
        AnalyticsWrapper.trackEvent("event_volume_adjust_stream", miStatParams);
        log("trackAdjustVolumeStream event:%s, params:[%s, %s]", "event_volume_adjust_stream", "stream_name", str);
    }

    public static void trackTimerRingerMode(boolean z) {
        MiStatParams miStatParams = new MiStatParams();
        String str = "silent_timer";
        miStatParams.putString("type", z ? str : "dnd_timer");
        AnalyticsWrapper.trackEvent("event_volume_timer_ringer_mode", miStatParams);
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
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putString("duration", str);
        AnalyticsWrapper.trackEvent("event_volume_timer", miStatParams);
        log("trackTimerDuration event:%s, params:[%s, %s]", "event_volume_timer", "duration", str);
    }

    public static void trackClickExpandRingerBtn(String str) {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putString("click_btn_type", str);
        AnalyticsWrapper.trackEvent("event_volume_expand_click_btn", miStatParams);
        log("trackClickExpandRingerBtn event:%s, params:[%s, %s]", "event_volume_expand_click_btn", "click_btn_type", str);
    }

    private static void log(String str, Object... objArr) {
        if (Constants.DEBUG) {
            Log.d(TAG, String.format(str, objArr));
        }
    }
}
