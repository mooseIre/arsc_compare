package com.android.systemui.media;

import android.content.Context;
import com.android.systemui.statusbar.policy.ConfigurationController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MediaViewController_Factory implements Factory<MediaViewController> {
    private final Provider<ConfigurationController> configurationControllerProvider;
    private final Provider<Context> contextProvider;
    private final Provider<MediaHostStatesManager> mediaHostStatesManagerProvider;

    public MediaViewController_Factory(Provider<Context> provider, Provider<ConfigurationController> provider2, Provider<MediaHostStatesManager> provider3) {
        this.contextProvider = provider;
        this.configurationControllerProvider = provider2;
        this.mediaHostStatesManagerProvider = provider3;
    }

    @Override // javax.inject.Provider
    public MediaViewController get() {
        return provideInstance(this.contextProvider, this.configurationControllerProvider, this.mediaHostStatesManagerProvider);
    }

    public static MediaViewController provideInstance(Provider<Context> provider, Provider<ConfigurationController> provider2, Provider<MediaHostStatesManager> provider3) {
        return new MediaViewController(provider.get(), provider2.get(), provider3.get());
    }

    public static MediaViewController_Factory create(Provider<Context> provider, Provider<ConfigurationController> provider2, Provider<MediaHostStatesManager> provider3) {
        return new MediaViewController_Factory(provider, provider2, provider3);
    }
}
