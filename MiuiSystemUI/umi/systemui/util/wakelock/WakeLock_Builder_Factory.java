package com.android.systemui.util.wakelock;

import android.content.Context;
import com.android.systemui.util.wakelock.WakeLock;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class WakeLock_Builder_Factory implements Factory<WakeLock.Builder> {
    private final Provider<Context> contextProvider;

    public WakeLock_Builder_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public WakeLock.Builder get() {
        return provideInstance(this.contextProvider);
    }

    public static WakeLock.Builder provideInstance(Provider<Context> provider) {
        return new WakeLock.Builder(provider.get());
    }

    public static WakeLock_Builder_Factory create(Provider<Context> provider) {
        return new WakeLock_Builder_Factory(provider);
    }
}
