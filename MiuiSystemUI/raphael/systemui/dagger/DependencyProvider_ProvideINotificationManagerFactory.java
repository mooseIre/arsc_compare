package com.android.systemui.dagger;

import android.app.INotificationManager;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

public final class DependencyProvider_ProvideINotificationManagerFactory implements Factory<INotificationManager> {
    private final DependencyProvider module;

    public DependencyProvider_ProvideINotificationManagerFactory(DependencyProvider dependencyProvider) {
        this.module = dependencyProvider;
    }

    @Override // javax.inject.Provider
    public INotificationManager get() {
        return provideInstance(this.module);
    }

    public static INotificationManager provideInstance(DependencyProvider dependencyProvider) {
        return proxyProvideINotificationManager(dependencyProvider);
    }

    public static DependencyProvider_ProvideINotificationManagerFactory create(DependencyProvider dependencyProvider) {
        return new DependencyProvider_ProvideINotificationManagerFactory(dependencyProvider);
    }

    public static INotificationManager proxyProvideINotificationManager(DependencyProvider dependencyProvider) {
        INotificationManager provideINotificationManager = dependencyProvider.provideINotificationManager();
        Preconditions.checkNotNull(provideINotificationManager, "Cannot return null from a non-@Nullable @Provides method");
        return provideINotificationManager;
    }
}
