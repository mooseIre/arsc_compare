package com.android.systemui.statusbar.notification.mediacontrol;

import android.content.Context;
import com.android.systemui.controlcenter.phone.ControlPanelController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiMediaTransferManager_Factory implements Factory<MiuiMediaTransferManager> {
    private final Provider<Context> contextProvider;
    private final Provider<ControlPanelController> controlPanelControllerProvider;

    public MiuiMediaTransferManager_Factory(Provider<Context> provider, Provider<ControlPanelController> provider2) {
        this.contextProvider = provider;
        this.controlPanelControllerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public MiuiMediaTransferManager get() {
        return provideInstance(this.contextProvider, this.controlPanelControllerProvider);
    }

    public static MiuiMediaTransferManager provideInstance(Provider<Context> provider, Provider<ControlPanelController> provider2) {
        MiuiMediaTransferManager miuiMediaTransferManager = new MiuiMediaTransferManager(provider.get());
        MiuiMediaTransferManager_MembersInjector.injectControlPanelController(miuiMediaTransferManager, provider2.get());
        return miuiMediaTransferManager;
    }

    public static MiuiMediaTransferManager_Factory create(Provider<Context> provider, Provider<ControlPanelController> provider2) {
        return new MiuiMediaTransferManager_Factory(provider, provider2);
    }
}
