package com.android.systemui.controls.management;

import androidx.recyclerview.widget.RecyclerView;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: ControlsEditingActivity.kt */
public final class ControlsEditingActivity$setUpList$$inlined$apply$lambda$1 extends RecyclerView.AdapterDataObserver {
    final /* synthetic */ RecyclerView $recyclerView$inlined;
    private boolean hasAnimated;

    ControlsEditingActivity$setUpList$$inlined$apply$lambda$1(RecyclerView recyclerView) {
        this.$recyclerView$inlined = recyclerView;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
    public void onChanged() {
        if (!this.hasAnimated) {
            this.hasAnimated = true;
            ControlsAnimations controlsAnimations = ControlsAnimations.INSTANCE;
            RecyclerView recyclerView = this.$recyclerView$inlined;
            Intrinsics.checkExpressionValueIsNotNull(recyclerView, "recyclerView");
            controlsAnimations.enterAnimation(recyclerView).start();
        }
    }
}
