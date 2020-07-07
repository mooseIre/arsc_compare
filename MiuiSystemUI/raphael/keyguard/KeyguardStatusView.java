package com.android.keyguard;

import android.app.AlarmManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Slog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextClock;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
import com.android.systemui.ChargingView;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.policy.DateView;
import java.util.Locale;

public class KeyguardStatusView extends GridLayout {
    private final AlarmManager mAlarmManager;
    private TextView mAlarmStatusView;
    private ChargingView mBatteryDoze;
    private ViewGroup mClockContainer;
    private TextClock mClockView;
    private DateView mDateView;
    private KeyguardUpdateMonitorCallback mInfoCallback;
    private final LockPatternUtils mLockPatternUtils;
    private TextView mOwnerInfo;
    private View[] mVisibleInDoze;

    public boolean hasOverlappingRendering() {
        return false;
    }

    public KeyguardStatusView(Context context) {
        this(context, (AttributeSet) null, 0);
    }

    public KeyguardStatusView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public KeyguardStatusView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mInfoCallback = new KeyguardUpdateMonitorCallback() {
            public void onTimeChanged() {
                KeyguardStatusView.this.refresh();
            }

            public void onKeyguardVisibilityChanged(boolean z) {
                if (z) {
                    Slog.v("KeyguardStatusView", "refresh statusview showing:" + z);
                    KeyguardStatusView.this.refresh();
                    KeyguardStatusView.this.updateOwnerInfo();
                }
            }

            public void onStartedWakingUp() {
                KeyguardStatusView.this.setEnableMarquee(true);
            }

            public void onFinishedGoingToSleep(int i) {
                KeyguardStatusView.this.setEnableMarquee(false);
            }

            public void onUserSwitchComplete(int i) {
                KeyguardStatusView.this.refresh();
                KeyguardStatusView.this.updateOwnerInfo();
            }
        };
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        this.mLockPatternUtils = new LockPatternUtils(getContext());
    }

    /* access modifiers changed from: private */
    public void setEnableMarquee(boolean z) {
        StringBuilder sb = new StringBuilder();
        sb.append(z ? "Enable" : "Disable");
        sb.append(" transport text marquee");
        Log.v("KeyguardStatusView", sb.toString());
        TextView textView = this.mAlarmStatusView;
        if (textView != null) {
            textView.setSelected(z);
        }
        TextView textView2 = this.mOwnerInfo;
        if (textView2 != null) {
            textView2.setSelected(z);
        }
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mClockContainer = (ViewGroup) findViewById(R.id.keyguard_clock_container);
        this.mAlarmStatusView = (TextView) findViewById(R.id.alarm_status);
        this.mDateView = (DateView) findViewById(R.id.date_view);
        this.mClockView = (TextClock) findViewById(R.id.clock_view);
        this.mClockView.setShowCurrentUserTime(true);
        this.mClockView.setAccessibilityDelegate(new KeyguardClockAccessibilityDelegate(this.mContext));
        this.mOwnerInfo = (TextView) findViewById(R.id.owner_info);
        this.mBatteryDoze = (ChargingView) findViewById(R.id.battery_doze);
        this.mVisibleInDoze = new View[]{this.mBatteryDoze, this.mClockView};
        setEnableMarquee(KeyguardUpdateMonitor.getInstance(this.mContext).isDeviceInteractive());
        refresh();
        updateOwnerInfo();
        this.mClockView.setElegantTextHeight(false);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mClockView.setTextSize(0, (float) getResources().getDimensionPixelSize(R.dimen.widget_big_font_size));
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mClockView.getLayoutParams();
        marginLayoutParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.bottom_text_spacing_digital);
        this.mClockView.setLayoutParams(marginLayoutParams);
        this.mDateView.setTextSize(0, (float) getResources().getDimensionPixelSize(R.dimen.widget_label_font_size));
        TextView textView = this.mOwnerInfo;
        if (textView != null) {
            textView.setTextSize(0, (float) getResources().getDimensionPixelSize(R.dimen.widget_label_font_size));
        }
    }

    public void refreshTime() {
        this.mDateView.setDatePattern(Patterns.dateViewSkel);
        this.mClockView.setFormat12Hour(Patterns.clockView12);
        this.mClockView.setFormat24Hour(Patterns.clockView24);
    }

    /* access modifiers changed from: private */
    public void refresh() {
        AlarmManager.AlarmClockInfo nextAlarmClock = this.mAlarmManager.getNextAlarmClock(-2);
        Patterns.update(this.mContext, nextAlarmClock != null);
        refreshTime();
        refreshAlarmStatus(nextAlarmClock);
    }

    /* access modifiers changed from: package-private */
    public void refreshAlarmStatus(AlarmManager.AlarmClockInfo alarmClockInfo) {
        if (alarmClockInfo != null) {
            String formatNextAlarm = formatNextAlarm(this.mContext, alarmClockInfo);
            this.mAlarmStatusView.setText(formatNextAlarm);
            this.mAlarmStatusView.setContentDescription(getResources().getString(R.string.keyguard_accessibility_next_alarm, new Object[]{formatNextAlarm}));
            this.mAlarmStatusView.setVisibility(0);
            return;
        }
        this.mAlarmStatusView.setVisibility(8);
    }

    public static String formatNextAlarm(Context context, AlarmManager.AlarmClockInfo alarmClockInfo) {
        if (alarmClockInfo == null) {
            return "";
        }
        return DateFormat.format(DateFormat.getBestDateTimePattern(Locale.getDefault(), DateFormat.is24HourFormat(context, KeyguardUpdateMonitor.getCurrentUser()) ? "EHm" : "Ehma"), alarmClockInfo.getTriggerTime()).toString();
    }

    /* access modifiers changed from: private */
    public void updateOwnerInfo() {
        if (this.mOwnerInfo != null) {
            String ownerInfo = getOwnerInfo();
            if (!TextUtils.isEmpty(ownerInfo)) {
                this.mOwnerInfo.setVisibility(0);
                this.mOwnerInfo.setText(ownerInfo);
                return;
            }
            this.mOwnerInfo.setVisibility(8);
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        KeyguardUpdateMonitor.getInstance(this.mContext).registerCallback(this.mInfoCallback);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        KeyguardUpdateMonitor.getInstance(this.mContext).removeCallback(this.mInfoCallback);
    }

    private String getOwnerInfo() {
        if (this.mLockPatternUtils.isDeviceOwnerInfoEnabled()) {
            return this.mLockPatternUtils.getDeviceOwnerInfo();
        }
        return null;
    }

    private static final class Patterns {
        static String cacheKey;
        static String clockView12;
        static String clockView24;
        static String dateViewSkel;

        static void update(Context context, boolean z) {
            Locale locale = Locale.getDefault();
            Resources resources = context.getResources();
            dateViewSkel = resources.getString(z ? R.string.abbrev_wday_month_day_no_year_alarm : R.string.abbrev_wday_month_day_no_year);
            String string = resources.getString(R.string.clock_12hr_format);
            String string2 = resources.getString(R.string.clock_24hr_format);
            String str = locale.toString() + dateViewSkel + string + string2;
            if (!str.equals(cacheKey)) {
                clockView12 = DateFormat.getBestDateTimePattern(locale, string);
                if (!context.getResources().getBoolean(R.bool.config_showAmpm) && !string.contains("a")) {
                    clockView12 = clockView12.replaceAll("a", "").trim();
                }
                clockView24 = DateFormat.getBestDateTimePattern(locale, string2);
                clockView24 = clockView24.replace(':', 60929);
                clockView12 = clockView12.replace(':', 60929);
                cacheKey = str;
            }
        }
    }
}
