package com.android.systemui.statusbar.notification.policy;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class UsbNotificationController_Factory implements Factory<UsbNotificationController> {
    private final Provider<Context> contextProvider;

    public UsbNotificationController_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public UsbNotificationController get() {
        return provideInstance(this.contextProvider);
    }

    public static UsbNotificationController provideInstance(Provider<Context> provider) {
        return new UsbNotificationController(provider.get());
    }

    public static UsbNotificationController_Factory create(Provider<Context> provider) {
        return new UsbNotificationController_Factory(provider);
    }
}
