package com.android.systemui.statusbar;

import com.android.systemui.log.LogBuffer;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ActionClickLogger_Factory implements Factory<ActionClickLogger> {
    private final Provider<LogBuffer> bufferProvider;

    public ActionClickLogger_Factory(Provider<LogBuffer> provider) {
        this.bufferProvider = provider;
    }

    @Override // javax.inject.Provider
    public ActionClickLogger get() {
        return provideInstance(this.bufferProvider);
    }

    public static ActionClickLogger provideInstance(Provider<LogBuffer> provider) {
        return new ActionClickLogger(provider.get());
    }

    public static ActionClickLogger_Factory create(Provider<LogBuffer> provider) {
        return new ActionClickLogger_Factory(provider);
    }
}
