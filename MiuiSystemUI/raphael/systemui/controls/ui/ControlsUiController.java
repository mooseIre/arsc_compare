package com.android.systemui.controls.ui;

import android.content.ComponentName;
import android.service.controls.Control;
import android.view.ViewGroup;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlsUiController.kt */
public interface ControlsUiController {
    static {
        Companion companion = Companion.$$INSTANCE;
    }

    void closeDialogs(boolean z);

    boolean getAvailable();

    void hide();

    void onActionResponse(@NotNull ComponentName componentName, @NotNull String str, int i);

    void onRefreshState(@NotNull ComponentName componentName, @NotNull List<Control> list);

    void show(@NotNull ViewGroup viewGroup, @NotNull Runnable runnable);

    /* compiled from: ControlsUiController.kt */
    public static final class Companion {
        static final /* synthetic */ Companion $$INSTANCE = new Companion();

        private Companion() {
        }
    }
}
