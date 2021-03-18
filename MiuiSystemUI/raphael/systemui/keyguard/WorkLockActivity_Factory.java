package com.android.systemui.keyguard;

import com.android.systemui.broadcast.BroadcastDispatcher;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class WorkLockActivity_Factory implements Factory<WorkLockActivity> {
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;

    public WorkLockActivity_Factory(Provider<BroadcastDispatcher> provider) {
        this.broadcastDispatcherProvider = provider;
    }

    @Override // javax.inject.Provider
    public WorkLockActivity get() {
        return provideInstance(this.broadcastDispatcherProvider);
    }

    public static WorkLockActivity provideInstance(Provider<BroadcastDispatcher> provider) {
        return new WorkLockActivity(provider.get());
    }

    public static WorkLockActivity_Factory create(Provider<BroadcastDispatcher> provider) {
        return new WorkLockActivity_Factory(provider);
    }
}
