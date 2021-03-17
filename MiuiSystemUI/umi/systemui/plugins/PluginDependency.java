package com.android.systemui.plugins;

import com.android.systemui.plugins.annotations.ProvidesInterface;

@ProvidesInterface(version = 1)
public class PluginDependency {
    public static final int VERSION = 1;
    static DependencyProvider sProvider;

    public static <T> T get(Plugin plugin, Class<T> cls) {
        return (T) sProvider.get(plugin, cls);
    }

    /* access modifiers changed from: package-private */
    public static abstract class DependencyProvider {
        /* access modifiers changed from: package-private */
        public abstract <T> T get(Plugin plugin, Class<T> cls);

        DependencyProvider() {
        }
    }
}
