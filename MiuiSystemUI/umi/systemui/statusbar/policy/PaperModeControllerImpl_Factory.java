package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.os.Looper;
import com.android.systemui.broadcast.BroadcastDispatcher;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class PaperModeControllerImpl_Factory implements Factory<PaperModeControllerImpl> {
    private final Provider<Looper> bgLooperProvider;
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<Context> contextProvider;

    public PaperModeControllerImpl_Factory(Provider<Context> provider, Provider<Looper> provider2, Provider<BroadcastDispatcher> provider3) {
        this.contextProvider = provider;
        this.bgLooperProvider = provider2;
        this.broadcastDispatcherProvider = provider3;
    }

    @Override // javax.inject.Provider
    public PaperModeControllerImpl get() {
        return provideInstance(this.contextProvider, this.bgLooperProvider, this.broadcastDispatcherProvider);
    }

    public static PaperModeControllerImpl provideInstance(Provider<Context> provider, Provider<Looper> provider2, Provider<BroadcastDispatcher> provider3) {
        return new PaperModeControllerImpl(provider.get(), provider2.get(), provider3.get());
    }

    public static PaperModeControllerImpl_Factory create(Provider<Context> provider, Provider<Looper> provider2, Provider<BroadcastDispatcher> provider3) {
        return new PaperModeControllerImpl_Factory(provider, provider2, provider3);
    }
}
