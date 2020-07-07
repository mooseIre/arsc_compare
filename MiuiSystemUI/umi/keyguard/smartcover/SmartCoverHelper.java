package com.android.keyguard.smartcover;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Slog;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.android.systemui.Util;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.plugins.R;
import com.xiaomi.stat.MiStat;

public class SmartCoverHelper {
    private ActivityManager mActivityManager;
    private final BroadcastReceiver mBatteryBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.BATTERY_CHANGED".equals(intent.getAction())) {
                boolean z = true;
                int intExtra = intent.getIntExtra(MiStat.Param.STATUS, 1);
                boolean unused = SmartCoverHelper.this.mCharging = (intExtra == 5 || intExtra == 2) && (intent.getIntExtra("plugged", 0) != 0);
                int unused2 = SmartCoverHelper.this.mLevel = intent.getIntExtra(MiStat.Param.LEVEL, 0);
                SmartCoverHelper smartCoverHelper = SmartCoverHelper.this;
                if (intExtra != 5 && smartCoverHelper.mLevel < 100) {
                    z = false;
                }
                boolean unused3 = smartCoverHelper.mFull = z;
                if (SmartCoverHelper.this.mSmartCoverView != null) {
                    SmartCoverHelper.this.mSmartCoverView.onBatteryInfoRefresh(SmartCoverHelper.this.mCharging, SmartCoverHelper.this.mFull, SmartCoverHelper.this.mLevel);
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean mCharging;
    private Context mContext;
    /* access modifiers changed from: private */
    public boolean mFull;
    /* access modifiers changed from: private */
    public boolean mIsSmartCoverClosed;
    /* access modifiers changed from: private */
    public boolean mIsSmartCoverFullMode;
    /* access modifiers changed from: private */
    public boolean mIsSmartCoverLatticeMode;
    /* access modifiers changed from: private */
    public int mLevel;
    /* access modifiers changed from: private */
    public PowerManager mPowerManager;
    Runnable mScreenOffRunnable = new Runnable() {
        public void run() {
            if (SmartCoverHelper.this.mSmartCoverView != null && SmartCoverHelper.this.mPowerManager.isScreenOn()) {
                Slog.i("SmartCoverHelper", "keyguard_screen_off_reason:smart cover");
                SmartCoverHelper.this.mPowerManager.goToSleep(SystemClock.uptimeMillis());
            }
        }
    };
    private final BroadcastReceiver mSmartCoverReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("miui.intent.action.SMART_COVER".equals(intent.getAction())) {
                int i = SystemProperties.getInt("persist.sys.smartcover_mode", -1);
                boolean z = !intent.getBooleanExtra("is_smart_cover_open", false);
                boolean unused = SmartCoverHelper.this.mIsSmartCoverLatticeMode = i == 3;
                boolean unused2 = SmartCoverHelper.this.mIsSmartCoverFullMode = i == 4;
                if (SmartCoverHelper.this.needRefreshSmartCoverWindow(i)) {
                    boolean unused3 = SmartCoverHelper.this.mIsSmartCoverClosed = z;
                    SmartCoverHelper.this.refreshSmartCover();
                }
                if (!SmartCoverHelper.this.hideLockForMode(i) || SmartCoverHelper.this.mViewMediator.isSecure()) {
                    SmartCoverHelper.this.mViewMediator.setHideLockForLid(false);
                } else if (z) {
                    SmartCoverHelper.this.mViewMediator.setHideLockForLid(true);
                } else {
                    if (SmartCoverHelper.this.mViewMediator.isShowingAndNotOccluded() && !SmartCoverHelper.this.mViewMediator.isSimLockedOrMissing()) {
                        SmartCoverHelper.this.mViewMediator.keyguardDone();
                    }
                    SmartCoverHelper.this.mViewMediator.setHideLockForLid(false);
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public SmartCoverView mSmartCoverView;
    /* access modifiers changed from: private */
    public KeyguardViewMediator mViewMediator;

    /* access modifiers changed from: private */
    public boolean hideLockForMode(int i) {
        return 1 == i || 3 == i || 4 == i || 2 == i;
    }

    /* access modifiers changed from: private */
    public boolean needRefreshSmartCoverWindow(int i) {
        return 3 == i || 4 == i;
    }

    public SmartCoverHelper(Context context, KeyguardViewMediator keyguardViewMediator) {
        this.mContext = context;
        this.mViewMediator = keyguardViewMediator;
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("miui.intent.action.SMART_COVER");
        this.mContext.registerReceiverAsUser(this.mSmartCoverReceiver, UserHandle.ALL, intentFilter, "android.permission.DEVICE_POWER", (Handler) null);
        this.mContext.registerReceiver(this.mBatteryBroadcastReceiver, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
    }

    public void onScreenTurnedOn() {
        SmartCoverView smartCoverView = this.mSmartCoverView;
        if (smartCoverView != null) {
            smartCoverView.removeCallbacks(this.mScreenOffRunnable);
            this.mSmartCoverView.postDelayed(this.mScreenOffRunnable, 5000);
        }
        refreshSmartCover();
    }

    public void refreshSmartCover() {
        if (!this.mIsSmartCoverLatticeMode && !this.mIsSmartCoverFullMode) {
            return;
        }
        if (this.mSmartCoverView != null) {
            if (!this.mIsSmartCoverClosed || isInCallOrClockShowing()) {
                showSmartCover(false);
            }
        } else if (this.mIsSmartCoverClosed && !isInCallOrClockShowing()) {
            showSmartCover(true);
        }
    }

    public void showSmartCover(boolean z) {
        WindowManager windowManager = (WindowManager) this.mContext.getSystemService("window");
        if (this.mSmartCoverView == null) {
            this.mSmartCoverView = (SmartCoverView) View.inflate(this.mContext, this.mIsSmartCoverFullMode ? R.layout.full_smart_cover_layout : R.layout.smart_cover_layout, (ViewGroup) null);
            this.mSmartCoverView.setSystemUiVisibility(4864);
        }
        if (z) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, -1, 2009, 84083968, 1);
            layoutParams.setTitle("smart_cover");
            windowManager.addView(this.mSmartCoverView, layoutParams);
            this.mSmartCoverView.onBatteryInfoRefresh(this.mCharging, this.mFull, this.mLevel);
            this.mSmartCoverView.removeCallbacks(this.mScreenOffRunnable);
            this.mSmartCoverView.postDelayed(this.mScreenOffRunnable, 5000);
            return;
        }
        this.mSmartCoverView.removeCallbacks(this.mScreenOffRunnable);
        windowManager.removeView(this.mSmartCoverView);
        this.mSmartCoverView = null;
    }

    private boolean isInCallOrClockShowing() {
        ComponentName topActivity = Util.getTopActivity(this.mContext);
        return topActivity != null && ("com.android.incallui.InCallActivity".equals(topActivity.getClassName()) || "com.android.deskclock.activity.AlarmAlertFullScreenActivity".equals(topActivity.getClassName()));
    }
}
