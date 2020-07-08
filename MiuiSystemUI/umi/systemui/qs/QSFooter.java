package com.android.systemui.qs;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Dependency;
import com.android.systemui.FontUtils;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.R;
import com.android.systemui.proxy.UserManager;
import com.android.systemui.qs.TouchAnimator;
import com.android.systemui.statusbar.phone.ExpandableIndicator;
import com.android.systemui.statusbar.phone.MultiUserSwitch;
import com.android.systemui.statusbar.phone.SettingsButton;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.statusbar.policy.UserInfoController;
import java.util.List;
import miui.telephony.SubscriptionInfo;

public class QSFooter extends FrameLayout implements View.OnClickListener, UserInfoController.OnUserInfoChangedListener, NetworkController.EmergencyListener, NetworkController.SignalCallback {
    private ActivityStarter mActivityStarter;
    private TouchAnimator mAlarmAnimator;
    private boolean mAlarmShowing;
    /* access modifiers changed from: private */
    public TextView mAlarmStatus;
    private View mAlarmStatusCollapsed;
    private boolean mAlwaysShowMultiUserSwitch;
    private TouchAnimator mAnimator;
    /* access modifiers changed from: private */
    public View mDate;
    private View mDateTimeGroup;
    protected View mEdit;
    protected ExpandableIndicator mExpandIndicator;
    private boolean mExpanded;
    private float mExpansionAmount;
    private boolean mKeyguardShowing;
    private boolean mListening;
    private ImageView mMultiUserAvatar;
    protected MultiUserSwitch mMultiUserSwitch;
    private AlarmManager.AlarmClockInfo mNextAlarm;
    /* access modifiers changed from: private */
    public QSPanel mQsPanel;
    protected TouchAnimator mSettingsAlpha;
    private SettingsButton mSettingsButton;
    protected View mSettingsContainer;
    private boolean mShowEditIcon;
    private boolean mShowEmergencyCallsOnly;
    private UserInfoController mUserInfoController;

    public void onUserInfoChanged(String str, Drawable drawable, String str2) {
    }

    public void setEthernetIndicators(NetworkController.IconState iconState) {
    }

    public void setIsAirplaneMode(NetworkController.IconState iconState) {
    }

    public void setIsDefaultDataSim(int i, boolean z) {
    }

    public void setIsImsRegisted(int i, boolean z) {
    }

    public void setMobileDataEnabled(boolean z) {
    }

    public void setMobileDataIndicators(NetworkController.IconState iconState, NetworkController.IconState iconState2, int i, int i2, boolean z, boolean z2, int i3, int i4, int i5, String str, String str2, boolean z3, int i6, boolean z4) {
    }

    public void setNetworkNameVoice(int i, String str) {
    }

    public void setNoSims(boolean z) {
    }

    public void setSlaveWifiIndicators(boolean z, NetworkController.IconState iconState, NetworkController.IconState iconState2) {
    }

    public void setSpeechHd(int i, boolean z) {
    }

    public void setSubs(List<SubscriptionInfo> list) {
    }

    public void setVolteNoService(int i, boolean z) {
    }

    public void setVowifi(int i, boolean z) {
    }

    public void setWifiIndicators(boolean z, NetworkController.IconState iconState, NetworkController.IconState iconState2, boolean z2, boolean z3, String str, boolean z4) {
    }

