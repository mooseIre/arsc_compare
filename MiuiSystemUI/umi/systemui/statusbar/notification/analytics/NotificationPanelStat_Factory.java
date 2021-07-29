package com.android.systemui.statusbar.notification.analytics;

import android.content.Context;
import com.miui.systemui.EventTracker;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NotificationPanelStat_Factory implements Factory<NotificationPanelStat> {
    private final Provider<Context> contextProvider;
    private final Provider<EventTracker> eventTrackerProvider;

    public NotificationPanelStat_Factory(Provider<Context> provider, Provider<EventTracker> provider2) {
        this.contextProvider = provider;
        this.eventTrackerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public NotificationPanelStat get() {
        return provideInstance(this.contextProvider, this.eventTrackerProvider);
    }

    public static NotificationPanelStat provideInstance(Provider<Context> provider, Provider<EventTracker> provider2) {
        return new NotificationPanelStat(provider.get(), provider2.get());
    }

    public static NotificationPanelStat_Factory create(Provider<Context> provider, Provider<EventTracker> provider2) {
        return new NotificationPanelStat_Factory(provider, provider2);
    }
}
