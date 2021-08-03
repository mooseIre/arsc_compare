package com.android.systemui.statusbar.notification.unimportant;

import android.content.Context;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class FoldNotifController_Factory implements Factory<FoldNotifController> {
    private final Provider<Context> contextProvider;
    private final Provider<SysuiStatusBarStateController> statusBarStateControllerProvider;

    public FoldNotifController_Factory(Provider<Context> provider, Provider<SysuiStatusBarStateController> provider2) {
        this.contextProvider = provider;
        this.statusBarStateControllerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public FoldNotifController get() {
        return provideInstance(this.contextProvider, this.statusBarStateControllerProvider);
    }

    public static FoldNotifController provideInstance(Provider<Context> provider, Provider<SysuiStatusBarStateController> provider2) {
        return new FoldNotifController(provider.get(), provider2.get());
    }

    public static FoldNotifController_Factory create(Provider<Context> provider, Provider<SysuiStatusBarStateController> provider2) {
        return new FoldNotifController_Factory(provider, provider2);
    }
}
