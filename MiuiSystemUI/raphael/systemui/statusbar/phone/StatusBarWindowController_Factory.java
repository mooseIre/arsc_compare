package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Resources;
import android.view.WindowManager;
import com.android.systemui.statusbar.SuperStatusBarViewFactory;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class StatusBarWindowController_Factory implements Factory<StatusBarWindowController> {
    private final Provider<Context> contextProvider;
    private final Provider<Resources> resourcesProvider;
    private final Provider<SuperStatusBarViewFactory> superStatusBarViewFactoryProvider;
    private final Provider<WindowManager> windowManagerProvider;

    public StatusBarWindowController_Factory(Provider<Context> provider, Provider<WindowManager> provider2, Provider<SuperStatusBarViewFactory> provider3, Provider<Resources> provider4) {
        this.contextProvider = provider;
        this.windowManagerProvider = provider2;
        this.superStatusBarViewFactoryProvider = provider3;
        this.resourcesProvider = provider4;
    }

    @Override // javax.inject.Provider
    public StatusBarWindowController get() {
        return provideInstance(this.contextProvider, this.windowManagerProvider, this.superStatusBarViewFactoryProvider, this.resourcesProvider);
    }

    public static StatusBarWindowController provideInstance(Provider<Context> provider, Provider<WindowManager> provider2, Provider<SuperStatusBarViewFactory> provider3, Provider<Resources> provider4) {
        return new StatusBarWindowController(provider.get(), provider2.get(), provider3.get(), provider4.get());
    }

    public static StatusBarWindowController_Factory create(Provider<Context> provider, Provider<WindowManager> provider2, Provider<SuperStatusBarViewFactory> provider3, Provider<Resources> provider4) {
        return new StatusBarWindowController_Factory(provider, provider2, provider3, provider4);
    }
}
