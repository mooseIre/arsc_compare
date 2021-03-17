package com.android.systemui.util.animation;

import android.graphics.Rect;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import kotlin.jvm.internal.FloatCompanionObject;
import org.jetbrains.annotations.Nullable;

/* compiled from: FloatProperties.kt */
public final class FloatProperties$Companion$RECT_X$1 extends FloatPropertyCompat<Rect> {
    FloatProperties$Companion$RECT_X$1(String str) {
        super(str);
    }

    public void setValue(@Nullable Rect rect, float f) {
        if (rect != null) {
            rect.offsetTo((int) f, rect.top);
        }
    }

    public float getValue(@Nullable Rect rect) {
        return rect != null ? (float) rect.left : -FloatCompanionObject.INSTANCE.getMAX_VALUE();
    }
}
