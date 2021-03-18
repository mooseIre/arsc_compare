package com.android.systemui.dagger;

import com.android.systemui.statusbar.policy.DataSaverController;
import com.android.systemui.statusbar.policy.NetworkController;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class DependencyProvider_ProvideDataSaverControllerFactory implements Factory<DataSaverController> {
    private final DependencyProvider module;
    private final Provider<NetworkController> networkControllerProvider;

    public DependencyProvider_ProvideDataSaverControllerFactory(DependencyProvider dependencyProvider, Provider<NetworkController> provider) {
        this.module = dependencyProvider;
        this.networkControllerProvider = provider;
    }

    @Override // javax.inject.Provider
    public DataSaverController get() {
        return provideInstance(this.module, this.networkControllerProvider);
    }

    public static DataSaverController provideInstance(DependencyProvider dependencyProvider, Provider<NetworkController> provider) {
        return proxyProvideDataSaverController(dependencyProvider, provider.get());
    }

    public static DependencyProvider_ProvideDataSaverControllerFactory create(DependencyProvider dependencyProvider, Provider<NetworkController> provider) {
        return new DependencyProvider_ProvideDataSaverControllerFactory(dependencyProvider, provider);
    }

    public static DataSaverController proxyProvideDataSaverController(DependencyProvider dependencyProvider, NetworkController networkController) {
        DataSaverController provideDataSaverController = dependencyProvider.provideDataSaverController(networkController);
        Preconditions.checkNotNull(provideDataSaverController, "Cannot return null from a non-@Nullable @Provides method");
        return provideDataSaverController;
    }
}
