package com.android.systemui.recents;

import android.content.Context;
import com.android.systemui.statusbar.CommandQueue;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiFullScreenGestureProxy_Factory implements Factory<MiuiFullScreenGestureProxy> {
    private final Provider<CommandQueue> commandQueueProvider;
    private final Provider<Context> contextProvider;

    public MiuiFullScreenGestureProxy_Factory(Provider<Context> provider, Provider<CommandQueue> provider2) {
        this.contextProvider = provider;
        this.commandQueueProvider = provider2;
    }

    @Override // javax.inject.Provider
    public MiuiFullScreenGestureProxy get() {
        return provideInstance(this.contextProvider, this.commandQueueProvider);
    }

    public static MiuiFullScreenGestureProxy provideInstance(Provider<Context> provider, Provider<CommandQueue> provider2) {
        return new MiuiFullScreenGestureProxy(provider.get(), provider2.get());
    }

    public static MiuiFullScreenGestureProxy_Factory create(Provider<Context> provider, Provider<CommandQueue> provider2) {
        return new MiuiFullScreenGestureProxy_Factory(provider, provider2);
    }
}
