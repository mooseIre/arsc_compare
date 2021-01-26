package com.android.systemui.statusbar.policy;

import dagger.internal.Factory;

public final class DualClockObserver_Factory implements Factory<DualClockObserver> {
    private static final DualClockObserver_Factory INSTANCE = new DualClockObserver_Factory();

    public DualClockObserver get() {
        return provideInstance();
    }

    public static DualClockObserver provideInstance() {
        return new DualClockObserver();
    }

    public static DualClockObserver_Factory create() {
        return INSTANCE;
    }
}
