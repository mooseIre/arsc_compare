package com.android.systemui.statusbar.phone;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiStatusBarPromptController_Factory implements Factory<MiuiStatusBarPromptController> {
    private final Provider<Context> contextProvider;

    public MiuiStatusBarPromptController_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public MiuiStatusBarPromptController get() {
        return provideInstance(this.contextProvider);
    }

    public static MiuiStatusBarPromptController provideInstance(Provider<Context> provider) {
        return new MiuiStatusBarPromptController(provider.get());
    }

    public static MiuiStatusBarPromptController_Factory create(Provider<Context> provider) {
        return new MiuiStatusBarPromptController_Factory(provider);
    }
}
