package com.android.systemui;

import dagger.internal.Factory;

public final class InitController_Factory implements Factory<InitController> {
    private static final InitController_Factory INSTANCE = new InitController_Factory();

    @Override // javax.inject.Provider
    public InitController get() {
        return provideInstance();
    }

    public static InitController provideInstance() {
        return new InitController();
    }

    public static InitController_Factory create() {
        return INSTANCE;
    }
}
