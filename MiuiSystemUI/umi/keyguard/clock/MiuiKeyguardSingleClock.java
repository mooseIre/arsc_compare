package com.android.keyguard.clock;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.security.MiuiLockPatternUtils;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.keyguard.CarrierText;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.magazine.LockScreenMagazineClockView;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.policy.RegionController;
import java.util.Locale;
import java.util.TimeZone;
import miui.date.Calendar;
import miui.keyguard.clock.MiuiBaseClock;

public class MiuiKeyguardSingleClock extends MiuiKeyguardBaseClock implements RegionController.Callback {
    protected Calendar mCalendar;
    protected FrameLayout mClockContainer;
    protected LinearLayout mClockExtraInfo;
    protected final Handler mHandler = new Handler() {
        /* class com.android.keyguard.clock.MiuiKeyguardSingleClock.AnonymousClass1 */

        public void handleMessage(Message message) {
            if (message.what == 0) {
                MiuiKeyguardSingleClock.this.updateClockView();
            }
        }
    };
    protected boolean mHasNotification = false;
    protected String mLastOwnerInfoString = null;
    protected boolean mLeftHoleDevice;
    protected MiuiLockPatternUtils mLockPatternUtils;
    ContentObserver mLunarCalendarObserver = new ContentObserver(new Handler()) {
        /* class com.android.keyguard.clock.MiuiKeyguardSingleClock.AnonymousClass3 */

        public void onChange(boolean z) {
            super.onChange(z);
            MiuiKeyguardSingleClock miuiKeyguardSingleClock = MiuiKeyguardSingleClock.this;
            boolean z2 = false;
            if (Settings.System.getIntForUser(miuiKeyguardSingleClock.mContext.getContentResolver(), "show_lunar_calendar", 0, MiuiKeyguardSingleClock.this.mUserId) == 1) {
                z2 = true;
            }
            miuiKeyguardSingleClock.mShowLunarCalendar = z2;
            MiuiKeyguardSingleClock.this.updateLunarCalendarInfo();
        }
    };
    protected LockScreenMagazineClockView mMagazineClockView;
    protected MiuiBaseClock mMiuiBaseClock;
    protected boolean mOldHasNotification = false;
    protected TextView mOwnerInfo;
    protected String mOwnerInfoString = null;
    protected int mSelectedClockPosition;
    private boolean mShowCarrier;
    private ContentObserver mShowCarrierObserver = new ContentObserver((Handler) Dependency.get(Dependency.MAIN_HANDLER)) {
        /* class com.android.keyguard.clock.MiuiKeyguardSingleClock.AnonymousClass2 */

        public void onChange(boolean z) {
            super.onChange(z);
            MiuiKeyguardSingleClock miuiKeyguardSingleClock = MiuiKeyguardSingleClock.this;
            boolean z2 = true;
            if (Settings.System.getIntForUser(miuiKeyguardSingleClock.mContext.getContentResolver(), "status_bar_show_carrier_under_keyguard", 1, -2) != 1) {
                z2 = false;
            }
            miuiKeyguardSingleClock.mShowCarrier = z2;
            MiuiKeyguardSingleClock.this.updateSimCardInfoVisibility();
        }
    };
    private boolean mShowCarrierUnderLeftHoleKeyguard;
    protected boolean mShowLunarCalendar = false;
    protected boolean mShowOwnerInfo = false;
    protected CarrierText mSimCardInfo;
    protected AnimatorSet mSwitchAnimationSet = new AnimatorSet();
    protected boolean mTWRegion;
    private int mToState;

    /* access modifiers changed from: protected */
    public boolean shouldShowSwitchAnim() {
        return true;
    }

    /* access modifiers changed from: protected */
    public void toNormalStateAnimInUpdate(float f) {
    }

    /* access modifiers changed from: protected */
    public void toNormalStateAnimOutEnd() {
    }

    /* access modifiers changed from: protected */
    public void toNormalStateAnimOutUpdate(float f) {
    }

    /* access modifiers changed from: protected */
    public void toNotificationStateAnimInUpdate(float f) {
    }

    /* access modifiers changed from: protected */
    public void toNotificationStateAnimOutEnd() {
    }

    /* access modifiers changed from: protected */
    public void toNotificationStateAnimOutUpdate(float f) {
    }

