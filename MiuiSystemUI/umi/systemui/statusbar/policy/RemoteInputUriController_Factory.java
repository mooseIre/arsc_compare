package com.android.systemui.statusbar.policy;

import com.android.internal.statusbar.IStatusBarService;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class RemoteInputUriController_Factory implements Factory<RemoteInputUriController> {
    private final Provider<IStatusBarService> statusBarServiceProvider;

    public RemoteInputUriController_Factory(Provider<IStatusBarService> provider) {
        this.statusBarServiceProvider = provider;
    }

    @Override // javax.inject.Provider
    public RemoteInputUriController get() {
        return provideInstance(this.statusBarServiceProvider);
    }

    public static RemoteInputUriController provideInstance(Provider<IStatusBarService> provider) {
        return new RemoteInputUriController(provider.get());
    }

    public static RemoteInputUriController_Factory create(Provider<IStatusBarService> provider) {
        return new RemoteInputUriController_Factory(provider);
    }
}
