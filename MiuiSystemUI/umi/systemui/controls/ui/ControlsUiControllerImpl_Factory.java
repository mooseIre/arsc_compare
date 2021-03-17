package com.android.systemui.controls.ui;

import android.content.Context;
import android.content.SharedPreferences;
import com.android.systemui.controls.controller.ControlsController;
import com.android.systemui.controls.management.ControlsListingController;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.statusbar.phone.ShadeController;
import com.android.systemui.util.concurrency.DelayableExecutor;
import dagger.internal.DoubleCheck;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ControlsUiControllerImpl_Factory implements Factory<ControlsUiControllerImpl> {
    private final Provider<ActivityStarter> activityStarterProvider;
    private final Provider<DelayableExecutor> bgExecutorProvider;
    private final Provider<Context> contextProvider;
    private final Provider<ControlActionCoordinator> controlActionCoordinatorProvider;
    private final Provider<ControlsController> controlsControllerProvider;
    private final Provider<ControlsListingController> controlsListingControllerProvider;
    private final Provider<ShadeController> shadeControllerProvider;
    private final Provider<SharedPreferences> sharedPreferencesProvider;
    private final Provider<DelayableExecutor> uiExecutorProvider;

    public ControlsUiControllerImpl_Factory(Provider<ControlsController> provider, Provider<Context> provider2, Provider<DelayableExecutor> provider3, Provider<DelayableExecutor> provider4, Provider<ControlsListingController> provider5, Provider<SharedPreferences> provider6, Provider<ControlActionCoordinator> provider7, Provider<ActivityStarter> provider8, Provider<ShadeController> provider9) {
        this.controlsControllerProvider = provider;
        this.contextProvider = provider2;
        this.uiExecutorProvider = provider3;
        this.bgExecutorProvider = provider4;
        this.controlsListingControllerProvider = provider5;
        this.sharedPreferencesProvider = provider6;
        this.controlActionCoordinatorProvider = provider7;
        this.activityStarterProvider = provider8;
        this.shadeControllerProvider = provider9;
    }

    @Override // javax.inject.Provider
    public ControlsUiControllerImpl get() {
        return provideInstance(this.controlsControllerProvider, this.contextProvider, this.uiExecutorProvider, this.bgExecutorProvider, this.controlsListingControllerProvider, this.sharedPreferencesProvider, this.controlActionCoordinatorProvider, this.activityStarterProvider, this.shadeControllerProvider);
    }

    public static ControlsUiControllerImpl provideInstance(Provider<ControlsController> provider, Provider<Context> provider2, Provider<DelayableExecutor> provider3, Provider<DelayableExecutor> provider4, Provider<ControlsListingController> provider5, Provider<SharedPreferences> provider6, Provider<ControlActionCoordinator> provider7, Provider<ActivityStarter> provider8, Provider<ShadeController> provider9) {
        return new ControlsUiControllerImpl(DoubleCheck.lazy(provider), provider2.get(), provider3.get(), provider4.get(), DoubleCheck.lazy(provider5), provider6.get(), provider7.get(), provider8.get(), provider9.get());
    }

    public static ControlsUiControllerImpl_Factory create(Provider<ControlsController> provider, Provider<Context> provider2, Provider<DelayableExecutor> provider3, Provider<DelayableExecutor> provider4, Provider<ControlsListingController> provider5, Provider<SharedPreferences> provider6, Provider<ControlActionCoordinator> provider7, Provider<ActivityStarter> provider8, Provider<ShadeController> provider9) {
        return new ControlsUiControllerImpl_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9);
    }
}
