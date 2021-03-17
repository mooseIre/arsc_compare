package com.android.systemui.assist.ui;

import android.content.Context;
import com.android.systemui.assist.AssistLogger;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class DefaultUiController_Factory implements Factory<DefaultUiController> {
    private final Provider<AssistLogger> assistLoggerProvider;
    private final Provider<Context> contextProvider;

    public DefaultUiController_Factory(Provider<Context> provider, Provider<AssistLogger> provider2) {
        this.contextProvider = provider;
        this.assistLoggerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public DefaultUiController get() {
        return provideInstance(this.contextProvider, this.assistLoggerProvider);
    }

    public static DefaultUiController provideInstance(Provider<Context> provider, Provider<AssistLogger> provider2) {
        return new DefaultUiController(provider.get(), provider2.get());
    }

    public static DefaultUiController_Factory create(Provider<Context> provider, Provider<AssistLogger> provider2) {
        return new DefaultUiController_Factory(provider, provider2);
    }
}
