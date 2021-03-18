package com.android.systemui.volume;

import android.content.Context;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.util.RingerModeTracker;
import dagger.Lazy;
import dagger.internal.Factory;
import java.util.Optional;
import javax.inject.Provider;

public final class VolumeDialogControllerImpl_Factory implements Factory<VolumeDialogControllerImpl> {
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<Context> contextProvider;
    private final Provider<RingerModeTracker> ringerModeTrackerProvider;
    private final Provider<Optional<Lazy<StatusBar>>> statusBarOptionalLazyProvider;

    public VolumeDialogControllerImpl_Factory(Provider<Context> provider, Provider<BroadcastDispatcher> provider2, Provider<Optional<Lazy<StatusBar>>> provider3, Provider<RingerModeTracker> provider4) {
        this.contextProvider = provider;
        this.broadcastDispatcherProvider = provider2;
        this.statusBarOptionalLazyProvider = provider3;
        this.ringerModeTrackerProvider = provider4;
    }

    @Override // javax.inject.Provider
    public VolumeDialogControllerImpl get() {
        return provideInstance(this.contextProvider, this.broadcastDispatcherProvider, this.statusBarOptionalLazyProvider, this.ringerModeTrackerProvider);
    }

    public static VolumeDialogControllerImpl provideInstance(Provider<Context> provider, Provider<BroadcastDispatcher> provider2, Provider<Optional<Lazy<StatusBar>>> provider3, Provider<RingerModeTracker> provider4) {
        return new VolumeDialogControllerImpl(provider.get(), provider2.get(), provider3.get(), provider4.get());
    }

    public static VolumeDialogControllerImpl_Factory create(Provider<Context> provider, Provider<BroadcastDispatcher> provider2, Provider<Optional<Lazy<StatusBar>>> provider3, Provider<RingerModeTracker> provider4) {
        return new VolumeDialogControllerImpl_Factory(provider, provider2, provider3, provider4);
    }
}
