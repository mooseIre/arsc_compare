package com.android.systemui.statusbar.phone;

import android.content.res.Resources;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0014R$fraction;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.statusbar.phone.KeyguardClockPositionAlgorithm;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt___RangesKt;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiKeyguardClockPositionAlgorithm.kt */
public final class MiuiKeyguardClockPositionAlgorithm extends KeyguardClockPositionAlgorithm {
    private float mClockNotificationsPadding;
    private float mClockYFractionMax;
    private float mClockYFractionMin;
    private int mKeyguardClockHeight;
    private int mKeyguardVisibleClockHeight;
    private int mKeyguardVisibleNotifications;
    private int mMaxKeyguardNotifications;

    public final void setupMiuiClock(int i, int i2, int i3, int i4) {
        this.mKeyguardClockHeight = i;
        this.mKeyguardVisibleClockHeight = i2;
        this.mKeyguardVisibleNotifications = i3;
        this.mMaxKeyguardNotifications = i4;
    }

    @Override // com.android.systemui.statusbar.phone.KeyguardClockPositionAlgorithm
    public void run(@Nullable KeyguardClockPositionAlgorithm.Result result) {
        super.run(result);
        int clockY = getClockY() - (this.mKeyguardClockHeight / 2);
        if (result != null) {
            result.stackScrollerPadding = (int) NotificationUtils.interpolate(((float) this.mKeyguardVisibleClockHeight) + this.mClockNotificationsPadding, (float) clockY, this.mDarkAmount);
            result.clockAlpha = 1.0f;
            return;
        }
        Intrinsics.throwNpe();
        throw null;
    }

    @Override // com.android.systemui.statusbar.phone.KeyguardClockPositionAlgorithm
    public void loadDimens(@Nullable Resources resources) {
        super.loadDimens(resources);
        if (resources != null) {
            this.mClockYFractionMin = resources.getFraction(C0014R$fraction.keyguard_clock_y_fraction_min, 1, 1);
            this.mClockYFractionMax = resources.getFraction(C0014R$fraction.keyguard_clock_y_fraction_max, 1, 1);
            resources.getDimensionPixelSize(C0012R$dimen.miui_keyguard_clock_magin_top);
            this.mClockNotificationsPadding = (float) resources.getDimensionPixelSize(C0012R$dimen.miui_keyguard_clock_stack_scroller_padding_top);
            return;
        }
        Intrinsics.throwNpe();
        throw null;
    }

    private final int getClockY() {
        return (int) NotificationUtils.interpolate(getClockYFraction() * ((float) this.mHeight), (((float) this.mHeight) * 0.33f) + (((float) this.mKeyguardClockHeight) / ((float) 2)), this.mDarkAmount);
    }

    private final float getClockYFraction() {
        float f = RangesKt___RangesKt.coerceAtMost(getNotificationAmountT(), 1.0f);
        return ((((float) 1) - f) * this.mClockYFractionMax) + (f * this.mClockYFractionMin);
    }

    private final float getNotificationAmountT() {
        int i = this.mMaxKeyguardNotifications;
        int i2 = i + i;
        if (i2 == 0) {
            return 0.0f;
        }
        return (float) (this.mKeyguardVisibleNotifications / i2);
    }
}
