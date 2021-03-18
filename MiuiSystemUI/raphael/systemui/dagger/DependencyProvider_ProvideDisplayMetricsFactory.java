package com.android.systemui.dagger;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class DependencyProvider_ProvideDisplayMetricsFactory implements Factory<DisplayMetrics> {
    private final Provider<Context> contextProvider;
    private final DependencyProvider module;
    private final Provider<WindowManager> windowManagerProvider;

    public DependencyProvider_ProvideDisplayMetricsFactory(DependencyProvider dependencyProvider, Provider<Context> provider, Provider<WindowManager> provider2) {
        this.module = dependencyProvider;
        this.contextProvider = provider;
        this.windowManagerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public DisplayMetrics get() {
        return provideInstance(this.module, this.contextProvider, this.windowManagerProvider);
    }

    public static DisplayMetrics provideInstance(DependencyProvider dependencyProvider, Provider<Context> provider, Provider<WindowManager> provider2) {
        return proxyProvideDisplayMetrics(dependencyProvider, provider.get(), provider2.get());
    }

    public static DependencyProvider_ProvideDisplayMetricsFactory create(DependencyProvider dependencyProvider, Provider<Context> provider, Provider<WindowManager> provider2) {
        return new DependencyProvider_ProvideDisplayMetricsFactory(dependencyProvider, provider, provider2);
    }

    public static DisplayMetrics proxyProvideDisplayMetrics(DependencyProvider dependencyProvider, Context context, WindowManager windowManager) {
        DisplayMetrics provideDisplayMetrics = dependencyProvider.provideDisplayMetrics(context, windowManager);
        Preconditions.checkNotNull(provideDisplayMetrics, "Cannot return null from a non-@Nullable @Provides method");
        return provideDisplayMetrics;
    }
}
