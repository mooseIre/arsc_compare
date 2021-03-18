package com.android.systemui.controls.management;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlAdapter.kt */
public final class MarginItemDecorator extends RecyclerView.ItemDecoration {
    private final int sideMargins;
    private final int topMargin;

    public MarginItemDecorator(int i, int i2) {
        this.topMargin = i;
        this.sideMargins = i2;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.ItemDecoration
    public void getItemOffsets(@NotNull Rect rect, @NotNull View view, @NotNull RecyclerView recyclerView, @NotNull RecyclerView.State state) {
        Intrinsics.checkParameterIsNotNull(rect, "outRect");
        Intrinsics.checkParameterIsNotNull(view, "view");
        Intrinsics.checkParameterIsNotNull(recyclerView, "parent");
        Intrinsics.checkParameterIsNotNull(state, "state");
        int childAdapterPosition = recyclerView.getChildAdapterPosition(view);
        if (childAdapterPosition != -1) {
            RecyclerView.Adapter adapter = recyclerView.getAdapter();
            Integer valueOf = adapter != null ? Integer.valueOf(adapter.getItemViewType(childAdapterPosition)) : null;
            if (valueOf != null && valueOf.intValue() == 1) {
                rect.top = this.topMargin;
                int i = this.sideMargins;
                rect.left = i;
                rect.right = i;
                rect.bottom = 0;
            } else if (valueOf != null && valueOf.intValue() == 0 && childAdapterPosition == 0) {
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                if (layoutParams != null) {
                    rect.top = -((ViewGroup.MarginLayoutParams) layoutParams).topMargin;
                    rect.left = 0;
                    rect.right = 0;
                    rect.bottom = 0;
                    return;
                }
                throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup.MarginLayoutParams");
            }
        }
    }
}
