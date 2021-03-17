package com.android.systemui.dump;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class DumpHandler_Factory implements Factory<DumpHandler> {
    private final Provider<Context> contextProvider;
    private final Provider<DumpManager> dumpManagerProvider;
    private final Provider<LogBufferEulogizer> logBufferEulogizerProvider;

    public DumpHandler_Factory(Provider<Context> provider, Provider<DumpManager> provider2, Provider<LogBufferEulogizer> provider3) {
        this.contextProvider = provider;
        this.dumpManagerProvider = provider2;
        this.logBufferEulogizerProvider = provider3;
    }

    @Override // javax.inject.Provider
    public DumpHandler get() {
        return provideInstance(this.contextProvider, this.dumpManagerProvider, this.logBufferEulogizerProvider);
    }

    public static DumpHandler provideInstance(Provider<Context> provider, Provider<DumpManager> provider2, Provider<LogBufferEulogizer> provider3) {
        return new DumpHandler(provider.get(), provider2.get(), provider3.get());
    }

    public static DumpHandler_Factory create(Provider<Context> provider, Provider<DumpManager> provider2, Provider<LogBufferEulogizer> provider3) {
        return new DumpHandler_Factory(provider, provider2, provider3);
    }
}
