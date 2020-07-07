package com.android.systemui.statusbar.phone;

import android.content.res.Resources;
import android.graphics.Path;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.PathInterpolator;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.notification.NotificationUtils;

public class KeyguardClockPositionAlgorithm {
    private static final PathInterpolator sSlowDownInterpolator;
    private AccelerateInterpolator mAccelerateInterpolator = new AccelerateInterpolator();
    private int mClockBottom;
    private float mClockMarginTop;
    private int mClockNotificationsMarginMax;
    private int mClockNotificationsMarginMin;
    private float mClockNotificationsPadding;
    private float mClockYFractionMax;
    private float mClockYFractionMin;
    private float mDarkAmount;
    private float mDensity;
    private float mEmptyDragAmount;
    private float mExpandedHeight;
    private int mHeight;
    private int mKeyguardStatusHeight;
    private float mKeyguardVisibleViewsHeight;
    private int mMaxKeyguardNotifications;
    private int mMaxPanelHeight;
    private float mMoreCardNotificationAmount;
    private int mNotificationCount;

    public static class Result {
        public float clockAlpha;
        public float clockScale;
        public int clockY;
        public int stackScrollerPadding;
        public int stackScrollerPaddingAdjustment;
    }

    static {
        Path path = new Path();
        path.moveTo(0.0f, 0.0f);
        path.cubicTo(0.3f, 0.875f, 0.6f, 1.0f, 1.0f, 1.0f);
        sSlowDownInterpolator = new PathInterpolator(path);
    }

    public void loadDimens(Resources resources) {
        this.mClockNotificationsMarginMin = resources.getDimensionPixelSize(R.dimen.keyguard_clock_notifications_margin_min);
        this.mClockNotificationsMarginMax = resources.getDimensionPixelSize(R.dimen.keyguard_clock_notifications_margin_max);
        this.mClockYFractionMin = resources.getFraction(R.fraction.keyguard_clock_y_fraction_min, 1, 1);
        this.mClockYFractionMax = resources.getFraction(R.fraction.keyguard_clock_y_fraction_max, 1, 1);
        this.mMoreCardNotificationAmount = ((float) resources.getDimensionPixelSize(R.dimen.notification_shelf_height)) / ((float) resources.getDimensionPixelSize(R.dimen.notification_min_height));
        this.mDensity = resources.getDisplayMetrics().density;
        this.mClockMarginTop = (float) resources.getDimensionPixelSize(R.dimen.miui_keyguard_clock_magin_top);
        this.mClockNotificationsPadding = (float) resources.getDimensionPixelSize(R.dimen.miui_keyguard_clock_stack_scroller_padding_top);
    }

    public void setup(int i, int i2, float f, int i3, int i4, int i5, float f2, int i6, float f3, float f4, float f5) {
        this.mMaxKeyguardNotifications = i;
        this.mMaxPanelHeight = i2;
        this.mExpandedHeight = f;
        this.mNotificationCount = i3;
        this.mHeight = i4;
        this.mKeyguardStatusHeight = i5;
        this.mKeyguardVisibleViewsHeight = f4;
        this.mEmptyDragAmount = f2;
        this.mClockBottom = i6;
        this.mDarkAmount = f3;
        this.mClockMarginTop = f5;
    }

    public void run(Result result) {
        int clockY = getClockY() - (this.mKeyguardStatusHeight / 2);
        result.stackScrollerPaddingAdjustment = 0;
        result.clockY = 0;
        result.stackScrollerPadding = (int) (this.mKeyguardVisibleViewsHeight + ((float) ((int) (this.mClockMarginTop + this.mClockNotificationsPadding))));
        result.clockScale = 1.0f;
        result.clockAlpha = 1.0f;
        result.stackScrollerPadding = (int) NotificationUtils.interpolate((float) result.stackScrollerPadding, (float) (this.mClockBottom + clockY), this.mDarkAmount);
    }

    private float getClockYFraction() {
        float min = Math.min(getNotificationAmountT(), 1.0f);
        return ((1.0f - min) * this.mClockYFractionMax) + (min * this.mClockYFractionMin);
    }

    private int getClockY() {
        return (int) NotificationUtils.interpolate(getClockYFraction() * ((float) this.mHeight), ((((float) this.mHeight) * 0.33f) + (((float) this.mKeyguardStatusHeight) / 2.0f)) - ((float) this.mClockBottom), this.mDarkAmount);
    }

    private float getNotificationAmountT() {
        return ((float) this.mNotificationCount) / (((float) this.mMaxKeyguardNotifications) + this.mMoreCardNotificationAmount);
    }

    public String toString() {
        return "{mHeight=" + this.mHeight + ", mKeyguardStatusHeight=" + this.mKeyguardStatusHeight + ", mKeyguardVisibleViewsHeight=" + this.mKeyguardVisibleViewsHeight + ", mClockMarginTop=" + this.mClockMarginTop + ", mClockNotificationsPadding=" + this.mClockNotificationsPadding + ", mClockBottom=" + this.mClockBottom + ", clockY=" + getClockY() + ", mDarkAmount=" + this.mDarkAmount + '}';
    }
}
