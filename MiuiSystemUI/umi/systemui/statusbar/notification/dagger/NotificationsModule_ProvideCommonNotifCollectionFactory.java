package com.android.systemui.statusbar.notification.dagger;

import com.android.systemui.statusbar.FeatureFlags;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.collection.notifcollection.CommonNotifCollection;
import dagger.Lazy;
import dagger.internal.DoubleCheck;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class NotificationsModule_ProvideCommonNotifCollectionFactory implements Factory<CommonNotifCollection> {
    private final Provider<NotificationEntryManager> entryManagerProvider;
    private final Provider<FeatureFlags> featureFlagsProvider;
    private final Provider<NotifPipeline> pipelineProvider;

    public NotificationsModule_ProvideCommonNotifCollectionFactory(Provider<FeatureFlags> provider, Provider<NotifPipeline> provider2, Provider<NotificationEntryManager> provider3) {
        this.featureFlagsProvider = provider;
        this.pipelineProvider = provider2;
        this.entryManagerProvider = provider3;
    }

    @Override // javax.inject.Provider
    public CommonNotifCollection get() {
        return provideInstance(this.featureFlagsProvider, this.pipelineProvider, this.entryManagerProvider);
    }

    public static CommonNotifCollection provideInstance(Provider<FeatureFlags> provider, Provider<NotifPipeline> provider2, Provider<NotificationEntryManager> provider3) {
        return proxyProvideCommonNotifCollection(provider.get(), DoubleCheck.lazy(provider2), provider3.get());
    }

    public static NotificationsModule_ProvideCommonNotifCollectionFactory create(Provider<FeatureFlags> provider, Provider<NotifPipeline> provider2, Provider<NotificationEntryManager> provider3) {
        return new NotificationsModule_ProvideCommonNotifCollectionFactory(provider, provider2, provider3);
    }

    public static CommonNotifCollection proxyProvideCommonNotifCollection(FeatureFlags featureFlags, Lazy<NotifPipeline> lazy, NotificationEntryManager notificationEntryManager) {
        CommonNotifCollection provideCommonNotifCollection = NotificationsModule.provideCommonNotifCollection(featureFlags, lazy, notificationEntryManager);
        Preconditions.checkNotNull(provideCommonNotifCollection, "Cannot return null from a non-@Nullable @Provides method");
        return provideCommonNotifCollection;
    }
}
