package com.android.systemui;

import android.app.ActivityThread;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Process;
import android.os.SystemProperties;
import android.os.UserHandleCompat;
import android.util.ArraySet;
import android.util.Log;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.media.RingtonePlayer;
import com.android.systemui.miui.Dependencies;
import com.android.systemui.miui.PackageEventController;
import com.android.systemui.miui.PackageEventReceiver;
import com.android.systemui.miui.statusbar.DependenciesSetup;
import com.android.systemui.miui.statusbar.analytics.StatManager;
import com.android.systemui.plugins.OverlayPlugin;
import com.android.systemui.plugins.PluginListener;
import com.android.systemui.plugins.PluginManager;
import com.android.systemui.plugins.R;
import com.android.systemui.power.PowerUI;
import com.android.systemui.recents.Recents;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.notification.NotificationCenter;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarWindowManager;
import com.android.systemui.statusbar.policy.EncryptionHelper;
import com.android.systemui.usb.StorageNotification;
import com.android.systemui.util.NotificationChannels;
import com.android.systemui.util.Utils;
import com.android.systemui.util.leak.GarbageMonitor;
import com.android.systemui.volume.VolumeUI;
import com.miui.systemui.gen.SystemUIDependencies;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import miui.external.ApplicationDelegate;

public class SystemUIApplication extends ApplicationDelegate implements SysUiServiceProvider, PackageEventReceiver {
    private final Class<?>[] BASE_SERVICES = {DependencyUI.class, NotificationChannels.class, CommandQueue.CommandQueueStart.class, KeyguardViewMediator.class, Recents.class, VolumeUI.class, Divider.class, SystemBars.class, StorageNotification.class, PowerUI.class, RingtonePlayer.class, VendorServices.class, GarbageMonitor.Service.class, LatencyTester.class, RoundedCorners.class, NotificationCenter.class};
    private final Class<?>[] SERVICES = ((Class[]) Utils.arrayConcat(this.BASE_SERVICES, (Class[]) Dependencies.getInstance().getAllClassesFor(SystemUI.class).toArray(new Class[0])));
    private final Class<?>[] SERVICES_PER_USER = {DependencyUI.class, NotificationChannels.class, Recents.class};
    /* access modifiers changed from: private */
    public boolean mBootCompleted;
    private final Map<Class<?>, Object> mComponents = new HashMap();
    /* access modifiers changed from: private */
    public SystemUI[] mServices = new SystemUI[this.SERVICES.length];
    /* access modifiers changed from: private */
    public boolean mServicesStarted;

    static {
        Dependencies.initialize(new SystemUIDependencies());
    }

