package com.android.keyguard.analytics;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Slog;
import com.android.internal.os.SomeArgs;
import com.android.systemui.Constants;
import com.android.systemui.SystemUI;
import com.android.systemui.miui.statusbar.analytics.StatManager;
import com.android.systemui.statusbar.phone.StatusBar;
import java.util.HashMap;
import java.util.Map;

public class AnalyticsHelper {
    private static boolean DEBUG = Constants.DEBUG;
    private static volatile AnalyticsHelper sInstance;
    private Context mContext;
    private Handler mHandler;
    private boolean mIsLockScreenMagazineMainPreShowing;
    private HashMap<String, TrackPageEvent> mTrackPageEvents = new HashMap<>();
    private String mUnlockWay = "none";
    private String mWakeupWay = "others";

    public static int booleanToInt(boolean z) {
        return z ? 1 : 0;
    }

    private final class WorkHandler extends Handler {
        public WorkHandler(AnalyticsHelper analyticsHelper, Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            super.handleMessage(message);
            int i = message.what;
            if (i == 201) {
                SomeArgs someArgs = (SomeArgs) message.obj;
                StatManager.trackGenericEvent((String) someArgs.arg1, (HashMap) someArgs.arg2);
            } else if (i == 202) {
                StatManager.trackGenericEvent((String) ((SomeArgs) message.obj).arg1, (Map<String, Object>) null);
            }
        }
    }

    private AnalyticsHelper(Context context) {
        this.mContext = context;
        HandlerThread handlerThread = new HandlerThread("keyguard_analytics", 10);
        handlerThread.start();
        this.mHandler = new WorkHandler(this, handlerThread.getLooper());
        initTrackPageEvents();
    }

    private void initTrackPageEvents() {
        this.mTrackPageEvents.put("keyguard_view_main_lock_screen", new TrackPageEvent("keyguard_view_main_lock_screen", 500, 5000, 500, 10000));
        this.mTrackPageEvents.put("pw_unlock_time", new TrackPageEvent("pw_unlock_time", 300, 700, 50, 1000));
        this.mTrackPageEvents.put("pw_verify_time", new TrackPageEvent("pw_verify_time", 30, 150, 10, 200));
        this.mTrackPageEvents.put("action_enter_left_view", new TrackPageEvent("action_enter_left_view", 100, 300, 20, 400));
        this.mTrackPageEvents.put("action_enter_camera_view", new TrackPageEvent("action_enter_camera_view", 200, 400, 20, 600));
    }

    public static AnalyticsHelper getInstance(Context context) {
        if (sInstance == null) {
            synchronized (AnalyticsHelper.class) {
                if (sInstance == null) {
                    sInstance = new AnalyticsHelper(context);
                }
            }
        }
        return sInstance;
    }

    public void setWakeupWay(String str) {
        this.mWakeupWay = str;
    }

    public void resetAnalyticsParams() {
        this.mWakeupWay = "others";
    }

    public void recordScreenOn(boolean z, boolean z2, boolean z3, boolean z4, boolean z5, boolean z6, String str, boolean z7) {
        HashMap hashMap = new HashMap();
        hashMap.put("way_screen_on", this.mWakeupWay);
        booleanToInt(z);
        hashMap.put("is_fingerprint_locked", Integer.valueOf(z ? 1 : 0));
        booleanToInt(z2);
        hashMap.put("is_password_locked", Integer.valueOf(z2 ? 1 : 0));
        booleanToInt(z3);
        hashMap.put("is_screen_on_delayed", Integer.valueOf(z3 ? 1 : 0));
        booleanToInt(z4);
        hashMap.put("is_unlocked_by_fingerprint", Integer.valueOf(z4 ? 1 : 0));
        booleanToInt(z5);
        hashMap.put("is_keyguard_showing", Integer.valueOf(z5 ? 1 : 0));
        booleanToInt(z6);
        hashMap.put("is_occluded", Integer.valueOf(z6 ? 1 : 0));
        hashMap.put("charging", str);
        booleanToInt(z7);
        hashMap.put("is_lockscreen_wallpaper_open", Integer.valueOf(z7 ? 1 : 0));
        hashMap.put("is_global_lockscreen_wallpaper_pre_show", Boolean.valueOf(this.mIsLockScreenMagazineMainPreShowing));
        track("keyguard_screen_on", hashMap);
    }

