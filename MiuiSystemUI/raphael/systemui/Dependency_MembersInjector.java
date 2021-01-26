package com.android.systemui;

import android.app.AlarmManager;
import android.app.INotificationManager;
import android.app.IWallpaperManager;
import android.hardware.SensorPrivacyManager;
import android.hardware.display.NightDisplayListener;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.IWindowManager;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.statusbar.IStatusBarService;
import com.android.keyguard.IPhoneSignalController;
import com.android.keyguard.KeyguardSecurityModel;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.MiuiCarrierTextController;
import com.android.keyguard.MiuiDozeServiceHost;
import com.android.keyguard.MiuiFastUnlockController;
import com.android.keyguard.charge.MiuiChargeController;
import com.android.keyguard.charge.MiuiChargeManager;
import com.android.keyguard.clock.ClockManager;
import com.android.keyguard.faceunlock.MiuiFaceUnlockManager;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.keyguard.injector.KeyguardBottomAreaInjector;
import com.android.keyguard.injector.KeyguardClockInjector;
import com.android.keyguard.injector.KeyguardIndicationInjector;
import com.android.keyguard.injector.KeyguardNegative1PageInjector;
import com.android.keyguard.injector.KeyguardPanelViewInjector;
import com.android.keyguard.injector.KeyguardSensorInjector;
import com.android.keyguard.injector.KeyguardUpdateMonitorInjector;
import com.android.keyguard.injector.KeyguardViewMediatorInjector;
import com.android.keyguard.magazine.LockScreenMagazineController;
import com.android.keyguard.wallpaper.IMiuiKeyguardWallpaperController;
import com.android.keyguard.wallpaper.MiuiKeyguardWallpaperControllerImpl;
import com.android.keyguard.wallpaper.MiuiWallpaperClient;
import com.android.keyguard.wallpaper.WallpaperCommandSender;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.systemui.appops.AppOpsController;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.colorextraction.SysuiColorExtractor;
import com.android.systemui.controlcenter.phone.ControlPanelController;
import com.android.systemui.controlcenter.phone.ControlPanelWindowManager;
import com.android.systemui.controlcenter.phone.ExpandInfoController;
import com.android.systemui.controlcenter.phone.controls.ControlsPluginManager;
import com.android.systemui.controlcenter.policy.ControlCenterActivityStarter;
import com.android.systemui.controlcenter.policy.NCSwitchController;
import com.android.systemui.controlcenter.policy.SuperSaveModeController;
import com.android.systemui.dock.DockManager;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.fragments.FragmentService;
import com.android.systemui.keyguard.ScreenLifecycle;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.model.SysUiState;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.plugins.PluginDependencyProvider;
import com.android.systemui.plugins.VolumeDialogController;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.power.EnhancedEstimates;
import com.android.systemui.power.PowerUI;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.recents.Recents;
import com.android.systemui.screenrecord.RecordingController;
import com.android.systemui.shared.plugins.PluginManager;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.DevicePolicyManagerWrapper;
import com.android.systemui.shared.system.PackageManagerWrapper;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.KeyguardIndicationController;
import com.android.systemui.statusbar.NavigationBarController;
import com.android.systemui.statusbar.NetworkSpeedController;
import com.android.systemui.statusbar.NotificationListener;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.NotificationViewHierarchyManager;
import com.android.systemui.statusbar.SmartReplyController;
import com.android.systemui.statusbar.VibratorHelper;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationFilter;
import com.android.systemui.statusbar.notification.NotificationPanelNavigationBarCoordinator;
import com.android.systemui.statusbar.notification.NotificationSettingsManager;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.notification.analytics.NotificationStat;
import com.android.systemui.statusbar.notification.logging.NotificationLogger;
import com.android.systemui.statusbar.notification.modal.ModalController;
import com.android.systemui.statusbar.notification.policy.AppMiniWindowManager;
import com.android.systemui.statusbar.notification.policy.KeyguardNotificationController;
import com.android.systemui.statusbar.notification.policy.NotificationBadgeController;
import com.android.systemui.statusbar.notification.policy.NotificationSensitiveController;
import com.android.systemui.statusbar.notification.policy.UsbNotificationController;
import com.android.systemui.statusbar.notification.row.NotificationBlockingHelperManager;
import com.android.systemui.statusbar.notification.row.NotificationGutsManager;
import com.android.systemui.statusbar.phone.AutoHideController;
import com.android.systemui.statusbar.phone.DozeParameters;
import com.android.systemui.statusbar.phone.KeyguardDismissUtil;
import com.android.systemui.statusbar.phone.LightBarController;
import com.android.systemui.statusbar.phone.LockscreenGestureLogger;
import com.android.systemui.statusbar.phone.ManagedProfileController;
import com.android.systemui.statusbar.phone.MiuiDripLeftStatusBarIconControllerImpl;
import com.android.systemui.statusbar.phone.MiuiStatusBarPromptController;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.android.systemui.statusbar.phone.NotificationGroupAlertTransferHelper;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.phone.NotificationIconObserver;
import com.android.systemui.statusbar.phone.NotificationShadeWindowController;
import com.android.systemui.statusbar.phone.ShadeController;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.phone.StatusBarWindowController;
import com.android.systemui.statusbar.policy.AccessibilityController;
import com.android.systemui.statusbar.policy.AccessibilityManagerWrapper;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BluetoothController;
import com.android.systemui.statusbar.policy.CallStateControllerImpl;
import com.android.systemui.statusbar.policy.CarrierObserver;
import com.android.systemui.statusbar.policy.CastController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.CustomCarrierObserver;
import com.android.systemui.statusbar.policy.DataSaverController;
import com.android.systemui.statusbar.policy.DemoModeController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.DualClockObserver;
import com.android.systemui.statusbar.policy.ExtensionController;
import com.android.systemui.statusbar.policy.FiveGControllerImpl;
import com.android.systemui.statusbar.policy.FlashlightController;
import com.android.systemui.statusbar.policy.HotspotController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.statusbar.policy.LocationController;
import com.android.systemui.statusbar.policy.MiuiAlarmControllerImpl;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.statusbar.policy.RegionController;
import com.android.systemui.statusbar.policy.RemoteInputQuickSettingsDisabler;
import com.android.systemui.statusbar.policy.RotationLockController;
import com.android.systemui.statusbar.policy.SecurityController;
import com.android.systemui.statusbar.policy.SensorPrivacyController;
import com.android.systemui.statusbar.policy.SlaveWifiSignalController;
import com.android.systemui.statusbar.policy.SmartReplyConstants;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.tracing.ProtoTracer;
import com.android.systemui.tuner.TunablePadding;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.util.leak.GarbageMonitor;
import com.android.systemui.util.leak.LeakDetector;
import com.android.systemui.util.leak.LeakReporter;
import com.android.systemui.util.sensors.AsyncSensorManager;
import com.android.systemui.wm.DisplayController;
import com.android.systemui.wm.DisplayImeController;
import com.android.systemui.wm.SystemWindows;
import com.miui.systemui.CloudDataManager;
import com.miui.systemui.EventTracker;
import com.miui.systemui.SettingsManager;
import com.miui.systemui.SettingsObserver;
import com.miui.systemui.analytics.SystemUIStat;
import com.miui.systemui.graphics.AppIconsManager;
import com.miui.systemui.statusbar.PanelExpansionObserver;
import com.miui.systemui.statusbar.phone.DriveModeObserver;
import com.miui.systemui.statusbar.phone.ForceBlackObserver;
import com.miui.systemui.statusbar.phone.SmartDarkObserver;
import com.miui.systemui.util.HapticFeedBackImpl;
import dagger.Lazy;

