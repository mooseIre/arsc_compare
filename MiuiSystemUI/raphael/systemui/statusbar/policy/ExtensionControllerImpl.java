package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.util.ArrayMap;
import com.android.systemui.plugins.Plugin;
import com.android.systemui.plugins.PluginListener;
import com.android.systemui.shared.plugins.PluginManager;
import com.android.systemui.statusbar.policy.ExtensionController;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.util.leak.LeakDetector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ExtensionControllerImpl implements ExtensionController {
    /* access modifiers changed from: private */
    public final Context mDefaultContext;
    /* access modifiers changed from: private */
    public final LeakDetector mLeakDetector;
    /* access modifiers changed from: private */
    public final PluginManager mPluginManager;
    /* access modifiers changed from: private */
    public final TunerService mTunerService;

    private interface Item<T> extends Producer<T> {
        int sortOrder();
    }

    private interface Producer<T> {
        void destroy();

        T get();
    }

    public ExtensionControllerImpl(Context context, LeakDetector leakDetector, PluginManager pluginManager, TunerService tunerService, ConfigurationController configurationController) {
        this.mDefaultContext = context;
        this.mLeakDetector = leakDetector;
        this.mPluginManager = pluginManager;
        this.mTunerService = tunerService;
    }

    public <T> ExtensionBuilder<T> newExtension(Class<T> cls) {
        return new ExtensionBuilder<>();
    }

    private class ExtensionBuilder<T> implements ExtensionController.ExtensionBuilder<T> {
        private ExtensionImpl<T> mExtension;

        private ExtensionBuilder() {
            this.mExtension = new ExtensionImpl<>();
        }

        public ExtensionController.ExtensionBuilder<T> withTunerFactory(ExtensionController.TunerFactory<T> tunerFactory) {
            this.mExtension.addTunerFactory(tunerFactory, tunerFactory.keys());
            return this;
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
            this.mExtension.addPlugin(str, cls, pluginConverter);
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

        public ExtensionController.Extension<T> build() {
            Collections.sort(this.mExtension.mProducers, Comparator.comparingInt($$Lambda$LO8p3lRLZXpohPDzojcJ_BVuMnk.INSTANCE));
            this.mExtension.notifyChanged();
            return this.mExtension;
        }
    }

    private class ExtensionImpl<T> implements ExtensionController.Extension<T> {
        /* access modifiers changed from: private */
        public final ArrayList<Consumer<T>> mCallbacks;
        private T mItem;
        /* access modifiers changed from: private */
        public Context mPluginContext;
        /* access modifiers changed from: private */
        public final ArrayList<Item<T>> mProducers;

        private ExtensionImpl() {
            this.mProducers = new ArrayList<>();
            this.mCallbacks = new ArrayList<>();
        }

        public void addCallback(Consumer<T> consumer) {
            this.mCallbacks.add(consumer);
        }

        public T get() {
            return this.mItem;
        }

        public Context getContext() {
            Context context = this.mPluginContext;
            return context != null ? context : ExtensionControllerImpl.this.mDefaultContext;
        }

        public void destroy() {
            for (int i = 0; i < this.mProducers.size(); i++) {
                this.mProducers.get(i).destroy();
            }
        }

        public void clearItem(boolean z) {
            if (z && this.mItem != null) {
                ExtensionControllerImpl.this.mLeakDetector.trackGarbage(this.mItem);
            }
            this.mItem = null;
        }

        /* access modifiers changed from: private */
        public void notifyChanged() {
            if (this.mItem != null) {
                ExtensionControllerImpl.this.mLeakDetector.trackGarbage(this.mItem);
            }
            this.mItem = null;
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
            this.mProducers.add(new Default(this, supplier));
        }

        public <P> void addPlugin(String str, Class<P> cls, ExtensionController.PluginConverter<T, P> pluginConverter) {
            this.mProducers.add(new PluginItem(str, cls, pluginConverter));
        }

        public void addTunerFactory(ExtensionController.TunerFactory<T> tunerFactory, String[] strArr) {
            this.mProducers.add(new TunerItem(tunerFactory, strArr));
        }

        private class PluginItem<P extends Plugin> implements Item<T>, PluginListener<P> {
            private final ExtensionController.PluginConverter<T, P> mConverter;
            private T mItem;

            public int sortOrder() {
                return 0;
            }

            public PluginItem(String str, Class<P> cls, ExtensionController.PluginConverter<T, P> pluginConverter) {
                this.mConverter = pluginConverter;
                ExtensionControllerImpl.this.mPluginManager.addPluginListener(str, this, (Class<?>) cls);
            }

            /* JADX WARNING: type inference failed for: r2v0, types: [P, T, java.lang.Object] */
            /* JADX WARNING: Unknown variable types count: 1 */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onPluginConnected(P r2, android.content.Context r3) {
                /*
                    r1 = this;
                    com.android.systemui.statusbar.policy.ExtensionControllerImpl$ExtensionImpl r0 = com.android.systemui.statusbar.policy.ExtensionControllerImpl.ExtensionImpl.this
                    android.content.Context unused = r0.mPluginContext = r3
                    com.android.systemui.statusbar.policy.ExtensionController$PluginConverter<T, P> r3 = r1.mConverter
                    if (r3 == 0) goto L_0x0010
                    java.lang.Object r2 = r3.getInterfaceFromPlugin(r2)
                    r1.mItem = r2
                    goto L_0x0012
                L_0x0010:
                    r1.mItem = r2
                L_0x0012:
                    com.android.systemui.statusbar.policy.ExtensionControllerImpl$ExtensionImpl r1 = com.android.systemui.statusbar.policy.ExtensionControllerImpl.ExtensionImpl.this
                    r1.notifyChanged()
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.ExtensionControllerImpl.ExtensionImpl.PluginItem.onPluginConnected(com.android.systemui.plugins.Plugin, android.content.Context):void");
            }

            public void onPluginDisconnected(P p) {
                Context unused = ExtensionImpl.this.mPluginContext = null;
                this.mItem = null;
                ExtensionImpl.this.notifyChanged();
            }

            public T get() {
                return this.mItem;
            }

            public void destroy() {
                ExtensionControllerImpl.this.mPluginManager.removePluginListener(this);
            }
        }

        private class TunerItem<T> implements Item<T>, TunerService.Tunable {
            private final ExtensionController.TunerFactory<T> mFactory;
            private T mItem;
            private final ArrayMap<String, String> mSettings = new ArrayMap<>();

            public int sortOrder() {
                return 1;
            }

            public TunerItem(ExtensionController.TunerFactory<T> tunerFactory, String... strArr) {
                this.mFactory = tunerFactory;
                ExtensionControllerImpl.this.mTunerService.addTunable(this, strArr);
            }

            public T get() {
                return this.mItem;
            }

            public void destroy() {
                ExtensionControllerImpl.this.mTunerService.removeTunable(this);
            }

            public void onTuningChanged(String str, String str2) {
                this.mSettings.put(str, str2);
                this.mItem = this.mFactory.create(this.mSettings);
                ExtensionImpl.this.notifyChanged();
            }
        }

        private class Default<T> implements Item<T> {
            private final Supplier<T> mSupplier;

            public void destroy() {
            }

            public int sortOrder() {
                return 4;
            }

            public Default(ExtensionImpl extensionImpl, Supplier<T> supplier) {
                this.mSupplier = supplier;
            }

            public T get() {
                return this.mSupplier.get();
            }
        }
    }
}
