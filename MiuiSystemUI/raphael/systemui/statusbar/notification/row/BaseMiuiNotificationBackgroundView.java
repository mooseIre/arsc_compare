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
    private boolean mInGameMode;

    public BaseMiuiNotificationBackgroundView(@Nullable Context context, @Nullable AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public final void setHighSamplingFrequency(boolean z) {
        this.mHighSamplingFrequency = z;
        postInvalidateOnAnimation();
    }

    public final void setGameModeHint(boolean z) {
        this.mInGameMode = z;
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
        if (this.mInGameMode) {
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
