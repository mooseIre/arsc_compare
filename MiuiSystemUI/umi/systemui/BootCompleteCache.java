package com.android.systemui;

import org.jetbrains.annotations.NotNull;

/* compiled from: BootCompleteCache.kt */
public interface BootCompleteCache {

    /* compiled from: BootCompleteCache.kt */
    public interface BootCompleteListener {
        void onBootComplete();
    }

    boolean addListener(@NotNull BootCompleteListener bootCompleteListener);

    boolean isBootComplete();

    void removeListener(@NotNull BootCompleteListener bootCompleteListener);
}