    public MiuiKeyguardSingleClock(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mResources = this.mContext.getResources();
        this.mUserId = KeyguardUpdateMonitor.getCurrentUser();
        this.mLockPatternUtils = new MiuiLockPatternUtils(this.mContext);
        this.mSelectedClockPosition = Settings.System.getIntForUser(this.mContext.getContentResolver(), "selected_keyguard_clock_position", MiuiKeyguardUtils.getDefaultKeyguardClockPosition(this.mContext), this.mUserId);
        View inflate = this.mLayoutInflater.inflate(C0017R$layout.keyguard_base_clock_layout, (ViewGroup) this, false);
        this.mClockContainer = (FrameLayout) inflate.findViewById(C0015R$id.clock_container1);
        this.mClockExtraInfo = (LinearLayout) inflate.findViewById(C0015R$id.miui_keyguard_clock_extra_info);
        this.mSimCardInfo = (CarrierText) inflate.findViewById(C0015R$id.unlock_screen_sim_info);
        this.mLeftHoleDevice = this.mResources.getBoolean(C0010R$bool.left_hole_device);
        this.mShowCarrierUnderLeftHoleKeyguard = this.mResources.getBoolean(C0010R$bool.show_carrier_under_left_hole_keyguard);
        this.mOwnerInfo = (TextView) inflate.findViewById(C0015R$id.unlock_screen_owner_info);
        this.mMagazineClockView = (LockScreenMagazineClockView) inflate.findViewById(C0015R$id.unlock_screen_lock_screen_magazine_info);
        addView(inflate);
        this.mCalendar = new Calendar();
        updateOwnerInfo();
        this.mLunarCalendarObserver.onChange(false);
        initAnimators();
    }

