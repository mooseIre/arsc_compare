package com.android.systemui.statusbar.policy;

import android.content.Context;
import com.android.systemui.shared.plugins.PluginManager;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.util.leak.LeakDetector;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ExtensionControllerImpl_Factory implements Factory<ExtensionControllerImpl> {
    private final Provider<ConfigurationController> configurationControllerProvider;
    private final Provider<Context> contextProvider;
    private final Provider<LeakDetector> leakDetectorProvider;
    private final Provider<PluginManager> pluginManagerProvider;
    private final Provider<TunerService> tunerServiceProvider;

    public ExtensionControllerImpl_Factory(Provider<Context> provider, Provider<LeakDetector> provider2, Provider<PluginManager> provider3, Provider<TunerService> provider4, Provider<ConfigurationController> provider5) {
        this.contextProvider = provider;
        this.leakDetectorProvider = provider2;
        this.pluginManagerProvider = provider3;
        this.tunerServiceProvider = provider4;
        this.configurationControllerProvider = provider5;
    }

    @Override // javax.inject.Provider
    public ExtensionControllerImpl get() {
        return provideInstance(this.contextProvider, this.leakDetectorProvider, this.pluginManagerProvider, this.tunerServiceProvider, this.configurationControllerProvider);
    }

    public static ExtensionControllerImpl provideInstance(Provider<Context> provider, Provider<LeakDetector> provider2, Provider<PluginManager> provider3, Provider<TunerService> provider4, Provider<ConfigurationController> provider5) {
        return new ExtensionControllerImpl(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get());
    }

    public static ExtensionControllerImpl_Factory create(Provider<Context> provider, Provider<LeakDetector> provider2, Provider<PluginManager> provider3, Provider<TunerService> provider4, Provider<ConfigurationController> provider5) {
        return new ExtensionControllerImpl_Factory(provider, provider2, provider3, provider4, provider5);
    }
}
