package com.android.systemui.doze;

import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.dump.DumpManager;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class DozeLog_Factory implements Factory<DozeLog> {
    private final Provider<DumpManager> dumpManagerProvider;
    private final Provider<KeyguardUpdateMonitor> keyguardUpdateMonitorProvider;
    private final Provider<DozeLogger> loggerProvider;

    public DozeLog_Factory(Provider<KeyguardUpdateMonitor> provider, Provider<DumpManager> provider2, Provider<DozeLogger> provider3) {
        this.keyguardUpdateMonitorProvider = provider;
        this.dumpManagerProvider = provider2;
        this.loggerProvider = provider3;
    }

    @Override // javax.inject.Provider
    public DozeLog get() {
        return provideInstance(this.keyguardUpdateMonitorProvider, this.dumpManagerProvider, this.loggerProvider);
    }

    public static DozeLog provideInstance(Provider<KeyguardUpdateMonitor> provider, Provider<DumpManager> provider2, Provider<DozeLogger> provider3) {
        return new DozeLog(provider.get(), provider2.get(), provider3.get());
    }

    public static DozeLog_Factory create(Provider<KeyguardUpdateMonitor> provider, Provider<DumpManager> provider2, Provider<DozeLogger> provider3) {
        return new DozeLog_Factory(provider, provider2, provider3);
    }
}
