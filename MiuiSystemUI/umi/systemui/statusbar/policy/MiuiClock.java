package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.SystemClock;
import android.os.UserHandle;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0021R$string;
import com.android.systemui.DemoMode;
import com.android.systemui.Dependency;
import com.android.systemui.FontSizeUtils;
import com.android.systemui.R$styleable;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.MiuiClock;
import com.android.systemui.tuner.TunerService;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.TimeZone;
import miui.date.Calendar;
import miui.date.DateUtils;

public class MiuiClock extends TextView implements DemoMode, TunerService.Tunable, CommandQueue.Callbacks, DarkIconDispatcher.DarkReceiver, ConfigurationController.ConfigurationListener {
    private final int mAmPmStyle;
    private boolean mAttached;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private Calendar mCalendar;
    private int mClockMode;
    private boolean mClockVisibleByController;
    private boolean mClockVisibleByPolicy;
    private boolean mClockVisibleByUser;
    private final CommandQueue mCommandQueue;
    private int mCurrentUserId;
    private final CurrentUserTracker mCurrentUserTracker;
    private boolean mDemoMode;
    private final BroadcastReceiver mIntentReceiver;
    private Locale mLocale;
    private int mNonAdaptedColor;
    private final BroadcastReceiver mScreenReceiver;
    private final Runnable mSecondTick;
    private Handler mSecondsHandler;
    private final boolean mShowDark;
    private boolean mShowSeconds;
    private boolean mStatusBarClock;
    private boolean mUseWallpaperTextColor;
    private LinkedList<ClockVisibilityListener> mVisibilityListeners;

    public interface ClockVisibilityListener {
        void onClockVisibilityChanged(boolean z);
    }

