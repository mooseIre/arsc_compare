package com.android.systemui;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.Preconditions;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.systemui.Dependency;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.fragments.FragmentService;
import com.android.systemui.keyguard.ScreenLifecycle;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.miui.Dependencies;
import com.android.systemui.miui.PackageEventReceiver;
import com.android.systemui.miui.controlcenter.ExpandInfoController;
import com.android.systemui.miui.controlcenter.ExpandInfoControllerImpl;
import com.android.systemui.miui.controls.ControlsPluginManager;
import com.android.systemui.miui.statusbar.ControlCenterActivityStarter;
import com.android.systemui.miui.statusbar.analytics.NotificationStat;
import com.android.systemui.miui.statusbar.analytics.SystemUIStat;
import com.android.systemui.miui.statusbar.phone.ControlPanelWindowManager;
import com.android.systemui.miui.statusbar.policy.ControlPanelController;
import com.android.systemui.miui.statusbar.policy.OldModeController;
import com.android.systemui.miui.statusbar.policy.SuperSaveModeController;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.PluginDependencyProvider;
import com.android.systemui.plugins.PluginInitializerImpl;
import com.android.systemui.plugins.PluginManager;
import com.android.systemui.plugins.PluginManagerImpl;
import com.android.systemui.plugins.VolumeDialogController;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.statusbar.CallStateController;
import com.android.systemui.statusbar.CallStateControllerImpl;
import com.android.systemui.statusbar.StatusBarStateControllerImpl;
import com.android.systemui.statusbar.phone.ConfigurationControllerImpl;
import com.android.systemui.statusbar.phone.LightBarController;
import com.android.systemui.statusbar.phone.ManagedProfileController;
import com.android.systemui.statusbar.phone.ManagedProfileControllerImpl;
import com.android.systemui.statusbar.phone.StatusBarWindowManager;
import com.android.systemui.statusbar.policy.AccessibilityController;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BatteryControllerImpl;
import com.android.systemui.statusbar.policy.BluetoothController;
import com.android.systemui.statusbar.policy.BluetoothControllerImpl;
import com.android.systemui.statusbar.policy.CastController;
import com.android.systemui.statusbar.policy.CastControllerImpl;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.DemoModeController;
import com.android.systemui.statusbar.policy.DemoModeControllerImpl;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.ExtensionController;
import com.android.systemui.statusbar.policy.ExtensionControllerImpl;
import com.android.systemui.statusbar.policy.FlashlightController;
import com.android.systemui.statusbar.policy.FlashlightControllerImpl;
import com.android.systemui.statusbar.policy.HotspotController;
import com.android.systemui.statusbar.policy.HotspotControllerImpl;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import com.android.systemui.statusbar.policy.KeyguardMonitorImpl;
import com.android.systemui.statusbar.policy.KeyguardNotificationController;
import com.android.systemui.statusbar.policy.KeyguardNotificationControllerImpl;
import com.android.systemui.statusbar.policy.LocationController;
import com.android.systemui.statusbar.policy.LocationControllerImpl;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.statusbar.policy.NextAlarmControllerImpl;
import com.android.systemui.statusbar.policy.PaperModeController;
import com.android.systemui.statusbar.policy.PaperModeControllerImpl;
import com.android.systemui.statusbar.policy.RotationLockController;
import com.android.systemui.statusbar.policy.RotationLockControllerImpl;
import com.android.systemui.statusbar.policy.SecurityController;
import com.android.systemui.statusbar.policy.SecurityControllerImpl;
import com.android.systemui.statusbar.policy.SilentModeObserverController;
import com.android.systemui.statusbar.policy.SilentModeObserverControllerImpl;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserInfoControllerImpl;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.statusbar.policy.ZenModeControllerImpl;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.tuner.TunerServiceImpl;
import com.android.systemui.util.leak.GarbageMonitor;
import com.android.systemui.util.leak.LeakDetector;
import com.android.systemui.util.leak.LeakReporter;
import com.android.systemui.volume.VolumeDialogControllerImpl;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;

public class DependencyUI extends SystemUI {
    /* access modifiers changed from: private */
    public static DependencyUI sDependency;
    private final ArrayMap<Object, Object> mDependencies = new ArrayMap<>();
    private final ArrayMap<Object, Dependency.DependencyProvider> mProviders = new ArrayMap<>();