public final class Dependency_MembersInjector {
    public static void injectMDumpManager(Dependency dependency, DumpManager dumpManager) {
        dependency.mDumpManager = dumpManager;
    }

    public static void injectMActivityStarter(Dependency dependency, Lazy<ActivityStarter> lazy) {
        dependency.mActivityStarter = lazy;
    }

    public static void injectMBroadcastDispatcher(Dependency dependency, Lazy<BroadcastDispatcher> lazy) {
        dependency.mBroadcastDispatcher = lazy;
    }

    public static void injectMAsyncSensorManager(Dependency dependency, Lazy<AsyncSensorManager> lazy) {
        dependency.mAsyncSensorManager = lazy;
    }

    public static void injectMBluetoothController(Dependency dependency, Lazy<BluetoothController> lazy) {
        dependency.mBluetoothController = lazy;
    }

    public static void injectMLocationController(Dependency dependency, Lazy<LocationController> lazy) {
        dependency.mLocationController = lazy;
    }

    public static void injectMRotationLockController(Dependency dependency, Lazy<RotationLockController> lazy) {
        dependency.mRotationLockController = lazy;
    }

    public static void injectMNetworkController(Dependency dependency, Lazy<NetworkController> lazy) {
        dependency.mNetworkController = lazy;
    }

    public static void injectMZenModeController(Dependency dependency, Lazy<ZenModeController> lazy) {
        dependency.mZenModeController = lazy;
    }

    public static void injectMHotspotController(Dependency dependency, Lazy<HotspotController> lazy) {
        dependency.mHotspotController = lazy;
    }

    public static void injectMCastController(Dependency dependency, Lazy<CastController> lazy) {
        dependency.mCastController = lazy;
    }

