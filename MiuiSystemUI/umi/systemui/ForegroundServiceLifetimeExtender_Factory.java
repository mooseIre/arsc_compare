package com.android.systemui;

import com.android.systemui.statusbar.NotificationInteractionTracker;
import com.android.systemui.util.time.SystemClock;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ForegroundServiceLifetimeExtender_Factory implements Factory<ForegroundServiceLifetimeExtender> {
    private final Provider<NotificationInteractionTracker> interactionTrackerProvider;
    private final Provider<SystemClock> systemClockProvider;

    public ForegroundServiceLifetimeExtender_Factory(Provider<NotificationInteractionTracker> provider, Provider<SystemClock> provider2) {
        this.interactionTrackerProvider = provider;
        this.systemClockProvider = provider2;
    }

    @Override // javax.inject.Provider
    public ForegroundServiceLifetimeExtender get() {
        return provideInstance(this.interactionTrackerProvider, this.systemClockProvider);
    }

    public static ForegroundServiceLifetimeExtender provideInstance(Provider<NotificationInteractionTracker> provider, Provider<SystemClock> provider2) {
        return new ForegroundServiceLifetimeExtender(provider.get(), provider2.get());
    }

    public static ForegroundServiceLifetimeExtender_Factory create(Provider<NotificationInteractionTracker> provider, Provider<SystemClock> provider2) {
        return new ForegroundServiceLifetimeExtender_Factory(provider, provider2);
    }
}
