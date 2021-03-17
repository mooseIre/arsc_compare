package com.android.systemui.accessibility;

import android.content.Context;
import android.os.Handler;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class WindowMagnification_Factory implements Factory<WindowMagnification> {
    private final Provider<Context> contextProvider;
    private final Provider<Handler> mainHandlerProvider;

    public WindowMagnification_Factory(Provider<Context> provider, Provider<Handler> provider2) {
        this.contextProvider = provider;
        this.mainHandlerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public WindowMagnification get() {
        return provideInstance(this.contextProvider, this.mainHandlerProvider);
    }

    public static WindowMagnification provideInstance(Provider<Context> provider, Provider<Handler> provider2) {
        return new WindowMagnification(provider.get(), provider2.get());
    }

    public static WindowMagnification_Factory create(Provider<Context> provider, Provider<Handler> provider2) {
        return new WindowMagnification_Factory(provider, provider2);
    }
}
