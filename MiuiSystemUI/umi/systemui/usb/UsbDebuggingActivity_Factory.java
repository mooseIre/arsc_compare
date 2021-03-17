package com.android.systemui.usb;

import dagger.internal.Factory;

public final class UsbDebuggingActivity_Factory implements Factory<UsbDebuggingActivity> {
    private static final UsbDebuggingActivity_Factory INSTANCE = new UsbDebuggingActivity_Factory();

    @Override // javax.inject.Provider
    public UsbDebuggingActivity get() {
        return provideInstance();
    }

    public static UsbDebuggingActivity provideInstance() {
        return new UsbDebuggingActivity();
    }

    public static UsbDebuggingActivity_Factory create() {
        return INSTANCE;
    }
}
