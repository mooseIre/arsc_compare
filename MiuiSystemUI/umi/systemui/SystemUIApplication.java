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
import com.android.systemui.miui.Dependencies;
import com.android.systemui.miui.PackageEventController;
import com.android.systemui.miui.PackageEventReceiver;
import com.android.systemui.miui.analytics.AnalyticsWrapper;
import com.android.systemui.miui.statusbar.DependenciesSetup;
import com.android.systemui.plugins.OverlayPlugin;
import com.android.systemui.plugins.PluginListener;
import com.android.systemui.plugins.PluginManager;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarWindowManager;
import com.android.systemui.statusbar.policy.EncryptionHelper;
import com.android.systemui.util.NotificationChannels;
import com.miui.systemui.gen.SystemUIDependencies;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import miui.external.ApplicationDelegate;

public class SystemUIApplication extends ApplicationDelegate implements SysUiServiceProvider, PackageEventReceiver {
    private final Class<?>[] BASE_SERVICES;
    private final Class<?>[] SERVICES;
    private final Class<?>[] SERVICES_PER_USER;
    /* access modifiers changed from: private */
    public boolean mBootCompleted;
    private final Map<Class<?>, Object> mComponents = new HashMap();
    /* access modifiers changed from: private */
    public SystemUI[] mServices;
    /* access modifiers changed from: private */
    public boolean mServicesStarted;

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v1, resolved type: java.lang.Class<?>[]} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v18, resolved type: java.lang.Class<?>[]} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public SystemUIApplication() {
        /*
            r10 = this;
            java.lang.Class<com.android.systemui.recents.Recents> r0 = com.android.systemui.recents.Recents.class
            java.lang.Class<com.android.systemui.util.NotificationChannels> r1 = com.android.systemui.util.NotificationChannels.class
            java.lang.Class<com.android.systemui.DependencyUI> r2 = com.android.systemui.DependencyUI.class
            r10.<init>()
            r3 = 16
            java.lang.Class[] r3 = new java.lang.Class[r3]
            r4 = 0
            r3[r4] = r2
            r5 = 1
            r3[r5] = r1
            java.lang.Class<com.android.systemui.statusbar.CommandQueue$CommandQueueStart> r6 = com.android.systemui.statusbar.CommandQueue.CommandQueueStart.class
            r7 = 2
            r3[r7] = r6
            java.lang.Class<com.android.systemui.keyguard.KeyguardViewMediator> r6 = com.android.systemui.keyguard.KeyguardViewMediator.class
            r8 = 3
            r3[r8] = r6
            r6 = 4
            r3[r6] = r0
            r6 = 5
            java.lang.Class<com.android.systemui.volume.VolumeUI> r9 = com.android.systemui.volume.VolumeUI.class
            r3[r6] = r9
            r6 = 6
            java.lang.Class<com.android.systemui.stackdivider.Divider> r9 = com.android.systemui.stackdivider.Divider.class
            r3[r6] = r9
            r6 = 7
            java.lang.Class<com.android.systemui.SystemBars> r9 = com.android.systemui.SystemBars.class
            r3[r6] = r9
            r6 = 8
            java.lang.Class<com.android.systemui.usb.StorageNotification> r9 = com.android.systemui.usb.StorageNotification.class
            r3[r6] = r9
            r6 = 9
            java.lang.Class<com.android.systemui.power.PowerUI> r9 = com.android.systemui.power.PowerUI.class
            r3[r6] = r9
            r6 = 10
            java.lang.Class<com.android.systemui.media.RingtonePlayer> r9 = com.android.systemui.media.RingtonePlayer.class
            r3[r6] = r9
            r6 = 11
            java.lang.Class<com.android.systemui.VendorServices> r9 = com.android.systemui.VendorServices.class
            r3[r6] = r9
            r6 = 12
            java.lang.Class<com.android.systemui.util.leak.GarbageMonitor$Service> r9 = com.android.systemui.util.leak.GarbageMonitor.Service.class
            r3[r6] = r9
            r6 = 13
            java.lang.Class<com.android.systemui.LatencyTester> r9 = com.android.systemui.LatencyTester.class
            r3[r6] = r9
            r6 = 14
            java.lang.Class<com.android.systemui.RoundedCorners> r9 = com.android.systemui.RoundedCorners.class
            r3[r6] = r9
            r6 = 15
            java.lang.Class<com.android.systemui.statusbar.notification.NotificationCenter> r9 = com.android.systemui.statusbar.notification.NotificationCenter.class
            r3[r6] = r9
            r10.BASE_SERVICES = r3
            com.android.systemui.miui.Dependencies r6 = com.android.systemui.miui.Dependencies.getInstance()
            java.lang.Class<com.android.systemui.SystemUI> r9 = com.android.systemui.SystemUI.class
            java.util.Set r6 = r6.getAllClassesFor(r9)
            java.lang.Class[] r9 = new java.lang.Class[r4]
            java.lang.Object[] r6 = r6.toArray(r9)
            java.lang.Class[] r6 = (java.lang.Class[]) r6
            java.lang.Object[] r3 = com.android.systemui.util.Utils.arrayConcat(r3, r6)
            java.lang.Class[] r3 = (java.lang.Class[]) r3
            r10.SERVICES = r3
            java.lang.Class[] r6 = new java.lang.Class[r8]
            r6[r4] = r2
            r6[r5] = r1
            r6[r7] = r0
            r10.SERVICES_PER_USER = r6
            int r0 = r3.length
            com.android.systemui.SystemUI[] r0 = new com.android.systemui.SystemUI[r0]
            r10.mServices = r0
            java.util.HashMap r0 = new java.util.HashMap
            r0.<init>()
            r10.mComponents = r0
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.SystemUIApplication.<init>():void");
    }

    static {
        Dependencies.initialize(new SystemUIDependencies());
    }

    public void onCreate() {
        super.onCreate();
        setTheme(R.style.Theme);
        ((DependenciesSetup) Dependencies.getInstance().get(DependenciesSetup.class, "")).setContext(this);
        SystemUIFactory.createFromConfig(this);
        AnalyticsWrapper.init(this);
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
                    Class cls = StatusBarWindowManager.class;
                    StatusBar statusBar = (StatusBar) SystemUIApplication.this.getComponent(StatusBar.class);
                    if (statusBar != null) {
                        overlayPlugin.setup(statusBar.getStatusBarWindow(), statusBar.getNavigationBarView());
                    }
                    if (this.mOverlays == null) {
                        this.mOverlays = new ArraySet<>();
                    }
                    if (overlayPlugin.holdStatusBarOpen()) {
                        this.mOverlays.add(overlayPlugin);
                        ((StatusBarWindowManager) Dependency.get(cls)).setStateListener(new StatusBarWindowManager.OtherwisedCollapsedListener() {
                            public void setWouldOtherwiseCollapse(boolean z) {
                                Iterator it = AnonymousClass3.this.mOverlays.iterator();
                                while (it.hasNext()) {
                                    ((OverlayPlugin) it.next()).setCollapseDesired(z);
                                }
                            }
                        });
                        ((StatusBarWindowManager) Dependency.get(cls)).setForcePluginOpen(this.mOverlays.size() != 0);
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
