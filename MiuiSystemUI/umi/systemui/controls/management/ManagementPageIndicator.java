package com.android.systemui.controls.management;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.android.systemui.qs.PageIndicator;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ManagementPageIndicator.kt */
public final class ManagementPageIndicator extends PageIndicator {
    @NotNull
    private Function1<? super Integer, Unit> visibilityListener = ManagementPageIndicator$visibilityListener$1.INSTANCE;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ManagementPageIndicator(@NotNull Context context, @NotNull AttributeSet attributeSet) {
        super(context, attributeSet);
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(attributeSet, "attrs");
    }

    @Override // com.android.systemui.qs.PageIndicator
    public void setLocation(float f) {
        if (getLayoutDirection() == 1) {
            super.setLocation(((float) (getChildCount() - 1)) - f);
        } else {
            super.setLocation(f);
        }
    }

    public final void setVisibilityListener(@NotNull Function1<? super Integer, Unit> function1) {
        Intrinsics.checkParameterIsNotNull(function1, "<set-?>");
        this.visibilityListener = function1;
    }

    /* access modifiers changed from: protected */
    public void onVisibilityChanged(@NotNull View view, int i) {
        Intrinsics.checkParameterIsNotNull(view, "changedView");
        super.onVisibilityChanged(view, i);
        if (Intrinsics.areEqual(view, this)) {
            this.visibilityListener.invoke(Integer.valueOf(i));
        }
    }
}