    private void initAnimators() {
        if (shouldShowSwitchAnim()) {
            ValueAnimator ofFloat = ValueAnimator.ofFloat(1.0f, 0.0f);
            ofFloat.setDuration(250L);
            ofFloat.addListener(new AnimatorListenerAdapter() {
                /* class com.android.keyguard.clock.MiuiKeyguardSingleClock.AnonymousClass4 */

                public void onAnimationEnd(Animator animator) {
                    if (MiuiKeyguardSingleClock.this.mToState == 1) {
                        MiuiKeyguardSingleClock.this.mOwnerInfo.setVisibility(8);
                        MiuiKeyguardSingleClock.this.toNotificationStateAnimOutEnd();
                    } else if (MiuiKeyguardSingleClock.this.mToState == 0) {
                        MiuiKeyguardSingleClock.this.mOwnerInfo.setAlpha(0.0f);
                        MiuiKeyguardSingleClock.this.updateOwnerInfo();
                        MiuiKeyguardSingleClock.this.toNormalStateAnimOutEnd();
                    }
                }
            });
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class com.android.keyguard.clock.$$Lambda$MiuiKeyguardSingleClock$YiwRgDSUp8BBLVHQmgcGLQpNWGc */

                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    MiuiKeyguardSingleClock.this.lambda$initAnimators$0$MiuiKeyguardSingleClock(valueAnimator);
                }
            });
            ValueAnimator ofFloat2 = ValueAnimator.ofFloat(0.0f, 1.0f);
            ofFloat2.setDuration(250L);
            ofFloat2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class com.android.keyguard.clock.$$Lambda$MiuiKeyguardSingleClock$_DmwqQoVvhSiFR_KSrCLMC95TjQ */

                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    MiuiKeyguardSingleClock.this.lambda$initAnimators$1$MiuiKeyguardSingleClock(valueAnimator);
                }
            });
            this.mSwitchAnimationSet.play(ofFloat2).after(ofFloat);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$initAnimators$0 */
    public /* synthetic */ void lambda$initAnimators$0$MiuiKeyguardSingleClock(ValueAnimator valueAnimator) {
        float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        int i = this.mToState;
        if (i == 1) {
            this.mSimCardInfo.setAlpha(floatValue);
            this.mOwnerInfo.setAlpha(floatValue);
            this.mMagazineClockView.setAlpha(floatValue);
            toNotificationStateAnimOutUpdate(floatValue);
        } else if (i == 0) {
            this.mSimCardInfo.setAlpha(floatValue);
            this.mMagazineClockView.setAlpha(floatValue);
            toNormalStateAnimOutUpdate(floatValue);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$initAnimators$1 */
    public /* synthetic */ void lambda$initAnimators$1$MiuiKeyguardSingleClock(ValueAnimator valueAnimator) {
        float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        int i = this.mToState;
        if (i == 1) {
            this.mSimCardInfo.setAlpha(floatValue);
            this.mMagazineClockView.setAlpha(floatValue);
            toNotificationStateAnimInUpdate(floatValue);
        } else if (i == 0) {
            this.mSimCardInfo.setAlpha(floatValue);
            this.mOwnerInfo.setAlpha(floatValue);
            this.mMagazineClockView.setAlpha(floatValue);
            toNormalStateAnimInUpdate(floatValue);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.clock.MiuiKeyguardBaseClock
    public void updateLunarCalendarInfo() {
        if (this.mMiuiBaseClock != null) {
            this.mMiuiBaseClock.setShowLunarCalendar(this.mShowLunarCalendar && Locale.CHINESE.getLanguage().equals(this.mLanguage));
        }
    }

    public void updateOwnerInfo() {
        if (this.mOwnerInfo != null) {
            if (this.mLockPatternUtils.isDeviceOwnerInfoEnabled()) {
                this.mOwnerInfoString = this.mLockPatternUtils.getDeviceOwnerInfo();
            } else if (this.mLockPatternUtils.isOwnerInfoEnabled(this.mUserId)) {
                this.mOwnerInfoString = this.mLockPatternUtils.getOwnerInfo(this.mUserId);
            } else {
                this.mOwnerInfoString = null;
            }
            this.mShowOwnerInfo = !TextUtils.isEmpty(this.mOwnerInfoString);
            if (TextUtils.isEmpty(this.mOwnerInfoString) || this.mHasNotification) {
                this.mOwnerInfo.setVisibility(8);
                return;
            }
            this.mOwnerInfo.setVisibility(0);
            if (!this.mOwnerInfoString.equals(this.mLastOwnerInfoString)) {
                String str = this.mOwnerInfoString;
                this.mLastOwnerInfoString = str;
                this.mOwnerInfo.setText(str);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.clock.MiuiKeyguardBaseClock
    public void onClockShowing() {
        updateTime();
        updateOwnerInfo();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.clock.MiuiKeyguardBaseClock
    public void onUserSwitch() {
        updateOwnerInfo();
        this.mLunarCalendarObserver.onChange(false);
    }

    @Override // com.android.keyguard.clock.MiuiKeyguardBaseClock
    public void updateClockMagazineInfo() {
        this.mMagazineClockView.updateInfo();
    }

    @Override // com.android.keyguard.clock.MiuiKeyguardBaseClock
    public void setSelectedClockPosition(int i) {
        this.mSelectedClockPosition = i;
        updateClockView();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.clock.MiuiKeyguardBaseClock
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("show_lunar_calendar"), false, this.mLunarCalendarObserver, -1);
        updateClockMagazineInfo();
        ((RegionController) Dependency.get(RegionController.class)).addCallback(this);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("status_bar_show_carrier_under_keyguard"), false, this.mShowCarrierObserver, -1);
        this.mShowCarrierObserver.onChange(false);
    }

    @Override // com.android.keyguard.clock.MiuiKeyguardBaseClock
    public void setDarkStyle(boolean z) {
        if (z != this.mDarkStyle) {
            super.setDarkStyle(z);
            updateDrawableResources();
            setInfoDarkMode();
        }
    }

    private void setInfoDarkMode() {
        int i;
        this.mSimCardInfo.setTextColor(this.mDarkStyle ? getContext().getResources().getColor(C0011R$color.miui_common_unlock_screen_common_time_dark_text_color) : -1);
        if (this.mDarkStyle) {
            i = getContext().getResources().getColor(C0011R$color.miui_owner_info_dark_text_color);
        } else {
            i = getContext().getResources().getColor(C0011R$color.miui_owner_info_light_text_color);
        }
        this.mOwnerInfo.setTextColor(i);
        this.mMagazineClockView.setTextColor(i);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.clock.MiuiKeyguardBaseClock
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mContext.getContentResolver().unregisterContentObserver(this.mShowCarrierObserver);
        ((RegionController) Dependency.get(RegionController.class)).removeCallback(this);
        this.mContext.getContentResolver().unregisterContentObserver(this.mLunarCalendarObserver);
    }

    @Override // com.android.keyguard.clock.MiuiKeyguardBaseClock
    public void updateTimeZone(String str) {
        if (!TextUtils.isEmpty(str)) {
            this.mCalendar = new Calendar(TimeZone.getTimeZone(str));
            updateTime();
            MiuiBaseClock miuiBaseClock = this.mMiuiBaseClock;
            if (miuiBaseClock != null) {
                miuiBaseClock.updateTimeZone(str);
            }
        }
    }

    @Override // com.android.keyguard.clock.MiuiKeyguardBaseClock
    public void updateHourFormat() {
        super.updateHourFormat();
        MiuiBaseClock miuiBaseClock = this.mMiuiBaseClock;
        if (miuiBaseClock != null) {
            miuiBaseClock.setIs24HourFormat(this.m24HourFormat);
        }
    }

    @Override // com.android.keyguard.clock.MiuiKeyguardBaseClock
    public void updateTime() {
        this.mCalendar.setTimeInMillis(System.currentTimeMillis());
        MiuiBaseClock miuiBaseClock = this.mMiuiBaseClock;
        if (miuiBaseClock != null) {
            miuiBaseClock.updateTime();
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.clock.MiuiKeyguardBaseClock
    public void updateDrawableResources() {
        this.mMagazineClockView.updateDrawableResources(this.mDarkStyle);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.clock.MiuiKeyguardBaseClock
    public void updateViewsTextSize() {
        float dimensionPixelSize = (float) this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.miui_clock_date_text_size);
        this.mOwnerInfo.setTextSize(0, dimensionPixelSize);
        this.mSimCardInfo.setTextSize(0, dimensionPixelSize);
        this.mMagazineClockView.setTextSize();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.clock.MiuiKeyguardBaseClock
    public void updateViewsLayoutParams() {
        int i;
        int i2;
        int i3;
        int i4 = this.mSelectedClockPosition;
        boolean z = true;
        int i5 = 0;
        if (i4 != 1 && !MiuiKeyguardUtils.isSupportVerticalClock(i4, this.mContext)) {
            z = false;
        }
        LinearLayout linearLayout = this.mClockExtraInfo;
        int i6 = 17;
        if (linearLayout != null) {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) linearLayout.getLayoutParams();
            layoutParams.gravity = z ? 17 : 8388611;
            if (z) {
                i3 = 0;
            } else {
                i3 = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.miui_left_top_clock_margin_left);
            }
            layoutParams.setMarginStart(i3);
            this.mClockExtraInfo.setLayoutParams(layoutParams);
        }
        TextView textView = this.mOwnerInfo;
        if (textView != null) {
            LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) textView.getLayoutParams();
            layoutParams2.gravity = z ? 17 : 8388611;
            if (z) {
                i2 = 0;
            } else {
                i2 = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.left_top_clock_date_margin_extra);
            }
            layoutParams2.setMarginStart(i2);
            this.mOwnerInfo.setLayoutParams(layoutParams2);
            this.mOwnerInfo.setGravity(z ? 17 : 8388611);
        }
        CarrierText carrierText = this.mSimCardInfo;
        if (carrierText != null) {
            LinearLayout.LayoutParams layoutParams3 = (LinearLayout.LayoutParams) carrierText.getLayoutParams();
            layoutParams3.gravity = z ? 17 : 8388611;
            if (z) {
                i = 0;
            } else {
                i = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.left_top_clock_date_margin_extra);
            }
            layoutParams3.setMarginStart(i);
            this.mSimCardInfo.setLayoutParams(layoutParams3);
            this.mSimCardInfo.setTextAlignment(z ? 4 : 5);
        }
        LockScreenMagazineClockView lockScreenMagazineClockView = this.mMagazineClockView;
        if (lockScreenMagazineClockView != null) {
            LinearLayout.LayoutParams layoutParams4 = (LinearLayout.LayoutParams) lockScreenMagazineClockView.getLayoutParams();
            layoutParams4.gravity = z ? 17 : 8388611;
            if (!z) {
                i5 = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.left_top_clock_date_margin_extra);
            }
            layoutParams4.setMarginStart(i5);
            this.mMagazineClockView.setLayoutParams(layoutParams4);
            LockScreenMagazineClockView lockScreenMagazineClockView2 = this.mMagazineClockView;
            if (!z) {
                i6 = 8388611;
            }
            lockScreenMagazineClockView2.setGravity(i6);
        }
    }

    @Override // com.android.keyguard.clock.MiuiKeyguardBaseClock
    public void updateClockView(boolean z) {
        if (!this.mHasNotification || z) {
            this.mHasNotification = z;
            this.mHandler.removeMessages(0);
            updateClockView();
            return;
        }
        this.mHasNotification = false;
        this.mHandler.sendEmptyMessageDelayed(0, 200);
    }

    public void updateClockView() {
        boolean z = this.mHasNotification;
        if (z != this.mOldHasNotification) {
            if (z) {
                switchToNotificationState();
            } else {
                switchToNormalState();
            }
            this.mOldHasNotification = this.mHasNotification;
        }
    }

    /* access modifiers changed from: protected */
    public void switchToNotificationState() {
        if (shouldShowSwitchAnim()) {
            this.mToState = 1;
            this.mSwitchAnimationSet.cancel();
            this.mSwitchAnimationSet.start();
        }
    }

    /* access modifiers changed from: protected */
    public void switchToNormalState() {
        if (shouldShowSwitchAnim()) {
            this.mToState = 0;
            this.mSwitchAnimationSet.cancel();
            this.mSwitchAnimationSet.start();
        }
    }

    @Override // com.android.systemui.statusbar.policy.RegionController.Callback
    public void onRegionChanged(String str) {
        this.mTWRegion = "TW".equals(str);
        updateSimCardInfoVisibility();
    }

    /* access modifiers changed from: protected */
    public void updateSimCardInfoVisibility() {
        this.mSimCardInfo.setVisibility((!this.mShowCarrier || !this.mLeftHoleDevice || this.mTWRegion || this.mShowCarrierUnderLeftHoleKeyguard) ? 8 : 0);
    }
}
