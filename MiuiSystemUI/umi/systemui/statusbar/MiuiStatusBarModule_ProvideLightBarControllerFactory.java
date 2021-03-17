package com.android.systemui.statusbar;

import android.content.Context;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.phone.LightBarController;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.android.systemui.statusbar.policy.BatteryController;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class MiuiStatusBarModule_ProvideLightBarControllerFactory implements Factory<LightBarController> {
    private final Provider<BatteryController> batteryControllerProvider;
    private final Provider<Context> ctxProvider;
    private final Provider<DarkIconDispatcher> darkIconDispatcherProvider;
    private final MiuiStatusBarModule module;
    private final Provider<NavigationModeController> navModeControllerProvider;

    public MiuiStatusBarModule_ProvideLightBarControllerFactory(MiuiStatusBarModule miuiStatusBarModule, Provider<Context> provider, Provider<DarkIconDispatcher> provider2, Provider<BatteryController> provider3, Provider<NavigationModeController> provider4) {
        this.module = miuiStatusBarModule;
        this.ctxProvider = provider;
        this.darkIconDispatcherProvider = provider2;
        this.batteryControllerProvider = provider3;
        this.navModeControllerProvider = provider4;
    }

    @Override // javax.inject.Provider
    public LightBarController get() {
        return provideInstance(this.module, this.ctxProvider, this.darkIconDispatcherProvider, this.batteryControllerProvider, this.navModeControllerProvider);
    }

    public static LightBarController provideInstance(MiuiStatusBarModule miuiStatusBarModule, Provider<Context> provider, Provider<DarkIconDispatcher> provider2, Provider<BatteryController> provider3, Provider<NavigationModeController> provider4) {
        return proxyProvideLightBarController(miuiStatusBarModule, provider.get(), provider2.get(), provider3.get(), provider4.get());
    }

    public static MiuiStatusBarModule_ProvideLightBarControllerFactory create(MiuiStatusBarModule miuiStatusBarModule, Provider<Context> provider, Provider<DarkIconDispatcher> provider2, Provider<BatteryController> provider3, Provider<NavigationModeController> provider4) {
        return new MiuiStatusBarModule_ProvideLightBarControllerFactory(miuiStatusBarModule, provider, provider2, provider3, provider4);
    }

    public static LightBarController proxyProvideLightBarController(MiuiStatusBarModule miuiStatusBarModule, Context context, DarkIconDispatcher darkIconDispatcher, BatteryController batteryController, NavigationModeController navigationModeController) {
        LightBarController provideLightBarController = miuiStatusBarModule.provideLightBarController(context, darkIconDispatcher, batteryController, navigationModeController);
        Preconditions.checkNotNull(provideLightBarController, "Cannot return null from a non-@Nullable @Provides method");
        return provideLightBarController;
    }
}