    public MiuiClock(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    /* JADX INFO: finally extract failed */
    public MiuiClock(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mVisibilityListeners = new LinkedList<>();
        this.mClockVisibleByPolicy = true;
        this.mClockVisibleByUser = true;
        this.mClockVisibleByController = true;
        this.mIntentReceiver = new BroadcastReceiver() {
            /* class com.android.systemui.statusbar.policy.MiuiClock.AnonymousClass2 */

            public void onReceive(Context context, Intent intent) {
                Handler handler = MiuiClock.this.getHandler();
                if (handler != null) {
                    String action = intent.getAction();
                    if (action.equals("android.intent.action.TIMEZONE_CHANGED")) {
                        handler.post(new Runnable(intent.getStringExtra("time-zone")) {
                            /* class com.android.systemui.statusbar.policy.$$Lambda$MiuiClock$2$X4rQia0YnKWxYiwVzzeV6zSOIxA */
                            public final /* synthetic */ String f$1;

                            {
                                this.f$1 = r2;
                            }

                            public final void run() {
                                MiuiClock.AnonymousClass2.this.lambda$onReceive$0$MiuiClock$2(this.f$1);
                            }
                        });
                    } else if (action.equals("android.intent.action.CONFIGURATION_CHANGED")) {
                        handler.post(new Runnable(MiuiClock.this.getResources().getConfiguration().locale) {
                            /* class com.android.systemui.statusbar.policy.$$Lambda$MiuiClock$2$Qx5tJlO2m9HFOiePlayxBAhL13s */
                            public final /* synthetic */ Locale f$1;

                            {
                                this.f$1 = r2;
                            }

                            public final void run() {
                                MiuiClock.AnonymousClass2.this.lambda$onReceive$1$MiuiClock$2(this.f$1);
                            }
                        });
                    }
                    handler.post(new Runnable() {
                        /* class com.android.systemui.statusbar.policy.$$Lambda$MiuiClock$2$3REMYcIorKCY4tLnmwfA081UFdw */

                        public final void run() {
                            MiuiClock.AnonymousClass2.this.lambda$onReceive$2$MiuiClock$2();
                        }
                    });
                }
            }

            /* access modifiers changed from: private */
            /* renamed from: lambda$onReceive$0 */
            public /* synthetic */ void lambda$onReceive$0$MiuiClock$2(String str) {
                MiuiClock.this.mCalendar = new Calendar(TimeZone.getTimeZone(str));
                MiuiClock.this.mCalendar.setTimeZone(MiuiClock.this.mCalendar.getTimeZone());
            }

            /* access modifiers changed from: private */
            /* renamed from: lambda$onReceive$1 */
            public /* synthetic */ void lambda$onReceive$1$MiuiClock$2(Locale locale) {
                if (!locale.equals(MiuiClock.this.mLocale)) {
                    MiuiClock.this.mLocale = locale;
                }
            }

            /* access modifiers changed from: private */
            /* renamed from: lambda$onReceive$2 */
            public /* synthetic */ void lambda$onReceive$2$MiuiClock$2() {
                MiuiClock.this.updateClock();
            }
        };
        this.mScreenReceiver = new BroadcastReceiver() {
            /* class com.android.systemui.statusbar.policy.MiuiClock.AnonymousClass3 */

            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.intent.action.SCREEN_OFF".equals(action)) {
                    if (MiuiClock.this.mSecondsHandler != null) {
                        MiuiClock.this.mSecondsHandler.removeCallbacks(MiuiClock.this.mSecondTick);
                    }
                } else if ("android.intent.action.SCREEN_ON".equals(action) && MiuiClock.this.mSecondsHandler != null) {
                    MiuiClock.this.mSecondsHandler.postAtTime(MiuiClock.this.mSecondTick, ((SystemClock.uptimeMillis() / 1000) * 1000) + 1000);
                }
            }
        };
        this.mSecondTick = new Runnable() {
            /* class com.android.systemui.statusbar.policy.MiuiClock.AnonymousClass4 */

            public void run() {
                if (MiuiClock.this.mCalendar != null) {
                    MiuiClock.this.updateClock();
                }
                MiuiClock.this.mSecondsHandler.postAtTime(this, ((SystemClock.uptimeMillis() / 1000) * 1000) + 1000);
            }
        };
        this.mCommandQueue = (CommandQueue) Dependency.get(CommandQueue.class);
        TypedArray obtainStyledAttributes = context.getTheme().obtainStyledAttributes(attributeSet, R$styleable.MiuiClock, 0, 0);
        try {
            this.mAmPmStyle = obtainStyledAttributes.getInt(R$styleable.MiuiClock_MiuiAmPmStyle, 2);
            this.mShowDark = obtainStyledAttributes.getBoolean(R$styleable.MiuiClock_MiuiClockShowDark, true);
            this.mClockMode = obtainStyledAttributes.getInt(R$styleable.MiuiClock_MiuiClockMode, 0);
            this.mStatusBarClock = obtainStyledAttributes.getBoolean(R$styleable.MiuiClock_MiuiStatusBarClock, false);
            this.mNonAdaptedColor = getCurrentTextColor();
            obtainStyledAttributes.recycle();
            BroadcastDispatcher broadcastDispatcher = (BroadcastDispatcher) Dependency.get(BroadcastDispatcher.class);
            this.mBroadcastDispatcher = broadcastDispatcher;
            this.mCurrentUserTracker = new CurrentUserTracker(broadcastDispatcher) {
                /* class com.android.systemui.statusbar.policy.MiuiClock.AnonymousClass1 */

                @Override // com.android.systemui.settings.CurrentUserTracker
                public void onUserSwitched(int i) {
                    MiuiClock.this.mCurrentUserId = i;
                }
            };
        } catch (Throwable th) {
            obtainStyledAttributes.recycle();
            throw th;
        }
    }

    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("clock_super_parcelable", super.onSaveInstanceState());
        bundle.putInt("current_user_id", this.mCurrentUserId);
        bundle.putBoolean("visible_by_policy", this.mClockVisibleByPolicy);
        bundle.putBoolean("visible_by_user", this.mClockVisibleByUser);
        bundle.putBoolean("show_seconds", this.mShowSeconds);
        bundle.putInt("visibility", getVisibility());
        return bundle;
    }

    public void onRestoreInstanceState(Parcelable parcelable) {
        if (parcelable == null || !(parcelable instanceof Bundle)) {
            super.onRestoreInstanceState(parcelable);
            return;
        }
        Bundle bundle = (Bundle) parcelable;
        super.onRestoreInstanceState(bundle.getParcelable("clock_super_parcelable"));
        if (bundle.containsKey("current_user_id")) {
            this.mCurrentUserId = bundle.getInt("current_user_id");
        }
        this.mClockVisibleByPolicy = bundle.getBoolean("visible_by_policy", true);
        this.mClockVisibleByUser = bundle.getBoolean("visible_by_user", true);
        this.mShowSeconds = bundle.getBoolean("show_seconds", false);
        if (bundle.containsKey("visibility")) {
            super.setVisibility(bundle.getInt("visibility"));
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!this.mAttached) {
            this.mAttached = true;
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.TIME_TICK");
            intentFilter.addAction("android.intent.action.TIME_SET");
            intentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
            intentFilter.addAction("android.intent.action.CONFIGURATION_CHANGED");
            intentFilter.addAction("android.intent.action.USER_SWITCHED");
            this.mBroadcastDispatcher.registerReceiverWithHandler(this.mIntentReceiver, intentFilter, (Handler) Dependency.get(Dependency.TIME_TICK_HANDLER), UserHandle.ALL);
            ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "clock_seconds", "icon_blacklist");
            if (this.mStatusBarClock) {
                this.mCommandQueue.addCallback((CommandQueue.Callbacks) this);
            }
            if (this.mShowDark) {
                ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).addDarkReceiver(this);
            }
            this.mCurrentUserTracker.startTracking();
            this.mCurrentUserId = this.mCurrentUserTracker.getCurrentUserId();
            ((DemoModeController) Dependency.get(DemoModeController.class)).addCallback(this);
        }
        this.mCalendar = new Calendar(TimeZone.getDefault());
        updateClock();
        updateClockVisibility();
        updateShowSeconds();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mAttached) {
            ((DemoModeController) Dependency.get(DemoModeController.class)).removeCallback(this);
            this.mBroadcastDispatcher.unregisterReceiver(this.mIntentReceiver);
            this.mAttached = false;
            ((TunerService) Dependency.get(TunerService.class)).removeTunable(this);
            if (this.mStatusBarClock) {
                this.mCommandQueue.removeCallback((CommandQueue.Callbacks) this);
            }
            if (this.mShowDark) {
                ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).removeDarkReceiver(this);
            }
            this.mCurrentUserTracker.stopTracking();
        }
    }

    public void setVisibility(int i) {
        if (i != 0 || shouldBeVisible()) {
            super.setVisibility(i);
        }
    }

    public void setClockMode(int i) {
        this.mClockMode = i;
        if (this.mCalendar != null) {
            updateClock();
        }
    }

    public void setClockVisibleByUser(boolean z) {
        this.mClockVisibleByUser = z;
        updateClockVisibility();
    }

    public void setClockVisibilityByPolicy(boolean z) {
        this.mClockVisibleByPolicy = z;
        updateClockVisibility();
    }

    public void setClockVisibilityByController(boolean z) {
        this.mClockVisibleByController = z;
        updateClockVisibility();
    }

    private boolean shouldBeVisible() {
        return this.mClockVisibleByPolicy && this.mClockVisibleByUser && this.mClockVisibleByController;
    }

    private void updateClockVisibility() {
        super.setVisibility(shouldBeVisible() ? 0 : 8);
    }

    /* access modifiers changed from: package-private */
    public final void updateClock() {
        Calendar calendar = this.mCalendar;
        if (calendar != null) {
            if (this.mDemoMode) {
                calendar.set(18, 8);
                this.mCalendar.set(20, 16);
            } else {
                calendar.setTimeInMillis(System.currentTimeMillis());
            }
            updateTime();
        }
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        if ("clock_seconds".equals(str)) {
            this.mShowSeconds = TunerService.parseIntegerSwitch(str2, false);
            updateShowSeconds();
            return;
        }
        setClockVisibleByUser(!StatusBarIconController.getIconBlacklist(getContext(), str2).contains("clock"));
        updateClockVisibility();
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void disable(int i, int i2, int i3, boolean z) {
        if (i == getDisplay().getDisplayId()) {
            boolean z2 = (8388608 & i2) == 0;
            if (z2 != this.mClockVisibleByPolicy) {
                setClockVisibilityByPolicy(z2);
            }
        }
    }

    @Override // com.android.systemui.plugins.DarkIconDispatcher.DarkReceiver
    public void onDarkChanged(Rect rect, float f, int i, int i2, int i3, boolean z) {
        if (z) {
            this.mNonAdaptedColor = DarkIconDispatcher.getTint(rect, this, i);
        } else {
            if (DarkIconDispatcher.getDarkIntensity(rect, this, f) > 0.0f) {
                i2 = i3;
            }
            this.mNonAdaptedColor = i2;
        }
        if (!this.mUseWallpaperTextColor) {
            setTextColor(this.mNonAdaptedColor);
        }
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onDensityOrFontScaleChanged() {
        FontSizeUtils.updateFontSize(this, C0012R$dimen.status_bar_clock_size);
        setPaddingRelative(((TextView) this).mContext.getResources().getDimensionPixelSize(C0012R$dimen.status_bar_clock_starting_padding), 0, ((TextView) this).mContext.getResources().getDimensionPixelSize(C0012R$dimen.status_bar_clock_end_padding), 0);
    }

    /* access modifiers changed from: protected */
    public void onVisibilityChanged(View view, int i) {
        super.onVisibilityChanged(view, i);
        Iterator<ClockVisibilityListener> it = this.mVisibilityListeners.iterator();
        while (it.hasNext()) {
            it.next().onClockVisibilityChanged(isShown());
        }
    }

    public void addVisibilityListener(ClockVisibilityListener clockVisibilityListener) {
        this.mVisibilityListeners.add(clockVisibilityListener);
    }

    private void updateShowSeconds() {
        if (this.mShowSeconds) {
            if (this.mSecondsHandler == null && getDisplay() != null) {
                this.mSecondsHandler = new Handler();
                if (getDisplay().getState() == 2) {
                    this.mSecondsHandler.postAtTime(this.mSecondTick, ((SystemClock.uptimeMillis() / 1000) * 1000) + 1000);
                }
                IntentFilter intentFilter = new IntentFilter("android.intent.action.SCREEN_OFF");
                intentFilter.addAction("android.intent.action.SCREEN_ON");
                this.mBroadcastDispatcher.registerReceiver(this.mScreenReceiver, intentFilter);
            }
        } else if (this.mSecondsHandler != null) {
            this.mBroadcastDispatcher.unregisterReceiver(this.mScreenReceiver);
            this.mSecondsHandler.removeCallbacks(this.mSecondTick);
            this.mSecondsHandler = null;
            updateClock();
        }
    }

    private final void updateTime() {
        int i;
        int i2;
        if (this.mCalendar != null) {
            Context context = getContext();
            int i3 = DateFormat.is24HourFormat(context, this.mCurrentUserId) ? 32 : 16;
            int i4 = this.mClockMode;
            if (i4 == 2) {
                if (i3 == 16) {
                    i = C0021R$string.status_bar_clock_date_time_format_12;
                } else {
                    i = C0021R$string.status_bar_clock_date_time_format;
                }
            } else if (i4 == 1) {
                if (i3 == 16) {
                    i = C0021R$string.status_bar_clock_date_format_12;
                } else {
                    i = C0021R$string.status_bar_clock_date_format;
                }
            } else if (i4 == 3) {
                if (i3 == 16) {
                    i2 = C0021R$string.status_bar_clock_date_weekday_format_12;
                } else {
                    i2 = C0021R$string.status_bar_clock_date_weekday_format;
                }
                setContentDescription(this.mCalendar.format(context.getString(i3 == 16 ? C0021R$string.status_bar_clock_date_format_12 : C0021R$string.status_bar_clock_date_format)));
                i = i2;
            } else if (this.mAmPmStyle == 0) {
                setText(DateUtils.formatDateTime(this.mCalendar.getTimeInMillis(), i3 | 12));
                return;
            } else {
                setText(DateUtils.formatDateTime(this.mCalendar.getTimeInMillis(), i3 | 12 | 64));
                return;
            }
            setText(this.mCalendar.format(context.getString(i)));
        }
    }

    @Override // com.android.systemui.DemoMode
    public void dispatchDemoCommand(String str, Bundle bundle) {
        if (!this.mDemoMode && str.equals("enter")) {
            this.mDemoMode = true;
            updateClock();
        } else if (this.mDemoMode && str.equals("exit")) {
            this.mDemoMode = false;
            updateClock();
        } else if (this.mDemoMode && str.equals("clock")) {
            String string = bundle.getString("millis");
            String string2 = bundle.getString("hhmm");
            Calendar calendar = this.mCalendar;
            if (calendar != null) {
                if (string != null) {
                    calendar.setTimeInMillis(Long.parseLong(string));
                } else if (string2 != null && string2.length() == 4) {
                    int parseInt = Integer.parseInt(string2.substring(0, 2));
                    int parseInt2 = Integer.parseInt(string2.substring(2));
                    this.mCalendar.set(18, parseInt);
                    this.mCalendar.set(20, parseInt2);
                }
            }
            updateTime();
        }
    }
}
