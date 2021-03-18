package com.android.systemui.screenshot;

import android.os.UserManager;
import com.android.internal.logging.UiEventLogger;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class TakeScreenshotService_Factory implements Factory<TakeScreenshotService> {
    private final Provider<GlobalScreenshot> globalScreenshotProvider;
    private final Provider<UiEventLogger> uiEventLoggerProvider;
    private final Provider<UserManager> userManagerProvider;

    public TakeScreenshotService_Factory(Provider<GlobalScreenshot> provider, Provider<UserManager> provider2, Provider<UiEventLogger> provider3) {
        this.globalScreenshotProvider = provider;
        this.userManagerProvider = provider2;
        this.uiEventLoggerProvider = provider3;
    }

    @Override // javax.inject.Provider
    public TakeScreenshotService get() {
        return provideInstance(this.globalScreenshotProvider, this.userManagerProvider, this.uiEventLoggerProvider);
    }

    public static TakeScreenshotService provideInstance(Provider<GlobalScreenshot> provider, Provider<UserManager> provider2, Provider<UiEventLogger> provider3) {
        return new TakeScreenshotService(provider.get(), provider2.get(), provider3.get());
    }

    public static TakeScreenshotService_Factory create(Provider<GlobalScreenshot> provider, Provider<UserManager> provider2, Provider<UiEventLogger> provider3) {
        return new TakeScreenshotService_Factory(provider, provider2, provider3);
    }
}
