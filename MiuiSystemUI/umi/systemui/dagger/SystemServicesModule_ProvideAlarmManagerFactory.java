package com.android.systemui.dagger;

import android.app.AlarmManager;
import android.content.Context;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class SystemServicesModule_ProvideAlarmManagerFactory implements Factory<AlarmManager> {
    private final Provider<Context> contextProvider;

    public SystemServicesModule_ProvideAlarmManagerFactory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public AlarmManager get() {
        return provideInstance(this.contextProvider);
    }

    public static AlarmManager provideInstance(Provider<Context> provider) {
        return proxyProvideAlarmManager(provider.get());
    }

    public static SystemServicesModule_ProvideAlarmManagerFactory create(Provider<Context> provider) {
        return new SystemServicesModule_ProvideAlarmManagerFactory(provider);
    }

    public static AlarmManager proxyProvideAlarmManager(Context context) {
        AlarmManager provideAlarmManager = SystemServicesModule.provideAlarmManager(context);
        Preconditions.checkNotNull(provideAlarmManager, "Cannot return null from a non-@Nullable @Provides method");
        return provideAlarmManager;
    }
}
