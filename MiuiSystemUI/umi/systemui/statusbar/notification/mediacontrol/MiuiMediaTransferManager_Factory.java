package com.android.systemui.statusbar.notification.mediacontrol;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiMediaTransferManager_Factory implements Factory<MiuiMediaTransferManager> {
    private final Provider<Context> contextProvider;

    public MiuiMediaTransferManager_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public MiuiMediaTransferManager get() {
        return provideInstance(this.contextProvider);
    }

    public static MiuiMediaTransferManager provideInstance(Provider<Context> provider) {
        return new MiuiMediaTransferManager(provider.get());
    }

    public static MiuiMediaTransferManager_Factory create(Provider<Context> provider) {
        return new MiuiMediaTransferManager_Factory(provider);
    }
}
