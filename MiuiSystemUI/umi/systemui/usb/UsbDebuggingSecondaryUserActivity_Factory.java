package com.android.systemui.usb;

import com.android.systemui.broadcast.BroadcastDispatcher;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class UsbDebuggingSecondaryUserActivity_Factory implements Factory<UsbDebuggingSecondaryUserActivity> {
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;

    public UsbDebuggingSecondaryUserActivity_Factory(Provider<BroadcastDispatcher> provider) {
        this.broadcastDispatcherProvider = provider;
    }

    @Override // javax.inject.Provider
    public UsbDebuggingSecondaryUserActivity get() {
        return provideInstance(this.broadcastDispatcherProvider);
    }

    public static UsbDebuggingSecondaryUserActivity provideInstance(Provider<BroadcastDispatcher> provider) {
        return new UsbDebuggingSecondaryUserActivity(provider.get());
    }

    public static UsbDebuggingSecondaryUserActivity_Factory create(Provider<BroadcastDispatcher> provider) {
        return new UsbDebuggingSecondaryUserActivity_Factory(provider);
    }
}
