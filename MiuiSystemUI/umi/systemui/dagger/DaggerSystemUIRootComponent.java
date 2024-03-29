package com.android.systemui.dagger;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.IActivityManager;
import android.app.INotificationManager;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.app.trust.TrustManager;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.pm.IPackageManager;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutManager;
import android.content.res.Resources;
import android.hardware.SensorPrivacyManager;
import android.hardware.display.NightDisplayListener;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkScoreManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.UserManager;
import android.os.Vibrator;
import android.service.dreams.IDreamManager;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Choreographer;
import android.view.IWindowManager;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import androidx.slice.Clock;
import com.android.internal.app.AssistUtils;
import com.android.internal.app.IBatteryStats;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.util.LatencyTracker;
import com.android.keyguard.KeyguardClockSwitch;
import com.android.keyguard.KeyguardMessageArea;
import com.android.keyguard.KeyguardSecurityModel;
import com.android.keyguard.KeyguardSecurityModel_Factory;
import com.android.keyguard.KeyguardSliceView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitor_Factory;
import com.android.keyguard.MiuiCarrierTextController_Factory;
import com.android.keyguard.MiuiDozeServiceHost;
import com.android.keyguard.MiuiDozeServiceHost_Factory;
import com.android.keyguard.MiuiFastUnlockController;
import com.android.keyguard.MiuiFastUnlockController_Factory;
import com.android.keyguard.PhoneSignalControllerImpl;
import com.android.keyguard.PhoneSignalControllerImpl_Factory;
import com.android.keyguard.charge.MiuiChargeController;
import com.android.keyguard.charge.MiuiChargeController_Factory;
import com.android.keyguard.charge.MiuiChargeManager;
import com.android.keyguard.charge.MiuiChargeManager_Factory;
import com.android.keyguard.clock.ClockManager;
import com.android.keyguard.clock.ClockManager_Factory;
import com.android.keyguard.faceunlock.MiuiFaceUnlockManager;
import com.android.keyguard.faceunlock.MiuiFaceUnlockManager_Factory;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.keyguard.fod.MiuiGxzwManager_Factory;
import com.android.keyguard.fod.policy.MiuiGxzwPolicy;
import com.android.keyguard.fod.policy.MiuiGxzwPolicy_Factory;
import com.android.keyguard.injector.KeyguardBottomAreaInjector;
import com.android.keyguard.injector.KeyguardBottomAreaInjector_Factory;
import com.android.keyguard.injector.KeyguardClockInjector;
import com.android.keyguard.injector.KeyguardClockInjector_Factory;
import com.android.keyguard.injector.KeyguardIndicationInjector;
import com.android.keyguard.injector.KeyguardIndicationInjector_Factory;
import com.android.keyguard.injector.KeyguardNegative1PageInjector;
import com.android.keyguard.injector.KeyguardNegative1PageInjector_Factory;
import com.android.keyguard.injector.KeyguardPanelViewInjector;
import com.android.keyguard.injector.KeyguardPanelViewInjector_Factory;
import com.android.keyguard.injector.KeyguardSensorInjector;
import com.android.keyguard.injector.KeyguardSensorInjector_Factory;
import com.android.keyguard.injector.KeyguardUpdateMonitorInjector;
import com.android.keyguard.injector.KeyguardUpdateMonitorInjector_Factory;
import com.android.keyguard.injector.KeyguardViewMediatorInjector;
import com.android.keyguard.injector.KeyguardViewMediatorInjector_Factory;
import com.android.keyguard.magazine.LockScreenMagazineController;
import com.android.keyguard.magazine.LockScreenMagazineController_Factory;
import com.android.keyguard.negative.MiuiQuickConnectController;
import com.android.keyguard.negative.MiuiQuickConnectController_Factory;
import com.android.keyguard.wallpaper.MiuiKeyguardWallpaperControllerImpl;
import com.android.keyguard.wallpaper.MiuiKeyguardWallpaperControllerImpl_Factory;
import com.android.keyguard.wallpaper.MiuiWallpaperClient;
import com.android.keyguard.wallpaper.MiuiWallpaperClient_Factory;
import com.android.keyguard.wallpaper.WallpaperCommandSender;
import com.android.keyguard.wallpaper.WallpaperCommandSender_Factory;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.systemui.ActivityIntentHelper;
import com.android.systemui.ActivityIntentHelper_Factory;
import com.android.systemui.ActivityStarterDelegate;
import com.android.systemui.ActivityStarterDelegate_Factory;
import com.android.systemui.BootCompleteCacheImpl;
import com.android.systemui.BootCompleteCacheImpl_Factory;
import com.android.systemui.CodeBlueService;
import com.android.systemui.CodeBlueService_Factory;
import com.android.systemui.Dependency;
import com.android.systemui.Dependency_MembersInjector;
import com.android.systemui.ForegroundServiceController;
import com.android.systemui.ForegroundServiceController_Factory;
import com.android.systemui.ForegroundServiceLifetimeExtender_Factory;
import com.android.systemui.ForegroundServiceNotificationListener;
import com.android.systemui.ForegroundServiceNotificationListener_Factory;
import com.android.systemui.ForegroundServicesDialog;
import com.android.systemui.ForegroundServicesDialog_Factory;
import com.android.systemui.ImageWallpaper;
import com.android.systemui.ImageWallpaper_Factory;
import com.android.systemui.InitController;
import com.android.systemui.InitController_Factory;
import com.android.systemui.LatencyTester;
import com.android.systemui.LatencyTester_Factory;
import com.android.systemui.MiuiVendorServices;
import com.android.systemui.MiuiVendorServices_Factory;
import com.android.systemui.MiuiWallpaperZoomOutService;
import com.android.systemui.MiuiWallpaperZoomOutService_Factory;
import com.android.systemui.ScreenDecorations;
import com.android.systemui.ScreenDecorations_Factory;
import com.android.systemui.SizeCompatModeActivityController;
import com.android.systemui.SizeCompatModeActivityController_Factory;
import com.android.systemui.SliceBroadcastRelayHandler;
import com.android.systemui.SliceBroadcastRelayHandler_Factory;
import com.android.systemui.SystemUI;
import com.android.systemui.SystemUIAppComponentFactory;
import com.android.systemui.SystemUIAppComponentFactory_MembersInjector;
import com.android.systemui.SystemUIFactory;
import com.android.systemui.SystemUIFactory_ContextHolder_ProvideContextFactory;
import com.android.systemui.SystemUIService;
import com.android.systemui.SystemUIService_Factory;
import com.android.systemui.ToggleManagerController;
import com.android.systemui.ToggleManagerController_Factory;
import com.android.systemui.TransactionPool;
import com.android.systemui.TransactionPool_Factory;
import com.android.systemui.UiOffloadThread;
import com.android.systemui.UiOffloadThread_Factory;
import com.android.systemui.accessibility.SystemActions;
import com.android.systemui.accessibility.SystemActions_Factory;
import com.android.systemui.accessibility.WindowMagnification;
import com.android.systemui.accessibility.WindowMagnification_Factory;
import com.android.systemui.appops.AppOpsControllerImpl;
import com.android.systemui.appops.AppOpsControllerImpl_Factory;
import com.android.systemui.assist.AssistHandleBehaviorController;
import com.android.systemui.assist.AssistHandleBehaviorController_Factory;
import com.android.systemui.assist.AssistHandleLikeHomeBehavior_Factory;
import com.android.systemui.assist.AssistHandleOffBehavior_Factory;
import com.android.systemui.assist.AssistHandleReminderExpBehavior_Factory;
import com.android.systemui.assist.AssistLogger;
import com.android.systemui.assist.AssistLogger_Factory;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.assist.AssistManager_Factory;
import com.android.systemui.assist.AssistModule_ProvideAssistHandleBehaviorControllerMapFactory;
import com.android.systemui.assist.AssistModule_ProvideAssistHandleViewControllerFactory;
import com.android.systemui.assist.AssistModule_ProvideAssistUtilsFactory;
import com.android.systemui.assist.AssistModule_ProvideBackgroundHandlerFactory;
import com.android.systemui.assist.AssistModule_ProvideSystemClockFactory;
import com.android.systemui.assist.DeviceConfigHelper;
import com.android.systemui.assist.DeviceConfigHelper_Factory;
import com.android.systemui.assist.PhoneStateMonitor;
import com.android.systemui.assist.PhoneStateMonitor_Factory;
import com.android.systemui.assist.ui.DefaultUiController;
import com.android.systemui.assist.ui.DefaultUiController_Factory;
import com.android.systemui.biometrics.AuthController;
import com.android.systemui.biometrics.AuthController_Factory;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.broadcast.logging.BroadcastDispatcherLogger_Factory;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.bubbles.BubbleData;
import com.android.systemui.bubbles.BubbleDataRepository;
import com.android.systemui.bubbles.BubbleDataRepository_Factory;
import com.android.systemui.bubbles.BubbleData_Factory;
import com.android.systemui.bubbles.BubbleOverflowActivity;
import com.android.systemui.bubbles.BubbleOverflowActivity_Factory;
import com.android.systemui.bubbles.dagger.BubbleModule_NewBubbleControllerFactory;
import com.android.systemui.bubbles.storage.BubblePersistentRepository;
import com.android.systemui.bubbles.storage.BubblePersistentRepository_Factory;
import com.android.systemui.bubbles.storage.BubbleVolatileRepository;
import com.android.systemui.bubbles.storage.BubbleVolatileRepository_Factory;
import com.android.systemui.classifier.FalsingManagerProxy;
import com.android.systemui.classifier.FalsingManagerProxy_Factory;
import com.android.systemui.colorextraction.SysuiColorExtractor;
import com.android.systemui.colorextraction.SysuiColorExtractor_Factory;
import com.android.systemui.controlcenter.ControlCenter;
import com.android.systemui.controlcenter.dagger.ControlCenterModule_ProvideControlCenterFactory;
import com.android.systemui.controlcenter.phone.ControlCenterPanelView;
import com.android.systemui.controlcenter.phone.ControlPanelController;
import com.android.systemui.controlcenter.phone.ControlPanelController_Factory;
import com.android.systemui.controlcenter.phone.ControlPanelWindowManager;
import com.android.systemui.controlcenter.phone.ControlPanelWindowManager_Factory;
import com.android.systemui.controlcenter.phone.ExpandInfoControllerImpl;
import com.android.systemui.controlcenter.phone.ExpandInfoControllerImpl_Factory;
import com.android.systemui.controlcenter.phone.controls.ControlsPluginManager;
import com.android.systemui.controlcenter.phone.controls.ControlsPluginManager_Factory;
import com.android.systemui.controlcenter.phone.controls.MiPlayPluginManager;
import com.android.systemui.controlcenter.phone.controls.MiPlayPluginManager_Factory;
import com.android.systemui.controlcenter.phone.customize.CCTileQueryHelper;
import com.android.systemui.controlcenter.phone.customize.QSControlCustomizer;
import com.android.systemui.controlcenter.phone.widget.ControlCenterBrightnessView;
import com.android.systemui.controlcenter.policy.ControlCenterActivityStarter;
import com.android.systemui.controlcenter.policy.ControlCenterActivityStarter_Factory;
import com.android.systemui.controlcenter.policy.NCSwitchController;
import com.android.systemui.controlcenter.policy.NCSwitchController_Factory;
import com.android.systemui.controlcenter.policy.OldModeController;
import com.android.systemui.controlcenter.policy.OldModeController_Factory;
import com.android.systemui.controlcenter.policy.SlaveWifiHelper;
import com.android.systemui.controlcenter.policy.SlaveWifiHelper_Factory;
import com.android.systemui.controlcenter.policy.SuperSaveModeController;
import com.android.systemui.controlcenter.policy.SuperSaveModeController_Factory;
import com.android.systemui.controlcenter.qs.MiuiQSTileHostInjector;
import com.android.systemui.controlcenter.qs.MiuiQSTileHostInjector_Factory;
import com.android.systemui.controls.controller.ControlsBindingControllerImpl;
import com.android.systemui.controls.controller.ControlsBindingControllerImpl_Factory;
import com.android.systemui.controls.controller.ControlsControllerImpl;
import com.android.systemui.controls.controller.ControlsControllerImpl_Factory;
import com.android.systemui.controls.controller.ControlsFavoritePersistenceWrapper;
import com.android.systemui.controls.dagger.ControlsComponent;
import com.android.systemui.controls.dagger.ControlsComponent_Factory;
import com.android.systemui.controls.dagger.ControlsModule_ProvidesControlsFeatureEnabledFactory;
import com.android.systemui.controls.management.ControlsEditingActivity;
import com.android.systemui.controls.management.ControlsEditingActivity_Factory;
import com.android.systemui.controls.management.ControlsFavoritingActivity;
import com.android.systemui.controls.management.ControlsFavoritingActivity_Factory;
import com.android.systemui.controls.management.ControlsListingControllerImpl;
import com.android.systemui.controls.management.ControlsListingControllerImpl_Factory;
import com.android.systemui.controls.management.ControlsProviderSelectorActivity;
import com.android.systemui.controls.management.ControlsProviderSelectorActivity_Factory;
import com.android.systemui.controls.management.ControlsRequestDialog;
import com.android.systemui.controls.management.ControlsRequestDialog_Factory;
import com.android.systemui.controls.ui.ControlActionCoordinatorImpl;
import com.android.systemui.controls.ui.ControlActionCoordinatorImpl_Factory;
import com.android.systemui.controls.ui.ControlsUiControllerImpl;
import com.android.systemui.controls.ui.ControlsUiControllerImpl_Factory;
import com.android.systemui.dock.DockManager;
import com.android.systemui.dock.DockManagerImpl;
import com.android.systemui.dock.DockManagerImpl_Factory;
import com.android.systemui.doze.DozeFactory_Factory;
import com.android.systemui.doze.DozeLog;
import com.android.systemui.doze.DozeLog_Factory;
import com.android.systemui.doze.DozeLogger_Factory;
import com.android.systemui.doze.DozeService;
import com.android.systemui.doze.DozeService_Factory;
import com.android.systemui.dump.DumpHandler_Factory;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.dump.DumpManager_Factory;
import com.android.systemui.dump.LogBufferEulogizer;
import com.android.systemui.dump.LogBufferEulogizer_Factory;
import com.android.systemui.dump.LogBufferFreezer_Factory;
import com.android.systemui.dump.SystemUIAuxiliaryDumpService;
import com.android.systemui.dump.SystemUIAuxiliaryDumpService_Factory;
import com.android.systemui.fragments.FragmentService;
import com.android.systemui.fragments.FragmentService_Factory;
import com.android.systemui.globalactions.GlobalActionsComponent;
import com.android.systemui.globalactions.GlobalActionsComponent_Factory;
import com.android.systemui.globalactions.GlobalActionsDialog_Factory;
import com.android.systemui.globalactions.GlobalActionsImpl_Factory;
import com.android.systemui.keyguard.DismissCallbackRegistry;
import com.android.systemui.keyguard.DismissCallbackRegistry_Factory;
import com.android.systemui.keyguard.KeyguardLifecyclesDispatcher;
import com.android.systemui.keyguard.KeyguardLifecyclesDispatcher_Factory;
import com.android.systemui.keyguard.KeyguardService;
import com.android.systemui.keyguard.KeyguardService_Factory;
import com.android.systemui.keyguard.KeyguardSliceProvider;
import com.android.systemui.keyguard.KeyguardSliceProvider_MembersInjector;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.keyguard.ScreenLifecycle;
import com.android.systemui.keyguard.ScreenLifecycle_Factory;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.keyguard.WakefulnessLifecycle_Factory;
import com.android.systemui.keyguard.WorkLockActivity;
import com.android.systemui.keyguard.WorkLockActivity_Factory;
import com.android.systemui.keyguard.dagger.KeyguardModule_NewKeyguardViewMediatorFactory;
import com.android.systemui.log.LogBuffer;
import com.android.systemui.log.LogcatEchoTracker;
import com.android.systemui.log.dagger.LogModule_ProvideBroadcastDispatcherLogBufferFactory;
import com.android.systemui.log.dagger.LogModule_ProvideDozeLogBufferFactory;
import com.android.systemui.log.dagger.LogModule_ProvideLogcatEchoTrackerFactory;
import com.android.systemui.log.dagger.LogModule_ProvideNotifInteractionLogBufferFactory;
import com.android.systemui.log.dagger.LogModule_ProvideNotificationSectionLogBufferFactory;
import com.android.systemui.log.dagger.LogModule_ProvideNotificationsLogBufferFactory;
import com.android.systemui.log.dagger.LogModule_ProvideQuickSettingsLogBufferFactory;
import com.android.systemui.media.LocalMediaManagerFactory_Factory;
import com.android.systemui.media.MediaCarouselController;
import com.android.systemui.media.MediaCarouselController_Factory;
import com.android.systemui.media.MediaControllerFactory_Factory;
import com.android.systemui.media.MediaDataCombineLatest;
import com.android.systemui.media.MediaDataCombineLatest_Factory;
import com.android.systemui.media.MediaDataFilter;
import com.android.systemui.media.MediaDataFilter_Factory;
import com.android.systemui.media.MediaDataManager;
import com.android.systemui.media.MediaDataManager_Factory;
import com.android.systemui.media.MediaDeviceManager;
import com.android.systemui.media.MediaDeviceManager_Factory;
import com.android.systemui.media.MediaFeatureFlag_Factory;
import com.android.systemui.media.MediaHierarchyManager;
import com.android.systemui.media.MediaHierarchyManager_Factory;
import com.android.systemui.media.MediaHost;
import com.android.systemui.media.MediaHostStatesManager;
import com.android.systemui.media.MediaHostStatesManager_Factory;
import com.android.systemui.media.MediaHost_Factory;
import com.android.systemui.media.MediaHost_MediaHostStateHolder_Factory;
import com.android.systemui.media.MediaResumeListener;
import com.android.systemui.media.MediaResumeListener_Factory;
import com.android.systemui.media.MediaTimeoutListener;
import com.android.systemui.media.MediaTimeoutListener_Factory;
import com.android.systemui.media.MediaViewController_Factory;
import com.android.systemui.media.SeekBarViewModel_Factory;
import com.android.systemui.model.SysUiState;
import com.android.systemui.pip.PipAnimationController_Factory;
import com.android.systemui.pip.PipBoundsHandler;
import com.android.systemui.pip.PipBoundsHandler_Factory;
import com.android.systemui.pip.PipSnapAlgorithm_Factory;
import com.android.systemui.pip.PipSurfaceTransactionHelper;
import com.android.systemui.pip.PipSurfaceTransactionHelper_Factory;
import com.android.systemui.pip.PipTaskOrganizer;
import com.android.systemui.pip.PipTaskOrganizer_Factory;
import com.android.systemui.pip.PipUI;
import com.android.systemui.pip.PipUI_Factory;
import com.android.systemui.pip.PipUiEventLogger;
import com.android.systemui.pip.PipUiEventLogger_Factory;
import com.android.systemui.pip.phone.PipManager;
import com.android.systemui.pip.phone.PipManager_Factory;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.plugins.PluginDependencyProvider;
import com.android.systemui.plugins.PluginDependencyProvider_Factory;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.power.EnhancedEstimatesImpl;
import com.android.systemui.power.EnhancedEstimatesImpl_Factory;
import com.android.systemui.power.PowerNotificationWarnings;
import com.android.systemui.power.PowerNotificationWarnings_Factory;
import com.android.systemui.power.PowerUI;
import com.android.systemui.power.PowerUI_Factory;
import com.android.systemui.qs.AutoAddTracker_Builder_Factory;
import com.android.systemui.qs.MiuiQSContainer;
import com.android.systemui.qs.MiuiQSFragment;
import com.android.systemui.qs.QSFooterDataUsage;
import com.android.systemui.qs.QSFooterImpl;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.qs.QSTileHost_Factory;
import com.android.systemui.qs.QuickQSPanel;
import com.android.systemui.qs.QuickStatusBarHeader;
import com.android.systemui.qs.customize.MiuiQSCustomizer;
import com.android.systemui.qs.customize.TileQueryHelper;
import com.android.systemui.qs.logging.QSLogger;
import com.android.systemui.qs.logging.QSLogger_Factory;
import com.android.systemui.qs.tileimpl.QSFactoryImpl;
import com.android.systemui.qs.tileimpl.QSFactoryImpl_Factory;
import com.android.systemui.qs.tileimpl.QSFactoryInjectorImpl;
import com.android.systemui.qs.tileimpl.QSFactoryInjectorImpl_Factory;
import com.android.systemui.qs.tiles.AirplaneModeTile_Factory;
import com.android.systemui.qs.tiles.AutoBrightnessTile_Factory;
import com.android.systemui.qs.tiles.BatterySaverTile_Factory;
import com.android.systemui.qs.tiles.BluetoothTile_Factory;
import com.android.systemui.qs.tiles.CastTile_Factory;
import com.android.systemui.qs.tiles.CellularTile_Factory;
import com.android.systemui.qs.tiles.ColorInversionTile_Factory;
import com.android.systemui.qs.tiles.DataSaverTile_Factory;
import com.android.systemui.qs.tiles.DndTile_Factory;
import com.android.systemui.qs.tiles.DriveModeTile_Factory;
import com.android.systemui.qs.tiles.EditTile_Factory;
import com.android.systemui.qs.tiles.FlashlightTile_Factory;
import com.android.systemui.qs.tiles.HotspotTile_Factory;
import com.android.systemui.qs.tiles.LocationTile_Factory;
import com.android.systemui.qs.tiles.MiuiAirplaneModeTile_Factory;
import com.android.systemui.qs.tiles.MiuiCellularTile_Factory;
import com.android.systemui.qs.tiles.MiuiHotspotTile_Factory;
import com.android.systemui.qs.tiles.MuteTile_Factory;
import com.android.systemui.qs.tiles.NfcTile_Factory;
import com.android.systemui.qs.tiles.NightDisplayTile_Factory;
import com.android.systemui.qs.tiles.NightModeTile_Factory;
import com.android.systemui.qs.tiles.PaperModeTile_Factory;
import com.android.systemui.qs.tiles.PowerModeTile_Factory;
import com.android.systemui.qs.tiles.PowerSaverExtremeTile_Factory;
import com.android.systemui.qs.tiles.PowerSaverTile_Factory;
import com.android.systemui.qs.tiles.QuietModeTile_Factory;
import com.android.systemui.qs.tiles.RotationLockTile_Factory;
import com.android.systemui.qs.tiles.ScreenButtonTile_Factory;
import com.android.systemui.qs.tiles.ScreenLockTile_Factory;
import com.android.systemui.qs.tiles.ScreenRecordTile_Factory;
import com.android.systemui.qs.tiles.ScreenShotTile_Factory;
import com.android.systemui.qs.tiles.SyncTile_Factory;
import com.android.systemui.qs.tiles.UiModeNightTile_Factory;
import com.android.systemui.qs.tiles.UserTile_Factory;
import com.android.systemui.qs.tiles.VibrateTile_Factory;
import com.android.systemui.qs.tiles.WifiTile_Factory;
import com.android.systemui.qs.tiles.WorkModeTile_Factory;
import com.android.systemui.recents.MiuiFullScreenGestureProxy;
import com.android.systemui.recents.MiuiFullScreenGestureProxy_Factory;
import com.android.systemui.recents.MiuiRecentProxy;
import com.android.systemui.recents.MiuiRecentProxy_Factory;
import com.android.systemui.recents.OverviewProxyRecentsImpl;
import com.android.systemui.recents.OverviewProxyRecentsImpl_Factory;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.recents.OverviewProxyService_Factory;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsImplementation;
import com.android.systemui.recents.RecentsModule_ProvideRecentsImplFactory;
import com.android.systemui.recents.ScreenPinningRequest_Factory;
import com.android.systemui.screenrecord.RecordingController;
import com.android.systemui.screenrecord.RecordingController_Factory;
import com.android.systemui.screenrecord.RecordingService;
import com.android.systemui.screenrecord.RecordingService_Factory;
import com.android.systemui.screenrecord.ScreenRecordDialog;
import com.android.systemui.screenrecord.ScreenRecordDialog_Factory;
import com.android.systemui.screenshot.FallbackTakeScreenshotService;
import com.android.systemui.screenshot.FallbackTakeScreenshotService_Factory;
import com.android.systemui.screenshot.GlobalScreenshot;
import com.android.systemui.screenshot.GlobalScreenshot_ActionProxyReceiver_Factory;
import com.android.systemui.screenshot.GlobalScreenshot_Factory;
import com.android.systemui.screenshot.ScreenshotNotificationsController_Factory;
import com.android.systemui.screenshot.TakeScreenshotService;
import com.android.systemui.screenshot.TakeScreenshotService_Factory;
import com.android.systemui.settings.BrightnessDialog;
import com.android.systemui.settings.BrightnessDialog_Factory;
import com.android.systemui.settings.CurrentUserContextTracker;
import com.android.systemui.settings.dagger.SettingsModule_ProvideCurrentUserContextTrackerFactory;
import com.android.systemui.shared.plugins.PluginManager;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.DevicePolicyManagerWrapper;
import com.android.systemui.shared.system.PackageManagerWrapper;
import com.android.systemui.shortcut.ShortcutKeyDispatcher;
import com.android.systemui.shortcut.ShortcutKeyDispatcher_Factory;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.stackdivider.DividerModule_ProvideDividerFactory;
import com.android.systemui.statusbar.ActionClickLogger_Factory;
import com.android.systemui.statusbar.BlurUtils;
import com.android.systemui.statusbar.BlurUtils_Factory;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.FeatureFlags;
import com.android.systemui.statusbar.FeatureFlags_Factory;
import com.android.systemui.statusbar.FlingAnimationUtils_Builder_Factory;
import com.android.systemui.statusbar.KeyguardIndicationController;
import com.android.systemui.statusbar.KeyguardIndicationController_Factory;
import com.android.systemui.statusbar.MediaArtworkProcessor;
import com.android.systemui.statusbar.MediaArtworkProcessor_Factory;
import com.android.systemui.statusbar.MiuiStatusBarModule;
import com.android.systemui.statusbar.MiuiStatusBarModule_ProvideLightBarControllerFactory;
import com.android.systemui.statusbar.NavigationBarController;
import com.android.systemui.statusbar.NetworkSpeedController;
import com.android.systemui.statusbar.NetworkSpeedController_Factory;
import com.android.systemui.statusbar.NotificationClickNotifier;
import com.android.systemui.statusbar.NotificationClickNotifier_Factory;
import com.android.systemui.statusbar.NotificationInteractionTracker;
import com.android.systemui.statusbar.NotificationInteractionTracker_Factory;
import com.android.systemui.statusbar.NotificationListener;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.NotificationLockscreenUserManagerImpl;
import com.android.systemui.statusbar.NotificationLockscreenUserManagerImpl_Factory;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.NotificationShadeDepthController;
import com.android.systemui.statusbar.NotificationShadeDepthController_Factory;
import com.android.systemui.statusbar.NotificationShelf;
import com.android.systemui.statusbar.NotificationViewHierarchyManager;
import com.android.systemui.statusbar.PulseExpansionHandler;
import com.android.systemui.statusbar.PulseExpansionHandler_Factory;
import com.android.systemui.statusbar.SmartReplyController;
import com.android.systemui.statusbar.StatusBarStateControllerImpl;
import com.android.systemui.statusbar.StatusBarStateControllerImpl_Factory;
import com.android.systemui.statusbar.SuperStatusBarViewFactory;
import com.android.systemui.statusbar.SuperStatusBarViewFactory_Factory;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.VibratorHelper;
import com.android.systemui.statusbar.VibratorHelper_Factory;
import com.android.systemui.statusbar.dagger.StatusBarDependenciesModule_ProvideCommandQueueFactory;
import com.android.systemui.statusbar.dagger.StatusBarDependenciesModule_ProvideNotificationListenerFactory;
import com.android.systemui.statusbar.dagger.StatusBarDependenciesModule_ProvideNotificationMediaManagerFactory;
import com.android.systemui.statusbar.dagger.StatusBarDependenciesModule_ProvideNotificationRemoteInputManagerFactory;
import com.android.systemui.statusbar.dagger.StatusBarDependenciesModule_ProvideNotificationViewHierarchyManagerFactory;
import com.android.systemui.statusbar.dagger.StatusBarDependenciesModule_ProvideSmartReplyControllerFactory;
import com.android.systemui.statusbar.notification.ConversationNotificationManager;
import com.android.systemui.statusbar.notification.ConversationNotificationManager_Factory;
import com.android.systemui.statusbar.notification.ConversationNotificationProcessor_Factory;
import com.android.systemui.statusbar.notification.DynamicChildBindController_Factory;
import com.android.systemui.statusbar.notification.DynamicPrivacyController;
import com.android.systemui.statusbar.notification.DynamicPrivacyController_Factory;
import com.android.systemui.statusbar.notification.ForegroundServiceDismissalFeatureController;
import com.android.systemui.statusbar.notification.ForegroundServiceDismissalFeatureController_Factory;
import com.android.systemui.statusbar.notification.InstantAppNotifier;
import com.android.systemui.statusbar.notification.InstantAppNotifier_Factory;
import com.android.systemui.statusbar.notification.MiuiNotificationSectionsFeatureManager;
import com.android.systemui.statusbar.notification.MiuiNotificationSectionsFeatureManager_Factory;
import com.android.systemui.statusbar.notification.MiuiNotificationSectionsManager;
import com.android.systemui.statusbar.notification.NotificationClickerLogger_Factory;
import com.android.systemui.statusbar.notification.NotificationClicker_Builder_Factory;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationEntryManagerLogger_Factory;
import com.android.systemui.statusbar.notification.NotificationFilter;
import com.android.systemui.statusbar.notification.NotificationFilter_Factory;
import com.android.systemui.statusbar.notification.NotificationPanelNavigationBarCoordinator;
import com.android.systemui.statusbar.notification.NotificationPanelNavigationBarCoordinator_Factory;
import com.android.systemui.statusbar.notification.NotificationSettingsManager;
import com.android.systemui.statusbar.notification.NotificationSettingsManager_Factory;
import com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator;
import com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator_Factory;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.notification.analytics.NotificationStat;
import com.android.systemui.statusbar.notification.analytics.NotificationStat_Factory;
import com.android.systemui.statusbar.notification.collection.NotifCollection;
import com.android.systemui.statusbar.notification.collection.NotifCollection_Factory;
import com.android.systemui.statusbar.notification.collection.NotifInflaterImpl;
import com.android.systemui.statusbar.notification.collection.NotifInflaterImpl_Factory;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.collection.NotifPipeline_Factory;
import com.android.systemui.statusbar.notification.collection.NotifViewBarn;
import com.android.systemui.statusbar.notification.collection.NotifViewBarn_Factory;
import com.android.systemui.statusbar.notification.collection.NotifViewManager;
import com.android.systemui.statusbar.notification.collection.NotifViewManager_Factory;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.NotificationRankingManager_Factory;
import com.android.systemui.statusbar.notification.collection.ShadeListBuilder;
import com.android.systemui.statusbar.notification.collection.ShadeListBuilder_Factory;
import com.android.systemui.statusbar.notification.collection.TargetSdkResolver;
import com.android.systemui.statusbar.notification.collection.TargetSdkResolver_Factory;
import com.android.systemui.statusbar.notification.collection.coalescer.GroupCoalescerLogger_Factory;
import com.android.systemui.statusbar.notification.collection.coalescer.GroupCoalescer_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.AppOpsCoordinator;
import com.android.systemui.statusbar.notification.collection.coordinator.AppOpsCoordinator_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.BubbleCoordinator;
import com.android.systemui.statusbar.notification.collection.coordinator.BubbleCoordinator_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.ConversationCoordinator;
import com.android.systemui.statusbar.notification.collection.coordinator.ConversationCoordinator_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.DeviceProvisionedCoordinator;
import com.android.systemui.statusbar.notification.collection.coordinator.DeviceProvisionedCoordinator_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.HeadsUpCoordinator;
import com.android.systemui.statusbar.notification.collection.coordinator.HeadsUpCoordinator_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.HideNotifsForOtherUsersCoordinator_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.KeyguardCoordinator;
import com.android.systemui.statusbar.notification.collection.coordinator.KeyguardCoordinator_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.MediaCoordinator_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.NotifCoordinators;
import com.android.systemui.statusbar.notification.collection.coordinator.NotifCoordinators_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.PreparationCoordinator;
import com.android.systemui.statusbar.notification.collection.coordinator.PreparationCoordinatorLogger_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.PreparationCoordinator_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.RankingCoordinator;
import com.android.systemui.statusbar.notification.collection.coordinator.RankingCoordinator_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.SharedCoordinatorLogger_Factory;
import com.android.systemui.statusbar.notification.collection.inflation.LowPriorityInflationHelper;
import com.android.systemui.statusbar.notification.collection.inflation.LowPriorityInflationHelper_Factory;
import com.android.systemui.statusbar.notification.collection.inflation.NotificationRowBinderImpl;
import com.android.systemui.statusbar.notification.collection.inflation.NotificationRowBinderImpl_Factory;
import com.android.systemui.statusbar.notification.collection.init.NotifPipelineInitializer;
import com.android.systemui.statusbar.notification.collection.init.NotifPipelineInitializer_Factory;
import com.android.systemui.statusbar.notification.collection.listbuilder.ShadeListBuilderLogger_Factory;
import com.android.systemui.statusbar.notification.collection.notifcollection.CommonNotifCollection;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionLogger_Factory;
import com.android.systemui.statusbar.notification.collection.provider.HighPriorityProvider;
import com.android.systemui.statusbar.notification.collection.provider.HighPriorityProvider_Factory;
import com.android.systemui.statusbar.notification.dagger.NotificationsModule_ProvideCommonNotifCollectionFactory;
import com.android.systemui.statusbar.notification.dagger.NotificationsModule_ProvideNotificationBlockingHelperManagerFactory;
import com.android.systemui.statusbar.notification.dagger.NotificationsModule_ProvideNotificationEntryManagerFactory;
import com.android.systemui.statusbar.notification.dagger.NotificationsModule_ProvideNotificationGutsManagerFactory;
import com.android.systemui.statusbar.notification.dagger.NotificationsModule_ProvideNotificationLoggerFactory;
import com.android.systemui.statusbar.notification.dagger.NotificationsModule_ProvideNotificationPanelLoggerFactory;
import com.android.systemui.statusbar.notification.dagger.NotificationsModule_ProvideNotificationsControllerFactory;
import com.android.systemui.statusbar.notification.dagger.NotificationsModule_ProvideVisualStabilityManagerFactory;
import com.android.systemui.statusbar.notification.icon.IconBuilder_Factory;
import com.android.systemui.statusbar.notification.icon.MiuiIconManager_Factory;
import com.android.systemui.statusbar.notification.init.NotificationsController;
import com.android.systemui.statusbar.notification.init.NotificationsControllerImpl;
import com.android.systemui.statusbar.notification.init.NotificationsControllerImpl_Factory;
import com.android.systemui.statusbar.notification.init.NotificationsControllerStub_Factory;
import com.android.systemui.statusbar.notification.interruption.BypassHeadsUpNotifier;
import com.android.systemui.statusbar.notification.interruption.BypassHeadsUpNotifier_Factory;
import com.android.systemui.statusbar.notification.interruption.HeadsUpController;
import com.android.systemui.statusbar.notification.interruption.HeadsUpController_Factory;
import com.android.systemui.statusbar.notification.interruption.HeadsUpViewBinder;
import com.android.systemui.statusbar.notification.interruption.HeadsUpViewBinder_Factory;
import com.android.systemui.statusbar.notification.interruption.MiuiNotificationInterruptStateProviderImpl;
import com.android.systemui.statusbar.notification.interruption.MiuiNotificationInterruptStateProviderImpl_Factory;
import com.android.systemui.statusbar.notification.logging.NotificationLogger;
import com.android.systemui.statusbar.notification.logging.NotificationLogger_ExpansionStateLogger_Factory;
import com.android.systemui.statusbar.notification.logging.NotificationPanelLogger;
import com.android.systemui.statusbar.notification.mediacontrol.MediaControlLogger;
import com.android.systemui.statusbar.notification.mediacontrol.MediaControlLogger_Factory;
import com.android.systemui.statusbar.notification.mediacontrol.MiuiKeyguardMediaController;
import com.android.systemui.statusbar.notification.mediacontrol.MiuiKeyguardMediaController_Factory;
import com.android.systemui.statusbar.notification.mediacontrol.MiuiMediaControlPanel_Factory;
import com.android.systemui.statusbar.notification.mediacontrol.MiuiMediaTransferManager;
import com.android.systemui.statusbar.notification.mediacontrol.MiuiMediaTransferManager_Factory;
import com.android.systemui.statusbar.notification.modal.ModalController;
import com.android.systemui.statusbar.notification.modal.ModalController_Factory;
import com.android.systemui.statusbar.notification.modal.ModalRowInflater;
import com.android.systemui.statusbar.notification.modal.ModalRowInflater_Factory;
import com.android.systemui.statusbar.notification.people.NotificationPersonExtractorPluginBoundary;
import com.android.systemui.statusbar.notification.people.NotificationPersonExtractorPluginBoundary_Factory;
import com.android.systemui.statusbar.notification.people.PeopleHubDataSourceImpl;
import com.android.systemui.statusbar.notification.people.PeopleHubDataSourceImpl_Factory;
import com.android.systemui.statusbar.notification.people.PeopleHubViewAdapter;
import com.android.systemui.statusbar.notification.people.PeopleHubViewAdapterImpl;
import com.android.systemui.statusbar.notification.people.PeopleHubViewAdapterImpl_Factory;
import com.android.systemui.statusbar.notification.people.PeopleHubViewModelFactoryDataSourceImpl;
import com.android.systemui.statusbar.notification.people.PeopleHubViewModelFactoryDataSourceImpl_Factory;
import com.android.systemui.statusbar.notification.people.PeopleNotificationIdentifierImpl;
import com.android.systemui.statusbar.notification.people.PeopleNotificationIdentifierImpl_Factory;
import com.android.systemui.statusbar.notification.policy.AppMiniWindowManager;
import com.android.systemui.statusbar.notification.policy.AppMiniWindowManager_Factory;
import com.android.systemui.statusbar.notification.policy.KeyguardNotificationController;
import com.android.systemui.statusbar.notification.policy.KeyguardNotificationController_Factory;
import com.android.systemui.statusbar.notification.policy.NotificationAlertController;
import com.android.systemui.statusbar.notification.policy.NotificationAlertController_Factory;
import com.android.systemui.statusbar.notification.policy.NotificationBadgeController;
import com.android.systemui.statusbar.notification.policy.NotificationBadgeController_Factory;
import com.android.systemui.statusbar.notification.policy.NotificationCountLimitPolicy;
import com.android.systemui.statusbar.notification.policy.NotificationCountLimitPolicy_Factory;
import com.android.systemui.statusbar.notification.policy.NotificationDynamicFpsController;
import com.android.systemui.statusbar.notification.policy.NotificationDynamicFpsController_Factory;
import com.android.systemui.statusbar.notification.policy.NotificationFilterController;
import com.android.systemui.statusbar.notification.policy.NotificationFilterController_Factory;
import com.android.systemui.statusbar.notification.policy.NotificationSensitiveController;
import com.android.systemui.statusbar.notification.policy.NotificationSensitiveController_Factory;
import com.android.systemui.statusbar.notification.policy.UsbNotificationController;
import com.android.systemui.statusbar.notification.policy.UsbNotificationController_Factory;
import com.android.systemui.statusbar.notification.row.ActivatableNotificationView;
import com.android.systemui.statusbar.notification.row.ActivatableNotificationViewController;
import com.android.systemui.statusbar.notification.row.ActivatableNotificationViewController_Factory;
import com.android.systemui.statusbar.notification.row.ChannelEditorDialogController;
import com.android.systemui.statusbar.notification.row.ChannelEditorDialogController_Factory;
import com.android.systemui.statusbar.notification.row.ChannelEditorDialog_Builder_Factory;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRowController;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRowController_Factory;
import com.android.systemui.statusbar.notification.row.ExpandableOutlineViewController;
import com.android.systemui.statusbar.notification.row.ExpandableOutlineViewController_Factory;
import com.android.systemui.statusbar.notification.row.ExpandableViewController;
import com.android.systemui.statusbar.notification.row.ExpandableViewController_Factory;
import com.android.systemui.statusbar.notification.row.NotifBindPipeline;
import com.android.systemui.statusbar.notification.row.NotifBindPipelineInitializer_Factory;
import com.android.systemui.statusbar.notification.row.NotifBindPipelineLogger_Factory;
import com.android.systemui.statusbar.notification.row.NotifBindPipeline_Factory;
import com.android.systemui.statusbar.notification.row.NotifInflationErrorManager;
import com.android.systemui.statusbar.notification.row.NotifInflationErrorManager_Factory;
import com.android.systemui.statusbar.notification.row.NotifRemoteViewCache;
import com.android.systemui.statusbar.notification.row.NotifRemoteViewCacheImpl_Factory;
import com.android.systemui.statusbar.notification.row.NotificationBlockingHelperManager;
import com.android.systemui.statusbar.notification.row.NotificationContentInflater;
import com.android.systemui.statusbar.notification.row.NotificationContentInflater_Factory;
import com.android.systemui.statusbar.notification.row.NotificationGutsManager;
import com.android.systemui.statusbar.notification.row.PriorityOnboardingDialogController_Builder_Factory;
import com.android.systemui.statusbar.notification.row.RowContentBindStage;
import com.android.systemui.statusbar.notification.row.RowContentBindStageLogger_Factory;
import com.android.systemui.statusbar.notification.row.RowContentBindStage_Factory;
import com.android.systemui.statusbar.notification.row.RowInflaterTask_Factory;
import com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent;
import com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideAppNameFactory;
import com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideNotificationKeyFactory;
import com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideStatusBarNotificationFactory;
import com.android.systemui.statusbar.notification.row.dagger.NotificationRowComponent;
import com.android.systemui.statusbar.notification.stack.ForegroundServiceSectionController;
import com.android.systemui.statusbar.notification.stack.ForegroundServiceSectionController_Factory;
import com.android.systemui.statusbar.notification.stack.NotificationRoundnessManager;
import com.android.systemui.statusbar.notification.stack.NotificationRoundnessManager_Factory;
import com.android.systemui.statusbar.notification.stack.NotificationSectionsLogger;
import com.android.systemui.statusbar.notification.stack.NotificationSectionsLogger_Factory;
import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout;
import com.android.systemui.statusbar.notification.unimportant.FoldNotifController;
import com.android.systemui.statusbar.notification.unimportant.FoldNotifController_Factory;
import com.android.systemui.statusbar.notification.unimportant.UnimportantSdk;
import com.android.systemui.statusbar.notification.unimportant.UnimportantSdk_Factory;
import com.android.systemui.statusbar.notification.zen.ZenModeViewController;
import com.android.systemui.statusbar.notification.zen.ZenModeViewController_Factory;
import com.android.systemui.statusbar.phone.AutoHideController;
import com.android.systemui.statusbar.phone.AutoTileManager_Factory;
import com.android.systemui.statusbar.phone.BiometricUnlockController;
import com.android.systemui.statusbar.phone.BiometricUnlockController_Factory;
import com.android.systemui.statusbar.phone.DarkIconDispatcherImpl;
import com.android.systemui.statusbar.phone.DarkIconDispatcherImpl_Factory;
import com.android.systemui.statusbar.phone.DozeParameters;
import com.android.systemui.statusbar.phone.DozeParameters_Factory;
import com.android.systemui.statusbar.phone.DozeScrimController;
import com.android.systemui.statusbar.phone.DozeScrimController_Factory;
import com.android.systemui.statusbar.phone.DozeServiceHost;
import com.android.systemui.statusbar.phone.DozeServiceHost_Factory;
import com.android.systemui.statusbar.phone.HeadsUpManagerPhone;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.statusbar.phone.KeyguardBypassController_Factory;
import com.android.systemui.statusbar.phone.KeyguardDismissUtil;
import com.android.systemui.statusbar.phone.KeyguardDismissUtil_Factory;
import com.android.systemui.statusbar.phone.KeyguardEnvironmentImpl;
import com.android.systemui.statusbar.phone.KeyguardEnvironmentImpl_Factory;
import com.android.systemui.statusbar.phone.KeyguardLiftController;
import com.android.systemui.statusbar.phone.LightBarController;
import com.android.systemui.statusbar.phone.LightsOutNotifController;
import com.android.systemui.statusbar.phone.LightsOutNotifController_Factory;
import com.android.systemui.statusbar.phone.LockscreenGestureLogger;
import com.android.systemui.statusbar.phone.LockscreenGestureLogger_Factory;
import com.android.systemui.statusbar.phone.LockscreenLockIconController;
import com.android.systemui.statusbar.phone.LockscreenLockIconController_Factory;
import com.android.systemui.statusbar.phone.LockscreenWallpaper;
import com.android.systemui.statusbar.phone.LockscreenWallpaper_Factory;
import com.android.systemui.statusbar.phone.ManagedProfileControllerImpl;
import com.android.systemui.statusbar.phone.ManagedProfileControllerImpl_Factory;
import com.android.systemui.statusbar.phone.MiuiDripLeftStatusBarIconControllerImpl;
import com.android.systemui.statusbar.phone.MiuiDripLeftStatusBarIconControllerImpl_Factory;
import com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController;
import com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController_Factory;
import com.android.systemui.statusbar.phone.MiuiPhoneStatusBarPolicy_Factory;
import com.android.systemui.statusbar.phone.MiuiStatusBarPromptController;
import com.android.systemui.statusbar.phone.MiuiStatusBarPromptController_Factory;
import com.android.systemui.statusbar.phone.NavigationBarFragment;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.android.systemui.statusbar.phone.NavigationModeController_Factory;
import com.android.systemui.statusbar.phone.NotificationGroupAlertTransferHelper;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.phone.NotificationGroupManager_Factory;
import com.android.systemui.statusbar.phone.NotificationIconObserver_Factory;
import com.android.systemui.statusbar.phone.NotificationPanelView;
import com.android.systemui.statusbar.phone.NotificationShadeWindowController;
import com.android.systemui.statusbar.phone.NotificationShadeWindowController_Factory;
import com.android.systemui.statusbar.phone.NotificationShadeWindowView;
import com.android.systemui.statusbar.phone.NotificationShadeWindowViewController;
import com.android.systemui.statusbar.phone.PanelViewLogger_Factory;
import com.android.systemui.statusbar.phone.ScrimController;
import com.android.systemui.statusbar.phone.ScrimController_Factory;
import com.android.systemui.statusbar.phone.ShadeController;
import com.android.systemui.statusbar.phone.ShadeControllerImpl;
import com.android.systemui.statusbar.phone.ShadeControllerImpl_Factory;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.phone.StatusBarIconControllerImpl;
import com.android.systemui.statusbar.phone.StatusBarIconControllerImpl_Factory;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager_Factory;
import com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter;
import com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarterLogger_Factory;
import com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter_Builder_Factory;
import com.android.systemui.statusbar.phone.StatusBarRemoteInputCallback;
import com.android.systemui.statusbar.phone.StatusBarRemoteInputCallback_Factory;
import com.android.systemui.statusbar.phone.StatusBarTouchableRegionManager;
import com.android.systemui.statusbar.phone.StatusBarTouchableRegionManager_Factory;
import com.android.systemui.statusbar.phone.StatusBarWindowController;
import com.android.systemui.statusbar.phone.StatusBarWindowController_Factory;
import com.android.systemui.statusbar.phone.dagger.StatusBarComponent;
import com.android.systemui.statusbar.phone.dagger.StatusBarPhoneDependenciesModule_ProvideNotificationGroupAlertTransferHelperFactory;
import com.android.systemui.statusbar.phone.dagger.StatusBarPhoneModule_ProvideStatusBarFactory;
import com.android.systemui.statusbar.phone.dagger.StatusBarViewModule_GetNotificationPanelViewFactory;
import com.android.systemui.statusbar.policy.AccessibilityController;
import com.android.systemui.statusbar.policy.AccessibilityController_Factory;
import com.android.systemui.statusbar.policy.AccessibilityManagerWrapper;
import com.android.systemui.statusbar.policy.AccessibilityManagerWrapper_Factory;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BluetoothControllerImpl;
import com.android.systemui.statusbar.policy.BluetoothControllerImpl_Factory;
import com.android.systemui.statusbar.policy.CallStateControllerImpl;
import com.android.systemui.statusbar.policy.CallStateControllerImpl_Factory;
import com.android.systemui.statusbar.policy.CarrierObserver_Factory;
import com.android.systemui.statusbar.policy.CastControllerImpl;
import com.android.systemui.statusbar.policy.CastControllerImpl_Factory;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.CustomCarrierObserver;
import com.android.systemui.statusbar.policy.CustomCarrierObserver_Factory;
import com.android.systemui.statusbar.policy.DataSaverController;
import com.android.systemui.statusbar.policy.DemoModeController;
import com.android.systemui.statusbar.policy.DemoModeController_Factory;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.DeviceProvisionedControllerImpl;
import com.android.systemui.statusbar.policy.DeviceProvisionedControllerImpl_Factory;
import com.android.systemui.statusbar.policy.DriveModeControllerImpl_Factory;
import com.android.systemui.statusbar.policy.DualClockObserver;
import com.android.systemui.statusbar.policy.DualClockObserver_Factory;
import com.android.systemui.statusbar.policy.ExtensionControllerImpl;
import com.android.systemui.statusbar.policy.ExtensionControllerImpl_Factory;
import com.android.systemui.statusbar.policy.FiveGControllerImpl;
import com.android.systemui.statusbar.policy.FiveGControllerImpl_Factory;
import com.android.systemui.statusbar.policy.HotspotControllerImpl;
import com.android.systemui.statusbar.policy.HotspotControllerImpl_Factory;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.statusbar.policy.KeyguardStateControllerImpl;
import com.android.systemui.statusbar.policy.KeyguardStateControllerImpl_Factory;
import com.android.systemui.statusbar.policy.LocationControllerImpl;
import com.android.systemui.statusbar.policy.LocationControllerImpl_Factory;
import com.android.systemui.statusbar.policy.MiuiAlarmControllerImpl;
import com.android.systemui.statusbar.policy.MiuiAlarmControllerImpl_Factory;
import com.android.systemui.statusbar.policy.MiuiBrightnessController;
import com.android.systemui.statusbar.policy.MiuiBrightnessController_Factory;
import com.android.systemui.statusbar.policy.MiuiFlashlightControllerImpl;
import com.android.systemui.statusbar.policy.MiuiFlashlightControllerImpl_Factory;
import com.android.systemui.statusbar.policy.MiuiHeadsUpPolicy;
import com.android.systemui.statusbar.policy.MiuiHeadsUpPolicy_Factory;
import com.android.systemui.statusbar.policy.MiuiNotificationShadePolicy;
import com.android.systemui.statusbar.policy.MiuiNotificationShadePolicy_Factory;
import com.android.systemui.statusbar.policy.MiuiStatusBarConfigurationListener;
import com.android.systemui.statusbar.policy.MiuiStatusBarConfigurationListener_Factory;
import com.android.systemui.statusbar.policy.NetworkControllerImpl;
import com.android.systemui.statusbar.policy.NetworkControllerImpl_Factory;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.statusbar.policy.NextAlarmControllerImpl;
import com.android.systemui.statusbar.policy.NextAlarmControllerImpl_Factory;
import com.android.systemui.statusbar.policy.PaperModeControllerImpl_Factory;
import com.android.systemui.statusbar.policy.RegionController;
import com.android.systemui.statusbar.policy.RegionController_Factory;
import com.android.systemui.statusbar.policy.RemoteInputQuickSettingsDisabler;
import com.android.systemui.statusbar.policy.RemoteInputQuickSettingsDisabler_Factory;
import com.android.systemui.statusbar.policy.RemoteInputUriController;
import com.android.systemui.statusbar.policy.RemoteInputUriController_Factory;
import com.android.systemui.statusbar.policy.RotationLockControllerImpl;
import com.android.systemui.statusbar.policy.RotationLockControllerImpl_Factory;
import com.android.systemui.statusbar.policy.SecurityControllerImpl;
import com.android.systemui.statusbar.policy.SecurityControllerImpl_Factory;
import com.android.systemui.statusbar.policy.SensorPrivacyControllerImpl;
import com.android.systemui.statusbar.policy.SensorPrivacyControllerImpl_Factory;
import com.android.systemui.statusbar.policy.SlaveWifiSignalController;
import com.android.systemui.statusbar.policy.SlaveWifiSignalController_Factory;
import com.android.systemui.statusbar.policy.SmartReplyConstants;
import com.android.systemui.statusbar.policy.SmartReplyConstants_Factory;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserInfoControllerImpl;
import com.android.systemui.statusbar.policy.UserInfoControllerImpl_Factory;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.android.systemui.statusbar.policy.UserSwitcherController_Factory;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.statusbar.policy.ZenModeControllerImpl;
import com.android.systemui.statusbar.policy.ZenModeControllerImpl_Factory;
import com.android.systemui.statusbar.tv.TvStatusBar;
import com.android.systemui.statusbar.tv.TvStatusBar_Factory;
import com.android.systemui.theme.ThemeOverlayController;
import com.android.systemui.theme.ThemeOverlayController_Factory;
import com.android.systemui.toast.ToastUI;
import com.android.systemui.toast.ToastUI_Factory;
import com.android.systemui.tracing.ProtoTracer;
import com.android.systemui.tracing.ProtoTracer_Factory;
import com.android.systemui.tuner.TunablePadding;
import com.android.systemui.tuner.TunablePadding_TunablePaddingService_Factory;
import com.android.systemui.tuner.TunerActivity;
import com.android.systemui.tuner.TunerActivity_Factory;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.tuner.TunerServiceImpl;
import com.android.systemui.tuner.TunerServiceImpl_Factory;
import com.android.systemui.usb.UsbDebuggingActivity;
import com.android.systemui.usb.UsbDebuggingActivity_Factory;
import com.android.systemui.usb.UsbDebuggingSecondaryUserActivity;
import com.android.systemui.usb.UsbDebuggingSecondaryUserActivity_Factory;
import com.android.systemui.util.DeviceConfigProxy;
import com.android.systemui.util.DeviceConfigProxy_Factory;
import com.android.systemui.util.FloatingContentCoordinator;
import com.android.systemui.util.FloatingContentCoordinator_Factory;
import com.android.systemui.util.InjectionInflationController;
import com.android.systemui.util.InjectionInflationController_Factory;
import com.android.systemui.util.InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory;
import com.android.systemui.util.InjectionInflationController_ViewAttributeProvider_ProvideContextFactory;
import com.android.systemui.util.RingerModeTracker;
import com.android.systemui.util.RingerModeTrackerImpl;
import com.android.systemui.util.RingerModeTrackerImpl_Factory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideBackgroundDelayableExecutorFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideBackgroundExecutorFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideBackgroundRepeatableExecutorFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideBgHandlerFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideBgLooperFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideDelayableExecutorFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideExecutorFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideHandlerFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideLongRunningExecutorFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideLongRunningLooperFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideMainDelayableExecutorFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideMainExecutorFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideMainHandlerFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideMainLooperFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideUiBackgroundExecutorFactory;
import com.android.systemui.util.concurrency.ControlCenterConcurrencyModule_ProvideCCBackgroundExecutorFactory;
import com.android.systemui.util.concurrency.ControlCenterConcurrencyModule_ProvideCCBgLooperFactory;
import com.android.systemui.util.concurrency.DelayableExecutor;
import com.android.systemui.util.concurrency.RepeatableExecutor;
import com.android.systemui.util.io.Files;
import com.android.systemui.util.io.Files_Factory;
import com.android.systemui.util.leak.GarbageMonitor;
import com.android.systemui.util.leak.GarbageMonitor_Factory;
import com.android.systemui.util.leak.GarbageMonitor_MemoryTile_Factory;
import com.android.systemui.util.leak.GarbageMonitor_Service_Factory;
import com.android.systemui.util.leak.LeakDetector;
import com.android.systemui.util.leak.LeakReporter;
import com.android.systemui.util.leak.LeakReporter_Factory;
import com.android.systemui.util.sensors.AsyncSensorManager;
import com.android.systemui.util.sensors.AsyncSensorManager_Factory;
import com.android.systemui.util.sensors.ProximitySensor_Factory;
import com.android.systemui.util.sensors.ProximitySensor_ProximityCheck_Factory;
import com.android.systemui.util.time.DateFormatUtil_Factory;
import com.android.systemui.util.time.SystemClock;
import com.android.systemui.util.time.SystemClockImpl_Factory;
import com.android.systemui.util.wakelock.DelayedWakeLock_Builder_Factory;
import com.android.systemui.util.wakelock.WakeLock_Builder_Factory;
import com.android.systemui.vendor.HeadsetPolicy;
import com.android.systemui.vendor.HeadsetPolicy_Factory;
import com.android.systemui.vendor.OrientationPolicy;
import com.android.systemui.vendor.OrientationPolicy_Factory;
import com.android.systemui.volume.VolumeDialogComponent;
import com.android.systemui.volume.VolumeDialogComponent_Factory;
import com.android.systemui.volume.VolumeDialogControllerImpl;
import com.android.systemui.volume.VolumeDialogControllerImpl_Factory;
import com.android.systemui.volume.VolumeUI;
import com.android.systemui.volume.VolumeUI_Factory;
import com.android.systemui.wm.DisplayController;
import com.android.systemui.wm.DisplayController_Factory;
import com.android.systemui.wm.DisplayImeController;
import com.android.systemui.wm.DisplayImeController_Factory;
import com.android.systemui.wm.SystemWindows;
import com.android.systemui.wm.SystemWindows_Factory;
import com.miui.systemui.CloudDataManager;
import com.miui.systemui.CloudDataManager_Factory;
import com.miui.systemui.EventTracker;
import com.miui.systemui.EventTracker_Factory;
import com.miui.systemui.SettingsManager;
import com.miui.systemui.SettingsManager_Factory;
import com.miui.systemui.SettingsObserverImpl;
import com.miui.systemui.SettingsObserverImpl_Factory;
import com.miui.systemui.analytics.SystemUIStat;
import com.miui.systemui.analytics.SystemUIStat_Factory;
import com.miui.systemui.display.OLEDScreenHelper;
import com.miui.systemui.display.OLEDScreenHelper_Factory;
import com.miui.systemui.graphics.AppIconsManager;
import com.miui.systemui.graphics.AppIconsManager_Factory;
import com.miui.systemui.statusbar.PanelExpansionObserver;
import com.miui.systemui.statusbar.PanelExpansionObserver_Factory;
import com.miui.systemui.statusbar.phone.ForceBlackObserver;
import com.miui.systemui.statusbar.phone.ForceBlackObserver_Factory;
import com.miui.systemui.statusbar.phone.SmartDarkObserver;
import com.miui.systemui.statusbar.phone.SmartDarkObserver_Factory;
import com.miui.systemui.util.GestureObserver_Factory;
import com.miui.systemui.util.HapticFeedBackImpl;
import com.miui.systemui.util.HapticFeedBackImpl_Factory;
import com.miui.systemui.util.MiuiActivityUtil;
import com.miui.systemui.util.MiuiActivityUtil_Factory;
import com.miui.systemui.util.PackageEventController;
import com.miui.systemui.util.PackageEventController_Factory;
import dagger.Lazy;
import dagger.internal.DelegateFactory;
import dagger.internal.DoubleCheck;
import dagger.internal.Factory;
import dagger.internal.InstanceFactory;
import dagger.internal.MapProviderFactory;
import dagger.internal.Preconditions;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import javax.inject.Provider;

