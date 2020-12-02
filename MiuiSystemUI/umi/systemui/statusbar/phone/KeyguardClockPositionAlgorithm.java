package com.android.systemui.statusbar.phone;

import android.content.res.Resources;
import android.util.MathUtils;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.Interpolators;
import com.android.systemui.doze.util.BurnInHelperKt;
import com.android.systemui.statusbar.notification.NotificationUtils;

public class KeyguardClockPositionAlgorithm {
    private static float CLOCK_HEIGHT_WEIGHT = 0.7f;
    private int mBurnInPreventionOffsetX;
    private int mBurnInPreventionOffsetY;
    private boolean mBypassEnabled;
    private int mClockNotificationsMargin;
    private int mClockPreferredY;
    private int mContainerTopPadding;
    protected float mDarkAmount;
    private float mEmptyDragAmount;
    private boolean mHasCustomClock;
    private boolean mHasVisibleNotifs;
    protected int mHeight;
    private int mKeyguardStatusHeight;
    private int mMaxShadeBottom;
    private int mMinTopMargin;
    private int mNotificationStackHeight;
    private float mPanelExpansion;
    private int mUnlockedStackScrollerPadding;

    public static class Result {
        public float clockAlpha;
        public int clockX;
        public int clockY;
        public int stackScrollerPadding;
        public int stackScrollerPaddingExpanded;
    }

    public void loadDimens(Resources resources) {
        this.mClockNotificationsMargin = resources.getDimensionPixelSize(C0012R$dimen.keyguard_clock_notifications_margin);
        this.mContainerTopPadding = Math.max(resources.getDimensionPixelSize(C0012R$dimen.keyguard_clock_top_margin), resources.getDimensionPixelSize(C0012R$dimen.keyguard_lock_height) + resources.getDimensionPixelSize(C0012R$dimen.keyguard_lock_padding) + resources.getDimensionPixelSize(C0012R$dimen.keyguard_clock_lock_margin));
        this.mBurnInPreventionOffsetX = resources.getDimensionPixelSize(C0012R$dimen.burn_in_prevention_offset_x);
        this.mBurnInPreventionOffsetY = resources.getDimensionPixelSize(C0012R$dimen.burn_in_prevention_offset_y);
    }

    public void setup(int i, int i2, int i3, float f, int i4, int i5, int i6, boolean z, boolean z2, float f2, float f3, boolean z3, int i7) {
        this.mMinTopMargin = i + this.mContainerTopPadding;
        this.mMaxShadeBottom = i2;
        this.mNotificationStackHeight = i3;
        this.mPanelExpansion = f;
        this.mHeight = i4;
        this.mKeyguardStatusHeight = i5;
        this.mClockPreferredY = i6;
        this.mHasCustomClock = z;
        this.mHasVisibleNotifs = z2;
        this.mDarkAmount = f2;
        this.mEmptyDragAmount = f3;
        this.mBypassEnabled = z3;
        this.mUnlockedStackScrollerPadding = i7;
    }

    public void run(Result result) {
        int i;
        int i2;
        int clockY = getClockY(this.mPanelExpansion);
        result.clockY = clockY;
        result.clockAlpha = getClockAlpha(clockY);
        if (this.mBypassEnabled) {
            i = this.mUnlockedStackScrollerPadding;
        } else {
            i = clockY + this.mKeyguardStatusHeight;
        }
        result.stackScrollerPadding = i;
        if (this.mBypassEnabled) {
            i2 = this.mUnlockedStackScrollerPadding;
        } else {
            i2 = getClockY(1.0f) + this.mKeyguardStatusHeight;
        }
        result.stackScrollerPaddingExpanded = i2;
        result.clockX = (int) NotificationUtils.interpolate(0.0f, burnInPreventionOffsetX(), this.mDarkAmount);
    }

    public float getMinStackScrollerPadding() {
        if (this.mBypassEnabled) {
            return (float) this.mUnlockedStackScrollerPadding;
        }
        return (float) (this.mMinTopMargin + this.mKeyguardStatusHeight + this.mClockNotificationsMargin);
    }

    private int getMaxClockY() {
        return ((this.mHeight / 2) - this.mKeyguardStatusHeight) - this.mClockNotificationsMargin;
    }

    private int getPreferredClockY() {
        return this.mClockPreferredY;
    }

    private int getExpandedPreferredClockY() {
        if (!this.mHasCustomClock || (this.mHasVisibleNotifs && !this.mBypassEnabled)) {
            return getExpandedClockPosition();
        }
        return getPreferredClockY();
    }

    public int getExpandedClockPosition() {
        int i = this.mMaxShadeBottom;
        int i2 = this.mMinTopMargin;
        float f = ((((float) (((i - i2) / 2) + i2)) - (((float) this.mKeyguardStatusHeight) * CLOCK_HEIGHT_WEIGHT)) - ((float) this.mClockNotificationsMargin)) - ((float) (this.mNotificationStackHeight / 2));
        if (f < ((float) i2)) {
            f = (float) i2;
        }
        float maxClockY = (float) getMaxClockY();
        if (f > maxClockY) {
            f = maxClockY;
        }
        return (int) f;
    }

    /* access modifiers changed from: protected */
    public int getClockY(float f) {
        float max = MathUtils.max(0.0f, ((float) (this.mHasCustomClock ? getPreferredClockY() : getMaxClockY())) + burnInPreventionOffsetY());
        float f2 = (float) (-this.mKeyguardStatusHeight);
        float interpolation = Interpolators.FAST_OUT_LINEAR_IN.getInterpolation(f);
        return (int) (MathUtils.lerp(MathUtils.lerp(f2, (float) getExpandedPreferredClockY(), interpolation), MathUtils.lerp(f2, max, interpolation), (!this.mBypassEnabled || this.mHasCustomClock) ? this.mDarkAmount : 1.0f) + this.mEmptyDragAmount);
    }

    private float getClockAlpha(int i) {
        return MathUtils.lerp(Interpolators.ACCELERATE.getInterpolation(Math.max(0.0f, ((float) i) / Math.max(1.0f, (float) getClockY(1.0f)))), 1.0f, this.mDarkAmount);
    }

    private float burnInPreventionOffsetY() {
        return (float) (BurnInHelperKt.getBurnInOffset(this.mBurnInPreventionOffsetY * 2, false) - this.mBurnInPreventionOffsetY);
    }

    private float burnInPreventionOffsetX() {
        return (float) (BurnInHelperKt.getBurnInOffset(this.mBurnInPreventionOffsetX * 2, true) - this.mBurnInPreventionOffsetX);
    }
}
