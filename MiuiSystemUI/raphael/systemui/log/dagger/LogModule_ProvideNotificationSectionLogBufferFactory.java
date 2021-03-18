package com.android.systemui.log.dagger;

import com.android.systemui.dump.DumpManager;
import com.android.systemui.log.LogBuffer;
import com.android.systemui.log.LogcatEchoTracker;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class LogModule_ProvideNotificationSectionLogBufferFactory implements Factory<LogBuffer> {
    private final Provider<LogcatEchoTracker> bufferFilterProvider;
    private final Provider<DumpManager> dumpManagerProvider;

    public LogModule_ProvideNotificationSectionLogBufferFactory(Provider<LogcatEchoTracker> provider, Provider<DumpManager> provider2) {
        this.bufferFilterProvider = provider;
        this.dumpManagerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public LogBuffer get() {
        return provideInstance(this.bufferFilterProvider, this.dumpManagerProvider);
    }

    public static LogBuffer provideInstance(Provider<LogcatEchoTracker> provider, Provider<DumpManager> provider2) {
        return proxyProvideNotificationSectionLogBuffer(provider.get(), provider2.get());
    }

    public static LogModule_ProvideNotificationSectionLogBufferFactory create(Provider<LogcatEchoTracker> provider, Provider<DumpManager> provider2) {
        return new LogModule_ProvideNotificationSectionLogBufferFactory(provider, provider2);
    }

    public static LogBuffer proxyProvideNotificationSectionLogBuffer(LogcatEchoTracker logcatEchoTracker, DumpManager dumpManager) {
        LogBuffer provideNotificationSectionLogBuffer = LogModule.provideNotificationSectionLogBuffer(logcatEchoTracker, dumpManager);
        Preconditions.checkNotNull(provideNotificationSectionLogBuffer, "Cannot return null from a non-@Nullable @Provides method");
        return provideNotificationSectionLogBuffer;
    }
}
