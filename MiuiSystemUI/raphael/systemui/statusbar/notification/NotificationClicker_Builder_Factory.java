package com.android.systemui.statusbar.notification;

import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.statusbar.notification.NotificationClicker;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NotificationClicker_Builder_Factory implements Factory<NotificationClicker.Builder> {
    private final Provider<BubbleController> bubbleControllerProvider;
    private final Provider<NotificationClickerLogger> loggerProvider;

    public NotificationClicker_Builder_Factory(Provider<BubbleController> provider, Provider<NotificationClickerLogger> provider2) {
        this.bubbleControllerProvider = provider;
        this.loggerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public NotificationClicker.Builder get() {
        return provideInstance(this.bubbleControllerProvider, this.loggerProvider);
    }

    public static NotificationClicker.Builder provideInstance(Provider<BubbleController> provider, Provider<NotificationClickerLogger> provider2) {
        return new NotificationClicker.Builder(provider.get(), provider2.get());
    }

    public static NotificationClicker_Builder_Factory create(Provider<BubbleController> provider, Provider<NotificationClickerLogger> provider2) {
        return new NotificationClicker_Builder_Factory(provider, provider2);
    }
}
