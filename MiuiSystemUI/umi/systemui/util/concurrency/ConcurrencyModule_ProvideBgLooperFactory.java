package com.android.systemui.util.concurrency;

import android.os.Looper;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

public final class ConcurrencyModule_ProvideBgLooperFactory implements Factory<Looper> {
    private static final ConcurrencyModule_ProvideBgLooperFactory INSTANCE = new ConcurrencyModule_ProvideBgLooperFactory();

    @Override // javax.inject.Provider
    public Looper get() {
        return provideInstance();
    }

    public static Looper provideInstance() {
        return proxyProvideBgLooper();
    }

    public static ConcurrencyModule_ProvideBgLooperFactory create() {
        return INSTANCE;
    }

    public static Looper proxyProvideBgLooper() {
        Looper provideBgLooper = ConcurrencyModule.provideBgLooper();
        Preconditions.checkNotNull(provideBgLooper, "Cannot return null from a non-@Nullable @Provides method");
        return provideBgLooper;
    }
}
