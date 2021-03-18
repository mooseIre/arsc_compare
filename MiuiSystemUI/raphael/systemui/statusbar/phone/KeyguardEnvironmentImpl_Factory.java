package com.android.systemui.statusbar.phone;

import dagger.internal.Factory;

public final class KeyguardEnvironmentImpl_Factory implements Factory<KeyguardEnvironmentImpl> {
    private static final KeyguardEnvironmentImpl_Factory INSTANCE = new KeyguardEnvironmentImpl_Factory();

    @Override // javax.inject.Provider
    public KeyguardEnvironmentImpl get() {
        return provideInstance();
    }

    public static KeyguardEnvironmentImpl provideInstance() {
        return new KeyguardEnvironmentImpl();
    }

    public static KeyguardEnvironmentImpl_Factory create() {
        return INSTANCE;
    }
}
