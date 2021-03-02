package com.android.systemui.statusbar.notification.row;

import android.content.Context;
import android.util.AttributeSet;
import com.miui.blur.sdk.backdrop.BlurStyle;
import com.miui.systemui.views.BlurOnDefaultThemeView;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: BaseMiuiNotificationBackgroundView.kt */
public class BaseMiuiNotificationBackgroundView extends BlurOnDefaultThemeView {
    private boolean mHighSamplingFrequency;
    private boolean mInTransparentMode;

    public BaseMiuiNotificationBackgroundView(@Nullable Context context, @Nullable AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        if (NotificationContentInflaterInjector.isTransparentMode()) {
            disableBlur();
        }
        super.onAttachedToWindow();
    }

    public final void setHighSamplingFrequency(boolean z) {
        this.mHighSamplingFrequency = z;
        postInvalidateOnAnimation();
    }

    public final void setTransparentModeHint(boolean z) {
        this.mInTransparentMode = z;
        postInvalidateOnAnimation();
    }

    public int getRequestedSamplingPeriodNs() {
        if (this.mHighSamplingFrequency) {
            return 0;
        }
        return super.getRequestedSamplingPeriodNs();
    }

    @NotNull
    public BlurStyle getBlurStyleDayMode() {
        BlurStyle blurStyle;
        String str;
        if (this.mInTransparentMode) {
            blurStyle = super.getBlurStyleNightMode();
            str = "super.getBlurStyleNightMode()";
        } else {
            blurStyle = super.getBlurStyleDayMode();
            str = "super.getBlurStyleDayMode()";
        }
        Intrinsics.checkExpressionValueIsNotNull(blurStyle, str);
        return blurStyle;
    }
}
