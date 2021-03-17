package com.android.systemui.util;

import dagger.internal.Factory;

public final class DeviceConfigProxy_Factory implements Factory<DeviceConfigProxy> {
    private static final DeviceConfigProxy_Factory INSTANCE = new DeviceConfigProxy_Factory();

    public DeviceConfigProxy get() {
        return provideInstance();
    }

    public static DeviceConfigProxy provideInstance() {
        return new DeviceConfigProxy();
    }

    public static DeviceConfigProxy_Factory create() {
        return INSTANCE;
    }
}
