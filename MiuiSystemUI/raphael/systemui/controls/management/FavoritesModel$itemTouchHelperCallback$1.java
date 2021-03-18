package com.android.systemui.controls.management;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: FavoritesModel.kt */
public final class FavoritesModel$itemTouchHelperCallback$1 extends ItemTouchHelper.SimpleCallback {
    private final int MOVEMENT = 15;
    final /* synthetic */ FavoritesModel this$0;

    @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
    public boolean isItemViewSwipeEnabled() {
        return false;
    }

    @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
    public void onSwiped(@NotNull RecyclerView.ViewHolder viewHolder, int i) {
        Intrinsics.checkParameterIsNotNull(viewHolder, "viewHolder");
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    FavoritesModel$itemTouchHelperCallback$1(FavoritesModel favoritesModel, int i, int i2) {
        super(i, i2);
        this.this$0 = favoritesModel;
    }

    @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
    public boolean onMove(@NotNull RecyclerView recyclerView, @NotNull RecyclerView.ViewHolder viewHolder, @NotNull RecyclerView.ViewHolder viewHolder2) {
        Intrinsics.checkParameterIsNotNull(recyclerView, "recyclerView");
        Intrinsics.checkParameterIsNotNull(viewHolder, "viewHolder");
        Intrinsics.checkParameterIsNotNull(viewHolder2, "target");
        this.this$0.onMoveItem(viewHolder.getAdapterPosition(), viewHolder2.getAdapterPosition());
        return true;
    }

    @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
    public int getMovementFlags(@NotNull RecyclerView recyclerView, @NotNull RecyclerView.ViewHolder viewHolder) {
        Intrinsics.checkParameterIsNotNull(recyclerView, "recyclerView");
        Intrinsics.checkParameterIsNotNull(viewHolder, "viewHolder");
        if (viewHolder.getAdapterPosition() < this.this$0.dividerPosition) {
            return ItemTouchHelper.Callback.makeMovementFlags(this.MOVEMENT, 0);
        }
        return ItemTouchHelper.Callback.makeMovementFlags(0, 0);
    }

    @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
    public boolean canDropOver(@NotNull RecyclerView recyclerView, @NotNull RecyclerView.ViewHolder viewHolder, @NotNull RecyclerView.ViewHolder viewHolder2) {
        Intrinsics.checkParameterIsNotNull(recyclerView, "recyclerView");
        Intrinsics.checkParameterIsNotNull(viewHolder, "current");
        Intrinsics.checkParameterIsNotNull(viewHolder2, "target");
        return viewHolder2.getAdapterPosition() < this.this$0.dividerPosition;
    }
}
