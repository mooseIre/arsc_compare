package com.android.systemui.globalactions;

import android.content.Context;
import com.android.systemui.statusbar.BlurUtils;
import com.android.systemui.statusbar.CommandQueue;
import dagger.internal.DoubleCheck;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class GlobalActionsImpl_Factory implements Factory<GlobalActionsImpl> {
    private final Provider<BlurUtils> blurUtilsProvider;
    private final Provider<CommandQueue> commandQueueProvider;
    private final Provider<Context> contextProvider;
    private final Provider<GlobalActionsDialog> globalActionsDialogLazyProvider;

    public GlobalActionsImpl_Factory(Provider<Context> provider, Provider<CommandQueue> provider2, Provider<GlobalActionsDialog> provider3, Provider<BlurUtils> provider4) {
        this.contextProvider = provider;
        this.commandQueueProvider = provider2;
        this.globalActionsDialogLazyProvider = provider3;
        this.blurUtilsProvider = provider4;
    }

    @Override // javax.inject.Provider
    public GlobalActionsImpl get() {
        return provideInstance(this.contextProvider, this.commandQueueProvider, this.globalActionsDialogLazyProvider, this.blurUtilsProvider);
    }

    public static GlobalActionsImpl provideInstance(Provider<Context> provider, Provider<CommandQueue> provider2, Provider<GlobalActionsDialog> provider3, Provider<BlurUtils> provider4) {
        return new GlobalActionsImpl(provider.get(), provider2.get(), DoubleCheck.lazy(provider3), provider4.get());
    }

    public static GlobalActionsImpl_Factory create(Provider<Context> provider, Provider<CommandQueue> provider2, Provider<GlobalActionsDialog> provider3, Provider<BlurUtils> provider4) {
        return new GlobalActionsImpl_Factory(provider, provider2, provider3, provider4);
    }
}
