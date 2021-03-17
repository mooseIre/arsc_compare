package com.android.systemui.util.animation;

import android.graphics.Rect;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: FloatProperties.kt */
public final class FloatProperties$Companion$RECT_HEIGHT$1 extends FloatPropertyCompat<Rect> {
    FloatProperties$Companion$RECT_HEIGHT$1(String str) {
        super(str);
    }

    public float getValue(@NotNull Rect rect) {
        Intrinsics.checkParameterIsNotNull(rect, "rect");
        return (float) rect.height();
    }

    public void setValue(@NotNull Rect rect, float f) {
        Intrinsics.checkParameterIsNotNull(rect, "rect");
        rect.bottom = rect.top + ((int) f);
    }
}
