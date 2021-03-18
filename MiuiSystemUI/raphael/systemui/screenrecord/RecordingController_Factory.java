package com.android.systemui.screenrecord;

import com.android.systemui.broadcast.BroadcastDispatcher;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class RecordingController_Factory implements Factory<RecordingController> {
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;

    public RecordingController_Factory(Provider<BroadcastDispatcher> provider) {
        this.broadcastDispatcherProvider = provider;
    }

    @Override // javax.inject.Provider
    public RecordingController get() {
        return provideInstance(this.broadcastDispatcherProvider);
    }

    public static RecordingController provideInstance(Provider<BroadcastDispatcher> provider) {
        return new RecordingController(provider.get());
    }

    public static RecordingController_Factory create(Provider<BroadcastDispatcher> provider) {
        return new RecordingController_Factory(provider);
    }
}
