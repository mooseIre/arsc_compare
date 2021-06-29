package com.android.systemui.statusbar.notification.unimportant;

import dagger.internal.Factory;

public final class UnimportantSdk_Factory implements Factory<UnimportantSdk> {
    private static final UnimportantSdk_Factory INSTANCE = new UnimportantSdk_Factory();

    @Override // javax.inject.Provider
    public UnimportantSdk get() {
        return provideInstance();
    }

    public static UnimportantSdk provideInstance() {
        return new UnimportantSdk();
    }

    public static UnimportantSdk_Factory create() {
        return INSTANCE;
    }
}
