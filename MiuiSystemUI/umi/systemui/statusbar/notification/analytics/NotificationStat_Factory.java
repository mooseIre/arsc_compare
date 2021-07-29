package com.android.systemui.statusbar.notification.analytics;

import android.content.Context;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationSettingsManager;
import com.android.systemui.statusbar.phone.HeadsUpManagerPhone;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.miui.systemui.EventTracker;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NotificationStat_Factory implements Factory<NotificationStat> {
    private final Provider<StatusBarStateController> barStateControllerProvider;
    private final Provider<Context> contextProvider;
    private final Provider<NotificationEntryManager> entryManagerProvider;
    private final Provider<EventTracker> eventTrackerProvider;
    private final Provider<NotificationGroupManager> groupManagerProvider;
    private final Provider<HeadsUpManagerPhone> headsUpManagerProvider;
    private final Provider<KeyguardStateController> keyguardStateControllerProvider;
    private final Provider<NotificationPanelStat> panelStatProvider;
    private final Provider<NotificationSettingsManager> settingsManagerProvider;

    public NotificationStat_Factory(Provider<Context> provider, Provider<NotificationEntryManager> provider2, Provider<NotificationGroupManager> provider3, Provider<HeadsUpManagerPhone> provider4, Provider<StatusBarStateController> provider5, Provider<KeyguardStateController> provider6, Provider<EventTracker> provider7, Provider<NotificationPanelStat> provider8, Provider<NotificationSettingsManager> provider9) {
        this.contextProvider = provider;
        this.entryManagerProvider = provider2;
        this.groupManagerProvider = provider3;
        this.headsUpManagerProvider = provider4;
        this.barStateControllerProvider = provider5;
        this.keyguardStateControllerProvider = provider6;
        this.eventTrackerProvider = provider7;
        this.panelStatProvider = provider8;
        this.settingsManagerProvider = provider9;
    }

    @Override // javax.inject.Provider
    public NotificationStat get() {
        return provideInstance(this.contextProvider, this.entryManagerProvider, this.groupManagerProvider, this.headsUpManagerProvider, this.barStateControllerProvider, this.keyguardStateControllerProvider, this.eventTrackerProvider, this.panelStatProvider, this.settingsManagerProvider);
    }

    public static NotificationStat provideInstance(Provider<Context> provider, Provider<NotificationEntryManager> provider2, Provider<NotificationGroupManager> provider3, Provider<HeadsUpManagerPhone> provider4, Provider<StatusBarStateController> provider5, Provider<KeyguardStateController> provider6, Provider<EventTracker> provider7, Provider<NotificationPanelStat> provider8, Provider<NotificationSettingsManager> provider9) {
        return new NotificationStat(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), provider9.get());
    }

    public static NotificationStat_Factory create(Provider<Context> provider, Provider<NotificationEntryManager> provider2, Provider<NotificationGroupManager> provider3, Provider<HeadsUpManagerPhone> provider4, Provider<StatusBarStateController> provider5, Provider<KeyguardStateController> provider6, Provider<EventTracker> provider7, Provider<NotificationPanelStat> provider8, Provider<NotificationSettingsManager> provider9) {
        return new NotificationStat_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9);
    }
}
