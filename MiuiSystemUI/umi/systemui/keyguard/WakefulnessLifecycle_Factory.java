package com.android.systemui.keyguard;

import dagger.internal.Factory;

public final class WakefulnessLifecycle_Factory implements Factory<WakefulnessLifecycle> {
    private static final WakefulnessLifecycle_Factory INSTANCE = new WakefulnessLifecycle_Factory();

    @Override // javax.inject.Provider
    public WakefulnessLifecycle get() {
        return provideInstance();
    }

    public static WakefulnessLifecycle provideInstance() {
        return new WakefulnessLifecycle();
    }

    public static WakefulnessLifecycle_Factory create() {
        return INSTANCE;
    }
}
