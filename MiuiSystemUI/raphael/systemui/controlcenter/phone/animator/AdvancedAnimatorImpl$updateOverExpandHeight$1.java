package com.android.systemui.controlcenter.phone.animator;

import android.view.View;
import com.android.systemui.C0015R$id;
import com.android.systemui.controlcenter.utils.ControlCenterUtils;
import java.util.function.Consumer;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: AdvancedAnimatorImpl.kt */
final class AdvancedAnimatorImpl$updateOverExpandHeight$1<T> implements Consumer<View> {
    final /* synthetic */ float $height;
    final /* synthetic */ int $threshold;
    final /* synthetic */ AdvancedAnimatorImpl this$0;

    AdvancedAnimatorImpl$updateOverExpandHeight$1(AdvancedAnimatorImpl advancedAnimatorImpl, float f, int i) {
        this.this$0 = advancedAnimatorImpl;
        this.$height = f;
        this.$threshold = i;
    }

    public final void accept(@NotNull View view) {
        Intrinsics.checkParameterIsNotNull(view, "view");
        Object tag = view.getTag(C0015R$id.tag_control_center_trans);
        if (tag != null) {
            view.setTranslationY(ControlCenterUtils.getTranslationY(((Integer) tag).intValue(), this.this$0.overFlingLines, this.$height, (float) this.$threshold));
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type kotlin.Int");
    }
}
