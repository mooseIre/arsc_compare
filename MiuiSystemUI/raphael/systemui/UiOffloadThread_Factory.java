package com.android.systemui;

import dagger.internal.Factory;

public final class UiOffloadThread_Factory implements Factory<UiOffloadThread> {
    private static final UiOffloadThread_Factory INSTANCE = new UiOffloadThread_Factory();

    @Override // javax.inject.Provider
    public UiOffloadThread get() {
        return provideInstance();
    }

    public static UiOffloadThread provideInstance() {
        return new UiOffloadThread();
    }

    public static UiOffloadThread_Factory create() {
        return INSTANCE;
    }
}
