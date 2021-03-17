package com.android.systemui.biometrics;

import android.content.Context;
import com.android.systemui.statusbar.CommandQueue;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class AuthController_Factory implements Factory<AuthController> {
    private final Provider<CommandQueue> commandQueueProvider;
    private final Provider<Context> contextProvider;

    public AuthController_Factory(Provider<Context> provider, Provider<CommandQueue> provider2) {
        this.contextProvider = provider;
        this.commandQueueProvider = provider2;
    }

    @Override // javax.inject.Provider
    public AuthController get() {
        return provideInstance(this.contextProvider, this.commandQueueProvider);
    }

    public static AuthController provideInstance(Provider<Context> provider, Provider<CommandQueue> provider2) {
        return new AuthController(provider.get(), provider2.get());
    }

    public static AuthController_Factory create(Provider<Context> provider, Provider<CommandQueue> provider2) {
        return new AuthController_Factory(provider, provider2);
    }
}
