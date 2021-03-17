package com.android.systemui.util;

import dagger.internal.Factory;

public final class FloatingContentCoordinator_Factory implements Factory<FloatingContentCoordinator> {
    private static final FloatingContentCoordinator_Factory INSTANCE = new FloatingContentCoordinator_Factory();

    @Override // javax.inject.Provider
    public FloatingContentCoordinator get() {
        return provideInstance();
    }

    public static FloatingContentCoordinator provideInstance() {
        return new FloatingContentCoordinator();
    }

    public static FloatingContentCoordinator_Factory create() {
        return INSTANCE;
    }
}
