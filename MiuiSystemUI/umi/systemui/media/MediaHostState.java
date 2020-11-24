package com.android.systemui.media;

import com.android.systemui.util.animation.DisappearParameters;
import com.android.systemui.util.animation.MeasurementInput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaHost.kt */
public interface MediaHostState {
    @NotNull
    MediaHostState copy();

    @NotNull
    DisappearParameters getDisappearParameters();

    float getExpansion();

    boolean getFalsingProtectionNeeded();

    @Nullable
    MeasurementInput getMeasurementInput();

    boolean getShowsOnlyActiveMedia();

    boolean getVisible();

    void setExpansion(float f);
}
