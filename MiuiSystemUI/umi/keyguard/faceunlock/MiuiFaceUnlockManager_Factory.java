package com.android.keyguard.faceunlock;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiFaceUnlockManager_Factory implements Factory<MiuiFaceUnlockManager> {
    private final Provider<Context> contextProvider;

    public MiuiFaceUnlockManager_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public MiuiFaceUnlockManager get() {
        return provideInstance(this.contextProvider);
    }

    public static MiuiFaceUnlockManager provideInstance(Provider<Context> provider) {
        return new MiuiFaceUnlockManager(provider.get());
    }

    public static MiuiFaceUnlockManager_Factory create(Provider<Context> provider) {
        return new MiuiFaceUnlockManager_Factory(provider);
    }
}
