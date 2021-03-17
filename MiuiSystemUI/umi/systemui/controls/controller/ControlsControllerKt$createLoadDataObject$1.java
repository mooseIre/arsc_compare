package com.android.systemui.controls.controller;

import com.android.systemui.controls.ControlStatus;
import com.android.systemui.controls.controller.ControlsController;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlsController.kt */
public final class ControlsControllerKt$createLoadDataObject$1 implements ControlsController.LoadData {
    @NotNull
    private final List<ControlStatus> allControls;
    private final boolean errorOnLoad;
    @NotNull
    private final List<String> favoritesIds;

    ControlsControllerKt$createLoadDataObject$1(List list, List list2, boolean z) {
        this.allControls = list;
        this.favoritesIds = list2;
        this.errorOnLoad = z;
    }

    @Override // com.android.systemui.controls.controller.ControlsController.LoadData
    @NotNull
    public List<ControlStatus> getAllControls() {
        return this.allControls;
    }

    @Override // com.android.systemui.controls.controller.ControlsController.LoadData
    @NotNull
    public List<String> getFavoritesIds() {
        return this.favoritesIds;
    }

    @Override // com.android.systemui.controls.controller.ControlsController.LoadData
    public boolean getErrorOnLoad() {
        return this.errorOnLoad;
    }
}
