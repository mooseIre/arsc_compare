package com.android.systemui.controls.controller;

import android.content.ComponentName;

/* compiled from: ControlsControllerImpl.kt */
final class ControlsControllerImpl$addFavorite$1 implements Runnable {
    final /* synthetic */ ComponentName $componentName;
    final /* synthetic */ ControlInfo $controlInfo;
    final /* synthetic */ CharSequence $structureName;
    final /* synthetic */ ControlsControllerImpl this$0;

    ControlsControllerImpl$addFavorite$1(ControlsControllerImpl controlsControllerImpl, ComponentName componentName, CharSequence charSequence, ControlInfo controlInfo) {
        this.this$0 = controlsControllerImpl;
        this.$componentName = componentName;
        this.$structureName = charSequence;
        this.$controlInfo = controlInfo;
    }

    public final void run() {
        if (Favorites.INSTANCE.addFavorite(this.$componentName, this.$structureName, this.$controlInfo)) {
            ControlsControllerImpl.access$getPersistenceWrapper$p(this.this$0).storeFavorites(Favorites.INSTANCE.getAllStructures());
        }
    }
}
