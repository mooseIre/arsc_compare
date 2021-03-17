package com.android.systemui.settings.dagger;

import android.content.Context;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.settings.CurrentUserContextTracker;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class SettingsModule_ProvideCurrentUserContextTrackerFactory implements Factory<CurrentUserContextTracker> {
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<Context> contextProvider;

    public SettingsModule_ProvideCurrentUserContextTrackerFactory(Provider<Context> provider, Provider<BroadcastDispatcher> provider2) {
        this.contextProvider = provider;
        this.broadcastDispatcherProvider = provider2;
    }

    @Override // javax.inject.Provider
    public CurrentUserContextTracker get() {
        return provideInstance(this.contextProvider, this.broadcastDispatcherProvider);
    }

    public static CurrentUserContextTracker provideInstance(Provider<Context> provider, Provider<BroadcastDispatcher> provider2) {
        return proxyProvideCurrentUserContextTracker(provider.get(), provider2.get());
    }

    public static SettingsModule_ProvideCurrentUserContextTrackerFactory create(Provider<Context> provider, Provider<BroadcastDispatcher> provider2) {
        return new SettingsModule_ProvideCurrentUserContextTrackerFactory(provider, provider2);
    }

    public static CurrentUserContextTracker proxyProvideCurrentUserContextTracker(Context context, BroadcastDispatcher broadcastDispatcher) {
        CurrentUserContextTracker provideCurrentUserContextTracker = SettingsModule.provideCurrentUserContextTracker(context, broadcastDispatcher);
        Preconditions.checkNotNull(provideCurrentUserContextTracker, "Cannot return null from a non-@Nullable @Provides method");
        return provideCurrentUserContextTracker;
    }
}
