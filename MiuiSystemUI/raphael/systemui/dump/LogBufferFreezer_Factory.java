package com.android.systemui.dump;

import com.android.systemui.util.concurrency.DelayableExecutor;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class LogBufferFreezer_Factory implements Factory<LogBufferFreezer> {
    private final Provider<DumpManager> dumpManagerProvider;
    private final Provider<DelayableExecutor> executorProvider;

    public LogBufferFreezer_Factory(Provider<DumpManager> provider, Provider<DelayableExecutor> provider2) {
        this.dumpManagerProvider = provider;
        this.executorProvider = provider2;
    }

    @Override // javax.inject.Provider
    public LogBufferFreezer get() {
        return provideInstance(this.dumpManagerProvider, this.executorProvider);
    }

    public static LogBufferFreezer provideInstance(Provider<DumpManager> provider, Provider<DelayableExecutor> provider2) {
        return new LogBufferFreezer(provider.get(), provider2.get());
    }

    public static LogBufferFreezer_Factory create(Provider<DumpManager> provider, Provider<DelayableExecutor> provider2) {
        return new LogBufferFreezer_Factory(provider, provider2);
    }
}
