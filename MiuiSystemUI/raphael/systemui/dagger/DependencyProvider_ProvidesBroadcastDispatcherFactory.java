package com.android.systemui.dagger;

import android.content.Context;
import android.os.Looper;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.broadcast.logging.BroadcastDispatcherLogger;
import com.android.systemui.dump.DumpManager;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import java.util.concurrent.Executor;
import javax.inject.Provider;

public final class DependencyProvider_ProvidesBroadcastDispatcherFactory implements Factory<BroadcastDispatcher> {
    private final Provider<Executor> backgroundExecutorProvider;
    private final Provider<Looper> backgroundLooperProvider;
    private final Provider<Context> contextProvider;
    private final Provider<DumpManager> dumpManagerProvider;
    private final Provider<BroadcastDispatcherLogger> loggerProvider;
    private final DependencyProvider module;

    public DependencyProvider_ProvidesBroadcastDispatcherFactory(DependencyProvider dependencyProvider, Provider<Context> provider, Provider<Looper> provider2, Provider<Executor> provider3, Provider<DumpManager> provider4, Provider<BroadcastDispatcherLogger> provider5) {
        this.module = dependencyProvider;
        this.contextProvider = provider;
        this.backgroundLooperProvider = provider2;
        this.backgroundExecutorProvider = provider3;
        this.dumpManagerProvider = provider4;
        this.loggerProvider = provider5;
    }

    @Override // javax.inject.Provider
    public BroadcastDispatcher get() {
        return provideInstance(this.module, this.contextProvider, this.backgroundLooperProvider, this.backgroundExecutorProvider, this.dumpManagerProvider, this.loggerProvider);
    }

    public static BroadcastDispatcher provideInstance(DependencyProvider dependencyProvider, Provider<Context> provider, Provider<Looper> provider2, Provider<Executor> provider3, Provider<DumpManager> provider4, Provider<BroadcastDispatcherLogger> provider5) {
        return proxyProvidesBroadcastDispatcher(dependencyProvider, provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get());
    }

    public static DependencyProvider_ProvidesBroadcastDispatcherFactory create(DependencyProvider dependencyProvider, Provider<Context> provider, Provider<Looper> provider2, Provider<Executor> provider3, Provider<DumpManager> provider4, Provider<BroadcastDispatcherLogger> provider5) {
        return new DependencyProvider_ProvidesBroadcastDispatcherFactory(dependencyProvider, provider, provider2, provider3, provider4, provider5);
    }

    public static BroadcastDispatcher proxyProvidesBroadcastDispatcher(DependencyProvider dependencyProvider, Context context, Looper looper, Executor executor, DumpManager dumpManager, BroadcastDispatcherLogger broadcastDispatcherLogger) {
        BroadcastDispatcher providesBroadcastDispatcher = dependencyProvider.providesBroadcastDispatcher(context, looper, executor, dumpManager, broadcastDispatcherLogger);
        Preconditions.checkNotNull(providesBroadcastDispatcher, "Cannot return null from a non-@Nullable @Provides method");
        return providesBroadcastDispatcher;
    }
}
