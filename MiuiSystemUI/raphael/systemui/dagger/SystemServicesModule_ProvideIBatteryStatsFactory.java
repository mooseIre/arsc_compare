package com.android.systemui.dagger;

import com.android.internal.app.IBatteryStats;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

public final class SystemServicesModule_ProvideIBatteryStatsFactory implements Factory<IBatteryStats> {
    private static final SystemServicesModule_ProvideIBatteryStatsFactory INSTANCE = new SystemServicesModule_ProvideIBatteryStatsFactory();

    @Override // javax.inject.Provider
    public IBatteryStats get() {
        return provideInstance();
    }

    public static IBatteryStats provideInstance() {
        return proxyProvideIBatteryStats();
    }

    public static SystemServicesModule_ProvideIBatteryStatsFactory create() {
        return INSTANCE;
    }

    public static IBatteryStats proxyProvideIBatteryStats() {
        IBatteryStats provideIBatteryStats = SystemServicesModule.provideIBatteryStats();
        Preconditions.checkNotNull(provideIBatteryStats, "Cannot return null from a non-@Nullable @Provides method");
        return provideIBatteryStats;
    }
}
