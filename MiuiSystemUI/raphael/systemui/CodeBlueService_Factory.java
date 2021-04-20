package com.android.systemui;

import android.content.Context;
import android.os.Looper;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.miui.systemui.EventTracker;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class CodeBlueService_Factory implements Factory<CodeBlueService> {
    private final Provider<Looper> bgLooperProvider;
    private final Provider<Context> contextProvider;
    private final Provider<NotificationEntryManager> entryManagerProvider;
    private final Provider<EventTracker> eventTrackerProvider;

    public CodeBlueService_Factory(Provider<Context> provider, Provider<Looper> provider2, Provider<NotificationEntryManager> provider3, Provider<EventTracker> provider4) {
        this.contextProvider = provider;
        this.bgLooperProvider = provider2;
        this.entryManagerProvider = provider3;
        this.eventTrackerProvider = provider4;
    }

    @Override // javax.inject.Provider
    public CodeBlueService get() {
        return provideInstance(this.contextProvider, this.bgLooperProvider, this.entryManagerProvider, this.eventTrackerProvider);
    }

    public static CodeBlueService provideInstance(Provider<Context> provider, Provider<Looper> provider2, Provider<NotificationEntryManager> provider3, Provider<EventTracker> provider4) {
        return new CodeBlueService(provider.get(), provider2.get(), provider3.get(), provider4.get());
    }

    public static CodeBlueService_Factory create(Provider<Context> provider, Provider<Looper> provider2, Provider<NotificationEntryManager> provider3, Provider<EventTracker> provider4) {
        return new CodeBlueService_Factory(provider, provider2, provider3, provider4);
    }
}
