package com.android.systemui.dagger;

import android.content.Context;
import android.content.pm.LauncherApps;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class SystemServicesModule_ProvideLauncherAppsFactory implements Factory<LauncherApps> {
    private final Provider<Context> contextProvider;

    public SystemServicesModule_ProvideLauncherAppsFactory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public LauncherApps get() {
        return provideInstance(this.contextProvider);
    }

    public static LauncherApps provideInstance(Provider<Context> provider) {
        return proxyProvideLauncherApps(provider.get());
    }

    public static SystemServicesModule_ProvideLauncherAppsFactory create(Provider<Context> provider) {
        return new SystemServicesModule_ProvideLauncherAppsFactory(provider);
    }

    public static LauncherApps proxyProvideLauncherApps(Context context) {
        LauncherApps provideLauncherApps = SystemServicesModule.provideLauncherApps(context);
        Preconditions.checkNotNull(provideLauncherApps, "Cannot return null from a non-@Nullable @Provides method");
        return provideLauncherApps;
    }
}
