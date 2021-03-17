package com.android.systemui.tuner;

import android.content.Context;
import android.os.Handler;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.util.leak.LeakDetector;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class TunerServiceImpl_Factory implements Factory<TunerServiceImpl> {
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<Context> contextProvider;
    private final Provider<LeakDetector> leakDetectorProvider;
    private final Provider<Handler> mainHandlerProvider;

    public TunerServiceImpl_Factory(Provider<Context> provider, Provider<Handler> provider2, Provider<LeakDetector> provider3, Provider<BroadcastDispatcher> provider4) {
        this.contextProvider = provider;
        this.mainHandlerProvider = provider2;
        this.leakDetectorProvider = provider3;
        this.broadcastDispatcherProvider = provider4;
    }

    @Override // javax.inject.Provider
    public TunerServiceImpl get() {
        return provideInstance(this.contextProvider, this.mainHandlerProvider, this.leakDetectorProvider, this.broadcastDispatcherProvider);
    }

    public static TunerServiceImpl provideInstance(Provider<Context> provider, Provider<Handler> provider2, Provider<LeakDetector> provider3, Provider<BroadcastDispatcher> provider4) {
        return new TunerServiceImpl(provider.get(), provider2.get(), provider3.get(), provider4.get());
    }

    public static TunerServiceImpl_Factory create(Provider<Context> provider, Provider<Handler> provider2, Provider<LeakDetector> provider3, Provider<BroadcastDispatcher> provider4) {
        return new TunerServiceImpl_Factory(provider, provider2, provider3, provider4);
    }
}
