package com.android.systemui.statusbar.notification.dagger;

import android.content.Context;
import com.android.systemui.statusbar.notification.init.NotificationsController;
import com.android.systemui.statusbar.notification.init.NotificationsControllerImpl;
import com.android.systemui.statusbar.notification.init.NotificationsControllerStub;
import dagger.Lazy;
import dagger.internal.DoubleCheck;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class NotificationsModule_ProvideNotificationsControllerFactory implements Factory<NotificationsController> {
    private final Provider<Context> contextProvider;
    private final Provider<NotificationsControllerImpl> realControllerProvider;
    private final Provider<NotificationsControllerStub> stubControllerProvider;

    public NotificationsModule_ProvideNotificationsControllerFactory(Provider<Context> provider, Provider<NotificationsControllerImpl> provider2, Provider<NotificationsControllerStub> provider3) {
        this.contextProvider = provider;
        this.realControllerProvider = provider2;
        this.stubControllerProvider = provider3;
    }

    @Override // javax.inject.Provider
    public NotificationsController get() {
        return provideInstance(this.contextProvider, this.realControllerProvider, this.stubControllerProvider);
    }

    public static NotificationsController provideInstance(Provider<Context> provider, Provider<NotificationsControllerImpl> provider2, Provider<NotificationsControllerStub> provider3) {
        return proxyProvideNotificationsController(provider.get(), DoubleCheck.lazy(provider2), DoubleCheck.lazy(provider3));
    }

    public static NotificationsModule_ProvideNotificationsControllerFactory create(Provider<Context> provider, Provider<NotificationsControllerImpl> provider2, Provider<NotificationsControllerStub> provider3) {
        return new NotificationsModule_ProvideNotificationsControllerFactory(provider, provider2, provider3);
    }

    public static NotificationsController proxyProvideNotificationsController(Context context, Lazy<NotificationsControllerImpl> lazy, Lazy<NotificationsControllerStub> lazy2) {
        NotificationsController provideNotificationsController = NotificationsModule.provideNotificationsController(context, lazy, lazy2);
        Preconditions.checkNotNull(provideNotificationsController, "Cannot return null from a non-@Nullable @Provides method");
        return provideNotificationsController;
    }
}
