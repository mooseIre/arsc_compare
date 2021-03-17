package com.android.systemui;

import com.miui.systemui.MemoryMonitor;
import com.miui.systemui.ViewLeakMonitor;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class PerformanceTools_Factory implements Factory<PerformanceTools> {
    private final Provider<MemoryMonitor> memoryMonitorProvider;
    private final Provider<ViewLeakMonitor> viewLeakMonitorProvider;

    public PerformanceTools_Factory(Provider<ViewLeakMonitor> provider, Provider<MemoryMonitor> provider2) {
        this.viewLeakMonitorProvider = provider;
        this.memoryMonitorProvider = provider2;
    }

    @Override // javax.inject.Provider
    public PerformanceTools get() {
        return provideInstance(this.viewLeakMonitorProvider, this.memoryMonitorProvider);
    }

    public static PerformanceTools provideInstance(Provider<ViewLeakMonitor> provider, Provider<MemoryMonitor> provider2) {
        return new PerformanceTools(provider.get(), provider2.get());
    }

    public static PerformanceTools_Factory create(Provider<ViewLeakMonitor> provider, Provider<MemoryMonitor> provider2) {
        return new PerformanceTools_Factory(provider, provider2);
    }
}
