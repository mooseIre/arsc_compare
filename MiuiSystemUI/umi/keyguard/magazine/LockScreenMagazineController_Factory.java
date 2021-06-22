package com.android.keyguard.magazine;

import android.content.Context;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class LockScreenMagazineController_Factory implements Factory<LockScreenMagazineController> {
    private final Provider<Context> contextProvider;
    private final Provider<StatusBarStateController> statusBarStateControllerProvider;

    public LockScreenMagazineController_Factory(Provider<Context> provider, Provider<StatusBarStateController> provider2) {
        this.contextProvider = provider;
        this.statusBarStateControllerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public LockScreenMagazineController get() {
        return provideInstance(this.contextProvider, this.statusBarStateControllerProvider);
    }

    public static LockScreenMagazineController provideInstance(Provider<Context> provider, Provider<StatusBarStateController> provider2) {
        return new LockScreenMagazineController(provider.get(), provider2.get());
    }

    public static LockScreenMagazineController_Factory create(Provider<Context> provider, Provider<StatusBarStateController> provider2) {
        return new LockScreenMagazineController_Factory(provider, provider2);
    }
}
