package com.android.systemui.dagger;

import com.android.internal.logging.MetricsLogger;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

public final class DependencyProvider_ProvideMetricsLoggerFactory implements Factory<MetricsLogger> {
    private final DependencyProvider module;

    public DependencyProvider_ProvideMetricsLoggerFactory(DependencyProvider dependencyProvider) {
        this.module = dependencyProvider;
    }

    @Override // javax.inject.Provider
    public MetricsLogger get() {
        return provideInstance(this.module);
    }

    public static MetricsLogger provideInstance(DependencyProvider dependencyProvider) {
        return proxyProvideMetricsLogger(dependencyProvider);
    }

    public static DependencyProvider_ProvideMetricsLoggerFactory create(DependencyProvider dependencyProvider) {
        return new DependencyProvider_ProvideMetricsLoggerFactory(dependencyProvider);
    }

    public static MetricsLogger proxyProvideMetricsLogger(DependencyProvider dependencyProvider) {
        MetricsLogger provideMetricsLogger = dependencyProvider.provideMetricsLogger();
        Preconditions.checkNotNull(provideMetricsLogger, "Cannot return null from a non-@Nullable @Provides method");
        return provideMetricsLogger;
    }
}