public final class DaggerSystemUIRootComponent implements SystemUIRootComponent {
    private static final Provider ABSENT_JDK_OPTIONAL_PROVIDER = InstanceFactory.create(Optional.empty());
    private Provider<AccessibilityController> accessibilityControllerProvider;
    private Provider<AccessibilityManagerWrapper> accessibilityManagerWrapperProvider;
    private ActionClickLogger_Factory actionClickLoggerProvider;
    private GlobalScreenshot_ActionProxyReceiver_Factory actionProxyReceiverProvider;
    private Provider<ActivityIntentHelper> activityIntentHelperProvider;
    private Provider<ActivityStarterDelegate> activityStarterDelegateProvider;
    private AirplaneModeTile_Factory airplaneModeTileProvider;
    private Provider<AppIconsManager> appIconsManagerProvider;
    private Provider<AppMiniWindowManager> appMiniWindowManagerProvider;
    private Provider<AppOpsControllerImpl> appOpsControllerImplProvider;
    private Provider<AppOpsCoordinator> appOpsCoordinatorProvider;
    private Provider<AssistHandleBehaviorController> assistHandleBehaviorControllerProvider;
    private Provider assistHandleLikeHomeBehaviorProvider;
    private Provider assistHandleOffBehaviorProvider;
    private Provider assistHandleReminderExpBehaviorProvider;
    private Provider<AssistLogger> assistLoggerProvider;
    private Provider<AssistManager> assistManagerProvider;
    private Provider<AsyncSensorManager> asyncSensorManagerProvider;
    private Provider<AuthController> authControllerProvider;
    private AutoBrightnessTile_Factory autoBrightnessTileProvider;
    private AutoTileManager_Factory autoTileManagerProvider;
    private BatterySaverTile_Factory batterySaverTileProvider;
    private Provider<SystemClock> bindSystemClockProvider;
    private Provider<BiometricUnlockController> biometricUnlockControllerProvider;
    private Provider<BluetoothControllerImpl> bluetoothControllerImplProvider;
    private BluetoothTile_Factory bluetoothTileProvider;
    private Provider<BlurUtils> blurUtilsProvider;
    private Provider<BootCompleteCacheImpl> bootCompleteCacheImplProvider;
    private BrightnessDialog_Factory brightnessDialogProvider;
    private BroadcastDispatcherLogger_Factory broadcastDispatcherLoggerProvider;
    private Provider<BubbleCoordinator> bubbleCoordinatorProvider;
    private Provider<BubbleData> bubbleDataProvider;
    private Provider<BubbleDataRepository> bubbleDataRepositoryProvider;
    private BubbleOverflowActivity_Factory bubbleOverflowActivityProvider;
    private Provider<BubblePersistentRepository> bubblePersistentRepositoryProvider;
    private Provider<BubbleVolatileRepository> bubbleVolatileRepositoryProvider;
    private NotificationClicker_Builder_Factory builderProvider;
    private WakeLock_Builder_Factory builderProvider2;
    private DelayedWakeLock_Builder_Factory builderProvider3;
    private Provider<StatusBarNotificationActivityStarter.Builder> builderProvider4;
    private AutoAddTracker_Builder_Factory builderProvider5;
    private Provider<BypassHeadsUpNotifier> bypassHeadsUpNotifierProvider;
    private Provider<CallStateControllerImpl> callStateControllerImplProvider;
    private Provider<CastControllerImpl> castControllerImplProvider;
    private CastTile_Factory castTileProvider;
    private CellularTile_Factory cellularTileProvider;
    private Provider<ChannelEditorDialogController> channelEditorDialogControllerProvider;
    private Provider<ClockManager> clockManagerProvider;
    private Provider<CloudDataManager> cloudDataManagerProvider;
    private Provider<CodeBlueService> codeBlueServiceProvider;
    private ColorInversionTile_Factory colorInversionTileProvider;
    private Provider<ContextComponentResolver> contextComponentResolverProvider;
    private SystemUIFactory.ContextHolder contextHolder;
    private Provider<ControlActionCoordinatorImpl> controlActionCoordinatorImplProvider;
    private Provider<ControlCenterActivityStarter> controlCenterActivityStarterProvider;
    private Provider<ControlPanelController> controlPanelControllerProvider;
    private Provider<ControlPanelWindowManager> controlPanelWindowManagerProvider;
    private Provider<ControlsBindingControllerImpl> controlsBindingControllerImplProvider;
    private Provider<ControlsComponent> controlsComponentProvider;
    private Provider<ControlsControllerImpl> controlsControllerImplProvider;
    private ControlsEditingActivity_Factory controlsEditingActivityProvider;
    private ControlsFavoritingActivity_Factory controlsFavoritingActivityProvider;
    private Provider<ControlsListingControllerImpl> controlsListingControllerImplProvider;
    private Provider<ControlsPluginManager> controlsPluginManagerProvider;
    private ControlsProviderSelectorActivity_Factory controlsProviderSelectorActivityProvider;
    private ControlsRequestDialog_Factory controlsRequestDialogProvider;
    private Provider<ControlsUiControllerImpl> controlsUiControllerImplProvider;
    private Provider<ConversationCoordinator> conversationCoordinatorProvider;
    private Provider<ConversationNotificationManager> conversationNotificationManagerProvider;
    private ConversationNotificationProcessor_Factory conversationNotificationProcessorProvider;
    private Provider<CustomCarrierObserver> customCarrierObserverProvider;
    private Provider<DarkIconDispatcherImpl> darkIconDispatcherImplProvider;
    private DataSaverTile_Factory dataSaverTileProvider;
    private DateFormatUtil_Factory dateFormatUtilProvider;
    private Provider<DefaultUiController> defaultUiControllerProvider;
    private Provider<DemoModeController> demoModeControllerProvider;
    private Provider<DeviceConfigHelper> deviceConfigHelperProvider;
    private Provider<DeviceProvisionedControllerImpl> deviceProvisionedControllerImplProvider;
    private Provider<DeviceProvisionedCoordinator> deviceProvisionedCoordinatorProvider;
    private Provider<DismissCallbackRegistry> dismissCallbackRegistryProvider;
    private Provider<DisplayController> displayControllerProvider;
    private Provider<DisplayImeController> displayImeControllerProvider;
    private DndTile_Factory dndTileProvider;
    private Provider<DockManagerImpl> dockManagerImplProvider;
    private DozeFactory_Factory dozeFactoryProvider;
    private Provider<DozeLog> dozeLogProvider;
    private DozeLogger_Factory dozeLoggerProvider;
    private Provider<DozeParameters> dozeParametersProvider;
    private Provider<DozeScrimController> dozeScrimControllerProvider;
    private Provider<DozeServiceHost> dozeServiceHostProvider;
    private DozeService_Factory dozeServiceProvider;
    private DriveModeControllerImpl_Factory driveModeControllerImplProvider;
    private DriveModeTile_Factory driveModeTileProvider;
    private Provider<DualClockObserver> dualClockObserverProvider;
    private DumpHandler_Factory dumpHandlerProvider;
    private Provider<DumpManager> dumpManagerProvider;
    private DynamicChildBindController_Factory dynamicChildBindControllerProvider;
    private Provider<DynamicPrivacyController> dynamicPrivacyControllerProvider;
    private EditTile_Factory editTileProvider;
    private Provider<EnhancedEstimatesImpl> enhancedEstimatesImplProvider;
    private Provider<EventTracker> eventTrackerProvider;
    private Provider<ExpandInfoControllerImpl> expandInfoControllerImplProvider;
    private Provider<ExpandableNotificationRowComponent.Builder> expandableNotificationRowComponentBuilderProvider;
    private NotificationLogger_ExpansionStateLogger_Factory expansionStateLoggerProvider;
    private Provider<ExtensionControllerImpl> extensionControllerImplProvider;
    private FallbackTakeScreenshotService_Factory fallbackTakeScreenshotServiceProvider;
    private Provider<FalsingManagerProxy> falsingManagerProxyProvider;
    private Provider<FeatureFlags> featureFlagsProvider;
    private Provider<Files> filesProvider;
    private Provider<FiveGControllerImpl> fiveGControllerImplProvider;
    private FlashlightTile_Factory flashlightTileProvider;
    private Provider<FloatingContentCoordinator> floatingContentCoordinatorProvider;
    private Provider<FoldNotifController> foldNotifControllerProvider;
    private Provider<ForceBlackObserver> forceBlackObserverProvider;
    private Provider<ForegroundServiceController> foregroundServiceControllerProvider;
    private Provider<ForegroundServiceDismissalFeatureController> foregroundServiceDismissalFeatureControllerProvider;
    private ForegroundServiceLifetimeExtender_Factory foregroundServiceLifetimeExtenderProvider;
    private Provider<ForegroundServiceNotificationListener> foregroundServiceNotificationListenerProvider;
    private Provider<ForegroundServiceSectionController> foregroundServiceSectionControllerProvider;
    private Provider<FragmentService> fragmentServiceProvider;
    private Provider<GarbageMonitor> garbageMonitorProvider;
    private Provider<GlobalActionsComponent> globalActionsComponentProvider;
    private GlobalActionsDialog_Factory globalActionsDialogProvider;
    private GlobalActionsImpl_Factory globalActionsImplProvider;
    private Provider<GlobalScreenshot> globalScreenshotProvider;
    private GroupCoalescerLogger_Factory groupCoalescerLoggerProvider;
    private GroupCoalescer_Factory groupCoalescerProvider;
    private Provider<HapticFeedBackImpl> hapticFeedBackImplProvider;
    private Provider<HeadsUpController> headsUpControllerProvider;
    private Provider<HeadsUpCoordinator> headsUpCoordinatorProvider;
    private Provider<HeadsUpViewBinder> headsUpViewBinderProvider;
    private Provider<HeadsetPolicy> headsetPolicyProvider;
    private HideNotifsForOtherUsersCoordinator_Factory hideNotifsForOtherUsersCoordinatorProvider;
    private Provider<HighPriorityProvider> highPriorityProvider;
    private Provider<HotspotControllerImpl> hotspotControllerImplProvider;
    private HotspotTile_Factory hotspotTileProvider;
    private IconBuilder_Factory iconBuilderProvider;
    private Provider<InitController> initControllerProvider;
    private Provider<InjectionInflationController> injectionInflationControllerProvider;
    private Provider<InstantAppNotifier> instantAppNotifierProvider;
    private Provider<KeyguardBottomAreaInjector> keyguardBottomAreaInjectorProvider;
    private Provider<KeyguardBypassController> keyguardBypassControllerProvider;
    private Provider<KeyguardClockInjector> keyguardClockInjectorProvider;
    private Provider<KeyguardCoordinator> keyguardCoordinatorProvider;
    private Provider<KeyguardDismissUtil> keyguardDismissUtilProvider;
    private Provider<KeyguardEnvironmentImpl> keyguardEnvironmentImplProvider;
    private Provider<KeyguardIndicationController> keyguardIndicationControllerProvider;
    private Provider<KeyguardIndicationInjector> keyguardIndicationInjectorProvider;
    private Provider<KeyguardLifecyclesDispatcher> keyguardLifecyclesDispatcherProvider;
    private Provider<KeyguardNegative1PageInjector> keyguardNegative1PageInjectorProvider;
    private Provider<KeyguardNotificationController> keyguardNotificationControllerProvider;
    private Provider<KeyguardPanelViewInjector> keyguardPanelViewInjectorProvider;
    private Provider<KeyguardSecurityModel> keyguardSecurityModelProvider;
    private Provider<KeyguardSensorInjector> keyguardSensorInjectorProvider;
    private KeyguardService_Factory keyguardServiceProvider;
    private Provider<KeyguardStateControllerImpl> keyguardStateControllerImplProvider;
    private Provider<KeyguardUpdateMonitorInjector> keyguardUpdateMonitorInjectorProvider;
    private Provider<KeyguardUpdateMonitor> keyguardUpdateMonitorProvider;
    private Provider<KeyguardViewMediatorInjector> keyguardViewMediatorInjectorProvider;
    private Provider<LatencyTester> latencyTesterProvider;
    private Provider<LeakReporter> leakReporterProvider;
    private Provider<LightsOutNotifController> lightsOutNotifControllerProvider;
    private LocalMediaManagerFactory_Factory localMediaManagerFactoryProvider;
    private Provider<LocationControllerImpl> locationControllerImplProvider;
    private LocationTile_Factory locationTileProvider;
    private Provider<LockScreenMagazineController> lockScreenMagazineControllerProvider;
    private Provider<LockscreenGestureLogger> lockscreenGestureLoggerProvider;
    private Provider<LockscreenLockIconController> lockscreenLockIconControllerProvider;
    private Provider<LockscreenWallpaper> lockscreenWallpaperProvider;
    private Provider<LogBufferEulogizer> logBufferEulogizerProvider;
    private LogBufferFreezer_Factory logBufferFreezerProvider;
    private Provider<LowPriorityInflationHelper> lowPriorityInflationHelperProvider;
    private Provider<ManagedProfileControllerImpl> managedProfileControllerImplProvider;
    private Provider<Map<Class<?>, Provider<Activity>>> mapOfClassOfAndProviderOfActivityProvider;
    private Provider<Map<Class<?>, Provider<BroadcastReceiver>>> mapOfClassOfAndProviderOfBroadcastReceiverProvider;
    private Provider<Map<Class<?>, Provider<RecentsImplementation>>> mapOfClassOfAndProviderOfRecentsImplementationProvider;
    private Provider<Map<Class<?>, Provider<Service>>> mapOfClassOfAndProviderOfServiceProvider;
    private Provider<Map<Class<?>, Provider<SystemUI>>> mapOfClassOfAndProviderOfSystemUIProvider;
    private Provider<MediaArtworkProcessor> mediaArtworkProcessorProvider;
    private Provider<MediaCarouselController> mediaCarouselControllerProvider;
    private MediaControlLogger_Factory mediaControlLoggerProvider;
    private MediaControllerFactory_Factory mediaControllerFactoryProvider;
    private MediaCoordinator_Factory mediaCoordinatorProvider;
    private Provider<MediaDataCombineLatest> mediaDataCombineLatestProvider;
    private Provider<MediaDataFilter> mediaDataFilterProvider;
    private Provider<MediaDataManager> mediaDataManagerProvider;
    private Provider<MediaDeviceManager> mediaDeviceManagerProvider;
    private MediaFeatureFlag_Factory mediaFeatureFlagProvider;
    private Provider<MediaHierarchyManager> mediaHierarchyManagerProvider;
    private MediaHost_Factory mediaHostProvider;
    private Provider<MediaHostStatesManager> mediaHostStatesManagerProvider;
    private Provider<MediaResumeListener> mediaResumeListenerProvider;
    private Provider<MediaTimeoutListener> mediaTimeoutListenerProvider;
    private MediaViewController_Factory mediaViewControllerProvider;
    private GarbageMonitor_MemoryTile_Factory memoryTileProvider;
    private Provider<MiPlayPluginManager> miPlayPluginManagerProvider;
    private Provider<MiuiActivityUtil> miuiActivityUtilProvider;
    private MiuiAirplaneModeTile_Factory miuiAirplaneModeTileProvider;
    private Provider<MiuiAlarmControllerImpl> miuiAlarmControllerImplProvider;
    private Provider<MiuiBrightnessController> miuiBrightnessControllerProvider;
    private MiuiCellularTile_Factory miuiCellularTileProvider;
    private Provider<MiuiChargeController> miuiChargeControllerProvider;
    private Provider<MiuiChargeManager> miuiChargeManagerProvider;
    private Provider<MiuiDozeServiceHost> miuiDozeServiceHostProvider;
    private Provider<MiuiDripLeftStatusBarIconControllerImpl> miuiDripLeftStatusBarIconControllerImplProvider;
    private Provider<MiuiFaceUnlockManager> miuiFaceUnlockManagerProvider;
    private Provider<MiuiFastUnlockController> miuiFastUnlockControllerProvider;
    private Provider<MiuiFlashlightControllerImpl> miuiFlashlightControllerImplProvider;
    private Provider<MiuiFullScreenGestureProxy> miuiFullScreenGestureProxyProvider;
    private Provider<MiuiGxzwManager> miuiGxzwManagerProvider;
    private Provider<MiuiGxzwPolicy> miuiGxzwPolicyProvider;
    private Provider<MiuiHeadsUpPolicy> miuiHeadsUpPolicyProvider;
    private MiuiHotspotTile_Factory miuiHotspotTileProvider;
    private MiuiIconManager_Factory miuiIconManagerProvider;
    private Provider<MiuiKeyguardMediaController> miuiKeyguardMediaControllerProvider;
    private Provider<MiuiKeyguardWallpaperControllerImpl> miuiKeyguardWallpaperControllerImplProvider;
    private MiuiMediaControlPanel_Factory miuiMediaControlPanelProvider;
    private Provider<MiuiMediaTransferManager> miuiMediaTransferManagerProvider;
    private Provider<MiuiNotificationInterruptStateProviderImpl> miuiNotificationInterruptStateProviderImplProvider;
    private MiuiNotificationSectionsFeatureManager_Factory miuiNotificationSectionsFeatureManagerProvider;
    private Provider<MiuiNotificationShadePolicy> miuiNotificationShadePolicyProvider;
    private MiuiPhoneStatusBarPolicy_Factory miuiPhoneStatusBarPolicyProvider;
    private Provider<MiuiQSTileHostInjector> miuiQSTileHostInjectorProvider;
    private Provider<MiuiQuickConnectController> miuiQuickConnectControllerProvider;
    private Provider<MiuiRecentProxy> miuiRecentProxyProvider;
    private Provider<MiuiStatusBarConfigurationListener> miuiStatusBarConfigurationListenerProvider;
    private Provider<MiuiStatusBarPromptController> miuiStatusBarPromptControllerProvider;
    private Provider<MiuiVendorServices> miuiVendorServicesProvider;
    private Provider<MiuiWallpaperClient> miuiWallpaperClientProvider;
    private Provider<MiuiWallpaperZoomOutService> miuiWallpaperZoomOutServiceProvider;
    private Provider<ModalController> modalControllerProvider;
    private Provider<ModalRowInflater> modalRowInflaterProvider;
    private MuteTile_Factory muteTileProvider;
    private Provider<NCSwitchController> nCSwitchControllerProvider;
    private Provider<NavigationModeController> navigationModeControllerProvider;
    private Provider<NetworkControllerImpl> networkControllerImplProvider;
    private Provider<NetworkSpeedController> networkSpeedControllerProvider;
    private Provider<BubbleController> newBubbleControllerProvider;
    private Provider<KeyguardViewMediator> newKeyguardViewMediatorProvider;
    private Provider<NextAlarmControllerImpl> nextAlarmControllerImplProvider;
    private NfcTile_Factory nfcTileProvider;
    private NightDisplayTile_Factory nightDisplayTileProvider;
    private NightModeTile_Factory nightModeTileProvider;
    private NotifBindPipelineInitializer_Factory notifBindPipelineInitializerProvider;
    private NotifBindPipelineLogger_Factory notifBindPipelineLoggerProvider;
    private Provider<NotifBindPipeline> notifBindPipelineProvider;
    private NotifCollectionLogger_Factory notifCollectionLoggerProvider;
    private Provider<NotifCollection> notifCollectionProvider;
    private Provider<NotifCoordinators> notifCoordinatorsProvider;
    private Provider<NotifInflaterImpl> notifInflaterImplProvider;
    private Provider<NotifInflationErrorManager> notifInflationErrorManagerProvider;
    private Provider<NotifPipelineInitializer> notifPipelineInitializerProvider;
    private Provider<NotifPipeline> notifPipelineProvider;
    private NotifRemoteViewCacheImpl_Factory notifRemoteViewCacheImplProvider;
    private Provider<NotifViewBarn> notifViewBarnProvider;
    private Provider<NotifViewManager> notifViewManagerProvider;
    private Provider<NotificationAlertController> notificationAlertControllerProvider;
    private Provider<NotificationBadgeController> notificationBadgeControllerProvider;
    private Provider<NotificationClickNotifier> notificationClickNotifierProvider;
    private NotificationClickerLogger_Factory notificationClickerLoggerProvider;
    private Provider<NotificationContentInflater> notificationContentInflaterProvider;
    private Provider<NotificationCountLimitPolicy> notificationCountLimitPolicyProvider;
    private Provider<NotificationDynamicFpsController> notificationDynamicFpsControllerProvider;
    private NotificationEntryManagerLogger_Factory notificationEntryManagerLoggerProvider;
    private Provider<NotificationFilterController> notificationFilterControllerProvider;
    private Provider<NotificationFilter> notificationFilterProvider;
    private Provider<NotificationGroupManager> notificationGroupManagerProvider;
    private Provider<NotificationInteractionTracker> notificationInteractionTrackerProvider;
    private Provider<NotificationLockscreenUserManagerImpl> notificationLockscreenUserManagerImplProvider;
    private Provider<NotificationPanelNavigationBarCoordinator> notificationPanelNavigationBarCoordinatorProvider;
    private Provider<NotificationPersonExtractorPluginBoundary> notificationPersonExtractorPluginBoundaryProvider;
    private NotificationRankingManager_Factory notificationRankingManagerProvider;
    private Provider<NotificationRoundnessManager> notificationRoundnessManagerProvider;
    private Provider<NotificationRowBinderImpl> notificationRowBinderImplProvider;
    private Provider<NotificationRowComponent.Builder> notificationRowComponentBuilderProvider;
    private Provider<NotificationSectionsLogger> notificationSectionsLoggerProvider;
    private Provider<NotificationSensitiveController> notificationSensitiveControllerProvider;
    private Provider<NotificationSettingsManager> notificationSettingsManagerProvider;
    private Provider<NotificationShadeDepthController> notificationShadeDepthControllerProvider;
    private Provider<NotificationShadeWindowController> notificationShadeWindowControllerProvider;
    private Provider<NotificationStat> notificationStatProvider;
    private Provider<NotificationWakeUpCoordinator> notificationWakeUpCoordinatorProvider;
    private Provider<NotificationsControllerImpl> notificationsControllerImplProvider;
    private NotificationsControllerStub_Factory notificationsControllerStubProvider;
    private Provider<OLEDScreenHelper> oLEDScreenHelperProvider;
    private Provider<OldModeController> oldModeControllerProvider;
    private Provider<Optional<ControlsFavoritePersistenceWrapper>> optionalOfControlsFavoritePersistenceWrapperProvider;
    private Provider<Optional<Divider>> optionalOfDividerProvider;
    private Provider<Optional<Lazy<Recents>>> optionalOfLazyOfRecentsProvider;
    private Provider<Optional<Lazy<StatusBar>>> optionalOfLazyOfStatusBarProvider;
    private Provider<Optional<Recents>> optionalOfRecentsProvider;
    private Provider<Optional<StatusBar>> optionalOfStatusBarProvider;
    private Provider<OrientationPolicy> orientationPolicyProvider;
    private Provider<OverviewProxyRecentsImpl> overviewProxyRecentsImplProvider;
    private Provider<OverviewProxyService> overviewProxyServiceProvider;
    private Provider<PackageEventController> packageEventControllerProvider;
    private Provider<PanelExpansionObserver> panelExpansionObserverProvider;
    private PanelViewLogger_Factory panelViewLoggerProvider;
    private PaperModeControllerImpl_Factory paperModeControllerImplProvider;
    private PaperModeTile_Factory paperModeTileProvider;
    private Provider<PeopleHubDataSourceImpl> peopleHubDataSourceImplProvider;
    private Provider<PeopleHubViewAdapterImpl> peopleHubViewAdapterImplProvider;
    private Provider<PeopleHubViewModelFactoryDataSourceImpl> peopleHubViewModelFactoryDataSourceImplProvider;
    private Provider<PeopleNotificationIdentifierImpl> peopleNotificationIdentifierImplProvider;
    private Provider<PhoneSignalControllerImpl> phoneSignalControllerImplProvider;
    private Provider<PhoneStateMonitor> phoneStateMonitorProvider;
    private PipAnimationController_Factory pipAnimationControllerProvider;
    private Provider<PipBoundsHandler> pipBoundsHandlerProvider;
    private Provider<PipManager> pipManagerProvider;
    private PipSnapAlgorithm_Factory pipSnapAlgorithmProvider;
    private Provider<PipSurfaceTransactionHelper> pipSurfaceTransactionHelperProvider;
    private Provider<PipTaskOrganizer> pipTaskOrganizerProvider;
    private Provider<PipUI> pipUIProvider;
    private Provider<PipUiEventLogger> pipUiEventLoggerProvider;
    private Provider<PluginDependencyProvider> pluginDependencyProvider;
    private PowerModeTile_Factory powerModeTileProvider;
    private Provider<PowerNotificationWarnings> powerNotificationWarningsProvider;
    private PowerSaverExtremeTile_Factory powerSaverExtremeTileProvider;
    private PowerSaverTile_Factory powerSaverTileProvider;
    private Provider<PowerUI> powerUIProvider;
    private PreparationCoordinatorLogger_Factory preparationCoordinatorLoggerProvider;
    private Provider<PreparationCoordinator> preparationCoordinatorProvider;
    private Provider<ProtoTracer> protoTracerProvider;
    private Provider<AccessibilityManager> provideAccessibilityManagerProvider;
    private Provider<ActivityManager> provideActivityManagerProvider;
    private Provider<ActivityManagerWrapper> provideActivityManagerWrapperProvider;
    private Provider<AlarmManager> provideAlarmManagerProvider;
    private Provider<Boolean> provideAllowNotificationLongPressProvider;
    private DependencyProvider_ProvideAlwaysOnDisplayPolicyFactory provideAlwaysOnDisplayPolicyProvider;
    private DependencyProvider_ProvideAmbientDisplayConfigurationFactory provideAmbientDisplayConfigurationProvider;
    private Provider provideAssistHandleBehaviorControllerMapProvider;
    private AssistModule_ProvideAssistHandleViewControllerFactory provideAssistHandleViewControllerProvider;
    private Provider<AssistUtils> provideAssistUtilsProvider;
    private Provider<AudioManager> provideAudioManagerProvider;
    private Provider<AutoHideController> provideAutoHideControllerProvider;
    private Provider<DelayableExecutor> provideBackgroundDelayableExecutorProvider;
    private Provider<Executor> provideBackgroundExecutorProvider;
    private Provider<Handler> provideBackgroundHandlerProvider;
    private Provider<RepeatableExecutor> provideBackgroundRepeatableExecutorProvider;
    private Provider<BatteryController> provideBatteryControllerProvider;
    private ConcurrencyModule_ProvideBgHandlerFactory provideBgHandlerProvider;
    private Provider<Looper> provideBgLooperProvider;
    private Provider<LogBuffer> provideBroadcastDispatcherLogBufferProvider;
    private Provider<Executor> provideCCBackgroundExecutorProvider;
    private Provider<Looper> provideCCBgLooperProvider;
    private Provider<CommandQueue> provideCommandQueueProvider;
    private Provider<CommonNotifCollection> provideCommonNotifCollectionProvider;
    private Provider<ConfigurationController> provideConfigurationControllerProvider;
    private Provider<ConnectivityManager> provideConnectivityManagagerProvider;
    private Provider<ContentResolver> provideContentResolverProvider;
    private SystemUIFactory_ContextHolder_ProvideContextFactory provideContextProvider;
    private Provider<ControlCenter> provideControlCenterProvider;
    private Provider<CurrentUserContextTracker> provideCurrentUserContextTrackerProvider;
    private Provider<DataSaverController> provideDataSaverControllerProvider;
    private Provider<DelayableExecutor> provideDelayableExecutorProvider;
    private Provider<DevicePolicyManager> provideDevicePolicyManagerProvider;
    private Provider<DevicePolicyManagerWrapper> provideDevicePolicyManagerWrapperProvider;
    private SystemServicesModule_ProvideDisplayIdFactory provideDisplayIdProvider;
    private Provider<DisplayMetrics> provideDisplayMetricsProvider;
    private Provider<Divider> provideDividerProvider;
    private Provider<LogBuffer> provideDozeLogBufferProvider;
    private Provider<Executor> provideExecutorProvider;
    private Provider<HeadsUpManagerPhone> provideHeadsUpManagerPhoneProvider;
    private Provider<IActivityManager> provideIActivityManagerProvider;
    private Provider<IBatteryStats> provideIBatteryStatsProvider;
    private Provider<IDreamManager> provideIDreamManagerProvider;
    private Provider<INotificationManager> provideINotificationManagerProvider;
    private Provider<IPackageManager> provideIPackageManagerProvider;
    private Provider<IStatusBarService> provideIStatusBarServiceProvider;
    private Provider<IWindowManager> provideIWindowManagerProvider;
    private Provider<KeyguardLiftController> provideKeyguardLiftControllerProvider;
    private Provider<KeyguardManager> provideKeyguardManagerProvider;
    private Provider<LatencyTracker> provideLatencyTrackerProvider;
    private Provider<LauncherApps> provideLauncherAppsProvider;
    private Provider<LeakDetector> provideLeakDetectorProvider;
    private Provider<String> provideLeakReportEmailProvider;
    private Provider<LightBarController> provideLightBarControllerProvider;
    private Provider<LocalBluetoothManager> provideLocalBluetoothControllerProvider;
    private DependencyProvider_ProvideLockPatternUtilsFactory provideLockPatternUtilsProvider;
    private Provider<LogcatEchoTracker> provideLogcatEchoTrackerProvider;
    private Provider<Executor> provideLongRunningExecutorProvider;
    private Provider<Looper> provideLongRunningLooperProvider;
    private Provider<DelayableExecutor> provideMainDelayableExecutorProvider;
    private ConcurrencyModule_ProvideMainExecutorFactory provideMainExecutorProvider;
    private ConcurrencyModule_ProvideMainHandlerFactory provideMainHandlerProvider;
    private Provider<LogBuffer> provideMediaControlLogBufferProvider;
    private SystemServicesModule_ProvideMediaRouter2ManagerFactory provideMediaRouter2ManagerProvider;
    private Provider<MetricsLogger> provideMetricsLoggerProvider;
    private Provider<NavigationBarController> provideNavigationBarControllerProvider;
    private Provider<NetworkScoreManager> provideNetworkScoreManagerProvider;
    private Provider<NightDisplayListener> provideNightDisplayListenerProvider;
    private Provider<LogBuffer> provideNotifInteractionLogBufferProvider;
    private Provider<NotifRemoteViewCache> provideNotifRemoteViewCacheProvider;
    private Provider<NotificationBlockingHelperManager> provideNotificationBlockingHelperManagerProvider;
    private Provider<NotificationEntryManager> provideNotificationEntryManagerProvider;
    private Provider<NotificationGroupAlertTransferHelper> provideNotificationGroupAlertTransferHelperProvider;
    private Provider<NotificationGutsManager> provideNotificationGutsManagerProvider;
    private Provider<NotificationListener> provideNotificationListenerProvider;
    private Provider<NotificationLogger> provideNotificationLoggerProvider;
    private Provider<NotificationManager> provideNotificationManagerProvider;
    private Provider<NotificationMediaManager> provideNotificationMediaManagerProvider;
    private DependencyProvider_ProvideNotificationMessagingUtilFactory provideNotificationMessagingUtilProvider;
    private Provider<NotificationPanelLogger> provideNotificationPanelLoggerProvider;
    private Provider<NotificationRemoteInputManager> provideNotificationRemoteInputManagerProvider;
    private Provider<LogBuffer> provideNotificationSectionLogBufferProvider;
    private Provider<NotificationViewHierarchyManager> provideNotificationViewHierarchyManagerProvider;
    private Provider<NotificationsController> provideNotificationsControllerProvider;
    private Provider<LogBuffer> provideNotificationsLogBufferProvider;
    private Provider<PackageManager> providePackageManagerProvider;
    private Provider<PackageManagerWrapper> providePackageManagerWrapperProvider;
    private Provider<LogBuffer> providePanelViewLogBufferProvider;
    private Provider<PluginManager> providePluginManagerProvider;
    private Provider<PowerManager> providePowerManagerProvider;
    private Provider<LogBuffer> provideQuickSettingsLogBufferProvider;
    private RecentsModule_ProvideRecentsImplFactory provideRecentsImplProvider;
    private Provider<Recents> provideRecentsProvider;
    private SystemServicesModule_ProvideResourcesFactory provideResourcesProvider;
    private Provider<SensorPrivacyManager> provideSensorPrivacyManagerProvider;
    private DependencyProvider_ProvideSharePreferencesFactory provideSharePreferencesProvider;
    private Provider<ShortcutManager> provideShortcutManagerProvider;
    private Provider<SmartReplyController> provideSmartReplyControllerProvider;
    private Provider<StatusBar> provideStatusBarProvider;
    private Provider<SysUiState> provideSysUiStateProvider;
    private Provider<Clock> provideSystemClockProvider;
    private Provider<TelecomManager> provideTelecomManagerProvider;
    private Provider<TelephonyManager> provideTelephonyManagerProvider;
    private Provider<Handler> provideTimeTickHandlerProvider;
    private Provider<TrustManager> provideTrustManagerProvider;
    private Provider<Executor> provideUiBackgroundExecutorProvider;
    private Provider<UiEventLogger> provideUiEventLoggerProvider;
    private Provider<UserManager> provideUserManagerProvider;
    private Provider<Vibrator> provideVibratorProvider;
    private Provider<VisualStabilityManager> provideVisualStabilityManagerProvider;
    private SystemServicesModule_ProvideWallpaperManagerFactory provideWallpaperManagerProvider;
    private Provider<WifiManager> provideWifiManagerProvider;
    private Provider<WindowManager> provideWindowManagerProvider;
    private Provider<BroadcastDispatcher> providesBroadcastDispatcherProvider;
    private Provider<Choreographer> providesChoreographerProvider;
    private Provider<Boolean> providesControlsFeatureEnabledProvider;
    private DependencyProvider_ProvidesViewMediatorCallbackFactory providesViewMediatorCallbackProvider;
    private ProximitySensor_ProximityCheck_Factory proximityCheckProvider;
    private ProximitySensor_Factory proximitySensorProvider;
    private Provider<PulseExpansionHandler> pulseExpansionHandlerProvider;
    private Provider<QSFactoryImpl> qSFactoryImplProvider;
    private Provider<QSFactoryInjectorImpl> qSFactoryInjectorImplProvider;
    private QSLogger_Factory qSLoggerProvider;
    private Provider<QSTileHost> qSTileHostProvider;
    private QuietModeTile_Factory quietModeTileProvider;
    private Provider<RankingCoordinator> rankingCoordinatorProvider;
    private Provider<RecordingController> recordingControllerProvider;
    private RecordingService_Factory recordingServiceProvider;
    private Provider<RegionController> regionControllerProvider;
    private Provider<RemoteInputQuickSettingsDisabler> remoteInputQuickSettingsDisablerProvider;
    private Provider<RemoteInputUriController> remoteInputUriControllerProvider;
    private Provider<RingerModeTrackerImpl> ringerModeTrackerImplProvider;
    private Provider<RotationLockControllerImpl> rotationLockControllerImplProvider;
    private RotationLockTile_Factory rotationLockTileProvider;
    private RowContentBindStageLogger_Factory rowContentBindStageLoggerProvider;
    private Provider<RowContentBindStage> rowContentBindStageProvider;
    private ScreenButtonTile_Factory screenButtonTileProvider;
    private Provider<ScreenDecorations> screenDecorationsProvider;
    private Provider<ScreenLifecycle> screenLifecycleProvider;
    private ScreenLockTile_Factory screenLockTileProvider;
    private ScreenPinningRequest_Factory screenPinningRequestProvider;
    private ScreenRecordDialog_Factory screenRecordDialogProvider;
    private ScreenRecordTile_Factory screenRecordTileProvider;
    private ScreenShotTile_Factory screenShotTileProvider;
    private ScreenshotNotificationsController_Factory screenshotNotificationsControllerProvider;
    private Provider<ScrimController> scrimControllerProvider;
    private Provider<SecurityControllerImpl> securityControllerImplProvider;
    private SeekBarViewModel_Factory seekBarViewModelProvider;
    private Provider<SensorPrivacyControllerImpl> sensorPrivacyControllerImplProvider;
    private Provider<GarbageMonitor.Service> serviceProvider;
    private Provider<SettingsManager> settingsManagerProvider;
    private Provider<SettingsObserverImpl> settingsObserverImplProvider;
    private Provider<ShadeControllerImpl> shadeControllerImplProvider;
    private ShadeListBuilderLogger_Factory shadeListBuilderLoggerProvider;
    private Provider<ShadeListBuilder> shadeListBuilderProvider;
    private SharedCoordinatorLogger_Factory sharedCoordinatorLoggerProvider;
    private Provider<ShortcutKeyDispatcher> shortcutKeyDispatcherProvider;
    private Provider<SizeCompatModeActivityController> sizeCompatModeActivityControllerProvider;
    private Provider<SlaveWifiHelper> slaveWifiHelperProvider;
    private Provider<SlaveWifiSignalController> slaveWifiSignalControllerProvider;
    private Provider<SliceBroadcastRelayHandler> sliceBroadcastRelayHandlerProvider;
    private Provider<SmartDarkObserver> smartDarkObserverProvider;
    private Provider<SmartReplyConstants> smartReplyConstantsProvider;
    private Provider<StatusBarComponent.Builder> statusBarComponentBuilderProvider;
    private Provider<StatusBarIconControllerImpl> statusBarIconControllerImplProvider;
    private Provider<StatusBarKeyguardViewManager> statusBarKeyguardViewManagerProvider;
    private StatusBarNotificationActivityStarterLogger_Factory statusBarNotificationActivityStarterLoggerProvider;
    private Provider<StatusBarRemoteInputCallback> statusBarRemoteInputCallbackProvider;
    private Provider<StatusBarStateControllerImpl> statusBarStateControllerImplProvider;
    private Provider<StatusBarTouchableRegionManager> statusBarTouchableRegionManagerProvider;
    private Provider<StatusBarWindowController> statusBarWindowControllerProvider;
    private Provider<SuperSaveModeController> superSaveModeControllerProvider;
    private Provider<SuperStatusBarViewFactory> superStatusBarViewFactoryProvider;
    private SyncTile_Factory syncTileProvider;
    private Provider<SystemActions> systemActionsProvider;
    private SystemUIAuxiliaryDumpService_Factory systemUIAuxiliaryDumpServiceProvider;
    private Provider<SystemUIRootComponent> systemUIRootComponentProvider;
    private SystemUIService_Factory systemUIServiceProvider;
    private Provider<SystemUIStat> systemUIStatProvider;
    private Provider<SystemWindows> systemWindowsProvider;
    private Provider<SysuiColorExtractor> sysuiColorExtractorProvider;
    private TakeScreenshotService_Factory takeScreenshotServiceProvider;
    private Provider<TargetSdkResolver> targetSdkResolverProvider;
    private Provider<ThemeOverlayController> themeOverlayControllerProvider;
    private Provider<ToastUI> toastUIProvider;
    private Provider<ToggleManagerController> toggleManagerControllerProvider;
    private Provider<TransactionPool> transactionPoolProvider;
    private Provider<TunablePadding.TunablePaddingService> tunablePaddingServiceProvider;
    private Provider<TunerServiceImpl> tunerServiceImplProvider;
    private Provider<TvStatusBar> tvStatusBarProvider;
    private UiModeNightTile_Factory uiModeNightTileProvider;
    private Provider<UiOffloadThread> uiOffloadThreadProvider;
    private Provider<UnimportantSdk> unimportantSdkProvider;
    private UsbDebuggingSecondaryUserActivity_Factory usbDebuggingSecondaryUserActivityProvider;
    private Provider<UsbNotificationController> usbNotificationControllerProvider;
    private Provider<UserInfoControllerImpl> userInfoControllerImplProvider;
    private Provider<UserSwitcherController> userSwitcherControllerProvider;
    private UserTile_Factory userTileProvider;
    private VibrateTile_Factory vibrateTileProvider;
    private Provider<VibratorHelper> vibratorHelperProvider;
    private Provider<VolumeDialogComponent> volumeDialogComponentProvider;
    private Provider<VolumeDialogControllerImpl> volumeDialogControllerImplProvider;
    private Provider<VolumeUI> volumeUIProvider;
    private Provider<WakefulnessLifecycle> wakefulnessLifecycleProvider;
    private Provider<WallpaperCommandSender> wallpaperCommandSenderProvider;
    private WifiTile_Factory wifiTileProvider;
    private Provider<WindowMagnification> windowMagnificationProvider;
    private WorkLockActivity_Factory workLockActivityProvider;
    private WorkModeTile_Factory workModeTileProvider;
    private Provider<ZenModeControllerImpl> zenModeControllerImplProvider;
    private Provider<ZenModeViewController> zenModeViewControllerProvider;

