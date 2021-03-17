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
    private final boolean isDebuggable;
    private final String mAction;
    private final boolean mAllowMultiple;
    private final Context mContext;
    private final PluginListener<T> mListener;
    @VisibleForTesting
    final PluginInstanceManager<T>.MainHandler mMainHandler;
    private final PluginManagerImpl mManager;
    @VisibleForTesting
    final PluginInstanceManager<T>.PluginHandler mPluginHandler;
    private final PackageManager mPm;
    private final VersionInfo mVersion;
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
        Iterator it = new ArrayList(((PluginHandler) this.mPluginHandler).mPlugins).iterator();
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
        Iterator it = new ArrayList(((PluginHandler) this.mPluginHandler).mPlugins).iterator();
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
        ArrayList arrayList = new ArrayList(((PluginHandler) this.mPluginHandler).mPlugins);
        boolean z = false;
        for (int i = 0; i < arrayList.size(); i++) {
            z |= disable((PluginInfo) arrayList.get(i), 3);
        }
        return z;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isPluginWhitelisted(ComponentName componentName) {
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
        Iterator it = new ArrayList(((PluginHandler) this.mPluginHandler).mPlugins).iterator();
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
        return String.format("%s@%s (action=%s)", PluginInstanceManager.class.getSimpleName(), Integer.valueOf(hashCode()), this.mAction);
    }

    /* access modifiers changed from: private */
    public class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }

        /* JADX DEBUG: Multi-variable search result rejected for r3v2, resolved type: com.android.systemui.plugins.PluginListener */
        /* JADX DEBUG: Multi-variable search result rejected for r3v4, resolved type: com.android.systemui.plugins.PluginListener */
        /* JADX WARN: Multi-variable type inference failed */
        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                PluginPrefs.setHasPlugins(PluginInstanceManager.this.mContext);
                PluginInfo pluginInfo = (PluginInfo) message.obj;
                PluginInstanceManager.this.mManager.handleWtfs();
                if (!(message.obj instanceof PluginFragment)) {
                    pluginInfo.mPlugin.onCreate(PluginInstanceManager.this.mContext, pluginInfo.mPluginContext);
                }
                PluginInstanceManager.this.mListener.onPluginConnected(pluginInfo.mPlugin, pluginInfo.mPluginContext);
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

    /* access modifiers changed from: private */
    public class PluginHandler extends Handler {
        private final ArrayList<PluginInfo<T>> mPlugins = new ArrayList<>();

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
                handleQueryPlugins(null);
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
                    PluginInfo<T> pluginInfo = this.mPlugins.get(size2);
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
                    PluginInfo<T> handleLoadPlugin = handleLoadPlugin(new ComponentName(serviceInfo.packageName, serviceInfo.name));
                    if (handleLoadPlugin != null) {
                        this.mPlugins.add(handleLoadPlugin);
                        PluginInstanceManager.this.mMainHandler.obtainMessage(1, handleLoadPlugin).sendToTarget();
                    }
                }
                return;
            }
            Log.w("PluginInstanceManager", "Multiple plugins found for " + PluginInstanceManager.this.mAction);
        }

        /* JADX DEBUG: Multi-variable search result rejected for r18v0, resolved type: com.android.systemui.shared.plugins.PluginInstanceManager$PluginHandler */
        /* JADX WARN: Multi-variable type inference failed */
        /* access modifiers changed from: protected */
        /* JADX WARNING: Removed duplicated region for block: B:31:0x0125  */
        /* JADX WARNING: Removed duplicated region for block: B:32:0x0156  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public com.android.systemui.shared.plugins.PluginInstanceManager.PluginInfo<T> handleLoadPlugin(android.content.ComponentName r19) {
            /*
            // Method dump skipped, instructions count: 533
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

        @Override // android.content.Context, android.content.ContextWrapper
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

    /* access modifiers changed from: package-private */
    public static class PluginInfo<T> {
        private String mClass;
        String mPackage;
        T mPlugin;
        private final Context mPluginContext;
        private final VersionInfo mVersion;

        public PluginInfo(String str, String str2, T t, Context context, VersionInfo versionInfo) {
            this.mPlugin = t;
            this.mClass = str2;
            this.mPackage = str;
            this.mPluginContext = context;
            this.mVersion = versionInfo;
        }
    }
}
