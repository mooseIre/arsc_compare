package com.android.systemui.statusbar.notification.modal;

import android.content.Context;
import com.android.systemui.controlcenter.ControlCenter;
import com.android.systemui.controlcenter.phone.controls.MiPlayPluginManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.phone.StatusBar;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ModalController_Factory implements Factory<ModalController> {
    private final Provider<Context> contextProvider;
    private final Provider<ControlCenter> controllCenterProvider;
    private final Provider<StatusBarStateController> mStatusBarStateControllerProvider;
    private final Provider<MiPlayPluginManager> miPlayPluginManagerProvider;
    private final Provider<ModalRowInflater> modalRowInflaterProvider;
    private final Provider<StatusBar> statusBarProvider;

    public ModalController_Factory(Provider<Context> provider, Provider<StatusBar> provider2, Provider<StatusBarStateController> provider3, Provider<MiPlayPluginManager> provider4, Provider<ModalRowInflater> provider5, Provider<ControlCenter> provider6) {
        this.contextProvider = provider;
        this.statusBarProvider = provider2;
        this.mStatusBarStateControllerProvider = provider3;
        this.miPlayPluginManagerProvider = provider4;
        this.modalRowInflaterProvider = provider5;
        this.controllCenterProvider = provider6;
    }

    @Override // javax.inject.Provider
    public ModalController get() {
        return provideInstance(this.contextProvider, this.statusBarProvider, this.mStatusBarStateControllerProvider, this.miPlayPluginManagerProvider, this.modalRowInflaterProvider, this.controllCenterProvider);
    }

    public static ModalController provideInstance(Provider<Context> provider, Provider<StatusBar> provider2, Provider<StatusBarStateController> provider3, Provider<MiPlayPluginManager> provider4, Provider<ModalRowInflater> provider5, Provider<ControlCenter> provider6) {
        ModalController modalController = new ModalController(provider.get(), provider2.get(), provider3.get(), provider4.get());
        ModalController_MembersInjector.injectModalRowInflater(modalController, provider5.get());
        ModalController_MembersInjector.injectControllCenter(modalController, provider6.get());
        return modalController;
    }

    public static ModalController_Factory create(Provider<Context> provider, Provider<StatusBar> provider2, Provider<StatusBarStateController> provider3, Provider<MiPlayPluginManager> provider4, Provider<ModalRowInflater> provider5, Provider<ControlCenter> provider6) {
        return new ModalController_Factory(provider, provider2, provider3, provider4, provider5, provider6);
    }
}
