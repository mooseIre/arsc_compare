package com.android.systemui.controls.controller;

import android.content.Context;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.controls.management.ControlsListingController;
import com.android.systemui.controls.ui.ControlsUiController;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.util.concurrency.DelayableExecutor;
import dagger.internal.Factory;
import java.util.Optional;
import javax.inject.Provider;

public final class ControlsControllerImpl_Factory implements Factory<ControlsControllerImpl> {
    private final Provider<ControlsBindingController> bindingControllerProvider;
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<Context> contextProvider;
    private final Provider<DumpManager> dumpManagerProvider;
    private final Provider<DelayableExecutor> executorProvider;
    private final Provider<ControlsListingController> listingControllerProvider;
    private final Provider<Optional<ControlsFavoritePersistenceWrapper>> optionalWrapperProvider;
    private final Provider<ControlsUiController> uiControllerProvider;

    public ControlsControllerImpl_Factory(Provider<Context> provider, Provider<DelayableExecutor> provider2, Provider<ControlsUiController> provider3, Provider<ControlsBindingController> provider4, Provider<ControlsListingController> provider5, Provider<BroadcastDispatcher> provider6, Provider<Optional<ControlsFavoritePersistenceWrapper>> provider7, Provider<DumpManager> provider8) {
        this.contextProvider = provider;
        this.executorProvider = provider2;
        this.uiControllerProvider = provider3;
        this.bindingControllerProvider = provider4;
        this.listingControllerProvider = provider5;
        this.broadcastDispatcherProvider = provider6;
        this.optionalWrapperProvider = provider7;
        this.dumpManagerProvider = provider8;
    }

    @Override // javax.inject.Provider
    public ControlsControllerImpl get() {
        return provideInstance(this.contextProvider, this.executorProvider, this.uiControllerProvider, this.bindingControllerProvider, this.listingControllerProvider, this.broadcastDispatcherProvider, this.optionalWrapperProvider, this.dumpManagerProvider);
    }

    public static ControlsControllerImpl provideInstance(Provider<Context> provider, Provider<DelayableExecutor> provider2, Provider<ControlsUiController> provider3, Provider<ControlsBindingController> provider4, Provider<ControlsListingController> provider5, Provider<BroadcastDispatcher> provider6, Provider<Optional<ControlsFavoritePersistenceWrapper>> provider7, Provider<DumpManager> provider8) {
        return new ControlsControllerImpl(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get());
    }

    public static ControlsControllerImpl_Factory create(Provider<Context> provider, Provider<DelayableExecutor> provider2, Provider<ControlsUiController> provider3, Provider<ControlsBindingController> provider4, Provider<ControlsListingController> provider5, Provider<BroadcastDispatcher> provider6, Provider<Optional<ControlsFavoritePersistenceWrapper>> provider7, Provider<DumpManager> provider8) {
        return new ControlsControllerImpl_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8);
    }
}
