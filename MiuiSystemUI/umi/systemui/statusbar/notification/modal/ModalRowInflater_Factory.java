package com.android.systemui.statusbar.notification.modal;

import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.notification.row.NotificationContentInflater;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ModalRowInflater_Factory implements Factory<ModalRowInflater> {
    private final Provider<NotificationContentInflater> contentInflaterProvider;
    private final Provider<NotificationRemoteInputManager> remoteInputManagerProvider;

    public ModalRowInflater_Factory(Provider<NotificationContentInflater> provider, Provider<NotificationRemoteInputManager> provider2) {
        this.contentInflaterProvider = provider;
        this.remoteInputManagerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public ModalRowInflater get() {
        return provideInstance(this.contentInflaterProvider, this.remoteInputManagerProvider);
    }

    public static ModalRowInflater provideInstance(Provider<NotificationContentInflater> provider, Provider<NotificationRemoteInputManager> provider2) {
        ModalRowInflater modalRowInflater = new ModalRowInflater();
        ModalRowInflater_MembersInjector.injectContentInflater(modalRowInflater, provider.get());
        ModalRowInflater_MembersInjector.injectRemoteInputManager(modalRowInflater, provider2.get());
        return modalRowInflater;
    }

    public static ModalRowInflater_Factory create(Provider<NotificationContentInflater> provider, Provider<NotificationRemoteInputManager> provider2) {
        return new ModalRowInflater_Factory(provider, provider2);
    }
}
