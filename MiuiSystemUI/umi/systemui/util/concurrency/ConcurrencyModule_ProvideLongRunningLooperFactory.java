package com.android.systemui.util.concurrency;

import android.os.Looper;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

public final class ConcurrencyModule_ProvideLongRunningLooperFactory implements Factory<Looper> {
    private static final ConcurrencyModule_ProvideLongRunningLooperFactory INSTANCE = new ConcurrencyModule_ProvideLongRunningLooperFactory();

    @Override // javax.inject.Provider
    public Looper get() {
        return provideInstance();
    }

    public static Looper provideInstance() {
        return proxyProvideLongRunningLooper();
    }

    public static ConcurrencyModule_ProvideLongRunningLooperFactory create() {
        return INSTANCE;
    }

    public static Looper proxyProvideLongRunningLooper() {
        Looper provideLongRunningLooper = ConcurrencyModule.provideLongRunningLooper();
        Preconditions.checkNotNull(provideLongRunningLooper, "Cannot return null from a non-@Nullable @Provides method");
        return provideLongRunningLooper;
    }
}
