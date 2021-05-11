package com.android.systemui.util.concurrency;

import android.os.Looper;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

public final class ControlCenterConcurrencyModule_ProvideCCBgLooperFactory implements Factory<Looper> {
    private static final ControlCenterConcurrencyModule_ProvideCCBgLooperFactory INSTANCE = new ControlCenterConcurrencyModule_ProvideCCBgLooperFactory();

    @Override // javax.inject.Provider
    public Looper get() {
        return provideInstance();
    }

    public static Looper provideInstance() {
        return proxyProvideCCBgLooper();
    }

    public static ControlCenterConcurrencyModule_ProvideCCBgLooperFactory create() {
        return INSTANCE;
    }

    public static Looper proxyProvideCCBgLooper() {
        Looper provideCCBgLooper = ControlCenterConcurrencyModule.provideCCBgLooper();
        Preconditions.checkNotNull(provideCCBgLooper, "Cannot return null from a non-@Nullable @Provides method");
        return provideCCBgLooper;
    }
}
