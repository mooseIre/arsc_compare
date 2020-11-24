package com.android.systemui;

import android.app.ActivityThread;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.os.Process;
import android.os.UserHandle;
import android.util.Log;
import android.util.TimingsTraceLog;
import com.android.systemui.SystemUIAppComponentFactory;
import com.android.systemui.dagger.ContextComponentHelper;
import com.android.systemui.dagger.SystemUIRootComponent;
import com.android.systemui.util.NotificationChannels;
import java.util.Map;
import miui.core.SdkManager;

public class SystemUIApplication extends Application implements SystemUIAppComponentFactory.ContextInitializer {
    private static Context sContext;
    /* access modifiers changed from: private */
    public BootCompleteCacheImpl mBootCompleteCache;
    private ContextComponentHelper mComponentHelper;
    private SystemUIAppComponentFactory.ContextAvailableCallback mContextAvailableCallback;
    private SystemUIRootComponent mRootComponent;
    /* access modifiers changed from: private */
    public SystemUI[] mServices;
    /* access modifiers changed from: private */
    public boolean mServicesStarted;

    public SystemUIApplication() {
        SdkManager.initialize(this, (Map) null);
        Log.v("SystemUIService", "SystemUIApplication constructed.");
    }

    public void onCreate() {
        super.onCreate();
        SdkManager.start((Map) null);
        Log.v("SystemUIService", "SystemUIApplication created.");
        TimingsTraceLog timingsTraceLog = new TimingsTraceLog("SystemUIBootTiming", 4096);
        timingsTraceLog.traceBegin("DependencyInjection");
        sContext = getApplicationContext();
        this.mContextAvailableCallback.onContextAvailable(this);
        SystemUIRootComponent rootComponent = SystemUIFactory.getInstance().getRootComponent();
        this.mRootComponent = rootComponent;
        this.mComponentHelper = rootComponent.getContextComponentHelper();
        this.mBootCompleteCache = this.mRootComponent.provideBootCacheImpl();
        timingsTraceLog.traceEnd();
        setTheme(C0019R$style.Theme_SystemUI);
        if (Process.myUserHandle().equals(UserHandle.SYSTEM)) {
            IntentFilter intentFilter = new IntentFilter("android.intent.action.BOOT_COMPLETED");
            intentFilter.setPriority(1000);
            registerReceiver(new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if (!SystemUIApplication.this.mBootCompleteCache.isBootComplete()) {
                        SystemUIApplication.this.unregisterReceiver(this);
                        SystemUIApplication.this.mBootCompleteCache.setBootComplete();
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
                    if ("android.intent.action.LOCALE_CHANGED".equals(intent.getAction()) && SystemUIApplication.this.mBootCompleteCache.isBootComplete()) {
                        NotificationChannels.createAll(context);
                    }
                }
            }, new IntentFilter("android.intent.action.LOCALE_CHANGED"));
            return;
        }
        String currentProcessName = ActivityThread.currentProcessName();
        ApplicationInfo applicationInfo = getApplicationInfo();
        if (currentProcessName != null) {
            if (currentProcessName.startsWith(applicationInfo.processName + ":")) {
                return;
            }
        }
        startSecondaryUserServicesIfNeeded();
    }

    public static Context getContext() {
        return sContext;
    }

    public void startServicesIfNeeded() {
        startServicesIfNeeded("StartServices", SystemUIFactory.getInstance().getSystemUIServiceComponents(getResources()));
    }

    /* access modifiers changed from: package-private */
    public void startSecondaryUserServicesIfNeeded() {
        startServicesIfNeeded("StartSecondaryServices", SystemUIFactory.getInstance().getSystemUIServiceComponentsPerUser(getResources()));
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v16, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v7, resolved type: com.android.systemui.SystemUI} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void startServicesIfNeeded(java.lang.String r14, java.lang.String[] r15) {
        /*
            r13 = this;
            boolean r0 = r13.mServicesStarted
            if (r0 == 0) goto L_0x0005
            return
        L_0x0005:
            int r0 = r15.length
            com.android.systemui.SystemUI[] r0 = new com.android.systemui.SystemUI[r0]
            r13.mServices = r0
            com.android.systemui.BootCompleteCacheImpl r0 = r13.mBootCompleteCache
            boolean r0 = r0.isBootComplete()
            if (r0 != 0) goto L_0x0025
            java.lang.String r0 = "sys.boot_completed"
            java.lang.String r0 = android.os.SystemProperties.get(r0)
            java.lang.String r1 = "1"
            boolean r0 = r1.equals(r0)
            if (r0 == 0) goto L_0x0025
            com.android.systemui.BootCompleteCacheImpl r0 = r13.mBootCompleteCache
            r0.setBootComplete()
        L_0x0025:
            com.android.systemui.dagger.SystemUIRootComponent r0 = r13.mRootComponent
            com.android.systemui.dump.DumpManager r0 = r0.createDumpManager()
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "Starting SystemUI services for user "
            r1.append(r2)
            android.os.UserHandle r2 = android.os.Process.myUserHandle()
            int r2 = r2.getIdentifier()
            r1.append(r2)
            java.lang.String r2 = "."
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            java.lang.String r2 = "SystemUIService"
            android.util.Log.v(r2, r1)
            android.util.TimingsTraceLog r1 = new android.util.TimingsTraceLog
            r3 = 4096(0x1000, double:2.0237E-320)
            java.lang.String r5 = "SystemUIBootTiming"
            r1.<init>(r5, r3)
            r1.traceBegin(r14)
            int r3 = r15.length
            r4 = 0
            r5 = r4
        L_0x005d:
            r6 = 1
            if (r5 >= r3) goto L_0x0100
            r7 = r15[r5]
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            r8.append(r14)
            r8.append(r7)
            java.lang.String r8 = r8.toString()
            r1.traceBegin(r8)
            long r8 = java.lang.System.currentTimeMillis()
            com.android.systemui.dagger.ContextComponentHelper r10 = r13.mComponentHelper     // Catch:{ ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException -> 0x00f9 }
            com.android.systemui.SystemUI r10 = r10.resolveSystemUI(r7)     // Catch:{ ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException -> 0x00f9 }
            if (r10 != 0) goto L_0x0099
            java.lang.Class r10 = java.lang.Class.forName(r7)     // Catch:{ ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException -> 0x00f9 }
            java.lang.Class[] r11 = new java.lang.Class[r6]     // Catch:{ ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException -> 0x00f9 }
            java.lang.Class<android.content.Context> r12 = android.content.Context.class
            r11[r4] = r12     // Catch:{ ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException -> 0x00f9 }
            java.lang.reflect.Constructor r10 = r10.getConstructor(r11)     // Catch:{ ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException -> 0x00f9 }
            java.lang.Object[] r6 = new java.lang.Object[r6]     // Catch:{ ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException -> 0x00f9 }
            r6[r4] = r13     // Catch:{ ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException -> 0x00f9 }
            java.lang.Object r6 = r10.newInstance(r6)     // Catch:{ ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException -> 0x00f9 }
            r10 = r6
            com.android.systemui.SystemUI r10 = (com.android.systemui.SystemUI) r10     // Catch:{ ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException -> 0x00f9 }
        L_0x0099:
            com.android.systemui.SystemUI[] r6 = r13.mServices     // Catch:{ ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException -> 0x00f9 }
            r6[r5] = r10     // Catch:{ ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException -> 0x00f9 }
            com.android.systemui.SystemUI[] r6 = r13.mServices
            r6 = r6[r5]
            r6.start()
            r1.traceEnd()
            long r10 = java.lang.System.currentTimeMillis()
            long r10 = r10 - r8
            r8 = 1000(0x3e8, double:4.94E-321)
            int r6 = (r10 > r8 ? 1 : (r10 == r8 ? 0 : -1))
            if (r6 <= 0) goto L_0x00d3
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r8 = "Initialization of "
            r6.append(r8)
            r6.append(r7)
            java.lang.String r7 = " took "
            r6.append(r7)
            r6.append(r10)
            java.lang.String r7 = " ms"
            r6.append(r7)
            java.lang.String r6 = r6.toString()
            android.util.Log.w(r2, r6)
        L_0x00d3:
            com.android.systemui.BootCompleteCacheImpl r6 = r13.mBootCompleteCache
            boolean r6 = r6.isBootComplete()
            if (r6 == 0) goto L_0x00e2
            com.android.systemui.SystemUI[] r6 = r13.mServices
            r6 = r6[r5]
            r6.onBootCompleted()
        L_0x00e2:
            com.android.systemui.SystemUI[] r6 = r13.mServices
            r6 = r6[r5]
            java.lang.Class r6 = r6.getClass()
            java.lang.String r6 = r6.getName()
            com.android.systemui.SystemUI[] r7 = r13.mServices
            r7 = r7[r5]
            r0.registerDumpable(r6, r7)
            int r5 = r5 + 1
            goto L_0x005d
        L_0x00f9:
            r13 = move-exception
            java.lang.RuntimeException r14 = new java.lang.RuntimeException
            r14.<init>(r13)
            throw r14
        L_0x0100:
            com.android.systemui.dagger.SystemUIRootComponent r14 = r13.mRootComponent
            com.android.systemui.InitController r14 = r14.getInitController()
            r14.executePostInitTasks()
            r1.traceEnd()
            r13.mServicesStarted = r6
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.SystemUIApplication.startServicesIfNeeded(java.lang.String, java.lang.String[]):void");
    }

    public void onConfigurationChanged(Configuration configuration) {
        if (this.mServicesStarted) {
            this.mRootComponent.getConfigurationController().onConfigurationChanged(configuration);
            int length = this.mServices.length;
            for (int i = 0; i < length; i++) {
                SystemUI[] systemUIArr = this.mServices;
                if (systemUIArr[i] != null) {
                    systemUIArr[i].onConfigurationChanged(configuration);
                }
            }
        }
    }

    public void setContextAvailableCallback(SystemUIAppComponentFactory.ContextAvailableCallback contextAvailableCallback) {
        this.mContextAvailableCallback = contextAvailableCallback;
    }
}
