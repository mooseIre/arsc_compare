package com.android.systemui.tracing;

import android.content.Context;
import com.android.systemui.dump.DumpManager;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ProtoTracer_Factory implements Factory<ProtoTracer> {
    private final Provider<Context> contextProvider;
    private final Provider<DumpManager> dumpManagerProvider;

    public ProtoTracer_Factory(Provider<Context> provider, Provider<DumpManager> provider2) {
        this.contextProvider = provider;
        this.dumpManagerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public ProtoTracer get() {
        return provideInstance(this.contextProvider, this.dumpManagerProvider);
    }

    public static ProtoTracer provideInstance(Provider<Context> provider, Provider<DumpManager> provider2) {
        return new ProtoTracer(provider.get(), provider2.get());
    }

    public static ProtoTracer_Factory create(Provider<Context> provider, Provider<DumpManager> provider2) {
        return new ProtoTracer_Factory(provider, provider2);
    }
}
