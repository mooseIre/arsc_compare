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
import com.android.systemui.miui.analytics.AnalyticsWrapper;
import com.android.systemui.statusbar.phone.StatusBar;
import com.xiaomi.stat.MiStatParams;
import java.util.HashMap;

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
                AnalyticsWrapper.trackEvent((String) someArgs.arg1, (MiStatParams) someArgs.arg2);
            } else if (i == 202) {
                AnalyticsWrapper.trackEvent((String) ((SomeArgs) message.obj).arg1);
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
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putString("way_screen_on", this.mWakeupWay);
        booleanToInt(z);
        miStatParams.putInt("is_fingerprint_locked", z ? 1 : 0);
        booleanToInt(z2);
        miStatParams.putInt("is_password_locked", z2 ? 1 : 0);
        booleanToInt(z3);
        miStatParams.putInt("is_screen_on_delayed", z3 ? 1 : 0);
        booleanToInt(z4);
        miStatParams.putInt("is_unlocked_by_fingerprint", z4 ? 1 : 0);
        booleanToInt(z5);
        miStatParams.putInt("is_keyguard_showing", z5 ? 1 : 0);
        booleanToInt(z6);
        miStatParams.putInt("is_occluded", z6 ? 1 : 0);
        miStatParams.putString("charging", str);
        booleanToInt(z7);
        miStatParams.putInt("is_lockscreen_wallpaper_open", z7 ? 1 : 0);
        miStatParams.putBoolean("is_global_lockscreen_wallpaper_pre_show", this.mIsLockScreenMagazineMainPreShowing);
        track("keyguard_screen_on", miStatParams);
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
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putString("unlock_way", str);
        booleanToInt(z);
        miStatParams.putInt("unlock_result", z ? 1 : 0);
        track("keyguard_unlock_way", miStatParams);
    }

    public void recordKeyguardAction(String str) {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putString("lock_screen_action", str);
        track("keyguard_action", miStatParams);
    }

    public void recordFaceUnlockEvent(boolean z, int i) {
        MiStatParams miStatParams = new MiStatParams();
        booleanToInt(z);
        miStatParams.putInt("unlock_result", z ? 1 : 0);
        if (!z) {
            miStatParams.putInt("face_unlock_fail_reason", i);
        }
        track("face_unlock_event", miStatParams);
    }

    public void recordFodQuickOpenExpandResultAction(boolean z) {
        MiStatParams miStatParams = new MiStatParams();
        booleanToInt(z);
        miStatParams.putInt("fod_quick_open_expand_result", z ? 1 : 0);
        track("fod_quick_open_action", miStatParams);
    }

    public void recordFodQuickOpenAppAction(String str) {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putString("fod_quick_open_app", str);
        track("fod_quick_open_action", miStatParams);
    }

    public void recordKeyguardProximitySensor(boolean z) {
        MiStatParams miStatParams = new MiStatParams();
        booleanToInt(z);
        miStatParams.putInt("proximity_sensor_too_close", z ? 1 : 0);
        track("keyguard_proximity_sensor_change", miStatParams);
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
        MiStatParams lockScreenMagazinePreviewActionParams = LockScreenMagazineAnalytics.getLockScreenMagazinePreviewActionParams(this.mContext, "click_entry");
        lockScreenMagazinePreviewActionParams.putBoolean("has_notification", ((StatusBar) SystemUI.getComponent(this.mContext, StatusBar.class)).getKeyguardNotifications() > 0);
        track("lock_screen_magazine_action", lockScreenMagazinePreviewActionParams);
    }

    public void recordNegativeStatus() {
        track("lock_screen_negative_status", LockScreenMagazineAnalytics.getNegativeStatusParams(this.mContext));
    }

    public void recordChargeAnimation(int i) {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putInt("charge_animation_type", i);
        track("charge_animation_start", miStatParams);
    }

    public void recordWirelessChargeEfficiency(long j, int i, int i2) {
        if (j > 0 && i > 0 && i2 > 0) {
            MiStatParams miStatParams = new MiStatParams();
            miStatParams.putLong("charge_efficiency_time", j / 60000);
            miStatParams.putInt("charge_efficiency_level", i);
            miStatParams.putInt("charge_efficiency_device", i2);
            track("charge_efficiency", miStatParams);
        }
    }

    public void recordFingerprintUnlockTimeEvent(long j) {
        if (j >= 0) {
            MiStatParams miStatParams = new MiStatParams();
            miStatParams.putLong("fingerprint_unlock_time", j);
            track("fingerprint_unlock_time_event", miStatParams);
        }
    }

    public void track(String str, MiStatParams miStatParams) {
        SomeArgs obtain = SomeArgs.obtain();
        obtain.arg1 = str;
        obtain.arg2 = miStatParams;
        log("trackEvent eventName=%s params=%s", str, miStatParams.toJsonString());
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
                MiStatParams miStatParams = new MiStatParams();
                miStatParams.putLong("duration", formatTime(System.currentTimeMillis() - this.mStartTime));
                if (str != null) {
                    miStatParams.putString("end_action", str);
                }
                AnalyticsHelper.this.track(this.mPageName, miStatParams);
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
