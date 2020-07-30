package com.android.systemui.statusbar.policy;

import android.text.TextUtils;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.Plugin;
import com.android.systemui.plugins.PluginListener;
import com.android.systemui.plugins.PluginManager;
import com.android.systemui.statusbar.policy.ExtensionController;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ExtensionControllerImpl implements ExtensionController {

    private interface Producer<T> {
        void destroy();

        T get();
    }

    public <T> ExtensionBuilder<T> newExtension(Class<T> cls) {
        return new ExtensionBuilder<>();
    }

    private class ExtensionBuilder<T> implements ExtensionController.ExtensionBuilder<T> {
        private ExtensionImpl<T> mExtension;

        private ExtensionBuilder() {
            this.mExtension = new ExtensionImpl<>();
        }

        public <P extends T> ExtensionController.ExtensionBuilder<T> withPlugin(Class<P> cls) {
            withPlugin(cls, PluginManager.Helper.getAction(cls));
            return this;
        }

        public <P extends T> ExtensionController.ExtensionBuilder<T> withPlugin(Class<P> cls, String str) {
            withPlugin(cls, str, (ExtensionController.PluginConverter) null);
            return this;
        }

        public <P> ExtensionController.ExtensionBuilder<T> withPlugin(Class<P> cls, String str, ExtensionController.PluginConverter<T, P> pluginConverter) {
            if (!TextUtils.isEmpty(str)) {
                this.mExtension.addPlugin(str, cls, pluginConverter);
            }
            return this;
        }

        public ExtensionController.ExtensionBuilder<T> withDefault(Supplier<T> supplier) {
            this.mExtension.addDefault(supplier);
            return this;
        }

        public ExtensionController.ExtensionBuilder<T> withCallback(Consumer<T> consumer) {
            this.mExtension.mCallbacks.add(consumer);
            return this;
        }

        public ExtensionController.Extension build() {
            Collections.sort(this.mExtension.mProducers, new Comparator<Producer<T>>() {
                public int compare(Producer<T> producer, Producer<T> producer2) {
                    if (!(producer instanceof ExtensionImpl.PluginItem) || (producer2 instanceof ExtensionImpl.PluginItem)) {
                        return 0;
                    }
                    return -1;
                }
            });
            this.mExtension.notifyChanged();
            return this.mExtension;
        }
    }

    private class ExtensionImpl<T> implements ExtensionController.Extension<T> {
        /* access modifiers changed from: private */
        public final ArrayList<Consumer<T>> mCallbacks;
        private T mItem;
        /* access modifiers changed from: private */
        public final ArrayList<Producer<T>> mProducers;

        private ExtensionImpl() {
            this.mProducers = new ArrayList<>();
            this.mCallbacks = new ArrayList<>();
        }

        public T get() {
            return this.mItem;
        }

        public void destroy() {
            for (int i = 0; i < this.mProducers.size(); i++) {
                this.mProducers.get(i).destroy();
            }
        }

        /* access modifiers changed from: private */
        public void notifyChanged() {
            int i = 0;
            while (true) {
                if (i >= this.mProducers.size()) {
                    break;
                }
                T t = this.mProducers.get(i).get();
                if (t != null) {
                    this.mItem = t;
                    break;
                }
                i++;
            }
            for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
                this.mCallbacks.get(i2).accept(this.mItem);
            }
        }

        public void addDefault(Supplier<T> supplier) {
            this.mProducers.add(new Default(supplier));
        }

        public <P> void addPlugin(String str, Class<P> cls, ExtensionController.PluginConverter<T, P> pluginConverter) {
            this.mProducers.add(new PluginItem(str, cls, pluginConverter));
        }

        private class PluginItem<P extends Plugin> implements Producer<T>, PluginListener<P> {
            private final ExtensionController.PluginConverter<T, P> mConverter;
            private T mItem;

            public PluginItem(String str, Class<P> cls, ExtensionController.PluginConverter<T, P> pluginConverter) {
                this.mConverter = pluginConverter;
                ((PluginManager) Dependency.get(PluginManager.class)).addPluginListener(str, this, (Class<?>) cls);
            }

            /* JADX WARNING: type inference failed for: r1v0, types: [P, T, java.lang.Object] */
            /* JADX WARNING: Unknown variable types count: 1 */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onPluginConnected(P r1, android.content.Context r2) {
                /*
                    r0 = this;
                    com.android.systemui.statusbar.policy.ExtensionController$PluginConverter<T, P> r2 = r0.mConverter
                    if (r2 == 0) goto L_0x000b
                    java.lang.Object r1 = r2.getInterfaceFromPlugin(r1)
                    r0.mItem = r1
                    goto L_0x000d
                L_0x000b:
                    r0.mItem = r1
                L_0x000d:
                    com.android.systemui.statusbar.policy.ExtensionControllerImpl$ExtensionImpl r0 = com.android.systemui.statusbar.policy.ExtensionControllerImpl.ExtensionImpl.this
                    r0.notifyChanged()
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.ExtensionControllerImpl.ExtensionImpl.PluginItem.onPluginConnected(com.android.systemui.plugins.Plugin, android.content.Context):void");
            }

            public void onPluginDisconnected(P p) {
                this.mItem = null;
                ExtensionImpl.this.notifyChanged();
            }

            public T get() {
                return this.mItem;
            }

            public void destroy() {
                ((PluginManager) Dependency.get(PluginManager.class)).removePluginListener(this);
            }
        }

        private class Default<T> implements Producer<T> {
            private final Supplier<T> mSupplier;

            public void destroy() {
            }

            public Default(Supplier<T> supplier) {
                this.mSupplier = supplier;
            }

            public T get() {
                return this.mSupplier.get();
            }
        }
    }
}
