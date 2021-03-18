package com.android.systemui.util;

import com.android.systemui.dagger.SystemUIRootComponent;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class InjectionInflationController_Factory implements Factory<InjectionInflationController> {
    private final Provider<SystemUIRootComponent> rootComponentProvider;

    public InjectionInflationController_Factory(Provider<SystemUIRootComponent> provider) {
        this.rootComponentProvider = provider;
    }

    @Override // javax.inject.Provider
    public InjectionInflationController get() {
        return provideInstance(this.rootComponentProvider);
    }

    public static InjectionInflationController provideInstance(Provider<SystemUIRootComponent> provider) {
        return new InjectionInflationController(provider.get());
    }

    public static InjectionInflationController_Factory create(Provider<SystemUIRootComponent> provider) {
        return new InjectionInflationController_Factory(provider);
    }
}
