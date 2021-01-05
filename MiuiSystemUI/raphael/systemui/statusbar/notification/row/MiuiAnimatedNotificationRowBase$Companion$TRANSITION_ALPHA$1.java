package com.android.systemui.statusbar.notification.row;

import android.view.View;
import kotlin.jvm.internal.Intrinsics;
import miuix.animation.property.FloatProperty;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiAnimatedNotificationRowBase.kt */
public final class MiuiAnimatedNotificationRowBase$Companion$TRANSITION_ALPHA$1 extends FloatProperty<View> {
    MiuiAnimatedNotificationRowBase$Companion$TRANSITION_ALPHA$1(String str) {
        super(str);
    }

    public void setValue(@NotNull View view, float f) {
        Intrinsics.checkParameterIsNotNull(view, "v");
        view.setTransitionAlpha(f);
    }

    public float getValue(@NotNull View view) {
        Intrinsics.checkParameterIsNotNull(view, "v");
        return view.getTransitionAlpha();
    }
}