    public static void injectMFlashlightController(Dependency dependency, Lazy<FlashlightController> lazy) {
        dependency.mFlashlightController = lazy;
    }

    public static void injectMUserSwitcherController(Dependency dependency, Lazy<UserSwitcherController> lazy) {
        dependency.mUserSwitcherController = lazy;
    }

    public static void injectMUserInfoController(Dependency dependency, Lazy<UserInfoController> lazy) {
        dependency.mUserInfoController = lazy;
    }

    public static void injectMKeyguardMonitor(Dependency dependency, Lazy<KeyguardStateController> lazy) {
        dependency.mKeyguardMonitor = lazy;
    }

    public static void injectMKeyguardUpdateMonitor(Dependency dependency, Lazy<KeyguardUpdateMonitor> lazy) {
        dependency.mKeyguardUpdateMonitor = lazy;
    }

    public static void injectMBatteryController(Dependency dependency, Lazy<BatteryController> lazy) {
        dependency.mBatteryController = lazy;
    }

    public static void injectMNightDisplayListener(Dependency dependency, Lazy<NightDisplayListener> lazy) {
        dependency.mNightDisplayListener = lazy;
    }

    public static void injectMManagedProfileController(Dependency dependency, Lazy<ManagedProfileController> lazy) {
        dependency.mManagedProfileController = lazy;
    }

    public static void injectMNextAlarmController(Dependency dependency, Lazy<NextAlarmController> lazy) {
        dependency.mNextAlarmController = lazy;
    }

    public static void injectMDataSaverController(Dependency dependency, Lazy<DataSaverController> lazy) {
        dependency.mDataSaverController = lazy;
    }

    public static void injectMAccessibilityController(Dependency dependency, Lazy<AccessibilityController> lazy) {
        dependency.mAccessibilityController = lazy;
    }

    public static void injectMDeviceProvisionedController(Dependency dependency, Lazy<DeviceProvisionedController> lazy) {
        dependency.mDeviceProvisionedController = lazy;
    }

    public static void injectMPluginManager(Dependency dependency, Lazy<PluginManager> lazy) {
        dependency.mPluginManager = lazy;
    }

    public static void injectMAssistManager(Dependency dependency, Lazy<AssistManager> lazy) {
        dependency.mAssistManager = lazy;
    }

    public static void injectMSecurityController(Dependency dependency, Lazy<SecurityController> lazy) {
        dependency.mSecurityController = lazy;
    }

    public static void injectMLeakDetector(Dependency dependency, Lazy<LeakDetector> lazy) {
        dependency.mLeakDetector = lazy;
    }

    public static void injectMLeakReporter(Dependency dependency, Lazy<LeakReporter> lazy) {
        dependency.mLeakReporter = lazy;
    }

    public static void injectMGarbageMonitor(Dependency dependency, Lazy<GarbageMonitor> lazy) {
        dependency.mGarbageMonitor = lazy;
    }

    public static void injectMTunerService(Dependency dependency, Lazy<TunerService> lazy) {
        dependency.mTunerService = lazy;
    }

    public static void injectMNotificationShadeWindowController(Dependency dependency, Lazy<NotificationShadeWindowController> lazy) {
        dependency.mNotificationShadeWindowController = lazy;
    }

    public static void injectMTempStatusBarWindowController(Dependency dependency, Lazy<StatusBarWindowController> lazy) {
        dependency.mTempStatusBarWindowController = lazy;
    }

    public static void injectMDarkIconDispatcher(Dependency dependency, Lazy<DarkIconDispatcher> lazy) {
        dependency.mDarkIconDispatcher = lazy;
    }

    public static void injectMConfigurationController(Dependency dependency, Lazy<ConfigurationController> lazy) {
        dependency.mConfigurationController = lazy;
    }

    public static void injectMStatusBarIconController(Dependency dependency, Lazy<StatusBarIconController> lazy) {
        dependency.mStatusBarIconController = lazy;
    }

    public static void injectMScreenLifecycle(Dependency dependency, Lazy<ScreenLifecycle> lazy) {
        dependency.mScreenLifecycle = lazy;
    }

    public static void injectMWakefulnessLifecycle(Dependency dependency, Lazy<WakefulnessLifecycle> lazy) {
        dependency.mWakefulnessLifecycle = lazy;
    }

    public static void injectMFragmentService(Dependency dependency, Lazy<FragmentService> lazy) {
        dependency.mFragmentService = lazy;
    }

    public static void injectMExtensionController(Dependency dependency, Lazy<ExtensionController> lazy) {
        dependency.mExtensionController = lazy;
    }

    public static void injectMPluginDependencyProvider(Dependency dependency, Lazy<PluginDependencyProvider> lazy) {
        dependency.mPluginDependencyProvider = lazy;
    }

