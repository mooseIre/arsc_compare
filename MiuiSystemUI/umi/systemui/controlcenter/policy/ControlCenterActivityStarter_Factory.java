package com.android.systemui.controlcenter.policy;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ControlCenterActivityStarter_Factory implements Factory<ControlCenterActivityStarter> {
    private final Provider<Context> contextProvider;

    public ControlCenterActivityStarter_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public ControlCenterActivityStarter get() {
        return provideInstance(this.contextProvider);
    }

    public static ControlCenterActivityStarter provideInstance(Provider<Context> provider) {
        return new ControlCenterActivityStarter(provider.get());
    }

    public static ControlCenterActivityStarter_Factory create(Provider<Context> provider) {
        return new ControlCenterActivityStarter_Factory(provider);
    }
}
