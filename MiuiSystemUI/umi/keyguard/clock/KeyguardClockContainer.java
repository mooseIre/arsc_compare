package com.android.keyguard.clock;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;
import com.android.keyguard.injector.KeyguardClockInjector;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.policy.DualClockObserver;
import java.util.TimeZone;

public class KeyguardClockContainer extends FrameLayout {
    ContentObserver mClockPositionObserver;
    private MiuiKeyguardBaseClock mClockView;
    private String mCurrentTimezone;
    private DualClockObserver mDualClockObserver;
    private boolean mDualClockOpen;
    ContentObserver mDualClockOpenObserver;
    private final Handler mHandler;
    private final BroadcastReceiver mIntentReceiver;
    private int mLastSelectedClockPosition;
    private String mResidentTimezone;
    ContentObserver mResidentTimezoneObserver;
    private int mSelectedClockPosition;
    private boolean mShowDualClock;
    private boolean mShowVerticalClock;
    private KeyguardUpdateMonitor mUpdateMonitor;
    private final MiuiKeyguardUpdateMonitorCallback mUpdateMonitorCallback;
    private Runnable mUpdateTimeRunnable;

    public KeyguardClockContainer(Context context) {
        this(context, null, 0, 0);
    }

    public KeyguardClockContainer(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0, 0);
    }

    public KeyguardClockContainer(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public KeyguardClockContainer(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mCurrentTimezone = TimeZone.getDefault().getID();
        boolean z = false;
        this.mDualClockOpen = false;
        this.mShowDualClock = false;
        this.mSelectedClockPosition = 0;
        this.mLastSelectedClockPosition = 0;
        this.mShowVerticalClock = false;
        this.mHandler = new Handler();
        this.mUpdateTimeRunnable = new Runnable() {
            /* class com.android.keyguard.clock.KeyguardClockContainer.AnonymousClass1 */

            public void run() {
                if (KeyguardClockContainer.this.mClockView != null) {
                    KeyguardClockContainer.this.mClockView.updateTime();
                }
            }
        };
        this.mIntentReceiver = new BroadcastReceiver() {
            /* class com.android.keyguard.clock.KeyguardClockContainer.AnonymousClass2 */

            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.intent.action.TIMEZONE_CHANGED")) {
                    KeyguardClockContainer.this.mHandler.post(new Runnable() {
                        /* class com.android.keyguard.clock.KeyguardClockContainer.AnonymousClass2.AnonymousClass1 */

                        public void run() {
                            KeyguardClockContainer.this.mCurrentTimezone = TimeZone.getDefault().getID();
                            KeyguardClockContainer.this.updateKeyguardClock();
                        }
                    });
                } else {
                    KeyguardClockContainer.this.mHandler.post(KeyguardClockContainer.this.mUpdateTimeRunnable);
                }
            }
        };
        this.mDualClockOpenObserver = new ContentObserver(new Handler()) {
            /* class com.android.keyguard.clock.KeyguardClockContainer.AnonymousClass4 */

            public void onChange(boolean z) {
                super.onChange(z);
                KeyguardClockContainer keyguardClockContainer = KeyguardClockContainer.this;
                ContentResolver contentResolver = ((FrameLayout) keyguardClockContainer).mContext.getContentResolver();
                KeyguardUpdateMonitor unused = KeyguardClockContainer.this.mUpdateMonitor;
                boolean z2 = false;
                if (Settings.System.getIntForUser(contentResolver, "auto_dual_clock", 0, KeyguardUpdateMonitor.getCurrentUser()) != 0) {
                    z2 = true;
                }
                keyguardClockContainer.mDualClockOpen = z2;
                KeyguardClockContainer.this.updateKeyguardClock();
            }
        };
        this.mResidentTimezoneObserver = new ContentObserver(new Handler()) {
            /* class com.android.keyguard.clock.KeyguardClockContainer.AnonymousClass5 */

            public void onChange(boolean z) {
                super.onChange(z);
                KeyguardClockContainer keyguardClockContainer = KeyguardClockContainer.this;
                ContentResolver contentResolver = ((FrameLayout) keyguardClockContainer).mContext.getContentResolver();
                KeyguardUpdateMonitor unused = KeyguardClockContainer.this.mUpdateMonitor;
                keyguardClockContainer.mResidentTimezone = Settings.System.getStringForUser(contentResolver, "resident_timezone", KeyguardUpdateMonitor.getCurrentUser());
                KeyguardClockContainer.this.updateKeyguardClock();
            }
        };
        this.mClockPositionObserver = new ContentObserver(new Handler()) {
            /* class com.android.keyguard.clock.KeyguardClockContainer.AnonymousClass6 */

            public void onChange(boolean z) {
                super.onChange(z);
                KeyguardClockContainer keyguardClockContainer = KeyguardClockContainer.this;
                ContentResolver contentResolver = ((FrameLayout) keyguardClockContainer).mContext.getContentResolver();
                int defaultKeyguardClockPosition = MiuiKeyguardUtils.getDefaultKeyguardClockPosition(((FrameLayout) KeyguardClockContainer.this).mContext);
                KeyguardUpdateMonitor unused = KeyguardClockContainer.this.mUpdateMonitor;
                keyguardClockContainer.mSelectedClockPosition = Settings.System.getIntForUser(contentResolver, "selected_keyguard_clock_position", defaultKeyguardClockPosition, KeyguardUpdateMonitor.getCurrentUser());
                KeyguardClockContainer keyguardClockContainer2 = KeyguardClockContainer.this;
                keyguardClockContainer2.mShowVerticalClock = MiuiKeyguardUtils.isSupportVerticalClock(keyguardClockContainer2.mSelectedClockPosition, ((FrameLayout) KeyguardClockContainer.this).mContext);
                KeyguardClockContainer.this.updateKeyguardClock();
            }
        };
        this.mUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        this.mDualClockObserver = (DualClockObserver) Dependency.get(DualClockObserver.class);
        this.mSelectedClockPosition = Settings.System.getIntForUser(((FrameLayout) this).mContext.getContentResolver(), "selected_keyguard_clock_position", MiuiKeyguardUtils.getDefaultKeyguardClockPosition(((FrameLayout) this).mContext), KeyguardUpdateMonitor.getCurrentUser());
        this.mDualClockOpen = Settings.System.getIntForUser(((FrameLayout) this).mContext.getContentResolver(), "auto_dual_clock", 0, KeyguardUpdateMonitor.getCurrentUser()) != 0;
        String stringForUser = Settings.System.getStringForUser(((FrameLayout) this).mContext.getContentResolver(), "resident_timezone", KeyguardUpdateMonitor.getCurrentUser());
        this.mResidentTimezone = stringForUser;
        if (this.mDualClockOpen && stringForUser != null && !stringForUser.equals(this.mCurrentTimezone)) {
            z = true;
        }
        this.mShowDualClock = z;
        this.mDualClockObserver.setShowDualClock(z);
        this.mShowVerticalClock = MiuiKeyguardUtils.isSupportVerticalClock(this.mSelectedClockPosition, ((FrameLayout) this).mContext);
        this.mUpdateMonitorCallback = new MiuiKeyguardUpdateMonitorCallback() {
            /* class com.android.keyguard.clock.KeyguardClockContainer.AnonymousClass3 */

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onUserSwitchComplete(int i) {
                KeyguardClockContainer.this.onUserChanged();
            }
        };
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        addClockView();
        updateKeyguardClock();
        ((KeyguardClockInjector) Dependency.get(KeyguardClockInjector.class)).onFinishInflate(this);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.TIME_TICK");
        intentFilter.addAction("android.intent.action.TIME_SET");
        intentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
        ((FrameLayout) this).mContext.registerReceiverAsUser(this.mIntentReceiver, UserHandle.ALL, intentFilter, null, (Handler) Dependency.get(Dependency.TIME_TICK_HANDLER));
        registerDualClockObserver();
        registerClockPositionObserver();
        this.mUpdateMonitor.registerCallback(this.mUpdateMonitorCallback);
        ((KeyguardClockInjector) Dependency.get(KeyguardClockInjector.class)).onAttachedToWindow();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((FrameLayout) this).mContext.unregisterReceiver(this.mIntentReceiver);
        unregisterDualClockObserver();
        unregisterClockPositionObserver();
        this.mUpdateMonitor.removeCallback(this.mUpdateMonitorCallback);
        ((KeyguardClockInjector) Dependency.get(KeyguardClockInjector.class)).onDetachedFromWindow();
    }

    private void registerDualClockObserver() {
        ((FrameLayout) this).mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("auto_dual_clock"), false, this.mDualClockOpenObserver, -1);
        this.mDualClockOpenObserver.onChange(false);
        ((FrameLayout) this).mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("resident_timezone"), false, this.mResidentTimezoneObserver, -1);
        this.mResidentTimezoneObserver.onChange(false);
    }

    private void unregisterDualClockObserver() {
        ((FrameLayout) this).mContext.getContentResolver().unregisterContentObserver(this.mDualClockOpenObserver);
        ((FrameLayout) this).mContext.getContentResolver().unregisterContentObserver(this.mResidentTimezoneObserver);
    }

    private void registerClockPositionObserver() {
        ((FrameLayout) this).mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("selected_keyguard_clock_position"), false, this.mClockPositionObserver, -1);
        this.mClockPositionObserver.onChange(false);
    }

    private void unregisterClockPositionObserver() {
        ((FrameLayout) this).mContext.getContentResolver().unregisterContentObserver(this.mClockPositionObserver);
    }

    private void addClockView() {
        MiuiKeyguardBaseClock miuiKeyguardBaseClock;
        if (this.mShowDualClock) {
            miuiKeyguardBaseClock = new MiuiKeyguardDualClock(((FrameLayout) this).mContext);
        } else if (this.mShowVerticalClock) {
            miuiKeyguardBaseClock = new MiuiKeyguardCenterVerticalClock(((FrameLayout) this).mContext);
        } else {
            int i = this.mSelectedClockPosition;
            if (i != 1) {
                miuiKeyguardBaseClock = i != 2 ? new MiuiKeyguardLeftTopLargeClock(((FrameLayout) this).mContext) : new MiuiKeyguardLeftTopClock(((FrameLayout) this).mContext);
            } else {
                miuiKeyguardBaseClock = new MiuiKeyguardCenterHorizontalClock(((FrameLayout) this).mContext);
            }
        }
        addView(miuiKeyguardBaseClock);
        this.mClockView = miuiKeyguardBaseClock;
    }

    public void updateKeyguardClock() {
        String str;
        boolean z = this.mDualClockOpen && (str = this.mResidentTimezone) != null && !str.equals(this.mCurrentTimezone);
        this.mDualClockObserver.setShowDualClock(z);
        if (!(this.mShowDualClock == z && this.mSelectedClockPosition == this.mLastSelectedClockPosition)) {
            this.mShowDualClock = z;
            this.mLastSelectedClockPosition = this.mSelectedClockPosition;
            removeAllViews();
            addClockView();
        }
        MiuiKeyguardBaseClock miuiKeyguardBaseClock = this.mClockView;
        if (miuiKeyguardBaseClock != null) {
            miuiKeyguardBaseClock.updateResidentTimeZone(this.mResidentTimezone);
            this.mClockView.updateTimeZone(this.mCurrentTimezone);
            this.mClockView.setSelectedClockPosition(this.mSelectedClockPosition);
        }
    }

    public void setDarkStyle(boolean z) {
        this.mClockView.setDarkStyle(z);
    }

    public void updateClockView(boolean z) {
        this.mClockView.updateClockView(z);
    }

    public void updateTime() {
        this.mClockView.updateTime();
    }

    public int getClockHeight() {
        return this.mClockView.getClockHeight();
    }

    public float getClockVisibleHeight() {
        return this.mClockView.getClockVisibleHeight();
    }

    public void onUserChanged() {
        this.mDualClockOpenObserver.onChange(false);
        this.mResidentTimezoneObserver.onChange(false);
        this.mClockPositionObserver.onChange(false);
    }

    public void updateClockMagazineInfo() {
        this.mClockView.updateClockMagazineInfo();
    }

    public void updateClock(float f, int i) {
        setAlpha(f);
        setImportantForAccessibility(i);
    }
}
