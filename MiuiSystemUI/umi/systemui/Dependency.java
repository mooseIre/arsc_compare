package com.android.systemui;

import android.app.AlarmManager;
import android.app.INotificationManager;
import android.app.IWallpaperManager;
import android.hardware.SensorPrivacyManager;
import android.hardware.display.NightDisplayListener;
import android.os.Handler;
import android.os.Looper;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.view.IWindowManager;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.util.Preconditions;
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
import java.util.Objects;
import java.util.function.Consumer;

public class Dependency {
    public static final DependencyKey<Looper> BG_LOOPER = new DependencyKey<>("background_looper");
    public static final DependencyKey<String> LEAK_REPORT_EMAIL = new DependencyKey<>("leak_report_email");
    public static final DependencyKey<Handler> MAIN_HANDLER = new DependencyKey<>("main_handler");
    public static final DependencyKey<Looper> MAIN_LOOPER = new DependencyKey<>("main_looper");
    public static final DependencyKey<Handler> TIME_TICK_HANDLER = new DependencyKey<>("time_tick_handler");
    private static Dependency sDependency;
    Lazy<AccessibilityController> mAccessibilityController;
    Lazy<AccessibilityManagerWrapper> mAccessibilityManagerWrapper;
    Lazy<ActivityManagerWrapper> mActivityManagerWrapper;
    Lazy<ActivityStarter> mActivityStarter;
    Lazy<AlarmManager> mAlarmManager;
    Lazy<AppIconsManager> mAppIconsManager;
    Lazy<AppMiniWindowManager> mAppMiniWindowManager;
    Lazy<AppOpsController> mAppOpsController;
    Lazy<AssistManager> mAssistManager;
    Lazy<AsyncSensorManager> mAsyncSensorManager;
    Lazy<AutoHideController> mAutoHideController;
    Lazy<BatteryController> mBatteryController;
    Lazy<Handler> mBgHandler;
    Lazy<Looper> mBgLooper;
    Lazy<BluetoothController> mBluetoothController;
    Lazy<BroadcastDispatcher> mBroadcastDispatcher;
    Lazy<BubbleController> mBubbleController;
    Lazy<CallStateControllerImpl> mCallStateController;
    Lazy<CarrierObserver> mCarrierObserver;
    Lazy<CastController> mCastController;
    Lazy<ClockManager> mClockManager;
    Lazy<CloudDataManager> mCloudDataManager;
    Lazy<CommandQueue> mCommandQueue;
    Lazy<ConfigurationController> mConfigurationController;
    Lazy<SettingsObserver> mContentObserver;
    Lazy<ControlCenterActivityStarter> mControlCenterActivityStarter;
    Lazy<ControlPanelController> mControlPanelController;
    Lazy<ControlPanelWindowManager> mControlPanelWindowManager;
    Lazy<ControlsPluginManager> mControlsPluginManager;
    Lazy<CustomCarrierObserver> mCustomCarrierObserver;
    Lazy<DarkIconDispatcher> mDarkIconDispatcher;
    Lazy<DataSaverController> mDataSaverController;
    Lazy<DemoModeController> mDemoModeController;
    private final ArrayMap<Object, Object> mDependencies = new ArrayMap<>();
    Lazy<DevicePolicyManagerWrapper> mDevicePolicyManagerWrapper;
    Lazy<DeviceProvisionedController> mDeviceProvisionedController;
    Lazy<DisplayController> mDisplayController;
    Lazy<DisplayImeController> mDisplayImeController;
    Lazy<DisplayMetrics> mDisplayMetrics;
    Lazy<Divider> mDivider;
    Lazy<DockManager> mDockManager;
    Lazy<DozeParameters> mDozeParameters;
    Lazy<MiuiDozeServiceHost> mDozeServiceHost;
    Lazy<DriveModeObserver> mDriveModeObserver;
    Lazy<DualClockObserver> mDualClockObserver;
    DumpManager mDumpManager;
    Lazy<EnhancedEstimates> mEnhancedEstimates;
    Lazy<EventTracker> mEventTracker;
    Lazy<ExpandInfoController> mExpandInfoController;
    Lazy<ExtensionController> mExtensionController;
    Lazy<FiveGControllerImpl> mFiveGControllerImpl;
    Lazy<FlashlightController> mFlashlightController;
    Lazy<ForceBlackObserver> mForceBlackObserver;
    Lazy<ForegroundServiceController> mForegroundServiceController;
    Lazy<ForegroundServiceNotificationListener> mForegroundServiceNotificationListener;
    Lazy<FragmentService> mFragmentService;
    Lazy<GarbageMonitor> mGarbageMonitor;
    Lazy<HotspotController> mHotspotController;
    Lazy<INotificationManager> mINotificationManager;
    Lazy<IStatusBarService> mIStatusBarService;
    Lazy<IWindowManager> mIWindowManager;
    Lazy<KeyguardBottomAreaInjector> mKeyguardBottomAreaInjector;
    Lazy<KeyguardClockInjector> mKeyguardClockInjector;
    Lazy<KeyguardDismissUtil> mKeyguardDismissUtil;
    Lazy<NotificationEntryManager.KeyguardEnvironment> mKeyguardEnvironment;
    Lazy<KeyguardIndicationController> mKeyguardIndicationController;
    Lazy<KeyguardIndicationInjector> mKeyguardIndicationInjector;
    Lazy<KeyguardStateController> mKeyguardMonitor;
    Lazy<KeyguardNegative1PageInjector> mKeyguardNegative1PageInjector;
    Lazy<KeyguardNotificationController> mKeyguardNotificationHelper;
    Lazy<KeyguardPanelViewInjector> mKeyguardNotificationInjector;
    Lazy<KeyguardSecurityModel> mKeyguardSecurityModel;
    Lazy<KeyguardSensorInjector> mKeyguardSensorInjector;
    Lazy<KeyguardUpdateMonitor> mKeyguardUpdateMonitor;
    Lazy<KeyguardUpdateMonitorInjector> mKeyguardUpdateMonitorInjector;
    Lazy<KeyguardViewMediatorInjector> mKeyguardViewMediatorInjector;
    Lazy<LeakDetector> mLeakDetector;
    Lazy<String> mLeakReportEmail;
    Lazy<LeakReporter> mLeakReporter;
    Lazy<LightBarController> mLightBarController;
    Lazy<LocalBluetoothManager> mLocalBluetoothManager;
    Lazy<LocationController> mLocationController;
    Lazy<LockScreenMagazineController> mLockScreenMagazineController;
    Lazy<LockscreenGestureLogger> mLockscreenGestureLogger;
    Lazy<Handler> mMainHandler;
    Lazy<Looper> mMainLooper;
    Lazy<ManagedProfileController> mManagedProfileController;
    Lazy<MetricsLogger> mMetricsLogger;
    Lazy<MiuiAlarmControllerImpl> mMiuiAlarmControllerImpl;
    Lazy<MiuiCarrierTextController> mMiuiCarrierTextController;
    Lazy<MiuiChargeController> mMiuiChargeController;
    Lazy<MiuiChargeManager> mMiuiChargeManager;
    Lazy<MiuiDripLeftStatusBarIconControllerImpl> mMiuiDripLeftStatusBarIconControllerImpl;
    Lazy<MiuiFaceUnlockManager> mMiuiFaceUnlockManager;
    Lazy<MiuiFastUnlockController> mMiuiFastUnlockController;
    Lazy<MiuiGxzwManager> mMiuiGxzwManager;
    Lazy<MiuiKeyguardWallpaperControllerImpl> mMiuiKeyguardWallpaperControllerImpl;
    Lazy<MiuiStatusBarPromptController> mMiuiStatusBarPromptController;
    Lazy<MiuiWallpaperClient> mMiuiWallpaperClient;
    Lazy<HapticFeedBackImpl> mMiuihapticFeedBack;
    Lazy<ModalController> mModalController;
    Lazy<NCSwitchController> mNCSwitchController;
    Lazy<NavigationModeController> mNavBarModeController;
    Lazy<NavigationBarController> mNavigationBarController;
    Lazy<NetworkController> mNetworkController;
    Lazy<NetworkSpeedController> mNetworkSpeedController;
    Lazy<NextAlarmController> mNextAlarmController;
    Lazy<NightDisplayListener> mNightDisplayListener;
    Lazy<NotificationBadgeController> mNotificationBadgeController;
    Lazy<NotificationBlockingHelperManager> mNotificationBlockingHelperManager;
    Lazy<NotificationEntryManager> mNotificationEntryManager;
    Lazy<NotificationFilter> mNotificationFilter;
    Lazy<NotificationGroupAlertTransferHelper> mNotificationGroupAlertTransferHelper;
    Lazy<NotificationGroupManager> mNotificationGroupManager;
    Lazy<NotificationGutsManager> mNotificationGutsManager;
    Lazy<NotificationIconObserver> mNotificationIconObserver;
    Lazy<NotificationListener> mNotificationListener;
    Lazy<NotificationLockscreenUserManager> mNotificationLockscreenUserManager;
    Lazy<NotificationLogger> mNotificationLogger;
    Lazy<NotificationMediaManager> mNotificationMediaManager;
    Lazy<NotificationPanelNavigationBarCoordinator> mNotificationNavigationCoordinator;
    Lazy<NotificationRemoteInputManager> mNotificationRemoteInputManager;
    Lazy<NotificationRemoteInputManager.Callback> mNotificationRemoteInputManagerCallback;
    Lazy<NotificationSensitiveController> mNotificationSensitiveController;
    Lazy<NotificationSettingsManager> mNotificationSettingsManager;
    Lazy<NotificationShadeWindowController> mNotificationShadeWindowController;
    Lazy<NotificationStat> mNotificationStat;
    Lazy<NotificationViewHierarchyManager> mNotificationViewHierarchyManager;
    Lazy<OverviewProxyService> mOverviewProxyService;
    Lazy<PackageManagerWrapper> mPackageManagerWrapper;
    Lazy<PanelExpansionObserver> mPanelExpansionObserver;
    Lazy<IPhoneSignalController> mPhoneSignalController;
    Lazy<PluginDependencyProvider> mPluginDependencyProvider;
    Lazy<PluginManager> mPluginManager;
    Lazy<ProtoTracer> mProtoTracer;
    private final ArrayMap<Object, LazyDependencyCreator> mProviders = new ArrayMap<>();
    Lazy<Recents> mRecents;
    Lazy<RecordingController> mRecordingController;
    Lazy<RegionController> mRegionController;
    Lazy<RemoteInputQuickSettingsDisabler> mRemoteInputQuickSettingsDisabler;
    Lazy<RotationLockController> mRotationLockController;
    Lazy<ScreenLifecycle> mScreenLifecycle;
    Lazy<SecurityController> mSecurityController;
    Lazy<SensorPrivacyController> mSensorPrivacyController;
    Lazy<SensorPrivacyManager> mSensorPrivacyManager;
    Lazy<SettingsManager> mSettingsManager;
    Lazy<ShadeController> mShadeController;
    Lazy<SlaveWifiSignalController> mSlaveWifiSignalController;
    Lazy<SmartDarkObserver> mSmartDarkObserver;
    Lazy<SmartReplyConstants> mSmartReplyConstants;
    Lazy<SmartReplyController> mSmartReplyController;
    Lazy<StatusBar> mStatusBar;
    Lazy<StatusBarIconController> mStatusBarIconController;
    Lazy<StatusBarStateController> mStatusBarStateController;
    Lazy<SuperSaveModeController> mSuperSaveModeController;
    Lazy<SysUiState> mSysUiStateFlagsContainer;
    Lazy<SystemUIStat> mSystemUIStat;
    Lazy<SystemWindows> mSystemWindows;
    Lazy<SysuiColorExtractor> mSysuiColorExtractor;
    Lazy<StatusBarWindowController> mTempStatusBarWindowController;
    Lazy<Handler> mTimeTickHandler;
    Lazy<ToggleManagerController> mToggleManagerController;
    Lazy<TunablePadding.TunablePaddingService> mTunablePaddingService;
    Lazy<TunerService> mTunerService;
    Lazy<UiOffloadThread> mUiOffloadThread;
    Lazy<WallpaperCommandSender> mUpdateWallpaperCommand;
    Lazy<UsbNotificationController> mUsbNotificationController;
    Lazy<UserInfoController> mUserInfoController;
    Lazy<UserSwitcherController> mUserSwitcherController;
    Lazy<VibratorHelper> mVibratorHelper;
    Lazy<VisualStabilityManager> mVisualStabilityManager;
    Lazy<VolumeDialogController> mVolumeDialogController;
    Lazy<WakefulnessLifecycle> mWakefulnessLifecycle;
    Lazy<IMiuiKeyguardWallpaperController> mWallPaperController;
    Lazy<IWallpaperManager> mWallpaperManager;
    Lazy<PowerUI.WarningsUI> mWarningsUI;
    Lazy<ZenModeController> mZenModeController;

