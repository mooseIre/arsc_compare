package com.android.systemui.controls.management;

import android.content.ComponentName;
import android.view.View;
import com.android.systemui.controls.controller.ControlInfo;
import com.android.systemui.controls.controller.ControlsControllerImpl;
import com.android.systemui.controls.controller.StructureInfo;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: ControlsFavoritingActivity.kt */
final class ControlsFavoritingActivity$bindButtons$$inlined$apply$lambda$2 implements View.OnClickListener {
    final /* synthetic */ ControlsFavoritingActivity this$0;

    ControlsFavoritingActivity$bindButtons$$inlined$apply$lambda$2(ControlsFavoritingActivity controlsFavoritingActivity) {
        this.this$0 = controlsFavoritingActivity;
    }

    public final void onClick(View view) {
        if (this.this$0.component != null) {
            for (StructureContainer structureContainer : this.this$0.listOfStructures) {
                List<ControlInfo> favorites = structureContainer.getModel().getFavorites();
                ControlsControllerImpl controlsControllerImpl = this.this$0.controller;
                ComponentName componentName = this.this$0.component;
                if (componentName != null) {
                    controlsControllerImpl.replaceFavoritesForStructure(new StructureInfo(componentName, structureContainer.getStructureName(), favorites));
                } else {
                    Intrinsics.throwNpe();
                    throw null;
                }
            }
            this.this$0.animateExitAndFinish();
            this.this$0.globalActionsComponent.handleShowGlobalActionsMenu();
        }
    }
}
