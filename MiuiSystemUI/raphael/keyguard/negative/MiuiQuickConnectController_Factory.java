package com.android.keyguard.negative;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiQuickConnectController_Factory implements Factory<MiuiQuickConnectController> {
    private final Provider<Context> mContextProvider;

    public MiuiQuickConnectController_Factory(Provider<Context> provider) {
        this.mContextProvider = provider;
    }

    @Override // javax.inject.Provider
    public MiuiQuickConnectController get() {
        return provideInstance(this.mContextProvider);
    }

    public static MiuiQuickConnectController provideInstance(Provider<Context> provider) {
        return new MiuiQuickConnectController(provider.get());
    }

    public static MiuiQuickConnectController_Factory create(Provider<Context> provider) {
        return new MiuiQuickConnectController_Factory(provider);
    }
}
