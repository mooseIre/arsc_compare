package com.android.systemui;

import android.os.Handler;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.dump.DumpHandler;
import com.android.systemui.dump.LogBufferFreezer;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class SystemUIService_Factory implements Factory<SystemUIService> {
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<DumpHandler> dumpHandlerProvider;
    private final Provider<LogBufferFreezer> logBufferFreezerProvider;
    private final Provider<Handler> mainHandlerProvider;

    public SystemUIService_Factory(Provider<Handler> provider, Provider<DumpHandler> provider2, Provider<BroadcastDispatcher> provider3, Provider<LogBufferFreezer> provider4) {
        this.mainHandlerProvider = provider;
        this.dumpHandlerProvider = provider2;
        this.broadcastDispatcherProvider = provider3;
        this.logBufferFreezerProvider = provider4;
    }

    @Override // javax.inject.Provider
    public SystemUIService get() {
        return provideInstance(this.mainHandlerProvider, this.dumpHandlerProvider, this.broadcastDispatcherProvider, this.logBufferFreezerProvider);
    }

    public static SystemUIService provideInstance(Provider<Handler> provider, Provider<DumpHandler> provider2, Provider<BroadcastDispatcher> provider3, Provider<LogBufferFreezer> provider4) {
        return new SystemUIService(provider.get(), provider2.get(), provider3.get(), provider4.get());
    }

    public static SystemUIService_Factory create(Provider<Handler> provider, Provider<DumpHandler> provider2, Provider<BroadcastDispatcher> provider3, Provider<LogBufferFreezer> provider4) {
        return new SystemUIService_Factory(provider, provider2, provider3, provider4);
    }
}
