package com.android.systemui.bubbles.storage;

import android.content.pm.LauncherApps;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class BubbleVolatileRepository_Factory implements Factory<BubbleVolatileRepository> {
    private final Provider<LauncherApps> launcherAppsProvider;

    public BubbleVolatileRepository_Factory(Provider<LauncherApps> provider) {
        this.launcherAppsProvider = provider;
    }

    @Override // javax.inject.Provider
    public BubbleVolatileRepository get() {
        return provideInstance(this.launcherAppsProvider);
    }

    public static BubbleVolatileRepository provideInstance(Provider<LauncherApps> provider) {
        return new BubbleVolatileRepository(provider.get());
    }

    public static BubbleVolatileRepository_Factory create(Provider<LauncherApps> provider) {
        return new BubbleVolatileRepository_Factory(provider);
    }
}
