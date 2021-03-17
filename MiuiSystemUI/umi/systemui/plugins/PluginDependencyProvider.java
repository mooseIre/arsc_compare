package com.android.systemui.plugins;

import android.util.ArrayMap;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.PluginDependency;
import com.android.systemui.shared.plugins.PluginManager;

public class PluginDependencyProvider extends PluginDependency.DependencyProvider {
    private final ArrayMap<Class<?>, Object> mDependencies = new ArrayMap<>();
    private final PluginManager mManager;

    public PluginDependencyProvider(PluginManager pluginManager) {
        this.mManager = pluginManager;
        PluginDependency.sProvider = this;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: com.android.systemui.plugins.PluginDependencyProvider */
    /* JADX WARN: Multi-variable type inference failed */
    public <T> void allowPluginDependency(Class<T> cls) {
        allowPluginDependency(cls, Dependency.get(cls));
    }

    public <T> void allowPluginDependency(Class<T> cls, T t) {
        synchronized (this.mDependencies) {
            this.mDependencies.put(cls, t);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.systemui.plugins.PluginDependency.DependencyProvider
    public <T> T get(Plugin plugin, Class<T> cls) {
        T t;
        if (this.mManager.dependsOn(plugin, cls)) {
            synchronized (this.mDependencies) {
                if (this.mDependencies.containsKey(cls)) {
                    t = (T) this.mDependencies.get(cls);
                } else {
                    throw new IllegalArgumentException("Unknown dependency " + cls);
                }
            }
            return t;
        }
        throw new IllegalArgumentException(plugin.getClass() + " does not depend on " + cls);
    }
}