    public void start() {
        sDependency = this;
        Dependency.setDependencyResolver(new Dependency.DependencyResolver() {
            public <T> T get(Class<T> cls) {
                return DependencyUI.sDependency.getDependency(cls);
            }

            public <T> T get(Dependency.DependencyKey<T> dependencyKey) {
                return DependencyUI.sDependency.getDependency(dependencyKey);
            }
        });
        this.mProviders.put(Dependency.TIME_TICK_HANDLER, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return Dependencies.getInstance().get(Handler.class, "TimeTick");
            }
        });
        this.mProviders.put(Dependency.SCREEN_OFF_HANDLER, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return Dependencies.getInstance().get(Handler.class, "ScreenOff");
            }
        });
        this.mProviders.put(Dependency.BG_LOOPER, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return Dependencies.getInstance().get(Looper.class, "SysUiBg");
            }
        });
        this.mProviders.put(Dependency.NET_BG_LOOPER, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return Dependencies.getInstance().get(Looper.class, "SysUiNetBg");
            }
        });
        this.mProviders.put(Dependency.BT_BG_LOOPER, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return Dependencies.getInstance().get(Looper.class, "SysUiBtBg");
            }
        });
        this.mProviders.put(Dependency.MAIN_HANDLER, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return Dependencies.getInstance().get(Handler.class, "main_handler");
            }
        });
        this.mProviders.put(ActivityStarter.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new ActivityStarterDelegate();
            }
        });
        this.mProviders.put(ActivityStarterDelegate.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return DependencyUI.this.getDependency(ActivityStarter.class);
            }
        });
        this.mProviders.put(BluetoothController.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                DependencyUI dependencyUI = DependencyUI.this;
                return new BluetoothControllerImpl(dependencyUI.mContext, (Looper) dependencyUI.getDependency(Dependency.BT_BG_LOOPER));
            }
        });
        this.mProviders.put(LocationController.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                DependencyUI dependencyUI = DependencyUI.this;
                return new LocationControllerImpl(dependencyUI.mContext, (Looper) dependencyUI.getDependency(Dependency.BG_LOOPER));
            }
        });
        this.mProviders.put(RotationLockController.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new RotationLockControllerImpl(DependencyUI.this.mContext);
            }
        });
        this.mProviders.put(ZenModeController.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                DependencyUI dependencyUI = DependencyUI.this;
                return new ZenModeControllerImpl(dependencyUI.mContext, (Handler) dependencyUI.getDependency(Dependency.MAIN_HANDLER));
            }
        });
        this.mProviders.put(HotspotController.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new HotspotControllerImpl(DependencyUI.this.mContext);
            }
        });
        this.mProviders.put(CastController.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new CastControllerImpl(DependencyUI.this.mContext);
            }
        });
        this.mProviders.put(PaperModeController.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                DependencyUI dependencyUI = DependencyUI.this;
                return new PaperModeControllerImpl(dependencyUI.mContext, (Looper) dependencyUI.getDependency(Dependency.NET_BG_LOOPER));
            }
        });
        this.mProviders.put(LightBarController.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new LightBarController(DependencyUI.this.mContext);
            }
        });
        this.mProviders.put(FlashlightController.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new FlashlightControllerImpl(DependencyUI.this.mContext);
            }
        });
        this.mProviders.put(KeyguardMonitor.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new KeyguardMonitorImpl(DependencyUI.this.mContext);
            }
        });
        this.mProviders.put(UserSwitcherController.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                DependencyUI dependencyUI = DependencyUI.this;
                return new UserSwitcherController(dependencyUI.mContext, (KeyguardMonitor) dependencyUI.getDependency(KeyguardMonitor.class), (Handler) DependencyUI.this.getDependency(Dependency.MAIN_HANDLER), (ActivityStarter) DependencyUI.this.getDependency(ActivityStarter.class));
            }
        });
        this.mProviders.put(UserInfoController.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new UserInfoControllerImpl(DependencyUI.this.mContext);
            }
        });
        this.mProviders.put(BatteryController.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new BatteryControllerImpl(DependencyUI.this.mContext);
            }
        });
        this.mProviders.put(ManagedProfileController.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new ManagedProfileControllerImpl(DependencyUI.this.mContext);
            }
        });
        this.mProviders.put(NextAlarmController.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new NextAlarmControllerImpl(DependencyUI.this.mContext);
            }
        });
        this.mProviders.put(AccessibilityController.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new AccessibilityController(DependencyUI.this.mContext);
            }
        });
        this.mProviders.put(PluginManager.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new PluginManagerImpl(DependencyUI.this.mContext, new PluginInitializerImpl());
            }
        });
        this.mProviders.put(SecurityController.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new SecurityControllerImpl(DependencyUI.this.mContext);
            }
        });
        this.mProviders.put(LeakDetector.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return LeakDetector.create();
            }
        });
        this.mProviders.put(Dependency.LEAK_REPORT_EMAIL, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return null;
            }
        });
        this.mProviders.put(LeakReporter.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                DependencyUI dependencyUI = DependencyUI.this;
                return new LeakReporter(dependencyUI.mContext, (LeakDetector) dependencyUI.getDependency(LeakDetector.class), (String) DependencyUI.this.getDependency(Dependency.LEAK_REPORT_EMAIL));
            }
        });
        this.mProviders.put(GarbageMonitor.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new GarbageMonitor((Looper) DependencyUI.this.getDependency(Dependency.BG_LOOPER), (LeakDetector) DependencyUI.this.getDependency(LeakDetector.class), (LeakReporter) DependencyUI.this.getDependency(LeakReporter.class));
            }
        });
        this.mProviders.put(TunerService.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new TunerServiceImpl(DependencyUI.this.mContext);
            }
        });
        this.mProviders.put(StatusBarWindowManager.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new StatusBarWindowManager(DependencyUI.this.mContext);
            }
        });
        this.mProviders.put(ConfigurationController.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new ConfigurationControllerImpl(DependencyUI.this.mContext);
            }
        });
        this.mProviders.put(ScreenLifecycle.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new ScreenLifecycle();
            }
        });
        this.mProviders.put(WakefulnessLifecycle.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new WakefulnessLifecycle();
            }
        });
        this.mProviders.put(FragmentService.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new FragmentService(DependencyUI.this.mContext);
            }
        });
        this.mProviders.put(ExtensionController.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new ExtensionControllerImpl();
            }
        });
        this.mProviders.put(PluginDependencyProvider.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new PluginDependencyProvider((PluginManager) Dependency.get(PluginManager.class));
            }
        });
        this.mProviders.put(LocalBluetoothManager.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return LocalBluetoothManager.getInstance(DependencyUI.this.mContext, (LocalBluetoothManager.BluetoothManagerCallback) null);
            }
        });
        this.mProviders.put(VolumeDialogController.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new VolumeDialogControllerImpl(DependencyUI.this.mContext);
            }
        });
        this.mProviders.put(MetricsLogger.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new MetricsLogger();
            }
        });
        this.mProviders.put(ForegroundServiceController.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new ForegroundServiceControllerImpl(DependencyUI.this.mContext);
            }
        });
        this.mProviders.put(UiOffloadThread.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new UiOffloadThread();
            }
        });
        this.mProviders.put(DemoModeController.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new DemoModeControllerImpl(DependencyUI.this.mContext);
            }
        });
        this.mProviders.put(SilentModeObserverController.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new SilentModeObserverControllerImpl(DependencyUI.this.mContext);
            }
        });
        this.mProviders.put(KeyguardNotificationController.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new KeyguardNotificationControllerImpl();
            }
        });
        this.mProviders.put(CallStateController.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new CallStateControllerImpl();
            }
        });
        this.mProviders.put(SystemUIStat.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new SystemUIStat(DependencyUI.this.mContext);
            }
        });
        this.mProviders.put(NotificationStat.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new NotificationStat(DependencyUI.this.mContext);
            }
        });
        this.mProviders.put(BubbleController.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new BubbleController(DependencyUI.this.mContext);
            }
        });
        this.mProviders.put(StatusBarStateController.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new StatusBarStateControllerImpl();
            }
        });
        this.mProviders.put(SuperSaveModeController.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new SuperSaveModeController(DependencyUI.this.mContext);
            }
        });
        this.mProviders.put(OldModeController.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new OldModeController(DependencyUI.this.mContext);
            }
        });
        this.mProviders.put(OverviewProxyService.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                DependencyUI dependencyUI = DependencyUI.this;
                return new OverviewProxyService(dependencyUI.mContext, (DeviceProvisionedController) dependencyUI.getDependency(DeviceProvisionedController.class));
            }
        });
        this.mProviders.put(ControlPanelController.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new ControlPanelController(DependencyUI.this.mContext);
            }
        });
        this.mProviders.put(ExpandInfoController.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new ExpandInfoControllerImpl(DependencyUI.this.mContext);
            }
        });
        this.mProviders.put(ControlCenterActivityStarter.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new ControlCenterActivityStarter(DependencyUI.this.mContext);
            }
        });
        this.mProviders.put(ControlPanelWindowManager.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new ControlPanelWindowManager(DependencyUI.this.mContext);
            }
        });
        this.mProviders.put(ControlsPluginManager.class, new Dependency.DependencyProvider() {
            public Object createDependency() {
                return new ControlsPluginManager();
            }
        });
        SystemUIFactory.getInstance().injectDependencies(this.mProviders, this.mContext);
    }

    public synchronized void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        super.dump(fileDescriptor, printWriter, strArr);
        printWriter.println("Dumping existing controllers:");
        for (Object next : this.mDependencies.values()) {
            if (next instanceof Dumpable) {
                ((Dumpable) next).dump(fileDescriptor, printWriter, strArr);
            }
        }
    }

    /* access modifiers changed from: protected */
    public synchronized void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        for (Object next : this.mDependencies.values()) {
            if (next instanceof ConfigurationChangedReceiver) {
                ((ConfigurationChangedReceiver) next).onConfigurationChanged(configuration);
            }
        }
    }

    public void onPackageChanged(int i, String str) {
        for (Object next : this.mDependencies.values()) {
            if (next instanceof PackageEventReceiver) {
                ((PackageEventReceiver) next).onPackageChanged(i, str);
            }
        }
    }

    public void onPackageAdded(int i, String str, boolean z) {
        for (Object next : this.mDependencies.values()) {
            if (next instanceof PackageEventReceiver) {
                ((PackageEventReceiver) next).onPackageAdded(i, str, z);
            }
        }
    }

    public void onPackageRemoved(int i, String str, boolean z, boolean z2) {
        for (Object next : this.mDependencies.values()) {
            if (next instanceof PackageEventReceiver) {
                ((PackageEventReceiver) next).onPackageRemoved(i, str, z, z2);
            }
        }
    }

    /* access modifiers changed from: protected */
    public final <T> T getDependency(Class<T> cls) {
        return getDependencyInner(cls);
    }

    /* access modifiers changed from: protected */
    public final <T> T getDependency(Dependency.DependencyKey<T> dependencyKey) {
        return getDependencyInner(dependencyKey);
    }

    private synchronized <T> T getDependencyInner(Object obj) {
        T t;
        t = this.mDependencies.get(obj);
        if (t == null) {
            t = createDependency(obj);
            this.mDependencies.put(obj, t);
        }
        return t;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public <T> T createDependency(Object obj) {
        Preconditions.checkArgument((obj instanceof Dependency.DependencyKey) || (obj instanceof Class));
        Dependency.DependencyProvider dependencyProvider = this.mProviders.get(obj);
        if (dependencyProvider != null) {
            return dependencyProvider.createDependency();
        }
        if (obj instanceof Class) {
            if (Constants.DEBUG) {
                Log.i("Dependency", "get dependency from injected : " + obj);
            }
            return Dependencies.getInstance().get((Class) obj, "");
        }
        throw new IllegalArgumentException("Unsupported dependency " + obj);
    }

    public static void initDependencies(Context context) {
        if (sDependency == null) {
            DependencyUI dependencyUI = new DependencyUI();
            dependencyUI.mContext = context;
            dependencyUI.mComponents = new HashMap();
            dependencyUI.start();
        }
    }
}
