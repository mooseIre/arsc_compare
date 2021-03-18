package com.android.systemui.statusbar.policy;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class FiveGControllerImpl_Factory implements Factory<FiveGControllerImpl> {
    private final Provider<Context> contextProvider;

    public FiveGControllerImpl_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public FiveGControllerImpl get() {
        return provideInstance(this.contextProvider);
    }

    public static FiveGControllerImpl provideInstance(Provider<Context> provider) {
        return new FiveGControllerImpl(provider.get());
    }

    public static FiveGControllerImpl_Factory create(Provider<Context> provider) {
        return new FiveGControllerImpl_Factory(provider);
    }
}
