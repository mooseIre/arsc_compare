package com.android.systemui.dagger;

import com.android.systemui.dump.DumpManager;
import com.android.systemui.log.LogBuffer;
import com.android.systemui.log.LogcatEchoTracker;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class MiuiLogModule_ProvideMediaControlLogBufferFactory implements Factory<LogBuffer> {
    private final Provider<LogcatEchoTracker> bufferFilterProvider;
    private final Provider<DumpManager> dumpManagerProvider;

    public MiuiLogModule_ProvideMediaControlLogBufferFactory(Provider<LogcatEchoTracker> provider, Provider<DumpManager> provider2) {
        this.bufferFilterProvider = provider;
        this.dumpManagerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public LogBuffer get() {
        return provideInstance(this.bufferFilterProvider, this.dumpManagerProvider);
    }

    public static LogBuffer provideInstance(Provider<LogcatEchoTracker> provider, Provider<DumpManager> provider2) {
        return proxyProvideMediaControlLogBuffer(provider.get(), provider2.get());
    }

    public static MiuiLogModule_ProvideMediaControlLogBufferFactory create(Provider<LogcatEchoTracker> provider, Provider<DumpManager> provider2) {
        return new MiuiLogModule_ProvideMediaControlLogBufferFactory(provider, provider2);
    }

    public static LogBuffer proxyProvideMediaControlLogBuffer(LogcatEchoTracker logcatEchoTracker, DumpManager dumpManager) {
        LogBuffer provideMediaControlLogBuffer = MiuiLogModule.provideMediaControlLogBuffer(logcatEchoTracker, dumpManager);
        Preconditions.checkNotNull(provideMediaControlLogBuffer, "Cannot return null from a non-@Nullable @Provides method");
        return provideMediaControlLogBuffer;
    }
}
