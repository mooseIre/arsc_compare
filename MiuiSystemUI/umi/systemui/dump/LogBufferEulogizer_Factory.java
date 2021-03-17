package com.android.systemui.dump;

import android.content.Context;
import com.android.systemui.util.io.Files;
import com.android.systemui.util.time.SystemClock;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class LogBufferEulogizer_Factory implements Factory<LogBufferEulogizer> {
    private final Provider<Context> contextProvider;
    private final Provider<DumpManager> dumpManagerProvider;
    private final Provider<Files> filesProvider;
    private final Provider<SystemClock> systemClockProvider;

    public LogBufferEulogizer_Factory(Provider<Context> provider, Provider<DumpManager> provider2, Provider<SystemClock> provider3, Provider<Files> provider4) {
        this.contextProvider = provider;
        this.dumpManagerProvider = provider2;
        this.systemClockProvider = provider3;
        this.filesProvider = provider4;
    }

    @Override // javax.inject.Provider
    public LogBufferEulogizer get() {
        return provideInstance(this.contextProvider, this.dumpManagerProvider, this.systemClockProvider, this.filesProvider);
    }

    public static LogBufferEulogizer provideInstance(Provider<Context> provider, Provider<DumpManager> provider2, Provider<SystemClock> provider3, Provider<Files> provider4) {
        return new LogBufferEulogizer(provider.get(), provider2.get(), provider3.get(), provider4.get());
    }

    public static LogBufferEulogizer_Factory create(Provider<Context> provider, Provider<DumpManager> provider2, Provider<SystemClock> provider3, Provider<Files> provider4) {
        return new LogBufferEulogizer_Factory(provider, provider2, provider3, provider4);
    }
}
