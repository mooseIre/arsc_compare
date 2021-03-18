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
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.settingslib.Utils;
import com.android.systemui.C0009R$attr;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.DemoMode;
import com.android.systemui.Dependency;
import com.android.systemui.FontSizeUtils;
import com.android.systemui.R$styleable;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.Clock;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.tuner.TunerService;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import libcore.icu.LocaleData;

public class Clock extends TextView implements DemoMode, TunerService.Tunable, CommandQueue.Callbacks, DarkIconDispatcher.DarkReceiver, ConfigurationController.ConfigurationListener {
    private final int mAmPmStyle;
    private boolean mAttached;
    private final BroadcastDispatcher mBroadcastDispatcher;
    protected Calendar mCalendar;
    private SimpleDateFormat mClockFormat;
    private String mClockFormatString;
    private boolean mClockVisibleByPolicy;
    private boolean mClockVisibleByUser;
    private final CommandQueue mCommandQueue;
    private SimpleDateFormat mContentDescriptionFormat;
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
    private boolean mUseWallpaperTextColor;

    public Clock(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    /* JADX INFO: finally extract failed */
    public Clock(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mClockVisibleByPolicy = true;
        this.mClockVisibleByUser = true;
        this.mIntentReceiver = new BroadcastReceiver() {
            /* class com.android.systemui.statusbar.policy.Clock.AnonymousClass2 */

            public void onReceive(Context context, Intent intent) {
                Handler handler = Clock.this.getHandler();
                if (handler != null) {
                    String action = intent.getAction();
                    if (action.equals("android.intent.action.TIMEZONE_CHANGED")) {
                        handler.post(new Runnable(intent.getStringExtra("time-zone")) {
                            /* class com.android.systemui.statusbar.policy.$$Lambda$Clock$2$NVwlBsd8V0hLupY9sb0smFA7zNw */
                            public final /* synthetic */ String f$1;

                            {
                                this.f$1 = r2;
                            }

                            public final void run() {
                                Clock.AnonymousClass2.this.lambda$onReceive$0$Clock$2(this.f$1);
                            }
                        });
                    } else if (action.equals("android.intent.action.CONFIGURATION_CHANGED")) {
                        handler.post(new Runnable(Clock.this.getResources().getConfiguration().locale) {
                            /* class com.android.systemui.statusbar.policy.$$Lambda$Clock$2$BzKxslldgL1SP5a4jbR8GDSq90w */
                            public final /* synthetic */ Locale f$1;

                            {
                                this.f$1 = r2;
                            }

                            public final void run() {
                                Clock.AnonymousClass2.this.lambda$onReceive$1$Clock$2(this.f$1);
                            }
                        });
                    }
                    handler.post(new Runnable() {
                        /* class com.android.systemui.statusbar.policy.$$Lambda$Clock$2$mOTwR4Tu5xrxBBIUbNE9701lx4 */

                        public final void run() {
                            Clock.AnonymousClass2.this.lambda$onReceive$2$Clock$2();
                        }
                    });
                }
            }

            /* access modifiers changed from: private */
            /* renamed from: lambda$onReceive$0 */
            public /* synthetic */ void lambda$onReceive$0$Clock$2(String str) {
                Clock.this.mCalendar = Calendar.getInstance(TimeZone.getTimeZone(str));
                if (Clock.this.mClockFormat != null) {
                    Clock.this.mClockFormat.setTimeZone(Clock.this.mCalendar.getTimeZone());
                }
            }

            /* access modifiers changed from: private */
            /* renamed from: lambda$onReceive$1 */
            public /* synthetic */ void lambda$onReceive$1$Clock$2(Locale locale) {
                if (!locale.equals(Clock.this.mLocale)) {
                    Clock.this.mLocale = locale;
                    Clock.this.mClockFormatString = "";
                }
            }

            /* access modifiers changed from: private */
            /* renamed from: lambda$onReceive$2 */
            public /* synthetic */ void lambda$onReceive$2$Clock$2() {
                Clock.this.updateClock();
            }
        };
        this.mScreenReceiver = new BroadcastReceiver() {
            /* class com.android.systemui.statusbar.policy.Clock.AnonymousClass3 */

            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.intent.action.SCREEN_OFF".equals(action)) {
                    if (Clock.this.mSecondsHandler != null) {
                        Clock.this.mSecondsHandler.removeCallbacks(Clock.this.mSecondTick);
                    }
                } else if ("android.intent.action.SCREEN_ON".equals(action) && Clock.this.mSecondsHandler != null) {
                    Clock.this.mSecondsHandler.postAtTime(Clock.this.mSecondTick, ((SystemClock.uptimeMillis() / 1000) * 1000) + 1000);
                }
            }
        };
        this.mSecondTick = new Runnable() {
            /* class com.android.systemui.statusbar.policy.Clock.AnonymousClass4 */

            public void run() {
                Clock clock = Clock.this;
                if (clock.mCalendar != null) {
                    clock.updateClock();
                }
                Clock.this.mSecondsHandler.postAtTime(this, ((SystemClock.uptimeMillis() / 1000) * 1000) + 1000);
            }
        };
        this.mCommandQueue = (CommandQueue) Dependency.get(CommandQueue.class);
        TypedArray obtainStyledAttributes = context.getTheme().obtainStyledAttributes(attributeSet, R$styleable.Clock, 0, 0);
        try {
            this.mAmPmStyle = obtainStyledAttributes.getInt(R$styleable.Clock_amPmStyle, 2);
            this.mShowDark = obtainStyledAttributes.getBoolean(R$styleable.Clock_showDark, true);
            this.mNonAdaptedColor = getCurrentTextColor();
            obtainStyledAttributes.recycle();
            BroadcastDispatcher broadcastDispatcher = (BroadcastDispatcher) Dependency.get(BroadcastDispatcher.class);
            this.mBroadcastDispatcher = broadcastDispatcher;
            this.mCurrentUserTracker = new CurrentUserTracker(broadcastDispatcher) {
                /* class com.android.systemui.statusbar.policy.Clock.AnonymousClass1 */

                @Override // com.android.systemui.settings.CurrentUserTracker
                public void onUserSwitched(int i) {
                    Clock.this.mCurrentUserId = i;
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
            this.mCommandQueue.addCallback((CommandQueue.Callbacks) this);
            if (this.mShowDark) {
                ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).addDarkReceiver(this);
            }
            this.mCurrentUserTracker.startTracking();
            this.mCurrentUserId = this.mCurrentUserTracker.getCurrentUserId();
        }
        this.mCalendar = Calendar.getInstance(TimeZone.getDefault());
        this.mClockFormatString = "";
        updateClock();
        updateClockVisibility();
        updateShowSeconds();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mAttached) {
            this.mBroadcastDispatcher.unregisterReceiver(this.mIntentReceiver);
            this.mAttached = false;
            ((TunerService) Dependency.get(TunerService.class)).removeTunable(this);
            this.mCommandQueue.removeCallback((CommandQueue.Callbacks) this);
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

    public void setClockVisibleByUser(boolean z) {
        this.mClockVisibleByUser = z;
        updateClockVisibility();
    }

    public void setClockVisibilityByPolicy(boolean z) {
        this.mClockVisibleByPolicy = z;
        updateClockVisibility();
    }

    private boolean shouldBeVisible() {
        return this.mClockVisibleByPolicy && this.mClockVisibleByUser;
    }

    private void updateClockVisibility() {
        super.setVisibility(shouldBeVisible() ? 0 : 8);
    }

    /* access modifiers changed from: protected */
    public void updateClock() {
        if (!this.mDemoMode) {
            this.mCalendar.setTimeInMillis(System.currentTimeMillis());
            setText(getSmallTime());
            setContentDescription(this.mContentDescriptionFormat.format(this.mCalendar.getTime()));
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
        int tint = DarkIconDispatcher.getTint(rect, this, i);
        this.mNonAdaptedColor = tint;
        if (!this.mUseWallpaperTextColor) {
            setTextColor(tint);
        }
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onDensityOrFontScaleChanged() {
        FontSizeUtils.updateFontSize(this, C0012R$dimen.status_bar_clock_size);
        setPaddingRelative(((TextView) this).mContext.getResources().getDimensionPixelSize(C0012R$dimen.status_bar_clock_starting_padding), 0, ((TextView) this).mContext.getResources().getDimensionPixelSize(C0012R$dimen.status_bar_clock_end_padding), 0);
    }

    public void useWallpaperTextColor(boolean z) {
        if (z != this.mUseWallpaperTextColor) {
            this.mUseWallpaperTextColor = z;
            if (z) {
                setTextColor(Utils.getColorAttr(((TextView) this).mContext, C0009R$attr.wallpaperTextColor));
            } else {
                setTextColor(this.mNonAdaptedColor);
            }
        }
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

    private final CharSequence getSmallTime() {
        String str;
        SimpleDateFormat simpleDateFormat;
        Context context = getContext();
        boolean is24HourFormat = DateFormat.is24HourFormat(context, this.mCurrentUserId);
        LocaleData localeData = LocaleData.get(context.getResources().getConfiguration().locale);
        if (this.mShowSeconds) {
            str = is24HourFormat ? localeData.timeFormat_Hms : localeData.timeFormat_hms;
        } else {
            str = is24HourFormat ? localeData.timeFormat_Hm : localeData.timeFormat_hm;
        }
        if (!str.equals(this.mClockFormatString)) {
            this.mContentDescriptionFormat = new SimpleDateFormat(str);
            if (this.mAmPmStyle != 0) {
                int i = 0;
                boolean z = false;
                while (true) {
                    if (i >= str.length()) {
                        i = -1;
                        break;
                    }
                    char charAt = str.charAt(i);
                    if (charAt == '\'') {
                        z = !z;
                    }
                    if (!z && charAt == 'a') {
                        break;
                    }
                    i++;
                }
                if (i >= 0) {
                    int i2 = i;
                    while (i2 > 0 && Character.isWhitespace(str.charAt(i2 - 1))) {
                        i2--;
                    }
                    str = str.substring(0, i2) + (char) 61184 + str.substring(i2, i) + "a" + (char) 61185 + str.substring(i + 1);
                }
            }
            simpleDateFormat = new SimpleDateFormat(str);
            this.mClockFormat = simpleDateFormat;
            this.mClockFormatString = str;
        } else {
            simpleDateFormat = this.mClockFormat;
        }
        String format = simpleDateFormat.format(this.mCalendar.getTime());
        if (this.mAmPmStyle != 0) {
            int indexOf = format.indexOf(61184);
            int indexOf2 = format.indexOf(61185);
            if (indexOf >= 0 && indexOf2 > indexOf) {
                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(format);
                int i3 = this.mAmPmStyle;
                if (i3 == 2) {
                    spannableStringBuilder.delete(indexOf, indexOf2 + 1);
                } else {
                    if (i3 == 1) {
                        spannableStringBuilder.setSpan(new RelativeSizeSpan(0.7f), indexOf, indexOf2, 34);
                    }
                    spannableStringBuilder.delete(indexOf2, indexOf2 + 1);
                    spannableStringBuilder.delete(indexOf, indexOf + 1);
                }
                return spannableStringBuilder;
            }
        }
        return format;
    }

    @Override // com.android.systemui.DemoMode
    public void dispatchDemoCommand(String str, Bundle bundle) {
        if (!this.mDemoMode && str.equals("enter")) {
            this.mDemoMode = true;
        } else if (this.mDemoMode && str.equals("exit")) {
            this.mDemoMode = false;
            updateClock();
        } else if (this.mDemoMode && str.equals("clock")) {
            String string = bundle.getString("millis");
            String string2 = bundle.getString("hhmm");
            if (string != null) {
                this.mCalendar.setTimeInMillis(Long.parseLong(string));
            } else if (string2 != null && string2.length() == 4) {
                int parseInt = Integer.parseInt(string2.substring(0, 2));
                int parseInt2 = Integer.parseInt(string2.substring(2));
                if (DateFormat.is24HourFormat(getContext(), this.mCurrentUserId)) {
                    this.mCalendar.set(11, parseInt);
                } else {
                    this.mCalendar.set(10, parseInt);
                }
                this.mCalendar.set(12, parseInt2);
            }
            setText(getSmallTime());
            setContentDescription(this.mContentDescriptionFormat.format(this.mCalendar.getTime()));
        }
    }
}
