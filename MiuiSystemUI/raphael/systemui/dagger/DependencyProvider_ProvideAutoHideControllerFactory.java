package com.android.systemui.dagger;

import android.content.Context;
import android.os.Handler;
import android.view.IWindowManager;
import com.android.systemui.statusbar.phone.AutoHideController;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class DependencyProvider_ProvideAutoHideControllerFactory implements Factory<AutoHideController> {
    private final Provider<Context> contextProvider;
    private final Provider<IWindowManager> iWindowManagerProvider;
    private final Provider<Handler> mainHandlerProvider;
    private final DependencyProvider module;

    public DependencyProvider_ProvideAutoHideControllerFactory(DependencyProvider dependencyProvider, Provider<Context> provider, Provider<Handler> provider2, Provider<IWindowManager> provider3) {
        this.module = dependencyProvider;
        this.contextProvider = provider;
        this.mainHandlerProvider = provider2;
        this.iWindowManagerProvider = provider3;
    }

    @Override // javax.inject.Provider
    public AutoHideController get() {
        return provideInstance(this.module, this.contextProvider, this.mainHandlerProvider, this.iWindowManagerProvider);
    }

    public static AutoHideController provideInstance(DependencyProvider dependencyProvider, Provider<Context> provider, Provider<Handler> provider2, Provider<IWindowManager> provider3) {
        return proxyProvideAutoHideController(dependencyProvider, provider.get(), provider2.get(), provider3.get());
    }

    public static DependencyProvider_ProvideAutoHideControllerFactory create(DependencyProvider dependencyProvider, Provider<Context> provider, Provider<Handler> provider2, Provider<IWindowManager> provider3) {
        return new DependencyProvider_ProvideAutoHideControllerFactory(dependencyProvider, provider, provider2, provider3);
    }

    public static AutoHideController proxyProvideAutoHideController(DependencyProvider dependencyProvider, Context context, Handler handler, IWindowManager iWindowManager) {
        AutoHideController provideAutoHideController = dependencyProvider.provideAutoHideController(context, handler, iWindowManager);
        Preconditions.checkNotNull(provideAutoHideController, "Cannot return null from a non-@Nullable @Provides method");
        return provideAutoHideController;
    }
}
