package com.android.systemui.statusbar.policy;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class CastControllerImpl_Factory implements Factory<CastControllerImpl> {
    private final Provider<Context> contextProvider;

    public CastControllerImpl_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public CastControllerImpl get() {
        return provideInstance(this.contextProvider);
    }

    public static CastControllerImpl provideInstance(Provider<Context> provider) {
        return new CastControllerImpl(provider.get());
    }

    public static CastControllerImpl_Factory create(Provider<Context> provider) {
        return new CastControllerImpl_Factory(provider);
    }
}
