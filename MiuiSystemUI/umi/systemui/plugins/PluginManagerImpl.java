package com.android.systemui.plugins;

import android.app.ActivityThread;
import android.app.LoadedApk;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import com.android.systemui.plugins.PluginManager;
import dalvik.system.PathClassLoader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

public class PluginManagerImpl extends BroadcastReceiver implements PluginManager {
    private static final String TAG = PluginManagerImpl.class.getSimpleName();
    private final Map<String, ClassLoader> mClassLoaders = new ArrayMap();
    private final Context mContext;
    private final PluginInstanceManagerFactory mFactory;
    private boolean mListening;
    private Looper mLooper;
    private ClassLoaderFilter mParentClassLoader;
    private final PluginEnabler mPluginEnabler;
    /* access modifiers changed from: private */
    public final ArrayMap<PluginListener<?>, PluginInstanceManager> mPluginMap = new ArrayMap<>();
    private final PluginPrefs mPluginPrefs;
    private final ArraySet<String> mWhitelistedPlugins = new ArraySet<>();

    public PluginManagerImpl(Context context, final PluginInitializer pluginInitializer) {
        this.mContext = context;
        this.mFactory = new PluginInstanceManagerFactory();
        this.mLooper = pluginInitializer.getBgLooper();
        this.mWhitelistedPlugins.addAll(Arrays.asList(pluginInitializer.getWhitelistedPlugins(this.mContext)));
        this.mPluginPrefs = new PluginPrefs(this.mContext);
        this.mPluginEnabler = pluginInitializer.getPluginEnabler(this.mContext);
        new Handler(this.mLooper).post(new Runnable(this) {
            public void run() {
                pluginInitializer.onPluginManagerInit();
            }
        });
    }

    public String[] getWhitelistedPlugins() {
        return (String[]) this.mWhitelistedPlugins.toArray(new String[0]);
    }

    public PluginEnabler getPluginEnabler() {
        return this.mPluginEnabler;
    }

    public <T extends Plugin> void addPluginListener(PluginListener<T> pluginListener, Class<?> cls) {
        addPluginListener(pluginListener, cls, false);
    }

    public <T extends Plugin> void addPluginListener(PluginListener<T> pluginListener, Class<?> cls, boolean z) {
        addPluginListener(PluginManager.Helper.getAction(cls), pluginListener, cls, z);
    }

    public <T extends Plugin> void addPluginListener(String str, PluginListener<T> pluginListener, Class<?> cls) {
        addPluginListener(str, pluginListener, cls, false);
    }

    public <T extends Plugin> void addPluginListener(String str, PluginListener<T> pluginListener, Class cls, boolean z) {
        this.mPluginPrefs.addAction(str);
        PluginInstanceManager createPluginInstanceManager = this.mFactory.createPluginInstanceManager(this.mContext, str, pluginListener, z, this.mLooper, cls, this);
        createPluginInstanceManager.loadAll();
        this.mPluginMap.put(pluginListener, createPluginInstanceManager);
        startListening();
    }

    public void removePluginListener(PluginListener<?> pluginListener) {
        if (this.mPluginMap.containsKey(pluginListener)) {
            this.mPluginMap.remove(pluginListener).destroy();
            if (this.mPluginMap.size() == 0) {
                stopListening();
            }
        }
    }

    private void startListening() {
        if (!this.mListening) {
            this.mListening = true;
            IntentFilter intentFilter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
            intentFilter.addAction("android.intent.action.PACKAGE_CHANGED");
            intentFilter.addAction("android.intent.action.PACKAGE_REPLACED");
            intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
            intentFilter.addAction(PluginManager.PLUGIN_CHANGED);
            intentFilter.addAction("com.android.systemui.action.DISABLE_PLUGIN");
            intentFilter.addDataScheme("package");
            this.mContext.registerReceiver(this, intentFilter);
            this.mContext.registerReceiver(this, new IntentFilter("android.intent.action.USER_UNLOCKED"));
        }
    }

    private void stopListening() {
        if (this.mListening) {
            this.mListening = false;
            this.mContext.unregisterReceiver(this);
        }
    }

    public void onReceive(Context context, Intent intent) {
        int disableReason;
        String str = TAG;
        if ("android.intent.action.USER_UNLOCKED".equals(intent.getAction())) {
            for (PluginInstanceManager loadAll : this.mPluginMap.values()) {
                loadAll.loadAll();
            }
        } else if ("com.android.systemui.action.DISABLE_PLUGIN".equals(intent.getAction())) {
            ComponentName unflattenFromString = ComponentName.unflattenFromString(intent.getData().toString().substring(10));
            if (!isPluginWhitelisted(unflattenFromString)) {
                getPluginEnabler().setDisabled(unflattenFromString, 1);
            }
        } else {
            String encodedSchemeSpecificPart = intent.getData().getEncodedSchemeSpecificPart();
            ComponentName unflattenFromString2 = ComponentName.unflattenFromString(encodedSchemeSpecificPart);
            if (clearClassLoader(encodedSchemeSpecificPart)) {
                Log.v(str, "Reloading " + encodedSchemeSpecificPart);
            }
            if ("android.intent.action.PACKAGE_REPLACED".equals(intent.getAction()) && unflattenFromString2 != null && ((disableReason = getPluginEnabler().getDisableReason(unflattenFromString2)) == 2 || disableReason == 3 || disableReason == 1)) {
                Log.i(str, "Re-enabling previously disabled plugin that has been updated: " + unflattenFromString2.flattenToShortString());
                getPluginEnabler().setEnabled(unflattenFromString2);
            }
            if (!"android.intent.action.PACKAGE_REMOVED".equals(intent.getAction())) {
                for (PluginInstanceManager onPackageChange : this.mPluginMap.values()) {
                    onPackageChange.onPackageChange(encodedSchemeSpecificPart);
                }
                return;
            }
            for (PluginInstanceManager onPackageRemoved : this.mPluginMap.values()) {
                onPackageRemoved.onPackageRemoved(encodedSchemeSpecificPart);
            }
        }
    }

