package com.android.systemui.statusbar.notification.collection.coordinator;

import com.android.systemui.media.MediaFeatureFlag;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MediaCoordinator_Factory implements Factory<MediaCoordinator> {
    private final Provider<MediaFeatureFlag> featureFlagProvider;

    public MediaCoordinator_Factory(Provider<MediaFeatureFlag> provider) {
        this.featureFlagProvider = provider;
    }

    @Override // javax.inject.Provider
    public MediaCoordinator get() {
        return provideInstance(this.featureFlagProvider);
    }

    public static MediaCoordinator provideInstance(Provider<MediaFeatureFlag> provider) {
        return new MediaCoordinator(provider.get());
    }

    public static MediaCoordinator_Factory create(Provider<MediaFeatureFlag> provider) {
        return new MediaCoordinator_Factory(provider);
    }
}
