package com.android.systemui.statusbar.notification.collection.inflation;

import com.android.systemui.statusbar.FeatureFlags;
import com.android.systemui.statusbar.notification.row.RowContentBindStage;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class LowPriorityInflationHelper_Factory implements Factory<LowPriorityInflationHelper> {
    private final Provider<FeatureFlags> featureFlagsProvider;
    private final Provider<NotificationGroupManager> groupManagerProvider;
    private final Provider<RowContentBindStage> rowContentBindStageProvider;

    public LowPriorityInflationHelper_Factory(Provider<FeatureFlags> provider, Provider<NotificationGroupManager> provider2, Provider<RowContentBindStage> provider3) {
        this.featureFlagsProvider = provider;
        this.groupManagerProvider = provider2;
        this.rowContentBindStageProvider = provider3;
    }

    @Override // javax.inject.Provider
    public LowPriorityInflationHelper get() {
        return provideInstance(this.featureFlagsProvider, this.groupManagerProvider, this.rowContentBindStageProvider);
    }

    public static LowPriorityInflationHelper provideInstance(Provider<FeatureFlags> provider, Provider<NotificationGroupManager> provider2, Provider<RowContentBindStage> provider3) {
        return new LowPriorityInflationHelper(provider.get(), provider2.get(), provider3.get());
    }

    public static LowPriorityInflationHelper_Factory create(Provider<FeatureFlags> provider, Provider<NotificationGroupManager> provider2, Provider<RowContentBindStage> provider3) {
        return new LowPriorityInflationHelper_Factory(provider, provider2, provider3);
    }
}
