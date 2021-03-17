package com.android.systemui.util.animation;

import android.view.View;
import com.android.systemui.C0015R$id;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: UniqueObjectHostView.kt */
public final class UniqueObjectHostViewKt {
    public static final boolean getRequiresRemeasuring(@NotNull View view) {
        Intrinsics.checkParameterIsNotNull(view, "$this$requiresRemeasuring");
        Object tag = view.getTag(C0015R$id.requires_remeasuring);
        if (tag != null) {
            return tag.equals(Boolean.TRUE);
        }
        return false;
    }

    public static final void setRequiresRemeasuring(@NotNull View view, boolean z) {
        Intrinsics.checkParameterIsNotNull(view, "$this$requiresRemeasuring");
        view.setTag(C0015R$id.requires_remeasuring, Boolean.valueOf(z));
    }
}
