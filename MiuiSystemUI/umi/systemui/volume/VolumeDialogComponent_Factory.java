package com.android.systemui.volume;

import android.content.Context;
import com.android.systemui.keyguard.KeyguardViewMediator;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class VolumeDialogComponent_Factory implements Factory<VolumeDialogComponent> {
    private final Provider<Context> contextProvider;
    private final Provider<KeyguardViewMediator> keyguardViewMediatorProvider;
    private final Provider<VolumeDialogControllerImpl> volumeDialogControllerProvider;

    public VolumeDialogComponent_Factory(Provider<Context> provider, Provider<KeyguardViewMediator> provider2, Provider<VolumeDialogControllerImpl> provider3) {
        this.contextProvider = provider;
        this.keyguardViewMediatorProvider = provider2;
        this.volumeDialogControllerProvider = provider3;
    }

    @Override // javax.inject.Provider
    public VolumeDialogComponent get() {
        return provideInstance(this.contextProvider, this.keyguardViewMediatorProvider, this.volumeDialogControllerProvider);
    }

    public static VolumeDialogComponent provideInstance(Provider<Context> provider, Provider<KeyguardViewMediator> provider2, Provider<VolumeDialogControllerImpl> provider3) {
        return new VolumeDialogComponent(provider.get(), provider2.get(), provider3.get());
    }

    public static VolumeDialogComponent_Factory create(Provider<Context> provider, Provider<KeyguardViewMediator> provider2, Provider<VolumeDialogControllerImpl> provider3) {
        return new VolumeDialogComponent_Factory(provider, provider2, provider3);
    }
}