    public static void injectMLocalBluetoothManager(Dependency dependency, Lazy<LocalBluetoothManager> lazy) {
        dependency.mLocalBluetoothManager = lazy;
    }

    public static void injectMVolumeDialogController(Dependency dependency, Lazy<VolumeDialogController> lazy) {
        dependency.mVolumeDialogController = lazy;
    }

    public static void injectMMetricsLogger(Dependency dependency, Lazy<MetricsLogger> lazy) {
        dependency.mMetricsLogger = lazy;
    }

    public static void injectMAccessibilityManagerWrapper(Dependency dependency, Lazy<AccessibilityManagerWrapper> lazy) {
        dependency.mAccessibilityManagerWrapper = lazy;
    }

    public static void injectMSysuiColorExtractor(Dependency dependency, Lazy<SysuiColorExtractor> lazy) {
        dependency.mSysuiColorExtractor = lazy;
    }

    public static void injectMTunablePaddingService(Dependency dependency, Lazy<TunablePadding.TunablePaddingService> lazy) {
        dependency.mTunablePaddingService = lazy;
    }

    public static void injectMForegroundServiceController(Dependency dependency, Lazy<ForegroundServiceController> lazy) {
        dependency.mForegroundServiceController = lazy;
    }

    public static void injectMUiOffloadThread(Dependency dependency, Lazy<UiOffloadThread> lazy) {
        dependency.mUiOffloadThread = lazy;
    }

    public static void injectMWarningsUI(Dependency dependency, Lazy<PowerUI.WarningsUI> lazy) {
        dependency.mWarningsUI = lazy;
    }

    public static void injectMLightBarController(Dependency dependency, Lazy<LightBarController> lazy) {
        dependency.mLightBarController = lazy;
    }

    public static void injectMIWindowManager(Dependency dependency, Lazy<IWindowManager> lazy) {
        dependency.mIWindowManager = lazy;
    }

    public static void injectMOverviewProxyService(Dependency dependency, Lazy<OverviewProxyService> lazy) {
        dependency.mOverviewProxyService = lazy;
    }

    public static void injectMNavBarModeController(Dependency dependency, Lazy<NavigationModeController> lazy) {
        dependency.mNavBarModeController = lazy;
    }

    public static void injectMEnhancedEstimates(Dependency dependency, Lazy<EnhancedEstimates> lazy) {
        dependency.mEnhancedEstimates = lazy;
    }

    public static void injectMVibratorHelper(Dependency dependency, Lazy<VibratorHelper> lazy) {
        dependency.mVibratorHelper = lazy;
    }

    public static void injectMIStatusBarService(Dependency dependency, Lazy<IStatusBarService> lazy) {
        dependency.mIStatusBarService = lazy;
    }

    public static void injectMDisplayMetrics(Dependency dependency, Lazy<DisplayMetrics> lazy) {
        dependency.mDisplayMetrics = lazy;
    }

    public static void injectMLockscreenGestureLogger(Dependency dependency, Lazy<LockscreenGestureLogger> lazy) {
        dependency.mLockscreenGestureLogger = lazy;
    }

    public static void injectMKeyguardEnvironment(Dependency dependency, Lazy<NotificationEntryManager.KeyguardEnvironment> lazy) {
        dependency.mKeyguardEnvironment = lazy;
    }

    public static void injectMShadeController(Dependency dependency, Lazy<ShadeController> lazy) {
        dependency.mShadeController = lazy;
    }

    public static void injectMNotificationRemoteInputManagerCallback(Dependency dependency, Lazy<NotificationRemoteInputManager.Callback> lazy) {
        dependency.mNotificationRemoteInputManagerCallback = lazy;
    }

    public static void injectMAppOpsController(Dependency dependency, Lazy<AppOpsController> lazy) {
        dependency.mAppOpsController = lazy;
    }

    public static void injectMNavigationBarController(Dependency dependency, Lazy<NavigationBarController> lazy) {
        dependency.mNavigationBarController = lazy;
    }

    public static void injectMStatusBarStateController(Dependency dependency, Lazy<StatusBarStateController> lazy) {
        dependency.mStatusBarStateController = lazy;
    }

    public static void injectMNotificationLockscreenUserManager(Dependency dependency, Lazy<NotificationLockscreenUserManager> lazy) {
        dependency.mNotificationLockscreenUserManager = lazy;
    }

    public static void injectMNotificationGroupAlertTransferHelper(Dependency dependency, Lazy<NotificationGroupAlertTransferHelper> lazy) {
        dependency.mNotificationGroupAlertTransferHelper = lazy;
    }

    public static void injectMNotificationGroupManager(Dependency dependency, Lazy<NotificationGroupManager> lazy) {
        dependency.mNotificationGroupManager = lazy;
    }

