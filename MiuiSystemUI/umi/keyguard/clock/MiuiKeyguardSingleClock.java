package com.android.keyguard.clock;

import android.animation.Animator;
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
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.keyguard.CarrierText;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.MiuiKeyguardUtils;
import com.android.keyguard.magazine.LockScreenMagazineClockView;
import com.android.systemui.DisplayCutoutCompat;
import com.android.systemui.plugins.R;
import java.util.Locale;
import java.util.TimeZone;
import miui.date.Calendar;
import miui.keyguard.clock.MiuiBaseClock;

public class MiuiKeyguardSingleClock extends MiuiKeyguardBaseClock {
    protected AnimatorSet mAnimToNormalState = new AnimatorSet();
    protected AnimatorSet mAnimToNotificationState = new AnimatorSet();
    protected Calendar mCalendar;
    protected FrameLayout mClockContainer;
    protected LinearLayout mClockExtraInfo;
    protected final Handler mHandler = new Handler() {
        public void handleMessage(Message message) {
            if (message.what == 0) {
                MiuiKeyguardSingleClock.this.updateClockView();
            }
        }
    };
    protected boolean mHasNotification = false;
    protected String mLastOwnerInfoString = null;
    protected MiuiLockPatternUtils mLockPatternUtils;
    protected LockScreenMagazineClockView mLockScreenMagazineInfo;
    ContentObserver mLunarCalendarObserver = new ContentObserver(new Handler()) {
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
    protected MiuiBaseClock mMiuiBaseClock;
    protected boolean mOldHasNotification = false;
    protected TextView mOwnerInfo;
    protected String mOwnerInfoString = null;
    protected boolean mShowLunarCalendar = false;
    protected boolean mShowOwnerInfo = false;
    protected CarrierText mSimCardInfo;

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
        this.mContext.getResources();
        this.mUserId = KeyguardUpdateMonitor.getCurrentUser();
        this.mLockPatternUtils = new MiuiLockPatternUtils(this.mContext);
        this.mSelectedClockPosition = Settings.System.getIntForUser(this.mContext.getContentResolver(), "selected_keyguard_clock_position", MiuiKeyguardUtils.getDefaultKeyguardClockPosition(this.mContext), this.mUserId);
        View inflate = this.mLayoutInflater.inflate(R.layout.keyguard_base_clock_layout, this, false);
        this.mClockContainer = (FrameLayout) inflate.findViewById(R.id.clock_container1);
        this.mClockExtraInfo = (LinearLayout) inflate.findViewById(R.id.miui_keyguard_clock_extra_info);
        this.mSimCardInfo = (CarrierText) inflate.findViewById(R.id.unlock_screen_sim_info);
        Display defaultDisplay = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        DisplayInfo displayInfo = new DisplayInfo();
        defaultDisplay.getDisplayInfo(displayInfo);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        defaultDisplay.getRealMetrics(displayMetrics);
        if (!DisplayCutoutCompat.isCutoutLeftTop(displayInfo, displayMetrics.widthPixels)) {
            this.mSimCardInfo.setVisibility(8);
            this.mSimCardInfo.setShowStyle(-1);
        }
        this.mOwnerInfo = (TextView) inflate.findViewById(R.id.unlock_screen_owner_info);
        this.mLockScreenMagazineInfo = (LockScreenMagazineClockView) inflate.findViewById(R.id.unlock_screen_lock_screen_magazine_info);
        addView(inflate);
        this.mCalendar = new Calendar();
        updateOwnerInfo();
        this.mLunarCalendarObserver.onChange(false);
    }

    /* access modifiers changed from: protected */
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
    public void onClockShowing() {
        updateTime();
        updateOwnerInfo();
    }

    /* access modifiers changed from: protected */
    public void onUserSwitch() {
        updateOwnerInfo();
        this.mLunarCalendarObserver.onChange(false);
    }

