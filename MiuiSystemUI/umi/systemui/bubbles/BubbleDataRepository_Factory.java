package com.android.systemui.bubbles;

import android.content.pm.LauncherApps;
import com.android.systemui.bubbles.storage.BubblePersistentRepository;
import com.android.systemui.bubbles.storage.BubbleVolatileRepository;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class BubbleDataRepository_Factory implements Factory<BubbleDataRepository> {
    private final Provider<LauncherApps> launcherAppsProvider;
    private final Provider<BubblePersistentRepository> persistentRepositoryProvider;
    private final Provider<BubbleVolatileRepository> volatileRepositoryProvider;

    public BubbleDataRepository_Factory(Provider<BubbleVolatileRepository> provider, Provider<BubblePersistentRepository> provider2, Provider<LauncherApps> provider3) {
        this.volatileRepositoryProvider = provider;
        this.persistentRepositoryProvider = provider2;
        this.launcherAppsProvider = provider3;
    }

    @Override // javax.inject.Provider
    public BubbleDataRepository get() {
        return provideInstance(this.volatileRepositoryProvider, this.persistentRepositoryProvider, this.launcherAppsProvider);
    }

    public static BubbleDataRepository provideInstance(Provider<BubbleVolatileRepository> provider, Provider<BubblePersistentRepository> provider2, Provider<LauncherApps> provider3) {
        return new BubbleDataRepository(provider.get(), provider2.get(), provider3.get());
    }

    public static BubbleDataRepository_Factory create(Provider<BubbleVolatileRepository> provider, Provider<BubblePersistentRepository> provider2, Provider<LauncherApps> provider3) {
        return new BubbleDataRepository_Factory(provider, provider2, provider3);
    }
}
