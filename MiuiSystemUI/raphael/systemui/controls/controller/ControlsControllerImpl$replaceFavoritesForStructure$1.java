package com.android.systemui.controls.controller;

/* access modifiers changed from: package-private */
/* compiled from: ControlsControllerImpl.kt */
public final class ControlsControllerImpl$replaceFavoritesForStructure$1 implements Runnable {
    final /* synthetic */ StructureInfo $structureInfo;
    final /* synthetic */ ControlsControllerImpl this$0;

    ControlsControllerImpl$replaceFavoritesForStructure$1(ControlsControllerImpl controlsControllerImpl, StructureInfo structureInfo) {
        this.this$0 = controlsControllerImpl;
        this.$structureInfo = structureInfo;
    }

    public final void run() {
        Favorites.INSTANCE.replaceControls(this.$structureInfo);
        this.this$0.persistenceWrapper.storeFavorites(Favorites.INSTANCE.getAllStructures());
    }
}
