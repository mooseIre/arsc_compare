package com.android.systemui.pip;

import com.android.internal.logging.UiEventLogger;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class PipUiEventLogger_Factory implements Factory<PipUiEventLogger> {
    private final Provider<UiEventLogger> uiEventLoggerProvider;

    public PipUiEventLogger_Factory(Provider<UiEventLogger> provider) {
        this.uiEventLoggerProvider = provider;
    }

    @Override // javax.inject.Provider
    public PipUiEventLogger get() {
        return provideInstance(this.uiEventLoggerProvider);
    }

    public static PipUiEventLogger provideInstance(Provider<UiEventLogger> provider) {
        return new PipUiEventLogger(provider.get());
    }

    public static PipUiEventLogger_Factory create(Provider<UiEventLogger> provider) {
        return new PipUiEventLogger_Factory(provider);
    }
}
