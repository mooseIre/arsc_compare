package com.android.systemui.statusbar.notification.collection;

import com.android.systemui.statusbar.FeatureFlags;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NotifViewManager_Factory implements Factory<NotifViewManager> {
    private final Provider<FeatureFlags> featureFlagsProvider;
    private final Provider<NotifViewBarn> rowRegistryProvider;
    private final Provider<VisualStabilityManager> stabilityManagerProvider;

    public NotifViewManager_Factory(Provider<NotifViewBarn> provider, Provider<VisualStabilityManager> provider2, Provider<FeatureFlags> provider3) {
        this.rowRegistryProvider = provider;
        this.stabilityManagerProvider = provider2;
        this.featureFlagsProvider = provider3;
    }

    @Override // javax.inject.Provider
    public NotifViewManager get() {
        return provideInstance(this.rowRegistryProvider, this.stabilityManagerProvider, this.featureFlagsProvider);
    }

    public static NotifViewManager provideInstance(Provider<NotifViewBarn> provider, Provider<VisualStabilityManager> provider2, Provider<FeatureFlags> provider3) {
        return new NotifViewManager(provider.get(), provider2.get(), provider3.get());
    }

    public static NotifViewManager_Factory create(Provider<NotifViewBarn> provider, Provider<VisualStabilityManager> provider2, Provider<FeatureFlags> provider3) {
        return new NotifViewManager_Factory(provider, provider2, provider3);
    }
}
