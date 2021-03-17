package com.android.systemui.keyguard;

import dagger.internal.Factory;

public final class ScreenLifecycle_Factory implements Factory<ScreenLifecycle> {
    private static final ScreenLifecycle_Factory INSTANCE = new ScreenLifecycle_Factory();

    @Override // javax.inject.Provider
    public ScreenLifecycle get() {
        return provideInstance();
    }

    public static ScreenLifecycle provideInstance() {
        return new ScreenLifecycle();
    }

    public static ScreenLifecycle_Factory create() {
        return INSTANCE;
    }
}
