package com.android.systemui;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ActivityIntentHelper_Factory implements Factory<ActivityIntentHelper> {
    private final Provider<Context> contextProvider;

    public ActivityIntentHelper_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public ActivityIntentHelper get() {
        return provideInstance(this.contextProvider);
    }

    public static ActivityIntentHelper provideInstance(Provider<Context> provider) {
        return new ActivityIntentHelper(provider.get());
    }

    public static ActivityIntentHelper_Factory create(Provider<Context> provider) {
        return new ActivityIntentHelper_Factory(provider);
    }
}
