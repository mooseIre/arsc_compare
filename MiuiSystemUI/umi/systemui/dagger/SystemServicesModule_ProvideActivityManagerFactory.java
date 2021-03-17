package com.android.systemui.dagger;

import android.app.ActivityManager;
import android.content.Context;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class SystemServicesModule_ProvideActivityManagerFactory implements Factory<ActivityManager> {
    private final Provider<Context> contextProvider;

    public SystemServicesModule_ProvideActivityManagerFactory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public ActivityManager get() {
        return provideInstance(this.contextProvider);
    }

    public static ActivityManager provideInstance(Provider<Context> provider) {
        return proxyProvideActivityManager(provider.get());
    }

    public static SystemServicesModule_ProvideActivityManagerFactory create(Provider<Context> provider) {
        return new SystemServicesModule_ProvideActivityManagerFactory(provider);
    }

    public static ActivityManager proxyProvideActivityManager(Context context) {
        ActivityManager provideActivityManager = SystemServicesModule.provideActivityManager(context);
        Preconditions.checkNotNull(provideActivityManager, "Cannot return null from a non-@Nullable @Provides method");
        return provideActivityManager;
    }
}
