package com.android.systemui.controls.management;

import android.content.Context;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlsEditingActivity.kt */
public final class ControlsEditingActivity$setUpList$$inlined$apply$lambda$2 extends GridLayoutManager {
    ControlsEditingActivity$setUpList$$inlined$apply$lambda$2(Context context, int i, ControlAdapter controlAdapter, RecyclerView recyclerView, MarginItemDecorator marginItemDecorator) {
        super(context, i);
    }

    @Override // androidx.recyclerview.widget.GridLayoutManager, androidx.recyclerview.widget.RecyclerView.LayoutManager
    public int getRowCountForAccessibility(@NotNull RecyclerView.Recycler recycler, @NotNull RecyclerView.State state) {
        Intrinsics.checkParameterIsNotNull(recycler, "recycler");
        Intrinsics.checkParameterIsNotNull(state, "state");
        int rowCountForAccessibility = super.getRowCountForAccessibility(recycler, state);
        return rowCountForAccessibility > 0 ? rowCountForAccessibility - 1 : rowCountForAccessibility;
    }
}
