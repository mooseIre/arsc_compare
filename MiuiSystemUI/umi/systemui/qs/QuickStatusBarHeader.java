package com.android.systemui.qs;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Handler;
import android.service.notification.ZenModeConfig;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.ContextThemeWrapper;
import android.view.DisplayCutout;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.Observer;
import com.android.systemui.C0009R$dimen;
import com.android.systemui.C0012R$id;
import com.android.systemui.C0019R$style;
import com.android.systemui.Dependency;
import com.android.systemui.DualToneHandler;
import com.android.systemui.MiuiBatteryMeterView;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.qs.TouchAnimator;
import com.android.systemui.qs.carrier.QSCarrierGroup;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.MiuiStatusIconContainer;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.phone.StatusBarWindowView;
import com.android.systemui.statusbar.policy.Clock;
import com.android.systemui.statusbar.policy.DateView;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.statusbar.views.NetworkSpeedView;
import com.android.systemui.util.RingerModeTracker;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class QuickStatusBarHeader extends RelativeLayout implements View.OnClickListener, NextAlarmController.NextAlarmChangeCallback, ZenModeController.Callback, LifecycleOwner {
    private final ActivityStarter mActivityStarter;
    private final NextAlarmController mAlarmController;
    private Clock mClockView;
    private final CommandQueue mCommandQueue;
    private int mContentMarginEnd;
    private int mContentMarginStart;
    private int mCutOutPaddingLeft;
    private int mCutOutPaddingRight;
    private float mExpandedHeaderAlpha = 1.0f;
    private View mHeaderTextContainerView;
    private StatusBarIconController.MiuiLightDarkIconManager mIconManager;
    private final LifecycleRegistry mLifecycle = new LifecycleRegistry(this);
    private boolean mListening;
    private AlarmManager.AlarmClockInfo mNextAlarm;
    private View mNextAlarmContainer;
    private ImageView mNextAlarmIcon;
    private TextView mNextAlarmTextView;
    private boolean mQsDisabled;
    private View mRingerContainer;
    private int mRingerMode = 2;
    private ImageView mRingerModeIcon;
    private TextView mRingerModeTextView;
    private RingerModeTracker mRingerModeTracker;
    private int mRoundedCornerPadding = 0;
    private final StatusBarIconController mStatusBarIconController;
    private int mStatusBarPaddingTop = 0;
    private View mStatusSeparator;
    private View mSystemIconsView;
    private int mWaterfallTopInset;
    private final ZenModeController mZenController;

    public static float getColorIntensity(int i) {
        return i == -1 ? 0.0f : 1.0f;
    }

    private void updateStatusIconAlphaAnimator() {
    }

    public QuickStatusBarHeader(Context context, AttributeSet attributeSet, NextAlarmController nextAlarmController, ZenModeController zenModeController, StatusBarIconController statusBarIconController, ActivityStarter activityStarter, CommandQueue commandQueue, RingerModeTracker ringerModeTracker) {
        super(context, attributeSet);
        new Handler();
        this.mAlarmController = nextAlarmController;
        this.mZenController = zenModeController;
        this.mStatusBarIconController = statusBarIconController;
        this.mActivityStarter = activityStarter;
        new DualToneHandler(new ContextThemeWrapper(context, C0019R$style.QSHeaderTheme));
        this.mCommandQueue = commandQueue;
        this.mRingerModeTracker = ringerModeTracker;
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        Class cls = DarkIconDispatcher.class;
        super.onFinishInflate();
        this.mSystemIconsView = findViewById(C0012R$id.quick_status_bar_system_icons);
        findViewById(C0012R$id.quick_qs_status_icons);
        MiuiStatusIconContainer miuiStatusIconContainer = (MiuiStatusIconContainer) findViewById(C0012R$id.statusIcons);
        miuiStatusIconContainer.addIgnoredSlots(getIgnoredIconSlots());
        miuiStatusIconContainer.setShouldRestrictIcons(false);
        this.mIconManager = new StatusBarIconController.MiuiLightDarkIconManager(miuiStatusIconContainer, this.mCommandQueue, true, ((DarkIconDispatcher) Dependency.get(cls)).getLightModeIconColorSingleTone());
        this.mHeaderTextContainerView = findViewById(C0012R$id.header_text_container);
        this.mStatusSeparator = findViewById(C0012R$id.status_separator);
        this.mNextAlarmIcon = (ImageView) findViewById(C0012R$id.next_alarm_icon);
        this.mNextAlarmTextView = (TextView) findViewById(C0012R$id.next_alarm_text);
        View findViewById = findViewById(C0012R$id.alarm_container);
        this.mNextAlarmContainer = findViewById;
        findViewById.setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                QuickStatusBarHeader.this.onClick(view);
            }
        });
        this.mRingerModeIcon = (ImageView) findViewById(C0012R$id.ringer_mode_icon);
        this.mRingerModeTextView = (TextView) findViewById(C0012R$id.ringer_mode_text);
        View findViewById2 = findViewById(C0012R$id.ringer_container);
        this.mRingerContainer = findViewById2;
        findViewById2.setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                QuickStatusBarHeader.this.onClick(view);
            }
        });
        QSCarrierGroup qSCarrierGroup = (QSCarrierGroup) findViewById(C0012R$id.carrier_group);
        updateResources();
        Rect rect = new Rect(0, 0, 0, 0);
        DarkIconDispatcher darkIconDispatcher = (DarkIconDispatcher) Dependency.get(cls);
        int lightModeIconColorSingleTone = darkIconDispatcher.getLightModeIconColorSingleTone();
        applyDarkness(C0012R$id.clock, rect, 0.0f, lightModeIconColorSingleTone, lightModeIconColorSingleTone, darkIconDispatcher.getDarkModeIconColorSingleTone());
        this.mIconManager.setLight(true, lightModeIconColorSingleTone);
        this.mNextAlarmIcon.setImageTintList(ColorStateList.valueOf(lightModeIconColorSingleTone));
        this.mRingerModeIcon.setImageTintList(ColorStateList.valueOf(lightModeIconColorSingleTone));
        NetworkSpeedView networkSpeedView = (NetworkSpeedView) findViewById(C0012R$id.fullscreen_network_speed_view);
        networkSpeedView.setVisibilityByStatusBar(true);
        networkSpeedView.setTextColor(lightModeIconColorSingleTone);
        Clock clock = (Clock) findViewById(C0012R$id.clock);
        this.mClockView = clock;
        clock.setOnClickListener(this);
        DateView dateView = (DateView) findViewById(C0012R$id.date);
        MiuiBatteryMeterView miuiBatteryMeterView = (MiuiBatteryMeterView) findViewById(C0012R$id.batteryRemainingIcon);
        this.mRingerModeTextView.setSelected(true);
        this.mNextAlarmTextView.setSelected(true);
    }

    private List<String> getIgnoredIconSlots() {
        ArrayList arrayList = new ArrayList();
        arrayList.add(this.mContext.getResources().getString(17041386));
        arrayList.add(this.mContext.getResources().getString(17041398));
        return arrayList;
    }

    private void updateStatusText() {
        boolean z = true;
        int i = 0;
        if (updateRingerStatus() || updateAlarmStatus()) {
            boolean z2 = this.mNextAlarmTextView.getVisibility() == 0;
            if (this.mRingerModeTextView.getVisibility() != 0) {
                z = false;
            }
            View view = this.mStatusSeparator;
            if (!z2 || !z) {
                i = 8;
            }
            view.setVisibility(i);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0051  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0053  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x005b  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x005d  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0065  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0078  */
    /* JADX WARNING: Removed duplicated region for block: B:31:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean updateRingerStatus() {
        /*
            r8 = this;
            android.widget.TextView r0 = r8.mRingerModeTextView
            int r0 = r0.getVisibility()
            r1 = 1
            r2 = 0
            if (r0 != 0) goto L_0x000c
            r0 = r1
            goto L_0x000d
        L_0x000c:
            r0 = r2
        L_0x000d:
            android.widget.TextView r3 = r8.mRingerModeTextView
            java.lang.CharSequence r3 = r3.getText()
            com.android.systemui.statusbar.policy.ZenModeController r4 = r8.mZenController
            int r4 = r4.getZen()
            com.android.systemui.statusbar.policy.ZenModeController r5 = r8.mZenController
            android.app.NotificationManager$Policy r5 = r5.getConsolidatedPolicy()
            boolean r4 = android.service.notification.ZenModeConfig.isZenOverridingRinger(r4, r5)
            if (r4 != 0) goto L_0x004a
            int r4 = r8.mRingerMode
            if (r4 != r1) goto L_0x0039
            android.widget.ImageView r4 = r8.mRingerModeIcon
            int r5 = com.android.systemui.C0010R$drawable.ic_volume_ringer_vibrate
            r4.setImageResource(r5)
            android.widget.TextView r4 = r8.mRingerModeTextView
            int r5 = com.android.systemui.C0018R$string.qs_status_phone_vibrate
            r4.setText(r5)
        L_0x0037:
            r4 = r1
            goto L_0x004b
        L_0x0039:
            if (r4 != 0) goto L_0x004a
            android.widget.ImageView r4 = r8.mRingerModeIcon
            int r5 = com.android.systemui.C0010R$drawable.ic_volume_ringer_mute
            r4.setImageResource(r5)
            android.widget.TextView r4 = r8.mRingerModeTextView
            int r5 = com.android.systemui.C0018R$string.qs_status_phone_muted
            r4.setText(r5)
            goto L_0x0037
        L_0x004a:
            r4 = r2
        L_0x004b:
            android.widget.ImageView r5 = r8.mRingerModeIcon
            r6 = 8
            if (r4 == 0) goto L_0x0053
            r7 = r2
            goto L_0x0054
        L_0x0053:
            r7 = r6
        L_0x0054:
            r5.setVisibility(r7)
            android.widget.TextView r5 = r8.mRingerModeTextView
            if (r4 == 0) goto L_0x005d
            r7 = r2
            goto L_0x005e
        L_0x005d:
            r7 = r6
        L_0x005e:
            r5.setVisibility(r7)
            android.view.View r5 = r8.mRingerContainer
            if (r4 == 0) goto L_0x0066
            r6 = r2
        L_0x0066:
            r5.setVisibility(r6)
            if (r0 != r4) goto L_0x0079
            android.widget.TextView r8 = r8.mRingerModeTextView
            java.lang.CharSequence r8 = r8.getText()
            boolean r8 = java.util.Objects.equals(r3, r8)
            if (r8 != 0) goto L_0x0078
            goto L_0x0079
        L_0x0078:
            r1 = r2
        L_0x0079:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.qs.QuickStatusBarHeader.updateRingerStatus():boolean");
    }

    private boolean updateAlarmStatus() {
        boolean z;
        boolean z2 = this.mNextAlarmTextView.getVisibility() == 0;
        CharSequence text = this.mNextAlarmTextView.getText();
        AlarmManager.AlarmClockInfo alarmClockInfo = this.mNextAlarm;
        if (alarmClockInfo != null) {
            this.mNextAlarmTextView.setText(formatNextAlarm(alarmClockInfo));
            z = true;
        } else {
            z = false;
        }
        int i = 8;
        this.mNextAlarmIcon.setVisibility(z ? 0 : 8);
        this.mNextAlarmTextView.setVisibility(z ? 0 : 8);
        View view = this.mNextAlarmContainer;
        if (z) {
            i = 0;
        }
        view.setVisibility(i);
        if (z2 != z || !Objects.equals(text, this.mNextAlarmTextView.getText())) {
            return true;
        }
        return false;
    }

    private void applyDarkness(int i, Rect rect, float f, int i2, int i3, int i4) {
        View findViewById = findViewById(i);
        if (findViewById instanceof DarkIconDispatcher.DarkReceiver) {
            ((DarkIconDispatcher.DarkReceiver) findViewById).onDarkChanged(rect, f, i2, i3, i4, false);
        }
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateResources();
        this.mClockView.useWallpaperTextColor(configuration.orientation == 2);
    }

    public void onRtlPropertiesChanged(int i) {
        super.onRtlPropertiesChanged(i);
        updateResources();
    }

    private void updateMinimumHeight() {
        setMinimumHeight(this.mContext.getResources().getDimensionPixelSize(17105489) + this.mContext.getResources().getDimensionPixelSize(C0009R$dimen.qs_quick_header_panel_height));
    }

    private void updateResources() {
        Resources resources = this.mContext.getResources();
        updateMinimumHeight();
        this.mRoundedCornerPadding = resources.getDimensionPixelSize(C0009R$dimen.rounded_corner_content_padding);
        this.mStatusBarPaddingTop = resources.getDimensionPixelSize(C0009R$dimen.status_bar_padding_top);
        this.mHeaderTextContainerView.getLayoutParams().height = resources.getDimensionPixelSize(C0009R$dimen.qs_header_tooltip_height);
        View view = this.mHeaderTextContainerView;
        view.setLayoutParams(view.getLayoutParams());
        this.mSystemIconsView.getLayoutParams().height = resources.getDimensionPixelSize(17105440);
        View view2 = this.mSystemIconsView;
        view2.setLayoutParams(view2.getLayoutParams());
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (this.mQsDisabled) {
            layoutParams.height = resources.getDimensionPixelSize(17105440);
        } else {
            layoutParams.height = -2;
        }
        setLayoutParams(layoutParams);
        updateStatusIconAlphaAnimator();
        updateHeaderTextContainerAlphaAnimator();
    }

    private void updateHeaderTextContainerAlphaAnimator() {
        TouchAnimator.Builder builder = new TouchAnimator.Builder();
        builder.addFloat(this.mHeaderTextContainerView, "alpha", 0.0f, 0.0f, this.mExpandedHeaderAlpha);
        builder.build();
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mRingerModeTracker.getRingerModeInternal().observe(this, new Observer() {
            public final void onChanged(Object obj) {
                QuickStatusBarHeader.this.lambda$onAttachedToWindow$0$QuickStatusBarHeader((Integer) obj);
            }
        });
        this.mStatusBarIconController.addIconGroup(this.mIconManager);
        requestApplyInsets();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onAttachedToWindow$0 */
    public /* synthetic */ void lambda$onAttachedToWindow$0$QuickStatusBarHeader(Integer num) {
        this.mRingerMode = num.intValue();
        updateStatusText();
    }

    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        int i;
        DisplayCutout displayCutout = windowInsets.getDisplayCutout();
        Pair<Integer, Integer> paddingNeededForCutoutAndRoundedCorner = StatusBarWindowView.paddingNeededForCutoutAndRoundedCorner(displayCutout, StatusBarWindowView.cornerCutoutMargins(displayCutout, getDisplay()), -1);
        this.mCutOutPaddingLeft = ((Integer) paddingNeededForCutoutAndRoundedCorner.first).intValue();
        this.mCutOutPaddingRight = ((Integer) paddingNeededForCutoutAndRoundedCorner.second).intValue();
        if (displayCutout == null) {
            i = 0;
        } else {
            i = displayCutout.getWaterfallInsets().top;
        }
        this.mWaterfallTopInset = i;
        updateClockPadding();
        return super.onApplyWindowInsets(windowInsets);
    }

    private void updateClockPadding() {
        int i;
        int i2;
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
        int i3 = layoutParams.leftMargin;
        int i4 = layoutParams.rightMargin;
        int i5 = this.mCutOutPaddingLeft;
        if (i5 > 0) {
            i = Math.max((Math.max(i5, this.mRoundedCornerPadding) - (isLayoutRtl() ? this.mContentMarginEnd : this.mContentMarginStart)) - i3, 0);
        } else {
            i = 0;
        }
        int i6 = this.mCutOutPaddingRight;
        if (i6 > 0) {
            i2 = Math.max((Math.max(i6, this.mRoundedCornerPadding) - (isLayoutRtl() ? this.mContentMarginStart : this.mContentMarginEnd)) - i4, 0);
        } else {
            i2 = 0;
        }
        this.mSystemIconsView.setPadding(i, this.mWaterfallTopInset + this.mStatusBarPaddingTop, i2, 0);
    }

    public void onDetachedFromWindow() {
        setListening(false);
        this.mRingerModeTracker.getRingerModeInternal().removeObservers(this);
        this.mStatusBarIconController.removeIconGroup(this.mIconManager);
        super.onDetachedFromWindow();
    }

    public void setListening(boolean z) {
        if (z != this.mListening) {
            this.mListening = z;
            if (z) {
                this.mZenController.addCallback(this);
                this.mAlarmController.addCallback(this);
                this.mLifecycle.setCurrentState(Lifecycle.State.RESUMED);
                return;
            }
            this.mZenController.removeCallback(this);
            this.mAlarmController.removeCallback(this);
            this.mLifecycle.setCurrentState(Lifecycle.State.CREATED);
        }
    }

    public void onClick(View view) {
        if (view == this.mClockView) {
            this.mActivityStarter.postStartActivityDismissingKeyguard(new Intent("android.intent.action.SHOW_ALARMS"), 0);
            return;
        }
        View view2 = this.mNextAlarmContainer;
        if (view != view2 || !view2.isVisibleToUser()) {
            View view3 = this.mRingerContainer;
            if (view == view3 && view3.isVisibleToUser()) {
                this.mActivityStarter.postStartActivityDismissingKeyguard(new Intent("android.settings.SOUND_SETTINGS"), 0);
            }
        } else if (this.mNextAlarm.getShowIntent() != null) {
            this.mActivityStarter.postStartActivityDismissingKeyguard(this.mNextAlarm.getShowIntent());
        } else {
            Log.d("QuickStatusBarHeader", "No PendingIntent for next alarm. Using default intent");
            this.mActivityStarter.postStartActivityDismissingKeyguard(new Intent("android.intent.action.SHOW_ALARMS"), 0);
        }
    }

    public void onNextAlarmChanged(AlarmManager.AlarmClockInfo alarmClockInfo) {
        this.mNextAlarm = alarmClockInfo;
        updateStatusText();
    }

    public void onConfigChanged(ZenModeConfig zenModeConfig) {
        updateStatusText();
    }

    private String formatNextAlarm(AlarmManager.AlarmClockInfo alarmClockInfo) {
        if (alarmClockInfo == null) {
            return "";
        }
        return DateFormat.format(DateFormat.getBestDateTimePattern(Locale.getDefault(), DateFormat.is24HourFormat(this.mContext, ActivityManager.getCurrentUser()) ? "EHm" : "Ehma"), alarmClockInfo.getTriggerTime()).toString();
    }

    public Lifecycle getLifecycle() {
        return this.mLifecycle;
    }
}
