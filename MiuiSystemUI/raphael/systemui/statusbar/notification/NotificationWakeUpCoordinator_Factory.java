package com.android.systemui.statusbar.notification;

import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.phone.DozeParameters;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NotificationWakeUpCoordinator_Factory implements Factory<NotificationWakeUpCoordinator> {
    private final Provider<KeyguardBypassController> bypassControllerProvider;
    private final Provider<DozeParameters> dozeParametersProvider;
    private final Provider<HeadsUpManager> mHeadsUpManagerProvider;
    private final Provider<StatusBarStateController> statusBarStateControllerProvider;

    public NotificationWakeUpCoordinator_Factory(Provider<HeadsUpManager> provider, Provider<StatusBarStateController> provider2, Provider<KeyguardBypassController> provider3, Provider<DozeParameters> provider4) {
        this.mHeadsUpManagerProvider = provider;
        this.statusBarStateControllerProvider = provider2;
        this.bypassControllerProvider = provider3;
        this.dozeParametersProvider = provider4;
    }

    @Override // javax.inject.Provider
    public NotificationWakeUpCoordinator get() {
        return provideInstance(this.mHeadsUpManagerProvider, this.statusBarStateControllerProvider, this.bypassControllerProvider, this.dozeParametersProvider);
    }

    public static NotificationWakeUpCoordinator provideInstance(Provider<HeadsUpManager> provider, Provider<StatusBarStateController> provider2, Provider<KeyguardBypassController> provider3, Provider<DozeParameters> provider4) {
        return new NotificationWakeUpCoordinator(provider.get(), provider2.get(), provider3.get(), provider4.get());
    }

    public static NotificationWakeUpCoordinator_Factory create(Provider<HeadsUpManager> provider, Provider<StatusBarStateController> provider2, Provider<KeyguardBypassController> provider3, Provider<DozeParameters> provider4) {
        return new NotificationWakeUpCoordinator_Factory(provider, provider2, provider3, provider4);
    }
}