    public void trackPageStart(String str) {
        this.mTrackPageEvents.get(str).onPageStart();
    }

    public void trackPageEnd(String str) {
        trackPageEnd(str, (String) null);
    }

    public void trackPageEnd(String str, String str2) {
        this.mTrackPageEvents.get(str).onPageEnd(str2);
    }

    public boolean isPWUnlock() {
        return "pw".equalsIgnoreCase(this.mUnlockWay);
    }

    public String getUnlockWay() {
        return this.mUnlockWay;
    }

    public void recordUnlockWay(String str, boolean z) {
        if (z) {
            Slog.w("miui_keyguard", "unlock keyguard by " + str);
            this.mUnlockWay = str;
        }
        HashMap hashMap = new HashMap();
        hashMap.put("unlock_way", str);
        booleanToInt(z);
        hashMap.put("unlock_result", Integer.valueOf(z ? 1 : 0));
        track("keyguard_unlock_way", hashMap);
    }

    public void recordKeyguardAction(String str) {
        HashMap hashMap = new HashMap();
        hashMap.put("lock_screen_action", str);
        track("keyguard_action", hashMap);
    }

    public void recordFaceUnlockEvent(boolean z, int i) {
        HashMap hashMap = new HashMap();
        booleanToInt(z);
        hashMap.put("unlock_result", Integer.valueOf(z ? 1 : 0));
        if (!z) {
            hashMap.put("face_unlock_fail_reason", Integer.valueOf(i));
        }
        track("face_unlock_event", hashMap);
    }

    public void recordFodQuickOpenExpandResultAction(boolean z) {
        HashMap hashMap = new HashMap();
        booleanToInt(z);
        hashMap.put("fod_quick_open_expand_result", Integer.valueOf(z ? 1 : 0));
        track("fod_quick_open_action", hashMap);
    }

    public void recordFodQuickOpenAppAction(String str) {
        HashMap hashMap = new HashMap();
        hashMap.put("fod_quick_open_app", str);
        track("fod_quick_open_action", hashMap);
    }

    public void recordKeyguardProximitySensor(boolean z) {
        HashMap hashMap = new HashMap();
        booleanToInt(z);
        hashMap.put("proximity_sensor_too_close", Integer.valueOf(z ? 1 : 0));
        track("keyguard_proximity_sensor_change", hashMap);
    }

    public void recordKeyguardSettingsEvent() {
        if (KeyguardSettingsAnalytics.getKeyguardSettingsStatParams(this.mContext) != null) {
            track("keyguard_settings_state", KeyguardSettingsAnalytics.getKeyguardSettingsStatParams(this.mContext));
        }
    }

    public void recordLockScreenWallperProviderChanged() {
        track("lock_screen_wallpaper_provider_changed", LockScreenMagazineAnalytics.getLockScreenWallperProviderStatus(this.mContext));
    }

    public void recordLockScreenWallperProviderStatus() {
        track("lock_screen_magazine_open_status", LockScreenMagazineAnalytics.getLockScreenWallperProviderStatus(this.mContext));
    }

    public void recordLockScreenMagazinePreviewAction(String str) {
        track("lock_screen_magazine_action", LockScreenMagazineAnalytics.getLockScreenMagazinePreviewActionParams(this.mContext, str));
    }

