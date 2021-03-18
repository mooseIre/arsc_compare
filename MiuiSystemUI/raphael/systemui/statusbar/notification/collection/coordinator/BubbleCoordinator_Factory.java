package com.android.systemui.statusbar.notification.collection.coordinator;

import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.statusbar.notification.collection.NotifCollection;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class BubbleCoordinator_Factory implements Factory<BubbleCoordinator> {
    private final Provider<BubbleController> bubbleControllerProvider;
    private final Provider<NotifCollection> notifCollectionProvider;

    public BubbleCoordinator_Factory(Provider<BubbleController> provider, Provider<NotifCollection> provider2) {
        this.bubbleControllerProvider = provider;
        this.notifCollectionProvider = provider2;
    }

    @Override // javax.inject.Provider
    public BubbleCoordinator get() {
        return provideInstance(this.bubbleControllerProvider, this.notifCollectionProvider);
    }

    public static BubbleCoordinator provideInstance(Provider<BubbleController> provider, Provider<NotifCollection> provider2) {
        return new BubbleCoordinator(provider.get(), provider2.get());
    }

    public static BubbleCoordinator_Factory create(Provider<BubbleController> provider, Provider<NotifCollection> provider2) {
        return new BubbleCoordinator_Factory(provider, provider2);
    }
}
