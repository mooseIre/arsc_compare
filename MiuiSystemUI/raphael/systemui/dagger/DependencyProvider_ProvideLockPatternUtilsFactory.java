package com.android.systemui.dagger;

import android.content.Context;
import com.android.internal.widget.LockPatternUtils;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class DependencyProvider_ProvideLockPatternUtilsFactory implements Factory<LockPatternUtils> {
    private final Provider<Context> contextProvider;
    private final DependencyProvider module;

    public DependencyProvider_ProvideLockPatternUtilsFactory(DependencyProvider dependencyProvider, Provider<Context> provider) {
        this.module = dependencyProvider;
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public LockPatternUtils get() {
        return provideInstance(this.module, this.contextProvider);
    }

    public static LockPatternUtils provideInstance(DependencyProvider dependencyProvider, Provider<Context> provider) {
        return proxyProvideLockPatternUtils(dependencyProvider, provider.get());
    }

    public static DependencyProvider_ProvideLockPatternUtilsFactory create(DependencyProvider dependencyProvider, Provider<Context> provider) {
        return new DependencyProvider_ProvideLockPatternUtilsFactory(dependencyProvider, provider);
    }

    public static LockPatternUtils proxyProvideLockPatternUtils(DependencyProvider dependencyProvider, Context context) {
        LockPatternUtils provideLockPatternUtils = dependencyProvider.provideLockPatternUtils(context);
        Preconditions.checkNotNull(provideLockPatternUtils, "Cannot return null from a non-@Nullable @Provides method");
        return provideLockPatternUtils;
    }
}
