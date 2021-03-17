package com.android.systemui.assist;

import dagger.internal.Factory;

public final class AssistHandleOffBehavior_Factory implements Factory<AssistHandleOffBehavior> {
    private static final AssistHandleOffBehavior_Factory INSTANCE = new AssistHandleOffBehavior_Factory();

    @Override // javax.inject.Provider
    public AssistHandleOffBehavior get() {
        return provideInstance();
    }

    public static AssistHandleOffBehavior provideInstance() {
        return new AssistHandleOffBehavior();
    }

    public static AssistHandleOffBehavior_Factory create() {
        return INSTANCE;
    }
}