    public static void injectMVisualStabilityManager(Dependency dependency, Lazy<VisualStabilityManager> lazy) {
        dependency.mVisualStabilityManager = lazy;
    }

    public static void injectMNotificationGutsManager(Dependency dependency, Lazy<NotificationGutsManager> lazy) {
        dependency.mNotificationGutsManager = lazy;
    }

    public static void injectMNotificationMediaManager(Dependency dependency, Lazy<NotificationMediaManager> lazy) {
        dependency.mNotificationMediaManager = lazy;
    }

    public static void injectMNotificationBlockingHelperManager(Dependency dependency, Lazy<NotificationBlockingHelperManager> lazy) {
        dependency.mNotificationBlockingHelperManager = lazy;
    }

    public static void injectMNotificationRemoteInputManager(Dependency dependency, Lazy<NotificationRemoteInputManager> lazy) {
        dependency.mNotificationRemoteInputManager = lazy;
    }

    public static void injectMSmartReplyConstants(Dependency dependency, Lazy<SmartReplyConstants> lazy) {
        dependency.mSmartReplyConstants = lazy;
    }

    public static void injectMNotificationListener(Dependency dependency, Lazy<NotificationListener> lazy) {
        dependency.mNotificationListener = lazy;
    }

    public static void injectMNotificationLogger(Dependency dependency, Lazy<NotificationLogger> lazy) {
        dependency.mNotificationLogger = lazy;
    }

    public static void injectMNotificationViewHierarchyManager(Dependency dependency, Lazy<NotificationViewHierarchyManager> lazy) {
        dependency.mNotificationViewHierarchyManager = lazy;
    }

    public static void injectMNotificationFilter(Dependency dependency, Lazy<NotificationFilter> lazy) {
        dependency.mNotificationFilter = lazy;
    }

    public static void injectMKeyguardDismissUtil(Dependency dependency, Lazy<KeyguardDismissUtil> lazy) {
        dependency.mKeyguardDismissUtil = lazy;
    }

    public static void injectMSmartReplyController(Dependency dependency, Lazy<SmartReplyController> lazy) {
        dependency.mSmartReplyController = lazy;
    }

    public static void injectMRemoteInputQuickSettingsDisabler(Dependency dependency, Lazy<RemoteInputQuickSettingsDisabler> lazy) {
        dependency.mRemoteInputQuickSettingsDisabler = lazy;
    }

    public static void injectMBubbleController(Dependency dependency, Lazy<BubbleController> lazy) {
        dependency.mBubbleController = lazy;
    }

    public static void injectMNotificationEntryManager(Dependency dependency, Lazy<NotificationEntryManager> lazy) {
        dependency.mNotificationEntryManager = lazy;
    }

    public static void injectMSensorPrivacyManager(Dependency dependency, Lazy<SensorPrivacyManager> lazy) {
        dependency.mSensorPrivacyManager = lazy;
    }

    public static void injectMAutoHideController(Dependency dependency, Lazy<AutoHideController> lazy) {
        dependency.mAutoHideController = lazy;
    }

    public static void injectMForegroundServiceNotificationListener(Dependency dependency, Lazy<ForegroundServiceNotificationListener> lazy) {
        dependency.mForegroundServiceNotificationListener = lazy;
    }

    public static void injectMBgLooper(Dependency dependency, Lazy<Looper> lazy) {
        dependency.mBgLooper = lazy;
    }

    public static void injectMBgHandler(Dependency dependency, Lazy<Handler> lazy) {
        dependency.mBgHandler = lazy;
    }

    public static void injectMMainLooper(Dependency dependency, Lazy<Looper> lazy) {
        dependency.mMainLooper = lazy;
    }

    public static void injectMMainHandler(Dependency dependency, Lazy<Handler> lazy) {
        dependency.mMainHandler = lazy;
    }

    public static void injectMTimeTickHandler(Dependency dependency, Lazy<Handler> lazy) {
        dependency.mTimeTickHandler = lazy;
    }

    public static void injectMLeakReportEmail(Dependency dependency, Lazy<String> lazy) {
        dependency.mLeakReportEmail = lazy;
    }

    public static void injectMClockManager(Dependency dependency, Lazy<ClockManager> lazy) {
        dependency.mClockManager = lazy;
    }

    public static void injectMActivityManagerWrapper(Dependency dependency, Lazy<ActivityManagerWrapper> lazy) {
        dependency.mActivityManagerWrapper = lazy;
    }

    public static void injectMDevicePolicyManagerWrapper(Dependency dependency, Lazy<DevicePolicyManagerWrapper> lazy) {
        dependency.mDevicePolicyManagerWrapper = lazy;
    }

