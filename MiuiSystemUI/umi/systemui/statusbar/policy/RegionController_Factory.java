package com.android.systemui.statusbar.policy;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class RegionController_Factory implements Factory<RegionController> {
    private final Provider<Context> contextProvider;

    public RegionController_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public RegionController get() {
        return provideInstance(this.contextProvider);
    }

    public static RegionController provideInstance(Provider<Context> provider) {
        return new RegionController(provider.get());
    }

    public static RegionController_Factory create(Provider<Context> provider) {
        return new RegionController_Factory(provider);
    }
}
