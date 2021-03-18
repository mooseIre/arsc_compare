package com.android.systemui.screenrecord;

import com.android.systemui.settings.CurrentUserContextTracker;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ScreenRecordDialog_Factory implements Factory<ScreenRecordDialog> {
    private final Provider<RecordingController> controllerProvider;
    private final Provider<CurrentUserContextTracker> currentUserContextTrackerProvider;

    public ScreenRecordDialog_Factory(Provider<RecordingController> provider, Provider<CurrentUserContextTracker> provider2) {
        this.controllerProvider = provider;
        this.currentUserContextTrackerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public ScreenRecordDialog get() {
        return provideInstance(this.controllerProvider, this.currentUserContextTrackerProvider);
    }

    public static ScreenRecordDialog provideInstance(Provider<RecordingController> provider, Provider<CurrentUserContextTracker> provider2) {
        return new ScreenRecordDialog(provider.get(), provider2.get());
    }

    public static ScreenRecordDialog_Factory create(Provider<RecordingController> provider, Provider<CurrentUserContextTracker> provider2) {
        return new ScreenRecordDialog_Factory(provider, provider2);
    }
}
