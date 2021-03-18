package com.android.keyguard;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiGoneFrameLayout.kt */
public final class MiuiGoneFrameLayout extends FrameLayout {
    public MiuiGoneFrameLayout(@NotNull Context context, @Nullable AttributeSet attributeSet) {
        this(context, attributeSet, 0, 4, null);
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public /* synthetic */ MiuiGoneFrameLayout(Context context, AttributeSet attributeSet, int i, int i2, DefaultConstructorMarker defaultConstructorMarker) {
        this(context, attributeSet, (i2 & 4) != 0 ? 0 : i);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MiuiGoneFrameLayout(@NotNull Context context, @Nullable AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        Intrinsics.checkParameterIsNotNull(context, "context");
    }

    public void setVisibility(int i) {
        super.setVisibility(8);
    }
}
