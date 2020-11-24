package com.android.systemui.controls.management;

import com.android.systemui.controls.controller.ControlInfo;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlsModel.kt */
public interface ControlsModel {

    /* compiled from: ControlsModel.kt */
    public interface ControlsModelCallback {
        void onFirstChange();
    }

    /* compiled from: ControlsModel.kt */
    public interface MoveHelper {
        boolean canMoveAfter(int i);

        boolean canMoveBefore(int i);

        void moveAfter(int i);

        void moveBefore(int i);
    }

    void changeFavoriteStatus(@NotNull String str, boolean z);

    @NotNull
    List<ElementWrapper> getElements();

    @NotNull
    List<ControlInfo> getFavorites();

    @Nullable
    MoveHelper getMoveHelper();
}
