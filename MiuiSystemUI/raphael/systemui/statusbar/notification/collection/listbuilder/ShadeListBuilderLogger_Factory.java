package com.android.systemui.statusbar.notification.collection.listbuilder;

import com.android.systemui.log.LogBuffer;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ShadeListBuilderLogger_Factory implements Factory<ShadeListBuilderLogger> {
    private final Provider<LogBuffer> bufferProvider;

    public ShadeListBuilderLogger_Factory(Provider<LogBuffer> provider) {
        this.bufferProvider = provider;
    }

    @Override // javax.inject.Provider
    public ShadeListBuilderLogger get() {
        return provideInstance(this.bufferProvider);
    }

    public static ShadeListBuilderLogger provideInstance(Provider<LogBuffer> provider) {
        return new ShadeListBuilderLogger(provider.get());
    }

    public static ShadeListBuilderLogger_Factory create(Provider<LogBuffer> provider) {
        return new ShadeListBuilderLogger_Factory(provider);
    }
}