    public ClassLoader getClassLoader(ApplicationInfo applicationInfo) {
        if (!isPluginPackageWhitelisted(applicationInfo.packageName)) {
            String str = TAG;
            Log.w(str, "Cannot get class loader for non-whitelisted plugin. Src:" + applicationInfo.sourceDir + ", pkg: " + applicationInfo.packageName);
            return null;
        } else if (this.mClassLoaders.containsKey(applicationInfo.packageName)) {
            return this.mClassLoaders.get(applicationInfo.packageName);
        } else {
            ArrayList arrayList = new ArrayList();
            ArrayList arrayList2 = new ArrayList();
            LoadedApk.makePaths((ActivityThread) null, true, applicationInfo, arrayList, arrayList2);
            PathClassLoader pathClassLoader = new PathClassLoader(TextUtils.join(File.pathSeparator, arrayList), TextUtils.join(File.pathSeparator, arrayList2), getParentClassLoader());
            this.mClassLoaders.put(applicationInfo.packageName, pathClassLoader);
            return pathClassLoader;
        }
    }

    private boolean clearClassLoader(String str) {
        return this.mClassLoaders.remove(str) != null;
    }

    /* access modifiers changed from: package-private */
    public ClassLoader getParentClassLoader() {
        if (this.mParentClassLoader == null) {
            this.mParentClassLoader = new ClassLoaderFilter(PluginManagerImpl.class.getClassLoader(), "com.android.systemui.plugin");
        }
        return this.mParentClassLoader;
    }

    public <T> boolean dependsOn(Plugin plugin, Class<T> cls) {
        for (int i = 0; i < this.mPluginMap.size(); i++) {
            if (this.mPluginMap.valueAt(i).dependsOn(plugin, cls)) {
                return true;
            }
        }
        return false;
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println(String.format("  plugin map (%d):", new Object[]{Integer.valueOf(this.mPluginMap.size())}));
        for (PluginListener next : this.mPluginMap.keySet()) {
            printWriter.println(String.format("    %s -> %s", new Object[]{next, this.mPluginMap.get(next)}));
        }
    }

    public static class PluginInstanceManagerFactory {
        public <T extends Plugin> PluginInstanceManager createPluginInstanceManager(Context context, String str, PluginListener<T> pluginListener, boolean z, Looper looper, Class<?> cls, PluginManagerImpl pluginManagerImpl) {
            return new PluginInstanceManager(context, str, pluginListener, z, looper, new VersionInfo().addClass(cls), pluginManagerImpl);
        }
    }

    private boolean isPluginPackageWhitelisted(String str) {
        Iterator<String> it = this.mWhitelistedPlugins.iterator();
        while (it.hasNext()) {
            String next = it.next();
            ComponentName unflattenFromString = ComponentName.unflattenFromString(next);
            if (unflattenFromString != null) {
                if (unflattenFromString.getPackageName().equals(str)) {
                    return true;
                }
            } else if (next.equals(str)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPluginWhitelisted(ComponentName componentName) {
        Iterator<String> it = this.mWhitelistedPlugins.iterator();
        while (it.hasNext()) {
            String next = it.next();
            ComponentName unflattenFromString = ComponentName.unflattenFromString(next);
            if (unflattenFromString != null) {
                if (unflattenFromString.equals(componentName)) {
                    return true;
                }
            } else if (next.equals(componentName.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    private static class ClassLoaderFilter extends ClassLoader {
        private final ClassLoader mBase;
        private final String mPackage;

        public ClassLoaderFilter(ClassLoader classLoader, String str) {
            super(ClassLoader.getSystemClassLoader());
            this.mBase = classLoader;
            this.mPackage = str;
        }

        /* access modifiers changed from: protected */
        public Class<?> loadClass(String str, boolean z) throws ClassNotFoundException {
            if (!str.startsWith(this.mPackage)) {
                super.loadClass(str, z);
            }
            return this.mBase.loadClass(str);
        }
    }

    private class PluginExceptionHandler implements Thread.UncaughtExceptionHandler {
        private final Thread.UncaughtExceptionHandler mHandler;
        final /* synthetic */ PluginManagerImpl this$0;

        public void uncaughtException(Thread thread, Throwable th) {
            if (SystemProperties.getBoolean("plugin.debugging", false)) {
                this.mHandler.uncaughtException(thread, th);
                return;
            }
            boolean checkStack = checkStack(th);
            if (!checkStack) {
                for (PluginInstanceManager disableAll : this.this$0.mPluginMap.values()) {
                    checkStack |= disableAll.disableAll();
                }
            }
            if (checkStack) {
                th = new CrashWhilePluginActiveException(th);
            }
            this.mHandler.uncaughtException(thread, th);
        }

        private boolean checkStack(Throwable th) {
            if (th == null) {
                return false;
            }
            boolean z = false;
            for (StackTraceElement stackTraceElement : th.getStackTrace()) {
                for (PluginInstanceManager checkAndDisable : this.this$0.mPluginMap.values()) {
                    z |= checkAndDisable.checkAndDisable(stackTraceElement.getClassName());
                }
            }
            return checkStack(th.getCause()) | z;
        }
    }

    public static class CrashWhilePluginActiveException extends RuntimeException {
        public CrashWhilePluginActiveException(Throwable th) {
            super(th);
        }
    }
}
