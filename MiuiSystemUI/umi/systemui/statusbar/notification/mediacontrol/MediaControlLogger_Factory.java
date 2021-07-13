package com.android.systemui.statusbar.notification.mediacontrol;

import com.android.systemui.log.LogBuffer;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MediaControlLogger_Factory implements Factory<MediaControlLogger> {
    private final Provider<LogBuffer> bufferProvider;

    public MediaControlLogger_Factory(Provider<LogBuffer> provider) {
        this.bufferProvider = provider;
    }

    @Override // javax.inject.Provider
    public MediaControlLogger get() {
        return provideInstance(this.bufferProvider);
    }

    public static MediaControlLogger provideInstance(Provider<LogBuffer> provider) {
        return new MediaControlLogger(provider.get());
    }

    public static MediaControlLogger_Factory create(Provider<LogBuffer> provider) {
        return new MediaControlLogger_Factory(provider);
    }
}
