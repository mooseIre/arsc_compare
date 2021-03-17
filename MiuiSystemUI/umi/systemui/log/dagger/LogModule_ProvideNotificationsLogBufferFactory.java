package com.android.systemui.log.dagger;

import com.android.systemui.dump.DumpManager;
import com.android.systemui.log.LogBuffer;
import com.android.systemui.log.LogcatEchoTracker;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class LogModule_ProvideNotificationsLogBufferFactory implements Factory<LogBuffer> {
    private final Provider<LogcatEchoTracker> bufferFilterProvider;
    private final Provider<DumpManager> dumpManagerProvider;

    public LogModule_ProvideNotificationsLogBufferFactory(Provider<LogcatEchoTracker> provider, Provider<DumpManager> provider2) {
        this.bufferFilterProvider = provider;
        this.dumpManagerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public LogBuffer get() {
        return provideInstance(this.bufferFilterProvider, this.dumpManagerProvider);
    }

    public static LogBuffer provideInstance(Provider<LogcatEchoTracker> provider, Provider<DumpManager> provider2) {
        return proxyProvideNotificationsLogBuffer(provider.get(), provider2.get());
    }

    public static LogModule_ProvideNotificationsLogBufferFactory create(Provider<LogcatEchoTracker> provider, Provider<DumpManager> provider2) {
        return new LogModule_ProvideNotificationsLogBufferFactory(provider, provider2);
    }

    public static LogBuffer proxyProvideNotificationsLogBuffer(LogcatEchoTracker logcatEchoTracker, DumpManager dumpManager) {
        LogBuffer provideNotificationsLogBuffer = LogModule.provideNotificationsLogBuffer(logcatEchoTracker, dumpManager);
        Preconditions.checkNotNull(provideNotificationsLogBuffer, "Cannot return null from a non-@Nullable @Provides method");
        return provideNotificationsLogBuffer;
    }
}
