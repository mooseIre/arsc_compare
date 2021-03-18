package com.android.systemui.statusbar.phone;

import com.android.systemui.doze.DozeLog;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class DozeScrimController_Factory implements Factory<DozeScrimController> {
    private final Provider<DozeLog> dozeLogProvider;
    private final Provider<DozeParameters> dozeParametersProvider;

    public DozeScrimController_Factory(Provider<DozeParameters> provider, Provider<DozeLog> provider2) {
        this.dozeParametersProvider = provider;
        this.dozeLogProvider = provider2;
    }

    @Override // javax.inject.Provider
    public DozeScrimController get() {
        return provideInstance(this.dozeParametersProvider, this.dozeLogProvider);
    }

    public static DozeScrimController provideInstance(Provider<DozeParameters> provider, Provider<DozeLog> provider2) {
        return new DozeScrimController(provider.get(), provider2.get());
    }

    public static DozeScrimController_Factory create(Provider<DozeParameters> provider, Provider<DozeLog> provider2) {
        return new DozeScrimController_Factory(provider, provider2);
    }
}
