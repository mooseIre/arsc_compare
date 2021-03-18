package com.android.systemui.statusbar.notification.row;

import com.android.systemui.log.LogBuffer;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NotifBindPipelineLogger_Factory implements Factory<NotifBindPipelineLogger> {
    private final Provider<LogBuffer> bufferProvider;

    public NotifBindPipelineLogger_Factory(Provider<LogBuffer> provider) {
        this.bufferProvider = provider;
    }

    @Override // javax.inject.Provider
    public NotifBindPipelineLogger get() {
        return provideInstance(this.bufferProvider);
    }

    public static NotifBindPipelineLogger provideInstance(Provider<LogBuffer> provider) {
        return new NotifBindPipelineLogger(provider.get());
    }

    public static NotifBindPipelineLogger_Factory create(Provider<LogBuffer> provider) {
        return new NotifBindPipelineLogger_Factory(provider);
    }
}
