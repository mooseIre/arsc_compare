package com.android.systemui.doze;

import com.android.systemui.log.LogBuffer;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class DozeLogger_Factory implements Factory<DozeLogger> {
    private final Provider<LogBuffer> bufferProvider;

    public DozeLogger_Factory(Provider<LogBuffer> provider) {
        this.bufferProvider = provider;
    }

    @Override // javax.inject.Provider
    public DozeLogger get() {
        return provideInstance(this.bufferProvider);
    }

    public static DozeLogger provideInstance(Provider<LogBuffer> provider) {
        return new DozeLogger(provider.get());
    }

    public static DozeLogger_Factory create(Provider<LogBuffer> provider) {
        return new DozeLogger_Factory(provider);
    }
}
