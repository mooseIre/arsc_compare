package com.android.systemui.statusbar.phone;

import android.content.Context;
import com.android.systemui.statusbar.CommandQueue;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class StatusBarIconControllerImpl_Factory implements Factory<StatusBarIconControllerImpl> {
    private final Provider<CommandQueue> commandQueueProvider;
    private final Provider<Context> contextProvider;

    public StatusBarIconControllerImpl_Factory(Provider<Context> provider, Provider<CommandQueue> provider2) {
        this.contextProvider = provider;
        this.commandQueueProvider = provider2;
    }

    @Override // javax.inject.Provider
    public StatusBarIconControllerImpl get() {
        return provideInstance(this.contextProvider, this.commandQueueProvider);
    }

    public static StatusBarIconControllerImpl provideInstance(Provider<Context> provider, Provider<CommandQueue> provider2) {
        return new StatusBarIconControllerImpl(provider.get(), provider2.get());
    }

    public static StatusBarIconControllerImpl_Factory create(Provider<Context> provider, Provider<CommandQueue> provider2) {
        return new StatusBarIconControllerImpl_Factory(provider, provider2);
    }
}
