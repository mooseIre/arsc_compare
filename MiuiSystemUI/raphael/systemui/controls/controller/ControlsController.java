package com.android.systemui.controls.controller;

import android.content.ComponentName;
import android.service.controls.Control;
import android.service.controls.actions.ControlAction;
import com.android.systemui.controls.ControlStatus;
import com.android.systemui.util.UserAwareController;
import java.util.List;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlsController.kt */
public interface ControlsController extends UserAwareController {

    /* compiled from: ControlsController.kt */
    public interface LoadData {
        @NotNull
        List<ControlStatus> getAllControls();

        boolean getErrorOnLoad();

        @NotNull
        List<String> getFavoritesIds();
    }

    void action(@NotNull ComponentName componentName, @NotNull ControlInfo controlInfo, @NotNull ControlAction controlAction);

    void addFavorite(@NotNull ComponentName componentName, @NotNull CharSequence charSequence, @NotNull ControlInfo controlInfo);

    boolean addSeedingFavoritesCallback(@NotNull Consumer<Boolean> consumer);

    int countFavoritesForComponent(@NotNull ComponentName componentName);

    boolean getAvailable();

    @NotNull
    List<StructureInfo> getFavorites();

    @NotNull
    List<StructureInfo> getFavoritesForComponent(@NotNull ComponentName componentName);

    void onActionResponse(@NotNull ComponentName componentName, @NotNull String str, int i);

    void refreshStatus(@NotNull ComponentName componentName, @NotNull Control control);

    void seedFavoritesForComponents(@NotNull List<ComponentName> list, @NotNull Consumer<SeedResponse> consumer);

    void subscribeToFavorites(@NotNull StructureInfo structureInfo);

    void unsubscribe();
}
