package com.android.systemui;

import com.android.systemui.statusbar.phone.StatusBar;
import dagger.Lazy;
import dagger.internal.Factory;
import java.util.Optional;
import javax.inject.Provider;

public final class ActivityStarterDelegate_Factory implements Factory<ActivityStarterDelegate> {
    private final Provider<Optional<Lazy<StatusBar>>> statusBarProvider;

    public ActivityStarterDelegate_Factory(Provider<Optional<Lazy<StatusBar>>> provider) {
        this.statusBarProvider = provider;
    }

    @Override // javax.inject.Provider
    public ActivityStarterDelegate get() {
        return provideInstance(this.statusBarProvider);
    }

    public static ActivityStarterDelegate provideInstance(Provider<Optional<Lazy<StatusBar>>> provider) {
        return new ActivityStarterDelegate(provider.get());
    }

    public static ActivityStarterDelegate_Factory create(Provider<Optional<Lazy<StatusBar>>> provider) {
        return new ActivityStarterDelegate_Factory(provider);
    }
}
