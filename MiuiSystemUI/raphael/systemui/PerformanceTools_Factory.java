package com.android.systemui;

import android.content.Context;
import com.android.systemui.dump.DumpManager;
import dagger.internal.Factory;
import javax.inject.Provider;
import miui.systemui.performance.BinderMonitor;
import miui.systemui.performance.EvilMethodMonitor;
import miui.systemui.performance.FrameMonitor;
import miui.systemui.performance.MemoryMonitor;
import miui.systemui.performance.MessageMonitor;
import miui.systemui.performance.ViewLeakMonitor;

public final class PerformanceTools_Factory implements Factory<PerformanceTools> {
    private final Provider<BinderMonitor> binderMonitorProvider;
    private final Provider<Context> contextProvider;
    private final Provider<DumpManager> dumpManagerProvider;
    private final Provider<EvilMethodMonitor> evilMethodMonitorProvider;
    private final Provider<FrameMonitor> frameMonitorProvider;
    private final Provider<MemoryMonitor> memoryMonitorProvider;
    private final Provider<MessageMonitor> messageMonitorProvider;
    private final Provider<ViewLeakMonitor> viewLeakMonitorProvider;

    public PerformanceTools_Factory(Provider<Context> provider, Provider<BinderMonitor> provider2, Provider<EvilMethodMonitor> provider3, Provider<FrameMonitor> provider4, Provider<MemoryMonitor> provider5, Provider<MessageMonitor> provider6, Provider<ViewLeakMonitor> provider7, Provider<DumpManager> provider8) {
        this.contextProvider = provider;
        this.binderMonitorProvider = provider2;
        this.evilMethodMonitorProvider = provider3;
        this.frameMonitorProvider = provider4;
        this.memoryMonitorProvider = provider5;
        this.messageMonitorProvider = provider6;
        this.viewLeakMonitorProvider = provider7;
        this.dumpManagerProvider = provider8;
    }

    @Override // javax.inject.Provider
    public PerformanceTools get() {
        return provideInstance(this.contextProvider, this.binderMonitorProvider, this.evilMethodMonitorProvider, this.frameMonitorProvider, this.memoryMonitorProvider, this.messageMonitorProvider, this.viewLeakMonitorProvider, this.dumpManagerProvider);
    }

    public static PerformanceTools provideInstance(Provider<Context> provider, Provider<BinderMonitor> provider2, Provider<EvilMethodMonitor> provider3, Provider<FrameMonitor> provider4, Provider<MemoryMonitor> provider5, Provider<MessageMonitor> provider6, Provider<ViewLeakMonitor> provider7, Provider<DumpManager> provider8) {
        return new PerformanceTools(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get());
    }

    public static PerformanceTools_Factory create(Provider<Context> provider, Provider<BinderMonitor> provider2, Provider<EvilMethodMonitor> provider3, Provider<FrameMonitor> provider4, Provider<MemoryMonitor> provider5, Provider<MessageMonitor> provider6, Provider<ViewLeakMonitor> provider7, Provider<DumpManager> provider8) {
        return new PerformanceTools_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8);
    }
}
