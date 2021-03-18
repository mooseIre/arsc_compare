package com.android.systemui.colorextraction;

import android.content.Context;
import com.android.systemui.statusbar.policy.ConfigurationController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class SysuiColorExtractor_Factory implements Factory<SysuiColorExtractor> {
    private final Provider<ConfigurationController> configurationControllerProvider;
    private final Provider<Context> contextProvider;

    public SysuiColorExtractor_Factory(Provider<Context> provider, Provider<ConfigurationController> provider2) {
        this.contextProvider = provider;
        this.configurationControllerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public SysuiColorExtractor get() {
        return provideInstance(this.contextProvider, this.configurationControllerProvider);
    }

    public static SysuiColorExtractor provideInstance(Provider<Context> provider, Provider<ConfigurationController> provider2) {
        return new SysuiColorExtractor(provider.get(), provider2.get());
    }

    public static SysuiColorExtractor_Factory create(Provider<Context> provider, Provider<ConfigurationController> provider2) {
        return new SysuiColorExtractor_Factory(provider, provider2);
    }
}
