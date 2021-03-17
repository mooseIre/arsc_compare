package com.android.systemui;

import android.content.Context;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.miui.systemui.util.PackageEventController;
import dagger.internal.DoubleCheck;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiWallpaperZoomOutService_Factory implements Factory<MiuiWallpaperZoomOutService> {
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<Context> contextProvider;
    private final Provider<DeviceProvisionedController> deviceProvisionedControllerProvider;
    private final Provider<PackageEventController> packageEventControllerProvider;
    private final Provider<StatusBar> statusBarLazyProvider;

    public MiuiWallpaperZoomOutService_Factory(Provider<Context> provider, Provider<StatusBar> provider2, Provider<DeviceProvisionedController> provider3, Provider<BroadcastDispatcher> provider4, Provider<PackageEventController> provider5) {
        this.contextProvider = provider;
        this.statusBarLazyProvider = provider2;
        this.deviceProvisionedControllerProvider = provider3;
        this.broadcastDispatcherProvider = provider4;
        this.packageEventControllerProvider = provider5;
    }

    @Override // javax.inject.Provider
    public MiuiWallpaperZoomOutService get() {
        return provideInstance(this.contextProvider, this.statusBarLazyProvider, this.deviceProvisionedControllerProvider, this.broadcastDispatcherProvider, this.packageEventControllerProvider);
    }

    public static MiuiWallpaperZoomOutService provideInstance(Provider<Context> provider, Provider<StatusBar> provider2, Provider<DeviceProvisionedController> provider3, Provider<BroadcastDispatcher> provider4, Provider<PackageEventController> provider5) {
        return new MiuiWallpaperZoomOutService(provider.get(), DoubleCheck.lazy(provider2), provider3.get(), provider4.get(), provider5.get());
    }

    public static MiuiWallpaperZoomOutService_Factory create(Provider<Context> provider, Provider<StatusBar> provider2, Provider<DeviceProvisionedController> provider3, Provider<BroadcastDispatcher> provider4, Provider<PackageEventController> provider5) {
        return new MiuiWallpaperZoomOutService_Factory(provider, provider2, provider3, provider4, provider5);
    }
}
