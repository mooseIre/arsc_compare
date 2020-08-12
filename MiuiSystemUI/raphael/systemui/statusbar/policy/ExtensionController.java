package com.android.systemui.statusbar.policy;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface ExtensionController {

    public interface Extension<T> {
        void destroy();

        T get();
    }

    public interface ExtensionBuilder<T> {
        Extension build();

        ExtensionBuilder<T> withCallback(Consumer<T> consumer);

        ExtensionBuilder<T> withDefault(Supplier<T> supplier);

        <P extends T> ExtensionBuilder<T> withPlugin(Class<P> cls);

        <P> ExtensionBuilder<T> withPlugin(Class<P> cls, String str, PluginConverter<T, P> pluginConverter);
    }

    public interface PluginConverter<T, P> {
        T getInterfaceFromPlugin(P p);
    }

    <T> ExtensionBuilder<T> newExtension(Class<T> cls);
}
