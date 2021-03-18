package com.android.systemui.pip.tv;

import android.content.Context;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.pip.PipBoundsHandler;
import com.android.systemui.pip.PipSurfaceTransactionHelper;
import com.android.systemui.pip.PipTaskOrganizer;
import com.android.systemui.stackdivider.Divider;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class PipManager_Factory implements Factory<PipManager> {
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<Context> contextProvider;
    private final Provider<Divider> dividerProvider;
    private final Provider<PipBoundsHandler> pipBoundsHandlerProvider;
    private final Provider<PipTaskOrganizer> pipTaskOrganizerProvider;
    private final Provider<PipSurfaceTransactionHelper> surfaceTransactionHelperProvider;

    public PipManager_Factory(Provider<Context> provider, Provider<BroadcastDispatcher> provider2, Provider<PipBoundsHandler> provider3, Provider<PipTaskOrganizer> provider4, Provider<PipSurfaceTransactionHelper> provider5, Provider<Divider> provider6) {
        this.contextProvider = provider;
        this.broadcastDispatcherProvider = provider2;
        this.pipBoundsHandlerProvider = provider3;
        this.pipTaskOrganizerProvider = provider4;
        this.surfaceTransactionHelperProvider = provider5;
        this.dividerProvider = provider6;
    }

    @Override // javax.inject.Provider
    public PipManager get() {
        return provideInstance(this.contextProvider, this.broadcastDispatcherProvider, this.pipBoundsHandlerProvider, this.pipTaskOrganizerProvider, this.surfaceTransactionHelperProvider, this.dividerProvider);
    }

    public static PipManager provideInstance(Provider<Context> provider, Provider<BroadcastDispatcher> provider2, Provider<PipBoundsHandler> provider3, Provider<PipTaskOrganizer> provider4, Provider<PipSurfaceTransactionHelper> provider5, Provider<Divider> provider6) {
        return new PipManager(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get());
    }

    public static PipManager_Factory create(Provider<Context> provider, Provider<BroadcastDispatcher> provider2, Provider<PipBoundsHandler> provider3, Provider<PipTaskOrganizer> provider4, Provider<PipSurfaceTransactionHelper> provider5, Provider<Divider> provider6) {
        return new PipManager_Factory(provider, provider2, provider3, provider4, provider5, provider6);
    }
}
