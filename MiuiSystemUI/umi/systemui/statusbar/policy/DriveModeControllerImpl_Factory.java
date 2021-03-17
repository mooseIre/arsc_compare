package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.os.Looper;
import dagger.internal.Factory;
import java.util.concurrent.Executor;
import javax.inject.Provider;

public final class DriveModeControllerImpl_Factory implements Factory<DriveModeControllerImpl> {
    private final Provider<Executor> bgExecutorProvider;
    private final Provider<Looper> bgLooperProvider;
    private final Provider<Context> mContextProvider;
    private final Provider<Executor> uiExecutorProvider;

    public DriveModeControllerImpl_Factory(Provider<Context> provider, Provider<Looper> provider2, Provider<Executor> provider3, Provider<Executor> provider4) {
        this.mContextProvider = provider;
        this.bgLooperProvider = provider2;
        this.bgExecutorProvider = provider3;
        this.uiExecutorProvider = provider4;
    }

    @Override // javax.inject.Provider
    public DriveModeControllerImpl get() {
        return provideInstance(this.mContextProvider, this.bgLooperProvider, this.bgExecutorProvider, this.uiExecutorProvider);
    }

    public static DriveModeControllerImpl provideInstance(Provider<Context> provider, Provider<Looper> provider2, Provider<Executor> provider3, Provider<Executor> provider4) {
        return new DriveModeControllerImpl(provider.get(), provider2.get(), provider3.get(), provider4.get());
    }

    public static DriveModeControllerImpl_Factory create(Provider<Context> provider, Provider<Looper> provider2, Provider<Executor> provider3, Provider<Executor> provider4) {
        return new DriveModeControllerImpl_Factory(provider, provider2, provider3, provider4);
    }
}
