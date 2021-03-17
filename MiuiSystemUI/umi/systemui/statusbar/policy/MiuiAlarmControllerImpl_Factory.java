package com.android.systemui.statusbar.policy;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiAlarmControllerImpl_Factory implements Factory<MiuiAlarmControllerImpl> {
    private final Provider<Context> contextProvider;

    public MiuiAlarmControllerImpl_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public MiuiAlarmControllerImpl get() {
        return provideInstance(this.contextProvider);
    }

    public static MiuiAlarmControllerImpl provideInstance(Provider<Context> provider) {
        return new MiuiAlarmControllerImpl(provider.get());
    }

    public static MiuiAlarmControllerImpl_Factory create(Provider<Context> provider) {
        return new MiuiAlarmControllerImpl_Factory(provider);
    }
}
