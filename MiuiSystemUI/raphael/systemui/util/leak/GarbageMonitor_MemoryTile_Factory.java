package com.android.systemui.util.leak;

import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.qs.QSHost;
import com.android.systemui.util.leak.GarbageMonitor;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class GarbageMonitor_MemoryTile_Factory implements Factory<GarbageMonitor.MemoryTile> {
    private final Provider<QSHost> hostProvider;
    private final Provider<GarbageMonitor> monitorProvider;
    private final Provider<ActivityStarter> starterProvider;

    public GarbageMonitor_MemoryTile_Factory(Provider<QSHost> provider, Provider<GarbageMonitor> provider2, Provider<ActivityStarter> provider3) {
        this.hostProvider = provider;
        this.monitorProvider = provider2;
        this.starterProvider = provider3;
    }

    @Override // javax.inject.Provider
    public GarbageMonitor.MemoryTile get() {
        return provideInstance(this.hostProvider, this.monitorProvider, this.starterProvider);
    }

    public static GarbageMonitor.MemoryTile provideInstance(Provider<QSHost> provider, Provider<GarbageMonitor> provider2, Provider<ActivityStarter> provider3) {
        return new GarbageMonitor.MemoryTile(provider.get(), provider2.get(), provider3.get());
    }

    public static GarbageMonitor_MemoryTile_Factory create(Provider<QSHost> provider, Provider<GarbageMonitor> provider2, Provider<ActivityStarter> provider3) {
        return new GarbageMonitor_MemoryTile_Factory(provider, provider2, provider3);
    }
}
