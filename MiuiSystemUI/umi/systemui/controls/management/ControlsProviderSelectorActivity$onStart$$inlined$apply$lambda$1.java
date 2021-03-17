package com.android.systemui.controls.management;

import androidx.recyclerview.widget.RecyclerView;

/* compiled from: ControlsProviderSelectorActivity.kt */
public final class ControlsProviderSelectorActivity$onStart$$inlined$apply$lambda$1 extends RecyclerView.AdapterDataObserver {
    private boolean hasAnimated;
    final /* synthetic */ ControlsProviderSelectorActivity this$0;

    ControlsProviderSelectorActivity$onStart$$inlined$apply$lambda$1(ControlsProviderSelectorActivity controlsProviderSelectorActivity) {
        this.this$0 = controlsProviderSelectorActivity;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
    public void onChanged() {
        if (!this.hasAnimated) {
            this.hasAnimated = true;
            ControlsAnimations.INSTANCE.enterAnimation(ControlsProviderSelectorActivity.access$getRecyclerView$p(this.this$0)).start();
        }
    }
}
