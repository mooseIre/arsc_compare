package com.android.systemui.controlcenter.phone;

import android.content.Context;
import com.android.systemui.keyguard.KeyguardViewMediator;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ControlPanelController_Factory implements Factory<ControlPanelController> {
    private final Provider<Context> contextProvider;
    private final Provider<KeyguardViewMediator> keyguardViewMediatorProvider;

    public ControlPanelController_Factory(Provider<Context> provider, Provider<KeyguardViewMediator> provider2) {
        this.contextProvider = provider;
        this.keyguardViewMediatorProvider = provider2;
    }

    public ControlPanelController get() {
        return provideInstance(this.contextProvider, this.keyguardViewMediatorProvider);
    }

    public static ControlPanelController provideInstance(Provider<Context> provider, Provider<KeyguardViewMediator> provider2) {
        return new ControlPanelController(provider.get(), provider2.get());
    }

    public static ControlPanelController_Factory create(Provider<Context> provider, Provider<KeyguardViewMediator> provider2) {
        return new ControlPanelController_Factory(provider, provider2);
    }
}
