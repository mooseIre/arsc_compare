package com.android.systemui.log.dagger;

import com.android.systemui.dump.DumpManager;
import com.android.systemui.log.LogBuffer;
import com.android.systemui.log.LogcatEchoTracker;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class LogModule_ProvideQuickSettingsLogBufferFactory implements Factory<LogBuffer> {
    private final Provider<LogcatEchoTracker> bufferFilterProvider;
    private final Provider<DumpManager> dumpManagerProvider;

    public LogModule_ProvideQuickSettingsLogBufferFactory(Provider<LogcatEchoTracker> provider, Provider<DumpManager> provider2) {
        this.bufferFilterProvider = provider;
        this.dumpManagerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public LogBuffer get() {
        return provideInstance(this.bufferFilterProvider, this.dumpManagerProvider);
    }

    public static LogBuffer provideInstance(Provider<LogcatEchoTracker> provider, Provider<DumpManager> provider2) {
        return proxyProvideQuickSettingsLogBuffer(provider.get(), provider2.get());
    }

    public static LogModule_ProvideQuickSettingsLogBufferFactory create(Provider<LogcatEchoTracker> provider, Provider<DumpManager> provider2) {
        return new LogModule_ProvideQuickSettingsLogBufferFactory(provider, provider2);
    }

    public static LogBuffer proxyProvideQuickSettingsLogBuffer(LogcatEchoTracker logcatEchoTracker, DumpManager dumpManager) {
        LogBuffer provideQuickSettingsLogBuffer = LogModule.provideQuickSettingsLogBuffer(logcatEchoTracker, dumpManager);
        Preconditions.checkNotNull(provideQuickSettingsLogBuffer, "Cannot return null from a non-@Nullable @Provides method");
        return provideQuickSettingsLogBuffer;
    }
}
