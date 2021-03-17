package com.android.systemui.statusbar.notification.policy;

import android.content.Context;
import android.os.Handler;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.statusbar.notification.NotificationSettingsManager;
import com.android.systemui.statusbar.notification.modal.ModalController;
import com.android.systemui.statusbar.phone.HeadsUpManagerPhone;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class AppMiniWindowManager_Factory implements Factory<AppMiniWindowManager> {
    private final Provider<Context> contextProvider;
    private final Provider<Divider> dividerProvider;
    private final Provider<Handler> handlerProvider;
    private final Provider<HeadsUpManagerPhone> headsUpManagerPhoneProvider;
    private final Provider<ModalController> modalControllerProvider;
    private final Provider<NotificationSettingsManager> notificationSettingsManagerProvider;

    public AppMiniWindowManager_Factory(Provider<Context> provider, Provider<Divider> provider2, Provider<HeadsUpManagerPhone> provider3, Provider<Handler> provider4, Provider<ModalController> provider5, Provider<NotificationSettingsManager> provider6) {
        this.contextProvider = provider;
        this.dividerProvider = provider2;
        this.headsUpManagerPhoneProvider = provider3;
        this.handlerProvider = provider4;
        this.modalControllerProvider = provider5;
        this.notificationSettingsManagerProvider = provider6;
    }

    @Override // javax.inject.Provider
    public AppMiniWindowManager get() {
        return provideInstance(this.contextProvider, this.dividerProvider, this.headsUpManagerPhoneProvider, this.handlerProvider, this.modalControllerProvider, this.notificationSettingsManagerProvider);
    }

    public static AppMiniWindowManager provideInstance(Provider<Context> provider, Provider<Divider> provider2, Provider<HeadsUpManagerPhone> provider3, Provider<Handler> provider4, Provider<ModalController> provider5, Provider<NotificationSettingsManager> provider6) {
        return new AppMiniWindowManager(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get());
    }

    public static AppMiniWindowManager_Factory create(Provider<Context> provider, Provider<Divider> provider2, Provider<HeadsUpManagerPhone> provider3, Provider<Handler> provider4, Provider<ModalController> provider5, Provider<NotificationSettingsManager> provider6) {
        return new AppMiniWindowManager_Factory(provider, provider2, provider3, provider4, provider5, provider6);
    }
}
