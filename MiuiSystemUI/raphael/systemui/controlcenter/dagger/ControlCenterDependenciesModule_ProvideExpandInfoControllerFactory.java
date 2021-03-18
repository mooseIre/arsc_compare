package com.android.systemui.controlcenter.dagger;

import android.content.Context;
import com.android.systemui.controlcenter.phone.ExpandInfoController;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class ControlCenterDependenciesModule_ProvideExpandInfoControllerFactory implements Factory<ExpandInfoController> {
    private final Provider<Context> contextProvider;

    public ControlCenterDependenciesModule_ProvideExpandInfoControllerFactory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public ExpandInfoController get() {
        return provideInstance(this.contextProvider);
    }

    public static ExpandInfoController provideInstance(Provider<Context> provider) {
        return proxyProvideExpandInfoController(provider.get());
    }

    public static ControlCenterDependenciesModule_ProvideExpandInfoControllerFactory create(Provider<Context> provider) {
        return new ControlCenterDependenciesModule_ProvideExpandInfoControllerFactory(provider);
    }

    public static ExpandInfoController proxyProvideExpandInfoController(Context context) {
        ExpandInfoController provideExpandInfoController = ControlCenterDependenciesModule.provideExpandInfoController(context);
        Preconditions.checkNotNull(provideExpandInfoController, "Cannot return null from a non-@Nullable @Provides method");
        return provideExpandInfoController;
    }
}
