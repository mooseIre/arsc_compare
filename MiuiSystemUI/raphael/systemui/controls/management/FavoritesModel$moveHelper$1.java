package com.android.systemui.controls.management;

import android.util.Log;
import com.android.systemui.controls.management.ControlsModel;

/* compiled from: FavoritesModel.kt */
public final class FavoritesModel$moveHelper$1 implements ControlsModel.MoveHelper {
    final /* synthetic */ FavoritesModel this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    FavoritesModel$moveHelper$1(FavoritesModel favoritesModel) {
        this.this$0 = favoritesModel;
    }

    @Override // com.android.systemui.controls.management.ControlsModel.MoveHelper
    public boolean canMoveBefore(int i) {
        return i > 0 && i < this.this$0.dividerPosition;
    }

    @Override // com.android.systemui.controls.management.ControlsModel.MoveHelper
    public boolean canMoveAfter(int i) {
        return i >= 0 && i < this.this$0.dividerPosition - 1;
    }

    @Override // com.android.systemui.controls.management.ControlsModel.MoveHelper
    public void moveBefore(int i) {
        if (!canMoveBefore(i)) {
            Log.w("FavoritesModel", "Cannot move position " + i + " before");
            return;
        }
        this.this$0.onMoveItem(i, i - 1);
    }

    @Override // com.android.systemui.controls.management.ControlsModel.MoveHelper
    public void moveAfter(int i) {
        if (!canMoveAfter(i)) {
            Log.w("FavoritesModel", "Cannot move position " + i + " after");
            return;
        }
        this.this$0.onMoveItem(i, i + 1);
    }
}
