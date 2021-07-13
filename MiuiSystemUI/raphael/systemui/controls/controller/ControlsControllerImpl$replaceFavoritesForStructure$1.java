package com.android.systemui.controls.controller;

/* compiled from: ControlsControllerImpl.kt */
final class ControlsControllerImpl$replaceFavoritesForStructure$1 implements Runnable {
    final /* synthetic */ StructureInfo $structureInfo;
    final /* synthetic */ ControlsControllerImpl this$0;

    ControlsControllerImpl$replaceFavoritesForStructure$1(ControlsControllerImpl controlsControllerImpl, StructureInfo structureInfo) {
        this.this$0 = controlsControllerImpl;
        this.$structureInfo = structureInfo;
    }

    public final void run() {
        Favorites.INSTANCE.replaceControls(this.$structureInfo);
        ControlsControllerImpl.access$getPersistenceWrapper$p(this.this$0).storeFavorites(Favorites.INSTANCE.getAllStructures());
    }
}
