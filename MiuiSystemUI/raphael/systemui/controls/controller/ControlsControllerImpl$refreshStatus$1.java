package com.android.systemui.controls.controller;

import android.content.ComponentName;
import android.service.controls.Control;
import kotlin.collections.CollectionsKt__CollectionsJVMKt;

/* compiled from: ControlsControllerImpl.kt */
final class ControlsControllerImpl$refreshStatus$1 implements Runnable {
    final /* synthetic */ ComponentName $componentName;
    final /* synthetic */ Control $control;
    final /* synthetic */ ControlsControllerImpl this$0;

    ControlsControllerImpl$refreshStatus$1(ControlsControllerImpl controlsControllerImpl, ComponentName componentName, Control control) {
        this.this$0 = controlsControllerImpl;
        this.$componentName = componentName;
        this.$control = control;
    }

    public final void run() {
        if (Favorites.INSTANCE.updateControls(this.$componentName, CollectionsKt__CollectionsJVMKt.listOf(this.$control))) {
            ControlsControllerImpl.access$getPersistenceWrapper$p(this.this$0).storeFavorites(Favorites.INSTANCE.getAllStructures());
        }
    }
}
