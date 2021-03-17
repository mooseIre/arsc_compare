package com.android.systemui.statusbar.notification;

import com.android.systemui.media.MediaFeatureFlag;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NotificationFilter_Factory implements Factory<NotificationFilter> {
    private final Provider<MediaFeatureFlag> mediaFeatureFlagProvider;
    private final Provider<StatusBarStateController> statusBarStateControllerProvider;

    public NotificationFilter_Factory(Provider<StatusBarStateController> provider, Provider<MediaFeatureFlag> provider2) {
        this.statusBarStateControllerProvider = provider;
        this.mediaFeatureFlagProvider = provider2;
    }

    @Override // javax.inject.Provider
    public NotificationFilter get() {
        return provideInstance(this.statusBarStateControllerProvider, this.mediaFeatureFlagProvider);
    }

    public static NotificationFilter provideInstance(Provider<StatusBarStateController> provider, Provider<MediaFeatureFlag> provider2) {
        return new NotificationFilter(provider.get(), provider2.get());
    }

    public static NotificationFilter_Factory create(Provider<StatusBarStateController> provider, Provider<MediaFeatureFlag> provider2) {
        return new NotificationFilter_Factory(provider, provider2);
    }
}
