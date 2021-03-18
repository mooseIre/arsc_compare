package com.android.systemui.statusbar.dagger;

import android.content.Context;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.tracing.ProtoTracer;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class StatusBarDependenciesModule_ProvideCommandQueueFactory implements Factory<CommandQueue> {
    private final Provider<Context> contextProvider;
    private final Provider<ProtoTracer> protoTracerProvider;

    public StatusBarDependenciesModule_ProvideCommandQueueFactory(Provider<Context> provider, Provider<ProtoTracer> provider2) {
        this.contextProvider = provider;
        this.protoTracerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public CommandQueue get() {
        return provideInstance(this.contextProvider, this.protoTracerProvider);
    }

    public static CommandQueue provideInstance(Provider<Context> provider, Provider<ProtoTracer> provider2) {
        return proxyProvideCommandQueue(provider.get(), provider2.get());
    }

    public static StatusBarDependenciesModule_ProvideCommandQueueFactory create(Provider<Context> provider, Provider<ProtoTracer> provider2) {
        return new StatusBarDependenciesModule_ProvideCommandQueueFactory(provider, provider2);
    }

    public static CommandQueue proxyProvideCommandQueue(Context context, ProtoTracer protoTracer) {
        CommandQueue provideCommandQueue = StatusBarDependenciesModule.provideCommandQueue(context, protoTracer);
        Preconditions.checkNotNull(provideCommandQueue, "Cannot return null from a non-@Nullable @Provides method");
        return provideCommandQueue;
    }
}
