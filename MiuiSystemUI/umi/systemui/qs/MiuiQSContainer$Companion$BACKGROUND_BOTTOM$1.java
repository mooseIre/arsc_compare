package com.android.systemui.qs;

import androidx.dynamicanimation.animation.FloatPropertyCompat;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiQSContainer.kt */
public final class MiuiQSContainer$Companion$BACKGROUND_BOTTOM$1 extends FloatPropertyCompat<MiuiQSContainer> {
    MiuiQSContainer$Companion$BACKGROUND_BOTTOM$1(String str) {
        super(str);
    }

    public float getValue(@NotNull MiuiQSContainer miuiQSContainer) {
        Intrinsics.checkParameterIsNotNull(miuiQSContainer, "qsImpl");
        return miuiQSContainer.getBackgroundBottom();
    }

    public void setValue(@NotNull MiuiQSContainer miuiQSContainer, float f) {
        Intrinsics.checkParameterIsNotNull(miuiQSContainer, "background");
        miuiQSContainer.setBackgroundBottom((int) f);
    }
}
