package com.android.systemui.statusbar.notification.row;

import com.android.systemui.log.LogBuffer;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class RowContentBindStageLogger_Factory implements Factory<RowContentBindStageLogger> {
    private final Provider<LogBuffer> bufferProvider;

    public RowContentBindStageLogger_Factory(Provider<LogBuffer> provider) {
        this.bufferProvider = provider;
    }

    @Override // javax.inject.Provider
    public RowContentBindStageLogger get() {
        return provideInstance(this.bufferProvider);
    }

    public static RowContentBindStageLogger provideInstance(Provider<LogBuffer> provider) {
        return new RowContentBindStageLogger(provider.get());
    }

    public static RowContentBindStageLogger_Factory create(Provider<LogBuffer> provider) {
        return new RowContentBindStageLogger_Factory(provider);
    }
}
