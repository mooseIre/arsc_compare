package com.android.systemui.accessibility;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class SystemActions_Factory implements Factory<SystemActions> {
    private final Provider<Context> contextProvider;

    public SystemActions_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public SystemActions get() {
        return provideInstance(this.contextProvider);
    }

    public static SystemActions provideInstance(Provider<Context> provider) {
        return new SystemActions(provider.get());
    }

    public static SystemActions_Factory create(Provider<Context> provider) {
        return new SystemActions_Factory(provider);
    }
}
