package com.android.keyguard.injector;

import android.content.Context;
import com.android.systemui.dump.DumpManager;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class KeyguardBottomAreaInjector_Factory implements Factory<KeyguardBottomAreaInjector> {
    private final Provider<DumpManager> dumpManagerProvider;
    private final Provider<Context> mContextProvider;

    public KeyguardBottomAreaInjector_Factory(Provider<Context> provider, Provider<DumpManager> provider2) {
        this.mContextProvider = provider;
        this.dumpManagerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public KeyguardBottomAreaInjector get() {
        return provideInstance(this.mContextProvider, this.dumpManagerProvider);
    }

    public static KeyguardBottomAreaInjector provideInstance(Provider<Context> provider, Provider<DumpManager> provider2) {
        return new KeyguardBottomAreaInjector(provider.get(), provider2.get());
    }

    public static KeyguardBottomAreaInjector_Factory create(Provider<Context> provider, Provider<DumpManager> provider2) {
        return new KeyguardBottomAreaInjector_Factory(provider, provider2);
    }
}
