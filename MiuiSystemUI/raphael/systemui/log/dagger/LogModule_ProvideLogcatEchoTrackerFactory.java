package com.android.systemui.log.dagger;

import android.content.ContentResolver;
import android.os.Looper;
import com.android.systemui.log.LogcatEchoTracker;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class LogModule_ProvideLogcatEchoTrackerFactory implements Factory<LogcatEchoTracker> {
    private final Provider<ContentResolver> contentResolverProvider;
    private final Provider<Looper> looperProvider;

    public LogModule_ProvideLogcatEchoTrackerFactory(Provider<ContentResolver> provider, Provider<Looper> provider2) {
        this.contentResolverProvider = provider;
        this.looperProvider = provider2;
    }

    @Override // javax.inject.Provider
    public LogcatEchoTracker get() {
        return provideInstance(this.contentResolverProvider, this.looperProvider);
    }

    public static LogcatEchoTracker provideInstance(Provider<ContentResolver> provider, Provider<Looper> provider2) {
        return proxyProvideLogcatEchoTracker(provider.get(), provider2.get());
    }

    public static LogModule_ProvideLogcatEchoTrackerFactory create(Provider<ContentResolver> provider, Provider<Looper> provider2) {
        return new LogModule_ProvideLogcatEchoTrackerFactory(provider, provider2);
    }

    public static LogcatEchoTracker proxyProvideLogcatEchoTracker(ContentResolver contentResolver, Looper looper) {
        LogcatEchoTracker provideLogcatEchoTracker = LogModule.provideLogcatEchoTracker(contentResolver, looper);
        Preconditions.checkNotNull(provideLogcatEchoTracker, "Cannot return null from a non-@Nullable @Provides method");
        return provideLogcatEchoTracker;
    }
}