    @Override // com.android.systemui.dagger.SystemUIRootComponent
    public void inject(ContentProvider contentProvider) {
    }

    private DaggerSystemUIRootComponent(Builder builder) {
        initialize(builder);
        initialize2(builder);
        initialize3(builder);
        initialize4(builder);
        initialize5(builder);
        initialize6(builder);
    }

    public static Builder builder() {
        return new Builder();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Handler getMainHandler() {
        return ConcurrencyModule_ProvideMainHandlerFactory.proxyProvideMainHandler(ConcurrencyModule_ProvideMainLooperFactory.proxyProvideMainLooper());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Handler getBackgroundHandler() {
        return ConcurrencyModule_ProvideBgHandlerFactory.proxyProvideBgHandler(this.provideBgLooperProvider.get());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Executor getMainExecutor() {
        return ConcurrencyModule_ProvideMainExecutorFactory.proxyProvideMainExecutor(SystemUIFactory_ContextHolder_ProvideContextFactory.proxyProvideContext(this.contextHolder));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private MiuiNotificationSectionsFeatureManager getMiuiNotificationSectionsFeatureManager() {
        return new MiuiNotificationSectionsFeatureManager(new DeviceConfigProxy(), SystemUIFactory_ContextHolder_ProvideContextFactory.proxyProvideContext(this.contextHolder));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Resources getMainResources() {
        return SystemServicesModule_ProvideResourcesFactory.proxyProvideResources(SystemUIFactory_ContextHolder_ProvideContextFactory.proxyProvideContext(this.contextHolder));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private QSLogger getQSLogger() {
        return new QSLogger(this.provideQuickSettingsLogBufferProvider.get());
    }

    private MediaControlLogger getMediaControlLogger() {
        return new MediaControlLogger(this.provideMediaControlLogBufferProvider.get());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private MediaHost getMediaHost() {
        return new MediaHost(new MediaHost.MediaHostStateHolder(), this.mediaHierarchyManagerProvider.get(), this.mediaDataFilterProvider.get(), this.mediaHostStatesManagerProvider.get(), getMediaControlLogger());
    }

    private void initialize(Builder builder) {
        Provider<DumpManager> provider = DoubleCheck.provider(DumpManager_Factory.create());
        this.dumpManagerProvider = provider;
        this.bootCompleteCacheImplProvider = DoubleCheck.provider(BootCompleteCacheImpl_Factory.create(provider));
        this.provideContextProvider = SystemUIFactory_ContextHolder_ProvideContextFactory.create(builder.contextHolder);
        this.provideConfigurationControllerProvider = DoubleCheck.provider(DependencyProvider_ProvideConfigurationControllerFactory.create(builder.dependencyProvider, this.provideContextProvider));
        Provider<Looper> provider2 = DoubleCheck.provider(ConcurrencyModule_ProvideBgLooperFactory.create());
        this.provideBgLooperProvider = provider2;
        this.provideBackgroundExecutorProvider = DoubleCheck.provider(ConcurrencyModule_ProvideBackgroundExecutorFactory.create(provider2));
        Provider<ContentResolver> provider3 = DoubleCheck.provider(SystemServicesModule_ProvideContentResolverFactory.create(this.provideContextProvider));
        this.provideContentResolverProvider = provider3;
        Provider<LogcatEchoTracker> provider4 = DoubleCheck.provider(LogModule_ProvideLogcatEchoTrackerFactory.create(provider3, ConcurrencyModule_ProvideMainLooperFactory.create()));
        this.provideLogcatEchoTrackerProvider = provider4;
        Provider<LogBuffer> provider5 = DoubleCheck.provider(LogModule_ProvideBroadcastDispatcherLogBufferFactory.create(provider4, this.dumpManagerProvider));
        this.provideBroadcastDispatcherLogBufferProvider = provider5;
        this.broadcastDispatcherLoggerProvider = BroadcastDispatcherLogger_Factory.create(provider5);
        Provider<BroadcastDispatcher> provider6 = DoubleCheck.provider(DependencyProvider_ProvidesBroadcastDispatcherFactory.create(builder.dependencyProvider, this.provideContextProvider, this.provideBgLooperProvider, this.provideBackgroundExecutorProvider, this.dumpManagerProvider, this.broadcastDispatcherLoggerProvider));
        this.providesBroadcastDispatcherProvider = provider6;
        this.workLockActivityProvider = WorkLockActivity_Factory.create(provider6);
        Provider<MiuiBrightnessController> provider7 = DoubleCheck.provider(MiuiBrightnessController_Factory.create(this.provideContextProvider, this.providesBroadcastDispatcherProvider));
        this.miuiBrightnessControllerProvider = provider7;
        this.brightnessDialogProvider = BrightnessDialog_Factory.create(this.providesBroadcastDispatcherProvider, provider7);
        this.recordingControllerProvider = DoubleCheck.provider(RecordingController_Factory.create(this.providesBroadcastDispatcherProvider));
        Provider<CurrentUserContextTracker> provider8 = DoubleCheck.provider(SettingsModule_ProvideCurrentUserContextTrackerFactory.create(this.provideContextProvider, this.providesBroadcastDispatcherProvider));
        this.provideCurrentUserContextTrackerProvider = provider8;
        this.screenRecordDialogProvider = ScreenRecordDialog_Factory.create(this.recordingControllerProvider, provider8);
        this.provideWindowManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideWindowManagerFactory.create(this.provideContextProvider));
        this.provideIActivityManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideIActivityManagerFactory.create());
        this.provideResourcesProvider = SystemServicesModule_ProvideResourcesFactory.create(this.provideContextProvider);
        this.provideAmbientDisplayConfigurationProvider = DependencyProvider_ProvideAmbientDisplayConfigurationFactory.create(builder.dependencyProvider, this.provideContextProvider);
        this.provideAlwaysOnDisplayPolicyProvider = DependencyProvider_ProvideAlwaysOnDisplayPolicyFactory.create(builder.dependencyProvider, this.provideContextProvider);
        this.providePowerManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvidePowerManagerFactory.create(this.provideContextProvider));
        this.provideMainHandlerProvider = ConcurrencyModule_ProvideMainHandlerFactory.create(ConcurrencyModule_ProvideMainLooperFactory.create());
        Provider<LeakDetector> provider9 = DoubleCheck.provider(DependencyProvider_ProvideLeakDetectorFactory.create(builder.dependencyProvider));
        this.provideLeakDetectorProvider = provider9;
        Provider<TunerServiceImpl> provider10 = DoubleCheck.provider(TunerServiceImpl_Factory.create(this.provideContextProvider, this.provideMainHandlerProvider, provider9, this.providesBroadcastDispatcherProvider));
        this.tunerServiceImplProvider = provider10;
        this.dozeParametersProvider = DoubleCheck.provider(DozeParameters_Factory.create(this.provideResourcesProvider, this.provideAmbientDisplayConfigurationProvider, this.provideAlwaysOnDisplayPolicyProvider, this.providePowerManagerProvider, provider10));
        Provider<UiEventLogger> provider11 = DoubleCheck.provider(DependencyProvider_ProvideUiEventLoggerFactory.create());
        this.provideUiEventLoggerProvider = provider11;
        this.statusBarStateControllerImplProvider = DoubleCheck.provider(StatusBarStateControllerImpl_Factory.create(provider11));
        this.providePluginManagerProvider = DoubleCheck.provider(DependencyProvider_ProvidePluginManagerFactory.create(builder.dependencyProvider, this.provideContextProvider));
        this.provideMainExecutorProvider = ConcurrencyModule_ProvideMainExecutorFactory.create(this.provideContextProvider);
        this.provideDisplayMetricsProvider = DoubleCheck.provider(DependencyProvider_ProvideDisplayMetricsFactory.create(builder.dependencyProvider, this.provideContextProvider, this.provideWindowManagerProvider));
        Provider<AsyncSensorManager> provider12 = DoubleCheck.provider(AsyncSensorManager_Factory.create(this.provideContextProvider, this.providePluginManagerProvider));
        this.asyncSensorManagerProvider = provider12;
        this.proximitySensorProvider = ProximitySensor_Factory.create(this.provideResourcesProvider, provider12);
        this.dockManagerImplProvider = DoubleCheck.provider(DockManagerImpl_Factory.create());
        Provider<AudioManager> provider13 = DoubleCheck.provider(SystemServicesModule_ProvideAudioManagerFactory.create(this.provideContextProvider));
        this.provideAudioManagerProvider = provider13;
        this.ringerModeTrackerImplProvider = DoubleCheck.provider(RingerModeTrackerImpl_Factory.create(provider13, this.providesBroadcastDispatcherProvider, this.provideBackgroundExecutorProvider));
        this.provideLockPatternUtilsProvider = DependencyProvider_ProvideLockPatternUtilsFactory.create(builder.dependencyProvider, this.provideContextProvider);
        this.keyguardUpdateMonitorProvider = DoubleCheck.provider(KeyguardUpdateMonitor_Factory.create(this.provideContextProvider, ConcurrencyModule_ProvideMainLooperFactory.create(), this.providesBroadcastDispatcherProvider, this.dumpManagerProvider, this.ringerModeTrackerImplProvider, this.provideBackgroundExecutorProvider, this.statusBarStateControllerImplProvider, this.provideLockPatternUtilsProvider));
        this.provideUiBackgroundExecutorProvider = DoubleCheck.provider(ConcurrencyModule_ProvideUiBackgroundExecutorFactory.create());
        this.falsingManagerProxyProvider = DoubleCheck.provider(FalsingManagerProxy_Factory.create(this.provideContextProvider, this.providePluginManagerProvider, this.provideMainExecutorProvider, this.provideDisplayMetricsProvider, this.proximitySensorProvider, DeviceConfigProxy_Factory.create(), this.dockManagerImplProvider, this.keyguardUpdateMonitorProvider, this.dumpManagerProvider, this.provideUiBackgroundExecutorProvider, this.statusBarStateControllerImplProvider));
        this.newKeyguardViewMediatorProvider = new DelegateFactory();
        this.providesViewMediatorCallbackProvider = DependencyProvider_ProvidesViewMediatorCallbackFactory.create(builder.dependencyProvider, this.newKeyguardViewMediatorProvider);
        Provider<DeviceProvisionedControllerImpl> provider14 = DoubleCheck.provider(DeviceProvisionedControllerImpl_Factory.create(this.provideContextProvider, this.provideMainHandlerProvider, this.providesBroadcastDispatcherProvider));
        this.deviceProvisionedControllerImplProvider = provider14;
        this.navigationModeControllerProvider = DoubleCheck.provider(NavigationModeController_Factory.create(this.provideContextProvider, provider14, this.provideConfigurationControllerProvider, this.provideUiBackgroundExecutorProvider));
        this.notificationShadeWindowControllerProvider = new DelegateFactory();
        this.keyguardStateControllerImplProvider = DoubleCheck.provider(KeyguardStateControllerImpl_Factory.create(this.provideContextProvider, this.keyguardUpdateMonitorProvider, this.provideLockPatternUtilsProvider));
        this.featureFlagsProvider = DoubleCheck.provider(FeatureFlags_Factory.create(this.provideBackgroundExecutorProvider));
        Provider<NotificationManager> provider15 = DoubleCheck.provider(SystemServicesModule_ProvideNotificationManagerFactory.create(this.provideContextProvider));
        this.provideNotificationManagerProvider = provider15;
        this.provideNotificationListenerProvider = DoubleCheck.provider(StatusBarDependenciesModule_ProvideNotificationListenerFactory.create(this.provideContextProvider, provider15, this.provideMainHandlerProvider));
        Provider<LogBuffer> provider16 = DoubleCheck.provider(LogModule_ProvideNotificationsLogBufferFactory.create(this.provideLogcatEchoTrackerProvider, this.dumpManagerProvider));
        this.provideNotificationsLogBufferProvider = provider16;
        this.notificationEntryManagerLoggerProvider = NotificationEntryManagerLogger_Factory.create(provider16);
        Provider<ExtensionControllerImpl> provider17 = DoubleCheck.provider(ExtensionControllerImpl_Factory.create(this.provideContextProvider, this.provideLeakDetectorProvider, this.providePluginManagerProvider, this.tunerServiceImplProvider, this.provideConfigurationControllerProvider));
        this.extensionControllerImplProvider = provider17;
        this.notificationPersonExtractorPluginBoundaryProvider = DoubleCheck.provider(NotificationPersonExtractorPluginBoundary_Factory.create(provider17));
        DelegateFactory delegateFactory = new DelegateFactory();
        this.notificationGroupManagerProvider = delegateFactory;
        Provider<PeopleNotificationIdentifierImpl> provider18 = DoubleCheck.provider(PeopleNotificationIdentifierImpl_Factory.create(this.notificationPersonExtractorPluginBoundaryProvider, delegateFactory));
        this.peopleNotificationIdentifierImplProvider = provider18;
        Provider<NotificationGroupManager> provider19 = DoubleCheck.provider(NotificationGroupManager_Factory.create(this.statusBarStateControllerImplProvider, provider18));
        this.notificationGroupManagerProvider = provider19;
        ((DelegateFactory) this.notificationGroupManagerProvider).setDelegatedProvider(provider19);
        this.provideNotificationMediaManagerProvider = new DelegateFactory();
        this.provideDevicePolicyManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideDevicePolicyManagerFactory.create(this.provideContextProvider));
        this.provideUserManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideUserManagerFactory.create(this.provideContextProvider));
        Provider<IStatusBarService> provider20 = DoubleCheck.provider(SystemServicesModule_ProvideIStatusBarServiceFactory.create());
        this.provideIStatusBarServiceProvider = provider20;
        this.notificationClickNotifierProvider = DoubleCheck.provider(NotificationClickNotifier_Factory.create(provider20, this.provideMainExecutorProvider));
        Provider<KeyguardManager> provider21 = DoubleCheck.provider(SystemServicesModule_ProvideKeyguardManagerFactory.create(this.provideContextProvider));
        this.provideKeyguardManagerProvider = provider21;
        Provider<NotificationLockscreenUserManagerImpl> provider22 = DoubleCheck.provider(NotificationLockscreenUserManagerImpl_Factory.create(this.provideContextProvider, this.providesBroadcastDispatcherProvider, this.provideDevicePolicyManagerProvider, this.provideUserManagerProvider, this.notificationClickNotifierProvider, provider21, this.statusBarStateControllerImplProvider, this.provideMainHandlerProvider, this.deviceProvisionedControllerImplProvider, this.keyguardStateControllerImplProvider));
        this.notificationLockscreenUserManagerImplProvider = provider22;
        Provider<KeyguardBypassController> provider23 = DoubleCheck.provider(KeyguardBypassController_Factory.create(this.provideContextProvider, this.tunerServiceImplProvider, this.statusBarStateControllerImplProvider, provider22, this.keyguardStateControllerImplProvider, this.dumpManagerProvider));
        this.keyguardBypassControllerProvider = provider23;
        this.provideHeadsUpManagerPhoneProvider = DoubleCheck.provider(SystemUIDefaultModule_ProvideHeadsUpManagerPhoneFactory.create(this.provideContextProvider, this.statusBarStateControllerImplProvider, provider23, this.notificationGroupManagerProvider, this.provideConfigurationControllerProvider));
        MediaFeatureFlag_Factory create = MediaFeatureFlag_Factory.create(this.provideContextProvider);
        this.mediaFeatureFlagProvider = create;
        this.notificationFilterProvider = DoubleCheck.provider(NotificationFilter_Factory.create(this.statusBarStateControllerImplProvider, create));
        this.miuiNotificationSectionsFeatureManagerProvider = MiuiNotificationSectionsFeatureManager_Factory.create(DeviceConfigProxy_Factory.create(), this.provideContextProvider);
        Provider<HighPriorityProvider> provider24 = DoubleCheck.provider(HighPriorityProvider_Factory.create(this.peopleNotificationIdentifierImplProvider, this.notificationGroupManagerProvider));
        this.highPriorityProvider = provider24;
        this.notificationRankingManagerProvider = NotificationRankingManager_Factory.create(this.provideNotificationMediaManagerProvider, this.notificationGroupManagerProvider, this.provideHeadsUpManagerPhoneProvider, this.notificationFilterProvider, this.notificationEntryManagerLoggerProvider, this.miuiNotificationSectionsFeatureManagerProvider, this.peopleNotificationIdentifierImplProvider, provider24);
        this.keyguardEnvironmentImplProvider = DoubleCheck.provider(KeyguardEnvironmentImpl_Factory.create());
        this.provideNotificationMessagingUtilProvider = DependencyProvider_ProvideNotificationMessagingUtilFactory.create(builder.dependencyProvider, this.provideContextProvider);
        DelegateFactory delegateFactory2 = new DelegateFactory();
        this.provideNotificationEntryManagerProvider = delegateFactory2;
        this.provideSmartReplyControllerProvider = DoubleCheck.provider(StatusBarDependenciesModule_ProvideSmartReplyControllerFactory.create(delegateFactory2, this.provideIStatusBarServiceProvider, this.notificationClickNotifierProvider));
        this.provideStatusBarProvider = new DelegateFactory();
        this.remoteInputUriControllerProvider = DoubleCheck.provider(RemoteInputUriController_Factory.create(this.provideIStatusBarServiceProvider));
        Provider<LogBuffer> provider25 = DoubleCheck.provider(LogModule_ProvideNotifInteractionLogBufferFactory.create(this.provideLogcatEchoTrackerProvider, this.dumpManagerProvider));
        this.provideNotifInteractionLogBufferProvider = provider25;
        this.actionClickLoggerProvider = ActionClickLogger_Factory.create(provider25);
        this.provideNotificationRemoteInputManagerProvider = DoubleCheck.provider(StatusBarDependenciesModule_ProvideNotificationRemoteInputManagerFactory.create(this.provideContextProvider, this.notificationLockscreenUserManagerImplProvider, this.provideSmartReplyControllerProvider, this.provideNotificationEntryManagerProvider, this.provideStatusBarProvider, this.statusBarStateControllerImplProvider, ConcurrencyModule_ProvideHandlerFactory.create(), this.remoteInputUriControllerProvider, this.notificationClickNotifierProvider, this.actionClickLoggerProvider));
        this.bindSystemClockProvider = DoubleCheck.provider(SystemClockImpl_Factory.create());
        this.notifCollectionLoggerProvider = NotifCollectionLogger_Factory.create(this.provideNotificationsLogBufferProvider);
        Provider<Files> provider26 = DoubleCheck.provider(Files_Factory.create());
        this.filesProvider = provider26;
        Provider<LogBufferEulogizer> provider27 = DoubleCheck.provider(LogBufferEulogizer_Factory.create(this.provideContextProvider, this.dumpManagerProvider, this.bindSystemClockProvider, provider26));
        this.logBufferEulogizerProvider = provider27;
        this.notifCollectionProvider = DoubleCheck.provider(NotifCollection_Factory.create(this.provideIStatusBarServiceProvider, this.bindSystemClockProvider, this.featureFlagsProvider, this.notifCollectionLoggerProvider, provider27, this.dumpManagerProvider));
        this.shadeListBuilderLoggerProvider = ShadeListBuilderLogger_Factory.create(this.provideNotificationsLogBufferProvider);
        Provider<NotificationInteractionTracker> provider28 = DoubleCheck.provider(NotificationInteractionTracker_Factory.create(this.notificationClickNotifierProvider, this.provideNotificationEntryManagerProvider));
        this.notificationInteractionTrackerProvider = provider28;
        Provider<ShadeListBuilder> provider29 = DoubleCheck.provider(ShadeListBuilder_Factory.create(this.bindSystemClockProvider, this.shadeListBuilderLoggerProvider, this.dumpManagerProvider, provider28));
        this.shadeListBuilderProvider = provider29;
        Provider<NotifPipeline> provider30 = DoubleCheck.provider(NotifPipeline_Factory.create(this.notifCollectionProvider, provider29));
        this.notifPipelineProvider = provider30;
        this.provideCommonNotifCollectionProvider = DoubleCheck.provider(NotificationsModule_ProvideCommonNotifCollectionFactory.create(this.featureFlagsProvider, provider30, this.provideNotificationEntryManagerProvider));
        NotifBindPipelineLogger_Factory create2 = NotifBindPipelineLogger_Factory.create(this.provideNotificationsLogBufferProvider);
        this.notifBindPipelineLoggerProvider = create2;
        this.notifBindPipelineProvider = DoubleCheck.provider(NotifBindPipeline_Factory.create(this.provideCommonNotifCollectionProvider, create2, ConcurrencyModule_ProvideMainLooperFactory.create()));
        NotifRemoteViewCacheImpl_Factory create3 = NotifRemoteViewCacheImpl_Factory.create(this.provideCommonNotifCollectionProvider);
        this.notifRemoteViewCacheImplProvider = create3;
        this.provideNotifRemoteViewCacheProvider = DoubleCheck.provider(create3);
        this.smartReplyConstantsProvider = DoubleCheck.provider(SmartReplyConstants_Factory.create(this.provideMainHandlerProvider, this.provideContextProvider, DeviceConfigProxy_Factory.create()));
        this.provideLauncherAppsProvider = DoubleCheck.provider(SystemServicesModule_ProvideLauncherAppsFactory.create(this.provideContextProvider));
        Provider<ConversationNotificationManager> provider31 = DoubleCheck.provider(ConversationNotificationManager_Factory.create(this.provideNotificationEntryManagerProvider, this.notificationGroupManagerProvider, this.provideContextProvider, this.provideMainHandlerProvider));
        this.conversationNotificationManagerProvider = provider31;
        ConversationNotificationProcessor_Factory create4 = ConversationNotificationProcessor_Factory.create(this.provideLauncherAppsProvider, provider31);
        this.conversationNotificationProcessorProvider = create4;
        this.notificationContentInflaterProvider = DoubleCheck.provider(NotificationContentInflater_Factory.create(this.provideNotifRemoteViewCacheProvider, this.provideNotificationRemoteInputManagerProvider, this.smartReplyConstantsProvider, this.provideSmartReplyControllerProvider, create4, this.provideBackgroundExecutorProvider));
        this.notifInflationErrorManagerProvider = DoubleCheck.provider(NotifInflationErrorManager_Factory.create());
    }

    private void initialize2(Builder builder) {
        RowContentBindStageLogger_Factory create = RowContentBindStageLogger_Factory.create(this.provideNotificationsLogBufferProvider);
        this.rowContentBindStageLoggerProvider = create;
        this.rowContentBindStageProvider = DoubleCheck.provider(RowContentBindStage_Factory.create(this.notificationContentInflaterProvider, this.notifInflationErrorManagerProvider, create));
        this.provideIDreamManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideIDreamManagerFactory.create());
        this.enhancedEstimatesImplProvider = DoubleCheck.provider(EnhancedEstimatesImpl_Factory.create());
        ConcurrencyModule_ProvideBgHandlerFactory create2 = ConcurrencyModule_ProvideBgHandlerFactory.create(this.provideBgLooperProvider);
        this.provideBgHandlerProvider = create2;
        this.provideBatteryControllerProvider = DoubleCheck.provider(SystemUIDefaultModule_ProvideBatteryControllerFactory.create(this.provideContextProvider, this.enhancedEstimatesImplProvider, this.providePowerManagerProvider, this.providesBroadcastDispatcherProvider, this.provideMainHandlerProvider, create2));
        this.zenModeControllerImplProvider = DoubleCheck.provider(ZenModeControllerImpl_Factory.create(this.provideContextProvider, this.provideMainHandlerProvider, this.providesBroadcastDispatcherProvider));
        this.settingsManagerProvider = DoubleCheck.provider(SettingsManager_Factory.create(this.provideContextProvider, this.providesBroadcastDispatcherProvider));
        Provider<ProtoTracer> provider = DoubleCheck.provider(ProtoTracer_Factory.create(this.provideContextProvider, this.dumpManagerProvider));
        this.protoTracerProvider = provider;
        this.provideCommandQueueProvider = DoubleCheck.provider(StatusBarDependenciesModule_ProvideCommandQueueFactory.create(this.provideContextProvider, provider));
        DelegateFactory delegateFactory = new DelegateFactory();
        this.statusBarKeyguardViewManagerProvider = delegateFactory;
        this.miuiNotificationInterruptStateProviderImplProvider = DoubleCheck.provider(MiuiNotificationInterruptStateProviderImpl_Factory.create(this.provideContextProvider, this.provideContentResolverProvider, this.providePowerManagerProvider, this.provideIDreamManagerProvider, this.provideAmbientDisplayConfigurationProvider, this.notificationFilterProvider, this.provideBatteryControllerProvider, this.statusBarStateControllerImplProvider, this.provideHeadsUpManagerPhoneProvider, this.provideMainHandlerProvider, this.zenModeControllerImplProvider, this.settingsManagerProvider, this.provideCommandQueueProvider, delegateFactory, this.deviceProvisionedControllerImplProvider));
        this.expandableNotificationRowComponentBuilderProvider = new Provider<ExpandableNotificationRowComponent.Builder>() {
            /* class com.android.systemui.dagger.DaggerSystemUIRootComponent.AnonymousClass1 */

            @Override // javax.inject.Provider
            public ExpandableNotificationRowComponent.Builder get() {
                return new ExpandableNotificationRowComponentBuilder();
            }
        };
        IconBuilder_Factory create3 = IconBuilder_Factory.create(this.provideContextProvider);
        this.iconBuilderProvider = create3;
        this.miuiIconManagerProvider = MiuiIconManager_Factory.create(this.provideCommonNotifCollectionProvider, this.provideLauncherAppsProvider, create3);
        this.lowPriorityInflationHelperProvider = DoubleCheck.provider(LowPriorityInflationHelper_Factory.create(this.featureFlagsProvider, this.notificationGroupManagerProvider, this.rowContentBindStageProvider));
        this.notificationRowBinderImplProvider = DoubleCheck.provider(NotificationRowBinderImpl_Factory.create(this.provideContextProvider, this.provideNotificationMessagingUtilProvider, this.provideNotificationRemoteInputManagerProvider, this.notificationLockscreenUserManagerImplProvider, this.notifBindPipelineProvider, this.rowContentBindStageProvider, this.miuiNotificationInterruptStateProviderImplProvider, RowInflaterTask_Factory.create(), this.expandableNotificationRowComponentBuilderProvider, this.miuiIconManagerProvider, this.lowPriorityInflationHelperProvider));
        Provider<ForegroundServiceDismissalFeatureController> provider2 = DoubleCheck.provider(ForegroundServiceDismissalFeatureController_Factory.create(DeviceConfigProxy_Factory.create(), this.provideContextProvider));
        this.foregroundServiceDismissalFeatureControllerProvider = provider2;
        Provider<NotificationEntryManager> provider3 = DoubleCheck.provider(NotificationsModule_ProvideNotificationEntryManagerFactory.create(this.provideContextProvider, this.notificationEntryManagerLoggerProvider, this.notificationGroupManagerProvider, this.notificationRankingManagerProvider, this.keyguardEnvironmentImplProvider, this.featureFlagsProvider, this.notificationRowBinderImplProvider, this.provideNotificationRemoteInputManagerProvider, this.provideLeakDetectorProvider, provider2));
        this.provideNotificationEntryManagerProvider = provider3;
        ((DelegateFactory) this.provideNotificationEntryManagerProvider).setDelegatedProvider(provider3);
        this.targetSdkResolverProvider = DoubleCheck.provider(TargetSdkResolver_Factory.create(this.provideContextProvider));
        this.provideMainDelayableExecutorProvider = DoubleCheck.provider(ConcurrencyModule_ProvideMainDelayableExecutorFactory.create(ConcurrencyModule_ProvideMainLooperFactory.create()));
        GroupCoalescerLogger_Factory create4 = GroupCoalescerLogger_Factory.create(this.provideNotificationsLogBufferProvider);
        this.groupCoalescerLoggerProvider = create4;
        this.groupCoalescerProvider = GroupCoalescer_Factory.create(this.provideMainDelayableExecutorProvider, this.bindSystemClockProvider, create4);
        SharedCoordinatorLogger_Factory create5 = SharedCoordinatorLogger_Factory.create(this.provideNotificationsLogBufferProvider);
        this.sharedCoordinatorLoggerProvider = create5;
        this.hideNotifsForOtherUsersCoordinatorProvider = HideNotifsForOtherUsersCoordinator_Factory.create(this.notificationLockscreenUserManagerImplProvider, create5);
        this.keyguardCoordinatorProvider = DoubleCheck.provider(KeyguardCoordinator_Factory.create(this.provideContextProvider, ConcurrencyModule_ProvideHandlerFactory.create(), this.keyguardStateControllerImplProvider, this.notificationLockscreenUserManagerImplProvider, this.providesBroadcastDispatcherProvider, this.statusBarStateControllerImplProvider, this.keyguardUpdateMonitorProvider, this.highPriorityProvider));
        this.rankingCoordinatorProvider = DoubleCheck.provider(RankingCoordinator_Factory.create(this.statusBarStateControllerImplProvider));
        Provider<AppOpsControllerImpl> provider4 = DoubleCheck.provider(AppOpsControllerImpl_Factory.create(this.provideContextProvider, this.provideBgLooperProvider, this.dumpManagerProvider));
        this.appOpsControllerImplProvider = provider4;
        Provider<ForegroundServiceController> provider5 = DoubleCheck.provider(ForegroundServiceController_Factory.create(this.provideNotificationEntryManagerProvider, provider4, this.provideMainHandlerProvider));
        this.foregroundServiceControllerProvider = provider5;
        this.appOpsCoordinatorProvider = DoubleCheck.provider(AppOpsCoordinator_Factory.create(provider5, this.appOpsControllerImplProvider, this.provideMainDelayableExecutorProvider));
        Provider<IPackageManager> provider6 = DoubleCheck.provider(SystemServicesModule_ProvideIPackageManagerFactory.create());
        this.provideIPackageManagerProvider = provider6;
        this.deviceProvisionedCoordinatorProvider = DoubleCheck.provider(DeviceProvisionedCoordinator_Factory.create(this.deviceProvisionedControllerImplProvider, provider6));
        DelegateFactory delegateFactory2 = new DelegateFactory();
        this.newBubbleControllerProvider = delegateFactory2;
        this.bubbleCoordinatorProvider = DoubleCheck.provider(BubbleCoordinator_Factory.create(delegateFactory2, this.notifCollectionProvider));
        Provider<HeadsUpViewBinder> provider7 = DoubleCheck.provider(HeadsUpViewBinder_Factory.create(this.provideNotificationMessagingUtilProvider, this.rowContentBindStageProvider));
        this.headsUpViewBinderProvider = provider7;
        this.headsUpCoordinatorProvider = DoubleCheck.provider(HeadsUpCoordinator_Factory.create(this.provideHeadsUpManagerPhoneProvider, provider7, this.miuiNotificationInterruptStateProviderImplProvider, this.provideNotificationRemoteInputManagerProvider));
        this.conversationCoordinatorProvider = DoubleCheck.provider(ConversationCoordinator_Factory.create());
        this.preparationCoordinatorLoggerProvider = PreparationCoordinatorLogger_Factory.create(this.provideNotificationsLogBufferProvider);
        this.notifInflaterImplProvider = DoubleCheck.provider(NotifInflaterImpl_Factory.create(this.provideIStatusBarServiceProvider, this.notifCollectionProvider, this.notifInflationErrorManagerProvider, this.notifPipelineProvider));
        Provider<NotifViewBarn> provider8 = DoubleCheck.provider(NotifViewBarn_Factory.create());
        this.notifViewBarnProvider = provider8;
        this.preparationCoordinatorProvider = DoubleCheck.provider(PreparationCoordinator_Factory.create(this.preparationCoordinatorLoggerProvider, this.notifInflaterImplProvider, this.notifInflationErrorManagerProvider, provider8, this.provideIStatusBarServiceProvider));
        MediaCoordinator_Factory create6 = MediaCoordinator_Factory.create(this.mediaFeatureFlagProvider);
        this.mediaCoordinatorProvider = create6;
        this.notifCoordinatorsProvider = DoubleCheck.provider(NotifCoordinators_Factory.create(this.dumpManagerProvider, this.featureFlagsProvider, this.hideNotifsForOtherUsersCoordinatorProvider, this.keyguardCoordinatorProvider, this.rankingCoordinatorProvider, this.appOpsCoordinatorProvider, this.deviceProvisionedCoordinatorProvider, this.bubbleCoordinatorProvider, this.headsUpCoordinatorProvider, this.conversationCoordinatorProvider, this.preparationCoordinatorProvider, create6));
        Provider<VisualStabilityManager> provider9 = DoubleCheck.provider(NotificationsModule_ProvideVisualStabilityManagerFactory.create(this.provideNotificationEntryManagerProvider, ConcurrencyModule_ProvideHandlerFactory.create()));
        this.provideVisualStabilityManagerProvider = provider9;
        Provider<NotifViewManager> provider10 = DoubleCheck.provider(NotifViewManager_Factory.create(this.notifViewBarnProvider, provider9, this.featureFlagsProvider));
        this.notifViewManagerProvider = provider10;
        this.notifPipelineInitializerProvider = DoubleCheck.provider(NotifPipelineInitializer_Factory.create(this.notifPipelineProvider, this.groupCoalescerProvider, this.notifCollectionProvider, this.shadeListBuilderProvider, this.notifCoordinatorsProvider, this.notifInflaterImplProvider, this.dumpManagerProvider, this.featureFlagsProvider, provider10));
        this.notifBindPipelineInitializerProvider = NotifBindPipelineInitializer_Factory.create(this.notifBindPipelineProvider, this.rowContentBindStageProvider);
        this.provideNotificationGroupAlertTransferHelperProvider = DoubleCheck.provider(StatusBarPhoneDependenciesModule_ProvideNotificationGroupAlertTransferHelperFactory.create(this.rowContentBindStageProvider));
        this.headsUpControllerProvider = DoubleCheck.provider(HeadsUpController_Factory.create(this.headsUpViewBinderProvider, this.miuiNotificationInterruptStateProviderImplProvider, this.provideHeadsUpManagerPhoneProvider, this.provideNotificationRemoteInputManagerProvider, this.statusBarStateControllerImplProvider, this.provideVisualStabilityManagerProvider, this.provideNotificationListenerProvider));
        NotificationClickerLogger_Factory create7 = NotificationClickerLogger_Factory.create(this.provideNotifInteractionLogBufferProvider);
        this.notificationClickerLoggerProvider = create7;
        NotificationClicker_Builder_Factory create8 = NotificationClicker_Builder_Factory.create(this.newBubbleControllerProvider, create7);
        this.builderProvider = create8;
        this.notificationsControllerImplProvider = DoubleCheck.provider(NotificationsControllerImpl_Factory.create(this.featureFlagsProvider, this.provideNotificationListenerProvider, this.provideNotificationEntryManagerProvider, this.notifPipelineProvider, this.targetSdkResolverProvider, this.notifPipelineInitializerProvider, this.notifBindPipelineInitializerProvider, this.deviceProvisionedControllerImplProvider, this.notificationRowBinderImplProvider, this.remoteInputUriControllerProvider, this.notificationGroupManagerProvider, this.provideNotificationGroupAlertTransferHelperProvider, this.provideHeadsUpManagerPhoneProvider, this.headsUpControllerProvider, this.headsUpViewBinderProvider, create8));
        NotificationsControllerStub_Factory create9 = NotificationsControllerStub_Factory.create(this.provideNotificationListenerProvider);
        this.notificationsControllerStubProvider = create9;
        this.provideNotificationsControllerProvider = DoubleCheck.provider(NotificationsModule_ProvideNotificationsControllerFactory.create(this.provideContextProvider, this.notificationsControllerImplProvider, create9));
        this.darkIconDispatcherImplProvider = DoubleCheck.provider(DarkIconDispatcherImpl_Factory.create(this.provideContextProvider, this.provideCommandQueueProvider));
        this.provideLightBarControllerProvider = DoubleCheck.provider(MiuiStatusBarModule_ProvideLightBarControllerFactory.create(builder.miuiStatusBarModule, this.provideContextProvider, this.darkIconDispatcherImplProvider, this.provideBatteryControllerProvider, this.navigationModeControllerProvider));
        this.provideIWindowManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideIWindowManagerFactory.create());
        this.provideAutoHideControllerProvider = DoubleCheck.provider(DependencyProvider_ProvideAutoHideControllerFactory.create(builder.dependencyProvider, this.provideContextProvider, this.provideMainHandlerProvider, this.provideIWindowManagerProvider));
        this.statusBarIconControllerImplProvider = DoubleCheck.provider(StatusBarIconControllerImpl_Factory.create(this.provideContextProvider, this.provideCommandQueueProvider));
        this.notificationWakeUpCoordinatorProvider = DoubleCheck.provider(NotificationWakeUpCoordinator_Factory.create(this.provideHeadsUpManagerPhoneProvider, this.statusBarStateControllerImplProvider, this.keyguardBypassControllerProvider, this.dozeParametersProvider));
        Provider<NotificationRoundnessManager> provider11 = DoubleCheck.provider(NotificationRoundnessManager_Factory.create(this.keyguardBypassControllerProvider, this.miuiNotificationSectionsFeatureManagerProvider));
        this.notificationRoundnessManagerProvider = provider11;
        this.pulseExpansionHandlerProvider = DoubleCheck.provider(PulseExpansionHandler_Factory.create(this.provideContextProvider, this.notificationWakeUpCoordinatorProvider, this.keyguardBypassControllerProvider, this.provideHeadsUpManagerPhoneProvider, provider11, this.statusBarStateControllerImplProvider, this.falsingManagerProxyProvider));
        this.dynamicPrivacyControllerProvider = DoubleCheck.provider(DynamicPrivacyController_Factory.create(this.notificationLockscreenUserManagerImplProvider, this.keyguardStateControllerImplProvider, this.statusBarStateControllerImplProvider));
        this.bypassHeadsUpNotifierProvider = DoubleCheck.provider(BypassHeadsUpNotifier_Factory.create(this.provideContextProvider, this.keyguardBypassControllerProvider, this.statusBarStateControllerImplProvider, this.provideHeadsUpManagerPhoneProvider, this.notificationLockscreenUserManagerImplProvider, this.provideNotificationMediaManagerProvider, this.provideNotificationEntryManagerProvider, this.tunerServiceImplProvider));
        this.remoteInputQuickSettingsDisablerProvider = DoubleCheck.provider(RemoteInputQuickSettingsDisabler_Factory.create(this.provideContextProvider, this.provideConfigurationControllerProvider, this.provideCommandQueueProvider));
        this.provideAccessibilityManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideAccessibilityManagerFactory.create(this.provideContextProvider));
        this.provideINotificationManagerProvider = DoubleCheck.provider(DependencyProvider_ProvideINotificationManagerFactory.create(builder.dependencyProvider));
        this.provideShortcutManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideShortcutManagerFactory.create(this.provideContextProvider));
        Provider<ChannelEditorDialogController> provider12 = DoubleCheck.provider(ChannelEditorDialogController_Factory.create(this.provideContextProvider, this.provideINotificationManagerProvider, ChannelEditorDialog_Builder_Factory.create()));
        this.channelEditorDialogControllerProvider = provider12;
        this.provideNotificationGutsManagerProvider = DoubleCheck.provider(NotificationsModule_ProvideNotificationGutsManagerFactory.create(this.provideContextProvider, this.provideVisualStabilityManagerProvider, this.provideStatusBarProvider, this.provideMainHandlerProvider, this.provideBgHandlerProvider, this.provideAccessibilityManagerProvider, this.highPriorityProvider, this.provideINotificationManagerProvider, this.provideLauncherAppsProvider, this.provideShortcutManagerProvider, provider12, this.provideCurrentUserContextTrackerProvider, PriorityOnboardingDialogController_Builder_Factory.create(), this.newBubbleControllerProvider, this.provideUiEventLoggerProvider));
        this.expansionStateLoggerProvider = NotificationLogger_ExpansionStateLogger_Factory.create(this.provideUiBackgroundExecutorProvider);
        Provider<NotificationPanelLogger> provider13 = DoubleCheck.provider(NotificationsModule_ProvideNotificationPanelLoggerFactory.create());
        this.provideNotificationPanelLoggerProvider = provider13;
        this.provideNotificationLoggerProvider = DoubleCheck.provider(NotificationsModule_ProvideNotificationLoggerFactory.create(this.provideNotificationListenerProvider, this.provideUiBackgroundExecutorProvider, this.provideNotificationEntryManagerProvider, this.statusBarStateControllerImplProvider, this.expansionStateLoggerProvider, provider13));
        this.foregroundServiceSectionControllerProvider = DoubleCheck.provider(ForegroundServiceSectionController_Factory.create(this.provideNotificationEntryManagerProvider, this.foregroundServiceDismissalFeatureControllerProvider));
        DynamicChildBindController_Factory create10 = DynamicChildBindController_Factory.create(this.rowContentBindStageProvider);
        this.dynamicChildBindControllerProvider = create10;
        this.provideNotificationViewHierarchyManagerProvider = DoubleCheck.provider(StatusBarDependenciesModule_ProvideNotificationViewHierarchyManagerFactory.create(this.provideContextProvider, this.provideMainHandlerProvider, this.notificationLockscreenUserManagerImplProvider, this.notificationGroupManagerProvider, this.provideVisualStabilityManagerProvider, this.statusBarStateControllerImplProvider, this.provideNotificationEntryManagerProvider, this.keyguardBypassControllerProvider, this.newBubbleControllerProvider, this.dynamicPrivacyControllerProvider, this.foregroundServiceSectionControllerProvider, create10, this.lowPriorityInflationHelperProvider));
        this.provideMetricsLoggerProvider = DoubleCheck.provider(DependencyProvider_ProvideMetricsLoggerFactory.create(builder.dependencyProvider));
        Provider<Optional<Lazy<StatusBar>>> of = PresentJdkOptionalLazyProvider.of(this.provideStatusBarProvider);
        this.optionalOfLazyOfStatusBarProvider = of;
        Provider<ActivityStarterDelegate> provider14 = DoubleCheck.provider(ActivityStarterDelegate_Factory.create(of));
        this.activityStarterDelegateProvider = provider14;
        this.userSwitcherControllerProvider = DoubleCheck.provider(UserSwitcherController_Factory.create(this.provideContextProvider, this.keyguardStateControllerImplProvider, this.provideMainHandlerProvider, provider14, this.providesBroadcastDispatcherProvider, this.provideUiEventLoggerProvider));
        this.provideConnectivityManagagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideConnectivityManagagerFactory.create(this.provideContextProvider));
        this.provideTelephonyManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideTelephonyManagerFactory.create(this.provideContextProvider));
        this.provideWifiManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideWifiManagerFactory.create(this.provideContextProvider));
        Provider<NetworkScoreManager> provider15 = DoubleCheck.provider(SystemServicesModule_ProvideNetworkScoreManagerFactory.create(this.provideContextProvider));
        this.provideNetworkScoreManagerProvider = provider15;
        this.networkControllerImplProvider = DoubleCheck.provider(NetworkControllerImpl_Factory.create(this.provideContextProvider, this.provideBgLooperProvider, this.deviceProvisionedControllerImplProvider, this.providesBroadcastDispatcherProvider, this.provideConnectivityManagagerProvider, this.provideTelephonyManagerProvider, this.provideWifiManagerProvider, provider15));
        this.sysuiColorExtractorProvider = DoubleCheck.provider(SysuiColorExtractor_Factory.create(this.provideContextProvider, this.provideConfigurationControllerProvider));
        this.screenLifecycleProvider = DoubleCheck.provider(ScreenLifecycle_Factory.create());
        this.wakefulnessLifecycleProvider = DoubleCheck.provider(WakefulnessLifecycle_Factory.create());
        this.vibratorHelperProvider = DoubleCheck.provider(VibratorHelper_Factory.create(this.provideContextProvider));
        this.provideNavigationBarControllerProvider = DoubleCheck.provider(DependencyProvider_ProvideNavigationBarControllerFactory.create(builder.dependencyProvider, this.provideContextProvider, this.provideMainHandlerProvider, this.provideCommandQueueProvider));
        this.provideAssistUtilsProvider = DoubleCheck.provider(AssistModule_ProvideAssistUtilsFactory.create(this.provideContextProvider));
        this.provideBackgroundHandlerProvider = DoubleCheck.provider(AssistModule_ProvideBackgroundHandlerFactory.create());
        this.provideAssistHandleViewControllerProvider = AssistModule_ProvideAssistHandleViewControllerFactory.create(this.provideNavigationBarControllerProvider);
        this.deviceConfigHelperProvider = DoubleCheck.provider(DeviceConfigHelper_Factory.create());
        this.assistHandleOffBehaviorProvider = DoubleCheck.provider(AssistHandleOffBehavior_Factory.create());
        Provider<SysUiState> provider16 = DoubleCheck.provider(SystemUIModule_ProvideSysUiStateFactory.create());
        this.provideSysUiStateProvider = provider16;
        this.assistHandleLikeHomeBehaviorProvider = DoubleCheck.provider(AssistHandleLikeHomeBehavior_Factory.create(this.statusBarStateControllerImplProvider, this.wakefulnessLifecycleProvider, provider16));
        this.provideSystemClockProvider = DoubleCheck.provider(AssistModule_ProvideSystemClockFactory.create());
        this.provideActivityManagerWrapperProvider = DoubleCheck.provider(DependencyProvider_ProvideActivityManagerWrapperFactory.create(builder.dependencyProvider));
        this.displayControllerProvider = DoubleCheck.provider(DisplayController_Factory.create(this.provideContextProvider, this.provideMainHandlerProvider, this.provideIWindowManagerProvider));
    }

    private void initialize3(Builder builder) {
        this.floatingContentCoordinatorProvider = DoubleCheck.provider(FloatingContentCoordinator_Factory.create());
        PipSnapAlgorithm_Factory create = PipSnapAlgorithm_Factory.create(this.provideContextProvider);
        this.pipSnapAlgorithmProvider = create;
        this.pipBoundsHandlerProvider = DoubleCheck.provider(PipBoundsHandler_Factory.create(this.provideContextProvider, create, this.displayControllerProvider));
        this.pipSurfaceTransactionHelperProvider = DoubleCheck.provider(PipSurfaceTransactionHelper_Factory.create(this.provideContextProvider, this.provideConfigurationControllerProvider));
        DelegateFactory delegateFactory = new DelegateFactory();
        this.contextComponentResolverProvider = delegateFactory;
        RecentsModule_ProvideRecentsImplFactory create2 = RecentsModule_ProvideRecentsImplFactory.create(this.provideContextProvider, delegateFactory);
        this.provideRecentsImplProvider = create2;
        Provider<Recents> provider = DoubleCheck.provider(SystemUIDefaultModule_ProvideRecentsFactory.create(this.provideContextProvider, create2, this.provideCommandQueueProvider));
        this.provideRecentsProvider = provider;
        this.optionalOfLazyOfRecentsProvider = PresentJdkOptionalLazyProvider.of(provider);
        this.systemWindowsProvider = DoubleCheck.provider(SystemWindows_Factory.create(this.provideContextProvider, this.displayControllerProvider, this.provideIWindowManagerProvider));
        Provider<TransactionPool> provider2 = DoubleCheck.provider(TransactionPool_Factory.create());
        this.transactionPoolProvider = provider2;
        Provider<DisplayImeController> provider3 = DoubleCheck.provider(DisplayImeController_Factory.create(this.systemWindowsProvider, this.displayControllerProvider, this.provideMainHandlerProvider, provider2));
        this.displayImeControllerProvider = provider3;
        this.provideDividerProvider = DoubleCheck.provider(DividerModule_ProvideDividerFactory.create(this.provideContextProvider, this.optionalOfLazyOfRecentsProvider, this.displayControllerProvider, this.systemWindowsProvider, provider3, this.provideMainHandlerProvider, this.keyguardStateControllerImplProvider, this.transactionPoolProvider));
        this.pipAnimationControllerProvider = PipAnimationController_Factory.create(this.provideContextProvider, this.pipSurfaceTransactionHelperProvider);
        Provider<PipUiEventLogger> provider4 = DoubleCheck.provider(PipUiEventLogger_Factory.create(this.provideUiEventLoggerProvider));
        this.pipUiEventLoggerProvider = provider4;
        this.pipTaskOrganizerProvider = DoubleCheck.provider(PipTaskOrganizer_Factory.create(this.provideContextProvider, this.pipBoundsHandlerProvider, this.pipSurfaceTransactionHelperProvider, this.provideDividerProvider, this.displayControllerProvider, this.pipAnimationControllerProvider, provider4));
        Provider<PipManager> provider5 = DoubleCheck.provider(PipManager_Factory.create(this.provideContextProvider, this.providesBroadcastDispatcherProvider, this.displayControllerProvider, this.floatingContentCoordinatorProvider, DeviceConfigProxy_Factory.create(), this.pipBoundsHandlerProvider, this.pipSnapAlgorithmProvider, this.pipTaskOrganizerProvider, this.provideSysUiStateProvider, this.pipUiEventLoggerProvider));
        this.pipManagerProvider = provider5;
        this.pipUIProvider = DoubleCheck.provider(PipUI_Factory.create(this.provideContextProvider, this.provideCommandQueueProvider, provider5));
        Provider<Optional<Divider>> of = PresentJdkOptionalInstanceProvider.of(this.provideDividerProvider);
        this.optionalOfDividerProvider = of;
        this.overviewProxyServiceProvider = DoubleCheck.provider(OverviewProxyService_Factory.create(this.provideContextProvider, this.provideCommandQueueProvider, this.provideNavigationBarControllerProvider, this.navigationModeControllerProvider, this.notificationShadeWindowControllerProvider, this.provideSysUiStateProvider, this.pipUIProvider, of, this.optionalOfLazyOfStatusBarProvider, this.providesBroadcastDispatcherProvider));
        Provider<PackageManagerWrapper> provider6 = DoubleCheck.provider(SystemServicesModule_ProvidePackageManagerWrapperFactory.create());
        this.providePackageManagerWrapperProvider = provider6;
        Provider provider7 = DoubleCheck.provider(AssistHandleReminderExpBehavior_Factory.create(this.provideSystemClockProvider, this.provideBackgroundHandlerProvider, this.deviceConfigHelperProvider, this.statusBarStateControllerImplProvider, this.provideActivityManagerWrapperProvider, this.overviewProxyServiceProvider, this.provideSysUiStateProvider, this.wakefulnessLifecycleProvider, provider6, this.providesBroadcastDispatcherProvider, this.bootCompleteCacheImplProvider));
        this.assistHandleReminderExpBehaviorProvider = provider7;
        Provider provider8 = DoubleCheck.provider(AssistModule_ProvideAssistHandleBehaviorControllerMapFactory.create(this.assistHandleOffBehaviorProvider, this.assistHandleLikeHomeBehaviorProvider, provider7));
        this.provideAssistHandleBehaviorControllerMapProvider = provider8;
        this.assistHandleBehaviorControllerProvider = DoubleCheck.provider(AssistHandleBehaviorController_Factory.create(this.provideContextProvider, this.provideAssistUtilsProvider, this.provideBackgroundHandlerProvider, this.provideAssistHandleViewControllerProvider, this.deviceConfigHelperProvider, provider8, this.navigationModeControllerProvider, this.provideAccessibilityManagerProvider, this.dumpManagerProvider));
        Provider<PhoneStateMonitor> provider9 = DoubleCheck.provider(PhoneStateMonitor_Factory.create(this.provideContextProvider, this.providesBroadcastDispatcherProvider, this.optionalOfLazyOfStatusBarProvider, this.bootCompleteCacheImplProvider));
        this.phoneStateMonitorProvider = provider9;
        Provider<AssistLogger> provider10 = DoubleCheck.provider(AssistLogger_Factory.create(this.provideContextProvider, this.provideUiEventLoggerProvider, this.provideAssistUtilsProvider, provider9, this.assistHandleBehaviorControllerProvider));
        this.assistLoggerProvider = provider10;
        Provider<DefaultUiController> provider11 = DoubleCheck.provider(DefaultUiController_Factory.create(this.provideContextProvider, provider10));
        this.defaultUiControllerProvider = provider11;
        this.assistManagerProvider = DoubleCheck.provider(AssistManager_Factory.create(this.deviceProvisionedControllerImplProvider, this.provideContextProvider, this.provideAssistUtilsProvider, this.assistHandleBehaviorControllerProvider, this.provideCommandQueueProvider, this.phoneStateMonitorProvider, this.overviewProxyServiceProvider, this.provideConfigurationControllerProvider, this.provideSysUiStateProvider, provider11, this.assistLoggerProvider));
        this.lockscreenGestureLoggerProvider = DoubleCheck.provider(LockscreenGestureLogger_Factory.create());
        this.shadeControllerImplProvider = DoubleCheck.provider(ShadeControllerImpl_Factory.create(this.provideCommandQueueProvider, this.statusBarStateControllerImplProvider, this.notificationShadeWindowControllerProvider, this.statusBarKeyguardViewManagerProvider, this.provideWindowManagerProvider, this.provideStatusBarProvider, this.assistManagerProvider, this.newBubbleControllerProvider));
        this.accessibilityControllerProvider = DoubleCheck.provider(AccessibilityController_Factory.create(this.provideContextProvider));
        this.builderProvider2 = WakeLock_Builder_Factory.create(this.provideContextProvider);
        Provider<IBatteryStats> provider12 = DoubleCheck.provider(SystemServicesModule_ProvideIBatteryStatsFactory.create());
        this.provideIBatteryStatsProvider = provider12;
        Provider<KeyguardIndicationController> provider13 = DoubleCheck.provider(KeyguardIndicationController_Factory.create(this.provideContextProvider, this.builderProvider2, this.keyguardStateControllerImplProvider, this.statusBarStateControllerImplProvider, this.keyguardUpdateMonitorProvider, this.dockManagerImplProvider, this.providesBroadcastDispatcherProvider, this.provideDevicePolicyManagerProvider, provider12, this.provideUserManagerProvider));
        this.keyguardIndicationControllerProvider = provider13;
        this.lockscreenLockIconControllerProvider = DoubleCheck.provider(LockscreenLockIconController_Factory.create(this.lockscreenGestureLoggerProvider, this.keyguardUpdateMonitorProvider, this.provideLockPatternUtilsProvider, this.shadeControllerImplProvider, this.accessibilityControllerProvider, provider13, this.statusBarStateControllerImplProvider, this.provideConfigurationControllerProvider, this.notificationWakeUpCoordinatorProvider, this.keyguardBypassControllerProvider, this.dockManagerImplProvider, this.keyguardStateControllerImplProvider, this.provideResourcesProvider, this.provideHeadsUpManagerPhoneProvider));
        this.provideAlarmManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideAlarmManagerFactory.create(this.provideContextProvider));
        this.builderProvider3 = DelayedWakeLock_Builder_Factory.create(this.provideContextProvider);
        this.blurUtilsProvider = DoubleCheck.provider(BlurUtils_Factory.create(this.provideResourcesProvider, this.dumpManagerProvider));
        this.scrimControllerProvider = DoubleCheck.provider(ScrimController_Factory.create(this.provideLightBarControllerProvider, this.dozeParametersProvider, this.provideAlarmManagerProvider, this.keyguardStateControllerImplProvider, this.builderProvider3, ConcurrencyModule_ProvideHandlerFactory.create(), this.keyguardUpdateMonitorProvider, this.sysuiColorExtractorProvider, this.dockManagerImplProvider, this.blurUtilsProvider));
        this.provideKeyguardLiftControllerProvider = DoubleCheck.provider(SystemUIModule_ProvideKeyguardLiftControllerFactory.create(this.provideContextProvider, this.statusBarStateControllerImplProvider, this.asyncSensorManagerProvider, this.keyguardUpdateMonitorProvider, this.dumpManagerProvider));
        SystemServicesModule_ProvideWallpaperManagerFactory create3 = SystemServicesModule_ProvideWallpaperManagerFactory.create(this.provideContextProvider);
        this.provideWallpaperManagerProvider = create3;
        this.lockscreenWallpaperProvider = DoubleCheck.provider(LockscreenWallpaper_Factory.create(create3, SystemServicesModule_ProvideIWallPaperManagerFactory.create(), this.keyguardUpdateMonitorProvider, this.dumpManagerProvider, this.provideNotificationMediaManagerProvider, this.provideMainHandlerProvider));
        Provider<LogBuffer> provider14 = DoubleCheck.provider(LogModule_ProvideDozeLogBufferFactory.create(this.provideLogcatEchoTrackerProvider, this.dumpManagerProvider));
        this.provideDozeLogBufferProvider = provider14;
        DozeLogger_Factory create4 = DozeLogger_Factory.create(provider14);
        this.dozeLoggerProvider = create4;
        Provider<DozeLog> provider15 = DoubleCheck.provider(DozeLog_Factory.create(this.keyguardUpdateMonitorProvider, this.dumpManagerProvider, create4));
        this.dozeLogProvider = provider15;
        Provider<DozeScrimController> provider16 = DoubleCheck.provider(DozeScrimController_Factory.create(this.dozeParametersProvider, provider15));
        this.dozeScrimControllerProvider = provider16;
        Provider<BiometricUnlockController> provider17 = DoubleCheck.provider(BiometricUnlockController_Factory.create(this.provideContextProvider, provider16, this.newKeyguardViewMediatorProvider, this.scrimControllerProvider, this.provideStatusBarProvider, this.shadeControllerImplProvider, this.notificationShadeWindowControllerProvider, this.keyguardStateControllerImplProvider, ConcurrencyModule_ProvideHandlerFactory.create(), this.keyguardUpdateMonitorProvider, this.provideResourcesProvider, this.keyguardBypassControllerProvider, this.dozeParametersProvider, this.provideMetricsLoggerProvider, this.dumpManagerProvider));
        this.biometricUnlockControllerProvider = provider17;
        this.miuiDozeServiceHostProvider = DoubleCheck.provider(MiuiDozeServiceHost_Factory.create(this.dozeLogProvider, this.providePowerManagerProvider, this.wakefulnessLifecycleProvider, this.statusBarStateControllerImplProvider, this.deviceProvisionedControllerImplProvider, this.provideHeadsUpManagerPhoneProvider, this.provideBatteryControllerProvider, this.scrimControllerProvider, provider17, this.newKeyguardViewMediatorProvider, this.assistManagerProvider, this.dozeScrimControllerProvider, this.keyguardUpdateMonitorProvider, this.provideVisualStabilityManagerProvider, this.pulseExpansionHandlerProvider, this.notificationShadeWindowControllerProvider, this.notificationWakeUpCoordinatorProvider, this.lockscreenLockIconControllerProvider, this.settingsManagerProvider));
        this.screenPinningRequestProvider = ScreenPinningRequest_Factory.create(this.provideContextProvider, this.optionalOfLazyOfStatusBarProvider);
        Provider<VolumeDialogControllerImpl> provider18 = DoubleCheck.provider(VolumeDialogControllerImpl_Factory.create(this.provideContextProvider, this.providesBroadcastDispatcherProvider, this.optionalOfLazyOfStatusBarProvider, this.ringerModeTrackerImplProvider));
        this.volumeDialogControllerImplProvider = provider18;
        this.volumeDialogComponentProvider = DoubleCheck.provider(VolumeDialogComponent_Factory.create(this.provideContextProvider, this.newKeyguardViewMediatorProvider, provider18));
        this.optionalOfRecentsProvider = PresentJdkOptionalInstanceProvider.of(this.provideRecentsProvider);
        this.statusBarComponentBuilderProvider = new Provider<StatusBarComponent.Builder>() {
            /* class com.android.systemui.dagger.DaggerSystemUIRootComponent.AnonymousClass2 */

            @Override // javax.inject.Provider
            public StatusBarComponent.Builder get() {
                return new StatusBarComponentBuilder();
            }
        };
        this.lightsOutNotifControllerProvider = DoubleCheck.provider(LightsOutNotifController_Factory.create(this.provideWindowManagerProvider, this.provideNotificationEntryManagerProvider, this.provideCommandQueueProvider));
        this.statusBarRemoteInputCallbackProvider = DoubleCheck.provider(StatusBarRemoteInputCallback_Factory.create(this.provideContextProvider, this.notificationGroupManagerProvider, this.notificationLockscreenUserManagerImplProvider, this.keyguardStateControllerImplProvider, this.statusBarStateControllerImplProvider, this.statusBarKeyguardViewManagerProvider, this.activityStarterDelegateProvider, this.shadeControllerImplProvider, this.provideCommandQueueProvider, this.actionClickLoggerProvider));
        this.activityIntentHelperProvider = DoubleCheck.provider(ActivityIntentHelper_Factory.create(this.provideContextProvider));
        StatusBarNotificationActivityStarterLogger_Factory create5 = StatusBarNotificationActivityStarterLogger_Factory.create(this.provideNotifInteractionLogBufferProvider);
        this.statusBarNotificationActivityStarterLoggerProvider = create5;
        this.builderProvider4 = DoubleCheck.provider(StatusBarNotificationActivityStarter_Builder_Factory.create(this.provideContextProvider, this.provideCommandQueueProvider, this.provideMainHandlerProvider, this.provideBgHandlerProvider, this.provideUiBackgroundExecutorProvider, this.provideNotificationEntryManagerProvider, this.notifPipelineProvider, this.notifCollectionProvider, this.provideHeadsUpManagerPhoneProvider, this.activityStarterDelegateProvider, this.notificationClickNotifierProvider, this.statusBarStateControllerImplProvider, this.statusBarKeyguardViewManagerProvider, this.provideKeyguardManagerProvider, this.provideIDreamManagerProvider, this.newBubbleControllerProvider, this.assistManagerProvider, this.provideNotificationRemoteInputManagerProvider, this.notificationGroupManagerProvider, this.notificationLockscreenUserManagerImplProvider, this.shadeControllerImplProvider, this.keyguardStateControllerImplProvider, this.miuiNotificationInterruptStateProviderImplProvider, this.provideLockPatternUtilsProvider, this.statusBarRemoteInputCallbackProvider, this.activityIntentHelperProvider, this.featureFlagsProvider, this.provideMetricsLoggerProvider, create5));
        Factory create6 = InstanceFactory.create(this);
        this.systemUIRootComponentProvider = create6;
        this.injectionInflationControllerProvider = DoubleCheck.provider(InjectionInflationController_Factory.create(create6));
        AnonymousClass3 r1 = new Provider<NotificationRowComponent.Builder>() {
            /* class com.android.systemui.dagger.DaggerSystemUIRootComponent.AnonymousClass3 */

            @Override // javax.inject.Provider
            public NotificationRowComponent.Builder get() {
                return new NotificationRowComponentBuilder();
            }
        };
        this.notificationRowComponentBuilderProvider = r1;
        this.superStatusBarViewFactoryProvider = DoubleCheck.provider(SuperStatusBarViewFactory_Factory.create(this.provideContextProvider, this.injectionInflationControllerProvider, r1, this.lockscreenLockIconControllerProvider));
        this.initControllerProvider = DoubleCheck.provider(InitController_Factory.create());
        this.provideTimeTickHandlerProvider = DoubleCheck.provider(DependencyProvider_ProvideTimeTickHandlerFactory.create(builder.dependencyProvider));
        this.pluginDependencyProvider = DoubleCheck.provider(PluginDependencyProvider_Factory.create(this.providePluginManagerProvider));
        this.keyguardDismissUtilProvider = DoubleCheck.provider(KeyguardDismissUtil_Factory.create());
        this.userInfoControllerImplProvider = DoubleCheck.provider(UserInfoControllerImpl_Factory.create(this.provideContextProvider));
        this.castControllerImplProvider = DoubleCheck.provider(CastControllerImpl_Factory.create(this.provideContextProvider));
        this.hotspotControllerImplProvider = DoubleCheck.provider(HotspotControllerImpl_Factory.create(this.provideContextProvider, this.provideMainHandlerProvider, this.provideBgHandlerProvider));
        this.provideLocalBluetoothControllerProvider = DoubleCheck.provider(SystemServicesModule_ProvideLocalBluetoothControllerFactory.create(this.provideContextProvider, this.provideBgHandlerProvider));
        this.bluetoothControllerImplProvider = DoubleCheck.provider(BluetoothControllerImpl_Factory.create(this.provideContextProvider, this.provideBgLooperProvider, ConcurrencyModule_ProvideMainLooperFactory.create(), this.provideLocalBluetoothControllerProvider));
        this.nextAlarmControllerImplProvider = DoubleCheck.provider(NextAlarmControllerImpl_Factory.create(this.provideContextProvider, this.providesBroadcastDispatcherProvider, this.provideMainHandlerProvider, this.provideBgHandlerProvider));
        this.rotationLockControllerImplProvider = DoubleCheck.provider(RotationLockControllerImpl_Factory.create(this.provideContextProvider));
        this.provideDataSaverControllerProvider = DoubleCheck.provider(DependencyProvider_ProvideDataSaverControllerFactory.create(builder.dependencyProvider, this.networkControllerImplProvider));
        this.locationControllerImplProvider = DoubleCheck.provider(LocationControllerImpl_Factory.create(this.provideContextProvider, ConcurrencyModule_ProvideMainLooperFactory.create(), this.provideBgLooperProvider, this.providesBroadcastDispatcherProvider, this.bootCompleteCacheImplProvider));
        this.sensorPrivacyControllerImplProvider = DoubleCheck.provider(SensorPrivacyControllerImpl_Factory.create(this.provideContextProvider));
        this.provideTelecomManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideTelecomManagerFactory.create(this.provideContextProvider));
        this.provideDisplayIdProvider = SystemServicesModule_ProvideDisplayIdFactory.create(this.provideContextProvider);
        this.provideSharePreferencesProvider = DependencyProvider_ProvideSharePreferencesFactory.create(builder.dependencyProvider, this.provideContextProvider);
        DateFormatUtil_Factory create7 = DateFormatUtil_Factory.create(this.provideContextProvider);
        this.dateFormatUtilProvider = create7;
        this.miuiPhoneStatusBarPolicyProvider = MiuiPhoneStatusBarPolicy_Factory.create(this.provideContextProvider, this.statusBarIconControllerImplProvider, this.provideCommandQueueProvider, this.providesBroadcastDispatcherProvider, this.provideUiBackgroundExecutorProvider, this.provideResourcesProvider, this.castControllerImplProvider, this.hotspotControllerImplProvider, this.bluetoothControllerImplProvider, this.nextAlarmControllerImplProvider, this.userInfoControllerImplProvider, this.rotationLockControllerImplProvider, this.provideDataSaverControllerProvider, this.zenModeControllerImplProvider, this.deviceProvisionedControllerImplProvider, this.keyguardStateControllerImplProvider, this.locationControllerImplProvider, this.sensorPrivacyControllerImplProvider, this.provideIActivityManagerProvider, this.provideAlarmManagerProvider, this.provideUserManagerProvider, this.recordingControllerProvider, this.provideTelecomManagerProvider, this.provideDisplayIdProvider, this.provideSharePreferencesProvider, create7, this.ringerModeTrackerImplProvider);
        Provider<Choreographer> provider19 = DoubleCheck.provider(DependencyProvider_ProvidesChoreographerFactory.create(builder.dependencyProvider));
        this.providesChoreographerProvider = provider19;
        this.notificationShadeDepthControllerProvider = DoubleCheck.provider(NotificationShadeDepthController_Factory.create(this.statusBarStateControllerImplProvider, this.blurUtilsProvider, this.biometricUnlockControllerProvider, this.keyguardStateControllerImplProvider, provider19, this.provideWallpaperManagerProvider, this.notificationShadeWindowControllerProvider, this.dozeParametersProvider, this.dumpManagerProvider));
        this.dismissCallbackRegistryProvider = DoubleCheck.provider(DismissCallbackRegistry_Factory.create(this.provideUiBackgroundExecutorProvider));
        this.statusBarTouchableRegionManagerProvider = DoubleCheck.provider(StatusBarTouchableRegionManager_Factory.create(this.provideContextProvider, this.notificationShadeWindowControllerProvider, this.provideConfigurationControllerProvider, this.provideHeadsUpManagerPhoneProvider));
        Provider<SettingsObserverImpl> provider20 = DoubleCheck.provider(SettingsObserverImpl_Factory.create(this.provideContextProvider, this.providesBroadcastDispatcherProvider, this.provideBgHandlerProvider));
        this.settingsObserverImplProvider = provider20;
        Provider<ControlPanelController> provider21 = DoubleCheck.provider(ControlPanelController_Factory.create(this.provideContextProvider, this.newKeyguardViewMediatorProvider, this.providesBroadcastDispatcherProvider, provider20, this.keyguardStateControllerImplProvider, this.shadeControllerImplProvider));
        this.controlPanelControllerProvider = provider21;
        DelegateFactory delegateFactory2 = (DelegateFactory) this.provideStatusBarProvider;
        Provider<StatusBar> provider22 = DoubleCheck.provider(StatusBarPhoneModule_ProvideStatusBarFactory.create(this.provideContextProvider, this.provideNotificationsControllerProvider, this.provideLightBarControllerProvider, this.provideAutoHideControllerProvider, this.keyguardUpdateMonitorProvider, this.statusBarIconControllerImplProvider, this.pulseExpansionHandlerProvider, this.notificationWakeUpCoordinatorProvider, this.keyguardBypassControllerProvider, this.keyguardStateControllerImplProvider, this.provideHeadsUpManagerPhoneProvider, this.dynamicPrivacyControllerProvider, this.bypassHeadsUpNotifierProvider, this.falsingManagerProxyProvider, this.providesBroadcastDispatcherProvider, this.remoteInputQuickSettingsDisablerProvider, this.provideNotificationGutsManagerProvider, this.provideNotificationLoggerProvider, this.miuiNotificationInterruptStateProviderImplProvider, this.provideNotificationViewHierarchyManagerProvider, this.newKeyguardViewMediatorProvider, this.provideDisplayMetricsProvider, this.provideMetricsLoggerProvider, this.provideUiBackgroundExecutorProvider, this.provideNotificationMediaManagerProvider, this.notificationLockscreenUserManagerImplProvider, this.provideNotificationRemoteInputManagerProvider, this.userSwitcherControllerProvider, this.networkControllerImplProvider, this.provideBatteryControllerProvider, this.sysuiColorExtractorProvider, this.screenLifecycleProvider, this.wakefulnessLifecycleProvider, this.statusBarStateControllerImplProvider, this.vibratorHelperProvider, this.newBubbleControllerProvider, this.notificationGroupManagerProvider, this.provideVisualStabilityManagerProvider, this.deviceProvisionedControllerImplProvider, this.provideNavigationBarControllerProvider, this.assistManagerProvider, this.provideConfigurationControllerProvider, this.notificationShadeWindowControllerProvider, this.lockscreenLockIconControllerProvider, this.dozeParametersProvider, this.scrimControllerProvider, this.provideKeyguardLiftControllerProvider, this.lockscreenWallpaperProvider, this.biometricUnlockControllerProvider, this.miuiDozeServiceHostProvider, this.providePowerManagerProvider, this.screenPinningRequestProvider, this.dozeScrimControllerProvider, this.volumeDialogComponentProvider, this.provideCommandQueueProvider, this.optionalOfRecentsProvider, this.statusBarComponentBuilderProvider, this.providePluginManagerProvider, this.optionalOfDividerProvider, this.lightsOutNotifControllerProvider, this.builderProvider4, this.shadeControllerImplProvider, this.superStatusBarViewFactoryProvider, this.statusBarKeyguardViewManagerProvider, this.providesViewMediatorCallbackProvider, this.initControllerProvider, this.darkIconDispatcherImplProvider, this.provideTimeTickHandlerProvider, this.pluginDependencyProvider, this.keyguardDismissUtilProvider, this.extensionControllerImplProvider, this.userInfoControllerImplProvider, this.miuiPhoneStatusBarPolicyProvider, this.keyguardIndicationControllerProvider, this.notificationShadeDepthControllerProvider, this.dismissCallbackRegistryProvider, this.statusBarTouchableRegionManagerProvider, provider21));
        this.provideStatusBarProvider = provider22;
        delegateFactory2.setDelegatedProvider(provider22);
        this.mediaArtworkProcessorProvider = DoubleCheck.provider(MediaArtworkProcessor_Factory.create());
        MediaControllerFactory_Factory create8 = MediaControllerFactory_Factory.create(this.provideContextProvider);
        this.mediaControllerFactoryProvider = create8;
        this.mediaTimeoutListenerProvider = DoubleCheck.provider(MediaTimeoutListener_Factory.create(create8, this.provideMainDelayableExecutorProvider));
        Provider<MediaResumeListener> provider23 = DoubleCheck.provider(MediaResumeListener_Factory.create(this.provideContextProvider, this.providesBroadcastDispatcherProvider, this.provideBackgroundExecutorProvider, this.tunerServiceImplProvider));
        this.mediaResumeListenerProvider = provider23;
        this.mediaDataManagerProvider = DoubleCheck.provider(MediaDataManager_Factory.create(this.provideContextProvider, this.provideBackgroundExecutorProvider, this.provideMainExecutorProvider, this.mediaControllerFactoryProvider, this.dumpManagerProvider, this.providesBroadcastDispatcherProvider, this.mediaTimeoutListenerProvider, provider23));
        Provider<NotificationMediaManager> provider24 = DoubleCheck.provider(StatusBarDependenciesModule_ProvideNotificationMediaManagerFactory.create(this.provideContextProvider, this.provideStatusBarProvider, this.notificationShadeWindowControllerProvider, this.provideNotificationEntryManagerProvider, this.mediaArtworkProcessorProvider, this.keyguardBypassControllerProvider, this.provideMainDelayableExecutorProvider, DeviceConfigProxy_Factory.create(), this.mediaDataManagerProvider));
        this.provideNotificationMediaManagerProvider = provider24;
        ((DelegateFactory) this.provideNotificationMediaManagerProvider).setDelegatedProvider(provider24);
        Provider<StatusBarKeyguardViewManager> provider25 = DoubleCheck.provider(StatusBarKeyguardViewManager_Factory.create(this.provideContextProvider, this.providesViewMediatorCallbackProvider, this.provideLockPatternUtilsProvider, this.statusBarStateControllerImplProvider, this.provideConfigurationControllerProvider, this.keyguardUpdateMonitorProvider, this.navigationModeControllerProvider, this.dockManagerImplProvider, this.notificationShadeWindowControllerProvider, this.keyguardStateControllerImplProvider, this.provideNotificationMediaManagerProvider));
        this.statusBarKeyguardViewManagerProvider = provider25;
        ((DelegateFactory) this.statusBarKeyguardViewManagerProvider).setDelegatedProvider(provider25);
        Provider<TrustManager> provider26 = DoubleCheck.provider(SystemServicesModule_ProvideTrustManagerFactory.create(this.provideContextProvider));
        this.provideTrustManagerProvider = provider26;
        Provider<KeyguardViewMediator> provider27 = DoubleCheck.provider(KeyguardModule_NewKeyguardViewMediatorFactory.create(this.provideContextProvider, this.falsingManagerProxyProvider, this.provideLockPatternUtilsProvider, this.providesBroadcastDispatcherProvider, this.statusBarKeyguardViewManagerProvider, this.dismissCallbackRegistryProvider, this.keyguardUpdateMonitorProvider, this.dumpManagerProvider, this.providePowerManagerProvider, provider26, this.provideUiBackgroundExecutorProvider, DeviceConfigProxy_Factory.create(), this.navigationModeControllerProvider));
        this.newKeyguardViewMediatorProvider = provider27;
        ((DelegateFactory) this.newKeyguardViewMediatorProvider).setDelegatedProvider(provider27);
        Provider<NotificationShadeWindowController> provider28 = DoubleCheck.provider(NotificationShadeWindowController_Factory.create(this.provideContextProvider, this.provideWindowManagerProvider, this.provideIActivityManagerProvider, this.dozeParametersProvider, this.statusBarStateControllerImplProvider, this.provideConfigurationControllerProvider, this.newKeyguardViewMediatorProvider, this.keyguardBypassControllerProvider, this.sysuiColorExtractorProvider, this.dumpManagerProvider));
        this.notificationShadeWindowControllerProvider = provider28;
        ((DelegateFactory) this.notificationShadeWindowControllerProvider).setDelegatedProvider(provider28);
        this.bubbleDataProvider = DoubleCheck.provider(BubbleData_Factory.create(this.provideContextProvider));
        this.bubbleVolatileRepositoryProvider = DoubleCheck.provider(BubbleVolatileRepository_Factory.create(this.provideLauncherAppsProvider));
        this.bubblePersistentRepositoryProvider = DoubleCheck.provider(BubblePersistentRepository_Factory.create(this.provideContextProvider));
    }

    private void initialize4(Builder builder) {
        Provider<BubbleDataRepository> provider = DoubleCheck.provider(BubbleDataRepository_Factory.create(this.bubbleVolatileRepositoryProvider, this.bubblePersistentRepositoryProvider, this.provideLauncherAppsProvider));
        this.bubbleDataRepositoryProvider = provider;
        DelegateFactory delegateFactory = (DelegateFactory) this.newBubbleControllerProvider;
        Provider<BubbleController> provider2 = DoubleCheck.provider(BubbleModule_NewBubbleControllerFactory.create(this.provideContextProvider, this.notificationShadeWindowControllerProvider, this.statusBarStateControllerImplProvider, this.shadeControllerImplProvider, this.bubbleDataProvider, this.provideConfigurationControllerProvider, this.miuiNotificationInterruptStateProviderImplProvider, this.zenModeControllerImplProvider, this.notificationLockscreenUserManagerImplProvider, this.notificationGroupManagerProvider, this.provideNotificationEntryManagerProvider, this.notifPipelineProvider, this.featureFlagsProvider, this.dumpManagerProvider, this.floatingContentCoordinatorProvider, provider, this.provideSysUiStateProvider, this.provideINotificationManagerProvider, this.provideIStatusBarServiceProvider, this.provideWindowManagerProvider, this.provideLauncherAppsProvider));
        this.newBubbleControllerProvider = provider2;
        delegateFactory.setDelegatedProvider(provider2);
        this.bubbleOverflowActivityProvider = BubbleOverflowActivity_Factory.create(this.newBubbleControllerProvider);
        this.usbDebuggingSecondaryUserActivityProvider = UsbDebuggingSecondaryUserActivity_Factory.create(this.providesBroadcastDispatcherProvider);
        Provider<Executor> provider3 = DoubleCheck.provider(ConcurrencyModule_ProvideExecutorFactory.create(this.provideBgLooperProvider));
        this.provideExecutorProvider = provider3;
        this.controlsListingControllerImplProvider = DoubleCheck.provider(ControlsListingControllerImpl_Factory.create(this.provideContextProvider, provider3));
        this.provideBackgroundDelayableExecutorProvider = DoubleCheck.provider(ConcurrencyModule_ProvideBackgroundDelayableExecutorFactory.create(this.provideBgLooperProvider));
        this.controlsControllerImplProvider = new DelegateFactory();
        this.provideDelayableExecutorProvider = DoubleCheck.provider(ConcurrencyModule_ProvideDelayableExecutorFactory.create(this.provideBgLooperProvider));
        this.globalActionsComponentProvider = new DelegateFactory();
        this.provideVibratorProvider = DoubleCheck.provider(SystemServicesModule_ProvideVibratorFactory.create(this.provideContextProvider));
        Provider<PackageManager> provider4 = DoubleCheck.provider(SystemServicesModule_ProvidePackageManagerFactory.create(this.provideContextProvider));
        this.providePackageManagerProvider = provider4;
        this.providesControlsFeatureEnabledProvider = DoubleCheck.provider(ControlsModule_ProvidesControlsFeatureEnabledFactory.create(provider4));
        DelegateFactory delegateFactory2 = new DelegateFactory();
        this.controlsUiControllerImplProvider = delegateFactory2;
        Provider<ControlsComponent> provider5 = DoubleCheck.provider(ControlsComponent_Factory.create(this.providesControlsFeatureEnabledProvider, this.controlsControllerImplProvider, delegateFactory2, this.controlsListingControllerImplProvider));
        this.controlsComponentProvider = provider5;
        GlobalActionsDialog_Factory create = GlobalActionsDialog_Factory.create(this.provideContextProvider, this.globalActionsComponentProvider, this.provideAudioManagerProvider, this.provideIDreamManagerProvider, this.provideDevicePolicyManagerProvider, this.provideLockPatternUtilsProvider, this.providesBroadcastDispatcherProvider, this.provideConnectivityManagagerProvider, this.provideTelephonyManagerProvider, this.provideContentResolverProvider, this.provideVibratorProvider, this.provideResourcesProvider, this.provideConfigurationControllerProvider, this.activityStarterDelegateProvider, this.keyguardStateControllerImplProvider, this.provideUserManagerProvider, this.provideTrustManagerProvider, this.provideIActivityManagerProvider, this.provideTelecomManagerProvider, this.provideMetricsLoggerProvider, this.notificationShadeDepthControllerProvider, this.sysuiColorExtractorProvider, this.provideIStatusBarServiceProvider, this.notificationShadeWindowControllerProvider, this.provideIWindowManagerProvider, this.provideBackgroundExecutorProvider, this.provideUiEventLoggerProvider, this.ringerModeTrackerImplProvider, this.provideSysUiStateProvider, this.provideMainHandlerProvider, provider5, this.provideCurrentUserContextTrackerProvider);
        this.globalActionsDialogProvider = create;
        GlobalActionsImpl_Factory create2 = GlobalActionsImpl_Factory.create(this.provideContextProvider, this.provideCommandQueueProvider, create, this.blurUtilsProvider);
        this.globalActionsImplProvider = create2;
        Provider<GlobalActionsComponent> provider6 = DoubleCheck.provider(GlobalActionsComponent_Factory.create(this.provideContextProvider, this.provideCommandQueueProvider, this.extensionControllerImplProvider, create2, this.statusBarKeyguardViewManagerProvider));
        this.globalActionsComponentProvider = provider6;
        ((DelegateFactory) this.globalActionsComponentProvider).setDelegatedProvider(provider6);
        Provider<ControlActionCoordinatorImpl> provider7 = DoubleCheck.provider(ControlActionCoordinatorImpl_Factory.create(this.provideContextProvider, this.provideDelayableExecutorProvider, this.provideMainDelayableExecutorProvider, this.activityStarterDelegateProvider, this.keyguardStateControllerImplProvider, this.globalActionsComponentProvider));
        this.controlActionCoordinatorImplProvider = provider7;
        Provider<ControlsUiControllerImpl> provider8 = DoubleCheck.provider(ControlsUiControllerImpl_Factory.create(this.controlsControllerImplProvider, this.provideContextProvider, this.provideMainDelayableExecutorProvider, this.provideBackgroundDelayableExecutorProvider, this.controlsListingControllerImplProvider, this.provideSharePreferencesProvider, provider7, this.activityStarterDelegateProvider, this.shadeControllerImplProvider));
        this.controlsUiControllerImplProvider = provider8;
        ((DelegateFactory) this.controlsUiControllerImplProvider).setDelegatedProvider(provider8);
        this.controlsBindingControllerImplProvider = DoubleCheck.provider(ControlsBindingControllerImpl_Factory.create(this.provideContextProvider, this.provideBackgroundDelayableExecutorProvider, this.controlsControllerImplProvider));
        Provider<Optional<ControlsFavoritePersistenceWrapper>> absentJdkOptionalProvider = absentJdkOptionalProvider();
        this.optionalOfControlsFavoritePersistenceWrapperProvider = absentJdkOptionalProvider;
        Provider<ControlsControllerImpl> provider9 = DoubleCheck.provider(ControlsControllerImpl_Factory.create(this.provideContextProvider, this.provideBackgroundDelayableExecutorProvider, this.controlsUiControllerImplProvider, this.controlsBindingControllerImplProvider, this.controlsListingControllerImplProvider, this.providesBroadcastDispatcherProvider, absentJdkOptionalProvider, this.dumpManagerProvider));
        this.controlsControllerImplProvider = provider9;
        ((DelegateFactory) this.controlsControllerImplProvider).setDelegatedProvider(provider9);
        this.controlsProviderSelectorActivityProvider = ControlsProviderSelectorActivity_Factory.create(this.provideMainExecutorProvider, this.provideBackgroundExecutorProvider, this.controlsListingControllerImplProvider, this.controlsControllerImplProvider, this.globalActionsComponentProvider, this.providesBroadcastDispatcherProvider);
        this.controlsFavoritingActivityProvider = ControlsFavoritingActivity_Factory.create(this.provideMainExecutorProvider, this.controlsControllerImplProvider, this.controlsListingControllerImplProvider, this.providesBroadcastDispatcherProvider, this.globalActionsComponentProvider);
        this.controlsEditingActivityProvider = ControlsEditingActivity_Factory.create(this.controlsControllerImplProvider, this.providesBroadcastDispatcherProvider, this.globalActionsComponentProvider);
        this.controlsRequestDialogProvider = ControlsRequestDialog_Factory.create(this.controlsControllerImplProvider, this.providesBroadcastDispatcherProvider, this.controlsListingControllerImplProvider);
        MapProviderFactory.Builder builder2 = MapProviderFactory.builder(12);
        builder2.put(TunerActivity.class, TunerActivity_Factory.create());
        builder2.put(ForegroundServicesDialog.class, ForegroundServicesDialog_Factory.create());
        builder2.put(WorkLockActivity.class, this.workLockActivityProvider);
        builder2.put(BrightnessDialog.class, this.brightnessDialogProvider);
        builder2.put(ScreenRecordDialog.class, this.screenRecordDialogProvider);
        builder2.put(BubbleOverflowActivity.class, this.bubbleOverflowActivityProvider);
        builder2.put(UsbDebuggingActivity.class, UsbDebuggingActivity_Factory.create());
        builder2.put(UsbDebuggingSecondaryUserActivity.class, this.usbDebuggingSecondaryUserActivityProvider);
        builder2.put(ControlsProviderSelectorActivity.class, this.controlsProviderSelectorActivityProvider);
        builder2.put(ControlsFavoritingActivity.class, this.controlsFavoritingActivityProvider);
        builder2.put(ControlsEditingActivity.class, this.controlsEditingActivityProvider);
        builder2.put(ControlsRequestDialog.class, this.controlsRequestDialogProvider);
        this.mapOfClassOfAndProviderOfActivityProvider = builder2.build();
        this.proximityCheckProvider = ProximitySensor_ProximityCheck_Factory.create(this.proximitySensorProvider, this.provideMainDelayableExecutorProvider);
        this.dozeServiceHostProvider = DoubleCheck.provider(DozeServiceHost_Factory.create(this.dozeLogProvider, this.providePowerManagerProvider, this.wakefulnessLifecycleProvider, this.statusBarStateControllerImplProvider, this.deviceProvisionedControllerImplProvider, this.provideHeadsUpManagerPhoneProvider, this.provideBatteryControllerProvider, this.scrimControllerProvider, this.biometricUnlockControllerProvider, this.newKeyguardViewMediatorProvider, this.assistManagerProvider, this.dozeScrimControllerProvider, this.keyguardUpdateMonitorProvider, this.provideVisualStabilityManagerProvider, this.pulseExpansionHandlerProvider, this.notificationShadeWindowControllerProvider, this.notificationWakeUpCoordinatorProvider, this.lockscreenLockIconControllerProvider));
        DozeFactory_Factory create3 = DozeFactory_Factory.create(this.falsingManagerProxyProvider, this.dozeLogProvider, this.dozeParametersProvider, this.provideBatteryControllerProvider, this.asyncSensorManagerProvider, this.provideAlarmManagerProvider, this.wakefulnessLifecycleProvider, this.keyguardUpdateMonitorProvider, this.dockManagerImplProvider, SystemServicesModule_ProvideIWallPaperManagerFactory.create(), this.proximitySensorProvider, this.proximityCheckProvider, this.builderProvider3, this.provideMainHandlerProvider, this.biometricUnlockControllerProvider, this.providesBroadcastDispatcherProvider, this.dozeServiceHostProvider);
        this.dozeFactoryProvider = create3;
        this.dozeServiceProvider = DozeService_Factory.create(create3, this.providePluginManagerProvider);
        Provider<KeyguardLifecyclesDispatcher> provider10 = DoubleCheck.provider(KeyguardLifecyclesDispatcher_Factory.create(this.screenLifecycleProvider, this.wakefulnessLifecycleProvider));
        this.keyguardLifecyclesDispatcherProvider = provider10;
        this.keyguardServiceProvider = KeyguardService_Factory.create(this.newKeyguardViewMediatorProvider, provider10);
        this.dumpHandlerProvider = DumpHandler_Factory.create(this.provideContextProvider, this.dumpManagerProvider, this.logBufferEulogizerProvider);
        LogBufferFreezer_Factory create4 = LogBufferFreezer_Factory.create(this.dumpManagerProvider, this.provideMainDelayableExecutorProvider);
        this.logBufferFreezerProvider = create4;
        this.systemUIServiceProvider = SystemUIService_Factory.create(this.provideMainHandlerProvider, this.dumpHandlerProvider, this.providesBroadcastDispatcherProvider, create4);
        this.systemUIAuxiliaryDumpServiceProvider = SystemUIAuxiliaryDumpService_Factory.create(this.dumpHandlerProvider);
        ScreenshotNotificationsController_Factory create5 = ScreenshotNotificationsController_Factory.create(this.provideContextProvider, this.provideWindowManagerProvider);
        this.screenshotNotificationsControllerProvider = create5;
        Provider<GlobalScreenshot> provider11 = DoubleCheck.provider(GlobalScreenshot_Factory.create(this.provideContextProvider, this.provideResourcesProvider, create5, this.provideUiEventLoggerProvider));
        this.globalScreenshotProvider = provider11;
        this.takeScreenshotServiceProvider = TakeScreenshotService_Factory.create(provider11, this.provideUserManagerProvider, this.provideUiEventLoggerProvider);
        this.fallbackTakeScreenshotServiceProvider = FallbackTakeScreenshotService_Factory.create(this.globalScreenshotProvider, this.provideUserManagerProvider, this.provideUiEventLoggerProvider);
        Provider<Looper> provider12 = DoubleCheck.provider(ConcurrencyModule_ProvideLongRunningLooperFactory.create());
        this.provideLongRunningLooperProvider = provider12;
        Provider<Executor> provider13 = DoubleCheck.provider(ConcurrencyModule_ProvideLongRunningExecutorFactory.create(provider12));
        this.provideLongRunningExecutorProvider = provider13;
        this.recordingServiceProvider = RecordingService_Factory.create(this.recordingControllerProvider, provider13, this.provideUiEventLoggerProvider, this.provideNotificationManagerProvider, this.provideCurrentUserContextTrackerProvider);
        MapProviderFactory.Builder builder3 = MapProviderFactory.builder(8);
        builder3.put(DozeService.class, this.dozeServiceProvider);
        builder3.put(ImageWallpaper.class, ImageWallpaper_Factory.create());
        builder3.put(KeyguardService.class, this.keyguardServiceProvider);
        builder3.put(SystemUIService.class, this.systemUIServiceProvider);
        builder3.put(SystemUIAuxiliaryDumpService.class, this.systemUIAuxiliaryDumpServiceProvider);
        builder3.put(TakeScreenshotService.class, this.takeScreenshotServiceProvider);
        builder3.put(FallbackTakeScreenshotService.class, this.fallbackTakeScreenshotServiceProvider);
        builder3.put(RecordingService.class, this.recordingServiceProvider);
        this.mapOfClassOfAndProviderOfServiceProvider = builder3.build();
        this.authControllerProvider = DoubleCheck.provider(AuthController_Factory.create(this.provideContextProvider, this.provideCommandQueueProvider));
        Provider<String> provider14 = DoubleCheck.provider(SystemUIDefaultModule_ProvideLeakReportEmailFactory.create());
        this.provideLeakReportEmailProvider = provider14;
        Provider<LeakReporter> provider15 = DoubleCheck.provider(LeakReporter_Factory.create(this.provideContextProvider, this.provideLeakDetectorProvider, provider14));
        this.leakReporterProvider = provider15;
        Provider<GarbageMonitor> provider16 = DoubleCheck.provider(GarbageMonitor_Factory.create(this.provideContextProvider, this.provideBgLooperProvider, this.provideLeakDetectorProvider, provider15));
        this.garbageMonitorProvider = provider16;
        this.serviceProvider = DoubleCheck.provider(GarbageMonitor_Service_Factory.create(this.provideContextProvider, provider16));
        this.instantAppNotifierProvider = DoubleCheck.provider(InstantAppNotifier_Factory.create(this.provideContextProvider, this.provideCommandQueueProvider, this.provideUiBackgroundExecutorProvider, this.provideDividerProvider));
        this.latencyTesterProvider = DoubleCheck.provider(LatencyTester_Factory.create(this.provideContextProvider, this.biometricUnlockControllerProvider, this.providePowerManagerProvider, this.providesBroadcastDispatcherProvider));
        this.powerUIProvider = DoubleCheck.provider(PowerUI_Factory.create(this.provideContextProvider, this.providesBroadcastDispatcherProvider, this.provideCommandQueueProvider, this.provideStatusBarProvider));
        this.screenDecorationsProvider = DoubleCheck.provider(ScreenDecorations_Factory.create(this.provideContextProvider, this.provideMainHandlerProvider, this.providesBroadcastDispatcherProvider, this.tunerServiceImplProvider, this.provideCommandQueueProvider));
        this.shortcutKeyDispatcherProvider = DoubleCheck.provider(ShortcutKeyDispatcher_Factory.create(this.provideContextProvider, this.provideDividerProvider, this.provideRecentsProvider));
        this.sizeCompatModeActivityControllerProvider = DoubleCheck.provider(SizeCompatModeActivityController_Factory.create(this.provideContextProvider, this.provideActivityManagerWrapperProvider, this.provideCommandQueueProvider));
        this.sliceBroadcastRelayHandlerProvider = DoubleCheck.provider(SliceBroadcastRelayHandler_Factory.create(this.provideContextProvider, this.providesBroadcastDispatcherProvider));
        this.systemActionsProvider = DoubleCheck.provider(SystemActions_Factory.create(this.provideContextProvider));
        this.themeOverlayControllerProvider = DoubleCheck.provider(ThemeOverlayController_Factory.create(this.provideContextProvider, this.providesBroadcastDispatcherProvider, this.provideBgHandlerProvider));
        this.toastUIProvider = DoubleCheck.provider(ToastUI_Factory.create(this.provideContextProvider, this.provideCommandQueueProvider));
        this.tvStatusBarProvider = DoubleCheck.provider(TvStatusBar_Factory.create(this.provideContextProvider, this.provideCommandQueueProvider, this.assistManagerProvider));
        this.volumeUIProvider = DoubleCheck.provider(VolumeUI_Factory.create(this.provideContextProvider, this.volumeDialogComponentProvider));
        this.windowMagnificationProvider = DoubleCheck.provider(WindowMagnification_Factory.create(this.provideContextProvider, this.provideMainHandlerProvider));
        this.expandInfoControllerImplProvider = DoubleCheck.provider(ExpandInfoControllerImpl_Factory.create(this.provideContextProvider, this.provideBackgroundExecutorProvider));
        this.superSaveModeControllerProvider = DoubleCheck.provider(SuperSaveModeController_Factory.create(this.provideContextProvider, this.settingsObserverImplProvider));
        this.controlCenterActivityStarterProvider = DoubleCheck.provider(ControlCenterActivityStarter_Factory.create(this.provideContextProvider));
        this.qSTileHostProvider = new DelegateFactory();
        Provider<SlaveWifiHelper> provider17 = DoubleCheck.provider(SlaveWifiHelper_Factory.create(this.provideContextProvider));
        this.slaveWifiHelperProvider = provider17;
        this.wifiTileProvider = WifiTile_Factory.create(this.qSTileHostProvider, this.networkControllerImplProvider, this.activityStarterDelegateProvider, provider17);
        this.bluetoothTileProvider = BluetoothTile_Factory.create(this.qSTileHostProvider, this.bluetoothControllerImplProvider, this.activityStarterDelegateProvider);
        this.cellularTileProvider = CellularTile_Factory.create(this.qSTileHostProvider, this.networkControllerImplProvider, this.activityStarterDelegateProvider);
        this.dndTileProvider = DndTile_Factory.create(this.qSTileHostProvider, this.zenModeControllerImplProvider, this.activityStarterDelegateProvider, this.providesBroadcastDispatcherProvider, this.provideSharePreferencesProvider);
        this.colorInversionTileProvider = ColorInversionTile_Factory.create(this.qSTileHostProvider);
        this.airplaneModeTileProvider = AirplaneModeTile_Factory.create(this.qSTileHostProvider, this.activityStarterDelegateProvider, this.providesBroadcastDispatcherProvider);
        Provider<ManagedProfileControllerImpl> provider18 = DoubleCheck.provider(ManagedProfileControllerImpl_Factory.create(this.provideContextProvider, this.providesBroadcastDispatcherProvider));
        this.managedProfileControllerImplProvider = provider18;
        this.workModeTileProvider = WorkModeTile_Factory.create(this.qSTileHostProvider, provider18);
        this.rotationLockTileProvider = RotationLockTile_Factory.create(this.qSTileHostProvider, this.rotationLockControllerImplProvider);
        Provider<MiuiFlashlightControllerImpl> provider19 = DoubleCheck.provider(MiuiFlashlightControllerImpl_Factory.create(this.provideContextProvider));
        this.miuiFlashlightControllerImplProvider = provider19;
        this.flashlightTileProvider = FlashlightTile_Factory.create(this.qSTileHostProvider, provider19);
        this.locationTileProvider = LocationTile_Factory.create(this.qSTileHostProvider, this.locationControllerImplProvider, this.keyguardStateControllerImplProvider, this.activityStarterDelegateProvider);
        this.castTileProvider = CastTile_Factory.create(this.qSTileHostProvider, this.castControllerImplProvider, this.keyguardStateControllerImplProvider, this.networkControllerImplProvider, this.activityStarterDelegateProvider);
        this.hotspotTileProvider = HotspotTile_Factory.create(this.qSTileHostProvider, this.hotspotControllerImplProvider, this.provideDataSaverControllerProvider);
        this.userTileProvider = UserTile_Factory.create(this.qSTileHostProvider, this.userSwitcherControllerProvider, this.userInfoControllerImplProvider);
        this.batterySaverTileProvider = BatterySaverTile_Factory.create(this.qSTileHostProvider, this.provideBatteryControllerProvider);
        this.dataSaverTileProvider = DataSaverTile_Factory.create(this.qSTileHostProvider, this.networkControllerImplProvider);
        this.nightDisplayTileProvider = NightDisplayTile_Factory.create(this.qSTileHostProvider, this.locationControllerImplProvider);
        this.nfcTileProvider = NfcTile_Factory.create(this.qSTileHostProvider, this.providesBroadcastDispatcherProvider);
        this.memoryTileProvider = GarbageMonitor_MemoryTile_Factory.create(this.qSTileHostProvider, this.garbageMonitorProvider, this.activityStarterDelegateProvider);
        this.uiModeNightTileProvider = UiModeNightTile_Factory.create(this.qSTileHostProvider, this.provideConfigurationControllerProvider, this.provideBatteryControllerProvider, this.locationControllerImplProvider);
        this.screenRecordTileProvider = ScreenRecordTile_Factory.create(this.qSTileHostProvider, this.recordingControllerProvider, this.keyguardDismissUtilProvider);
        this.autoBrightnessTileProvider = AutoBrightnessTile_Factory.create(this.qSTileHostProvider);
        Provider<Looper> provider20 = DoubleCheck.provider(ControlCenterConcurrencyModule_ProvideCCBgLooperFactory.create());
        this.provideCCBgLooperProvider = provider20;
        Provider<Executor> provider21 = DoubleCheck.provider(ControlCenterConcurrencyModule_ProvideCCBackgroundExecutorFactory.create(provider20));
        this.provideCCBackgroundExecutorProvider = provider21;
        DriveModeControllerImpl_Factory create6 = DriveModeControllerImpl_Factory.create(this.provideContextProvider, this.provideCCBgLooperProvider, provider21, this.provideMainExecutorProvider);
        this.driveModeControllerImplProvider = create6;
        this.driveModeTileProvider = DriveModeTile_Factory.create(this.qSTileHostProvider, create6);
        this.editTileProvider = EditTile_Factory.create(this.qSTileHostProvider);
        this.miuiCellularTileProvider = MiuiCellularTile_Factory.create(this.qSTileHostProvider, this.networkControllerImplProvider);
        this.miuiHotspotTileProvider = MiuiHotspotTile_Factory.create(this.qSTileHostProvider, this.hotspotControllerImplProvider, this.networkControllerImplProvider);
        this.muteTileProvider = MuteTile_Factory.create(this.qSTileHostProvider, this.zenModeControllerImplProvider);
    }

    private void initialize5(Builder builder) {
        this.nightModeTileProvider = NightModeTile_Factory.create(this.qSTileHostProvider);
        PaperModeControllerImpl_Factory create = PaperModeControllerImpl_Factory.create(this.provideContextProvider, this.provideCCBgLooperProvider, this.providesBroadcastDispatcherProvider);
        this.paperModeControllerImplProvider = create;
        this.paperModeTileProvider = PaperModeTile_Factory.create(this.qSTileHostProvider, create);
        this.powerModeTileProvider = PowerModeTile_Factory.create(this.qSTileHostProvider);
        this.powerSaverExtremeTileProvider = PowerSaverExtremeTile_Factory.create(this.qSTileHostProvider);
        this.powerSaverTileProvider = PowerSaverTile_Factory.create(this.qSTileHostProvider);
        this.quietModeTileProvider = QuietModeTile_Factory.create(this.qSTileHostProvider, this.zenModeControllerImplProvider, this.provideSharePreferencesProvider);
        this.screenButtonTileProvider = ScreenButtonTile_Factory.create(this.qSTileHostProvider);
        this.screenLockTileProvider = ScreenLockTile_Factory.create(this.qSTileHostProvider);
        this.screenShotTileProvider = ScreenShotTile_Factory.create(this.qSTileHostProvider);
        this.syncTileProvider = SyncTile_Factory.create(this.qSTileHostProvider);
        this.vibrateTileProvider = VibrateTile_Factory.create(this.qSTileHostProvider, this.zenModeControllerImplProvider);
        MiuiAirplaneModeTile_Factory create2 = MiuiAirplaneModeTile_Factory.create(this.qSTileHostProvider, this.networkControllerImplProvider);
        this.miuiAirplaneModeTileProvider = create2;
        Provider<QSFactoryInjectorImpl> provider = DoubleCheck.provider(QSFactoryInjectorImpl_Factory.create(this.qSTileHostProvider, this.autoBrightnessTileProvider, this.driveModeTileProvider, this.editTileProvider, this.miuiCellularTileProvider, this.miuiHotspotTileProvider, this.muteTileProvider, this.nightModeTileProvider, this.paperModeTileProvider, this.powerModeTileProvider, this.powerSaverExtremeTileProvider, this.powerSaverTileProvider, this.quietModeTileProvider, this.screenButtonTileProvider, this.screenLockTileProvider, this.screenShotTileProvider, this.syncTileProvider, this.vibrateTileProvider, create2, this.locationTileProvider));
        this.qSFactoryInjectorImplProvider = provider;
        this.qSFactoryImplProvider = DoubleCheck.provider(QSFactoryImpl_Factory.create(this.qSTileHostProvider, this.wifiTileProvider, this.bluetoothTileProvider, this.cellularTileProvider, this.dndTileProvider, this.colorInversionTileProvider, this.airplaneModeTileProvider, this.workModeTileProvider, this.rotationLockTileProvider, this.flashlightTileProvider, this.locationTileProvider, this.castTileProvider, this.hotspotTileProvider, this.userTileProvider, this.batterySaverTileProvider, this.dataSaverTileProvider, this.nightDisplayTileProvider, this.nfcTileProvider, this.memoryTileProvider, this.uiModeNightTileProvider, this.screenRecordTileProvider, provider));
        this.builderProvider5 = AutoAddTracker_Builder_Factory.create(this.provideContextProvider);
        Provider<NightDisplayListener> provider2 = DoubleCheck.provider(DependencyProvider_ProvideNightDisplayListenerFactory.create(builder.dependencyProvider, this.provideContextProvider, this.provideBgHandlerProvider));
        this.provideNightDisplayListenerProvider = provider2;
        this.autoTileManagerProvider = AutoTileManager_Factory.create(this.provideContextProvider, this.builderProvider5, this.qSTileHostProvider, this.provideBgHandlerProvider, this.hotspotControllerImplProvider, this.provideDataSaverControllerProvider, this.managedProfileControllerImplProvider, provider2, this.castControllerImplProvider);
        this.optionalOfStatusBarProvider = PresentJdkOptionalInstanceProvider.of(this.provideStatusBarProvider);
        Provider<LogBuffer> provider3 = DoubleCheck.provider(LogModule_ProvideQuickSettingsLogBufferFactory.create(this.provideLogcatEchoTrackerProvider, this.dumpManagerProvider));
        this.provideQuickSettingsLogBufferProvider = provider3;
        this.qSLoggerProvider = QSLogger_Factory.create(provider3);
        Provider<OldModeController> provider4 = DoubleCheck.provider(OldModeController_Factory.create(this.providesBroadcastDispatcherProvider, this.settingsObserverImplProvider));
        this.oldModeControllerProvider = provider4;
        Provider<MiuiQSTileHostInjector> provider5 = DoubleCheck.provider(MiuiQSTileHostInjector_Factory.create(this.provideContextProvider, this.providePluginManagerProvider, this.tunerServiceImplProvider, this.controlPanelControllerProvider, this.superSaveModeControllerProvider, provider4, this.deviceProvisionedControllerImplProvider, this.provideBgHandlerProvider));
        this.miuiQSTileHostInjectorProvider = provider5;
        DelegateFactory delegateFactory = (DelegateFactory) this.qSTileHostProvider;
        Provider<QSTileHost> provider6 = DoubleCheck.provider(QSTileHost_Factory.create(this.provideContextProvider, this.statusBarIconControllerImplProvider, this.qSFactoryImplProvider, this.provideMainHandlerProvider, this.provideCCBgLooperProvider, this.provideCCBackgroundExecutorProvider, this.providePluginManagerProvider, this.tunerServiceImplProvider, this.autoTileManagerProvider, this.dumpManagerProvider, this.providesBroadcastDispatcherProvider, this.optionalOfStatusBarProvider, this.qSLoggerProvider, this.provideUiEventLoggerProvider, this.statusBarStateControllerImplProvider, provider5, this.controlPanelControllerProvider));
        this.qSTileHostProvider = provider6;
        delegateFactory.setDelegatedProvider(provider6);
        Provider<MiuiNotificationShadePolicy> provider7 = DoubleCheck.provider(MiuiNotificationShadePolicy_Factory.create(this.provideContextProvider, this.provideBgHandlerProvider, this.provideHeadsUpManagerPhoneProvider, this.notificationShadeWindowControllerProvider, this.controlPanelControllerProvider));
        this.miuiNotificationShadePolicyProvider = provider7;
        this.controlPanelWindowManagerProvider = DoubleCheck.provider(ControlPanelWindowManager_Factory.create(this.provideContextProvider, this.provideStatusBarProvider, this.controlPanelControllerProvider, this.provideHeadsUpManagerPhoneProvider, this.statusBarStateControllerImplProvider, provider7));
        this.controlsPluginManagerProvider = DoubleCheck.provider(ControlsPluginManager_Factory.create());
        Provider<MiPlayPluginManager> provider8 = DoubleCheck.provider(MiPlayPluginManager_Factory.create());
        this.miPlayPluginManagerProvider = provider8;
        this.provideControlCenterProvider = DoubleCheck.provider(ControlCenterModule_ProvideControlCenterFactory.create(this.provideContextProvider, this.controlPanelControllerProvider, this.statusBarIconControllerImplProvider, this.expandInfoControllerImplProvider, this.activityStarterDelegateProvider, this.provideCommandQueueProvider, this.injectionInflationControllerProvider, this.superSaveModeControllerProvider, this.controlCenterActivityStarterProvider, this.qSTileHostProvider, this.controlPanelWindowManagerProvider, this.provideStatusBarProvider, this.controlsPluginManagerProvider, provider8, this.providesBroadcastDispatcherProvider, this.provideConfigurationControllerProvider));
        Provider<PackageEventController> provider9 = DoubleCheck.provider(PackageEventController_Factory.create(this.provideContextProvider, this.provideMainHandlerProvider));
        this.packageEventControllerProvider = provider9;
        this.miuiWallpaperZoomOutServiceProvider = DoubleCheck.provider(MiuiWallpaperZoomOutService_Factory.create(this.provideContextProvider, this.provideStatusBarProvider, this.deviceProvisionedControllerImplProvider, this.providesBroadcastDispatcherProvider, provider9));
        this.miuiHeadsUpPolicyProvider = DoubleCheck.provider(MiuiHeadsUpPolicy_Factory.create(this.providesBroadcastDispatcherProvider, this.provideHeadsUpManagerPhoneProvider));
        Provider<PanelExpansionObserver> provider10 = DoubleCheck.provider(PanelExpansionObserver_Factory.create());
        this.panelExpansionObserverProvider = provider10;
        this.miuiGxzwPolicyProvider = DoubleCheck.provider(MiuiGxzwPolicy_Factory.create(provider10, this.statusBarStateControllerImplProvider, this.provideStatusBarProvider));
        Provider<CloudDataManager> provider11 = DoubleCheck.provider(CloudDataManager_Factory.create(this.provideContextProvider, this.dumpManagerProvider));
        this.cloudDataManagerProvider = provider11;
        Provider<NotificationSettingsManager> provider12 = DoubleCheck.provider(NotificationSettingsManager_Factory.create(this.provideContextProvider, provider11));
        this.notificationSettingsManagerProvider = provider12;
        this.notificationFilterControllerProvider = DoubleCheck.provider(NotificationFilterController_Factory.create(this.provideContextProvider, this.provideNotificationListenerProvider, this.provideNotificationEntryManagerProvider, provider12, this.providesBroadcastDispatcherProvider));
        this.notificationAlertControllerProvider = DoubleCheck.provider(NotificationAlertController_Factory.create(this.provideContextProvider, this.provideINotificationManagerProvider, this.provideNotificationEntryManagerProvider, this.notificationGroupManagerProvider, this.statusBarStateControllerImplProvider, this.screenLifecycleProvider, this.zenModeControllerImplProvider, this.settingsManagerProvider, this.notificationLockscreenUserManagerImplProvider, this.statusBarKeyguardViewManagerProvider));
        this.notificationDynamicFpsControllerProvider = DoubleCheck.provider(NotificationDynamicFpsController_Factory.create(this.provideContextProvider, this.provideNotificationEntryManagerProvider, this.provideHeadsUpManagerPhoneProvider, this.provideStatusBarProvider, this.statusBarStateControllerImplProvider, this.screenLifecycleProvider));
        this.notificationCountLimitPolicyProvider = DoubleCheck.provider(NotificationCountLimitPolicy_Factory.create(this.provideNotificationEntryManagerProvider));
        this.miuiRecentProxyProvider = DoubleCheck.provider(MiuiRecentProxy_Factory.create(this.provideContextProvider, this.provideCommandQueueProvider));
        this.orientationPolicyProvider = DoubleCheck.provider(OrientationPolicy_Factory.create(this.provideContextProvider));
        this.notificationPanelNavigationBarCoordinatorProvider = DoubleCheck.provider(NotificationPanelNavigationBarCoordinator_Factory.create(this.provideCommandQueueProvider, this.provideConfigurationControllerProvider, this.provideLightBarControllerProvider));
        this.headsetPolicyProvider = DoubleCheck.provider(HeadsetPolicy_Factory.create(this.provideContextProvider));
        this.miuiFullScreenGestureProxyProvider = DoubleCheck.provider(MiuiFullScreenGestureProxy_Factory.create(this.provideContextProvider, this.provideCommandQueueProvider));
        Provider<EventTracker> provider13 = DoubleCheck.provider(EventTracker_Factory.create(this.provideContextProvider));
        this.eventTrackerProvider = provider13;
        this.codeBlueServiceProvider = DoubleCheck.provider(CodeBlueService_Factory.create(this.provideContextProvider, this.provideBgLooperProvider, this.provideNotificationEntryManagerProvider, provider13));
        this.oLEDScreenHelperProvider = DoubleCheck.provider(OLEDScreenHelper_Factory.create(this.provideContextProvider, this.screenLifecycleProvider, this.provideConfigurationControllerProvider, this.dumpManagerProvider, this.provideNavigationBarControllerProvider, this.superStatusBarViewFactoryProvider));
        this.miuiChargeManagerProvider = DoubleCheck.provider(MiuiChargeManager_Factory.create(this.provideContextProvider));
        this.miuiFaceUnlockManagerProvider = DoubleCheck.provider(MiuiFaceUnlockManager_Factory.create(this.provideContextProvider));
        this.miuiStatusBarConfigurationListenerProvider = DoubleCheck.provider(MiuiStatusBarConfigurationListener_Factory.create(this.provideConfigurationControllerProvider, this.provideContextProvider));
        Provider<MiuiActivityUtil> provider14 = DoubleCheck.provider(MiuiActivityUtil_Factory.create(this.provideContextProvider, this.provideMainHandlerProvider));
        this.miuiActivityUtilProvider = provider14;
        this.miuiVendorServicesProvider = DoubleCheck.provider(MiuiVendorServices_Factory.create(this.provideContextProvider, this.provideBgLooperProvider, this.miuiWallpaperZoomOutServiceProvider, this.miuiHeadsUpPolicyProvider, this.miuiGxzwPolicyProvider, this.notificationFilterControllerProvider, this.notificationAlertControllerProvider, this.notificationDynamicFpsControllerProvider, this.notificationCountLimitPolicyProvider, this.miuiNotificationShadePolicyProvider, this.miuiRecentProxyProvider, this.orientationPolicyProvider, this.notificationPanelNavigationBarCoordinatorProvider, this.headsetPolicyProvider, this.miuiFullScreenGestureProxyProvider, this.codeBlueServiceProvider, this.oLEDScreenHelperProvider, this.miuiChargeManagerProvider, this.provideNotificationEntryManagerProvider, this.miuiFaceUnlockManagerProvider, this.miuiStatusBarConfigurationListenerProvider, provider14));
        MapProviderFactory.Builder builder2 = MapProviderFactory.builder(23);
        builder2.put(AuthController.class, this.authControllerProvider);
        builder2.put(Divider.class, this.provideDividerProvider);
        builder2.put(GarbageMonitor.Service.class, this.serviceProvider);
        builder2.put(GlobalActionsComponent.class, this.globalActionsComponentProvider);
        builder2.put(InstantAppNotifier.class, this.instantAppNotifierProvider);
        builder2.put(KeyguardViewMediator.class, this.newKeyguardViewMediatorProvider);
        builder2.put(LatencyTester.class, this.latencyTesterProvider);
        builder2.put(PipUI.class, this.pipUIProvider);
        builder2.put(PowerUI.class, this.powerUIProvider);
        builder2.put(Recents.class, this.provideRecentsProvider);
        builder2.put(ScreenDecorations.class, this.screenDecorationsProvider);
        builder2.put(ShortcutKeyDispatcher.class, this.shortcutKeyDispatcherProvider);
        builder2.put(SizeCompatModeActivityController.class, this.sizeCompatModeActivityControllerProvider);
        builder2.put(SliceBroadcastRelayHandler.class, this.sliceBroadcastRelayHandlerProvider);
        builder2.put(StatusBar.class, this.provideStatusBarProvider);
        builder2.put(SystemActions.class, this.systemActionsProvider);
        builder2.put(ThemeOverlayController.class, this.themeOverlayControllerProvider);
        builder2.put(ToastUI.class, this.toastUIProvider);
        builder2.put(TvStatusBar.class, this.tvStatusBarProvider);
        builder2.put(VolumeUI.class, this.volumeUIProvider);
        builder2.put(WindowMagnification.class, this.windowMagnificationProvider);
        builder2.put(ControlCenter.class, this.provideControlCenterProvider);
        builder2.put(MiuiVendorServices.class, this.miuiVendorServicesProvider);
        this.mapOfClassOfAndProviderOfSystemUIProvider = builder2.build();
        this.overviewProxyRecentsImplProvider = DoubleCheck.provider(OverviewProxyRecentsImpl_Factory.create(this.optionalOfLazyOfStatusBarProvider, this.optionalOfDividerProvider));
        MapProviderFactory.Builder builder3 = MapProviderFactory.builder(1);
        builder3.put(OverviewProxyRecentsImpl.class, this.overviewProxyRecentsImplProvider);
        this.mapOfClassOfAndProviderOfRecentsImplementationProvider = builder3.build();
        this.actionProxyReceiverProvider = GlobalScreenshot_ActionProxyReceiver_Factory.create(this.optionalOfLazyOfStatusBarProvider);
        MapProviderFactory.Builder builder4 = MapProviderFactory.builder(1);
        builder4.put(GlobalScreenshot.ActionProxyReceiver.class, this.actionProxyReceiverProvider);
        MapProviderFactory build = builder4.build();
        this.mapOfClassOfAndProviderOfBroadcastReceiverProvider = build;
        Provider<ContextComponentResolver> provider15 = DoubleCheck.provider(ContextComponentResolver_Factory.create(this.mapOfClassOfAndProviderOfActivityProvider, this.mapOfClassOfAndProviderOfServiceProvider, this.mapOfClassOfAndProviderOfSystemUIProvider, this.mapOfClassOfAndProviderOfRecentsImplementationProvider, build));
        this.contextComponentResolverProvider = provider15;
        ((DelegateFactory) this.contextComponentResolverProvider).setDelegatedProvider(provider15);
        this.provideAllowNotificationLongPressProvider = DoubleCheck.provider(SystemUIDefaultModule_ProvideAllowNotificationLongPressFactory.create());
        this.securityControllerImplProvider = DoubleCheck.provider(SecurityControllerImpl_Factory.create(this.provideContextProvider, this.provideBgHandlerProvider, this.providesBroadcastDispatcherProvider, this.provideBackgroundExecutorProvider));
        this.statusBarWindowControllerProvider = DoubleCheck.provider(StatusBarWindowController_Factory.create(this.provideContextProvider, this.provideWindowManagerProvider, this.superStatusBarViewFactoryProvider, this.provideResourcesProvider));
        this.fragmentServiceProvider = DoubleCheck.provider(FragmentService_Factory.create(this.systemUIRootComponentProvider, this.provideConfigurationControllerProvider));
        this.accessibilityManagerWrapperProvider = DoubleCheck.provider(AccessibilityManagerWrapper_Factory.create(this.provideContextProvider));
        this.tunablePaddingServiceProvider = DoubleCheck.provider(TunablePadding_TunablePaddingService_Factory.create(this.tunerServiceImplProvider));
        this.uiOffloadThreadProvider = DoubleCheck.provider(UiOffloadThread_Factory.create());
        this.powerNotificationWarningsProvider = DoubleCheck.provider(PowerNotificationWarnings_Factory.create(this.provideContextProvider, this.activityStarterDelegateProvider));
        this.provideNotificationBlockingHelperManagerProvider = DoubleCheck.provider(NotificationsModule_ProvideNotificationBlockingHelperManagerFactory.create(this.provideContextProvider, this.provideNotificationGutsManagerProvider, this.provideNotificationEntryManagerProvider, this.provideMetricsLoggerProvider));
        this.provideSensorPrivacyManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideSensorPrivacyManagerFactory.create(this.provideContextProvider));
        ForegroundServiceLifetimeExtender_Factory create3 = ForegroundServiceLifetimeExtender_Factory.create(this.notificationInteractionTrackerProvider, this.bindSystemClockProvider);
        this.foregroundServiceLifetimeExtenderProvider = create3;
        this.foregroundServiceNotificationListenerProvider = DoubleCheck.provider(ForegroundServiceNotificationListener_Factory.create(this.provideContextProvider, this.foregroundServiceControllerProvider, this.provideNotificationEntryManagerProvider, this.notifPipelineProvider, create3, this.bindSystemClockProvider));
        this.clockManagerProvider = DoubleCheck.provider(ClockManager_Factory.create(this.provideContextProvider, this.injectionInflationControllerProvider, this.providePluginManagerProvider, this.sysuiColorExtractorProvider, this.dockManagerImplProvider, this.providesBroadcastDispatcherProvider));
        this.provideDevicePolicyManagerWrapperProvider = DoubleCheck.provider(DependencyProvider_ProvideDevicePolicyManagerWrapperFactory.create(builder.dependencyProvider));
        this.keyguardSecurityModelProvider = DoubleCheck.provider(KeyguardSecurityModel_Factory.create(this.provideContextProvider));
        this.appIconsManagerProvider = DoubleCheck.provider(AppIconsManager_Factory.create(this.provideContextProvider));
        this.notificationStatProvider = DoubleCheck.provider(NotificationStat_Factory.create(this.provideContextProvider, this.provideNotificationEntryManagerProvider, this.notificationGroupManagerProvider, this.provideHeadsUpManagerPhoneProvider, this.statusBarStateControllerImplProvider, this.keyguardStateControllerImplProvider, this.eventTrackerProvider, this.notificationSettingsManagerProvider, this.provideBatteryControllerProvider));
        this.usbNotificationControllerProvider = DoubleCheck.provider(UsbNotificationController_Factory.create(this.provideContextProvider));
        this.keyguardNotificationControllerProvider = DoubleCheck.provider(KeyguardNotificationController_Factory.create(this.provideContextProvider, this.provideNotificationEntryManagerProvider, this.notificationGroupManagerProvider, this.keyguardStateControllerImplProvider, this.notificationLockscreenUserManagerImplProvider));
        this.notificationBadgeControllerProvider = DoubleCheck.provider(NotificationBadgeController_Factory.create(this.provideContextProvider, this.provideNotificationEntryManagerProvider, this.notificationGroupManagerProvider, this.providesBroadcastDispatcherProvider));
        this.notificationSensitiveControllerProvider = DoubleCheck.provider(NotificationSensitiveController_Factory.create(this.provideContextProvider, this.userSwitcherControllerProvider));
        this.miuiChargeControllerProvider = DoubleCheck.provider(MiuiChargeController_Factory.create(this.provideContextProvider, this.wakefulnessLifecycleProvider));
        this.hapticFeedBackImplProvider = DoubleCheck.provider(HapticFeedBackImpl_Factory.create(this.provideContextProvider));
        this.lockScreenMagazineControllerProvider = DoubleCheck.provider(LockScreenMagazineController_Factory.create(this.provideContextProvider, this.statusBarStateControllerImplProvider));
        this.miuiQuickConnectControllerProvider = DoubleCheck.provider(MiuiQuickConnectController_Factory.create(this.provideContextProvider));
        this.miuiGxzwManagerProvider = DoubleCheck.provider(MiuiGxzwManager_Factory.create(this.provideContextProvider, this.wakefulnessLifecycleProvider));
        this.miuiFastUnlockControllerProvider = DoubleCheck.provider(MiuiFastUnlockController_Factory.create(this.provideContextProvider, this.newKeyguardViewMediatorProvider));
        this.keyguardIndicationInjectorProvider = DoubleCheck.provider(KeyguardIndicationInjector_Factory.create(this.provideContextProvider));
        this.keyguardPanelViewInjectorProvider = DoubleCheck.provider(KeyguardPanelViewInjector_Factory.create(this.provideContextProvider, this.provideStatusBarProvider, this.wakefulnessLifecycleProvider));
        this.keyguardUpdateMonitorInjectorProvider = DoubleCheck.provider(KeyguardUpdateMonitorInjector_Factory.create(this.provideContextProvider, this.superSaveModeControllerProvider));
        this.forceBlackObserverProvider = DoubleCheck.provider(ForceBlackObserver_Factory.create(this.provideContextProvider, this.provideMainHandlerProvider, this.provideBgHandlerProvider));
        this.keyguardClockInjectorProvider = DoubleCheck.provider(KeyguardClockInjector_Factory.create(this.provideContextProvider));
        this.keyguardBottomAreaInjectorProvider = DoubleCheck.provider(KeyguardBottomAreaInjector_Factory.create(this.provideContextProvider, this.dumpManagerProvider));
        Provider<MiuiKeyguardWallpaperControllerImpl> provider16 = DoubleCheck.provider(MiuiKeyguardWallpaperControllerImpl_Factory.create(this.provideContextProvider, this.providesBroadcastDispatcherProvider));
        this.miuiKeyguardWallpaperControllerImplProvider = provider16;
        this.keyguardNegative1PageInjectorProvider = DoubleCheck.provider(KeyguardNegative1PageInjector_Factory.create(this.provideContextProvider, provider16));
        this.keyguardSensorInjectorProvider = DoubleCheck.provider(KeyguardSensorInjector_Factory.create(this.provideContextProvider, this.newKeyguardViewMediatorProvider, this.providePowerManagerProvider, this.keyguardUpdateMonitorProvider, this.wakefulnessLifecycleProvider));
        this.keyguardViewMediatorInjectorProvider = DoubleCheck.provider(KeyguardViewMediatorInjector_Factory.create(this.provideContextProvider, this.providesBroadcastDispatcherProvider, this.statusBarKeyguardViewManagerProvider));
        this.smartDarkObserverProvider = DoubleCheck.provider(SmartDarkObserver_Factory.create(this.provideContextProvider, this.provideMainHandlerProvider, this.provideBgHandlerProvider));
        this.miuiStatusBarPromptControllerProvider = DoubleCheck.provider(MiuiStatusBarPromptController_Factory.create(this.provideContextProvider));
        this.networkSpeedControllerProvider = DoubleCheck.provider(NetworkSpeedController_Factory.create(this.provideContextProvider, this.provideBgLooperProvider));
        this.miuiDripLeftStatusBarIconControllerImplProvider = DoubleCheck.provider(MiuiDripLeftStatusBarIconControllerImpl_Factory.create(this.provideContextProvider));
    }

    private void initialize6(Builder builder) {
        this.wallpaperCommandSenderProvider = DoubleCheck.provider(WallpaperCommandSender_Factory.create());
        this.miuiWallpaperClientProvider = DoubleCheck.provider(MiuiWallpaperClient_Factory.create(this.provideContextProvider, this.wakefulnessLifecycleProvider));
        Provider<ModalRowInflater> provider = DoubleCheck.provider(ModalRowInflater_Factory.create(this.notificationContentInflaterProvider, this.provideNotificationRemoteInputManagerProvider));
        this.modalRowInflaterProvider = provider;
        Provider<ModalController> provider2 = DoubleCheck.provider(ModalController_Factory.create(this.provideContextProvider, this.provideStatusBarProvider, this.statusBarStateControllerImplProvider, this.miPlayPluginManagerProvider, provider, this.provideControlCenterProvider));
        this.modalControllerProvider = provider2;
        this.appMiniWindowManagerProvider = DoubleCheck.provider(AppMiniWindowManager_Factory.create(this.provideContextProvider, this.provideDividerProvider, this.provideHeadsUpManagerPhoneProvider, this.provideMainHandlerProvider, provider2, this.notificationSettingsManagerProvider));
        this.fiveGControllerImplProvider = DoubleCheck.provider(FiveGControllerImpl_Factory.create(this.provideContextProvider));
        this.callStateControllerImplProvider = DoubleCheck.provider(CallStateControllerImpl_Factory.create());
        this.regionControllerProvider = DoubleCheck.provider(RegionController_Factory.create(this.provideContextProvider));
        this.customCarrierObserverProvider = DoubleCheck.provider(CustomCarrierObserver_Factory.create(this.provideContextProvider, this.provideMainHandlerProvider, this.provideBgHandlerProvider));
        this.dualClockObserverProvider = DoubleCheck.provider(DualClockObserver_Factory.create());
        this.toggleManagerControllerProvider = DoubleCheck.provider(ToggleManagerController_Factory.create(this.provideContextProvider, this.providesBroadcastDispatcherProvider, this.provideBgHandlerProvider));
        this.demoModeControllerProvider = DoubleCheck.provider(DemoModeController_Factory.create(this.providesBroadcastDispatcherProvider));
        this.slaveWifiSignalControllerProvider = DoubleCheck.provider(SlaveWifiSignalController_Factory.create(this.provideContextProvider, this.provideBgHandlerProvider, this.statusBarIconControllerImplProvider, this.provideMainHandlerProvider, this.providesBroadcastDispatcherProvider));
        this.miuiAlarmControllerImplProvider = DoubleCheck.provider(MiuiAlarmControllerImpl_Factory.create(this.provideContextProvider));
        Provider<LogBuffer> provider3 = DoubleCheck.provider(MiuiLogModule_ProvidePanelViewLogBufferFactory.create(this.provideLogcatEchoTrackerProvider, this.dumpManagerProvider));
        this.providePanelViewLogBufferProvider = provider3;
        this.panelViewLoggerProvider = PanelViewLogger_Factory.create(provider3);
        Provider<SystemUIStat> provider4 = DoubleCheck.provider(SystemUIStat_Factory.create(this.provideContextProvider, this.eventTrackerProvider));
        this.systemUIStatProvider = provider4;
        this.nCSwitchControllerProvider = DoubleCheck.provider(NCSwitchController_Factory.create(this.provideContextProvider, this.statusBarStateControllerImplProvider, this.controlPanelControllerProvider, this.shadeControllerImplProvider, this.provideHeadsUpManagerPhoneProvider, this.provideMainHandlerProvider, this.panelViewLoggerProvider, provider4));
        this.phoneSignalControllerImplProvider = DoubleCheck.provider(PhoneSignalControllerImpl_Factory.create(this.provideContextProvider));
        this.unimportantSdkProvider = DoubleCheck.provider(UnimportantSdk_Factory.create(this.provideContextProvider));
        this.foldNotifControllerProvider = DoubleCheck.provider(FoldNotifController_Factory.create(this.provideContextProvider, this.statusBarStateControllerImplProvider));
        this.contextHolder = builder.contextHolder;
        Provider<MediaHostStatesManager> provider5 = DoubleCheck.provider(MediaHostStatesManager_Factory.create());
        this.mediaHostStatesManagerProvider = provider5;
        this.mediaViewControllerProvider = MediaViewController_Factory.create(this.provideContextProvider, this.provideConfigurationControllerProvider, provider5);
        Provider<RepeatableExecutor> provider6 = DoubleCheck.provider(ConcurrencyModule_ProvideBackgroundRepeatableExecutorFactory.create(this.provideBackgroundDelayableExecutorProvider));
        this.provideBackgroundRepeatableExecutorProvider = provider6;
        this.seekBarViewModelProvider = SeekBarViewModel_Factory.create(provider6);
        this.miuiMediaTransferManagerProvider = DoubleCheck.provider(MiuiMediaTransferManager_Factory.create(this.provideContextProvider, this.controlPanelControllerProvider));
        Provider<LogBuffer> provider7 = DoubleCheck.provider(MiuiLogModule_ProvideMediaControlLogBufferFactory.create(this.provideLogcatEchoTrackerProvider, this.dumpManagerProvider));
        this.provideMediaControlLogBufferProvider = provider7;
        MediaControlLogger_Factory create = MediaControlLogger_Factory.create(provider7);
        this.mediaControlLoggerProvider = create;
        this.miuiMediaControlPanelProvider = MiuiMediaControlPanel_Factory.create(this.provideContextProvider, this.provideBackgroundExecutorProvider, this.activityStarterDelegateProvider, this.mediaViewControllerProvider, this.seekBarViewModelProvider, this.miuiMediaTransferManagerProvider, create);
        this.localMediaManagerFactoryProvider = LocalMediaManagerFactory_Factory.create(this.provideContextProvider, this.provideLocalBluetoothControllerProvider);
        SystemServicesModule_ProvideMediaRouter2ManagerFactory create2 = SystemServicesModule_ProvideMediaRouter2ManagerFactory.create(this.provideContextProvider);
        this.provideMediaRouter2ManagerProvider = create2;
        Provider<MediaDeviceManager> provider8 = DoubleCheck.provider(MediaDeviceManager_Factory.create(this.provideContextProvider, this.localMediaManagerFactoryProvider, create2, this.provideMainExecutorProvider, this.mediaDataManagerProvider, this.dumpManagerProvider));
        this.mediaDeviceManagerProvider = provider8;
        Provider<MediaDataCombineLatest> provider9 = DoubleCheck.provider(MediaDataCombineLatest_Factory.create(this.mediaDataManagerProvider, provider8));
        this.mediaDataCombineLatestProvider = provider9;
        Provider<MediaDataFilter> provider10 = DoubleCheck.provider(MediaDataFilter_Factory.create(provider9, this.providesBroadcastDispatcherProvider, this.mediaResumeListenerProvider, this.mediaDataManagerProvider, this.notificationLockscreenUserManagerImplProvider, this.provideMainExecutorProvider, this.provideNotificationEntryManagerProvider, this.mediaControlLoggerProvider));
        this.mediaDataFilterProvider = provider10;
        Provider<MediaCarouselController> provider11 = DoubleCheck.provider(MediaCarouselController_Factory.create(this.provideContextProvider, this.miuiMediaControlPanelProvider, this.provideVisualStabilityManagerProvider, this.mediaHostStatesManagerProvider, this.activityStarterDelegateProvider, this.provideMainDelayableExecutorProvider, provider10, this.provideConfigurationControllerProvider, this.falsingManagerProxyProvider));
        this.mediaCarouselControllerProvider = provider11;
        this.mediaHierarchyManagerProvider = DoubleCheck.provider(MediaHierarchyManager_Factory.create(this.provideContextProvider, this.statusBarStateControllerImplProvider, this.keyguardStateControllerImplProvider, this.keyguardBypassControllerProvider, provider11, this.notificationLockscreenUserManagerImplProvider, this.wakefulnessLifecycleProvider));
        MediaHost_Factory create3 = MediaHost_Factory.create(MediaHost_MediaHostStateHolder_Factory.create(), this.mediaHierarchyManagerProvider, this.mediaDataFilterProvider, this.mediaHostStatesManagerProvider, this.mediaControlLoggerProvider);
        this.mediaHostProvider = create3;
        this.miuiKeyguardMediaControllerProvider = DoubleCheck.provider(MiuiKeyguardMediaController_Factory.create(create3, this.keyguardBypassControllerProvider, this.statusBarStateControllerImplProvider, this.notificationLockscreenUserManagerImplProvider));
        this.zenModeViewControllerProvider = DoubleCheck.provider(ZenModeViewController_Factory.create(this.zenModeControllerImplProvider, this.keyguardBypassControllerProvider, this.statusBarStateControllerImplProvider, this.notificationLockscreenUserManagerImplProvider, this.notificationRowComponentBuilderProvider));
        Provider<PeopleHubDataSourceImpl> provider12 = DoubleCheck.provider(PeopleHubDataSourceImpl_Factory.create(this.provideNotificationEntryManagerProvider, this.notificationPersonExtractorPluginBoundaryProvider, this.provideUserManagerProvider, this.provideLauncherAppsProvider, this.providePackageManagerProvider, this.provideContextProvider, this.provideNotificationListenerProvider, this.provideBackgroundExecutorProvider, this.provideMainExecutorProvider, this.notificationLockscreenUserManagerImplProvider, this.peopleNotificationIdentifierImplProvider));
        this.peopleHubDataSourceImplProvider = provider12;
        Provider<PeopleHubViewModelFactoryDataSourceImpl> provider13 = DoubleCheck.provider(PeopleHubViewModelFactoryDataSourceImpl_Factory.create(this.activityStarterDelegateProvider, provider12));
        this.peopleHubViewModelFactoryDataSourceImplProvider = provider13;
        this.peopleHubViewAdapterImplProvider = DoubleCheck.provider(PeopleHubViewAdapterImpl_Factory.create(provider13));
        Provider<LogBuffer> provider14 = DoubleCheck.provider(LogModule_ProvideNotificationSectionLogBufferFactory.create(this.provideLogcatEchoTrackerProvider, this.dumpManagerProvider));
        this.provideNotificationSectionLogBufferProvider = provider14;
        this.notificationSectionsLoggerProvider = DoubleCheck.provider(NotificationSectionsLogger_Factory.create(provider14));
        this.provideLatencyTrackerProvider = DoubleCheck.provider(SystemServicesModule_ProvideLatencyTrackerFactory.create(this.provideContextProvider));
        this.provideActivityManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideActivityManagerFactory.create(this.provideContextProvider));
    }

    @Override // com.android.systemui.dagger.SystemUIRootComponent
    public BootCompleteCacheImpl provideBootCacheImpl() {
        return this.bootCompleteCacheImplProvider.get();
    }

    @Override // com.android.systemui.dagger.SystemUIRootComponent
    public ConfigurationController getConfigurationController() {
        return this.provideConfigurationControllerProvider.get();
    }

    @Override // com.android.systemui.dagger.SystemUIRootComponent
    public ContextComponentHelper getContextComponentHelper() {
        return this.contextComponentResolverProvider.get();
    }

    @Override // com.android.systemui.dagger.SystemUIRootComponent
    public DumpManager createDumpManager() {
        return this.dumpManagerProvider.get();
    }

    @Override // com.android.systemui.dagger.SystemUIRootComponent
    public InitController getInitController() {
        return this.initControllerProvider.get();
    }

    @Override // com.android.systemui.dagger.SystemUIRootComponent
    public void inject(SystemUIAppComponentFactory systemUIAppComponentFactory) {
        injectSystemUIAppComponentFactory(systemUIAppComponentFactory);
    }

    @Override // com.android.systemui.dagger.SystemUIRootComponent
    public void inject(KeyguardSliceProvider keyguardSliceProvider) {
        injectKeyguardSliceProvider(keyguardSliceProvider);
    }

    @Override // com.android.systemui.dagger.SystemUIRootComponent
    public Dependency.DependencyInjector createDependency() {
        return new DependencyInjectorImpl();
    }

    @Override // com.android.systemui.dagger.SystemUIRootComponent
    public FragmentService.FragmentCreator createFragmentCreator() {
        return new FragmentCreatorImpl();
    }

    @Override // com.android.systemui.dagger.SystemUIRootComponent
    public InjectionInflationController.ViewCreator createViewCreator() {
        return new ViewCreatorImpl();
    }

    private SystemUIAppComponentFactory injectSystemUIAppComponentFactory(SystemUIAppComponentFactory systemUIAppComponentFactory) {
        SystemUIAppComponentFactory_MembersInjector.injectMComponentHelper(systemUIAppComponentFactory, this.contextComponentResolverProvider.get());
        return systemUIAppComponentFactory;
    }

    private KeyguardSliceProvider injectKeyguardSliceProvider(KeyguardSliceProvider keyguardSliceProvider) {
        KeyguardSliceProvider_MembersInjector.injectMDozeParameters(keyguardSliceProvider, this.dozeParametersProvider.get());
        KeyguardSliceProvider_MembersInjector.injectMZenModeController(keyguardSliceProvider, this.zenModeControllerImplProvider.get());
        KeyguardSliceProvider_MembersInjector.injectMNextAlarmController(keyguardSliceProvider, this.nextAlarmControllerImplProvider.get());
        KeyguardSliceProvider_MembersInjector.injectMAlarmManager(keyguardSliceProvider, this.provideAlarmManagerProvider.get());
        KeyguardSliceProvider_MembersInjector.injectMContentResolver(keyguardSliceProvider, this.provideContentResolverProvider.get());
        KeyguardSliceProvider_MembersInjector.injectMMediaManager(keyguardSliceProvider, this.provideNotificationMediaManagerProvider.get());
        KeyguardSliceProvider_MembersInjector.injectMStatusBarStateController(keyguardSliceProvider, this.statusBarStateControllerImplProvider.get());
        KeyguardSliceProvider_MembersInjector.injectMKeyguardBypassController(keyguardSliceProvider, this.keyguardBypassControllerProvider.get());
        return keyguardSliceProvider;
    }

    private static <T> Provider<Optional<T>> absentJdkOptionalProvider() {
        return ABSENT_JDK_OPTIONAL_PROVIDER;
    }

    /* access modifiers changed from: private */
    public static final class PresentJdkOptionalLazyProvider<T> implements Provider<Optional<Lazy<T>>> {
        private final Provider<T> delegate;

        private PresentJdkOptionalLazyProvider(Provider<T> provider) {
            Preconditions.checkNotNull(provider);
            this.delegate = provider;
        }

        @Override // javax.inject.Provider
        public Optional<Lazy<T>> get() {
            return Optional.of(DoubleCheck.lazy(this.delegate));
        }

        /* access modifiers changed from: private */
        public static <T> Provider<Optional<Lazy<T>>> of(Provider<T> provider) {
            return new PresentJdkOptionalLazyProvider(provider);
        }
    }

    /* access modifiers changed from: private */
    public static final class PresentJdkOptionalInstanceProvider<T> implements Provider<Optional<T>> {
        private final Provider<T> delegate;

        private PresentJdkOptionalInstanceProvider(Provider<T> provider) {
            Preconditions.checkNotNull(provider);
            this.delegate = provider;
        }

        @Override // javax.inject.Provider
        public Optional<T> get() {
            return Optional.of(this.delegate.get());
        }

        /* access modifiers changed from: private */
        public static <T> Provider<Optional<T>> of(Provider<T> provider) {
            return new PresentJdkOptionalInstanceProvider(provider);
        }
    }

    public static final class Builder {
        private SystemUIFactory.ContextHolder contextHolder;
        private DependencyProvider dependencyProvider;
        private MiuiStatusBarModule miuiStatusBarModule;

        private Builder() {
        }

        public SystemUIRootComponent build() {
            if (this.dependencyProvider == null) {
                this.dependencyProvider = new DependencyProvider();
            }
            if (this.contextHolder != null) {
                if (this.miuiStatusBarModule == null) {
                    this.miuiStatusBarModule = new MiuiStatusBarModule();
                }
                return new DaggerSystemUIRootComponent(this);
            }
            throw new IllegalStateException(SystemUIFactory.ContextHolder.class.getCanonicalName() + " must be set");
        }

        public Builder dependencyProvider(DependencyProvider dependencyProvider2) {
            Preconditions.checkNotNull(dependencyProvider2);
            this.dependencyProvider = dependencyProvider2;
            return this;
        }

        public Builder contextHolder(SystemUIFactory.ContextHolder contextHolder2) {
            Preconditions.checkNotNull(contextHolder2);
            this.contextHolder = contextHolder2;
            return this;
        }
    }

    private final class DependencyInjectorImpl implements Dependency.DependencyInjector {
        private CarrierObserver_Factory carrierObserverProvider;
        private GestureObserver_Factory gestureObserverProvider;
        private MiuiCarrierTextController_Factory miuiCarrierTextControllerProvider;
        private NotificationIconObserver_Factory notificationIconObserverProvider;

        private DependencyInjectorImpl() {
            initialize();
        }

        private void initialize() {
            this.carrierObserverProvider = CarrierObserver_Factory.create(DaggerSystemUIRootComponent.this.provideContextProvider, DaggerSystemUIRootComponent.this.provideMainHandlerProvider, DaggerSystemUIRootComponent.this.provideBgHandlerProvider);
            this.miuiCarrierTextControllerProvider = MiuiCarrierTextController_Factory.create(DaggerSystemUIRootComponent.this.provideContextProvider, DaggerSystemUIRootComponent.this.provideMainHandlerProvider, DaggerSystemUIRootComponent.this.provideBgHandlerProvider);
            this.notificationIconObserverProvider = NotificationIconObserver_Factory.create(DaggerSystemUIRootComponent.this.provideContextProvider, DaggerSystemUIRootComponent.this.provideMainHandlerProvider);
            this.gestureObserverProvider = GestureObserver_Factory.create(DaggerSystemUIRootComponent.this.provideContextProvider, DaggerSystemUIRootComponent.this.provideMainHandlerProvider, DaggerSystemUIRootComponent.this.provideBgHandlerProvider);
        }

        @Override // com.android.systemui.Dependency.DependencyInjector
        public void createSystemUI(Dependency dependency) {
            injectDependency(dependency);
        }

        private Dependency injectDependency(Dependency dependency) {
            Dependency_MembersInjector.injectMDumpManager(dependency, (DumpManager) DaggerSystemUIRootComponent.this.dumpManagerProvider.get());
            Dependency_MembersInjector.injectMActivityStarter(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.activityStarterDelegateProvider));
            Dependency_MembersInjector.injectMBroadcastDispatcher(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.providesBroadcastDispatcherProvider));
            Dependency_MembersInjector.injectMAsyncSensorManager(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.asyncSensorManagerProvider));
            Dependency_MembersInjector.injectMBluetoothController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.bluetoothControllerImplProvider));
            Dependency_MembersInjector.injectMLocationController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.locationControllerImplProvider));
            Dependency_MembersInjector.injectMRotationLockController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.rotationLockControllerImplProvider));
            Dependency_MembersInjector.injectMNetworkController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.networkControllerImplProvider));
            Dependency_MembersInjector.injectMZenModeController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.zenModeControllerImplProvider));
            Dependency_MembersInjector.injectMHotspotController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.hotspotControllerImplProvider));
            Dependency_MembersInjector.injectMCastController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.castControllerImplProvider));
            Dependency_MembersInjector.injectMFlashlightController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.miuiFlashlightControllerImplProvider));
            Dependency_MembersInjector.injectMUserSwitcherController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.userSwitcherControllerProvider));
            Dependency_MembersInjector.injectMUserInfoController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.userInfoControllerImplProvider));
            Dependency_MembersInjector.injectMKeyguardMonitor(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.keyguardStateControllerImplProvider));
            Dependency_MembersInjector.injectMKeyguardUpdateMonitor(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.keyguardUpdateMonitorProvider));
            Dependency_MembersInjector.injectMBatteryController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideBatteryControllerProvider));
            Dependency_MembersInjector.injectMNightDisplayListener(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideNightDisplayListenerProvider));
            Dependency_MembersInjector.injectMManagedProfileController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.managedProfileControllerImplProvider));
            Dependency_MembersInjector.injectMNextAlarmController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.nextAlarmControllerImplProvider));
            Dependency_MembersInjector.injectMDataSaverController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideDataSaverControllerProvider));
            Dependency_MembersInjector.injectMAccessibilityController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.accessibilityControllerProvider));
            Dependency_MembersInjector.injectMDeviceProvisionedController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.deviceProvisionedControllerImplProvider));
            Dependency_MembersInjector.injectMPluginManager(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.providePluginManagerProvider));
            Dependency_MembersInjector.injectMAssistManager(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.assistManagerProvider));
            Dependency_MembersInjector.injectMSecurityController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.securityControllerImplProvider));
            Dependency_MembersInjector.injectMLeakDetector(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideLeakDetectorProvider));
            Dependency_MembersInjector.injectMLeakReporter(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.leakReporterProvider));
            Dependency_MembersInjector.injectMGarbageMonitor(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.garbageMonitorProvider));
            Dependency_MembersInjector.injectMTunerService(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.tunerServiceImplProvider));
            Dependency_MembersInjector.injectMNotificationShadeWindowController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.notificationShadeWindowControllerProvider));
            Dependency_MembersInjector.injectMTempStatusBarWindowController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.statusBarWindowControllerProvider));
            Dependency_MembersInjector.injectMDarkIconDispatcher(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.darkIconDispatcherImplProvider));
            Dependency_MembersInjector.injectMConfigurationController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideConfigurationControllerProvider));
            Dependency_MembersInjector.injectMStatusBarIconController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.statusBarIconControllerImplProvider));
            Dependency_MembersInjector.injectMScreenLifecycle(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.screenLifecycleProvider));
            Dependency_MembersInjector.injectMWakefulnessLifecycle(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.wakefulnessLifecycleProvider));
            Dependency_MembersInjector.injectMFragmentService(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.fragmentServiceProvider));
            Dependency_MembersInjector.injectMExtensionController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.extensionControllerImplProvider));
            Dependency_MembersInjector.injectMPluginDependencyProvider(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.pluginDependencyProvider));
            Dependency_MembersInjector.injectMLocalBluetoothManager(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideLocalBluetoothControllerProvider));
            Dependency_MembersInjector.injectMVolumeDialogController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.volumeDialogControllerImplProvider));
            Dependency_MembersInjector.injectMMetricsLogger(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideMetricsLoggerProvider));
            Dependency_MembersInjector.injectMAccessibilityManagerWrapper(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.accessibilityManagerWrapperProvider));
            Dependency_MembersInjector.injectMSysuiColorExtractor(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.sysuiColorExtractorProvider));
            Dependency_MembersInjector.injectMTunablePaddingService(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.tunablePaddingServiceProvider));
            Dependency_MembersInjector.injectMForegroundServiceController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.foregroundServiceControllerProvider));
            Dependency_MembersInjector.injectMUiOffloadThread(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.uiOffloadThreadProvider));
            Dependency_MembersInjector.injectMWarningsUI(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.powerNotificationWarningsProvider));
            Dependency_MembersInjector.injectMLightBarController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideLightBarControllerProvider));
            Dependency_MembersInjector.injectMIWindowManager(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideIWindowManagerProvider));
            Dependency_MembersInjector.injectMOverviewProxyService(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.overviewProxyServiceProvider));
            Dependency_MembersInjector.injectMNavBarModeController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.navigationModeControllerProvider));
            Dependency_MembersInjector.injectMEnhancedEstimates(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.enhancedEstimatesImplProvider));
            Dependency_MembersInjector.injectMVibratorHelper(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.vibratorHelperProvider));
            Dependency_MembersInjector.injectMIStatusBarService(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideIStatusBarServiceProvider));
            Dependency_MembersInjector.injectMDisplayMetrics(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideDisplayMetricsProvider));
            Dependency_MembersInjector.injectMLockscreenGestureLogger(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.lockscreenGestureLoggerProvider));
            Dependency_MembersInjector.injectMKeyguardEnvironment(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.keyguardEnvironmentImplProvider));
            Dependency_MembersInjector.injectMShadeController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.shadeControllerImplProvider));
            Dependency_MembersInjector.injectMNotificationRemoteInputManagerCallback(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.statusBarRemoteInputCallbackProvider));
            Dependency_MembersInjector.injectMAppOpsController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.appOpsControllerImplProvider));
            Dependency_MembersInjector.injectMNavigationBarController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideNavigationBarControllerProvider));
            Dependency_MembersInjector.injectMStatusBarStateController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.statusBarStateControllerImplProvider));
            Dependency_MembersInjector.injectMNotificationLockscreenUserManager(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.notificationLockscreenUserManagerImplProvider));
            Dependency_MembersInjector.injectMNotificationGroupAlertTransferHelper(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideNotificationGroupAlertTransferHelperProvider));
            Dependency_MembersInjector.injectMNotificationGroupManager(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.notificationGroupManagerProvider));
            Dependency_MembersInjector.injectMVisualStabilityManager(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideVisualStabilityManagerProvider));
            Dependency_MembersInjector.injectMNotificationGutsManager(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideNotificationGutsManagerProvider));
            Dependency_MembersInjector.injectMNotificationMediaManager(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideNotificationMediaManagerProvider));
            Dependency_MembersInjector.injectMNotificationBlockingHelperManager(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideNotificationBlockingHelperManagerProvider));
            Dependency_MembersInjector.injectMNotificationRemoteInputManager(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideNotificationRemoteInputManagerProvider));
            Dependency_MembersInjector.injectMSmartReplyConstants(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.smartReplyConstantsProvider));
            Dependency_MembersInjector.injectMNotificationListener(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideNotificationListenerProvider));
            Dependency_MembersInjector.injectMNotificationLogger(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideNotificationLoggerProvider));
            Dependency_MembersInjector.injectMNotificationViewHierarchyManager(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideNotificationViewHierarchyManagerProvider));
            Dependency_MembersInjector.injectMNotificationFilter(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.notificationFilterProvider));
            Dependency_MembersInjector.injectMKeyguardDismissUtil(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.keyguardDismissUtilProvider));
            Dependency_MembersInjector.injectMSmartReplyController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideSmartReplyControllerProvider));
            Dependency_MembersInjector.injectMRemoteInputQuickSettingsDisabler(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.remoteInputQuickSettingsDisablerProvider));
            Dependency_MembersInjector.injectMBubbleController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.newBubbleControllerProvider));
            Dependency_MembersInjector.injectMNotificationEntryManager(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideNotificationEntryManagerProvider));
            Dependency_MembersInjector.injectMSensorPrivacyManager(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideSensorPrivacyManagerProvider));
            Dependency_MembersInjector.injectMAutoHideController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideAutoHideControllerProvider));
            Dependency_MembersInjector.injectMForegroundServiceNotificationListener(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.foregroundServiceNotificationListenerProvider));
            Dependency_MembersInjector.injectMBgLooper(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideBgLooperProvider));
            Dependency_MembersInjector.injectMBgHandler(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideBgHandlerProvider));
            Dependency_MembersInjector.injectMMainLooper(dependency, DoubleCheck.lazy(ConcurrencyModule_ProvideMainLooperFactory.create()));
            Dependency_MembersInjector.injectMMainHandler(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideMainHandlerProvider));
            Dependency_MembersInjector.injectMTimeTickHandler(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideTimeTickHandlerProvider));
            Dependency_MembersInjector.injectMLeakReportEmail(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideLeakReportEmailProvider));
            Dependency_MembersInjector.injectMClockManager(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.clockManagerProvider));
            Dependency_MembersInjector.injectMActivityManagerWrapper(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideActivityManagerWrapperProvider));
            Dependency_MembersInjector.injectMDevicePolicyManagerWrapper(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideDevicePolicyManagerWrapperProvider));
            Dependency_MembersInjector.injectMPackageManagerWrapper(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.providePackageManagerWrapperProvider));
            Dependency_MembersInjector.injectMSensorPrivacyController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.sensorPrivacyControllerImplProvider));
            Dependency_MembersInjector.injectMDockManager(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.dockManagerImplProvider));
            Dependency_MembersInjector.injectMINotificationManager(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideINotificationManagerProvider));
            Dependency_MembersInjector.injectMSysUiStateFlagsContainer(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideSysUiStateProvider));
            Dependency_MembersInjector.injectMAlarmManager(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideAlarmManagerProvider));
            Dependency_MembersInjector.injectMKeyguardSecurityModel(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.keyguardSecurityModelProvider));
            Dependency_MembersInjector.injectMDozeParameters(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.dozeParametersProvider));
            Dependency_MembersInjector.injectMWallpaperManager(dependency, DoubleCheck.lazy(SystemServicesModule_ProvideIWallPaperManagerFactory.create()));
            Dependency_MembersInjector.injectMCommandQueue(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideCommandQueueProvider));
            Dependency_MembersInjector.injectMRecents(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideRecentsProvider));
            Dependency_MembersInjector.injectMStatusBar(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideStatusBarProvider));
            Dependency_MembersInjector.injectMDisplayController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.displayControllerProvider));
            Dependency_MembersInjector.injectMSystemWindows(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.systemWindowsProvider));
            Dependency_MembersInjector.injectMDisplayImeController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.displayImeControllerProvider));
            Dependency_MembersInjector.injectMRecordingController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.recordingControllerProvider));
            Dependency_MembersInjector.injectMProtoTracer(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.protoTracerProvider));
            Dependency_MembersInjector.injectMDivider(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideDividerProvider));
            Dependency_MembersInjector.injectMSettingsManager(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.settingsManagerProvider));
            Dependency_MembersInjector.injectMCodeBlueService(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.codeBlueServiceProvider));
            Dependency_MembersInjector.injectMCloudDataManager(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.cloudDataManagerProvider));
            Dependency_MembersInjector.injectMEventTracker(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.eventTrackerProvider));
            Dependency_MembersInjector.injectMAppIconsManager(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.appIconsManagerProvider));
            Dependency_MembersInjector.injectMNotificationStat(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.notificationStatProvider));
            Dependency_MembersInjector.injectMUsbNotificationController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.usbNotificationControllerProvider));
            Dependency_MembersInjector.injectMKeyguardNotificationHelper(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.keyguardNotificationControllerProvider));
            Dependency_MembersInjector.injectMNotificationSettingsManager(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.notificationSettingsManagerProvider));
            Dependency_MembersInjector.injectMNotificationBadgeController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.notificationBadgeControllerProvider));
            Dependency_MembersInjector.injectMNotificationSensitiveController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.notificationSensitiveControllerProvider));
            Dependency_MembersInjector.injectMMiuiChargeManager(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.miuiChargeManagerProvider));
            Dependency_MembersInjector.injectMMiuiChargeController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.miuiChargeControllerProvider));
            Dependency_MembersInjector.injectMMiuihapticFeedBack(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.hapticFeedBackImplProvider));
            Dependency_MembersInjector.injectMContentObserver(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.settingsObserverImplProvider));
            Dependency_MembersInjector.injectMKeyguardIndicationController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.keyguardIndicationControllerProvider));
            Dependency_MembersInjector.injectMLockScreenMagazineController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.lockScreenMagazineControllerProvider));
            Dependency_MembersInjector.injectMMiuiQuickConnectController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.miuiQuickConnectControllerProvider));
            Dependency_MembersInjector.injectMMiuiFaceUnlockManager(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.miuiFaceUnlockManagerProvider));
            Dependency_MembersInjector.injectMMiuiGxzwManager(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.miuiGxzwManagerProvider));
            Dependency_MembersInjector.injectMMiuiFastUnlockController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.miuiFastUnlockControllerProvider));
            Dependency_MembersInjector.injectMKeyguardIndicationInjector(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.keyguardIndicationInjectorProvider));
            Dependency_MembersInjector.injectMKeyguardNotificationInjector(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.keyguardPanelViewInjectorProvider));
            Dependency_MembersInjector.injectMKeyguardUpdateMonitorInjector(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.keyguardUpdateMonitorInjectorProvider));
            Dependency_MembersInjector.injectMDozeServiceHost(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.miuiDozeServiceHostProvider));
            Dependency_MembersInjector.injectMForceBlackObserver(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.forceBlackObserverProvider));
            Dependency_MembersInjector.injectMKeyguardClockInjector(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.keyguardClockInjectorProvider));
            Dependency_MembersInjector.injectMKeyguardBottomAreaInjector(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.keyguardBottomAreaInjectorProvider));
            Dependency_MembersInjector.injectMKeyguardNegative1PageInjector(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.keyguardNegative1PageInjectorProvider));
            Dependency_MembersInjector.injectMKeyguardSensorInjector(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.keyguardSensorInjectorProvider));
            Dependency_MembersInjector.injectMKeyguardViewMediatorInjector(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.keyguardViewMediatorInjectorProvider));
            Dependency_MembersInjector.injectMSmartDarkObserver(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.smartDarkObserverProvider));
            Dependency_MembersInjector.injectMMiuiStatusBarPromptController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.miuiStatusBarPromptControllerProvider));
            Dependency_MembersInjector.injectMNetworkSpeedController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.networkSpeedControllerProvider));
            Dependency_MembersInjector.injectMMiuiDripLeftStatusBarIconControllerImpl(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.miuiDripLeftStatusBarIconControllerImplProvider));
            Dependency_MembersInjector.injectMMiuiKeyguardWallpaperControllerImpl(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.miuiKeyguardWallpaperControllerImplProvider));
            Dependency_MembersInjector.injectMUpdateWallpaperCommand(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.wallpaperCommandSenderProvider));
            Dependency_MembersInjector.injectMMiuiWallpaperClient(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.miuiWallpaperClientProvider));
            Dependency_MembersInjector.injectMControlPanelController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.controlPanelControllerProvider));
            Dependency_MembersInjector.injectMControlPanelWindowManager(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.controlPanelWindowManagerProvider));
            Dependency_MembersInjector.injectMControlCenterActivityStarter(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.controlCenterActivityStarterProvider));
            Dependency_MembersInjector.injectMExpandInfoController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.expandInfoControllerImplProvider));
            Dependency_MembersInjector.injectMControlsPluginManager(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.controlsPluginManagerProvider));
            Dependency_MembersInjector.injectMMiPlayPluginManager(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.miPlayPluginManagerProvider));
            Dependency_MembersInjector.injectMAppMiniWindowManager(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.appMiniWindowManagerProvider));
            Dependency_MembersInjector.injectMModalController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.modalControllerProvider));
            Dependency_MembersInjector.injectMFiveGControllerImpl(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.fiveGControllerImplProvider));
            Dependency_MembersInjector.injectMCallStateController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.callStateControllerImplProvider));
            Dependency_MembersInjector.injectMRegionController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.regionControllerProvider));
            Dependency_MembersInjector.injectMCustomCarrierObserver(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.customCarrierObserverProvider));
            Dependency_MembersInjector.injectMCarrierObserver(dependency, DoubleCheck.lazy(this.carrierObserverProvider));
            Dependency_MembersInjector.injectMMiuiCarrierTextController(dependency, DoubleCheck.lazy(this.miuiCarrierTextControllerProvider));
            Dependency_MembersInjector.injectMNotificationIconObserver(dependency, DoubleCheck.lazy(this.notificationIconObserverProvider));
            Dependency_MembersInjector.injectMDualClockObserver(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.dualClockObserverProvider));
            Dependency_MembersInjector.injectMToggleManagerController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.toggleManagerControllerProvider));
            Dependency_MembersInjector.injectMWallPaperController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.miuiKeyguardWallpaperControllerImplProvider));
            Dependency_MembersInjector.injectMPanelExpansionObserver(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.panelExpansionObserverProvider));
            Dependency_MembersInjector.injectMSuperSaveModeController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.superSaveModeControllerProvider));
            Dependency_MembersInjector.injectMDemoModeController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.demoModeControllerProvider));
            Dependency_MembersInjector.injectMSlaveWifiSignalController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.slaveWifiSignalControllerProvider));
            Dependency_MembersInjector.injectMMiuiAlarmControllerImpl(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.miuiAlarmControllerImplProvider));
            Dependency_MembersInjector.injectMNotificationNavigationCoordinator(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.notificationPanelNavigationBarCoordinatorProvider));
            Dependency_MembersInjector.injectMNCSwitchController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.nCSwitchControllerProvider));
            Dependency_MembersInjector.injectMSystemUIStat(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.systemUIStatProvider));
            Dependency_MembersInjector.injectMPhoneSignalController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.phoneSignalControllerImplProvider));
            Dependency_MembersInjector.injectMGestureObserver(dependency, DoubleCheck.lazy(this.gestureObserverProvider));
            Dependency_MembersInjector.injectMMiuiActivityUtil(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.miuiActivityUtilProvider));
            Dependency_MembersInjector.injectMUnimportantSdk(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.unimportantSdkProvider));
            Dependency_MembersInjector.injectMFoldNotifController(dependency, DoubleCheck.lazy(DaggerSystemUIRootComponent.this.foldNotifControllerProvider));
            return dependency;
        }
    }

    private final class FragmentCreatorImpl implements FragmentService.FragmentCreator {
        private FragmentCreatorImpl() {
        }

        @Override // com.android.systemui.fragments.FragmentService.FragmentCreator
        public NavigationBarFragment createNavigationBarFragment() {
            return new NavigationBarFragment((AccessibilityManagerWrapper) DaggerSystemUIRootComponent.this.accessibilityManagerWrapperProvider.get(), (DeviceProvisionedController) DaggerSystemUIRootComponent.this.deviceProvisionedControllerImplProvider.get(), (MetricsLogger) DaggerSystemUIRootComponent.this.provideMetricsLoggerProvider.get(), (AssistManager) DaggerSystemUIRootComponent.this.assistManagerProvider.get(), (OverviewProxyService) DaggerSystemUIRootComponent.this.overviewProxyServiceProvider.get(), (NavigationModeController) DaggerSystemUIRootComponent.this.navigationModeControllerProvider.get(), (StatusBarStateController) DaggerSystemUIRootComponent.this.statusBarStateControllerImplProvider.get(), (SysUiState) DaggerSystemUIRootComponent.this.provideSysUiStateProvider.get(), (BroadcastDispatcher) DaggerSystemUIRootComponent.this.providesBroadcastDispatcherProvider.get(), (CommandQueue) DaggerSystemUIRootComponent.this.provideCommandQueueProvider.get(), (Divider) DaggerSystemUIRootComponent.this.provideDividerProvider.get(), Optional.of((Recents) DaggerSystemUIRootComponent.this.provideRecentsProvider.get()), DoubleCheck.lazy(DaggerSystemUIRootComponent.this.provideStatusBarProvider), (ShadeController) DaggerSystemUIRootComponent.this.shadeControllerImplProvider.get(), (NotificationRemoteInputManager) DaggerSystemUIRootComponent.this.provideNotificationRemoteInputManagerProvider.get(), (SystemActions) DaggerSystemUIRootComponent.this.systemActionsProvider.get(), DaggerSystemUIRootComponent.this.getMainHandler(), (UiEventLogger) DaggerSystemUIRootComponent.this.provideUiEventLoggerProvider.get());
        }

        @Override // com.android.systemui.fragments.FragmentService.FragmentCreator
        public MiuiQSFragment createQSFragment() {
            return new MiuiQSFragment((RemoteInputQuickSettingsDisabler) DaggerSystemUIRootComponent.this.remoteInputQuickSettingsDisablerProvider.get(), (InjectionInflationController) DaggerSystemUIRootComponent.this.injectionInflationControllerProvider.get(), (QSTileHost) DaggerSystemUIRootComponent.this.qSTileHostProvider.get(), (StatusBarStateController) DaggerSystemUIRootComponent.this.statusBarStateControllerImplProvider.get(), (CommandQueue) DaggerSystemUIRootComponent.this.provideCommandQueueProvider.get(), (ControlPanelController) DaggerSystemUIRootComponent.this.controlPanelControllerProvider.get(), SystemUIFactory_ContextHolder_ProvideContextFactory.proxyProvideContext(DaggerSystemUIRootComponent.this.contextHolder), DaggerSystemUIRootComponent.this.getBackgroundHandler(), DaggerSystemUIRootComponent.this.getMainExecutor(), (StatusBar) DaggerSystemUIRootComponent.this.provideStatusBarProvider.get());
        }
    }

    /* access modifiers changed from: private */
    public final class ViewCreatorImpl implements InjectionInflationController.ViewCreator {
        private ViewCreatorImpl() {
        }

        @Override // com.android.systemui.util.InjectionInflationController.ViewCreator
        public InjectionInflationController.ViewInstanceCreator createInstanceCreator(InjectionInflationController.ViewAttributeProvider viewAttributeProvider) {
            return new ViewInstanceCreatorImpl(viewAttributeProvider);
        }

        private final class ViewInstanceCreatorImpl implements InjectionInflationController.ViewInstanceCreator {
            private InjectionInflationController.ViewAttributeProvider viewAttributeProvider;

            private ViewInstanceCreatorImpl(InjectionInflationController.ViewAttributeProvider viewAttributeProvider2) {
                initialize(viewAttributeProvider2);
            }

            private MiuiNotificationSectionsManager getMiuiNotificationSectionsManager() {
                return new MiuiNotificationSectionsManager((ActivityStarter) DaggerSystemUIRootComponent.this.activityStarterDelegateProvider.get(), (StatusBarStateController) DaggerSystemUIRootComponent.this.statusBarStateControllerImplProvider.get(), (ConfigurationController) DaggerSystemUIRootComponent.this.provideConfigurationControllerProvider.get(), (PeopleHubViewAdapter) DaggerSystemUIRootComponent.this.peopleHubViewAdapterImplProvider.get(), (MiuiKeyguardMediaController) DaggerSystemUIRootComponent.this.miuiKeyguardMediaControllerProvider.get(), (ZenModeViewController) DaggerSystemUIRootComponent.this.zenModeViewControllerProvider.get(), DaggerSystemUIRootComponent.this.getMiuiNotificationSectionsFeatureManager(), (NotificationSectionsLogger) DaggerSystemUIRootComponent.this.notificationSectionsLoggerProvider.get());
            }

            private TileQueryHelper getTileQueryHelper() {
                return new TileQueryHelper(SystemUIFactory_ContextHolder_ProvideContextFactory.proxyProvideContext(DaggerSystemUIRootComponent.this.contextHolder), DaggerSystemUIRootComponent.this.getMainExecutor(), (Executor) DaggerSystemUIRootComponent.this.provideCCBackgroundExecutorProvider.get());
            }

            private CCTileQueryHelper getCCTileQueryHelper() {
                return new CCTileQueryHelper(SystemUIFactory_ContextHolder_ProvideContextFactory.proxyProvideContext(DaggerSystemUIRootComponent.this.contextHolder), DaggerSystemUIRootComponent.this.getMainExecutor(), (Executor) DaggerSystemUIRootComponent.this.provideCCBackgroundExecutorProvider.get());
            }

            private void initialize(InjectionInflationController.ViewAttributeProvider viewAttributeProvider2) {
                Preconditions.checkNotNull(viewAttributeProvider2);
                this.viewAttributeProvider = viewAttributeProvider2;
            }

            @Override // com.android.systemui.util.InjectionInflationController.ViewInstanceCreator
            public QuickStatusBarHeader createQsHeader() {
                return new QuickStatusBarHeader(InjectionInflationController_ViewAttributeProvider_ProvideContextFactory.proxyProvideContext(this.viewAttributeProvider), InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory.proxyProvideAttributeSet(this.viewAttributeProvider), (NextAlarmController) DaggerSystemUIRootComponent.this.nextAlarmControllerImplProvider.get(), (ZenModeController) DaggerSystemUIRootComponent.this.zenModeControllerImplProvider.get(), (StatusBarIconController) DaggerSystemUIRootComponent.this.statusBarIconControllerImplProvider.get(), (ActivityStarter) DaggerSystemUIRootComponent.this.activityStarterDelegateProvider.get(), (CommandQueue) DaggerSystemUIRootComponent.this.provideCommandQueueProvider.get(), (RingerModeTracker) DaggerSystemUIRootComponent.this.ringerModeTrackerImplProvider.get());
            }

            @Override // com.android.systemui.util.InjectionInflationController.ViewInstanceCreator
            public QSFooterImpl createQsFooter() {
                return new QSFooterImpl(InjectionInflationController_ViewAttributeProvider_ProvideContextFactory.proxyProvideContext(this.viewAttributeProvider), InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory.proxyProvideAttributeSet(this.viewAttributeProvider), (ActivityStarter) DaggerSystemUIRootComponent.this.activityStarterDelegateProvider.get(), (UserInfoController) DaggerSystemUIRootComponent.this.userInfoControllerImplProvider.get(), (DeviceProvisionedController) DaggerSystemUIRootComponent.this.deviceProvisionedControllerImplProvider.get());
            }

            @Override // com.android.systemui.util.InjectionInflationController.ViewInstanceCreator
            public NotificationStackScrollLayout createNotificationStackScrollLayout() {
                return new NotificationStackScrollLayout(InjectionInflationController_ViewAttributeProvider_ProvideContextFactory.proxyProvideContext(this.viewAttributeProvider), InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory.proxyProvideAttributeSet(this.viewAttributeProvider), ((Boolean) DaggerSystemUIRootComponent.this.provideAllowNotificationLongPressProvider.get()).booleanValue(), (NotificationRoundnessManager) DaggerSystemUIRootComponent.this.notificationRoundnessManagerProvider.get(), (DynamicPrivacyController) DaggerSystemUIRootComponent.this.dynamicPrivacyControllerProvider.get(), (SysuiStatusBarStateController) DaggerSystemUIRootComponent.this.statusBarStateControllerImplProvider.get(), (HeadsUpManagerPhone) DaggerSystemUIRootComponent.this.provideHeadsUpManagerPhoneProvider.get(), (KeyguardBypassController) DaggerSystemUIRootComponent.this.keyguardBypassControllerProvider.get(), (MiuiKeyguardMediaController) DaggerSystemUIRootComponent.this.miuiKeyguardMediaControllerProvider.get(), (ZenModeViewController) DaggerSystemUIRootComponent.this.zenModeViewControllerProvider.get(), (FalsingManager) DaggerSystemUIRootComponent.this.falsingManagerProxyProvider.get(), (NotificationLockscreenUserManager) DaggerSystemUIRootComponent.this.notificationLockscreenUserManagerImplProvider.get(), (NotificationGutsManager) DaggerSystemUIRootComponent.this.provideNotificationGutsManagerProvider.get(), (ZenModeController) DaggerSystemUIRootComponent.this.zenModeControllerImplProvider.get(), getMiuiNotificationSectionsManager(), (ForegroundServiceSectionController) DaggerSystemUIRootComponent.this.foregroundServiceSectionControllerProvider.get(), (ForegroundServiceDismissalFeatureController) DaggerSystemUIRootComponent.this.foregroundServiceDismissalFeatureControllerProvider.get(), (FeatureFlags) DaggerSystemUIRootComponent.this.featureFlagsProvider.get(), (NotifPipeline) DaggerSystemUIRootComponent.this.notifPipelineProvider.get(), (NotificationEntryManager) DaggerSystemUIRootComponent.this.provideNotificationEntryManagerProvider.get(), (NotifCollection) DaggerSystemUIRootComponent.this.notifCollectionProvider.get(), (UiEventLogger) DaggerSystemUIRootComponent.this.provideUiEventLoggerProvider.get(), (MediaTimeoutListener) DaggerSystemUIRootComponent.this.mediaTimeoutListenerProvider.get(), (MediaDataFilter) DaggerSystemUIRootComponent.this.mediaDataFilterProvider.get());
            }

            @Override // com.android.systemui.util.InjectionInflationController.ViewInstanceCreator
            public NotificationShelf creatNotificationShelf() {
                return new NotificationShelf(InjectionInflationController_ViewAttributeProvider_ProvideContextFactory.proxyProvideContext(this.viewAttributeProvider), InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory.proxyProvideAttributeSet(this.viewAttributeProvider), (KeyguardBypassController) DaggerSystemUIRootComponent.this.keyguardBypassControllerProvider.get());
            }

            @Override // com.android.systemui.util.InjectionInflationController.ViewInstanceCreator
            public KeyguardClockSwitch createKeyguardClockSwitch() {
                return new KeyguardClockSwitch(InjectionInflationController_ViewAttributeProvider_ProvideContextFactory.proxyProvideContext(this.viewAttributeProvider), InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory.proxyProvideAttributeSet(this.viewAttributeProvider), (StatusBarStateController) DaggerSystemUIRootComponent.this.statusBarStateControllerImplProvider.get(), (SysuiColorExtractor) DaggerSystemUIRootComponent.this.sysuiColorExtractorProvider.get(), (ClockManager) DaggerSystemUIRootComponent.this.clockManagerProvider.get());
            }

            @Override // com.android.systemui.util.InjectionInflationController.ViewInstanceCreator
            public KeyguardSliceView createKeyguardSliceView() {
                return new KeyguardSliceView(InjectionInflationController_ViewAttributeProvider_ProvideContextFactory.proxyProvideContext(this.viewAttributeProvider), InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory.proxyProvideAttributeSet(this.viewAttributeProvider), (ActivityStarter) DaggerSystemUIRootComponent.this.activityStarterDelegateProvider.get(), (ConfigurationController) DaggerSystemUIRootComponent.this.provideConfigurationControllerProvider.get(), (TunerService) DaggerSystemUIRootComponent.this.tunerServiceImplProvider.get(), DaggerSystemUIRootComponent.this.getMainResources());
            }

            @Override // com.android.systemui.util.InjectionInflationController.ViewInstanceCreator
            public KeyguardMessageArea createKeyguardMessageArea() {
                return new KeyguardMessageArea(InjectionInflationController_ViewAttributeProvider_ProvideContextFactory.proxyProvideContext(this.viewAttributeProvider), InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory.proxyProvideAttributeSet(this.viewAttributeProvider), (ConfigurationController) DaggerSystemUIRootComponent.this.provideConfigurationControllerProvider.get());
            }

            @Override // com.android.systemui.util.InjectionInflationController.ViewInstanceCreator
            public QSPanel createQSPanel() {
                return new QSPanel(InjectionInflationController_ViewAttributeProvider_ProvideContextFactory.proxyProvideContext(this.viewAttributeProvider), InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory.proxyProvideAttributeSet(this.viewAttributeProvider), (DumpManager) DaggerSystemUIRootComponent.this.dumpManagerProvider.get(), (BroadcastDispatcher) DaggerSystemUIRootComponent.this.providesBroadcastDispatcherProvider.get(), DaggerSystemUIRootComponent.this.getQSLogger(), DaggerSystemUIRootComponent.this.getMediaHost(), (UiEventLogger) DaggerSystemUIRootComponent.this.provideUiEventLoggerProvider.get(), (MiuiBrightnessController) DaggerSystemUIRootComponent.this.miuiBrightnessControllerProvider.get());
            }

            @Override // com.android.systemui.util.InjectionInflationController.ViewInstanceCreator
            public QuickQSPanel createQuickQSPanel() {
                return new QuickQSPanel(InjectionInflationController_ViewAttributeProvider_ProvideContextFactory.proxyProvideContext(this.viewAttributeProvider), InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory.proxyProvideAttributeSet(this.viewAttributeProvider), (DumpManager) DaggerSystemUIRootComponent.this.dumpManagerProvider.get(), (BroadcastDispatcher) DaggerSystemUIRootComponent.this.providesBroadcastDispatcherProvider.get(), DaggerSystemUIRootComponent.this.getQSLogger(), DaggerSystemUIRootComponent.this.getMediaHost(), (UiEventLogger) DaggerSystemUIRootComponent.this.provideUiEventLoggerProvider.get(), (MiuiBrightnessController) DaggerSystemUIRootComponent.this.miuiBrightnessControllerProvider.get());
            }

            @Override // com.android.systemui.util.InjectionInflationController.ViewInstanceCreator
            public MiuiQSCustomizer createQSCustomizer() {
                return new MiuiQSCustomizer(SystemUIFactory_ContextHolder_ProvideContextFactory.proxyProvideContext(DaggerSystemUIRootComponent.this.contextHolder), InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory.proxyProvideAttributeSet(this.viewAttributeProvider), (LightBarController) DaggerSystemUIRootComponent.this.provideLightBarControllerProvider.get(), (KeyguardStateController) DaggerSystemUIRootComponent.this.keyguardStateControllerImplProvider.get(), (ScreenLifecycle) DaggerSystemUIRootComponent.this.screenLifecycleProvider.get(), getTileQueryHelper(), (UiEventLogger) DaggerSystemUIRootComponent.this.provideUiEventLoggerProvider.get());
            }

            @Override // com.android.systemui.util.InjectionInflationController.ViewInstanceCreator
            public QSControlCustomizer createQSControlCustomizer() {
                return new QSControlCustomizer(SystemUIFactory_ContextHolder_ProvideContextFactory.proxyProvideContext(DaggerSystemUIRootComponent.this.contextHolder), InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory.proxyProvideAttributeSet(this.viewAttributeProvider), getCCTileQueryHelper(), (KeyguardUpdateMonitorInjector) DaggerSystemUIRootComponent.this.keyguardUpdateMonitorInjectorProvider.get());
            }

            @Override // com.android.systemui.util.InjectionInflationController.ViewInstanceCreator
            public MiuiQSContainer createMiuiQSContainer() {
                return new MiuiQSContainer(SystemUIFactory_ContextHolder_ProvideContextFactory.proxyProvideContext(DaggerSystemUIRootComponent.this.contextHolder), InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory.proxyProvideAttributeSet(this.viewAttributeProvider), (BroadcastDispatcher) DaggerSystemUIRootComponent.this.providesBroadcastDispatcherProvider.get(), (InjectionInflationController) DaggerSystemUIRootComponent.this.injectionInflationControllerProvider.get(), (TunerService) DaggerSystemUIRootComponent.this.tunerServiceImplProvider.get(), (MiuiBrightnessController) DaggerSystemUIRootComponent.this.miuiBrightnessControllerProvider.get());
            }

            @Override // com.android.systemui.util.InjectionInflationController.ViewInstanceCreator
            public QSFooterDataUsage createQSFooterDataUsage() {
                return new QSFooterDataUsage(SystemUIFactory_ContextHolder_ProvideContextFactory.proxyProvideContext(DaggerSystemUIRootComponent.this.contextHolder), InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory.proxyProvideAttributeSet(this.viewAttributeProvider), (ActivityStarter) DaggerSystemUIRootComponent.this.activityStarterDelegateProvider.get(), ConcurrencyModule_ProvideMainLooperFactory.proxyProvideMainLooper(), (Looper) DaggerSystemUIRootComponent.this.provideCCBgLooperProvider.get());
            }

            @Override // com.android.systemui.util.InjectionInflationController.ViewInstanceCreator
            public ControlCenterPanelView createControlCenterPanelView() {
                return new ControlCenterPanelView(InjectionInflationController_ViewAttributeProvider_ProvideContextFactory.proxyProvideContext(this.viewAttributeProvider), InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory.proxyProvideAttributeSet(this.viewAttributeProvider), ConcurrencyModule_ProvideMainLooperFactory.proxyProvideMainLooper(), (ConfigurationController) DaggerSystemUIRootComponent.this.provideConfigurationControllerProvider.get(), (ControlsPluginManager) DaggerSystemUIRootComponent.this.controlsPluginManagerProvider.get(), (MiPlayPluginManager) DaggerSystemUIRootComponent.this.miPlayPluginManagerProvider.get(), (ControlPanelController) DaggerSystemUIRootComponent.this.controlPanelControllerProvider.get(), (NCSwitchController) DaggerSystemUIRootComponent.this.nCSwitchControllerProvider.get(), (StatusBarStateController) DaggerSystemUIRootComponent.this.statusBarStateControllerImplProvider.get());
            }

            @Override // com.android.systemui.util.InjectionInflationController.ViewInstanceCreator
            public ControlCenterBrightnessView createControlCenterBrightnessView() {
                return new ControlCenterBrightnessView(InjectionInflationController_ViewAttributeProvider_ProvideContextFactory.proxyProvideContext(this.viewAttributeProvider), InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory.proxyProvideAttributeSet(this.viewAttributeProvider), (BroadcastDispatcher) DaggerSystemUIRootComponent.this.providesBroadcastDispatcherProvider.get(), (MiuiBrightnessController) DaggerSystemUIRootComponent.this.miuiBrightnessControllerProvider.get());
            }
        }
    }

    /* access modifiers changed from: private */
    public final class ExpandableNotificationRowComponentBuilder implements ExpandableNotificationRowComponent.Builder {
        private ExpandableNotificationRow expandableNotificationRow;
        private NotificationEntry notificationEntry;
        private Runnable onDismissRunnable;
        private ExpandableNotificationRow.OnExpandClickListener onExpandClickListener;
        private RowContentBindStage rowContentBindStage;

        private ExpandableNotificationRowComponentBuilder() {
        }

        @Override // com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent.Builder
        public ExpandableNotificationRowComponent build() {
            if (this.expandableNotificationRow == null) {
                throw new IllegalStateException(ExpandableNotificationRow.class.getCanonicalName() + " must be set");
            } else if (this.notificationEntry == null) {
                throw new IllegalStateException(NotificationEntry.class.getCanonicalName() + " must be set");
            } else if (this.onDismissRunnable == null) {
                throw new IllegalStateException(Runnable.class.getCanonicalName() + " must be set");
            } else if (this.rowContentBindStage == null) {
                throw new IllegalStateException(RowContentBindStage.class.getCanonicalName() + " must be set");
            } else if (this.onExpandClickListener != null) {
                return new ExpandableNotificationRowComponentImpl(this);
            } else {
                throw new IllegalStateException(ExpandableNotificationRow.OnExpandClickListener.class.getCanonicalName() + " must be set");
            }
        }

        @Override // com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent.Builder
        public ExpandableNotificationRowComponentBuilder expandableNotificationRow(ExpandableNotificationRow expandableNotificationRow2) {
            Preconditions.checkNotNull(expandableNotificationRow2);
            this.expandableNotificationRow = expandableNotificationRow2;
            return this;
        }

        @Override // com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent.Builder
        public ExpandableNotificationRowComponentBuilder notificationEntry(NotificationEntry notificationEntry2) {
            Preconditions.checkNotNull(notificationEntry2);
            this.notificationEntry = notificationEntry2;
            return this;
        }

        @Override // com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent.Builder
        public ExpandableNotificationRowComponentBuilder onDismissRunnable(Runnable runnable) {
            Preconditions.checkNotNull(runnable);
            this.onDismissRunnable = runnable;
            return this;
        }

        @Override // com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent.Builder
        public ExpandableNotificationRowComponentBuilder rowContentBindStage(RowContentBindStage rowContentBindStage2) {
            Preconditions.checkNotNull(rowContentBindStage2);
            this.rowContentBindStage = rowContentBindStage2;
            return this;
        }

        @Override // com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent.Builder
        public ExpandableNotificationRowComponentBuilder onExpandClickListener(ExpandableNotificationRow.OnExpandClickListener onExpandClickListener2) {
            Preconditions.checkNotNull(onExpandClickListener2);
            this.onExpandClickListener = onExpandClickListener2;
            return this;
        }
    }

    private final class ExpandableNotificationRowComponentImpl implements ExpandableNotificationRowComponent {
        private ActivatableNotificationViewController_Factory activatableNotificationViewControllerProvider;
        private Provider<ExpandableNotificationRowController> expandableNotificationRowControllerProvider;
        private Provider<ExpandableNotificationRow> expandableNotificationRowProvider;
        private ExpandableOutlineViewController_Factory expandableOutlineViewControllerProvider;
        private ExpandableViewController_Factory expandableViewControllerProvider;
        private Provider<NotificationEntry> notificationEntryProvider;
        private Provider<Runnable> onDismissRunnableProvider;
        private Provider<ExpandableNotificationRow.OnExpandClickListener> onExpandClickListenerProvider;
        private ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideAppNameFactory provideAppNameProvider;
        private ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideNotificationKeyFactory provideNotificationKeyProvider;
        private ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideStatusBarNotificationFactory provideStatusBarNotificationProvider;
        private Provider<RowContentBindStage> rowContentBindStageProvider;

        private ExpandableNotificationRowComponentImpl(ExpandableNotificationRowComponentBuilder expandableNotificationRowComponentBuilder) {
            initialize(expandableNotificationRowComponentBuilder);
        }

        private void initialize(ExpandableNotificationRowComponentBuilder expandableNotificationRowComponentBuilder) {
            Factory create = InstanceFactory.create(expandableNotificationRowComponentBuilder.expandableNotificationRow);
            this.expandableNotificationRowProvider = create;
            ExpandableViewController_Factory create2 = ExpandableViewController_Factory.create(create);
            this.expandableViewControllerProvider = create2;
            ExpandableOutlineViewController_Factory create3 = ExpandableOutlineViewController_Factory.create(this.expandableNotificationRowProvider, create2);
            this.expandableOutlineViewControllerProvider = create3;
            this.activatableNotificationViewControllerProvider = ActivatableNotificationViewController_Factory.create(this.expandableNotificationRowProvider, create3, DaggerSystemUIRootComponent.this.provideAccessibilityManagerProvider, DaggerSystemUIRootComponent.this.falsingManagerProxyProvider);
            Factory create4 = InstanceFactory.create(expandableNotificationRowComponentBuilder.notificationEntry);
            this.notificationEntryProvider = create4;
            this.provideStatusBarNotificationProvider = ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideStatusBarNotificationFactory.create(create4);
            this.provideAppNameProvider = ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideAppNameFactory.create(DaggerSystemUIRootComponent.this.provideContextProvider, this.provideStatusBarNotificationProvider);
            this.provideNotificationKeyProvider = ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideNotificationKeyFactory.create(this.provideStatusBarNotificationProvider);
            this.rowContentBindStageProvider = InstanceFactory.create(expandableNotificationRowComponentBuilder.rowContentBindStage);
            this.onExpandClickListenerProvider = InstanceFactory.create(expandableNotificationRowComponentBuilder.onExpandClickListener);
            this.onDismissRunnableProvider = InstanceFactory.create(expandableNotificationRowComponentBuilder.onDismissRunnable);
            this.expandableNotificationRowControllerProvider = DoubleCheck.provider(ExpandableNotificationRowController_Factory.create(this.expandableNotificationRowProvider, this.activatableNotificationViewControllerProvider, DaggerSystemUIRootComponent.this.provideNotificationMediaManagerProvider, DaggerSystemUIRootComponent.this.providePluginManagerProvider, DaggerSystemUIRootComponent.this.bindSystemClockProvider, this.provideAppNameProvider, this.provideNotificationKeyProvider, DaggerSystemUIRootComponent.this.keyguardBypassControllerProvider, DaggerSystemUIRootComponent.this.notificationGroupManagerProvider, this.rowContentBindStageProvider, DaggerSystemUIRootComponent.this.provideNotificationLoggerProvider, DaggerSystemUIRootComponent.this.provideHeadsUpManagerPhoneProvider, this.onExpandClickListenerProvider, DaggerSystemUIRootComponent.this.statusBarStateControllerImplProvider, DaggerSystemUIRootComponent.this.provideNotificationGutsManagerProvider, DaggerSystemUIRootComponent.this.provideAllowNotificationLongPressProvider, this.onDismissRunnableProvider, DaggerSystemUIRootComponent.this.falsingManagerProxyProvider, DaggerSystemUIRootComponent.this.peopleNotificationIdentifierImplProvider));
        }

        @Override // com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent
        public ExpandableNotificationRowController getExpandableNotificationRowController() {
            return this.expandableNotificationRowControllerProvider.get();
        }
    }

    /* access modifiers changed from: private */
    public final class StatusBarComponentBuilder implements StatusBarComponent.Builder {
        private NotificationShadeWindowView statusBarWindowView;

        private StatusBarComponentBuilder() {
        }

        @Override // com.android.systemui.statusbar.phone.dagger.StatusBarComponent.Builder
        public StatusBarComponent build() {
            if (this.statusBarWindowView != null) {
                return new StatusBarComponentImpl(this);
            }
            throw new IllegalStateException(NotificationShadeWindowView.class.getCanonicalName() + " must be set");
        }

        @Override // com.android.systemui.statusbar.phone.dagger.StatusBarComponent.Builder
        public StatusBarComponentBuilder statusBarWindowView(NotificationShadeWindowView notificationShadeWindowView) {
            Preconditions.checkNotNull(notificationShadeWindowView);
            this.statusBarWindowView = notificationShadeWindowView;
            return this;
        }
    }

    private final class StatusBarComponentImpl implements StatusBarComponent {
        private FlingAnimationUtils_Builder_Factory builderProvider;
        private Provider<NotificationPanelView> getNotificationPanelViewProvider;
        private Provider<MiuiNotificationPanelViewController> miuiNotificationPanelViewControllerProvider;
        private NotificationShadeWindowView statusBarWindowView;
        private Provider<NotificationShadeWindowView> statusBarWindowViewProvider;

        private StatusBarComponentImpl(StatusBarComponentBuilder statusBarComponentBuilder) {
            initialize(statusBarComponentBuilder);
        }

        private void initialize(StatusBarComponentBuilder statusBarComponentBuilder) {
            this.statusBarWindowView = statusBarComponentBuilder.statusBarWindowView;
            Factory create = InstanceFactory.create(statusBarComponentBuilder.statusBarWindowView);
            this.statusBarWindowViewProvider = create;
            this.getNotificationPanelViewProvider = DoubleCheck.provider(StatusBarViewModule_GetNotificationPanelViewFactory.create(create));
            this.builderProvider = FlingAnimationUtils_Builder_Factory.create(DaggerSystemUIRootComponent.this.provideDisplayMetricsProvider);
            this.miuiNotificationPanelViewControllerProvider = DoubleCheck.provider(MiuiNotificationPanelViewController_Factory.create(this.getNotificationPanelViewProvider, DaggerSystemUIRootComponent.this.injectionInflationControllerProvider, DaggerSystemUIRootComponent.this.notificationWakeUpCoordinatorProvider, DaggerSystemUIRootComponent.this.pulseExpansionHandlerProvider, DaggerSystemUIRootComponent.this.dynamicPrivacyControllerProvider, DaggerSystemUIRootComponent.this.keyguardBypassControllerProvider, DaggerSystemUIRootComponent.this.falsingManagerProxyProvider, DaggerSystemUIRootComponent.this.shadeControllerImplProvider, DaggerSystemUIRootComponent.this.notificationLockscreenUserManagerImplProvider, DaggerSystemUIRootComponent.this.provideNotificationEntryManagerProvider, DaggerSystemUIRootComponent.this.keyguardStateControllerImplProvider, DaggerSystemUIRootComponent.this.statusBarStateControllerImplProvider, DaggerSystemUIRootComponent.this.dozeLogProvider, DaggerSystemUIRootComponent.this.dozeParametersProvider, DaggerSystemUIRootComponent.this.provideCommandQueueProvider, DaggerSystemUIRootComponent.this.vibratorHelperProvider, DaggerSystemUIRootComponent.this.provideLatencyTrackerProvider, DaggerSystemUIRootComponent.this.providePowerManagerProvider, DaggerSystemUIRootComponent.this.provideAccessibilityManagerProvider, DaggerSystemUIRootComponent.this.provideDisplayIdProvider, DaggerSystemUIRootComponent.this.keyguardUpdateMonitorProvider, DaggerSystemUIRootComponent.this.provideMetricsLoggerProvider, DaggerSystemUIRootComponent.this.provideActivityManagerProvider, DaggerSystemUIRootComponent.this.zenModeControllerImplProvider, DaggerSystemUIRootComponent.this.provideConfigurationControllerProvider, this.builderProvider, DaggerSystemUIRootComponent.this.statusBarTouchableRegionManagerProvider, DaggerSystemUIRootComponent.this.conversationNotificationManagerProvider, DaggerSystemUIRootComponent.this.mediaHierarchyManagerProvider, DaggerSystemUIRootComponent.this.biometricUnlockControllerProvider, DaggerSystemUIRootComponent.this.statusBarKeyguardViewManagerProvider, DaggerSystemUIRootComponent.this.controlPanelControllerProvider, DaggerSystemUIRootComponent.this.eventTrackerProvider, DaggerSystemUIRootComponent.this.wakefulnessLifecycleProvider, DaggerSystemUIRootComponent.this.notificationShadeWindowControllerProvider, DaggerSystemUIRootComponent.this.panelViewLoggerProvider));
        }

        @Override // com.android.systemui.statusbar.phone.dagger.StatusBarComponent
        public NotificationShadeWindowViewController getNotificationShadeWindowViewController() {
            return new NotificationShadeWindowViewController((InjectionInflationController) DaggerSystemUIRootComponent.this.injectionInflationControllerProvider.get(), (NotificationWakeUpCoordinator) DaggerSystemUIRootComponent.this.notificationWakeUpCoordinatorProvider.get(), (PulseExpansionHandler) DaggerSystemUIRootComponent.this.pulseExpansionHandlerProvider.get(), (DynamicPrivacyController) DaggerSystemUIRootComponent.this.dynamicPrivacyControllerProvider.get(), (KeyguardBypassController) DaggerSystemUIRootComponent.this.keyguardBypassControllerProvider.get(), (FalsingManager) DaggerSystemUIRootComponent.this.falsingManagerProxyProvider.get(), (PluginManager) DaggerSystemUIRootComponent.this.providePluginManagerProvider.get(), (TunerService) DaggerSystemUIRootComponent.this.tunerServiceImplProvider.get(), (NotificationLockscreenUserManager) DaggerSystemUIRootComponent.this.notificationLockscreenUserManagerImplProvider.get(), (NotificationEntryManager) DaggerSystemUIRootComponent.this.provideNotificationEntryManagerProvider.get(), (KeyguardStateController) DaggerSystemUIRootComponent.this.keyguardStateControllerImplProvider.get(), (SysuiStatusBarStateController) DaggerSystemUIRootComponent.this.statusBarStateControllerImplProvider.get(), (DozeLog) DaggerSystemUIRootComponent.this.dozeLogProvider.get(), (DozeParameters) DaggerSystemUIRootComponent.this.dozeParametersProvider.get(), (CommandQueue) DaggerSystemUIRootComponent.this.provideCommandQueueProvider.get(), (ShadeController) DaggerSystemUIRootComponent.this.shadeControllerImplProvider.get(), (DockManager) DaggerSystemUIRootComponent.this.dockManagerImplProvider.get(), (NotificationShadeDepthController) DaggerSystemUIRootComponent.this.notificationShadeDepthControllerProvider.get(), this.statusBarWindowView, this.miuiNotificationPanelViewControllerProvider.get(), (SuperStatusBarViewFactory) DaggerSystemUIRootComponent.this.superStatusBarViewFactoryProvider.get(), (ControlPanelWindowManager) DaggerSystemUIRootComponent.this.controlPanelWindowManagerProvider.get(), (NCSwitchController) DaggerSystemUIRootComponent.this.nCSwitchControllerProvider.get(), (MiuiKeyguardMediaController) DaggerSystemUIRootComponent.this.miuiKeyguardMediaControllerProvider.get());
        }

        @Override // com.android.systemui.statusbar.phone.dagger.StatusBarComponent
        public StatusBarWindowController getStatusBarWindowController() {
            return (StatusBarWindowController) DaggerSystemUIRootComponent.this.statusBarWindowControllerProvider.get();
        }

        @Override // com.android.systemui.statusbar.phone.dagger.StatusBarComponent
        public MiuiNotificationPanelViewController getNotificationPanelViewController() {
            return this.miuiNotificationPanelViewControllerProvider.get();
        }
    }

    /* access modifiers changed from: private */
    public final class NotificationRowComponentBuilder implements NotificationRowComponent.Builder {
        private ActivatableNotificationView activatableNotificationView;

        private NotificationRowComponentBuilder() {
        }

        @Override // com.android.systemui.statusbar.notification.row.dagger.NotificationRowComponent.Builder
        public NotificationRowComponent build() {
            if (this.activatableNotificationView != null) {
                return new NotificationRowComponentImpl(this);
            }
            throw new IllegalStateException(ActivatableNotificationView.class.getCanonicalName() + " must be set");
        }

        @Override // com.android.systemui.statusbar.notification.row.dagger.NotificationRowComponent.Builder
        public NotificationRowComponentBuilder activatableNotificationView(ActivatableNotificationView activatableNotificationView2) {
            Preconditions.checkNotNull(activatableNotificationView2);
            this.activatableNotificationView = activatableNotificationView2;
            return this;
        }
    }

    private final class NotificationRowComponentImpl implements NotificationRowComponent {
        private ActivatableNotificationView activatableNotificationView;

        private NotificationRowComponentImpl(NotificationRowComponentBuilder notificationRowComponentBuilder) {
            initialize(notificationRowComponentBuilder);
        }

        private ExpandableViewController getExpandableViewController() {
            return new ExpandableViewController(this.activatableNotificationView);
        }

        private ExpandableOutlineViewController getExpandableOutlineViewController() {
            return new ExpandableOutlineViewController(this.activatableNotificationView, getExpandableViewController());
        }

        private void initialize(NotificationRowComponentBuilder notificationRowComponentBuilder) {
            this.activatableNotificationView = notificationRowComponentBuilder.activatableNotificationView;
        }

        @Override // com.android.systemui.statusbar.notification.row.dagger.NotificationRowComponent
        public ActivatableNotificationViewController getActivatableNotificationViewController() {
            return new ActivatableNotificationViewController(this.activatableNotificationView, getExpandableOutlineViewController(), (AccessibilityManager) DaggerSystemUIRootComponent.this.provideAccessibilityManagerProvider.get(), (FalsingManager) DaggerSystemUIRootComponent.this.falsingManagerProxyProvider.get());
        }
    }
}
