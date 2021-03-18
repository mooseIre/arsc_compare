package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.os.Handler;
import com.android.systemui.controlcenter.phone.ControlPanelController;
import com.android.systemui.statusbar.phone.HeadsUpManagerPhone;
import com.android.systemui.statusbar.phone.NotificationShadeWindowController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiNotificationShadePolicy_Factory implements Factory<MiuiNotificationShadePolicy> {
    private final Provider<Context> contextProvider;
    private final Provider<ControlPanelController> controlPanelControllerProvider;
    private final Provider<Handler> handlerProvider;
    private final Provider<HeadsUpManagerPhone> headsUpManagerPhoneProvider;
    private final Provider<NotificationShadeWindowController> notificationShadeWindowControllerProvider;

    public MiuiNotificationShadePolicy_Factory(Provider<Context> provider, Provider<Handler> provider2, Provider<HeadsUpManagerPhone> provider3, Provider<NotificationShadeWindowController> provider4, Provider<ControlPanelController> provider5) {
        this.contextProvider = provider;
        this.handlerProvider = provider2;
        this.headsUpManagerPhoneProvider = provider3;
        this.notificationShadeWindowControllerProvider = provider4;
        this.controlPanelControllerProvider = provider5;
    }

    @Override // javax.inject.Provider
    public MiuiNotificationShadePolicy get() {
        return provideInstance(this.contextProvider, this.handlerProvider, this.headsUpManagerPhoneProvider, this.notificationShadeWindowControllerProvider, this.controlPanelControllerProvider);
    }

    public static MiuiNotificationShadePolicy provideInstance(Provider<Context> provider, Provider<Handler> provider2, Provider<HeadsUpManagerPhone> provider3, Provider<NotificationShadeWindowController> provider4, Provider<ControlPanelController> provider5) {
        return new MiuiNotificationShadePolicy(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get());
    }

    public static MiuiNotificationShadePolicy_Factory create(Provider<Context> provider, Provider<Handler> provider2, Provider<HeadsUpManagerPhone> provider3, Provider<NotificationShadeWindowController> provider4, Provider<ControlPanelController> provider5) {
        return new MiuiNotificationShadePolicy_Factory(provider, provider2, provider3, provider4, provider5);
    }
}
