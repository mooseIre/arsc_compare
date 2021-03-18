package com.android.systemui.statusbar.policy;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NextAlarmControllerImpl_Factory implements Factory<NextAlarmControllerImpl> {
    private final Provider<Context> contextProvider;

    public NextAlarmControllerImpl_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public NextAlarmControllerImpl get() {
        return provideInstance(this.contextProvider);
    }

    public static NextAlarmControllerImpl provideInstance(Provider<Context> provider) {
        return new NextAlarmControllerImpl(provider.get());
    }

    public static NextAlarmControllerImpl_Factory create(Provider<Context> provider) {
        return new NextAlarmControllerImpl_Factory(provider);
    }
}
