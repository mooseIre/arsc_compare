package com.android.systemui.pip;

import android.content.Context;
import com.android.systemui.statusbar.CommandQueue;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class PipUI_Factory implements Factory<PipUI> {
    private final Provider<CommandQueue> commandQueueProvider;
    private final Provider<Context> contextProvider;
    private final Provider<BasePipManager> pipManagerProvider;

    public PipUI_Factory(Provider<Context> provider, Provider<CommandQueue> provider2, Provider<BasePipManager> provider3) {
        this.contextProvider = provider;
        this.commandQueueProvider = provider2;
        this.pipManagerProvider = provider3;
    }

    @Override // javax.inject.Provider
    public PipUI get() {
        return provideInstance(this.contextProvider, this.commandQueueProvider, this.pipManagerProvider);
    }

    public static PipUI provideInstance(Provider<Context> provider, Provider<CommandQueue> provider2, Provider<BasePipManager> provider3) {
        return new PipUI(provider.get(), provider2.get(), provider3.get());
    }

    public static PipUI_Factory create(Provider<Context> provider, Provider<CommandQueue> provider2, Provider<BasePipManager> provider3) {
        return new PipUI_Factory(provider, provider2, provider3);
    }
}