    public QSFooter(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        Resources resources = getResources();
        this.mShowEditIcon = resources.getBoolean(R.bool.config_showQuickSettingsEditingIcon);
        View findViewById = findViewById(16908291);
        this.mEdit = findViewById;
        int i = 0;
        findViewById.setVisibility(this.mShowEditIcon ? 0 : 8);
        if (this.mShowEditIcon) {
            findViewById(16908291).setOnClickListener(new View.OnClickListener() {
                public void onClick(final View view) {
                    ((ActivityStarter) Dependency.get(ActivityStarter.class)).postQSRunnableDismissingKeyguard(new Runnable() {
                        public void run() {
                            QSFooter.this.mQsPanel.showEdit(view);
                        }
                    });
                }
            });
        }
        this.mDateTimeGroup = findViewById(R.id.date_time_alarm_group);
        this.mDate = findViewById(R.id.date);
        ExpandableIndicator expandableIndicator = (ExpandableIndicator) findViewById(R.id.expand_indicator);
        this.mExpandIndicator = expandableIndicator;
        if (!resources.getBoolean(R.bool.config_showQuickSettingsExpandIndicator)) {
            i = 8;
        }
        expandableIndicator.setVisibility(i);
        this.mSettingsButton = (SettingsButton) findViewById(R.id.settings_button);
        this.mSettingsContainer = findViewById(R.id.settings_button_container);
        this.mSettingsButton.setOnClickListener(this);
        this.mAlarmStatusCollapsed = findViewById(R.id.alarm_status_collapsed);
        this.mAlarmStatus = (TextView) findViewById(R.id.alarm_status);
        this.mDateTimeGroup.setOnClickListener(this);
        MultiUserSwitch multiUserSwitch = (MultiUserSwitch) findViewById(R.id.multi_user_switch);
        this.mMultiUserSwitch = multiUserSwitch;
        this.mMultiUserAvatar = (ImageView) multiUserSwitch.findViewById(R.id.multi_user_avatar);
        this.mAlwaysShowMultiUserSwitch = resources.getBoolean(R.bool.config_alwaysShowMultiUserSwitcher);
        ((RippleDrawable) this.mSettingsButton.getBackground()).setForceSoftware(true);
        ((RippleDrawable) this.mExpandIndicator.getBackground()).setForceSoftware(true);
        updateResources();
        NextAlarmController nextAlarmController = (NextAlarmController) Dependency.get(NextAlarmController.class);
        this.mUserInfoController = (UserInfoController) Dependency.get(UserInfoController.class);
        this.mActivityStarter = (ActivityStarter) Dependency.get(ActivityStarter.class);
        addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                QSFooter.this.updateAnimator(i3 - i);
            }
        });
    }

    /* access modifiers changed from: private */
    public void updateAnimator(int i) {
        int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_quick_tile_icon_bg_size) - this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_quick_tile_padding);
        int integer = getResources().getInteger(R.integer.quick_settings_qqs_count);
        int i2 = (i - (dimensionPixelSize * integer)) / (integer - 1);
        int dimensionPixelOffset = this.mContext.getResources().getDimensionPixelOffset(R.dimen.default_gear_space);
        TouchAnimator.Builder builder = new TouchAnimator.Builder();
        builder.addFloat(this.mSettingsContainer, "translationX", (float) (-(i2 - dimensionPixelOffset)), 0.0f);
        builder.addFloat(this.mSettingsButton, "rotation", -120.0f, 0.0f);
        this.mAnimator = builder.build();
        if (this.mAlarmShowing) {
            TouchAnimator.Builder builder2 = new TouchAnimator.Builder();
            builder2.addFloat(this.mDate, "alpha", 1.0f, 0.0f);
            builder2.addFloat(this.mDateTimeGroup, "translationX", 0.0f, (float) (-this.mDate.getWidth()));
            builder2.addFloat(this.mAlarmStatus, "alpha", 0.0f, 1.0f);
            builder2.setListener(new TouchAnimator.ListenerAdapter() {
                public void onAnimationAtStart() {
                    QSFooter.this.mAlarmStatus.setVisibility(8);
                }

                public void onAnimationStarted() {
                    QSFooter.this.mAlarmStatus.setVisibility(0);
                }
            });
            this.mAlarmAnimator = builder2.build();
        } else {
            this.mAlarmAnimator = null;
            this.mAlarmStatus.setVisibility(8);
            this.mDate.setAlpha(1.0f);
            this.mDateTimeGroup.setTranslationX(0.0f);
        }
        setExpansion(this.mExpansionAmount);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateResources();
    }

    public void onRtlPropertiesChanged(int i) {
        super.onRtlPropertiesChanged(i);
        updateResources();
    }

    private void updateResources() {
        FontUtils.updateFontSize(this.mAlarmStatus, R.dimen.qs_date_collapsed_size);
        updateSettingsAnimator();
    }

    private void updateSettingsAnimator() {
        this.mSettingsAlpha = createSettingsAlphaAnimator();
        boolean isLayoutRtl = isLayoutRtl();
        if (!isLayoutRtl || this.mDate.getWidth() != 0) {
            View view = this.mDate;
            view.setPivotX(isLayoutRtl ? (float) view.getWidth() : 0.0f);
            return;
        }
        this.mDate.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                QSFooter.this.mDate.setPivotX((float) QSFooter.this.getWidth());
                QSFooter.this.mDate.removeOnLayoutChangeListener(this);
            }
        });
    }

    private TouchAnimator createSettingsAlphaAnimator() {
        if (!this.mShowEditIcon && this.mAlwaysShowMultiUserSwitch) {
            return null;
        }
        TouchAnimator.Builder builder = new TouchAnimator.Builder();
        builder.setStartDelay(0.5f);
        if (this.mShowEditIcon) {
            builder.addFloat(this.mEdit, "alpha", 0.0f, 1.0f);
        }
        if (!this.mAlwaysShowMultiUserSwitch) {
            builder.addFloat(this.mMultiUserSwitch, "alpha", 0.0f, 1.0f);
        }
        return builder.build();
    }

    public void setKeyguardShowing(boolean z) {
        this.mKeyguardShowing = z;
        setExpansion(this.mExpansionAmount);
    }

    public void setExpanded(boolean z) {
        if (this.mExpanded != z) {
            this.mExpanded = z;
            updateEverything();
        }
    }

    public void setExpansion(float f) {
        this.mExpansionAmount = f;
        TouchAnimator touchAnimator = this.mAnimator;
        if (touchAnimator != null) {
            touchAnimator.setPosition(f);
        }
        TouchAnimator touchAnimator2 = this.mAlarmAnimator;
        if (touchAnimator2 != null) {
            touchAnimator2.setPosition(this.mKeyguardShowing ? 0.0f : f);
        }
        TouchAnimator touchAnimator3 = this.mSettingsAlpha;
        if (touchAnimator3 != null) {
            touchAnimator3.setPosition(f);
        }
        updateAlarmVisibilities();
        this.mExpandIndicator.setExpanded(f > 0.93f);
    }

    public void onDetachedFromWindow() {
        setListening(false);
        super.onDetachedFromWindow();
    }

    private void updateAlarmVisibilities() {
        this.mAlarmStatusCollapsed.setVisibility(this.mAlarmShowing ? 0 : 8);
    }

    public void setListening(boolean z) {
        if (z != this.mListening) {
            this.mListening = z;
            updateListeners();
        }
    }

    public View getExpandView() {
        return findViewById(R.id.expand_indicator);
    }

    public void updateEverything() {
        post(new Runnable() {
            public void run() {
                QSFooter.this.updateVisibilities();
                QSFooter.this.setClickable(false);
            }
        });
    }

    /* access modifiers changed from: private */
    public void updateVisibilities() {
        updateAlarmVisibilities();
        int i = 4;
        this.mSettingsContainer.findViewById(R.id.tuner_icon).setVisibility(4);
        boolean isDeviceInDemoMode = UserManager.isDeviceInDemoMode(this.mContext);
        this.mMultiUserSwitch.setVisibility(((this.mExpanded || this.mAlwaysShowMultiUserSwitch) && this.mMultiUserSwitch.hasMultipleUsers() && !isDeviceInDemoMode) ? 0 : 4);
        if (this.mShowEditIcon) {
            View view = this.mEdit;
            if (!isDeviceInDemoMode && this.mExpanded) {
                i = 0;
            }
            view.setVisibility(i);
        }
    }

    private void updateListeners() {
        Class cls = NetworkController.class;
        if (this.mListening) {
            this.mUserInfoController.addCallback(this);
            if (((NetworkController) Dependency.get(cls)).hasVoiceCallingFeature()) {
                ((NetworkController) Dependency.get(cls)).addEmergencyListener(this);
                ((NetworkController) Dependency.get(cls)).addCallback((NetworkController.SignalCallback) this);
                return;
            }
            return;
        }
        this.mUserInfoController.removeCallback(this);
        ((NetworkController) Dependency.get(cls)).removeEmergencyListener(this);
        ((NetworkController) Dependency.get(cls)).removeCallback((NetworkController.SignalCallback) this);
    }

    public void setQSPanel(QSPanel qSPanel) {
        this.mQsPanel = qSPanel;
        if (qSPanel != null) {
            this.mMultiUserSwitch.setQsPanel(qSPanel);
        }
    }

    public void onClick(View view) {
        if (view == this.mSettingsButton) {
            if (!((DeviceProvisionedController) Dependency.get(DeviceProvisionedController.class)).isCurrentUserSetup()) {
                this.mActivityStarter.postQSRunnableDismissingKeyguard(new Runnable() {
                    public void run() {
                    }
                });
                return;
            }
            MetricsLogger.action(this.mContext, this.mExpanded ? 406 : 490);
            if (!this.mSettingsButton.isTunerClick()) {
                startSettingsActivity();
            }
        } else if (view == this.mDateTimeGroup) {
            MetricsLogger.action(this.mContext, 930, this.mNextAlarm != null);
            AlarmManager.AlarmClockInfo alarmClockInfo = this.mNextAlarm;
            if (alarmClockInfo != null) {
                this.mActivityStarter.startPendingIntentDismissingKeyguard(alarmClockInfo.getShowIntent());
            } else {
                this.mActivityStarter.postStartActivityDismissingKeyguard(new Intent("android.intent.action.SHOW_ALARMS"), 0);
            }
        }
    }

    private void startSettingsActivity() {
        this.mActivityStarter.startActivity(new Intent("android.settings.SETTINGS"), true);
    }

    public void setEmergencyCallsOnly(boolean z) {
        if (z != this.mShowEmergencyCallsOnly) {
            this.mShowEmergencyCallsOnly = z;
            if (this.mExpanded) {
                updateEverything();
            }
        }
    }
}
