package com.android.systemui.wm;

import android.os.Handler;
import com.android.systemui.TransactionPool;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class DisplayImeController_Factory implements Factory<DisplayImeController> {
    private final Provider<DisplayController> displayControllerProvider;
    private final Provider<Handler> mainHandlerProvider;
    private final Provider<SystemWindows> syswinProvider;
    private final Provider<TransactionPool> transactionPoolProvider;

    public DisplayImeController_Factory(Provider<SystemWindows> provider, Provider<DisplayController> provider2, Provider<Handler> provider3, Provider<TransactionPool> provider4) {
        this.syswinProvider = provider;
        this.displayControllerProvider = provider2;
        this.mainHandlerProvider = provider3;
        this.transactionPoolProvider = provider4;
    }

    @Override // javax.inject.Provider
    public DisplayImeController get() {
        return provideInstance(this.syswinProvider, this.displayControllerProvider, this.mainHandlerProvider, this.transactionPoolProvider);
    }

    public static DisplayImeController provideInstance(Provider<SystemWindows> provider, Provider<DisplayController> provider2, Provider<Handler> provider3, Provider<TransactionPool> provider4) {
        return new DisplayImeController(provider.get(), provider2.get(), provider3.get(), provider4.get());
    }

    public static DisplayImeController_Factory create(Provider<SystemWindows> provider, Provider<DisplayController> provider2, Provider<Handler> provider3, Provider<TransactionPool> provider4) {
        return new DisplayImeController_Factory(provider, provider2, provider3, provider4);
    }
}
