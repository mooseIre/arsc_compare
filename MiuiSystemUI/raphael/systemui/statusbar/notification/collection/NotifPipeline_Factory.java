package com.android.systemui.statusbar.notification.collection;

import dagger.internal.Factory;
import javax.inject.Provider;

public final class NotifPipeline_Factory implements Factory<NotifPipeline> {
    private final Provider<NotifCollection> notifCollectionProvider;
    private final Provider<ShadeListBuilder> shadeListBuilderProvider;

    public NotifPipeline_Factory(Provider<NotifCollection> provider, Provider<ShadeListBuilder> provider2) {
        this.notifCollectionProvider = provider;
        this.shadeListBuilderProvider = provider2;
    }

    @Override // javax.inject.Provider
    public NotifPipeline get() {
        return provideInstance(this.notifCollectionProvider, this.shadeListBuilderProvider);
    }

    public static NotifPipeline provideInstance(Provider<NotifCollection> provider, Provider<ShadeListBuilder> provider2) {
        return new NotifPipeline(provider.get(), provider2.get());
    }

    public static NotifPipeline_Factory create(Provider<NotifCollection> provider, Provider<ShadeListBuilder> provider2) {
        return new NotifPipeline_Factory(provider, provider2);
    }
}
