package com.android.systemui.statusbar.notification.policy;

import android.content.Context;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.statusbar.notification.NotificationSettingsManager;
import com.android.systemui.statusbar.phone.HeadsUpManagerPhone;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class AppMiniWindowManager_Factory implements Factory<AppMiniWindowManager> {
    private final Provider<Context> contextProvider;
    private final Provider<Divider> dividerProvider;
    private final Provider<HeadsUpManagerPhone> headsUpManagerPhoneProvider;
    private final Provider<NotificationSettingsManager> notificationSettingsManagerProvider;

    public AppMiniWindowManager_Factory(Provider<Context> provider, Provider<Divider> provider2, Provider<HeadsUpManagerPhone> provider3, Provider<NotificationSettingsManager> provider4) {
        this.contextProvider = provider;
        this.dividerProvider = provider2;
        this.headsUpManagerPhoneProvider = provider3;
        this.notificationSettingsManagerProvider = provider4;
    }

    public AppMiniWindowManager get() {
        return provideInstance(this.contextProvider, this.dividerProvider, this.headsUpManagerPhoneProvider, this.notificationSettingsManagerProvider);
    }

    public static AppMiniWindowManager provideInstance(Provider<Context> provider, Provider<Divider> provider2, Provider<HeadsUpManagerPhone> provider3, Provider<NotificationSettingsManager> provider4) {
        return new AppMiniWindowManager(provider.get(), provider2.get(), provider3.get(), provider4.get());
    }

    public static AppMiniWindowManager_Factory create(Provider<Context> provider, Provider<Divider> provider2, Provider<HeadsUpManagerPhone> provider3, Provider<NotificationSettingsManager> provider4) {
        return new AppMiniWindowManager_Factory(provider, provider2, provider3, provider4);
    }
}
