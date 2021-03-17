package com.android.systemui.pip.tv;

import com.android.systemui.pip.tv.dagger.TvPipComponent;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class PipMenuActivity_Factory implements Factory<PipMenuActivity> {
    private final Provider<TvPipComponent.Builder> pipComponentBuilderProvider;
    private final Provider<PipManager> pipManagerProvider;

    public PipMenuActivity_Factory(Provider<TvPipComponent.Builder> provider, Provider<PipManager> provider2) {
        this.pipComponentBuilderProvider = provider;
        this.pipManagerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public PipMenuActivity get() {
        return provideInstance(this.pipComponentBuilderProvider, this.pipManagerProvider);
    }

    public static PipMenuActivity provideInstance(Provider<TvPipComponent.Builder> provider, Provider<PipManager> provider2) {
        return new PipMenuActivity(provider.get(), provider2.get());
    }

    public static PipMenuActivity_Factory create(Provider<TvPipComponent.Builder> provider, Provider<PipManager> provider2) {
        return new PipMenuActivity_Factory(provider, provider2);
    }
}
