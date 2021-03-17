package com.android.systemui.util.leak;

import android.content.Context;
import com.android.systemui.util.leak.GarbageMonitor;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class GarbageMonitor_Service_Factory implements Factory<GarbageMonitor.Service> {
    private final Provider<Context> contextProvider;
    private final Provider<GarbageMonitor> garbageMonitorProvider;

    public GarbageMonitor_Service_Factory(Provider<Context> provider, Provider<GarbageMonitor> provider2) {
        this.contextProvider = provider;
        this.garbageMonitorProvider = provider2;
    }

    @Override // javax.inject.Provider
    public GarbageMonitor.Service get() {
        return provideInstance(this.contextProvider, this.garbageMonitorProvider);
    }

    public static GarbageMonitor.Service provideInstance(Provider<Context> provider, Provider<GarbageMonitor> provider2) {
        return new GarbageMonitor.Service(provider.get(), provider2.get());
    }

    public static GarbageMonitor_Service_Factory create(Provider<Context> provider, Provider<GarbageMonitor> provider2) {
        return new GarbageMonitor_Service_Factory(provider, provider2);
    }
}
