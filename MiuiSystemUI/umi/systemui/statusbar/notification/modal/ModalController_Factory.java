package com.android.systemui.statusbar.notification.modal;

import android.content.Context;
import com.android.systemui.statusbar.phone.StatusBar;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ModalController_Factory implements Factory<ModalController> {
    private final Provider<Context> contextProvider;
    private final Provider<ModalRowInflater> modalRowInflaterProvider;
    private final Provider<StatusBar> statusBarProvider;

    public ModalController_Factory(Provider<Context> provider, Provider<StatusBar> provider2, Provider<ModalRowInflater> provider3) {
        this.contextProvider = provider;
        this.statusBarProvider = provider2;
        this.modalRowInflaterProvider = provider3;
    }

    public ModalController get() {
        return provideInstance(this.contextProvider, this.statusBarProvider, this.modalRowInflaterProvider);
    }

    public static ModalController provideInstance(Provider<Context> provider, Provider<StatusBar> provider2, Provider<ModalRowInflater> provider3) {
        ModalController modalController = new ModalController(provider.get(), provider2.get());
        ModalController_MembersInjector.injectModalRowInflater(modalController, provider3.get());
        return modalController;
    }

    public static ModalController_Factory create(Provider<Context> provider, Provider<StatusBar> provider2, Provider<ModalRowInflater> provider3) {
        return new ModalController_Factory(provider, provider2, provider3);
    }
}
