package com.android.systemui.log.dagger;

import com.android.systemui.dump.DumpManager;
import com.android.systemui.log.LogBuffer;
import com.android.systemui.log.LogcatEchoTracker;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class LogModule_ProvideNotifInteractionLogBufferFactory implements Factory<LogBuffer> {
    private final Provider<DumpManager> dumpManagerProvider;
    private final Provider<LogcatEchoTracker> echoTrackerProvider;

    public LogModule_ProvideNotifInteractionLogBufferFactory(Provider<LogcatEchoTracker> provider, Provider<DumpManager> provider2) {
        this.echoTrackerProvider = provider;
        this.dumpManagerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public LogBuffer get() {
        return provideInstance(this.echoTrackerProvider, this.dumpManagerProvider);
    }

    public static LogBuffer provideInstance(Provider<LogcatEchoTracker> provider, Provider<DumpManager> provider2) {
        return proxyProvideNotifInteractionLogBuffer(provider.get(), provider2.get());
    }

    public static LogModule_ProvideNotifInteractionLogBufferFactory create(Provider<LogcatEchoTracker> provider, Provider<DumpManager> provider2) {
        return new LogModule_ProvideNotifInteractionLogBufferFactory(provider, provider2);
    }

    public static LogBuffer proxyProvideNotifInteractionLogBuffer(LogcatEchoTracker logcatEchoTracker, DumpManager dumpManager) {
        LogBuffer provideNotifInteractionLogBuffer = LogModule.provideNotifInteractionLogBuffer(logcatEchoTracker, dumpManager);
        Preconditions.checkNotNull(provideNotifInteractionLogBuffer, "Cannot return null from a non-@Nullable @Provides method");
        return provideNotifInteractionLogBuffer;
    }
}
