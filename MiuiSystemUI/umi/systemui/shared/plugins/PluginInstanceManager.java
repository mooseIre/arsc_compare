package com.android.systemui.shared.plugins;

import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.plugins.Plugin;
import com.android.systemui.plugins.PluginFragment;
import com.android.systemui.plugins.PluginListener;
import com.android.systemui.shared.plugins.VersionInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class PluginInstanceManager<T extends Plugin> {
    /* access modifiers changed from: private */
    public final boolean isDebuggable;
    /* access modifiers changed from: private */
    public final String mAction;
    /* access modifiers changed from: private */
    public final boolean mAllowMultiple;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public final PluginListener<T> mListener;
    @VisibleForTesting
    final PluginInstanceManager<T>.MainHandler mMainHandler;
    /* access modifiers changed from: private */
    public final PluginManagerImpl mManager;
    @VisibleForTesting
    final PluginInstanceManager<T>.PluginHandler mPluginHandler;
    /* access modifiers changed from: private */
    public final PackageManager mPm;
    /* access modifiers changed from: private */
    public final VersionInfo mVersion;
    private final ArraySet<String> mWhitelistedPlugins;

    PluginInstanceManager(Context context, String str, PluginListener<T> pluginListener, boolean z, Looper looper, VersionInfo versionInfo, PluginManagerImpl pluginManagerImpl) {
        this(context, context.getPackageManager(), str, pluginListener, z, looper, versionInfo, pluginManagerImpl, Build.IS_DEBUGGABLE, pluginManagerImpl.getWhitelistedPlugins());
    }

    @VisibleForTesting
    PluginInstanceManager(Context context, PackageManager packageManager, String str, PluginListener<T> pluginListener, boolean z, Looper looper, VersionInfo versionInfo, PluginManagerImpl pluginManagerImpl, boolean z2, String[] strArr) {
        this.mWhitelistedPlugins = new ArraySet<>();
        this.mMainHandler = new MainHandler(Looper.getMainLooper());
        this.mPluginHandler = new PluginHandler(looper);
        this.mManager = pluginManagerImpl;
        this.mContext = context;
        this.mPm = packageManager;
        this.mAction = str;
        this.mListener = pluginListener;
        this.mAllowMultiple = z;
        this.mVersion = versionInfo;
        this.mWhitelistedPlugins.addAll(Arrays.asList(strArr));
        this.isDebuggable = z2;
    }

    public void loadAll() {
        this.mPluginHandler.sendEmptyMessage(1);
    }

    public void destroy() {
        Iterator it = new ArrayList(this.mPluginHandler.mPlugins).iterator();
        while (it.hasNext()) {
            this.mMainHandler.obtainMessage(2, ((PluginInfo) it.next()).mPlugin).sendToTarget();
        }
    }

    public void onPackageRemoved(String str) {
        this.mPluginHandler.obtainMessage(3, str).sendToTarget();
    }

    public void onPackageChange(String str) {
        this.mPluginHandler.obtainMessage(3, str).sendToTarget();
        this.mPluginHandler.obtainMessage(2, str).sendToTarget();
    }

    public boolean checkAndDisable(String str) {
        Iterator it = new ArrayList(this.mPluginHandler.mPlugins).iterator();
        boolean z = false;
        while (it.hasNext()) {
            PluginInfo pluginInfo = (PluginInfo) it.next();
            if (str.startsWith(pluginInfo.mPackage)) {
                z |= disable(pluginInfo, 2);
            }
        }
        return z;
    }

    public boolean disableAll() {
        ArrayList arrayList = new ArrayList(this.mPluginHandler.mPlugins);
        boolean z = false;
        for (int i = 0; i < arrayList.size(); i++) {
            z |= disable((PluginInfo) arrayList.get(i), 3);
        }
        return z;
    }

    /* access modifiers changed from: private */
    public boolean isPluginWhitelisted(ComponentName componentName) {
        Iterator<String> it = this.mWhitelistedPlugins.iterator();
        while (it.hasNext()) {
            String next = it.next();
            ComponentName unflattenFromString = ComponentName.unflattenFromString(next);
            if (unflattenFromString == null) {
                if (next.equals(componentName.getPackageName())) {
                    return true;
                }
            } else if (unflattenFromString.equals(componentName)) {
                return true;
            }
        }
        return false;
    }

    private boolean disable(PluginInfo pluginInfo, int i) {
        ComponentName componentName = new ComponentName(pluginInfo.mPackage, pluginInfo.mClass);
        if (isPluginWhitelisted(componentName)) {
            return false;
        }
        Log.w("PluginInstanceManager", "Disabling plugin " + componentName.flattenToShortString());
        this.mManager.getPluginEnabler().setDisabled(componentName, i);
        return true;
    }

    public <T> boolean dependsOn(Plugin plugin, Class<T> cls) {
        Iterator it = new ArrayList(this.mPluginHandler.mPlugins).iterator();
        while (it.hasNext()) {
            PluginInfo pluginInfo = (PluginInfo) it.next();
            if (pluginInfo.mPlugin.getClass().getName().equals(plugin.getClass().getName())) {
                if (pluginInfo.mVersion == null || !pluginInfo.mVersion.hasClass(cls)) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    public String toString() {
        return String.format("%s@%s (action=%s)", new Object[]{PluginInstanceManager.class.getSimpleName(), Integer.valueOf(hashCode()), this.mAction});
    }

    private class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                PluginPrefs.setHasPlugins(PluginInstanceManager.this.mContext);
                PluginInfo pluginInfo = (PluginInfo) message.obj;
                PluginInstanceManager.this.mManager.handleWtfs();
                if (!(message.obj instanceof PluginFragment)) {
                    ((Plugin) pluginInfo.mPlugin).onCreate(PluginInstanceManager.this.mContext, pluginInfo.mPluginContext);
                }
                PluginInstanceManager.this.mListener.onPluginConnected((Plugin) pluginInfo.mPlugin, pluginInfo.mPluginContext);
            } else if (i != 2) {
                super.handleMessage(message);
            } else {
                PluginInstanceManager.this.mListener.onPluginDisconnected((Plugin) message.obj);
                Object obj = message.obj;
                if (!(obj instanceof PluginFragment)) {
                    ((Plugin) obj).onDestroy();
                }
            }
        }
    }

    private class PluginHandler extends Handler {
        /* access modifiers changed from: private */
        public final ArrayList<PluginInfo<T>> mPlugins = new ArrayList<>();

        public PluginHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                for (int size = this.mPlugins.size() - 1; size >= 0; size--) {
                    PluginInstanceManager.this.mMainHandler.obtainMessage(2, this.mPlugins.get(size).mPlugin).sendToTarget();
                }
                this.mPlugins.clear();
                handleQueryPlugins((String) null);
            } else if (i == 2) {
                String str = (String) message.obj;
                if (PluginInstanceManager.this.mAllowMultiple || this.mPlugins.size() == 0) {
                    handleQueryPlugins(str);
                }
            } else if (i != 3) {
                super.handleMessage(message);
            } else {
                String str2 = (String) message.obj;
                for (int size2 = this.mPlugins.size() - 1; size2 >= 0; size2--) {
                    PluginInfo pluginInfo = this.mPlugins.get(size2);
                    if (pluginInfo.mPackage.equals(str2)) {
                        PluginInstanceManager.this.mMainHandler.obtainMessage(2, pluginInfo.mPlugin).sendToTarget();
                        this.mPlugins.remove(size2);
                    }
                }
            }
        }

        private void handleQueryPlugins(String str) {
            Intent intent = new Intent(PluginInstanceManager.this.mAction);
            if (str != null) {
                intent.setPackage(str);
            }
            List<ResolveInfo> queryIntentServices = PluginInstanceManager.this.mPm.queryIntentServices(intent, 0);
            if (queryIntentServices.size() <= 1 || PluginInstanceManager.this.mAllowMultiple) {
                for (ResolveInfo resolveInfo : queryIntentServices) {
                    ServiceInfo serviceInfo = resolveInfo.serviceInfo;
                    PluginInfo handleLoadPlugin = handleLoadPlugin(new ComponentName(serviceInfo.packageName, serviceInfo.name));
                    if (handleLoadPlugin != null) {
                        this.mPlugins.add(handleLoadPlugin);
                        PluginInstanceManager.this.mMainHandler.obtainMessage(1, handleLoadPlugin).sendToTarget();
                    }
                }
                return;
            }
            Log.w("PluginInstanceManager", "Multiple plugins found for " + PluginInstanceManager.this.mAction);
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
        /* JADX WARNING: Missing exception handler attribute for start block: B:29:0x011d */
        /* JADX WARNING: Removed duplicated region for block: B:33:0x0125 A[SYNTHETIC, Splitter:B:33:0x0125] */
        /* JADX WARNING: Removed duplicated region for block: B:35:0x0156 A[Catch:{ all -> 0x01ff }] */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public com.android.systemui.shared.plugins.PluginInstanceManager.PluginInfo<T> handleLoadPlugin(android.content.ComponentName r19) {
            /*
                r18 = this;
                r1 = r18
                r2 = r19
                java.lang.String r3 = "android"
                com.android.systemui.shared.plugins.PluginInstanceManager r0 = com.android.systemui.shared.plugins.PluginInstanceManager.this
                boolean r0 = r0.isDebuggable
                java.lang.String r4 = "PluginInstanceManager"
                r5 = 0
                if (r0 != 0) goto L_0x002e
                com.android.systemui.shared.plugins.PluginInstanceManager r0 = com.android.systemui.shared.plugins.PluginInstanceManager.this
                boolean r0 = r0.isPluginWhitelisted(r2)
                if (r0 != 0) goto L_0x002e
                java.lang.StringBuilder r0 = new java.lang.StringBuilder
                r0.<init>()
                java.lang.String r1 = "Plugin cannot be loaded on production build: "
                r0.append(r1)
                r0.append(r2)
                java.lang.String r0 = r0.toString()
                android.util.Log.w(r4, r0)
                return r5
            L_0x002e:
                com.android.systemui.shared.plugins.PluginInstanceManager r0 = com.android.systemui.shared.plugins.PluginInstanceManager.this
                com.android.systemui.shared.plugins.PluginManagerImpl r0 = r0.mManager
                com.android.systemui.shared.plugins.PluginEnabler r0 = r0.getPluginEnabler()
                boolean r0 = r0.isEnabled(r2)
                if (r0 != 0) goto L_0x003f
                return r5
            L_0x003f:
                java.lang.String r12 = r19.getPackageName()
                java.lang.String r13 = r19.getClassName()
                com.android.systemui.shared.plugins.PluginInstanceManager r0 = com.android.systemui.shared.plugins.PluginInstanceManager.this     // Catch:{ all -> 0x01ff }
                android.content.pm.PackageManager r0 = r0.mPm     // Catch:{ all -> 0x01ff }
                r14 = 0
                android.content.pm.ApplicationInfo r0 = r0.getApplicationInfo(r12, r14)     // Catch:{ all -> 0x01ff }
                com.android.systemui.shared.plugins.PluginInstanceManager r6 = com.android.systemui.shared.plugins.PluginInstanceManager.this     // Catch:{ all -> 0x01ff }
                android.content.pm.PackageManager r6 = r6.mPm     // Catch:{ all -> 0x01ff }
                java.lang.String r7 = "com.android.systemui.permission.PLUGIN"
                int r6 = r6.checkPermission(r7, r12)     // Catch:{ all -> 0x01ff }
                if (r6 == 0) goto L_0x0075
                java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ all -> 0x01ff }
                r0.<init>()     // Catch:{ all -> 0x01ff }
                java.lang.String r1 = "Plugin doesn't have permission: "
                r0.append(r1)     // Catch:{ all -> 0x01ff }
                r0.append(r12)     // Catch:{ all -> 0x01ff }
                java.lang.String r0 = r0.toString()     // Catch:{ all -> 0x01ff }
                android.util.Log.d(r4, r0)     // Catch:{ all -> 0x01ff }
                return r5
            L_0x0075:
                com.android.systemui.shared.plugins.PluginInstanceManager r6 = com.android.systemui.shared.plugins.PluginInstanceManager.this     // Catch:{ all -> 0x01ff }
                com.android.systemui.shared.plugins.PluginManagerImpl r6 = r6.mManager     // Catch:{ all -> 0x01ff }
                java.lang.ClassLoader r6 = r6.getClassLoader(r0)     // Catch:{ all -> 0x01ff }
                com.android.systemui.shared.plugins.PluginInstanceManager$PluginContextWrapper r10 = new com.android.systemui.shared.plugins.PluginInstanceManager$PluginContextWrapper     // Catch:{ all -> 0x01ff }
                com.android.systemui.shared.plugins.PluginInstanceManager r7 = com.android.systemui.shared.plugins.PluginInstanceManager.this     // Catch:{ all -> 0x01ff }
                android.content.Context r7 = r7.mContext     // Catch:{ all -> 0x01ff }
                android.content.Context r0 = r7.createApplicationContext(r0, r14)     // Catch:{ all -> 0x01ff }
                r10.<init>(r0, r6)     // Catch:{ all -> 0x01ff }
                r15 = 1
                java.lang.Class r0 = java.lang.Class.forName(r13, r15, r6)     // Catch:{ all -> 0x01ff }
                java.lang.Object r6 = r0.newInstance()     // Catch:{ all -> 0x01ff }
                r11 = r6
                com.android.systemui.plugins.Plugin r11 = (com.android.systemui.plugins.Plugin) r11     // Catch:{ all -> 0x01ff }
                com.android.systemui.shared.plugins.PluginInstanceManager r6 = com.android.systemui.shared.plugins.PluginInstanceManager.this     // Catch:{ InvalidVersionException -> 0x00b4 }
                com.android.systemui.shared.plugins.VersionInfo r6 = r6.mVersion     // Catch:{ InvalidVersionException -> 0x00b4 }
                com.android.systemui.shared.plugins.VersionInfo r0 = r1.checkVersion(r0, r11, r6)     // Catch:{ InvalidVersionException -> 0x00b4 }
                com.android.systemui.shared.plugins.PluginInstanceManager$PluginInfo r16 = new com.android.systemui.shared.plugins.PluginInstanceManager$PluginInfo     // Catch:{ InvalidVersionException -> 0x00b4 }
                r6 = r16
                r7 = r12
                r8 = r13
                r9 = r11
                r17 = r11
                r11 = r0
                r6.<init>(r7, r8, r9, r10, r11)     // Catch:{ InvalidVersionException -> 0x00b2 }
                return r16
            L_0x00b2:
                r0 = move-exception
                goto L_0x00b7
            L_0x00b4:
                r0 = move-exception
                r17 = r11
            L_0x00b7:
                android.content.res.Resources r6 = android.content.res.Resources.getSystem()     // Catch:{ all -> 0x01ff }
                java.lang.String r7 = "stat_sys_warning"
                java.lang.String r8 = "drawable"
                int r6 = r6.getIdentifier(r7, r8, r3)     // Catch:{ all -> 0x01ff }
                android.content.res.Resources r7 = android.content.res.Resources.getSystem()     // Catch:{ all -> 0x01ff }
                java.lang.String r8 = "system_notification_accent_color"
                java.lang.String r9 = "color"
                int r3 = r7.getIdentifier(r8, r9, r3)     // Catch:{ all -> 0x01ff }
                android.app.Notification$Builder r7 = new android.app.Notification$Builder     // Catch:{ all -> 0x01ff }
                com.android.systemui.shared.plugins.PluginInstanceManager r8 = com.android.systemui.shared.plugins.PluginInstanceManager.this     // Catch:{ all -> 0x01ff }
                android.content.Context r8 = r8.mContext     // Catch:{ all -> 0x01ff }
                java.lang.String r9 = "ALR"
                r7.<init>(r8, r9)     // Catch:{ all -> 0x01ff }
                android.app.Notification$BigTextStyle r8 = new android.app.Notification$BigTextStyle     // Catch:{ all -> 0x01ff }
                r8.<init>()     // Catch:{ all -> 0x01ff }
                android.app.Notification$Builder r7 = r7.setStyle(r8)     // Catch:{ all -> 0x01ff }
                android.app.Notification$Builder r6 = r7.setSmallIcon(r6)     // Catch:{ all -> 0x01ff }
                r7 = 0
                android.app.Notification$Builder r6 = r6.setWhen(r7)     // Catch:{ all -> 0x01ff }
                android.app.Notification$Builder r6 = r6.setShowWhen(r14)     // Catch:{ all -> 0x01ff }
                android.app.Notification$Builder r6 = r6.setVisibility(r15)     // Catch:{ all -> 0x01ff }
                com.android.systemui.shared.plugins.PluginInstanceManager r7 = com.android.systemui.shared.plugins.PluginInstanceManager.this     // Catch:{ all -> 0x01ff }
                android.content.Context r7 = r7.mContext     // Catch:{ all -> 0x01ff }
                int r3 = r7.getColor(r3)     // Catch:{ all -> 0x01ff }
                android.app.Notification$Builder r3 = r6.setColor(r3)     // Catch:{ all -> 0x01ff }
                com.android.systemui.shared.plugins.PluginInstanceManager r6 = com.android.systemui.shared.plugins.PluginInstanceManager.this     // Catch:{ NameNotFoundException -> 0x011d }
                android.content.pm.PackageManager r6 = r6.mPm     // Catch:{ NameNotFoundException -> 0x011d }
                android.content.pm.ServiceInfo r6 = r6.getServiceInfo(r2, r14)     // Catch:{ NameNotFoundException -> 0x011d }
                com.android.systemui.shared.plugins.PluginInstanceManager r7 = com.android.systemui.shared.plugins.PluginInstanceManager.this     // Catch:{ NameNotFoundException -> 0x011d }
                android.content.pm.PackageManager r7 = r7.mPm     // Catch:{ NameNotFoundException -> 0x011d }
                java.lang.CharSequence r6 = r6.loadLabel(r7)     // Catch:{ NameNotFoundException -> 0x011d }
                java.lang.String r13 = r6.toString()     // Catch:{ NameNotFoundException -> 0x011d }
            L_0x011d:
                boolean r6 = r0.isTooNew()     // Catch:{ all -> 0x01ff }
                java.lang.String r7 = "Plugin \""
                if (r6 != 0) goto L_0x0156
                java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x01ff }
                r6.<init>()     // Catch:{ all -> 0x01ff }
                r6.append(r7)     // Catch:{ all -> 0x01ff }
                r6.append(r13)     // Catch:{ all -> 0x01ff }
                java.lang.String r7 = "\" is too old"
                r6.append(r7)     // Catch:{ all -> 0x01ff }
                java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x01ff }
                android.app.Notification$Builder r6 = r3.setContentTitle(r6)     // Catch:{ all -> 0x01ff }
                java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ all -> 0x01ff }
                r7.<init>()     // Catch:{ all -> 0x01ff }
                java.lang.String r8 = "Contact plugin developer to get an updated version.\n"
                r7.append(r8)     // Catch:{ all -> 0x01ff }
                java.lang.String r0 = r0.getMessage()     // Catch:{ all -> 0x01ff }
                r7.append(r0)     // Catch:{ all -> 0x01ff }
                java.lang.String r0 = r7.toString()     // Catch:{ all -> 0x01ff }
                r6.setContentText(r0)     // Catch:{ all -> 0x01ff }
                goto L_0x0186
            L_0x0156:
                java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x01ff }
                r6.<init>()     // Catch:{ all -> 0x01ff }
                r6.append(r7)     // Catch:{ all -> 0x01ff }
                r6.append(r13)     // Catch:{ all -> 0x01ff }
                java.lang.String r7 = "\" is too new"
                r6.append(r7)     // Catch:{ all -> 0x01ff }
                java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x01ff }
                android.app.Notification$Builder r6 = r3.setContentTitle(r6)     // Catch:{ all -> 0x01ff }
                java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ all -> 0x01ff }
                r7.<init>()     // Catch:{ all -> 0x01ff }
                java.lang.String r8 = "Check to see if an OTA is available.\n"
                r7.append(r8)     // Catch:{ all -> 0x01ff }
                java.lang.String r0 = r0.getMessage()     // Catch:{ all -> 0x01ff }
                r7.append(r0)     // Catch:{ all -> 0x01ff }
                java.lang.String r0 = r7.toString()     // Catch:{ all -> 0x01ff }
                r6.setContentText(r0)     // Catch:{ all -> 0x01ff }
            L_0x0186:
                android.content.Intent r0 = new android.content.Intent     // Catch:{ all -> 0x01ff }
                java.lang.String r6 = "com.android.systemui.action.DISABLE_PLUGIN"
                r0.<init>(r6)     // Catch:{ all -> 0x01ff }
                java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x01ff }
                r6.<init>()     // Catch:{ all -> 0x01ff }
                java.lang.String r7 = "package://"
                r6.append(r7)     // Catch:{ all -> 0x01ff }
                java.lang.String r2 = r19.flattenToString()     // Catch:{ all -> 0x01ff }
                r6.append(r2)     // Catch:{ all -> 0x01ff }
                java.lang.String r2 = r6.toString()     // Catch:{ all -> 0x01ff }
                android.net.Uri r2 = android.net.Uri.parse(r2)     // Catch:{ all -> 0x01ff }
                android.content.Intent r0 = r0.setData(r2)     // Catch:{ all -> 0x01ff }
                com.android.systemui.shared.plugins.PluginInstanceManager r2 = com.android.systemui.shared.plugins.PluginInstanceManager.this     // Catch:{ all -> 0x01ff }
                android.content.Context r2 = r2.mContext     // Catch:{ all -> 0x01ff }
                android.app.PendingIntent r0 = android.app.PendingIntent.getBroadcast(r2, r14, r0, r14)     // Catch:{ all -> 0x01ff }
                android.app.Notification$Action$Builder r2 = new android.app.Notification$Action$Builder     // Catch:{ all -> 0x01ff }
                java.lang.String r6 = "Disable plugin"
                r2.<init>(r5, r6, r0)     // Catch:{ all -> 0x01ff }
                android.app.Notification$Action r0 = r2.build()     // Catch:{ all -> 0x01ff }
                r3.addAction(r0)     // Catch:{ all -> 0x01ff }
                com.android.systemui.shared.plugins.PluginInstanceManager r0 = com.android.systemui.shared.plugins.PluginInstanceManager.this     // Catch:{ all -> 0x01ff }
                android.content.Context r0 = r0.mContext     // Catch:{ all -> 0x01ff }
                java.lang.Class<android.app.NotificationManager> r2 = android.app.NotificationManager.class
                java.lang.Object r0 = r0.getSystemService(r2)     // Catch:{ all -> 0x01ff }
                android.app.NotificationManager r0 = (android.app.NotificationManager) r0     // Catch:{ all -> 0x01ff }
                r2 = 6
                android.app.Notification r3 = r3.build()     // Catch:{ all -> 0x01ff }
                r0.notify(r2, r3)     // Catch:{ all -> 0x01ff }
                java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ all -> 0x01ff }
                r0.<init>()     // Catch:{ all -> 0x01ff }
                java.lang.String r2 = "Plugin has invalid interface version "
                r0.append(r2)     // Catch:{ all -> 0x01ff }
                int r2 = r17.getVersion()     // Catch:{ all -> 0x01ff }
                r0.append(r2)     // Catch:{ all -> 0x01ff }
                java.lang.String r2 = ", expected "
                r0.append(r2)     // Catch:{ all -> 0x01ff }
                com.android.systemui.shared.plugins.PluginInstanceManager r1 = com.android.systemui.shared.plugins.PluginInstanceManager.this     // Catch:{ all -> 0x01ff }
                com.android.systemui.shared.plugins.VersionInfo r1 = r1.mVersion     // Catch:{ all -> 0x01ff }
                r0.append(r1)     // Catch:{ all -> 0x01ff }
                java.lang.String r0 = r0.toString()     // Catch:{ all -> 0x01ff }
                android.util.Log.w(r4, r0)     // Catch:{ all -> 0x01ff }
                return r5
            L_0x01ff:
                r0 = move-exception
                java.lang.StringBuilder r1 = new java.lang.StringBuilder
                r1.<init>()
                java.lang.String r2 = "Couldn't load plugin: "
                r1.append(r2)
                r1.append(r12)
                java.lang.String r1 = r1.toString()
                android.util.Log.w(r4, r1, r0)
                return r5
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.shared.plugins.PluginInstanceManager.PluginHandler.handleLoadPlugin(android.content.ComponentName):com.android.systemui.shared.plugins.PluginInstanceManager$PluginInfo");
        }

        private VersionInfo checkVersion(Class<?> cls, T t, VersionInfo versionInfo) throws VersionInfo.InvalidVersionException {
            VersionInfo versionInfo2 = new VersionInfo();
            versionInfo2.addClass(cls);
            if (versionInfo2.hasVersionInfo()) {
                versionInfo.checkVersion(versionInfo2);
                return versionInfo2;
            } else if (t.getVersion() == versionInfo.getDefaultVersion()) {
                return null;
            } else {
                throw new VersionInfo.InvalidVersionException("Invalid legacy version", false);
            }
        }
    }

    public static class PluginContextWrapper extends ContextWrapper {
        private final ClassLoader mClassLoader;
        private LayoutInflater mInflater;

        public PluginContextWrapper(Context context, ClassLoader classLoader) {
            super(context);
            this.mClassLoader = classLoader;
        }

        public ClassLoader getClassLoader() {
            return this.mClassLoader;
        }

        public Object getSystemService(String str) {
            if (!"layout_inflater".equals(str)) {
                return getBaseContext().getSystemService(str);
            }
            if (this.mInflater == null) {
                this.mInflater = LayoutInflater.from(getBaseContext()).cloneInContext(this);
            }
            return this.mInflater;
        }
    }

    static class PluginInfo<T> {
        /* access modifiers changed from: private */
        public String mClass;
        String mPackage;
        T mPlugin;
        /* access modifiers changed from: private */
        public final Context mPluginContext;
        /* access modifiers changed from: private */
        public final VersionInfo mVersion;

        public PluginInfo(String str, String str2, T t, Context context, VersionInfo versionInfo) {
            this.mPlugin = t;
            this.mClass = str2;
            this.mPackage = str;
            this.mPluginContext = context;
            this.mVersion = versionInfo;
        }
    }
}
