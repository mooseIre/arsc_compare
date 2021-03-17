package com.android.systemui.media;

import android.view.View;
import android.view.ViewParent;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MediaHierarchyManager.kt */
public final class MediaHierarchyManagerKt {
    public static final boolean isShownNotFaded(@NotNull View view) {
        ViewParent parent;
        Intrinsics.checkParameterIsNotNull(view, "$this$isShownNotFaded");
        while (view.getVisibility() == 0 && view.getAlpha() != 0.0f && (parent = view.getParent()) != null) {
            if (!(parent instanceof View)) {
                return true;
            }
            view = (View) parent;
        }
        return false;
    }
}
