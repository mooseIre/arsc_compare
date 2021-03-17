package com.android.systemui.bubbles.dagger;

import android.app.INotificationManager;
import android.content.Context;
import android.content.pm.LauncherApps;
import android.view.WindowManager;
import com.android.internal.statusbar.IStatusBarService;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.bubbles.BubbleData;
import com.android.systemui.bubbles.BubbleDataRepository;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.model.SysUiState;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.FeatureFlags;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProvider;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.phone.NotificationShadeWindowController;
import com.android.systemui.statusbar.phone.ShadeController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.util.FloatingContentCoordinator;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class BubbleModule_NewBubbleControllerFactory implements Factory<BubbleController> {
    private final Provider<BubbleDataRepository> bubbleDataRepositoryProvider;
    private final Provider<ConfigurationController> configurationControllerProvider;
    private final Provider<Context> contextProvider;
    private final Provider<BubbleData> dataProvider;
    private final Provider<DumpManager> dumpManagerProvider;
    private final Provider<NotificationEntryManager> entryManagerProvider;
    private final Provider<FeatureFlags> featureFlagsProvider;
    private final Provider<FloatingContentCoordinator> floatingContentCoordinatorProvider;
    private final Provider<NotificationGroupManager> groupManagerProvider;
    private final Provider<NotificationInterruptStateProvider> interruptionStateProvider;
    private final Provider<LauncherApps> launcherAppsProvider;
    private final Provider<INotificationManager> notifManagerProvider;
    private final Provider<NotifPipeline> notifPipelineProvider;
    private final Provider<NotificationLockscreenUserManager> notifUserManagerProvider;
    private final Provider<NotificationShadeWindowController> notificationShadeWindowControllerProvider;
    private final Provider<ShadeController> shadeControllerProvider;
    private final Provider<IStatusBarService> statusBarServiceProvider;
    private final Provider<StatusBarStateController> statusBarStateControllerProvider;
    private final Provider<SysUiState> sysUiStateProvider;
    private final Provider<WindowManager> windowManagerProvider;
    private final Provider<ZenModeController> zenModeControllerProvider;

    public BubbleModule_NewBubbleControllerFactory(Provider<Context> provider, Provider<NotificationShadeWindowController> provider2, Provider<StatusBarStateController> provider3, Provider<ShadeController> provider4, Provider<BubbleData> provider5, Provider<ConfigurationController> provider6, Provider<NotificationInterruptStateProvider> provider7, Provider<ZenModeController> provider8, Provider<NotificationLockscreenUserManager> provider9, Provider<NotificationGroupManager> provider10, Provider<NotificationEntryManager> provider11, Provider<NotifPipeline> provider12, Provider<FeatureFlags> provider13, Provider<DumpManager> provider14, Provider<FloatingContentCoordinator> provider15, Provider<BubbleDataRepository> provider16, Provider<SysUiState> provider17, Provider<INotificationManager> provider18, Provider<IStatusBarService> provider19, Provider<WindowManager> provider20, Provider<LauncherApps> provider21) {
        this.contextProvider = provider;
        this.notificationShadeWindowControllerProvider = provider2;
        this.statusBarStateControllerProvider = provider3;
        this.shadeControllerProvider = provider4;
        this.dataProvider = provider5;
        this.configurationControllerProvider = provider6;
        this.interruptionStateProvider = provider7;
        this.zenModeControllerProvider = provider8;
        this.notifUserManagerProvider = provider9;
        this.groupManagerProvider = provider10;
        this.entryManagerProvider = provider11;
        this.notifPipelineProvider = provider12;
        this.featureFlagsProvider = provider13;
        this.dumpManagerProvider = provider14;
        this.floatingContentCoordinatorProvider = provider15;
        this.bubbleDataRepositoryProvider = provider16;
        this.sysUiStateProvider = provider17;
        this.notifManagerProvider = provider18;
        this.statusBarServiceProvider = provider19;
        this.windowManagerProvider = provider20;
        this.launcherAppsProvider = provider21;
    }

    @Override // javax.inject.Provider
    public BubbleController get() {
        return provideInstance(this.contextProvider, this.notificationShadeWindowControllerProvider, this.statusBarStateControllerProvider, this.shadeControllerProvider, this.dataProvider, this.configurationControllerProvider, this.interruptionStateProvider, this.zenModeControllerProvider, this.notifUserManagerProvider, this.groupManagerProvider, this.entryManagerProvider, this.notifPipelineProvider, this.featureFlagsProvider, this.dumpManagerProvider, this.floatingContentCoordinatorProvider, this.bubbleDataRepositoryProvider, this.sysUiStateProvider, this.notifManagerProvider, this.statusBarServiceProvider, this.windowManagerProvider, this.launcherAppsProvider);
    }

    public static BubbleController provideInstance(Provider<Context> provider, Provider<NotificationShadeWindowController> provider2, Provider<StatusBarStateController> provider3, Provider<ShadeController> provider4, Provider<BubbleData> provider5, Provider<ConfigurationController> provider6, Provider<NotificationInterruptStateProvider> provider7, Provider<ZenModeController> provider8, Provider<NotificationLockscreenUserManager> provider9, Provider<NotificationGroupManager> provider10, Provider<NotificationEntryManager> provider11, Provider<NotifPipeline> provider12, Provider<FeatureFlags> provider13, Provider<DumpManager> provider14, Provider<FloatingContentCoordinator> provider15, Provider<BubbleDataRepository> provider16, Provider<SysUiState> provider17, Provider<INotificationManager> provider18, Provider<IStatusBarService> provider19, Provider<WindowManager> provider20, Provider<LauncherApps> provider21) {
        return proxyNewBubbleController(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), provider9.get(), provider10.get(), provider11.get(), provider12.get(), provider13.get(), provider14.get(), provider15.get(), provider16.get(), provider17.get(), provider18.get(), provider19.get(), provider20.get(), provider21.get());
    }

    public static BubbleModule_NewBubbleControllerFactory create(Provider<Context> provider, Provider<NotificationShadeWindowController> provider2, Provider<StatusBarStateController> provider3, Provider<ShadeController> provider4, Provider<BubbleData> provider5, Provider<ConfigurationController> provider6, Provider<NotificationInterruptStateProvider> provider7, Provider<ZenModeController> provider8, Provider<NotificationLockscreenUserManager> provider9, Provider<NotificationGroupManager> provider10, Provider<NotificationEntryManager> provider11, Provider<NotifPipeline> provider12, Provider<FeatureFlags> provider13, Provider<DumpManager> provider14, Provider<FloatingContentCoordinator> provider15, Provider<BubbleDataRepository> provider16, Provider<SysUiState> provider17, Provider<INotificationManager> provider18, Provider<IStatusBarService> provider19, Provider<WindowManager> provider20, Provider<LauncherApps> provider21) {
        return new BubbleModule_NewBubbleControllerFactory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10, provider11, provider12, provider13, provider14, provider15, provider16, provider17, provider18, provider19, provider20, provider21);
    }

    public static BubbleController proxyNewBubbleController(Context context, NotificationShadeWindowController notificationShadeWindowController, StatusBarStateController statusBarStateController, ShadeController shadeController, BubbleData bubbleData, ConfigurationController configurationController, NotificationInterruptStateProvider notificationInterruptStateProvider, ZenModeController zenModeController, NotificationLockscreenUserManager notificationLockscreenUserManager, NotificationGroupManager notificationGroupManager, NotificationEntryManager notificationEntryManager, NotifPipeline notifPipeline, FeatureFlags featureFlags, DumpManager dumpManager, FloatingContentCoordinator floatingContentCoordinator, BubbleDataRepository bubbleDataRepository, SysUiState sysUiState, INotificationManager iNotificationManager, IStatusBarService iStatusBarService, WindowManager windowManager, LauncherApps launcherApps) {
        BubbleController newBubbleController = BubbleModule.newBubbleController(context, notificationShadeWindowController, statusBarStateController, shadeController, bubbleData, configurationController, notificationInterruptStateProvider, zenModeController, notificationLockscreenUserManager, notificationGroupManager, notificationEntryManager, notifPipeline, featureFlags, dumpManager, floatingContentCoordinator, bubbleDataRepository, sysUiState, iNotificationManager, iStatusBarService, windowManager, launcherApps);
        Preconditions.checkNotNull(newBubbleController, "Cannot return null from a non-@Nullable @Provides method");
        return newBubbleController;
    }
}
