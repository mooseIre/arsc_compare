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
    private final Context mDefaultContext;
    private final LeakDetector mLeakDetector;
    private final PluginManager mPluginManager;
    private final TunerService mTunerService;

    /* access modifiers changed from: private */
    public interface Item<T> extends Producer<T> {
        int sortOrder();
    }

    /* access modifiers changed from: private */
    public interface Producer<T> {
        void destroy();

        T get();
    }

    public ExtensionControllerImpl(Context context, LeakDetector leakDetector, PluginManager pluginManager, TunerService tunerService, ConfigurationController configurationController) {
        this.mDefaultContext = context;
        this.mLeakDetector = leakDetector;
        this.mPluginManager = pluginManager;
        this.mTunerService = tunerService;
    }

    @Override // com.android.systemui.statusbar.policy.ExtensionController
    public <T> ExtensionBuilder<T> newExtension(Class<T> cls) {
        return new ExtensionBuilder<>();
    }

    /* access modifiers changed from: private */
    public class ExtensionBuilder<T> implements ExtensionController.ExtensionBuilder<T> {
        private ExtensionImpl<T> mExtension;

        private ExtensionBuilder() {
            this.mExtension = new ExtensionImpl<>();
        }

        @Override // com.android.systemui.statusbar.policy.ExtensionController.ExtensionBuilder
        public ExtensionController.ExtensionBuilder<T> withTunerFactory(ExtensionController.TunerFactory<T> tunerFactory) {
            this.mExtension.addTunerFactory(tunerFactory, tunerFactory.keys());
            return this;
        }

        @Override // com.android.systemui.statusbar.policy.ExtensionController.ExtensionBuilder
        public <P extends T> ExtensionController.ExtensionBuilder<T> withPlugin(Class<P> cls) {
            withPlugin(cls, PluginManager.Helper.getAction(cls));
            return this;
        }

        public <P extends T> ExtensionController.ExtensionBuilder<T> withPlugin(Class<P> cls, String str) {
            withPlugin(cls, str, null);
            return this;
        }

        @Override // com.android.systemui.statusbar.policy.ExtensionController.ExtensionBuilder
        public <P> ExtensionController.ExtensionBuilder<T> withPlugin(Class<P> cls, String str, ExtensionController.PluginConverter<T, P> pluginConverter) {
            this.mExtension.addPlugin(str, cls, pluginConverter);
            return this;
        }

        @Override // com.android.systemui.statusbar.policy.ExtensionController.ExtensionBuilder
        public ExtensionController.ExtensionBuilder<T> withDefault(Supplier<T> supplier) {
            this.mExtension.addDefault(supplier);
            return this;
        }

        @Override // com.android.systemui.statusbar.policy.ExtensionController.ExtensionBuilder
        public ExtensionController.ExtensionBuilder<T> withCallback(Consumer<T> consumer) {
            ((ExtensionImpl) this.mExtension).mCallbacks.add(consumer);
            return this;
        }

        @Override // com.android.systemui.statusbar.policy.ExtensionController.ExtensionBuilder
        public ExtensionController.Extension<T> build() {
            Collections.sort(((ExtensionImpl) this.mExtension).mProducers, Comparator.comparingInt($$Lambda$LO8p3lRLZXpohPDzojcJ_BVuMnk.INSTANCE));
            this.mExtension.notifyChanged();
            return this.mExtension;
        }
    }

    /* access modifiers changed from: private */
    public class ExtensionImpl<T> implements ExtensionController.Extension<T> {
        private final ArrayList<Consumer<T>> mCallbacks;
        private T mItem;
        private Context mPluginContext;
        private final ArrayList<Item<T>> mProducers;

        private ExtensionImpl() {
            this.mProducers = new ArrayList<>();
            this.mCallbacks = new ArrayList<>();
        }

        @Override // com.android.systemui.statusbar.policy.ExtensionController.Extension
        public void addCallback(Consumer<T> consumer) {
            this.mCallbacks.add(consumer);
        }

        @Override // com.android.systemui.statusbar.policy.ExtensionController.Extension
        public T get() {
            return this.mItem;
        }

        @Override // com.android.systemui.statusbar.policy.ExtensionController.Extension
        public Context getContext() {
            Context context = this.mPluginContext;
            return context != null ? context : ExtensionControllerImpl.this.mDefaultContext;
        }

        @Override // com.android.systemui.statusbar.policy.ExtensionController.Extension
        public void destroy() {
            for (int i = 0; i < this.mProducers.size(); i++) {
                this.mProducers.get(i).destroy();
            }
        }

        @Override // com.android.systemui.statusbar.policy.ExtensionController.Extension
        public void clearItem(boolean z) {
            if (z && this.mItem != null) {
                ExtensionControllerImpl.this.mLeakDetector.trackGarbage(this.mItem);
            }
            this.mItem = null;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void notifyChanged() {
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

        /* JADX WARN: Unknown type variable: T in type: com.android.systemui.statusbar.policy.ExtensionController$PluginConverter<T, P> */
        /* JADX WARN: Unknown type variable: T in type: T */
        /* access modifiers changed from: private */
        public class PluginItem<P extends Plugin> implements Item<T>, PluginListener<P> {
            private final ExtensionController.PluginConverter<T, P> mConverter;
            private T mItem;

            @Override // com.android.systemui.statusbar.policy.ExtensionControllerImpl.Item
            public int sortOrder() {
                return 0;
            }

            public PluginItem(String str, Class<P> cls, ExtensionController.PluginConverter<T, P> pluginConverter) {
                this.mConverter = pluginConverter;
                ExtensionControllerImpl.this.mPluginManager.addPluginListener(str, (PluginListener) this, (Class<?>) cls);
            }

            /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: P extends com.android.systemui.plugins.Plugin */
            /* JADX WARN: Multi-variable type inference failed */
            @Override // com.android.systemui.plugins.PluginListener
            public void onPluginConnected(P p, Context context) {
                ExtensionImpl.this.mPluginContext = context;
                ExtensionController.PluginConverter<T, P> pluginConverter = this.mConverter;
                if (pluginConverter != null) {
                    this.mItem = pluginConverter.getInterfaceFromPlugin(p);
                } else {
                    this.mItem = p;
                }
                ExtensionImpl.this.notifyChanged();
            }

            @Override // com.android.systemui.plugins.PluginListener
            public void onPluginDisconnected(P p) {
                ExtensionImpl.this.mPluginContext = null;
                this.mItem = null;
                ExtensionImpl.this.notifyChanged();
            }

            @Override // com.android.systemui.statusbar.policy.ExtensionControllerImpl.Producer
            public T get() {
                return this.mItem;
            }

            @Override // com.android.systemui.statusbar.policy.ExtensionControllerImpl.Producer
            public void destroy() {
                ExtensionControllerImpl.this.mPluginManager.removePluginListener(this);
            }
        }

        /* access modifiers changed from: private */
        public class TunerItem<T> implements Item<T>, TunerService.Tunable {
            private final ExtensionController.TunerFactory<T> mFactory;
            private T mItem;
            private final ArrayMap<String, String> mSettings = new ArrayMap<>();

            @Override // com.android.systemui.statusbar.policy.ExtensionControllerImpl.Item
            public int sortOrder() {
                return 1;
            }

            public TunerItem(ExtensionController.TunerFactory<T> tunerFactory, String... strArr) {
                this.mFactory = tunerFactory;
                ExtensionControllerImpl.this.mTunerService.addTunable(this, strArr);
            }

            @Override // com.android.systemui.statusbar.policy.ExtensionControllerImpl.Producer
            public T get() {
                return this.mItem;
            }

            @Override // com.android.systemui.statusbar.policy.ExtensionControllerImpl.Producer
            public void destroy() {
                ExtensionControllerImpl.this.mTunerService.removeTunable(this);
            }

            @Override // com.android.systemui.tuner.TunerService.Tunable
            public void onTuningChanged(String str, String str2) {
                this.mSettings.put(str, str2);
                this.mItem = this.mFactory.create(this.mSettings);
                ExtensionImpl.this.notifyChanged();
            }
        }

        /* access modifiers changed from: private */
        public class Default<T> implements Item<T> {
            private final Supplier<T> mSupplier;

            @Override // com.android.systemui.statusbar.policy.ExtensionControllerImpl.Producer
            public void destroy() {
            }

            @Override // com.android.systemui.statusbar.policy.ExtensionControllerImpl.Item
            public int sortOrder() {
                return 4;
            }

            public Default(ExtensionImpl extensionImpl, Supplier<T> supplier) {
                this.mSupplier = supplier;
            }

            @Override // com.android.systemui.statusbar.policy.ExtensionControllerImpl.Producer
            public T get() {
                return this.mSupplier.get();
            }
        }
    }
}