    public static void injectMPackageManagerWrapper(Dependency dependency, Lazy<PackageManagerWrapper> lazy) {
        dependency.mPackageManagerWrapper = lazy;
    }

    public static void injectMSensorPrivacyController(Dependency dependency, Lazy<SensorPrivacyController> lazy) {
        dependency.mSensorPrivacyController = lazy;
    }

    public static void injectMDockManager(Dependency dependency, Lazy<DockManager> lazy) {
        dependency.mDockManager = lazy;
    }

    public static void injectMINotificationManager(Dependency dependency, Lazy<INotificationManager> lazy) {
        dependency.mINotificationManager = lazy;
    }

    public static void injectMSysUiStateFlagsContainer(Dependency dependency, Lazy<SysUiState> lazy) {
        dependency.mSysUiStateFlagsContainer = lazy;
    }

    public static void injectMAlarmManager(Dependency dependency, Lazy<AlarmManager> lazy) {
        dependency.mAlarmManager = lazy;
    }

    public static void injectMKeyguardSecurityModel(Dependency dependency, Lazy<KeyguardSecurityModel> lazy) {
        dependency.mKeyguardSecurityModel = lazy;
    }

    public static void injectMDozeParameters(Dependency dependency, Lazy<DozeParameters> lazy) {
        dependency.mDozeParameters = lazy;
    }

    public static void injectMWallpaperManager(Dependency dependency, Lazy<IWallpaperManager> lazy) {
        dependency.mWallpaperManager = lazy;
    }

    public static void injectMCommandQueue(Dependency dependency, Lazy<CommandQueue> lazy) {
        dependency.mCommandQueue = lazy;
    }

    public static void injectMRecents(Dependency dependency, Lazy<Recents> lazy) {
        dependency.mRecents = lazy;
    }

    public static void injectMStatusBar(Dependency dependency, Lazy<StatusBar> lazy) {
        dependency.mStatusBar = lazy;
    }

    public static void injectMDisplayController(Dependency dependency, Lazy<DisplayController> lazy) {
        dependency.mDisplayController = lazy;
    }

    public static void injectMSystemWindows(Dependency dependency, Lazy<SystemWindows> lazy) {
        dependency.mSystemWindows = lazy;
    }

    public static void injectMDisplayImeController(Dependency dependency, Lazy<DisplayImeController> lazy) {
        dependency.mDisplayImeController = lazy;
    }

    public static void injectMRecordingController(Dependency dependency, Lazy<RecordingController> lazy) {
        dependency.mRecordingController = lazy;
    }

    public static void injectMProtoTracer(Dependency dependency, Lazy<ProtoTracer> lazy) {
        dependency.mProtoTracer = lazy;
    }

    public static void injectMDivider(Dependency dependency, Lazy<Divider> lazy) {
        dependency.mDivider = lazy;
    }

    public static void injectMSettingsManager(Dependency dependency, Lazy<SettingsManager> lazy) {
        dependency.mSettingsManager = lazy;
    }

    public static void injectMCloudDataManager(Dependency dependency, Lazy<CloudDataManager> lazy) {
        dependency.mCloudDataManager = lazy;
    }

    public static void injectMEventTracker(Dependency dependency, Lazy<EventTracker> lazy) {
        dependency.mEventTracker = lazy;
    }

    public static void injectMAppIconsManager(Dependency dependency, Lazy<AppIconsManager> lazy) {
        dependency.mAppIconsManager = lazy;
    }

    public static void injectMNotificationStat(Dependency dependency, Lazy<NotificationStat> lazy) {
        dependency.mNotificationStat = lazy;
    }

    public static void injectMUsbNotificationController(Dependency dependency, Lazy<UsbNotificationController> lazy) {
        dependency.mUsbNotificationController = lazy;
    }

    public static void injectMKeyguardNotificationHelper(Dependency dependency, Lazy<KeyguardNotificationController> lazy) {
        dependency.mKeyguardNotificationHelper = lazy;
    }

    public static void injectMNotificationSettingsManager(Dependency dependency, Lazy<NotificationSettingsManager> lazy) {
        dependency.mNotificationSettingsManager = lazy;
    }

    public static void injectMNotificationBadgeController(Dependency dependency, Lazy<NotificationBadgeController> lazy) {
        dependency.mNotificationBadgeController = lazy;
    }

    public static void injectMNotificationSensitiveController(Dependency dependency, Lazy<NotificationSensitiveController> lazy) {
        dependency.mNotificationSensitiveController = lazy;
    }

    public static void injectMMiuiChargeManager(Dependency dependency, Lazy<MiuiChargeManager> lazy) {
        dependency.mMiuiChargeManager = lazy;
    }

    public static void injectMMiuiChargeController(Dependency dependency, Lazy<MiuiChargeController> lazy) {
        dependency.mMiuiChargeController = lazy;
    }

