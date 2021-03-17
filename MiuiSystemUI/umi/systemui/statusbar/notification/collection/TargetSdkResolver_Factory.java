package com.android.systemui.statusbar.notification.collection;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class TargetSdkResolver_Factory implements Factory<TargetSdkResolver> {
    private final Provider<Context> contextProvider;

    public TargetSdkResolver_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public TargetSdkResolver get() {
        return provideInstance(this.contextProvider);
    }

    public static TargetSdkResolver provideInstance(Provider<Context> provider) {
        return new TargetSdkResolver(provider.get());
    }

    public static TargetSdkResolver_Factory create(Provider<Context> provider) {
        return new TargetSdkResolver_Factory(provider);
    }
}
