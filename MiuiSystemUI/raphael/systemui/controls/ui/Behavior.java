package com.android.systemui.controls.ui;

import org.jetbrains.annotations.NotNull;

/* compiled from: Behavior.kt */
public interface Behavior {
    void bind(@NotNull ControlWithState controlWithState, int i);

    void initialize(@NotNull ControlViewHolder controlViewHolder);
}
