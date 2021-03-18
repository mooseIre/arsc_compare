package com.android.systemui.appops;

import android.content.Context;
import android.os.Looper;
import com.android.systemui.dump.DumpManager;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class AppOpsControllerImpl_Factory implements Factory<AppOpsControllerImpl> {
    private final Provider<Looper> bgLooperProvider;
    private final Provider<Context> contextProvider;
    private final Provider<DumpManager> dumpManagerProvider;

    public AppOpsControllerImpl_Factory(Provider<Context> provider, Provider<Looper> provider2, Provider<DumpManager> provider3) {
        this.contextProvider = provider;
        this.bgLooperProvider = provider2;
        this.dumpManagerProvider = provider3;
    }

    @Override // javax.inject.Provider
    public AppOpsControllerImpl get() {
        return provideInstance(this.contextProvider, this.bgLooperProvider, this.dumpManagerProvider);
    }

    public static AppOpsControllerImpl provideInstance(Provider<Context> provider, Provider<Looper> provider2, Provider<DumpManager> provider3) {
        return new AppOpsControllerImpl(provider.get(), provider2.get(), provider3.get());
    }

    public static AppOpsControllerImpl_Factory create(Provider<Context> provider, Provider<Looper> provider2, Provider<DumpManager> provider3) {
        return new AppOpsControllerImpl_Factory(provider, provider2, provider3);
    }
}