    public interface DependencyInjector {
        void createSystemUI(Dependency dependency);
    }

    /* access modifiers changed from: private */
    public interface LazyDependencyCreator<T> {
        T createDependency();
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public boolean autoRegisterModulesForDump() {
        return true;
    }

    /* access modifiers changed from: protected */
    public void start() {
        ArrayMap<Object, LazyDependencyCreator> arrayMap = this.mProviders;
        DependencyKey<Handler> dependencyKey = TIME_TICK_HANDLER;
        Lazy<Handler> lazy = this.mTimeTickHandler;
        Objects.requireNonNull(lazy);
        arrayMap.put(dependencyKey, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap2 = this.mProviders;
        DependencyKey<Looper> dependencyKey2 = BG_LOOPER;
        Lazy<Looper> lazy2 = this.mBgLooper;
        Objects.requireNonNull(lazy2);
        arrayMap2.put(dependencyKey2, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap3 = this.mProviders;
        DependencyKey<Looper> dependencyKey3 = MAIN_LOOPER;
        Lazy<Looper> lazy3 = this.mMainLooper;
        Objects.requireNonNull(lazy3);
        arrayMap3.put(dependencyKey3, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap4 = this.mProviders;
        DependencyKey<Handler> dependencyKey4 = MAIN_HANDLER;
        Lazy<Handler> lazy4 = this.mMainHandler;
        Objects.requireNonNull(lazy4);
        arrayMap4.put(dependencyKey4, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap5 = this.mProviders;
        Lazy<ActivityStarter> lazy5 = this.mActivityStarter;
        Objects.requireNonNull(lazy5);
        arrayMap5.put(ActivityStarter.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap6 = this.mProviders;
        Lazy<BroadcastDispatcher> lazy6 = this.mBroadcastDispatcher;
        Objects.requireNonNull(lazy6);
        arrayMap6.put(BroadcastDispatcher.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap7 = this.mProviders;
        Lazy<AsyncSensorManager> lazy7 = this.mAsyncSensorManager;
        Objects.requireNonNull(lazy7);
        arrayMap7.put(AsyncSensorManager.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap8 = this.mProviders;
        Lazy<BluetoothController> lazy8 = this.mBluetoothController;
        Objects.requireNonNull(lazy8);
        arrayMap8.put(BluetoothController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap9 = this.mProviders;
        Lazy<SensorPrivacyManager> lazy9 = this.mSensorPrivacyManager;
        Objects.requireNonNull(lazy9);
        arrayMap9.put(SensorPrivacyManager.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap10 = this.mProviders;
        Lazy<LocationController> lazy10 = this.mLocationController;
        Objects.requireNonNull(lazy10);
        arrayMap10.put(LocationController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap11 = this.mProviders;
        Lazy<RotationLockController> lazy11 = this.mRotationLockController;
        Objects.requireNonNull(lazy11);
        arrayMap11.put(RotationLockController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap12 = this.mProviders;
        Lazy<NetworkController> lazy12 = this.mNetworkController;
        Objects.requireNonNull(lazy12);
        arrayMap12.put(NetworkController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap13 = this.mProviders;
        Lazy<ZenModeController> lazy13 = this.mZenModeController;
        Objects.requireNonNull(lazy13);
        arrayMap13.put(ZenModeController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap14 = this.mProviders;
        Lazy<HotspotController> lazy14 = this.mHotspotController;
        Objects.requireNonNull(lazy14);
        arrayMap14.put(HotspotController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap15 = this.mProviders;
        Lazy<CastController> lazy15 = this.mCastController;
        Objects.requireNonNull(lazy15);
        arrayMap15.put(CastController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap16 = this.mProviders;
        Lazy<FlashlightController> lazy16 = this.mFlashlightController;
        Objects.requireNonNull(lazy16);
        arrayMap16.put(FlashlightController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap17 = this.mProviders;
        Lazy<KeyguardStateController> lazy17 = this.mKeyguardMonitor;
        Objects.requireNonNull(lazy17);
        arrayMap17.put(KeyguardStateController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap18 = this.mProviders;
        Lazy<KeyguardUpdateMonitor> lazy18 = this.mKeyguardUpdateMonitor;
        Objects.requireNonNull(lazy18);
        arrayMap18.put(KeyguardUpdateMonitor.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap19 = this.mProviders;
        Lazy<UserSwitcherController> lazy19 = this.mUserSwitcherController;
        Objects.requireNonNull(lazy19);
        arrayMap19.put(UserSwitcherController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap20 = this.mProviders;
        Lazy<UserInfoController> lazy20 = this.mUserInfoController;
        Objects.requireNonNull(lazy20);
        arrayMap20.put(UserInfoController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap21 = this.mProviders;
        Lazy<BatteryController> lazy21 = this.mBatteryController;
        Objects.requireNonNull(lazy21);
        arrayMap21.put(BatteryController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap22 = this.mProviders;
        Lazy<NightDisplayListener> lazy22 = this.mNightDisplayListener;
        Objects.requireNonNull(lazy22);
        arrayMap22.put(NightDisplayListener.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap23 = this.mProviders;
        Lazy<ManagedProfileController> lazy23 = this.mManagedProfileController;
        Objects.requireNonNull(lazy23);
        arrayMap23.put(ManagedProfileController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap24 = this.mProviders;
        Lazy<NextAlarmController> lazy24 = this.mNextAlarmController;
        Objects.requireNonNull(lazy24);
        arrayMap24.put(NextAlarmController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap25 = this.mProviders;
        Lazy<DataSaverController> lazy25 = this.mDataSaverController;
        Objects.requireNonNull(lazy25);
        arrayMap25.put(DataSaverController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap26 = this.mProviders;
        Lazy<AccessibilityController> lazy26 = this.mAccessibilityController;
        Objects.requireNonNull(lazy26);
        arrayMap26.put(AccessibilityController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap27 = this.mProviders;
        Lazy<DeviceProvisionedController> lazy27 = this.mDeviceProvisionedController;
        Objects.requireNonNull(lazy27);
        arrayMap27.put(DeviceProvisionedController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap28 = this.mProviders;
        Lazy<PluginManager> lazy28 = this.mPluginManager;
        Objects.requireNonNull(lazy28);
        arrayMap28.put(PluginManager.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap29 = this.mProviders;
        Lazy<AssistManager> lazy29 = this.mAssistManager;
        Objects.requireNonNull(lazy29);
        arrayMap29.put(AssistManager.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap30 = this.mProviders;
        Lazy<SecurityController> lazy30 = this.mSecurityController;
        Objects.requireNonNull(lazy30);
        arrayMap30.put(SecurityController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap31 = this.mProviders;
        Lazy<LeakDetector> lazy31 = this.mLeakDetector;
        Objects.requireNonNull(lazy31);
        arrayMap31.put(LeakDetector.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap32 = this.mProviders;
        DependencyKey<String> dependencyKey5 = LEAK_REPORT_EMAIL;
        Lazy<String> lazy32 = this.mLeakReportEmail;
        Objects.requireNonNull(lazy32);
        arrayMap32.put(dependencyKey5, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap33 = this.mProviders;
        Lazy<LeakReporter> lazy33 = this.mLeakReporter;
        Objects.requireNonNull(lazy33);
        arrayMap33.put(LeakReporter.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap34 = this.mProviders;
        Lazy<GarbageMonitor> lazy34 = this.mGarbageMonitor;
        Objects.requireNonNull(lazy34);
        arrayMap34.put(GarbageMonitor.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap35 = this.mProviders;
        Lazy<TunerService> lazy35 = this.mTunerService;
        Objects.requireNonNull(lazy35);
        arrayMap35.put(TunerService.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap36 = this.mProviders;
        Lazy<NotificationShadeWindowController> lazy36 = this.mNotificationShadeWindowController;
        Objects.requireNonNull(lazy36);
        arrayMap36.put(NotificationShadeWindowController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap37 = this.mProviders;
        Lazy<StatusBarWindowController> lazy37 = this.mTempStatusBarWindowController;
        Objects.requireNonNull(lazy37);
        arrayMap37.put(StatusBarWindowController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap38 = this.mProviders;
        Lazy<DarkIconDispatcher> lazy38 = this.mDarkIconDispatcher;
        Objects.requireNonNull(lazy38);
        arrayMap38.put(DarkIconDispatcher.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap39 = this.mProviders;
        Lazy<ConfigurationController> lazy39 = this.mConfigurationController;
        Objects.requireNonNull(lazy39);
        arrayMap39.put(ConfigurationController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap40 = this.mProviders;
        Lazy<StatusBarIconController> lazy40 = this.mStatusBarIconController;
        Objects.requireNonNull(lazy40);
        arrayMap40.put(StatusBarIconController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap41 = this.mProviders;
        Lazy<ScreenLifecycle> lazy41 = this.mScreenLifecycle;
        Objects.requireNonNull(lazy41);
        arrayMap41.put(ScreenLifecycle.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap42 = this.mProviders;
        Lazy<WakefulnessLifecycle> lazy42 = this.mWakefulnessLifecycle;
        Objects.requireNonNull(lazy42);
        arrayMap42.put(WakefulnessLifecycle.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap43 = this.mProviders;
        Lazy<FragmentService> lazy43 = this.mFragmentService;
        Objects.requireNonNull(lazy43);
        arrayMap43.put(FragmentService.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap44 = this.mProviders;
        Lazy<ExtensionController> lazy44 = this.mExtensionController;
        Objects.requireNonNull(lazy44);
        arrayMap44.put(ExtensionController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap45 = this.mProviders;
        Lazy<PluginDependencyProvider> lazy45 = this.mPluginDependencyProvider;
        Objects.requireNonNull(lazy45);
        arrayMap45.put(PluginDependencyProvider.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap46 = this.mProviders;
        Lazy<LocalBluetoothManager> lazy46 = this.mLocalBluetoothManager;
        Objects.requireNonNull(lazy46);
        arrayMap46.put(LocalBluetoothManager.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap47 = this.mProviders;
        Lazy<VolumeDialogController> lazy47 = this.mVolumeDialogController;
        Objects.requireNonNull(lazy47);
        arrayMap47.put(VolumeDialogController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap48 = this.mProviders;
        Lazy<MetricsLogger> lazy48 = this.mMetricsLogger;
        Objects.requireNonNull(lazy48);
        arrayMap48.put(MetricsLogger.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap49 = this.mProviders;
        Lazy<AccessibilityManagerWrapper> lazy49 = this.mAccessibilityManagerWrapper;
        Objects.requireNonNull(lazy49);
        arrayMap49.put(AccessibilityManagerWrapper.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap50 = this.mProviders;
        Lazy<SysuiColorExtractor> lazy50 = this.mSysuiColorExtractor;
        Objects.requireNonNull(lazy50);
        arrayMap50.put(SysuiColorExtractor.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap51 = this.mProviders;
        Lazy<TunablePadding.TunablePaddingService> lazy51 = this.mTunablePaddingService;
        Objects.requireNonNull(lazy51);
        arrayMap51.put(TunablePadding.TunablePaddingService.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap52 = this.mProviders;
        Lazy<ForegroundServiceController> lazy52 = this.mForegroundServiceController;
        Objects.requireNonNull(lazy52);
        arrayMap52.put(ForegroundServiceController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap53 = this.mProviders;
        Lazy<UiOffloadThread> lazy53 = this.mUiOffloadThread;
        Objects.requireNonNull(lazy53);
        arrayMap53.put(UiOffloadThread.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap54 = this.mProviders;
        Lazy<PowerUI.WarningsUI> lazy54 = this.mWarningsUI;
        Objects.requireNonNull(lazy54);
        arrayMap54.put(PowerUI.WarningsUI.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap55 = this.mProviders;
        Lazy<LightBarController> lazy55 = this.mLightBarController;
        Objects.requireNonNull(lazy55);
        arrayMap55.put(LightBarController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap56 = this.mProviders;
        Lazy<IWindowManager> lazy56 = this.mIWindowManager;
        Objects.requireNonNull(lazy56);
        arrayMap56.put(IWindowManager.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap57 = this.mProviders;
        Lazy<OverviewProxyService> lazy57 = this.mOverviewProxyService;
        Objects.requireNonNull(lazy57);
        arrayMap57.put(OverviewProxyService.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap58 = this.mProviders;
        Lazy<NavigationModeController> lazy58 = this.mNavBarModeController;
        Objects.requireNonNull(lazy58);
        arrayMap58.put(NavigationModeController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap59 = this.mProviders;
        Lazy<EnhancedEstimates> lazy59 = this.mEnhancedEstimates;
        Objects.requireNonNull(lazy59);
        arrayMap59.put(EnhancedEstimates.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap60 = this.mProviders;
        Lazy<VibratorHelper> lazy60 = this.mVibratorHelper;
        Objects.requireNonNull(lazy60);
        arrayMap60.put(VibratorHelper.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap61 = this.mProviders;
        Lazy<IStatusBarService> lazy61 = this.mIStatusBarService;
        Objects.requireNonNull(lazy61);
        arrayMap61.put(IStatusBarService.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap62 = this.mProviders;
        Lazy<DisplayMetrics> lazy62 = this.mDisplayMetrics;
        Objects.requireNonNull(lazy62);
        arrayMap62.put(DisplayMetrics.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap63 = this.mProviders;
        Lazy<LockscreenGestureLogger> lazy63 = this.mLockscreenGestureLogger;
        Objects.requireNonNull(lazy63);
        arrayMap63.put(LockscreenGestureLogger.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap64 = this.mProviders;
        Lazy<NotificationEntryManager.KeyguardEnvironment> lazy64 = this.mKeyguardEnvironment;
        Objects.requireNonNull(lazy64);
        arrayMap64.put(NotificationEntryManager.KeyguardEnvironment.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap65 = this.mProviders;
        Lazy<ShadeController> lazy65 = this.mShadeController;
        Objects.requireNonNull(lazy65);
        arrayMap65.put(ShadeController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap66 = this.mProviders;
        Lazy<NotificationRemoteInputManager.Callback> lazy66 = this.mNotificationRemoteInputManagerCallback;
        Objects.requireNonNull(lazy66);
        arrayMap66.put(NotificationRemoteInputManager.Callback.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap67 = this.mProviders;
        Lazy<AppOpsController> lazy67 = this.mAppOpsController;
        Objects.requireNonNull(lazy67);
        arrayMap67.put(AppOpsController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap68 = this.mProviders;
        Lazy<NavigationBarController> lazy68 = this.mNavigationBarController;
        Objects.requireNonNull(lazy68);
        arrayMap68.put(NavigationBarController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap69 = this.mProviders;
        Lazy<StatusBarStateController> lazy69 = this.mStatusBarStateController;
        Objects.requireNonNull(lazy69);
        arrayMap69.put(StatusBarStateController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap70 = this.mProviders;
        Lazy<NotificationLockscreenUserManager> lazy70 = this.mNotificationLockscreenUserManager;
        Objects.requireNonNull(lazy70);
        arrayMap70.put(NotificationLockscreenUserManager.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap71 = this.mProviders;
        Lazy<VisualStabilityManager> lazy71 = this.mVisualStabilityManager;
        Objects.requireNonNull(lazy71);
        arrayMap71.put(VisualStabilityManager.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap72 = this.mProviders;
        Lazy<NotificationGroupManager> lazy72 = this.mNotificationGroupManager;
        Objects.requireNonNull(lazy72);
        arrayMap72.put(NotificationGroupManager.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap73 = this.mProviders;
        Lazy<NotificationGroupAlertTransferHelper> lazy73 = this.mNotificationGroupAlertTransferHelper;
        Objects.requireNonNull(lazy73);
        arrayMap73.put(NotificationGroupAlertTransferHelper.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap74 = this.mProviders;
        Lazy<NotificationMediaManager> lazy74 = this.mNotificationMediaManager;
        Objects.requireNonNull(lazy74);
        arrayMap74.put(NotificationMediaManager.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap75 = this.mProviders;
        Lazy<NotificationGutsManager> lazy75 = this.mNotificationGutsManager;
        Objects.requireNonNull(lazy75);
        arrayMap75.put(NotificationGutsManager.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap76 = this.mProviders;
        Lazy<NotificationBlockingHelperManager> lazy76 = this.mNotificationBlockingHelperManager;
        Objects.requireNonNull(lazy76);
        arrayMap76.put(NotificationBlockingHelperManager.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap77 = this.mProviders;
        Lazy<NotificationRemoteInputManager> lazy77 = this.mNotificationRemoteInputManager;
        Objects.requireNonNull(lazy77);
        arrayMap77.put(NotificationRemoteInputManager.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap78 = this.mProviders;
        Lazy<SmartReplyConstants> lazy78 = this.mSmartReplyConstants;
        Objects.requireNonNull(lazy78);
        arrayMap78.put(SmartReplyConstants.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap79 = this.mProviders;
        Lazy<NotificationListener> lazy79 = this.mNotificationListener;
        Objects.requireNonNull(lazy79);
        arrayMap79.put(NotificationListener.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap80 = this.mProviders;
        Lazy<NotificationLogger> lazy80 = this.mNotificationLogger;
        Objects.requireNonNull(lazy80);
        arrayMap80.put(NotificationLogger.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap81 = this.mProviders;
        Lazy<NotificationViewHierarchyManager> lazy81 = this.mNotificationViewHierarchyManager;
        Objects.requireNonNull(lazy81);
        arrayMap81.put(NotificationViewHierarchyManager.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap82 = this.mProviders;
        Lazy<NotificationFilter> lazy82 = this.mNotificationFilter;
        Objects.requireNonNull(lazy82);
        arrayMap82.put(NotificationFilter.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap83 = this.mProviders;
        Lazy<KeyguardDismissUtil> lazy83 = this.mKeyguardDismissUtil;
        Objects.requireNonNull(lazy83);
        arrayMap83.put(KeyguardDismissUtil.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap84 = this.mProviders;
        Lazy<SmartReplyController> lazy84 = this.mSmartReplyController;
        Objects.requireNonNull(lazy84);
        arrayMap84.put(SmartReplyController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap85 = this.mProviders;
        Lazy<RemoteInputQuickSettingsDisabler> lazy85 = this.mRemoteInputQuickSettingsDisabler;
        Objects.requireNonNull(lazy85);
        arrayMap85.put(RemoteInputQuickSettingsDisabler.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap86 = this.mProviders;
        Lazy<BubbleController> lazy86 = this.mBubbleController;
        Objects.requireNonNull(lazy86);
        arrayMap86.put(BubbleController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap87 = this.mProviders;
        Lazy<NotificationEntryManager> lazy87 = this.mNotificationEntryManager;
        Objects.requireNonNull(lazy87);
        arrayMap87.put(NotificationEntryManager.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap88 = this.mProviders;
        Lazy<ForegroundServiceNotificationListener> lazy88 = this.mForegroundServiceNotificationListener;
        Objects.requireNonNull(lazy88);
        arrayMap88.put(ForegroundServiceNotificationListener.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap89 = this.mProviders;
        Lazy<ClockManager> lazy89 = this.mClockManager;
        Objects.requireNonNull(lazy89);
        arrayMap89.put(ClockManager.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap90 = this.mProviders;
        Lazy<ActivityManagerWrapper> lazy90 = this.mActivityManagerWrapper;
        Objects.requireNonNull(lazy90);
        arrayMap90.put(ActivityManagerWrapper.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap91 = this.mProviders;
        Lazy<DevicePolicyManagerWrapper> lazy91 = this.mDevicePolicyManagerWrapper;
        Objects.requireNonNull(lazy91);
        arrayMap91.put(DevicePolicyManagerWrapper.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap92 = this.mProviders;
        Lazy<PackageManagerWrapper> lazy92 = this.mPackageManagerWrapper;
        Objects.requireNonNull(lazy92);
        arrayMap92.put(PackageManagerWrapper.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap93 = this.mProviders;
        Lazy<SensorPrivacyController> lazy93 = this.mSensorPrivacyController;
        Objects.requireNonNull(lazy93);
        arrayMap93.put(SensorPrivacyController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap94 = this.mProviders;
        Lazy<DockManager> lazy94 = this.mDockManager;
        Objects.requireNonNull(lazy94);
        arrayMap94.put(DockManager.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap95 = this.mProviders;
        Lazy<INotificationManager> lazy95 = this.mINotificationManager;
        Objects.requireNonNull(lazy95);
        arrayMap95.put(INotificationManager.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap96 = this.mProviders;
        Lazy<SysUiState> lazy96 = this.mSysUiStateFlagsContainer;
        Objects.requireNonNull(lazy96);
        arrayMap96.put(SysUiState.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap97 = this.mProviders;
        Lazy<AlarmManager> lazy97 = this.mAlarmManager;
        Objects.requireNonNull(lazy97);
        arrayMap97.put(AlarmManager.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap98 = this.mProviders;
        Lazy<KeyguardSecurityModel> lazy98 = this.mKeyguardSecurityModel;
        Objects.requireNonNull(lazy98);
        arrayMap98.put(KeyguardSecurityModel.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap99 = this.mProviders;
        Lazy<DozeParameters> lazy99 = this.mDozeParameters;
        Objects.requireNonNull(lazy99);
        arrayMap99.put(DozeParameters.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap100 = this.mProviders;
        Lazy<IWallpaperManager> lazy100 = this.mWallpaperManager;
        Objects.requireNonNull(lazy100);
        arrayMap100.put(IWallpaperManager.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap101 = this.mProviders;
        Lazy<CommandQueue> lazy101 = this.mCommandQueue;
        Objects.requireNonNull(lazy101);
        arrayMap101.put(CommandQueue.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap102 = this.mProviders;
        Lazy<Recents> lazy102 = this.mRecents;
        Objects.requireNonNull(lazy102);
        arrayMap102.put(Recents.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap103 = this.mProviders;
        Lazy<StatusBar> lazy103 = this.mStatusBar;
        Objects.requireNonNull(lazy103);
        arrayMap103.put(StatusBar.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap104 = this.mProviders;
        Lazy<DisplayController> lazy104 = this.mDisplayController;
        Objects.requireNonNull(lazy104);
        arrayMap104.put(DisplayController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap105 = this.mProviders;
        Lazy<SystemWindows> lazy105 = this.mSystemWindows;
        Objects.requireNonNull(lazy105);
        arrayMap105.put(SystemWindows.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap106 = this.mProviders;
        Lazy<DisplayImeController> lazy106 = this.mDisplayImeController;
        Objects.requireNonNull(lazy106);
        arrayMap106.put(DisplayImeController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap107 = this.mProviders;
        Lazy<ProtoTracer> lazy107 = this.mProtoTracer;
        Objects.requireNonNull(lazy107);
        arrayMap107.put(ProtoTracer.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap108 = this.mProviders;
        Lazy<AutoHideController> lazy108 = this.mAutoHideController;
        Objects.requireNonNull(lazy108);
        arrayMap108.put(AutoHideController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap109 = this.mProviders;
        Lazy<RecordingController> lazy109 = this.mRecordingController;
        Objects.requireNonNull(lazy109);
        arrayMap109.put(RecordingController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap110 = this.mProviders;
        Lazy<Divider> lazy110 = this.mDivider;
        Objects.requireNonNull(lazy110);
        arrayMap110.put(Divider.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap111 = this.mProviders;
        Lazy<SettingsManager> lazy111 = this.mSettingsManager;
        Objects.requireNonNull(lazy111);
        arrayMap111.put(SettingsManager.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap112 = this.mProviders;
        Lazy<CloudDataManager> lazy112 = this.mCloudDataManager;
        Objects.requireNonNull(lazy112);
        arrayMap112.put(CloudDataManager.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap113 = this.mProviders;
        Lazy<EventTracker> lazy113 = this.mEventTracker;
        Objects.requireNonNull(lazy113);
        arrayMap113.put(EventTracker.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap114 = this.mProviders;
        Lazy<AppIconsManager> lazy114 = this.mAppIconsManager;
        Objects.requireNonNull(lazy114);
        arrayMap114.put(AppIconsManager.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap115 = this.mProviders;
        Lazy<NotificationStat> lazy115 = this.mNotificationStat;
        Objects.requireNonNull(lazy115);
        arrayMap115.put(NotificationStat.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap116 = this.mProviders;
        Lazy<UsbNotificationController> lazy116 = this.mUsbNotificationController;
        Objects.requireNonNull(lazy116);
        arrayMap116.put(UsbNotificationController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap117 = this.mProviders;
        Lazy<KeyguardNotificationController> lazy117 = this.mKeyguardNotificationHelper;
        Objects.requireNonNull(lazy117);
        arrayMap117.put(KeyguardNotificationController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap118 = this.mProviders;
        Lazy<NotificationSettingsManager> lazy118 = this.mNotificationSettingsManager;
        Objects.requireNonNull(lazy118);
        arrayMap118.put(NotificationSettingsManager.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap119 = this.mProviders;
        Lazy<NotificationBadgeController> lazy119 = this.mNotificationBadgeController;
        Objects.requireNonNull(lazy119);
        arrayMap119.put(NotificationBadgeController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap120 = this.mProviders;
        Lazy<NotificationSensitiveController> lazy120 = this.mNotificationSensitiveController;
        Objects.requireNonNull(lazy120);
        arrayMap120.put(NotificationSensitiveController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap121 = this.mProviders;
        Lazy<MiuiChargeManager> lazy121 = this.mMiuiChargeManager;
        Objects.requireNonNull(lazy121);
        arrayMap121.put(MiuiChargeManager.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap122 = this.mProviders;
        Lazy<MiuiChargeController> lazy122 = this.mMiuiChargeController;
        Objects.requireNonNull(lazy122);
        arrayMap122.put(MiuiChargeController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap123 = this.mProviders;
        Lazy<HapticFeedBackImpl> lazy123 = this.mMiuihapticFeedBack;
        Objects.requireNonNull(lazy123);
        arrayMap123.put(HapticFeedBackImpl.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap124 = this.mProviders;
        Lazy<KeyguardIndicationInjector> lazy124 = this.mKeyguardIndicationInjector;
        Objects.requireNonNull(lazy124);
        arrayMap124.put(KeyguardIndicationInjector.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap125 = this.mProviders;
        Lazy<KeyguardPanelViewInjector> lazy125 = this.mKeyguardNotificationInjector;
        Objects.requireNonNull(lazy125);
        arrayMap125.put(KeyguardPanelViewInjector.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap126 = this.mProviders;
        Lazy<KeyguardUpdateMonitorInjector> lazy126 = this.mKeyguardUpdateMonitorInjector;
        Objects.requireNonNull(lazy126);
        arrayMap126.put(KeyguardUpdateMonitorInjector.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap127 = this.mProviders;
        Lazy<MiuiDozeServiceHost> lazy127 = this.mDozeServiceHost;
        Objects.requireNonNull(lazy127);
        arrayMap127.put(MiuiDozeServiceHost.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap128 = this.mProviders;
        Lazy<SettingsObserver> lazy128 = this.mContentObserver;
        Objects.requireNonNull(lazy128);
        arrayMap128.put(SettingsObserver.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap129 = this.mProviders;
        Lazy<KeyguardIndicationController> lazy129 = this.mKeyguardIndicationController;
        Objects.requireNonNull(lazy129);
        arrayMap129.put(KeyguardIndicationController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap130 = this.mProviders;
        Lazy<LockScreenMagazineController> lazy130 = this.mLockScreenMagazineController;
        Objects.requireNonNull(lazy130);
        arrayMap130.put(LockScreenMagazineController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap131 = this.mProviders;
        Lazy<MiuiFaceUnlockManager> lazy131 = this.mMiuiFaceUnlockManager;
        Objects.requireNonNull(lazy131);
        arrayMap131.put(MiuiFaceUnlockManager.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap132 = this.mProviders;
        Lazy<MiuiGxzwManager> lazy132 = this.mMiuiGxzwManager;
        Objects.requireNonNull(lazy132);
        arrayMap132.put(MiuiGxzwManager.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap133 = this.mProviders;
        Lazy<MiuiFastUnlockController> lazy133 = this.mMiuiFastUnlockController;
        Objects.requireNonNull(lazy133);
        arrayMap133.put(MiuiFastUnlockController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap134 = this.mProviders;
        Lazy<ForceBlackObserver> lazy134 = this.mForceBlackObserver;
        Objects.requireNonNull(lazy134);
        arrayMap134.put(ForceBlackObserver.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap135 = this.mProviders;
        Lazy<KeyguardClockInjector> lazy135 = this.mKeyguardClockInjector;
        Objects.requireNonNull(lazy135);
        arrayMap135.put(KeyguardClockInjector.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap136 = this.mProviders;
        Lazy<KeyguardBottomAreaInjector> lazy136 = this.mKeyguardBottomAreaInjector;
        Objects.requireNonNull(lazy136);
        arrayMap136.put(KeyguardBottomAreaInjector.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap137 = this.mProviders;
        Lazy<KeyguardNegative1PageInjector> lazy137 = this.mKeyguardNegative1PageInjector;
        Objects.requireNonNull(lazy137);
        arrayMap137.put(KeyguardNegative1PageInjector.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap138 = this.mProviders;
        Lazy<KeyguardSensorInjector> lazy138 = this.mKeyguardSensorInjector;
        Objects.requireNonNull(lazy138);
        arrayMap138.put(KeyguardSensorInjector.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap139 = this.mProviders;
        Lazy<KeyguardViewMediatorInjector> lazy139 = this.mKeyguardViewMediatorInjector;
        Objects.requireNonNull(lazy139);
        arrayMap139.put(KeyguardViewMediatorInjector.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap140 = this.mProviders;
        Lazy<SmartDarkObserver> lazy140 = this.mSmartDarkObserver;
        Objects.requireNonNull(lazy140);
        arrayMap140.put(SmartDarkObserver.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap141 = this.mProviders;
        Lazy<MiuiStatusBarPromptController> lazy141 = this.mMiuiStatusBarPromptController;
        Objects.requireNonNull(lazy141);
        arrayMap141.put(MiuiStatusBarPromptController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap142 = this.mProviders;
        Lazy<NotificationIconObserver> lazy142 = this.mNotificationIconObserver;
        Objects.requireNonNull(lazy142);
        arrayMap142.put(NotificationIconObserver.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap143 = this.mProviders;
        Lazy<DualClockObserver> lazy143 = this.mDualClockObserver;
        Objects.requireNonNull(lazy143);
        arrayMap143.put(DualClockObserver.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap144 = this.mProviders;
        Lazy<DriveModeObserver> lazy144 = this.mDriveModeObserver;
        Objects.requireNonNull(lazy144);
        arrayMap144.put(DriveModeObserver.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap145 = this.mProviders;
        Lazy<MiuiDripLeftStatusBarIconControllerImpl> lazy145 = this.mMiuiDripLeftStatusBarIconControllerImpl;
        Objects.requireNonNull(lazy145);
        arrayMap145.put(MiuiDripLeftStatusBarIconControllerImpl.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap146 = this.mProviders;
        Lazy<WallpaperCommandSender> lazy146 = this.mUpdateWallpaperCommand;
        Objects.requireNonNull(lazy146);
        arrayMap146.put(WallpaperCommandSender.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap147 = this.mProviders;
        Lazy<MiuiKeyguardWallpaperControllerImpl> lazy147 = this.mMiuiKeyguardWallpaperControllerImpl;
        Objects.requireNonNull(lazy147);
        arrayMap147.put(MiuiKeyguardWallpaperControllerImpl.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap148 = this.mProviders;
        Lazy<MiuiWallpaperClient> lazy148 = this.mMiuiWallpaperClient;
        Objects.requireNonNull(lazy148);
        arrayMap148.put(MiuiWallpaperClient.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap149 = this.mProviders;
        Lazy<ControlPanelController> lazy149 = this.mControlPanelController;
        Objects.requireNonNull(lazy149);
        arrayMap149.put(ControlPanelController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap150 = this.mProviders;
        Lazy<ControlPanelWindowManager> lazy150 = this.mControlPanelWindowManager;
        Objects.requireNonNull(lazy150);
        arrayMap150.put(ControlPanelWindowManager.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap151 = this.mProviders;
        Lazy<NetworkSpeedController> lazy151 = this.mNetworkSpeedController;
        Objects.requireNonNull(lazy151);
        arrayMap151.put(NetworkSpeedController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap152 = this.mProviders;
        Lazy<ControlCenterActivityStarter> lazy152 = this.mControlCenterActivityStarter;
        Objects.requireNonNull(lazy152);
        arrayMap152.put(ControlCenterActivityStarter.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap153 = this.mProviders;
        Lazy<ExpandInfoController> lazy153 = this.mExpandInfoController;
        Objects.requireNonNull(lazy153);
        arrayMap153.put(ExpandInfoController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap154 = this.mProviders;
        Lazy<ControlsPluginManager> lazy154 = this.mControlsPluginManager;
        Objects.requireNonNull(lazy154);
        arrayMap154.put(ControlsPluginManager.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap155 = this.mProviders;
        Lazy<AppMiniWindowManager> lazy155 = this.mAppMiniWindowManager;
        Objects.requireNonNull(lazy155);
        arrayMap155.put(AppMiniWindowManager.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap156 = this.mProviders;
        Lazy<ModalController> lazy156 = this.mModalController;
        Objects.requireNonNull(lazy156);
        arrayMap156.put(ModalController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap157 = this.mProviders;
        Lazy<FiveGControllerImpl> lazy157 = this.mFiveGControllerImpl;
        Objects.requireNonNull(lazy157);
        arrayMap157.put(FiveGControllerImpl.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap158 = this.mProviders;
        Lazy<CallStateControllerImpl> lazy158 = this.mCallStateController;
        Objects.requireNonNull(lazy158);
        arrayMap158.put(CallStateControllerImpl.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap159 = this.mProviders;
        Lazy<RegionController> lazy159 = this.mRegionController;
        Objects.requireNonNull(lazy159);
        arrayMap159.put(RegionController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap160 = this.mProviders;
        Lazy<CustomCarrierObserver> lazy160 = this.mCustomCarrierObserver;
        Objects.requireNonNull(lazy160);
        arrayMap160.put(CustomCarrierObserver.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap161 = this.mProviders;
        Lazy<CarrierObserver> lazy161 = this.mCarrierObserver;
        Objects.requireNonNull(lazy161);
        arrayMap161.put(CarrierObserver.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap162 = this.mProviders;
        Lazy<MiuiCarrierTextController> lazy162 = this.mMiuiCarrierTextController;
        Objects.requireNonNull(lazy162);
        arrayMap162.put(MiuiCarrierTextController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap163 = this.mProviders;
        Lazy<ToggleManagerController> lazy163 = this.mToggleManagerController;
        Objects.requireNonNull(lazy163);
        arrayMap163.put(ToggleManagerController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap164 = this.mProviders;
        Lazy<IMiuiKeyguardWallpaperController> lazy164 = this.mWallPaperController;
        Objects.requireNonNull(lazy164);
        arrayMap164.put(IMiuiKeyguardWallpaperController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap165 = this.mProviders;
        Lazy<PanelExpansionObserver> lazy165 = this.mPanelExpansionObserver;
        Objects.requireNonNull(lazy165);
        arrayMap165.put(PanelExpansionObserver.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap166 = this.mProviders;
        Lazy<SuperSaveModeController> lazy166 = this.mSuperSaveModeController;
        Objects.requireNonNull(lazy166);
        arrayMap166.put(SuperSaveModeController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap167 = this.mProviders;
        Lazy<DemoModeController> lazy167 = this.mDemoModeController;
        Objects.requireNonNull(lazy167);
        arrayMap167.put(DemoModeController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap168 = this.mProviders;
        Lazy<SlaveWifiSignalController> lazy168 = this.mSlaveWifiSignalController;
        Objects.requireNonNull(lazy168);
        arrayMap168.put(SlaveWifiSignalController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap169 = this.mProviders;
        Lazy<MiuiAlarmControllerImpl> lazy169 = this.mMiuiAlarmControllerImpl;
        Objects.requireNonNull(lazy169);
        arrayMap169.put(MiuiAlarmControllerImpl.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap170 = this.mProviders;
        Lazy<NotificationPanelNavigationBarCoordinator> lazy170 = this.mNotificationNavigationCoordinator;
        Objects.requireNonNull(lazy170);
        arrayMap170.put(NotificationPanelNavigationBarCoordinator.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap171 = this.mProviders;
        Lazy<NCSwitchController> lazy171 = this.mNCSwitchController;
        Objects.requireNonNull(lazy171);
        arrayMap171.put(NCSwitchController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap172 = this.mProviders;
        Lazy<SystemUIStat> lazy172 = this.mSystemUIStat;
        Objects.requireNonNull(lazy172);
        arrayMap172.put(SystemUIStat.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap173 = this.mProviders;
        Lazy<IPhoneSignalController> lazy173 = this.mPhoneSignalController;
        Objects.requireNonNull(lazy173);
        arrayMap173.put(IPhoneSignalController.class, new LazyDependencyCreator() {
            /* class com.android.systemui.$$Lambda$VsMsjQwuYhfrxzUr7AqZvcfoH4 */

            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        sDependency = this;
    }

    /* access modifiers changed from: protected */
    public final <T> T getDependency(Class<T> cls) {
        return (T) getDependencyInner(cls);
    }

    /* access modifiers changed from: protected */
    public final <T> T getDependency(DependencyKey<T> dependencyKey) {
        return (T) getDependencyInner(dependencyKey);
    }

    private synchronized <T> T getDependencyInner(Object obj) {
        T t;
        t = (T) this.mDependencies.get(obj);
        if (t == null) {
            t = (T) createDependency(obj);
            this.mDependencies.put(obj, t);
            if (autoRegisterModulesForDump() && (t instanceof Dumpable)) {
                this.mDumpManager.registerDumpable(t.getClass().getName(), t);
            }
        }
        return t;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public <T> T createDependency(Object obj) {
        Preconditions.checkArgument((obj instanceof DependencyKey) || (obj instanceof Class));
        LazyDependencyCreator lazyDependencyCreator = this.mProviders.get(obj);
        if (lazyDependencyCreator != null) {
            return (T) lazyDependencyCreator.createDependency();
        }
        throw new IllegalArgumentException("Unsupported dependency " + obj + ". " + this.mProviders.size() + " providers known.");
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: java.util.function.Consumer<T> */
    /* JADX WARN: Multi-variable type inference failed */
    private <T> void destroyDependency(Class<T> cls, Consumer<T> consumer) {
        Object remove = this.mDependencies.remove(cls);
        if (remove instanceof Dumpable) {
            this.mDumpManager.unregisterDumpable(remove.getClass().getName());
        }
        if (remove != null && consumer != 0) {
            consumer.accept(remove);
        }
    }

    public static <T> void destroy(Class<T> cls, Consumer<T> consumer) {
        sDependency.destroyDependency(cls, consumer);
    }

    @Deprecated
    public static <T> T get(Class<T> cls) {
        return (T) sDependency.getDependency(cls);
    }

    @Deprecated
    public static <T> T get(DependencyKey<T> dependencyKey) {
        return (T) sDependency.getDependency(dependencyKey);
    }

    public static final class DependencyKey<V> {
        private final String mDisplayName;

        public DependencyKey(String str) {
            this.mDisplayName = str;
        }

        public String toString() {
            return this.mDisplayName;
        }
    }
}
