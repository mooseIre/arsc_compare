package com.android.systemui.statusbar.policy;

import dagger.internal.Factory;

public final class CallStateControllerImpl_Factory implements Factory<CallStateControllerImpl> {
    private static final CallStateControllerImpl_Factory INSTANCE = new CallStateControllerImpl_Factory();

    @Override // javax.inject.Provider
    public CallStateControllerImpl get() {
        return provideInstance();
    }

    public static CallStateControllerImpl provideInstance() {
        return new CallStateControllerImpl();
    }

    public static CallStateControllerImpl_Factory create() {
        return INSTANCE;
    }
}
