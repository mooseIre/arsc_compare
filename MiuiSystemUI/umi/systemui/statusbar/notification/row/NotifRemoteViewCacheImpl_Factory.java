package com.android.systemui.statusbar.notification.row;

import com.android.systemui.statusbar.notification.collection.notifcollection.CommonNotifCollection;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NotifRemoteViewCacheImpl_Factory implements Factory<NotifRemoteViewCacheImpl> {
    private final Provider<CommonNotifCollection> collectionProvider;

    public NotifRemoteViewCacheImpl_Factory(Provider<CommonNotifCollection> provider) {
        this.collectionProvider = provider;
    }

    @Override // javax.inject.Provider
    public NotifRemoteViewCacheImpl get() {
        return provideInstance(this.collectionProvider);
    }

    public static NotifRemoteViewCacheImpl provideInstance(Provider<CommonNotifCollection> provider) {
        return new NotifRemoteViewCacheImpl(provider.get());
    }

    public static NotifRemoteViewCacheImpl_Factory create(Provider<CommonNotifCollection> provider) {
        return new NotifRemoteViewCacheImpl_Factory(provider);
    }
}
