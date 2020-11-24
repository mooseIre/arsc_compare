package com.android.systemui.controls.controller;

import android.content.ComponentName;
import android.service.controls.Control;
import android.service.controls.actions.ControlAction;
import com.android.systemui.util.UserAwareController;
import java.util.List;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlsBindingController.kt */
public interface ControlsBindingController extends UserAwareController {

    /* compiled from: ControlsBindingController.kt */
    public interface LoadCallback extends Consumer<List<? extends Control>> {
        void error(@NotNull String str);
    }

    void action(@NotNull ComponentName componentName, @NotNull ControlInfo controlInfo, @NotNull ControlAction controlAction);

    @NotNull
    Runnable bindAndLoad(@NotNull ComponentName componentName, @NotNull LoadCallback loadCallback);

    void bindAndLoadSuggested(@NotNull ComponentName componentName, @NotNull LoadCallback loadCallback);

    void onComponentRemoved(@NotNull ComponentName componentName);

    void subscribe(@NotNull StructureInfo structureInfo);

    void unsubscribe();
}
