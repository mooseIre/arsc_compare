package com.android.systemui.screenshot;

import android.content.Context;
import android.content.res.Resources;
import com.android.internal.logging.UiEventLogger;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class GlobalScreenshot_Factory implements Factory<GlobalScreenshot> {
    private final Provider<Context> contextProvider;
    private final Provider<Resources> resourcesProvider;
    private final Provider<ScreenshotNotificationsController> screenshotNotificationsControllerProvider;
    private final Provider<UiEventLogger> uiEventLoggerProvider;

    public GlobalScreenshot_Factory(Provider<Context> provider, Provider<Resources> provider2, Provider<ScreenshotNotificationsController> provider3, Provider<UiEventLogger> provider4) {
        this.contextProvider = provider;
        this.resourcesProvider = provider2;
        this.screenshotNotificationsControllerProvider = provider3;
        this.uiEventLoggerProvider = provider4;
    }

    @Override // javax.inject.Provider
    public GlobalScreenshot get() {
        return provideInstance(this.contextProvider, this.resourcesProvider, this.screenshotNotificationsControllerProvider, this.uiEventLoggerProvider);
    }

    public static GlobalScreenshot provideInstance(Provider<Context> provider, Provider<Resources> provider2, Provider<ScreenshotNotificationsController> provider3, Provider<UiEventLogger> provider4) {
        return new GlobalScreenshot(provider.get(), provider2.get(), provider3.get(), provider4.get());
    }

    public static GlobalScreenshot_Factory create(Provider<Context> provider, Provider<Resources> provider2, Provider<ScreenshotNotificationsController> provider3, Provider<UiEventLogger> provider4) {
        return new GlobalScreenshot_Factory(provider, provider2, provider3, provider4);
    }
}
