package com.android.systemui.statusbar.notification.unimportant;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class UnimportantSdk_Factory implements Factory<UnimportantSdk> {
    private final Provider<Context> contextProvider;

    public UnimportantSdk_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public UnimportantSdk get() {
        return provideInstance(this.contextProvider);
    }

    public static UnimportantSdk provideInstance(Provider<Context> provider) {
        return new UnimportantSdk(provider.get());
    }

    public static UnimportantSdk_Factory create(Provider<Context> provider) {
        return new UnimportantSdk_Factory(provider);
    }
}
