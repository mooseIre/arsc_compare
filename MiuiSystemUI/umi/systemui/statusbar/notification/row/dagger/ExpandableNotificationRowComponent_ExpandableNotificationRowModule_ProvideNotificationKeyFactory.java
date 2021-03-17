package com.android.systemui.statusbar.notification.row.dagger;

import android.service.notification.StatusBarNotification;
import com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideNotificationKeyFactory implements Factory<String> {
    private final Provider<StatusBarNotification> statusBarNotificationProvider;

    public ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideNotificationKeyFactory(Provider<StatusBarNotification> provider) {
        this.statusBarNotificationProvider = provider;
    }

    @Override // javax.inject.Provider
    public String get() {
        return provideInstance(this.statusBarNotificationProvider);
    }

    public static String provideInstance(Provider<StatusBarNotification> provider) {
        return proxyProvideNotificationKey(provider.get());
    }

    public static ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideNotificationKeyFactory create(Provider<StatusBarNotification> provider) {
        return new ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideNotificationKeyFactory(provider);
    }

    public static String proxyProvideNotificationKey(StatusBarNotification statusBarNotification) {
        String provideNotificationKey = ExpandableNotificationRowComponent.ExpandableNotificationRowModule.provideNotificationKey(statusBarNotification);
        Preconditions.checkNotNull(provideNotificationKey, "Cannot return null from a non-@Nullable @Provides method");
        return provideNotificationKey;
    }
}
