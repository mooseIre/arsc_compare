package com.android.systemui.util.concurrency;

import android.os.Looper;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

public final class ConcurrencyModule_ProvideMainLooperFactory implements Factory<Looper> {
    private static final ConcurrencyModule_ProvideMainLooperFactory INSTANCE = new ConcurrencyModule_ProvideMainLooperFactory();

    @Override // javax.inject.Provider
    public Looper get() {
        return provideInstance();
    }

    public static Looper provideInstance() {
        return proxyProvideMainLooper();
    }

    public static ConcurrencyModule_ProvideMainLooperFactory create() {
        return INSTANCE;
    }

    public static Looper proxyProvideMainLooper() {
        Looper provideMainLooper = ConcurrencyModule.provideMainLooper();
        Preconditions.checkNotNull(provideMainLooper, "Cannot return null from a non-@Nullable @Provides method");
        return provideMainLooper;
    }
}
