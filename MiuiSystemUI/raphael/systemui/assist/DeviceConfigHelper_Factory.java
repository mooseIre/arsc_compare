package com.android.systemui.assist;

import dagger.internal.Factory;

public final class DeviceConfigHelper_Factory implements Factory<DeviceConfigHelper> {
    private static final DeviceConfigHelper_Factory INSTANCE = new DeviceConfigHelper_Factory();

    @Override // javax.inject.Provider
    public DeviceConfigHelper get() {
        return provideInstance();
    }

    public static DeviceConfigHelper provideInstance() {
        return new DeviceConfigHelper();
    }

    public static DeviceConfigHelper_Factory create() {
        return INSTANCE;
    }
}