    public void updateLockScreenMagazineInfo() {
        this.mLockScreenMagazineInfo.updateInfo();
    }

    public void setSelectedClockPosition(int i) {
        this.mSelectedClockPosition = i;
        updateClockView();
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("show_lunar_calendar"), false, this.mLunarCalendarObserver, -1);
        updateLockScreenMagazineInfo();
    }

    public void setDarkMode(boolean z) {
        super.setDarkMode(z);
        updateDrawableResources();
        setInfoDarkMode();
    }

    private void setInfoDarkMode() {
        int i;
        this.mSimCardInfo.setTextColor(this.mDarkMode ? getContext().getResources().getColor(R.color.miui_common_unlock_screen_common_time_dark_text_color) : -1);
        if (this.mDarkMode) {
            i = getContext().getResources().getColor(R.color.miui_owner_info_dark_text_color);
        } else {
            i = getContext().getResources().getColor(R.color.miui_owner_info_light_text_color);
        }
        this.mOwnerInfo.setTextColor(i);
        this.mLockScreenMagazineInfo.setTextColor(i);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mContext.getContentResolver().unregisterContentObserver(this.mLunarCalendarObserver);
    }

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

    public void updateHourFormat() {
        super.updateHourFormat();
        MiuiBaseClock miuiBaseClock = this.mMiuiBaseClock;
        if (miuiBaseClock != null) {
            miuiBaseClock.setIs24HourFormat(this.m24HourFormat);
        }
    }

    public void updateTime() {
        this.mCalendar.setTimeInMillis(System.currentTimeMillis());
        MiuiBaseClock miuiBaseClock = this.mMiuiBaseClock;
        if (miuiBaseClock != null) {
            miuiBaseClock.updateTime();
        }
    }

    /* access modifiers changed from: protected */
    public void updateDrawableResources() {
        this.mLockScreenMagazineInfo.updateDrawableResources(this.mDarkMode);
    }

    /* access modifiers changed from: protected */
    public void updateViewsTextSize() {
        float dimensionPixelSize = (float) this.mContext.getResources().getDimensionPixelSize(R.dimen.miui_clock_date_text_size);
        this.mOwnerInfo.setTextSize(0, dimensionPixelSize);
        this.mSimCardInfo.setTextSize(0, dimensionPixelSize);
        this.mLockScreenMagazineInfo.setTextSize();
    }

    /* access modifiers changed from: protected */
    public void updateViewsLayoutParams() {
        int i;
        int i2;
        int i3;
        int i4 = this.mSelectedClockPosition;
        boolean z = true;
        int i5 = 0;
        if (!(i4 == 1 || i4 == 3 || i4 == 0)) {
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
                i3 = this.mContext.getResources().getDimensionPixelSize(R.dimen.miui_left_top_clock_margin_left);
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
                i2 = this.mContext.getResources().getDimensionPixelSize(R.dimen.left_top_clock_date_margin_extra);
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
                i = this.mContext.getResources().getDimensionPixelSize(R.dimen.left_top_clock_date_margin_extra);
            }
            layoutParams3.setMarginStart(i);
            this.mSimCardInfo.setLayoutParams(layoutParams3);
            this.mSimCardInfo.setGravity(z ? 17 : 8388611);
        }
        LockScreenMagazineClockView lockScreenMagazineClockView = this.mLockScreenMagazineInfo;
        if (lockScreenMagazineClockView != null) {
            LinearLayout.LayoutParams layoutParams4 = (LinearLayout.LayoutParams) lockScreenMagazineClockView.getLayoutParams();
            layoutParams4.gravity = z ? 17 : 8388611;
            if (!z) {
                i5 = this.mContext.getResources().getDimensionPixelSize(R.dimen.left_top_clock_date_margin_extra);
            }
            layoutParams4.setMarginStart(i5);
            this.mLockScreenMagazineInfo.setLayoutParams(layoutParams4);
            LockScreenMagazineClockView lockScreenMagazineClockView2 = this.mLockScreenMagazineInfo;
            if (!z) {
                i6 = 8388611;
            }
            lockScreenMagazineClockView2.setGravity(i6);
        }
    }

    public void updateClockView(boolean z, boolean z2) {
        if (!z2) {
            return;
        }
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
            this.mAnimToNormalState.cancel();
            this.mAnimToNotificationState.cancel();
            ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{1.0f, 0.0f});
            ofFloat.setDuration(250);
            ofFloat.addListener(new Animator.AnimatorListener() {
                public void onAnimationCancel(Animator animator) {
                }

                public void onAnimationRepeat(Animator animator) {
                }

                public void onAnimationStart(Animator animator) {
                }

                public void onAnimationEnd(Animator animator) {
                    MiuiKeyguardSingleClock.this.mOwnerInfo.setVisibility(8);
                    MiuiKeyguardSingleClock.this.toNotificationStateAnimOutEnd();
                }
            });
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    MiuiKeyguardSingleClock.this.mSimCardInfo.setAlpha(floatValue);
                    MiuiKeyguardSingleClock.this.mOwnerInfo.setAlpha(floatValue);
                    MiuiKeyguardSingleClock.this.mLockScreenMagazineInfo.setAlpha(floatValue);
                    MiuiKeyguardSingleClock.this.toNotificationStateAnimOutUpdate(floatValue);
                }
            });
            ValueAnimator ofFloat2 = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
            ofFloat2.setDuration(250);
            ofFloat2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    MiuiKeyguardSingleClock.this.mSimCardInfo.setAlpha(floatValue);
                    MiuiKeyguardSingleClock.this.mLockScreenMagazineInfo.setAlpha(floatValue);
                    MiuiKeyguardSingleClock.this.toNotificationStateAnimInUpdate(floatValue);
                }
            });
            this.mAnimToNotificationState.play(ofFloat2).after(ofFloat);
            this.mAnimToNotificationState.start();
        }
    }

    /* access modifiers changed from: protected */
    public void switchToNormalState() {
        if (shouldShowSwitchAnim()) {
            this.mAnimToNormalState.cancel();
            this.mAnimToNotificationState.cancel();
            ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{1.0f, 0.0f});
            ofFloat.setDuration(250);
            ofFloat.addListener(new Animator.AnimatorListener() {
                public void onAnimationCancel(Animator animator) {
                }

                public void onAnimationRepeat(Animator animator) {
                }

                public void onAnimationStart(Animator animator) {
                }

                public void onAnimationEnd(Animator animator) {
                    MiuiKeyguardSingleClock.this.mOwnerInfo.setAlpha(0.0f);
                    MiuiKeyguardSingleClock.this.updateOwnerInfo();
                    MiuiKeyguardSingleClock.this.toNormalStateAnimOutEnd();
                }
            });
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    MiuiKeyguardSingleClock.this.mSimCardInfo.setAlpha(floatValue);
                    MiuiKeyguardSingleClock.this.mLockScreenMagazineInfo.setAlpha(floatValue);
                    MiuiKeyguardSingleClock.this.toNormalStateAnimOutUpdate(floatValue);
                }
            });
            ValueAnimator ofFloat2 = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
            ofFloat2.setDuration(250);
            ofFloat2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    MiuiKeyguardSingleClock.this.mSimCardInfo.setAlpha(floatValue);
                    MiuiKeyguardSingleClock.this.mOwnerInfo.setAlpha(floatValue);
                    MiuiKeyguardSingleClock.this.mLockScreenMagazineInfo.setAlpha(floatValue);
                    MiuiKeyguardSingleClock.this.toNormalStateAnimInUpdate(floatValue);
                }
            });
            this.mAnimToNormalState.play(ofFloat2).after(ofFloat);
            this.mAnimToNormalState.start();
        }
    }
}
