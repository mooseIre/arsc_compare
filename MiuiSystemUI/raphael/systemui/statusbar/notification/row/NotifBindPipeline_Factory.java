package com.android.systemui.statusbar.notification.row;

import android.os.Looper;
import com.android.systemui.statusbar.notification.collection.notifcollection.CommonNotifCollection;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NotifBindPipeline_Factory implements Factory<NotifBindPipeline> {
    private final Provider<CommonNotifCollection> collectionProvider;
    private final Provider<NotifBindPipelineLogger> loggerProvider;
    private final Provider<Looper> mainLooperProvider;

    public NotifBindPipeline_Factory(Provider<CommonNotifCollection> provider, Provider<NotifBindPipelineLogger> provider2, Provider<Looper> provider3) {
        this.collectionProvider = provider;
        this.loggerProvider = provider2;
        this.mainLooperProvider = provider3;
    }

    @Override // javax.inject.Provider
    public NotifBindPipeline get() {
        return provideInstance(this.collectionProvider, this.loggerProvider, this.mainLooperProvider);
    }

    public static NotifBindPipeline provideInstance(Provider<CommonNotifCollection> provider, Provider<NotifBindPipelineLogger> provider2, Provider<Looper> provider3) {
        return new NotifBindPipeline(provider.get(), provider2.get(), provider3.get());
    }

    public static NotifBindPipeline_Factory create(Provider<CommonNotifCollection> provider, Provider<NotifBindPipelineLogger> provider2, Provider<Looper> provider3) {
        return new NotifBindPipeline_Factory(provider, provider2, provider3);
    }
}
