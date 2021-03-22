package com.android.systemui.controlcenter.phone;

import android.content.Context;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.miui.systemui.SettingsObserver;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ControlPanelController_Factory implements Factory<ControlPanelController> {
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<Context> contextProvider;
    private final Provider<KeyguardStateController> keyguardStateControllerProvider;
    private final Provider<KeyguardViewMediator> keyguardViewMediatorProvider;
    private final Provider<SettingsObserver> observerProvider;

    public ControlPanelController_Factory(Provider<Context> provider, Provider<KeyguardViewMediator> provider2, Provider<BroadcastDispatcher> provider3, Provider<SettingsObserver> provider4, Provider<KeyguardStateController> provider5) {
        this.contextProvider = provider;
        this.keyguardViewMediatorProvider = provider2;
        this.broadcastDispatcherProvider = provider3;
        this.observerProvider = provider4;
        this.keyguardStateControllerProvider = provider5;
    }

    @Override // javax.inject.Provider
    public ControlPanelController get() {
        return provideInstance(this.contextProvider, this.keyguardViewMediatorProvider, this.broadcastDispatcherProvider, this.observerProvider, this.keyguardStateControllerProvider);
    }

    public static ControlPanelController provideInstance(Provider<Context> provider, Provider<KeyguardViewMediator> provider2, Provider<BroadcastDispatcher> provider3, Provider<SettingsObserver> provider4, Provider<KeyguardStateController> provider5) {
        return new ControlPanelController(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get());
    }

    public static ControlPanelController_Factory create(Provider<Context> provider, Provider<KeyguardViewMediator> provider2, Provider<BroadcastDispatcher> provider3, Provider<SettingsObserver> provider4, Provider<KeyguardStateController> provider5) {
        return new ControlPanelController_Factory(provider, provider2, provider3, provider4, provider5);
    }
}
