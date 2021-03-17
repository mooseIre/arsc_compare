package com.android.systemui.wm;

import android.content.Context;
import android.view.IWindowManager;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class SystemWindows_Factory implements Factory<SystemWindows> {
    private final Provider<Context> contextProvider;
    private final Provider<DisplayController> displayControllerProvider;
    private final Provider<IWindowManager> wmServiceProvider;

    public SystemWindows_Factory(Provider<Context> provider, Provider<DisplayController> provider2, Provider<IWindowManager> provider3) {
        this.contextProvider = provider;
        this.displayControllerProvider = provider2;
        this.wmServiceProvider = provider3;
    }

    @Override // javax.inject.Provider
    public SystemWindows get() {
        return provideInstance(this.contextProvider, this.displayControllerProvider, this.wmServiceProvider);
    }

    public static SystemWindows provideInstance(Provider<Context> provider, Provider<DisplayController> provider2, Provider<IWindowManager> provider3) {
        return new SystemWindows(provider.get(), provider2.get(), provider3.get());
    }

    public static SystemWindows_Factory create(Provider<Context> provider, Provider<DisplayController> provider2, Provider<IWindowManager> provider3) {
        return new SystemWindows_Factory(provider, provider2, provider3);
    }
}
