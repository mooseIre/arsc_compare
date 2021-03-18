package com.android.systemui.statusbar.notification.collection.init;

import com.android.systemui.dump.DumpManager;
import com.android.systemui.statusbar.FeatureFlags;
import com.android.systemui.statusbar.notification.collection.NotifCollection;
import com.android.systemui.statusbar.notification.collection.NotifInflaterImpl;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.collection.NotifViewManager;
import com.android.systemui.statusbar.notification.collection.ShadeListBuilder;
import com.android.systemui.statusbar.notification.collection.coalescer.GroupCoalescer;
import com.android.systemui.statusbar.notification.collection.coordinator.NotifCoordinators;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NotifPipelineInitializer_Factory implements Factory<NotifPipelineInitializer> {
    private final Provider<DumpManager> dumpManagerProvider;
    private final Provider<FeatureFlags> featureFlagsProvider;
    private final Provider<GroupCoalescer> groupCoalescerProvider;
    private final Provider<ShadeListBuilder> listBuilderProvider;
    private final Provider<NotifCollection> notifCollectionProvider;
    private final Provider<NotifCoordinators> notifCoordinatorsProvider;
    private final Provider<NotifInflaterImpl> notifInflaterProvider;
    private final Provider<NotifViewManager> notifViewManagerProvider;
    private final Provider<NotifPipeline> pipelineWrapperProvider;

    public NotifPipelineInitializer_Factory(Provider<NotifPipeline> provider, Provider<GroupCoalescer> provider2, Provider<NotifCollection> provider3, Provider<ShadeListBuilder> provider4, Provider<NotifCoordinators> provider5, Provider<NotifInflaterImpl> provider6, Provider<DumpManager> provider7, Provider<FeatureFlags> provider8, Provider<NotifViewManager> provider9) {
        this.pipelineWrapperProvider = provider;
        this.groupCoalescerProvider = provider2;
        this.notifCollectionProvider = provider3;
        this.listBuilderProvider = provider4;
        this.notifCoordinatorsProvider = provider5;
        this.notifInflaterProvider = provider6;
        this.dumpManagerProvider = provider7;
        this.featureFlagsProvider = provider8;
        this.notifViewManagerProvider = provider9;
    }

    @Override // javax.inject.Provider
    public NotifPipelineInitializer get() {
        return provideInstance(this.pipelineWrapperProvider, this.groupCoalescerProvider, this.notifCollectionProvider, this.listBuilderProvider, this.notifCoordinatorsProvider, this.notifInflaterProvider, this.dumpManagerProvider, this.featureFlagsProvider, this.notifViewManagerProvider);
    }

    public static NotifPipelineInitializer provideInstance(Provider<NotifPipeline> provider, Provider<GroupCoalescer> provider2, Provider<NotifCollection> provider3, Provider<ShadeListBuilder> provider4, Provider<NotifCoordinators> provider5, Provider<NotifInflaterImpl> provider6, Provider<DumpManager> provider7, Provider<FeatureFlags> provider8, Provider<NotifViewManager> provider9) {
        return new NotifPipelineInitializer(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), provider9.get());
    }

    public static NotifPipelineInitializer_Factory create(Provider<NotifPipeline> provider, Provider<GroupCoalescer> provider2, Provider<NotifCollection> provider3, Provider<ShadeListBuilder> provider4, Provider<NotifCoordinators> provider5, Provider<NotifInflaterImpl> provider6, Provider<DumpManager> provider7, Provider<FeatureFlags> provider8, Provider<NotifViewManager> provider9) {
        return new NotifPipelineInitializer_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9);
    }
}
