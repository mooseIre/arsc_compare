package com.android.systemui.statusbar.dagger;

import android.content.Context;
import com.android.systemui.media.MediaDataManager;
import com.android.systemui.statusbar.MediaArtworkProcessor;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.statusbar.phone.NotificationShadeWindowController;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.util.DeviceConfigProxy;
import com.android.systemui.util.concurrency.DelayableExecutor;
import dagger.Lazy;
import dagger.internal.DoubleCheck;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class StatusBarDependenciesModule_ProvideNotificationMediaManagerFactory implements Factory<NotificationMediaManager> {
    private final Provider<Context> contextProvider;
    private final Provider<DeviceConfigProxy> deviceConfigProxyProvider;
    private final Provider<KeyguardBypassController> keyguardBypassControllerProvider;
    private final Provider<DelayableExecutor> mainExecutorProvider;
    private final Provider<MediaArtworkProcessor> mediaArtworkProcessorProvider;
    private final Provider<MediaDataManager> mediaDataManagerProvider;
    private final Provider<NotificationEntryManager> notificationEntryManagerProvider;
    private final Provider<NotificationShadeWindowController> notificationShadeWindowControllerProvider;
    private final Provider<StatusBar> statusBarLazyProvider;

    public StatusBarDependenciesModule_ProvideNotificationMediaManagerFactory(Provider<Context> provider, Provider<StatusBar> provider2, Provider<NotificationShadeWindowController> provider3, Provider<NotificationEntryManager> provider4, Provider<MediaArtworkProcessor> provider5, Provider<KeyguardBypassController> provider6, Provider<DelayableExecutor> provider7, Provider<DeviceConfigProxy> provider8, Provider<MediaDataManager> provider9) {
        this.contextProvider = provider;
        this.statusBarLazyProvider = provider2;
        this.notificationShadeWindowControllerProvider = provider3;
        this.notificationEntryManagerProvider = provider4;
        this.mediaArtworkProcessorProvider = provider5;
        this.keyguardBypassControllerProvider = provider6;
        this.mainExecutorProvider = provider7;
        this.deviceConfigProxyProvider = provider8;
        this.mediaDataManagerProvider = provider9;
    }

    @Override // javax.inject.Provider
    public NotificationMediaManager get() {
        return provideInstance(this.contextProvider, this.statusBarLazyProvider, this.notificationShadeWindowControllerProvider, this.notificationEntryManagerProvider, this.mediaArtworkProcessorProvider, this.keyguardBypassControllerProvider, this.mainExecutorProvider, this.deviceConfigProxyProvider, this.mediaDataManagerProvider);
    }

    public static NotificationMediaManager provideInstance(Provider<Context> provider, Provider<StatusBar> provider2, Provider<NotificationShadeWindowController> provider3, Provider<NotificationEntryManager> provider4, Provider<MediaArtworkProcessor> provider5, Provider<KeyguardBypassController> provider6, Provider<DelayableExecutor> provider7, Provider<DeviceConfigProxy> provider8, Provider<MediaDataManager> provider9) {
        return proxyProvideNotificationMediaManager(provider.get(), DoubleCheck.lazy(provider2), DoubleCheck.lazy(provider3), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), provider9.get());
    }

    public static StatusBarDependenciesModule_ProvideNotificationMediaManagerFactory create(Provider<Context> provider, Provider<StatusBar> provider2, Provider<NotificationShadeWindowController> provider3, Provider<NotificationEntryManager> provider4, Provider<MediaArtworkProcessor> provider5, Provider<KeyguardBypassController> provider6, Provider<DelayableExecutor> provider7, Provider<DeviceConfigProxy> provider8, Provider<MediaDataManager> provider9) {
        return new StatusBarDependenciesModule_ProvideNotificationMediaManagerFactory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9);
    }

    public static NotificationMediaManager proxyProvideNotificationMediaManager(Context context, Lazy<StatusBar> lazy, Lazy<NotificationShadeWindowController> lazy2, NotificationEntryManager notificationEntryManager, MediaArtworkProcessor mediaArtworkProcessor, KeyguardBypassController keyguardBypassController, DelayableExecutor delayableExecutor, DeviceConfigProxy deviceConfigProxy, MediaDataManager mediaDataManager) {
        NotificationMediaManager provideNotificationMediaManager = StatusBarDependenciesModule.provideNotificationMediaManager(context, lazy, lazy2, notificationEntryManager, mediaArtworkProcessor, keyguardBypassController, delayableExecutor, deviceConfigProxy, mediaDataManager);
        Preconditions.checkNotNull(provideNotificationMediaManager, "Cannot return null from a non-@Nullable @Provides method");
        return provideNotificationMediaManager;
    }
}
