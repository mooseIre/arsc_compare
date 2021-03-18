package com.android.systemui;

import dagger.internal.Factory;

public final class ForegroundServicesDialog_Factory implements Factory<ForegroundServicesDialog> {
    private static final ForegroundServicesDialog_Factory INSTANCE = new ForegroundServicesDialog_Factory();

    @Override // javax.inject.Provider
    public ForegroundServicesDialog get() {
        return provideInstance();
    }

    public static ForegroundServicesDialog provideInstance() {
        return new ForegroundServicesDialog();
    }

    public static ForegroundServicesDialog_Factory create() {
        return INSTANCE;
    }
}
