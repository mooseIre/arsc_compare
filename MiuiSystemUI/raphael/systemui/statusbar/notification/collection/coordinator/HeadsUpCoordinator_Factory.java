package com.android.systemui.statusbar.notification.collection.coordinator;

import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.notification.interruption.HeadsUpViewBinder;
import com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProvider;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class HeadsUpCoordinator_Factory implements Factory<HeadsUpCoordinator> {
    private final Provider<HeadsUpManager> headsUpManagerProvider;
    private final Provider<HeadsUpViewBinder> headsUpViewBinderProvider;
    private final Provider<NotificationInterruptStateProvider> notificationInterruptStateProvider;
    private final Provider<NotificationRemoteInputManager> remoteInputManagerProvider;

    public HeadsUpCoordinator_Factory(Provider<HeadsUpManager> provider, Provider<HeadsUpViewBinder> provider2, Provider<NotificationInterruptStateProvider> provider3, Provider<NotificationRemoteInputManager> provider4) {
        this.headsUpManagerProvider = provider;
        this.headsUpViewBinderProvider = provider2;
        this.notificationInterruptStateProvider = provider3;
        this.remoteInputManagerProvider = provider4;
    }

    @Override // javax.inject.Provider
    public HeadsUpCoordinator get() {
        return provideInstance(this.headsUpManagerProvider, this.headsUpViewBinderProvider, this.notificationInterruptStateProvider, this.remoteInputManagerProvider);
    }

    public static HeadsUpCoordinator provideInstance(Provider<HeadsUpManager> provider, Provider<HeadsUpViewBinder> provider2, Provider<NotificationInterruptStateProvider> provider3, Provider<NotificationRemoteInputManager> provider4) {
        return new HeadsUpCoordinator(provider.get(), provider2.get(), provider3.get(), provider4.get());
    }

    public static HeadsUpCoordinator_Factory create(Provider<HeadsUpManager> provider, Provider<HeadsUpViewBinder> provider2, Provider<NotificationInterruptStateProvider> provider3, Provider<NotificationRemoteInputManager> provider4) {
        return new HeadsUpCoordinator_Factory(provider, provider2, provider3, provider4);
    }
}
