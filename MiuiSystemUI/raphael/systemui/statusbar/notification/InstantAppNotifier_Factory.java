package com.android.systemui.statusbar.notification;

import android.content.Context;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.statusbar.CommandQueue;
import dagger.internal.Factory;
import java.util.concurrent.Executor;
import javax.inject.Provider;

public final class InstantAppNotifier_Factory implements Factory<InstantAppNotifier> {
    private final Provider<CommandQueue> commandQueueProvider;
    private final Provider<Context> contextProvider;
    private final Provider<Divider> dividerProvider;
    private final Provider<Executor> uiBgExecutorProvider;

    public InstantAppNotifier_Factory(Provider<Context> provider, Provider<CommandQueue> provider2, Provider<Executor> provider3, Provider<Divider> provider4) {
        this.contextProvider = provider;
        this.commandQueueProvider = provider2;
        this.uiBgExecutorProvider = provider3;
        this.dividerProvider = provider4;
    }

    @Override // javax.inject.Provider
    public InstantAppNotifier get() {
        return provideInstance(this.contextProvider, this.commandQueueProvider, this.uiBgExecutorProvider, this.dividerProvider);
    }

    public static InstantAppNotifier provideInstance(Provider<Context> provider, Provider<CommandQueue> provider2, Provider<Executor> provider3, Provider<Divider> provider4) {
        return new InstantAppNotifier(provider.get(), provider2.get(), provider3.get(), provider4.get());
    }

    public static InstantAppNotifier_Factory create(Provider<Context> provider, Provider<CommandQueue> provider2, Provider<Executor> provider3, Provider<Divider> provider4) {
        return new InstantAppNotifier_Factory(provider, provider2, provider3, provider4);
    }
}
