package com.android.systemui.screenshot;

import android.content.Context;
import android.view.WindowManager;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ScreenshotNotificationsController_Factory implements Factory<ScreenshotNotificationsController> {
    private final Provider<Context> contextProvider;
    private final Provider<WindowManager> windowManagerProvider;

    public ScreenshotNotificationsController_Factory(Provider<Context> provider, Provider<WindowManager> provider2) {
        this.contextProvider = provider;
        this.windowManagerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public ScreenshotNotificationsController get() {
        return provideInstance(this.contextProvider, this.windowManagerProvider);
    }

    public static ScreenshotNotificationsController provideInstance(Provider<Context> provider, Provider<WindowManager> provider2) {
        return new ScreenshotNotificationsController(provider.get(), provider2.get());
    }

    public static ScreenshotNotificationsController_Factory create(Provider<Context> provider, Provider<WindowManager> provider2) {
        return new ScreenshotNotificationsController_Factory(provider, provider2);
    }
}
