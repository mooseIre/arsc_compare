package com.android.systemui.volume;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class VolumeUI_Factory implements Factory<VolumeUI> {
    private final Provider<Context> contextProvider;
    private final Provider<VolumeDialogComponent> volumeDialogComponentProvider;

    public VolumeUI_Factory(Provider<Context> provider, Provider<VolumeDialogComponent> provider2) {
        this.contextProvider = provider;
        this.volumeDialogComponentProvider = provider2;
    }

    @Override // javax.inject.Provider
    public VolumeUI get() {
        return provideInstance(this.contextProvider, this.volumeDialogComponentProvider);
    }

    public static VolumeUI provideInstance(Provider<Context> provider, Provider<VolumeDialogComponent> provider2) {
        return new VolumeUI(provider.get(), provider2.get());
    }

    public static VolumeUI_Factory create(Provider<Context> provider, Provider<VolumeDialogComponent> provider2) {
        return new VolumeUI_Factory(provider, provider2);
    }
}
