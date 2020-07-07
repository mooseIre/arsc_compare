package com.android.systemui.plugins;

import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import com.android.systemui.plugins.Plugin;
import com.android.systemui.plugins.PluginEnabler;
import com.android.systemui.plugins.VersionInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class PluginInstanceManager<T extends Plugin> {
    public static final String PLUGIN_PERMISSION = "com.android.systemui.permission.PLUGIN";
    /* access modifiers changed from: private */
    public final String mAction;
    /* access modifiers changed from: private */
    public final boolean mAllowMultiple;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public final PluginListener<T> mListener;
    /* access modifiers changed from: private */
    public final PluginInstanceManager<T>.MainHandler mMainHandler = new MainHandler(Looper.getMainLooper());
    /* access modifiers changed from: private */
    public final PluginManagerImpl mManager;
    private final PluginInstanceManager<T>.PluginHandler mPluginHandler;
    /* access modifiers changed from: private */
    public final PackageManager mPm;
    /* access modifiers changed from: private */
    public final VersionInfo mVersion;
    private final ArraySet<String> mWhitelistedPlugins = new ArraySet<>();

    PluginInstanceManager(Context context, String str, PluginListener<T> pluginListener, boolean z, Looper looper, VersionInfo versionInfo, PluginManagerImpl pluginManagerImpl) {
        this.mPluginHandler = new PluginHandler(looper);
        this.mManager = pluginManagerImpl;
        this.mContext = context;
        this.mPm = context.getPackageManager();
        this.mAction = str;
        this.mListener = pluginListener;
        this.mAllowMultiple = z;
        this.mVersion = versionInfo;
        this.mWhitelistedPlugins.addAll(Arrays.asList(pluginManagerImpl.getWhitelistedPlugins()));
    }

    public PluginInfo<T> getPlugin() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            this.mPluginHandler.handleQueryPlugins((String) null);
            if (this.mPluginHandler.mPlugins.size() <= 0) {
                return null;
            }
            this.mMainHandler.removeMessages(1);
            PluginInfo<T> pluginInfo = (PluginInfo) this.mPluginHandler.mPlugins.get(0);
            PluginPrefs.setHasPlugins(this.mContext);
            ((Plugin) pluginInfo.mPlugin).onCreate(this.mContext, pluginInfo.mPluginContext);
            return pluginInfo;
        }
        throw new RuntimeException("Must be called from UI thread");
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
                disable(pluginInfo, 2);
                z = true;
            }
        }
        return z;
    }

    public boolean disableAll() {
        ArrayList arrayList = new ArrayList(this.mPluginHandler.mPlugins);
        for (int i = 0; i < arrayList.size(); i++) {
            disable((PluginInfo) arrayList.get(i), 3);
        }
        if (arrayList.size() != 0) {
            return true;
        }
        return false;
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

    private void disable(PluginInfo pluginInfo, @PluginEnabler.DisableReason int i) {
        ComponentName componentName = new ComponentName(pluginInfo.mPackage, pluginInfo.mClass);
        if (!isPluginWhitelisted(componentName)) {
            Log.w("PluginInstanceManager", "Disabling plugin " + componentName.flattenToShortString());
            this.mManager.getPluginEnabler().setDisabled(componentName, i);
        }
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
                Object obj = message.obj;
                PluginInfo pluginInfo = (PluginInfo) obj;
                if (!(obj instanceof PluginFragment)) {
                    ((Plugin) pluginInfo.mPlugin).onCreate(PluginInstanceManager.this.mContext, pluginInfo.mPluginContext);
                }
                PluginInstanceManager.this.mListener.onPluginConnected((Plugin) pluginInfo.mPlugin, pluginInfo.mPluginContext);
            } else if (i != 2) {
                super.handleMessage(message);
            } else {
                PluginInstanceManager.this.mListener.onPluginDisconnected((Plugin) message.obj);
                Object obj2 = message.obj;
                if (!(obj2 instanceof PluginFragment)) {
                    ((Plugin) obj2).onDestroy();
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
                    PluginInfo pluginInfo = this.mPlugins.get(size);
                    PluginInstanceManager.this.mListener.onPluginDisconnected((Plugin) pluginInfo.mPlugin);
                    T t = pluginInfo.mPlugin;
                    if (!(t instanceof PluginFragment)) {
                        ((Plugin) t).onDestroy();
                    }
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
                    PluginInfo pluginInfo2 = this.mPlugins.get(size2);
                    if (pluginInfo2.mPackage.equals(str2)) {
                        PluginInstanceManager.this.mMainHandler.obtainMessage(2, pluginInfo2.mPlugin).sendToTarget();
                        this.mPlugins.remove(size2);
                    }
                }
            }
        }

        /* access modifiers changed from: private */
        public void handleQueryPlugins(String str) {
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
        /* JADX WARNING: Can't wrap try/catch for region: R(3:17|18|19) */
        /* JADX WARNING: Code restructure failed: missing block: B:18:?, code lost:
            android.util.Log.w("PluginInstanceManager", "Plugin has invalid interface version " + r9.getVersion() + ", expected " + com.android.systemui.plugins.PluginInstanceManager.access$1300(r10.this$0));
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x00c5, code lost:
            return null;
         */
        /* JADX WARNING: Missing exception handler attribute for start block: B:17:0x009f */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public com.android.systemui.plugins.PluginInstanceManager.PluginInfo<T> handleLoadPlugin(android.content.ComponentName r11) {
            /*
                r10 = this;
                com.android.systemui.plugins.PluginInstanceManager r0 = com.android.systemui.plugins.PluginInstanceManager.this
                boolean r0 = r0.isPluginWhitelisted(r11)
                java.lang.String r1 = "PluginInstanceManager"
                r2 = 0
                if (r0 != 0) goto L_0x0020
                java.lang.StringBuilder r10 = new java.lang.StringBuilder
                r10.<init>()
                java.lang.String r0 = "Plugin not in whitelist: "
                r10.append(r0)
                r10.append(r11)
                java.lang.String r10 = r10.toString()
                android.util.Log.w(r1, r10)
                return r2
            L_0x0020:
                com.android.systemui.plugins.PluginInstanceManager r0 = com.android.systemui.plugins.PluginInstanceManager.this
                com.android.systemui.plugins.PluginManagerImpl r0 = r0.mManager
                com.android.systemui.plugins.PluginEnabler r0 = r0.getPluginEnabler()
                boolean r0 = r0.isEnabled(r11)
                if (r0 != 0) goto L_0x0031
                return r2
            L_0x0031:
                java.lang.String r0 = r11.getPackageName()
                java.lang.String r5 = r11.getClassName()
                com.android.systemui.plugins.PluginInstanceManager r11 = com.android.systemui.plugins.PluginInstanceManager.this     // Catch:{ all -> 0x00c6 }
                android.content.pm.PackageManager r11 = r11.mPm     // Catch:{ all -> 0x00c6 }
                r3 = 0
                android.content.pm.ApplicationInfo r11 = r11.getApplicationInfo(r0, r3)     // Catch:{ all -> 0x00c6 }
                com.android.systemui.plugins.PluginInstanceManager r4 = com.android.systemui.plugins.PluginInstanceManager.this     // Catch:{ all -> 0x00c6 }
                android.content.pm.PackageManager r4 = r4.mPm     // Catch:{ all -> 0x00c6 }
                java.lang.String r6 = "com.android.systemui.permission.PLUGIN"
                int r4 = r4.checkPermission(r6, r0)     // Catch:{ all -> 0x00c6 }
                if (r4 == 0) goto L_0x0067
                java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ all -> 0x00c6 }
                r10.<init>()     // Catch:{ all -> 0x00c6 }
                java.lang.String r11 = "Plugin doesn't have permission: "
                r10.append(r11)     // Catch:{ all -> 0x00c6 }
                r10.append(r0)     // Catch:{ all -> 0x00c6 }
                java.lang.String r10 = r10.toString()     // Catch:{ all -> 0x00c6 }
                android.util.Log.d(r1, r10)     // Catch:{ all -> 0x00c6 }
                return r2
            L_0x0067:
                com.android.systemui.plugins.PluginInstanceManager r4 = com.android.systemui.plugins.PluginInstanceManager.this     // Catch:{ all -> 0x00c6 }
                com.android.systemui.plugins.PluginManagerImpl r4 = r4.mManager     // Catch:{ all -> 0x00c6 }
                java.lang.ClassLoader r4 = r4.getClassLoader(r11)     // Catch:{ all -> 0x00c6 }
                com.android.systemui.plugins.PluginInstanceManager$PluginContextWrapper r7 = new com.android.systemui.plugins.PluginInstanceManager$PluginContextWrapper     // Catch:{ all -> 0x00c6 }
                com.android.systemui.plugins.PluginInstanceManager r6 = com.android.systemui.plugins.PluginInstanceManager.this     // Catch:{ all -> 0x00c6 }
                android.content.Context r6 = r6.mContext     // Catch:{ all -> 0x00c6 }
                android.content.Context r11 = r6.createApplicationContext(r11, r3)     // Catch:{ all -> 0x00c6 }
                r7.<init>(r11, r4)     // Catch:{ all -> 0x00c6 }
                r11 = 1
                java.lang.Class r11 = java.lang.Class.forName(r5, r11, r4)     // Catch:{ all -> 0x00c6 }
                java.lang.Object r3 = r11.newInstance()     // Catch:{ all -> 0x00c6 }
                r9 = r3
                com.android.systemui.plugins.Plugin r9 = (com.android.systemui.plugins.Plugin) r9     // Catch:{ all -> 0x00c6 }
                com.android.systemui.plugins.PluginInstanceManager r3 = com.android.systemui.plugins.PluginInstanceManager.this     // Catch:{ InvalidVersionException -> 0x009f }
                com.android.systemui.plugins.VersionInfo r3 = r3.mVersion     // Catch:{ InvalidVersionException -> 0x009f }
                com.android.systemui.plugins.VersionInfo r8 = r10.checkVersion(r11, r9, r3)     // Catch:{ InvalidVersionException -> 0x009f }
                com.android.systemui.plugins.PluginInstanceManager$PluginInfo r11 = new com.android.systemui.plugins.PluginInstanceManager$PluginInfo     // Catch:{ InvalidVersionException -> 0x009f }
                r3 = r11
                r4 = r0
                r6 = r9
                r3.<init>(r4, r5, r6, r7, r8)     // Catch:{ InvalidVersionException -> 0x009f }
                return r11
            L_0x009f:
                java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch:{ all -> 0x00c6 }
                r11.<init>()     // Catch:{ all -> 0x00c6 }
                java.lang.String r3 = "Plugin has invalid interface version "
                r11.append(r3)     // Catch:{ all -> 0x00c6 }
                int r3 = r9.getVersion()     // Catch:{ all -> 0x00c6 }
                r11.append(r3)     // Catch:{ all -> 0x00c6 }
                java.lang.String r3 = ", expected "
                r11.append(r3)     // Catch:{ all -> 0x00c6 }
                com.android.systemui.plugins.PluginInstanceManager r10 = com.android.systemui.plugins.PluginInstanceManager.this     // Catch:{ all -> 0x00c6 }
                com.android.systemui.plugins.VersionInfo r10 = r10.mVersion     // Catch:{ all -> 0x00c6 }
                r11.append(r10)     // Catch:{ all -> 0x00c6 }
                java.lang.String r10 = r11.toString()     // Catch:{ all -> 0x00c6 }
                android.util.Log.w(r1, r10)     // Catch:{ all -> 0x00c6 }
                return r2
            L_0x00c6:
                r10 = move-exception
                java.lang.StringBuilder r11 = new java.lang.StringBuilder
                r11.<init>()
                java.lang.String r3 = "Couldn't load plugin: "
                r11.append(r3)
                r11.append(r0)
                java.lang.String r11 = r11.toString()
                android.util.Log.w(r1, r11, r10)
                return r2
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.plugins.PluginInstanceManager.PluginHandler.handleLoadPlugin(android.content.ComponentName):com.android.systemui.plugins.PluginInstanceManager$PluginInfo");
        }

        private VersionInfo checkVersion(Class<?> cls, T t, VersionInfo versionInfo) throws VersionInfo.InvalidVersionException {
            VersionInfo addClass = new VersionInfo().addClass(cls);
            if (addClass.hasVersionInfo()) {
                versionInfo.checkVersion(addClass);
                return addClass;
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