    public void onCreate() {
        super.onCreate();
        setTheme(R.style.Theme);
        ((DependenciesSetup) Dependencies.getInstance().get(DependenciesSetup.class, "")).setContext(this);
        SystemUIFactory.createFromConfig(this);
        StatManager.init(this);
        if (Process.myUserHandle().equals(UserHandleCompat.SYSTEM)) {
            IntentFilter intentFilter = new IntentFilter("android.intent.action.BOOT_COMPLETED");
            intentFilter.setPriority(1000);
            registerReceiver(new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if (!SystemUIApplication.this.mBootCompleted) {
                        Log.v("SystemUIService", "BOOT_COMPLETED received");
                        SystemUIApplication.this.unregisterReceiver(this);
                        boolean unused = SystemUIApplication.this.mBootCompleted = true;
                        if (SystemUIApplication.this.mServicesStarted) {
                            for (SystemUI onBootCompleted : SystemUIApplication.this.mServices) {
                                onBootCompleted.onBootCompleted();
                            }
                        }
                    }
                }
            }, intentFilter);
            registerReceiver(new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if ("android.intent.action.LOCALE_CHANGED".equals(intent.getAction()) && SystemUIApplication.this.mBootCompleted) {
                        NotificationChannels.createAll(context);
                    }
                }
            }, new IntentFilter("android.intent.action.LOCALE_CHANGED"));
        } else {
            String currentProcessName = ActivityThread.currentProcessName();
            ApplicationInfo applicationInfo = getApplicationInfo();
            if (currentProcessName != null) {
                if (currentProcessName.startsWith(applicationInfo.processName + ":")) {
                    return;
                }
            }
            startServicesIfNeeded(this.SERVICES_PER_USER);
        }
        new PackageEventController(this, this, (Handler) null).start();
    }

    public void startServicesIfNeeded() {
        startServicesIfNeeded(this.SERVICES);
    }

    /* access modifiers changed from: package-private */
    public void startSecondaryUserServicesIfNeeded() {
        startServicesIfNeeded(this.SERVICES_PER_USER);
    }

    private void startServicesIfNeeded(Class<?>[] clsArr) {
        if (EncryptionHelper.systemNotReady()) {
            Log.e("SystemUIService", "abort starting service, system not ready due to data encryption");
        } else if (!this.mServicesStarted) {
            if (!this.mBootCompleted && "1".equals(SystemProperties.get("sys.boot_completed"))) {
                this.mBootCompleted = true;
                Log.v("SystemUIService", "BOOT_COMPLETED was already sent");
            }
            Log.v("SystemUIService", "Starting SystemUI services for user " + Process.myUserHandle().getIdentifier() + ".");
            int length = clsArr.length;
            int i = 0;
            while (i < length) {
                Class<?> cls = clsArr[i];
                Log.d("SystemUIService", "loading: " + cls);
                try {
                    Object createInstance = SystemUIFactory.getInstance().createInstance(cls);
                    SystemUI[] systemUIArr = this.mServices;
                    if (createInstance == null) {
                        createInstance = cls.newInstance();
                    }
                    systemUIArr[i] = (SystemUI) createInstance;
                    SystemUI[] systemUIArr2 = this.mServices;
                    systemUIArr2[i].mContext = this;
                    systemUIArr2[i].mComponents = this.mComponents;
                    Log.d("SystemUIService", "running: " + this.mServices[i]);
                    this.mServices[i].start();
                    if (this.mBootCompleted) {
                        this.mServices[i].onBootCompleted();
                    }
                    i++;
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InstantiationException e2) {
                    throw new RuntimeException(e2);
                }
            }
            ((PluginManager) Dependency.get(PluginManager.class)).addPluginListener(new PluginListener<OverlayPlugin>() {
                /* access modifiers changed from: private */
                public ArraySet<OverlayPlugin> mOverlays;

                public void onPluginConnected(OverlayPlugin overlayPlugin, Context context) {
                    StatusBar statusBar = (StatusBar) SystemUIApplication.this.getComponent(StatusBar.class);
                    if (statusBar != null) {
                        overlayPlugin.setup(statusBar.getStatusBarWindow(), statusBar.getNavigationBarView());
                    }
                    if (this.mOverlays == null) {
                        this.mOverlays = new ArraySet<>();
                    }
                    if (overlayPlugin.holdStatusBarOpen()) {
                        this.mOverlays.add(overlayPlugin);
                        ((StatusBarWindowManager) Dependency.get(StatusBarWindowManager.class)).setStateListener(new StatusBarWindowManager.OtherwisedCollapsedListener() {
                            public void setWouldOtherwiseCollapse(boolean z) {
                                Iterator it = AnonymousClass3.this.mOverlays.iterator();
                                while (it.hasNext()) {
                                    ((OverlayPlugin) it.next()).setCollapseDesired(z);
                                }
                            }
                        });
                        ((StatusBarWindowManager) Dependency.get(StatusBarWindowManager.class)).setForcePluginOpen(this.mOverlays.size() != 0);
                    }
                }

                public void onPluginDisconnected(OverlayPlugin overlayPlugin) {
                    this.mOverlays.remove(overlayPlugin);
                    ((StatusBarWindowManager) Dependency.get(StatusBarWindowManager.class)).setForcePluginOpen(this.mOverlays.size() != 0);
                }
            }, (Class<?>) OverlayPlugin.class, true);
            this.mServicesStarted = true;
        }
    }

    public void onConfigurationChanged(Configuration configuration) {
        Log.v("SystemUIService", "onConfigurationChanged: " + configuration);
        if (this.mServicesStarted) {
            int length = this.mServices.length;
            for (int i = 0; i < length; i++) {
                SystemUI[] systemUIArr = this.mServices;
                if (systemUIArr[i] != null) {
                    systemUIArr[i].onConfigurationChanged(configuration);
                }
            }
        }
    }

    public void onPackageChanged(int i, String str) {
        if (this.mServicesStarted) {
            for (SystemUI systemUI : this.mServices) {
                if (systemUI != null) {
                    systemUI.onPackageChanged(i, str);
                }
            }
        }
    }

    public void onPackageAdded(int i, String str, boolean z) {
        if (this.mServicesStarted) {
            for (SystemUI systemUI : this.mServices) {
                if (systemUI != null) {
                    systemUI.onPackageAdded(i, str, z);
                }
            }
        }
    }

    public void onPackageRemoved(int i, String str, boolean z, boolean z2) {
        if (this.mServicesStarted) {
            for (SystemUI systemUI : this.mServices) {
                if (systemUI != null) {
                    systemUI.onPackageRemoved(i, str, z, z2);
                }
            }
        }
    }

    public <T> T getComponent(Class<T> cls) {
        return this.mComponents.get(cls);
    }

    public SystemUI[] getServices() {
        return this.mServices;
    }
}
