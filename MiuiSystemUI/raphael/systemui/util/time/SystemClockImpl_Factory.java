package com.android.systemui.util.time;

import dagger.internal.Factory;

public final class SystemClockImpl_Factory implements Factory<SystemClockImpl> {
    private static final SystemClockImpl_Factory INSTANCE = new SystemClockImpl_Factory();

    @Override // javax.inject.Provider
    public SystemClockImpl get() {
        return provideInstance();
    }

    public static SystemClockImpl provideInstance() {
        return new SystemClockImpl();
    }

    public static SystemClockImpl_Factory create() {
        return INSTANCE;
    }
}
