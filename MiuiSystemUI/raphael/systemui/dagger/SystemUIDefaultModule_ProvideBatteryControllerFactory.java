package com.android.systemui.dagger;

import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.power.EnhancedEstimates;
import com.android.systemui.statusbar.policy.BatteryController;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class SystemUIDefaultModule_ProvideBatteryControllerFactory implements Factory<BatteryController> {
    private final Provider<Handler> bgHandlerProvider;
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<Context> contextProvider;
    private final Provider<EnhancedEstimates> enhancedEstimatesProvider;
    private final Provider<Handler> mainHandlerProvider;
    private final Provider<PowerManager> powerManagerProvider;

    public SystemUIDefaultModule_ProvideBatteryControllerFactory(Provider<Context> provider, Provider<EnhancedEstimates> provider2, Provider<PowerManager> provider3, Provider<BroadcastDispatcher> provider4, Provider<Handler> provider5, Provider<Handler> provider6) {
        this.contextProvider = provider;
        this.enhancedEstimatesProvider = provider2;
        this.powerManagerProvider = provider3;
        this.broadcastDispatcherProvider = provider4;
        this.mainHandlerProvider = provider5;
        this.bgHandlerProvider = provider6;
    }

    @Override // javax.inject.Provider
    public BatteryController get() {
        return provideInstance(this.contextProvider, this.enhancedEstimatesProvider, this.powerManagerProvider, this.broadcastDispatcherProvider, this.mainHandlerProvider, this.bgHandlerProvider);
    }

    public static BatteryController provideInstance(Provider<Context> provider, Provider<EnhancedEstimates> provider2, Provider<PowerManager> provider3, Provider<BroadcastDispatcher> provider4, Provider<Handler> provider5, Provider<Handler> provider6) {
        return proxyProvideBatteryController(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get());
    }

    public static SystemUIDefaultModule_ProvideBatteryControllerFactory create(Provider<Context> provider, Provider<EnhancedEstimates> provider2, Provider<PowerManager> provider3, Provider<BroadcastDispatcher> provider4, Provider<Handler> provider5, Provider<Handler> provider6) {
        return new SystemUIDefaultModule_ProvideBatteryControllerFactory(provider, provider2, provider3, provider4, provider5, provider6);
    }

    public static BatteryController proxyProvideBatteryController(Context context, EnhancedEstimates enhancedEstimates, PowerManager powerManager, BroadcastDispatcher broadcastDispatcher, Handler handler, Handler handler2) {
        BatteryController provideBatteryController = SystemUIDefaultModule.provideBatteryController(context, enhancedEstimates, powerManager, broadcastDispatcher, handler, handler2);
        Preconditions.checkNotNull(provideBatteryController, "Cannot return null from a non-@Nullable @Provides method");
        return provideBatteryController;
    }
}