    public static void injectMMiuihapticFeedBack(Dependency dependency, Lazy<HapticFeedBackImpl> lazy) {
        dependency.mMiuihapticFeedBack = lazy;
    }

    public static void injectMContentObserver(Dependency dependency, Lazy<SettingsObserver> lazy) {
        dependency.mContentObserver = lazy;
    }

    public static void injectMKeyguardIndicationController(Dependency dependency, Lazy<KeyguardIndicationController> lazy) {
        dependency.mKeyguardIndicationController = lazy;
    }

    public static void injectMLockScreenMagazineController(Dependency dependency, Lazy<LockScreenMagazineController> lazy) {
        dependency.mLockScreenMagazineController = lazy;
    }

    public static void injectMMiuiFaceUnlockManager(Dependency dependency, Lazy<MiuiFaceUnlockManager> lazy) {
        dependency.mMiuiFaceUnlockManager = lazy;
    }

    public static void injectMMiuiGxzwManager(Dependency dependency, Lazy<MiuiGxzwManager> lazy) {
        dependency.mMiuiGxzwManager = lazy;
    }

    public static void injectMMiuiFastUnlockController(Dependency dependency, Lazy<MiuiFastUnlockController> lazy) {
        dependency.mMiuiFastUnlockController = lazy;
    }

    public static void injectMKeyguardIndicationInjector(Dependency dependency, Lazy<KeyguardIndicationInjector> lazy) {
        dependency.mKeyguardIndicationInjector = lazy;
    }

    public static void injectMKeyguardNotificationInjector(Dependency dependency, Lazy<KeyguardPanelViewInjector> lazy) {
        dependency.mKeyguardNotificationInjector = lazy;
    }

    public static void injectMKeyguardUpdateMonitorInjector(Dependency dependency, Lazy<KeyguardUpdateMonitorInjector> lazy) {
        dependency.mKeyguardUpdateMonitorInjector = lazy;
    }

    public static void injectMDozeServiceHost(Dependency dependency, Lazy<MiuiDozeServiceHost> lazy) {
        dependency.mDozeServiceHost = lazy;
    }

    public static void injectMForceBlackObserver(Dependency dependency, Lazy<ForceBlackObserver> lazy) {
        dependency.mForceBlackObserver = lazy;
    }

    public static void injectMKeyguardClockInjector(Dependency dependency, Lazy<KeyguardClockInjector> lazy) {
        dependency.mKeyguardClockInjector = lazy;
    }

    public static void injectMKeyguardBottomAreaInjector(Dependency dependency, Lazy<KeyguardBottomAreaInjector> lazy) {
        dependency.mKeyguardBottomAreaInjector = lazy;
    }

    public static void injectMKeyguardNegative1PageInjector(Dependency dependency, Lazy<KeyguardNegative1PageInjector> lazy) {
        dependency.mKeyguardNegative1PageInjector = lazy;
    }

    public static void injectMKeyguardSensorInjector(Dependency dependency, Lazy<KeyguardSensorInjector> lazy) {
        dependency.mKeyguardSensorInjector = lazy;
    }

    public static void injectMKeyguardViewMediatorInjector(Dependency dependency, Lazy<KeyguardViewMediatorInjector> lazy) {
        dependency.mKeyguardViewMediatorInjector = lazy;
    }

    public static void injectMSmartDarkObserver(Dependency dependency, Lazy<SmartDarkObserver> lazy) {
        dependency.mSmartDarkObserver = lazy;
    }

    public static void injectMMiuiStatusBarPromptController(Dependency dependency, Lazy<MiuiStatusBarPromptController> lazy) {
        dependency.mMiuiStatusBarPromptController = lazy;
    }

    public static void injectMDriveModeObserver(Dependency dependency, Lazy<DriveModeObserver> lazy) {
        dependency.mDriveModeObserver = lazy;
    }

    public static void injectMNetworkSpeedController(Dependency dependency, Lazy<NetworkSpeedController> lazy) {
        dependency.mNetworkSpeedController = lazy;
    }

    public static void injectMMiuiDripLeftStatusBarIconControllerImpl(Dependency dependency, Lazy<MiuiDripLeftStatusBarIconControllerImpl> lazy) {
        dependency.mMiuiDripLeftStatusBarIconControllerImpl = lazy;
    }

    public static void injectMMiuiKeyguardWallpaperControllerImpl(Dependency dependency, Lazy<MiuiKeyguardWallpaperControllerImpl> lazy) {
        dependency.mMiuiKeyguardWallpaperControllerImpl = lazy;
    }

    public static void injectMUpdateWallpaperCommand(Dependency dependency, Lazy<WallpaperCommandSender> lazy) {
        dependency.mUpdateWallpaperCommand = lazy;
    }

