package com.android.keyguard;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.IActivityManager;
import android.app.IStopUserCallback;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.RemoteException;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Slog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.graphics.ColorUtils;
import com.android.internal.widget.LockPatternUtils;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.DateView;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.TimeZone;

public class KeyguardStatusView extends GridLayout implements ConfigurationController.ConfigurationListener {
    private final AlarmManager mAlarmManager;
    private KeyguardClockSwitch mClockView;
    private float mDarkAmount;
    private DateView mDateView;
    private float mDateViewTextSize;
    private Handler mHandler;
    private final IActivityManager mIActivityManager;
    private int mIconTopMargin;
    private int mIconTopMarginWithHeader;
    private KeyguardUpdateMonitorCallback mInfoCallback;
    private KeyguardSliceView mKeyguardSlice;
    private final LockPatternUtils mLockPatternUtils;
    private TextView mLogoutView;
    private View mNotificationIcons;
    private TextView mOwnerInfo;
    private Runnable mPendingMarqueeStart;
    private boolean mPulsing;
    private boolean mShowingHeader;
    private int mTextColor;

    public KeyguardStatusView(Context context) {
        this(context, null, 0);
    }

    public KeyguardStatusView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public KeyguardStatusView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mDarkAmount = 0.0f;
        this.mInfoCallback = new KeyguardUpdateMonitorCallback() {
            /* class com.android.keyguard.KeyguardStatusView.AnonymousClass1 */

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onTimeChanged() {
                KeyguardStatusView.this.refreshTime();
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onTimeZoneChanged(TimeZone timeZone) {
                KeyguardStatusView.this.updateTimeZone(timeZone);
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onKeyguardVisibilityChanged(boolean z) {
                if (z) {
                    Slog.v("KeyguardStatusView", "refresh statusview showing:" + z);
                    KeyguardStatusView.this.refreshTime();
                    KeyguardStatusView.this.updateOwnerInfo();
                    KeyguardStatusView.this.updateLogoutView();
                }
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onStartedWakingUp() {
                KeyguardStatusView.this.setEnableMarquee(true);
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onFinishedGoingToSleep(int i) {
                KeyguardStatusView.this.setEnableMarquee(false);
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onUserSwitchComplete(int i) {
                KeyguardStatusView.this.refreshFormat();
                KeyguardStatusView.this.updateOwnerInfo();
                KeyguardStatusView.this.updateLogoutView();
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onLogoutEnabledChanged() {
                KeyguardStatusView.this.updateLogoutView();
            }
        };
        this.mIActivityManager = ActivityManager.getService();
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        this.mLockPatternUtils = new LockPatternUtils(getContext());
        this.mHandler = new Handler();
        onDensityOrFontScaleChanged();
    }

    public boolean hasCustomClock() {
        return this.mClockView.hasCustomClock();
    }

    public void setHasVisibleNotifications(boolean z) {
        this.mClockView.setHasVisibleNotifications(z);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setEnableMarquee(boolean z) {
        StringBuilder sb = new StringBuilder();
        sb.append("Schedule setEnableMarquee: ");
        sb.append(z ? "Enable" : "Disable");
        Log.v("KeyguardStatusView", sb.toString());
        if (!z) {
            Runnable runnable = this.mPendingMarqueeStart;
            if (runnable != null) {
                this.mHandler.removeCallbacks(runnable);
                this.mPendingMarqueeStart = null;
            }
            setEnableMarqueeImpl(false);
        } else if (this.mPendingMarqueeStart == null) {
            $$Lambda$KeyguardStatusView$ps9yj97ShIVR2u2hJB8SKuKkkQ r3 = new Runnable() {
                /* class com.android.keyguard.$$Lambda$KeyguardStatusView$ps9yj97ShIVR2u2hJB8SKuKkkQ */

                public final void run() {
                    KeyguardStatusView.this.lambda$setEnableMarquee$0$KeyguardStatusView();
                }
            };
            this.mPendingMarqueeStart = r3;
            this.mHandler.postDelayed(r3, 2000);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setEnableMarquee$0 */
    public /* synthetic */ void lambda$setEnableMarquee$0$KeyguardStatusView() {
        setEnableMarqueeImpl(true);
        this.mPendingMarqueeStart = null;
    }

    private void setEnableMarqueeImpl(boolean z) {
        StringBuilder sb = new StringBuilder();
        sb.append(z ? "Enable" : "Disable");
        sb.append(" transport text marquee");
        Log.v("KeyguardStatusView", sb.toString());
        TextView textView = this.mOwnerInfo;
        if (textView != null) {
            textView.setSelected(z);
        }
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        LinearLayout linearLayout = (LinearLayout) findViewById(C0015R$id.status_view_container);
        this.mLogoutView = (TextView) findViewById(C0015R$id.logout);
        this.mNotificationIcons = findViewById(C0015R$id.clock_notification_icon_container);
        TextView textView = this.mLogoutView;
        if (textView != null) {
            textView.setOnClickListener(new View.OnClickListener() {
                /* class com.android.keyguard.$$Lambda$KeyguardStatusView$Pryio69yVoRI9F153p5QiMZebw */

                public final void onClick(View view) {
                    KeyguardStatusView.m3lambda$Pryio69yVoRI9F153p5QiMZebw(KeyguardStatusView.this, view);
                }
            });
        }
        KeyguardClockSwitch keyguardClockSwitch = (KeyguardClockSwitch) findViewById(C0015R$id.keyguard_clock_container);
        this.mClockView = keyguardClockSwitch;
        keyguardClockSwitch.setShowCurrentUserTime(true);
        if (KeyguardClockAccessibilityDelegate.isNeeded(((GridLayout) this).mContext)) {
            this.mClockView.setAccessibilityDelegate(new KeyguardClockAccessibilityDelegate(((GridLayout) this).mContext));
        }
        this.mOwnerInfo = (TextView) findViewById(C0015R$id.owner_info);
        this.mKeyguardSlice = (KeyguardSliceView) findViewById(C0015R$id.keyguard_status_area);
        this.mTextColor = this.mClockView.getCurrentTextColor();
        this.mDateView = (DateView) findViewById(C0015R$id.date_view);
        this.mKeyguardSlice.setContentChangeListener(new Runnable() {
            /* class com.android.keyguard.$$Lambda$KeyguardStatusView$Xo7rGDTjuOiD9nJpe80IUZ1ddFw */

            public final void run() {
                KeyguardStatusView.lambda$Xo7rGDTjuOiD9nJpe80IUZ1ddFw(KeyguardStatusView.this);
            }
        });
        onSliceContentChanged();
        setEnableMarquee(((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isDeviceInteractive());
        refreshFormat();
        updateOwnerInfo();
        updateLogoutView();
        updateDark();
    }

    /* access modifiers changed from: private */
    public void onSliceContentChanged() {
        boolean hasHeader = this.mKeyguardSlice.hasHeader();
        this.mClockView.setKeyguardShowingHeader(hasHeader);
        if (this.mShowingHeader != hasHeader) {
            this.mShowingHeader = hasHeader;
            View view = this.mNotificationIcons;
            if (view != null) {
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
                marginLayoutParams.setMargins(marginLayoutParams.leftMargin, hasHeader ? this.mIconTopMarginWithHeader : this.mIconTopMargin, marginLayoutParams.rightMargin, marginLayoutParams.bottomMargin);
                this.mNotificationIcons.setLayoutParams(marginLayoutParams);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        layoutOwnerInfo();
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onDensityOrFontScaleChanged() {
        KeyguardClockSwitch keyguardClockSwitch = this.mClockView;
        if (keyguardClockSwitch != null) {
            keyguardClockSwitch.setTextSize(0, (float) getResources().getDimensionPixelSize(C0012R$dimen.widget_big_font_size));
        }
        if (this.mDateView != null) {
            float dimensionPixelSize = (float) getResources().getDimensionPixelSize(C0012R$dimen.widget_label_font_size);
            this.mDateViewTextSize = dimensionPixelSize;
            this.mDateView.setTextSize(0, dimensionPixelSize);
        }
        TextView textView = this.mOwnerInfo;
        if (textView != null) {
            textView.setTextSize(0, (float) getResources().getDimensionPixelSize(C0012R$dimen.widget_label_font_size));
        }
        loadBottomMargin();
    }

    public void dozeTimeTick() {
        refreshTime();
        this.mKeyguardSlice.refresh();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void refreshTime() {
        if (getVisibility() == 0) {
            this.mClockView.refresh();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateTimeZone(TimeZone timeZone) {
        if (getVisibility() == 0) {
            this.mClockView.onTimeZoneChanged(timeZone);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void refreshFormat() {
        Patterns.update(((GridLayout) this).mContext, this.mAlarmManager.getNextAlarmClock(-2) != null);
        this.mDateView.setDatePattern(Patterns.dateViewSkel);
        this.mClockView.setFormat12Hour(Patterns.clockView12);
        this.mClockView.setFormat24Hour(Patterns.clockView24);
    }

    public int getLogoutButtonHeight() {
        TextView textView = this.mLogoutView;
        if (textView != null && textView.getVisibility() == 0) {
            return this.mLogoutView.getHeight();
        }
        return 0;
    }

    public float getClockTextSize() {
        return this.mClockView.getTextSize();
    }

    public int getClockPreferredY(int i) {
        return this.mClockView.getPreferredY(i);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateLogoutView() {
        TextView textView = this.mLogoutView;
        if (textView != null) {
            textView.setVisibility(shouldShowLogout() ? 0 : 8);
            this.mLogoutView.setText(((GridLayout) this).mContext.getResources().getString(17040300));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateOwnerInfo() {
        if (this.mOwnerInfo != null) {
            String deviceOwnerInfo = this.mLockPatternUtils.getDeviceOwnerInfo();
            if (deviceOwnerInfo == null && this.mLockPatternUtils.isOwnerInfoEnabled(KeyguardUpdateMonitor.getCurrentUser())) {
                deviceOwnerInfo = this.mLockPatternUtils.getOwnerInfo(KeyguardUpdateMonitor.getCurrentUser());
            }
            this.mOwnerInfo.setText(deviceOwnerInfo);
            updateDark();
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).registerCallback(this.mInfoCallback);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).removeCallback(this.mInfoCallback);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).removeCallback(this);
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onLocaleListChanged() {
        refreshFormat();
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        Object obj;
        printWriter.println("KeyguardStatusView:");
        StringBuilder sb = new StringBuilder();
        sb.append("  mOwnerInfo: ");
        TextView textView = this.mOwnerInfo;
        boolean z = true;
        if (textView == null) {
            obj = "null";
        } else {
            obj = Boolean.valueOf(textView.getVisibility() == 0);
        }
        sb.append(obj);
        printWriter.println(sb.toString());
        printWriter.println("  mPulsing: " + this.mPulsing);
        printWriter.println("  mDarkAmount: " + this.mDarkAmount);
        printWriter.println("  mTextColor: " + Integer.toHexString(this.mTextColor));
        if (this.mLogoutView != null) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("  logout visible: ");
            if (this.mLogoutView.getVisibility() != 0) {
                z = false;
            }
            sb2.append(z);
            printWriter.println(sb2.toString());
        }
        KeyguardClockSwitch keyguardClockSwitch = this.mClockView;
        if (keyguardClockSwitch != null) {
            keyguardClockSwitch.dump(fileDescriptor, printWriter, strArr);
        }
        KeyguardSliceView keyguardSliceView = this.mKeyguardSlice;
        if (keyguardSliceView != null) {
            keyguardSliceView.dump(fileDescriptor, printWriter, strArr);
        }
    }

    private void loadBottomMargin() {
        this.mIconTopMargin = getResources().getDimensionPixelSize(C0012R$dimen.widget_vertical_padding);
        this.mIconTopMarginWithHeader = getResources().getDimensionPixelSize(C0012R$dimen.widget_vertical_padding_with_header);
    }

    /* access modifiers changed from: private */
    public static final class Patterns {
        static String cacheKey;
        static String clockView12;
        static String clockView24;
        static String dateViewSkel;

        static void update(Context context, boolean z) {
            int i;
            Locale locale = Locale.getDefault();
            Resources resources = context.getResources();
            if (z) {
                i = C0021R$string.abbrev_wday_month_day_no_year_alarm;
            } else {
                i = C0021R$string.abbrev_wday_month_day_no_year;
            }
            dateViewSkel = resources.getString(i);
            String string = resources.getString(C0021R$string.clock_12hr_format);
            String string2 = resources.getString(C0021R$string.clock_24hr_format);
            String str = locale.toString() + string + string2;
            if (!str.equals(cacheKey)) {
                clockView12 = DateFormat.getBestDateTimePattern(locale, string);
                if (!context.getResources().getBoolean(C0010R$bool.config_showAmpm) && !string.contains("a")) {
                    clockView12 = clockView12.replaceAll("a", "").trim();
                }
                String bestDateTimePattern = DateFormat.getBestDateTimePattern(locale, string2);
                clockView24 = bestDateTimePattern;
                clockView24 = bestDateTimePattern.replace(':', (char) 60929);
                clockView12 = clockView12.replace(':', (char) 60929);
                cacheKey = str;
            }
        }
    }

    public void setDarkAmount(float f) {
        if (this.mDarkAmount != f) {
            this.mDarkAmount = f;
            this.mClockView.setDarkAmount(f);
            updateDark();
        }
    }

    private void updateDark() {
        float f = 1.0f;
        int i = 0;
        boolean z = this.mDarkAmount == 1.0f;
        TextView textView = this.mLogoutView;
        if (textView != null) {
            if (z) {
                f = 0.0f;
            }
            textView.setAlpha(f);
        }
        TextView textView2 = this.mOwnerInfo;
        if (textView2 != null) {
            boolean z2 = !TextUtils.isEmpty(textView2.getText());
            TextView textView3 = this.mOwnerInfo;
            if (!z2) {
                i = 8;
            }
            textView3.setVisibility(i);
            layoutOwnerInfo();
        }
        int blendARGB = ColorUtils.blendARGB(this.mTextColor, -1, this.mDarkAmount);
        this.mKeyguardSlice.setDarkAmount(this.mDarkAmount);
        this.mClockView.setTextColor(blendARGB);
    }

    private void layoutOwnerInfo() {
        TextView textView = this.mOwnerInfo;
        if (textView == null || textView.getVisibility() == 8) {
            View view = this.mNotificationIcons;
            if (view != null) {
                view.setScrollY(0);
                return;
            }
            return;
        }
        this.mOwnerInfo.setAlpha(1.0f - this.mDarkAmount);
        int bottom = (int) (((float) ((this.mOwnerInfo.getBottom() + this.mOwnerInfo.getPaddingBottom()) - (this.mOwnerInfo.getTop() - this.mOwnerInfo.getPaddingTop()))) * this.mDarkAmount);
        setBottom(getMeasuredHeight() - bottom);
        View view2 = this.mNotificationIcons;
        if (view2 != null) {
            view2.setScrollY(bottom);
        }
    }

    public void setPulsing(boolean z) {
        if (this.mPulsing != z) {
            this.mPulsing = z;
        }
    }

    private boolean shouldShowLogout() {
        return ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isLogoutEnabled() && KeyguardUpdateMonitor.getCurrentUser() != 0;
    }

    /* access modifiers changed from: private */
    public void onLogoutClicked(View view) {
        int currentUser = KeyguardUpdateMonitor.getCurrentUser();
        try {
            this.mIActivityManager.switchUser(0);
            this.mIActivityManager.stopUser(currentUser, true, (IStopUserCallback) null);
        } catch (RemoteException e) {
            Log.e("KeyguardStatusView", "Failed to logout user", e);
        }
    }
}
