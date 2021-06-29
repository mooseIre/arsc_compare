package com.android.systemui.statusbar.phone;

import com.android.systemui.log.LogBuffer;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class PanelViewLogger_Factory implements Factory<PanelViewLogger> {
    private final Provider<LogBuffer> bufferProvider;

    public PanelViewLogger_Factory(Provider<LogBuffer> provider) {
        this.bufferProvider = provider;
    }

    @Override // javax.inject.Provider
    public PanelViewLogger get() {
        return provideInstance(this.bufferProvider);
    }

    public static PanelViewLogger provideInstance(Provider<LogBuffer> provider) {
        return new PanelViewLogger(provider.get());
    }

    public static PanelViewLogger_Factory create(Provider<LogBuffer> provider) {
        return new PanelViewLogger_Factory(provider);
    }
}
