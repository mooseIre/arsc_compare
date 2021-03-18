package com.android.systemui.controls.dagger;

import com.android.systemui.controls.controller.ControlsController;
import com.android.systemui.controls.management.ControlsListingController;
import com.android.systemui.controls.ui.ControlsUiController;
import dagger.internal.DoubleCheck;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ControlsComponent_Factory implements Factory<ControlsComponent> {
    private final Provider<ControlsController> controlsControllerProvider;
    private final Provider<ControlsListingController> controlsListingControllerProvider;
    private final Provider<ControlsUiController> controlsUiControllerProvider;
    private final Provider<Boolean> featureEnabledProvider;

    public ControlsComponent_Factory(Provider<Boolean> provider, Provider<ControlsController> provider2, Provider<ControlsUiController> provider3, Provider<ControlsListingController> provider4) {
        this.featureEnabledProvider = provider;
        this.controlsControllerProvider = provider2;
        this.controlsUiControllerProvider = provider3;
        this.controlsListingControllerProvider = provider4;
    }

    @Override // javax.inject.Provider
    public ControlsComponent get() {
        return provideInstance(this.featureEnabledProvider, this.controlsControllerProvider, this.controlsUiControllerProvider, this.controlsListingControllerProvider);
    }

    public static ControlsComponent provideInstance(Provider<Boolean> provider, Provider<ControlsController> provider2, Provider<ControlsUiController> provider3, Provider<ControlsListingController> provider4) {
        return new ControlsComponent(provider.get().booleanValue(), DoubleCheck.lazy(provider2), DoubleCheck.lazy(provider3), DoubleCheck.lazy(provider4));
    }

    public static ControlsComponent_Factory create(Provider<Boolean> provider, Provider<ControlsController> provider2, Provider<ControlsUiController> provider3, Provider<ControlsListingController> provider4) {
        return new ControlsComponent_Factory(provider, provider2, provider3, provider4);
    }
}
