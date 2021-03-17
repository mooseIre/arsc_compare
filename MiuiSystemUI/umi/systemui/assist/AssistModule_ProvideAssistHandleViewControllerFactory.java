package com.android.systemui.assist;

import com.android.systemui.statusbar.NavigationBarController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class AssistModule_ProvideAssistHandleViewControllerFactory implements Factory<AssistHandleViewController> {
    private final Provider<NavigationBarController> navigationBarControllerProvider;

    public AssistModule_ProvideAssistHandleViewControllerFactory(Provider<NavigationBarController> provider) {
        this.navigationBarControllerProvider = provider;
    }

    @Override // javax.inject.Provider
    public AssistHandleViewController get() {
        return provideInstance(this.navigationBarControllerProvider);
    }

    public static AssistHandleViewController provideInstance(Provider<NavigationBarController> provider) {
        return proxyProvideAssistHandleViewController(provider.get());
    }

    public static AssistModule_ProvideAssistHandleViewControllerFactory create(Provider<NavigationBarController> provider) {
        return new AssistModule_ProvideAssistHandleViewControllerFactory(provider);
    }

    public static AssistHandleViewController proxyProvideAssistHandleViewController(NavigationBarController navigationBarController) {
        return AssistModule.provideAssistHandleViewController(navigationBarController);
    }
}
