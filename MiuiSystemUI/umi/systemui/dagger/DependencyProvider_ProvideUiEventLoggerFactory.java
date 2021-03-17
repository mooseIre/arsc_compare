package com.android.systemui.dagger;

import com.android.internal.logging.UiEventLogger;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

public final class DependencyProvider_ProvideUiEventLoggerFactory implements Factory<UiEventLogger> {
    private static final DependencyProvider_ProvideUiEventLoggerFactory INSTANCE = new DependencyProvider_ProvideUiEventLoggerFactory();

    @Override // javax.inject.Provider
    public UiEventLogger get() {
        return provideInstance();
    }

    public static UiEventLogger provideInstance() {
        return proxyProvideUiEventLogger();
    }

    public static DependencyProvider_ProvideUiEventLoggerFactory create() {
        return INSTANCE;
    }

    public static UiEventLogger proxyProvideUiEventLogger() {
        UiEventLogger provideUiEventLogger = DependencyProvider.provideUiEventLogger();
        Preconditions.checkNotNull(provideUiEventLogger, "Cannot return null from a non-@Nullable @Provides method");
        return provideUiEventLogger;
    }
}
