package com.android.systemui.statusbar.notification.row;

import android.content.Context;
import android.util.AttributeSet;
import com.miui.systemui.views.BlurOnDefaultThemeView;
import org.jetbrains.annotations.Nullable;

/* compiled from: BaseMiuiNotificationBackgroundView.kt */
public class BaseMiuiNotificationBackgroundView extends BlurOnDefaultThemeView {
    private boolean mHighSamplingFrequency;

    public BaseMiuiNotificationBackgroundView(@Nullable Context context, @Nullable AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public final void setHighSamplingFrequency(boolean z) {
        this.mHighSamplingFrequency = z;
        postInvalidateOnAnimation();
    }

    public final void setBlurDisable(boolean z) {
        updateBlurStatusIfNeed(z);
        postInvalidateOnAnimation();
    }

    @Override // com.miui.blur.sdk.backdrop.BlurDrawInfo
    public int getRequestedSamplingPeriodNs() {
        if (this.mHighSamplingFrequency) {
            return 0;
        }
        return super.getRequestedSamplingPeriodNs();
    }
}
