package com.android.systemui.statusbar.notification.modal;

import android.content.Context;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.phone.StatusBar;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ModalController_Factory implements Factory<ModalController> {
    private final Provider<Context> contextProvider;
    private final Provider<StatusBarStateController> mStatusBarStateControllerProvider;
    private final Provider<ModalRowInflater> modalRowInflaterProvider;
    private final Provider<StatusBar> statusBarProvider;

    public ModalController_Factory(Provider<Context> provider, Provider<StatusBar> provider2, Provider<StatusBarStateController> provider3, Provider<ModalRowInflater> provider4) {
        this.contextProvider = provider;
        this.statusBarProvider = provider2;
        this.mStatusBarStateControllerProvider = provider3;
        this.modalRowInflaterProvider = provider4;
    }

    @Override // javax.inject.Provider
    public ModalController get() {
        return provideInstance(this.contextProvider, this.statusBarProvider, this.mStatusBarStateControllerProvider, this.modalRowInflaterProvider);
    }

    public static ModalController provideInstance(Provider<Context> provider, Provider<StatusBar> provider2, Provider<StatusBarStateController> provider3, Provider<ModalRowInflater> provider4) {
        ModalController modalController = new ModalController(provider.get(), provider2.get(), provider3.get());
        ModalController_MembersInjector.injectModalRowInflater(modalController, provider4.get());
        return modalController;
    }

    public static ModalController_Factory create(Provider<Context> provider, Provider<StatusBar> provider2, Provider<StatusBarStateController> provider3, Provider<ModalRowInflater> provider4) {
        return new ModalController_Factory(provider, provider2, provider3, provider4);
    }
}