    public void recordLockScreenMagazineEntryClickAction() {
        HashMap lockScreenMagazinePreviewActionParams = LockScreenMagazineAnalytics.getLockScreenMagazinePreviewActionParams(this.mContext, "click_entry");
        lockScreenMagazinePreviewActionParams.put("has_notification", Boolean.valueOf(((StatusBar) SystemUI.getComponent(this.mContext, StatusBar.class)).getKeyguardNotifications() > 0));
        track("lock_screen_magazine_action", lockScreenMagazinePreviewActionParams);
    }

    public void recordNegativeStatus() {
        track("lock_screen_negative_status", LockScreenMagazineAnalytics.getNegativeStatusParams(this.mContext));
    }

    public void recordChargeAnimation(int i) {
        HashMap hashMap = new HashMap();
        hashMap.put("charge_animation_type", Integer.valueOf(i));
        track("charge_animation_start", hashMap);
    }

    public void recordWirelessChargeEfficiency(long j, int i, int i2) {
        if (j > 0 && i > 0 && i2 > 0) {
            HashMap hashMap = new HashMap();
            hashMap.put("charge_efficiency_time", Long.valueOf(j / 60000));
            hashMap.put("charge_efficiency_level", Integer.valueOf(i));
            hashMap.put("charge_efficiency_device", Integer.valueOf(i2));
            track("charge_efficiency", hashMap);
        }
    }

    public void recordFingerprintUnlockTimeEvent(long j) {
        if (j >= 0) {
            HashMap hashMap = new HashMap();
            hashMap.put("fingerprint_unlock_time", Long.valueOf(j));
            track("fingerprint_unlock_time_event", hashMap);
        }
    }

    public void track(String str, HashMap hashMap) {
        SomeArgs obtain = SomeArgs.obtain();
        obtain.arg1 = str;
        obtain.arg2 = hashMap;
        log("trackEvent eventName=%s params=%s", str, hashMap.toString());
        this.mHandler.obtainMessage(201, obtain).sendToTarget();
    }

    public void record(String str) {
        SomeArgs obtain = SomeArgs.obtain();
        obtain.arg1 = str;
        log("trackEvent eventName=%s", str);
        this.mHandler.obtainMessage(202, obtain).sendToTarget();
    }

    private void log(String str, Object... objArr) {
        if (DEBUG) {
            Log.d("MiuiKeyguardStat", String.format(str, objArr));
        }
    }

    public void setLockScreenMagazineMainPreShow(boolean z) {
        this.mIsLockScreenMagazineMainPreShowing = z;
    }

    private class TrackPageEvent {
        private long mEndLevel;
        private long mMaxValue;
        private String mPageName;
        private long mStartLevel;
        private long mStartTime;
        private long mStep;
        private boolean mTrackStarted;

        public TrackPageEvent(String str, long j, long j2, long j3, long j4) {
            this.mPageName = str;
            this.mStartLevel = j;
            this.mEndLevel = j2;
            this.mStep = j3;
            this.mMaxValue = j4;
        }

        public void onPageStart() {
            this.mTrackStarted = true;
            this.mStartTime = System.currentTimeMillis();
        }

        public void onPageEnd(String str) {
            if (this.mTrackStarted) {
                this.mTrackStarted = false;
                HashMap hashMap = new HashMap();
                hashMap.put("duration", Long.valueOf(formatTime(System.currentTimeMillis() - this.mStartTime)));
                if (str != null) {
                    hashMap.put("end_action", str);
                }
                AnalyticsHelper.this.track(this.mPageName, hashMap);
            }
        }

        private long formatTime(long j) {
            if (j < 0) {
                return 0;
            }
            if (j > this.mMaxValue) {
                return -1;
            }
            long j2 = this.mStartLevel;
            if (j <= j2) {
                return j2;
            }
            long j3 = this.mEndLevel;
            return j < j3 ? j2 + (((long) Math.round((((float) j) - ((float) j2)) / ((float) this.mStep))) * this.mStep) : j3;
        }
    }
}
