package com.android.systemui.statusbar.notification;

import android.content.Context;
import com.miui.systemui.CloudDataManager;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NotificationSettingsManager_Factory implements Factory<NotificationSettingsManager> {
    private final Provider<Context> contextProvider;
    private final Provider<CloudDataManager> managerProvider;

    public NotificationSettingsManager_Factory(Provider<Context> provider, Provider<CloudDataManager> provider2) {
        this.contextProvider = provider;
        this.managerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public NotificationSettingsManager get() {
        return provideInstance(this.contextProvider, this.managerProvider);
    }

    public static NotificationSettingsManager provideInstance(Provider<Context> provider, Provider<CloudDataManager> provider2) {
        return new NotificationSettingsManager(provider.get(), provider2.get());
    }

    public static NotificationSettingsManager_Factory create(Provider<Context> provider, Provider<CloudDataManager> provider2) {
        return new NotificationSettingsManager_Factory(provider, provider2);
    }
}
