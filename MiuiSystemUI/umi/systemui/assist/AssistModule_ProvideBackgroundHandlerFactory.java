package com.android.systemui.assist;

import android.os.Handler;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

public final class AssistModule_ProvideBackgroundHandlerFactory implements Factory<Handler> {
    private static final AssistModule_ProvideBackgroundHandlerFactory INSTANCE = new AssistModule_ProvideBackgroundHandlerFactory();

    @Override // javax.inject.Provider
    public Handler get() {
        return provideInstance();
    }

    public static Handler provideInstance() {
        return proxyProvideBackgroundHandler();
    }

    public static AssistModule_ProvideBackgroundHandlerFactory create() {
        return INSTANCE;
    }

    public static Handler proxyProvideBackgroundHandler() {
        Handler provideBackgroundHandler = AssistModule.provideBackgroundHandler();
        Preconditions.checkNotNull(provideBackgroundHandler, "Cannot return null from a non-@Nullable @Provides method");
        return provideBackgroundHandler;
    }
}
