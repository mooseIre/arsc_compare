package com.android.systemui.qs;

import android.content.Context;
import com.android.systemui.qs.AutoAddTracker;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class AutoAddTracker_Builder_Factory implements Factory<AutoAddTracker.Builder> {
    private final Provider<Context> contextProvider;

    public AutoAddTracker_Builder_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public AutoAddTracker.Builder get() {
        return provideInstance(this.contextProvider);
    }

    public static AutoAddTracker.Builder provideInstance(Provider<Context> provider) {
        return new AutoAddTracker.Builder(provider.get());
    }

    public static AutoAddTracker_Builder_Factory create(Provider<Context> provider) {
        return new AutoAddTracker_Builder_Factory(provider);
    }
}
