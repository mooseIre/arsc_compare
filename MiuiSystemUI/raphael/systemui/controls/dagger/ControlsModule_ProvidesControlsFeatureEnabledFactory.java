package com.android.systemui.controls.dagger;

import android.content.pm.PackageManager;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ControlsModule_ProvidesControlsFeatureEnabledFactory implements Factory<Boolean> {
    private final Provider<PackageManager> pmProvider;

    public ControlsModule_ProvidesControlsFeatureEnabledFactory(Provider<PackageManager> provider) {
        this.pmProvider = provider;
    }

    @Override // javax.inject.Provider
    public Boolean get() {
        return provideInstance(this.pmProvider);
    }

    public static Boolean provideInstance(Provider<PackageManager> provider) {
        return Boolean.valueOf(proxyProvidesControlsFeatureEnabled(provider.get()));
    }

    public static ControlsModule_ProvidesControlsFeatureEnabledFactory create(Provider<PackageManager> provider) {
        return new ControlsModule_ProvidesControlsFeatureEnabledFactory(provider);
    }

    public static boolean proxyProvidesControlsFeatureEnabled(PackageManager packageManager) {
        return ControlsModule.providesControlsFeatureEnabled(packageManager);
    }
}
