package com.android.systemui.dagger;

import android.content.Context;
import com.android.internal.util.NotificationMessagingUtil;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class DependencyProvider_ProvideNotificationMessagingUtilFactory implements Factory<NotificationMessagingUtil> {
    private final Provider<Context> contextProvider;
    private final DependencyProvider module;

    public DependencyProvider_ProvideNotificationMessagingUtilFactory(DependencyProvider dependencyProvider, Provider<Context> provider) {
        this.module = dependencyProvider;
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public NotificationMessagingUtil get() {
        return provideInstance(this.module, this.contextProvider);
    }

    public static NotificationMessagingUtil provideInstance(DependencyProvider dependencyProvider, Provider<Context> provider) {
        return proxyProvideNotificationMessagingUtil(dependencyProvider, provider.get());
    }

    public static DependencyProvider_ProvideNotificationMessagingUtilFactory create(DependencyProvider dependencyProvider, Provider<Context> provider) {
        return new DependencyProvider_ProvideNotificationMessagingUtilFactory(dependencyProvider, provider);
    }

    public static NotificationMessagingUtil proxyProvideNotificationMessagingUtil(DependencyProvider dependencyProvider, Context context) {
        NotificationMessagingUtil provideNotificationMessagingUtil = dependencyProvider.provideNotificationMessagingUtil(context);
        Preconditions.checkNotNull(provideNotificationMessagingUtil, "Cannot return null from a non-@Nullable @Provides method");
        return provideNotificationMessagingUtil;
    }
}
