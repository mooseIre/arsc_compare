package com.android.systemui.toast;

import android.content.Context;
import com.android.systemui.statusbar.CommandQueue;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ToastUI_Factory implements Factory<ToastUI> {
    private final Provider<CommandQueue> commandQueueProvider;
    private final Provider<Context> contextProvider;

    public ToastUI_Factory(Provider<Context> provider, Provider<CommandQueue> provider2) {
        this.contextProvider = provider;
        this.commandQueueProvider = provider2;
    }

    @Override // javax.inject.Provider
    public ToastUI get() {
        return provideInstance(this.contextProvider, this.commandQueueProvider);
    }

    public static ToastUI provideInstance(Provider<Context> provider, Provider<CommandQueue> provider2) {
        return new ToastUI(provider.get(), provider2.get());
    }

    public static ToastUI_Factory create(Provider<Context> provider, Provider<CommandQueue> provider2) {
        return new ToastUI_Factory(provider, provider2);
    }
}
