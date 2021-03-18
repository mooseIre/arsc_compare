package com.android.systemui.statusbar.notification.people;

import com.android.systemui.statusbar.policy.ExtensionController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NotificationPersonExtractorPluginBoundary_Factory implements Factory<NotificationPersonExtractorPluginBoundary> {
    private final Provider<ExtensionController> extensionControllerProvider;

    public NotificationPersonExtractorPluginBoundary_Factory(Provider<ExtensionController> provider) {
        this.extensionControllerProvider = provider;
    }

    @Override // javax.inject.Provider
    public NotificationPersonExtractorPluginBoundary get() {
        return provideInstance(this.extensionControllerProvider);
    }

    public static NotificationPersonExtractorPluginBoundary provideInstance(Provider<ExtensionController> provider) {
        return new NotificationPersonExtractorPluginBoundary(provider.get());
    }

    public static NotificationPersonExtractorPluginBoundary_Factory create(Provider<ExtensionController> provider) {
        return new NotificationPersonExtractorPluginBoundary_Factory(provider);
    }
}
