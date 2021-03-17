package com.android.systemui.statusbar.dagger;

import android.content.Context;
import android.os.Handler;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.ActionClickLogger;
import com.android.systemui.statusbar.NotificationClickNotifier;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.SmartReplyController;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.RemoteInputUriController;
import dagger.Lazy;
import dagger.internal.DoubleCheck;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class StatusBarDependenciesModule_ProvideNotificationRemoteInputManagerFactory implements Factory<NotificationRemoteInputManager> {
    private final Provider<ActionClickLogger> actionClickLoggerProvider;
    private final Provider<NotificationClickNotifier> clickNotifierProvider;
    private final Provider<Context> contextProvider;
    private final Provider<NotificationLockscreenUserManager> lockscreenUserManagerProvider;
    private final Provider<Handler> mainHandlerProvider;
    private final Provider<NotificationEntryManager> notificationEntryManagerProvider;
    private final Provider<RemoteInputUriController> remoteInputUriControllerProvider;
    private final Provider<SmartReplyController> smartReplyControllerProvider;
    private final Provider<StatusBar> statusBarLazyProvider;
    private final Provider<StatusBarStateController> statusBarStateControllerProvider;

    public StatusBarDependenciesModule_ProvideNotificationRemoteInputManagerFactory(Provider<Context> provider, Provider<NotificationLockscreenUserManager> provider2, Provider<SmartReplyController> provider3, Provider<NotificationEntryManager> provider4, Provider<StatusBar> provider5, Provider<StatusBarStateController> provider6, Provider<Handler> provider7, Provider<RemoteInputUriController> provider8, Provider<NotificationClickNotifier> provider9, Provider<ActionClickLogger> provider10) {
        this.contextProvider = provider;
        this.lockscreenUserManagerProvider = provider2;
        this.smartReplyControllerProvider = provider3;
        this.notificationEntryManagerProvider = provider4;
        this.statusBarLazyProvider = provider5;
        this.statusBarStateControllerProvider = provider6;
        this.mainHandlerProvider = provider7;
        this.remoteInputUriControllerProvider = provider8;
        this.clickNotifierProvider = provider9;
        this.actionClickLoggerProvider = provider10;
    }

    @Override // javax.inject.Provider
    public NotificationRemoteInputManager get() {
        return provideInstance(this.contextProvider, this.lockscreenUserManagerProvider, this.smartReplyControllerProvider, this.notificationEntryManagerProvider, this.statusBarLazyProvider, this.statusBarStateControllerProvider, this.mainHandlerProvider, this.remoteInputUriControllerProvider, this.clickNotifierProvider, this.actionClickLoggerProvider);
    }

    public static NotificationRemoteInputManager provideInstance(Provider<Context> provider, Provider<NotificationLockscreenUserManager> provider2, Provider<SmartReplyController> provider3, Provider<NotificationEntryManager> provider4, Provider<StatusBar> provider5, Provider<StatusBarStateController> provider6, Provider<Handler> provider7, Provider<RemoteInputUriController> provider8, Provider<NotificationClickNotifier> provider9, Provider<ActionClickLogger> provider10) {
        return proxyProvideNotificationRemoteInputManager(provider.get(), provider2.get(), provider3.get(), provider4.get(), DoubleCheck.lazy(provider5), provider6.get(), provider7.get(), provider8.get(), provider9.get(), provider10.get());
    }

    public static StatusBarDependenciesModule_ProvideNotificationRemoteInputManagerFactory create(Provider<Context> provider, Provider<NotificationLockscreenUserManager> provider2, Provider<SmartReplyController> provider3, Provider<NotificationEntryManager> provider4, Provider<StatusBar> provider5, Provider<StatusBarStateController> provider6, Provider<Handler> provider7, Provider<RemoteInputUriController> provider8, Provider<NotificationClickNotifier> provider9, Provider<ActionClickLogger> provider10) {
        return new StatusBarDependenciesModule_ProvideNotificationRemoteInputManagerFactory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10);
    }

    public static NotificationRemoteInputManager proxyProvideNotificationRemoteInputManager(Context context, NotificationLockscreenUserManager notificationLockscreenUserManager, SmartReplyController smartReplyController, NotificationEntryManager notificationEntryManager, Lazy<StatusBar> lazy, StatusBarStateController statusBarStateController, Handler handler, RemoteInputUriController remoteInputUriController, NotificationClickNotifier notificationClickNotifier, ActionClickLogger actionClickLogger) {
        NotificationRemoteInputManager provideNotificationRemoteInputManager = StatusBarDependenciesModule.provideNotificationRemoteInputManager(context, notificationLockscreenUserManager, smartReplyController, notificationEntryManager, lazy, statusBarStateController, handler, remoteInputUriController, notificationClickNotifier, actionClickLogger);
        Preconditions.checkNotNull(provideNotificationRemoteInputManager, "Cannot return null from a non-@Nullable @Provides method");
        return provideNotificationRemoteInputManager;
    }
}
