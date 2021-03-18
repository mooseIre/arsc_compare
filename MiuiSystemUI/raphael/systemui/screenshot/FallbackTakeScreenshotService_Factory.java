package com.android.systemui.screenshot;

import android.os.UserManager;
import com.android.internal.logging.UiEventLogger;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class FallbackTakeScreenshotService_Factory implements Factory<FallbackTakeScreenshotService> {
    private final Provider<GlobalScreenshot> globalScreenshotProvider;
    private final Provider<UiEventLogger> uiEventLoggerProvider;
    private final Provider<UserManager> userManagerProvider;

    public FallbackTakeScreenshotService_Factory(Provider<GlobalScreenshot> provider, Provider<UserManager> provider2, Provider<UiEventLogger> provider3) {
        this.globalScreenshotProvider = provider;
        this.userManagerProvider = provider2;
        this.uiEventLoggerProvider = provider3;
    }

    @Override // javax.inject.Provider
    public FallbackTakeScreenshotService get() {
        return provideInstance(this.globalScreenshotProvider, this.userManagerProvider, this.uiEventLoggerProvider);
    }

    public static FallbackTakeScreenshotService provideInstance(Provider<GlobalScreenshot> provider, Provider<UserManager> provider2, Provider<UiEventLogger> provider3) {
        return new FallbackTakeScreenshotService(provider.get(), provider2.get(), provider3.get());
    }

    public static FallbackTakeScreenshotService_Factory create(Provider<GlobalScreenshot> provider, Provider<UserManager> provider2, Provider<UiEventLogger> provider3) {
        return new FallbackTakeScreenshotService_Factory(provider, provider2, provider3);
    }
}
