package com.android.systemui.statusbar.notification.collection.coalescer;

import com.android.systemui.util.concurrency.DelayableExecutor;
import com.android.systemui.util.time.SystemClock;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class GroupCoalescer_Factory implements Factory<GroupCoalescer> {
    private final Provider<SystemClock> clockProvider;
    private final Provider<GroupCoalescerLogger> loggerProvider;
    private final Provider<DelayableExecutor> mainExecutorProvider;

    public GroupCoalescer_Factory(Provider<DelayableExecutor> provider, Provider<SystemClock> provider2, Provider<GroupCoalescerLogger> provider3) {
        this.mainExecutorProvider = provider;
        this.clockProvider = provider2;
        this.loggerProvider = provider3;
    }

    @Override // javax.inject.Provider
    public GroupCoalescer get() {
        return provideInstance(this.mainExecutorProvider, this.clockProvider, this.loggerProvider);
    }

    public static GroupCoalescer provideInstance(Provider<DelayableExecutor> provider, Provider<SystemClock> provider2, Provider<GroupCoalescerLogger> provider3) {
        return new GroupCoalescer(provider.get(), provider2.get(), provider3.get());
    }

    public static GroupCoalescer_Factory create(Provider<DelayableExecutor> provider, Provider<SystemClock> provider2, Provider<GroupCoalescerLogger> provider3) {
        return new GroupCoalescer_Factory(provider, provider2, provider3);
    }
}
