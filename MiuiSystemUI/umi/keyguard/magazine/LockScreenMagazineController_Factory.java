package com.android.keyguard.magazine;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class LockScreenMagazineController_Factory implements Factory<LockScreenMagazineController> {
    private final Provider<Context> contextProvider;

    public LockScreenMagazineController_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public LockScreenMagazineController get() {
        return provideInstance(this.contextProvider);
    }

    public static LockScreenMagazineController provideInstance(Provider<Context> provider) {
        return new LockScreenMagazineController(provider.get());
    }

    public static LockScreenMagazineController_Factory create(Provider<Context> provider) {
        return new LockScreenMagazineController_Factory(provider);
    }
}
