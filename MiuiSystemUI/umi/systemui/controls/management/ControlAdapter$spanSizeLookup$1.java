package com.android.systemui.controls.management;

import androidx.recyclerview.widget.GridLayoutManager;

/* compiled from: ControlAdapter.kt */
public final class ControlAdapter$spanSizeLookup$1 extends GridLayoutManager.SpanSizeLookup {
    final /* synthetic */ ControlAdapter this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    ControlAdapter$spanSizeLookup$1(ControlAdapter controlAdapter) {
        this.this$0 = controlAdapter;
    }

    @Override // androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
    public int getSpanSize(int i) {
        return this.this$0.getItemViewType(i) != 1 ? 2 : 1;
    }
}