    public static void injectMMiuiWallpaperClient(Dependency dependency, Lazy<MiuiWallpaperClient> lazy) {
        dependency.mMiuiWallpaperClient = lazy;
    }

    public static void injectMControlPanelController(Dependency dependency, Lazy<ControlPanelController> lazy) {
        dependency.mControlPanelController = lazy;
    }

    public static void injectMControlPanelWindowManager(Dependency dependency, Lazy<ControlPanelWindowManager> lazy) {
        dependency.mControlPanelWindowManager = lazy;
    }

    public static void injectMControlCenterActivityStarter(Dependency dependency, Lazy<ControlCenterActivityStarter> lazy) {
        dependency.mControlCenterActivityStarter = lazy;
    }

    public static void injectMExpandInfoController(Dependency dependency, Lazy<ExpandInfoController> lazy) {
        dependency.mExpandInfoController = lazy;
    }

    public static void injectMControlsPluginManager(Dependency dependency, Lazy<ControlsPluginManager> lazy) {
        dependency.mControlsPluginManager = lazy;
    }

    public static void injectMAppMiniWindowManager(Dependency dependency, Lazy<AppMiniWindowManager> lazy) {
        dependency.mAppMiniWindowManager = lazy;
    }

    public static void injectMModalController(Dependency dependency, Lazy<ModalController> lazy) {
        dependency.mModalController = lazy;
    }

    public static void injectMFiveGControllerImpl(Dependency dependency, Lazy<FiveGControllerImpl> lazy) {
        dependency.mFiveGControllerImpl = lazy;
    }

    public static void injectMCallStateController(Dependency dependency, Lazy<CallStateControllerImpl> lazy) {
        dependency.mCallStateController = lazy;
    }

    public static void injectMRegionController(Dependency dependency, Lazy<RegionController> lazy) {
        dependency.mRegionController = lazy;
    }

    public static void injectMCustomCarrierObserver(Dependency dependency, Lazy<CustomCarrierObserver> lazy) {
        dependency.mCustomCarrierObserver = lazy;
    }

    public static void injectMCarrierObserver(Dependency dependency, Lazy<CarrierObserver> lazy) {
        dependency.mCarrierObserver = lazy;
    }

    public static void injectMMiuiCarrierTextController(Dependency dependency, Lazy<MiuiCarrierTextController> lazy) {
        dependency.mMiuiCarrierTextController = lazy;
    }

    public static void injectMNotificationIconObserver(Dependency dependency, Lazy<NotificationIconObserver> lazy) {
        dependency.mNotificationIconObserver = lazy;
    }

    public static void injectMDualClockObserver(Dependency dependency, Lazy<DualClockObserver> lazy) {
        dependency.mDualClockObserver = lazy;
    }

    public static void injectMToggleManagerController(Dependency dependency, Lazy<ToggleManagerController> lazy) {
        dependency.mToggleManagerController = lazy;
    }

    public static void injectMWallPaperController(Dependency dependency, Lazy<IMiuiKeyguardWallpaperController> lazy) {
        dependency.mWallPaperController = lazy;
    }

    public static void injectMPanelExpansionObserver(Dependency dependency, Lazy<PanelExpansionObserver> lazy) {
        dependency.mPanelExpansionObserver = lazy;
    }

    public static void injectMSuperSaveModeController(Dependency dependency, Lazy<SuperSaveModeController> lazy) {
        dependency.mSuperSaveModeController = lazy;
    }

    public static void injectMDemoModeController(Dependency dependency, Lazy<DemoModeController> lazy) {
        dependency.mDemoModeController = lazy;
    }

    public static void injectMSlaveWifiSignalController(Dependency dependency, Lazy<SlaveWifiSignalController> lazy) {
        dependency.mSlaveWifiSignalController = lazy;
    }

    public static void injectMMiuiAlarmControllerImpl(Dependency dependency, Lazy<MiuiAlarmControllerImpl> lazy) {
        dependency.mMiuiAlarmControllerImpl = lazy;
    }

    public static void injectMNotificationNavigationCoordinator(Dependency dependency, Lazy<NotificationPanelNavigationBarCoordinator> lazy) {
        dependency.mNotificationNavigationCoordinator = lazy;
    }

    public static void injectMNCSwitchController(Dependency dependency, Lazy<NCSwitchController> lazy) {
        dependency.mNCSwitchController = lazy;
    }

    public static void injectMSystemUIStat(Dependency dependency, Lazy<SystemUIStat> lazy) {
        dependency.mSystemUIStat = lazy;
    }

    public static void injectMPhoneSignalController(Dependency dependency, Lazy<IPhoneSignalController> lazy) {
        dependency.mPhoneSignalController = lazy;
    }
}
