package com.android.systemui.statusbar.dagger;

import com.android.internal.statusbar.IStatusBarService;
import com.android.systemui.statusbar.NotificationClickNotifier;
import com.android.systemui.statusbar.SmartReplyController;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class StatusBarDependenciesModule_ProvideSmartReplyControllerFactory implements Factory<SmartReplyController> {
    private final Provider<NotificationClickNotifier> clickNotifierProvider;
    private final Provider<NotificationEntryManager> entryManagerProvider;
    private final Provider<IStatusBarService> statusBarServiceProvider;

    public StatusBarDependenciesModule_ProvideSmartReplyControllerFactory(Provider<NotificationEntryManager> provider, Provider<IStatusBarService> provider2, Provider<NotificationClickNotifier> provider3) {
        this.entryManagerProvider = provider;
        this.statusBarServiceProvider = provider2;
        this.clickNotifierProvider = provider3;
    }

    @Override // javax.inject.Provider
    public SmartReplyController get() {
        return provideInstance(this.entryManagerProvider, this.statusBarServiceProvider, this.clickNotifierProvider);
    }

    public static SmartReplyController provideInstance(Provider<NotificationEntryManager> provider, Provider<IStatusBarService> provider2, Provider<NotificationClickNotifier> provider3) {
        return proxyProvideSmartReplyController(provider.get(), provider2.get(), provider3.get());
    }

    public static StatusBarDependenciesModule_ProvideSmartReplyControllerFactory create(Provider<NotificationEntryManager> provider, Provider<IStatusBarService> provider2, Provider<NotificationClickNotifier> provider3) {
        return new StatusBarDependenciesModule_ProvideSmartReplyControllerFactory(provider, provider2, provider3);
    }

    public static SmartReplyController proxyProvideSmartReplyController(NotificationEntryManager notificationEntryManager, IStatusBarService iStatusBarService, NotificationClickNotifier notificationClickNotifier) {
        SmartReplyController provideSmartReplyController = StatusBarDependenciesModule.provideSmartReplyController(notificationEntryManager, iStatusBarService, notificationClickNotifier);
        Preconditions.checkNotNull(provideSmartReplyController, "Cannot return null from a non-@Nullable @Provides method");
        return provideSmartReplyController;
    }
}
