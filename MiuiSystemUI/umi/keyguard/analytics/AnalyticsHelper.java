package com.android.keyguard.analytics;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.android.internal.os.SomeArgs;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.phone.StatusBar;
import com.miui.systemui.DebugConfig;
import java.util.HashMap;

public class AnalyticsHelper {
    private static boolean DEBUG = DebugConfig.DEBUG_KEYGUARD;
    private static volatile AnalyticsHelper sInstance;
    private Handler mHandler;
    private HashMap<String, TrackPageEvent> mTrackPageEvents = new HashMap<>();
    private String mUnlockWay = "none";

    public static int booleanToInt(boolean z) {
        return z ? 1 : 0;
    }

    public void setLockScreenMagazineMainPreShow(boolean z) {
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
            } else if (i == 202) {
                SomeArgs someArgs2 = (SomeArgs) message.obj;
            }
        }
    }

    private AnalyticsHelper(Context context) {
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

    public void trackPageStart(String str) {
        this.mTrackPageEvents.get(str).onPageStart();
    }

    public void trackPageEnd(String str, String str2) {
        this.mTrackPageEvents.get(str).onPageEnd(str2);
    }

    public String getUnlockWay() {
        return this.mUnlockWay;
    }

    public void recordKeyguardAction(String str) {
        HashMap hashMap = new HashMap();
        hashMap.put("lock_screen_action", str);
        track("keyguard_action", hashMap);
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

    public void recordLockScreenWallperProviderChanged() {
        track("lock_screen_wallpaper_provider_changed", LockScreenMagazineAnalytics.getLockScreenWallperProviderStatus());
    }

    public void recordLockScreenMagazinePreviewAction(String str) {
        track("lock_screen_magazine_action", LockScreenMagazineAnalytics.getLockScreenMagazinePreviewActionParams(str));
    }

    public void recordLockScreenMagazineEntryClickAction() {
        HashMap lockScreenMagazinePreviewActionParams = LockScreenMagazineAnalytics.getLockScreenMagazinePreviewActionParams("click_entry");
        lockScreenMagazinePreviewActionParams.put("has_notification", Boolean.valueOf(((StatusBar) Dependency.get(StatusBar.class)).getKeyguardNotifications() > 0));
        track("lock_screen_magazine_action", lockScreenMagazinePreviewActionParams);
    }

    public void recordNegativeStatus() {
        track("lock_screen_negative_status", LockScreenMagazineAnalytics.getNegativeStatusParams());
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

    /* access modifiers changed from: private */
    public class TrackPageEvent {
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
