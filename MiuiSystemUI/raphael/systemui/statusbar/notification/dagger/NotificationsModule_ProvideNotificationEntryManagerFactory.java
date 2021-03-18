package com.android.systemui.statusbar.notification.dagger;

import com.android.systemui.statusbar.FeatureFlags;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.notification.ForegroundServiceDismissalFeatureController;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationEntryManagerLogger;
import com.android.systemui.statusbar.notification.collection.NotificationRankingManager;
import com.android.systemui.statusbar.notification.collection.inflation.NotificationRowBinder;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.util.leak.LeakDetector;
import dagger.Lazy;
import dagger.internal.DoubleCheck;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class NotificationsModule_ProvideNotificationEntryManagerFactory implements Factory<NotificationEntryManager> {
    private final Provider<FeatureFlags> featureFlagsProvider;
    private final Provider<ForegroundServiceDismissalFeatureController> fgsFeatureControllerProvider;
    private final Provider<NotificationGroupManager> groupManagerProvider;
    private final Provider<NotificationEntryManager.KeyguardEnvironment> keyguardEnvironmentProvider;
    private final Provider<LeakDetector> leakDetectorProvider;
    private final Provider<NotificationEntryManagerLogger> loggerProvider;
    private final Provider<NotificationRemoteInputManager> notificationRemoteInputManagerLazyProvider;
    private final Provider<NotificationRowBinder> notificationRowBinderLazyProvider;
    private final Provider<NotificationRankingManager> rankingManagerProvider;

    public NotificationsModule_ProvideNotificationEntryManagerFactory(Provider<NotificationEntryManagerLogger> provider, Provider<NotificationGroupManager> provider2, Provider<NotificationRankingManager> provider3, Provider<NotificationEntryManager.KeyguardEnvironment> provider4, Provider<FeatureFlags> provider5, Provider<NotificationRowBinder> provider6, Provider<NotificationRemoteInputManager> provider7, Provider<LeakDetector> provider8, Provider<ForegroundServiceDismissalFeatureController> provider9) {
        this.loggerProvider = provider;
        this.groupManagerProvider = provider2;
        this.rankingManagerProvider = provider3;
        this.keyguardEnvironmentProvider = provider4;
        this.featureFlagsProvider = provider5;
        this.notificationRowBinderLazyProvider = provider6;
        this.notificationRemoteInputManagerLazyProvider = provider7;
        this.leakDetectorProvider = provider8;
        this.fgsFeatureControllerProvider = provider9;
    }

    @Override // javax.inject.Provider
    public NotificationEntryManager get() {
        return provideInstance(this.loggerProvider, this.groupManagerProvider, this.rankingManagerProvider, this.keyguardEnvironmentProvider, this.featureFlagsProvider, this.notificationRowBinderLazyProvider, this.notificationRemoteInputManagerLazyProvider, this.leakDetectorProvider, this.fgsFeatureControllerProvider);
    }

    public static NotificationEntryManager provideInstance(Provider<NotificationEntryManagerLogger> provider, Provider<NotificationGroupManager> provider2, Provider<NotificationRankingManager> provider3, Provider<NotificationEntryManager.KeyguardEnvironment> provider4, Provider<FeatureFlags> provider5, Provider<NotificationRowBinder> provider6, Provider<NotificationRemoteInputManager> provider7, Provider<LeakDetector> provider8, Provider<ForegroundServiceDismissalFeatureController> provider9) {
        return proxyProvideNotificationEntryManager(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), DoubleCheck.lazy(provider6), DoubleCheck.lazy(provider7), provider8.get(), provider9.get());
    }

    public static NotificationsModule_ProvideNotificationEntryManagerFactory create(Provider<NotificationEntryManagerLogger> provider, Provider<NotificationGroupManager> provider2, Provider<NotificationRankingManager> provider3, Provider<NotificationEntryManager.KeyguardEnvironment> provider4, Provider<FeatureFlags> provider5, Provider<NotificationRowBinder> provider6, Provider<NotificationRemoteInputManager> provider7, Provider<LeakDetector> provider8, Provider<ForegroundServiceDismissalFeatureController> provider9) {
        return new NotificationsModule_ProvideNotificationEntryManagerFactory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9);
    }

    public static NotificationEntryManager proxyProvideNotificationEntryManager(NotificationEntryManagerLogger notificationEntryManagerLogger, NotificationGroupManager notificationGroupManager, NotificationRankingManager notificationRankingManager, NotificationEntryManager.KeyguardEnvironment keyguardEnvironment, FeatureFlags featureFlags, Lazy<NotificationRowBinder> lazy, Lazy<NotificationRemoteInputManager> lazy2, LeakDetector leakDetector, ForegroundServiceDismissalFeatureController foregroundServiceDismissalFeatureController) {
        NotificationEntryManager provideNotificationEntryManager = NotificationsModule.provideNotificationEntryManager(notificationEntryManagerLogger, notificationGroupManager, notificationRankingManager, keyguardEnvironment, featureFlags, lazy, lazy2, leakDetector, foregroundServiceDismissalFeatureController);
        Preconditions.checkNotNull(provideNotificationEntryManager, "Cannot return null from a non-@Nullable @Provides method");
        return provideNotificationEntryManager;
    }
}
