package com.android.systemui.statusbar.notification.collection;

import com.android.internal.statusbar.IStatusBarService;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.dump.LogBufferEulogizer;
import com.android.systemui.statusbar.FeatureFlags;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionLogger;
import com.android.systemui.util.time.SystemClock;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NotifCollection_Factory implements Factory<NotifCollection> {
    private final Provider<SystemClock> clockProvider;
    private final Provider<DumpManager> dumpManagerProvider;
    private final Provider<FeatureFlags> featureFlagsProvider;
    private final Provider<LogBufferEulogizer> logBufferEulogizerProvider;
    private final Provider<NotifCollectionLogger> loggerProvider;
    private final Provider<IStatusBarService> statusBarServiceProvider;

    public NotifCollection_Factory(Provider<IStatusBarService> provider, Provider<SystemClock> provider2, Provider<FeatureFlags> provider3, Provider<NotifCollectionLogger> provider4, Provider<LogBufferEulogizer> provider5, Provider<DumpManager> provider6) {
        this.statusBarServiceProvider = provider;
        this.clockProvider = provider2;
        this.featureFlagsProvider = provider3;
        this.loggerProvider = provider4;
        this.logBufferEulogizerProvider = provider5;
        this.dumpManagerProvider = provider6;
    }

    @Override // javax.inject.Provider
    public NotifCollection get() {
        return provideInstance(this.statusBarServiceProvider, this.clockProvider, this.featureFlagsProvider, this.loggerProvider, this.logBufferEulogizerProvider, this.dumpManagerProvider);
    }

    public static NotifCollection provideInstance(Provider<IStatusBarService> provider, Provider<SystemClock> provider2, Provider<FeatureFlags> provider3, Provider<NotifCollectionLogger> provider4, Provider<LogBufferEulogizer> provider5, Provider<DumpManager> provider6) {
        return new NotifCollection(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get());
    }

    public static NotifCollection_Factory create(Provider<IStatusBarService> provider, Provider<SystemClock> provider2, Provider<FeatureFlags> provider3, Provider<NotifCollectionLogger> provider4, Provider<LogBufferEulogizer> provider5, Provider<DumpManager> provider6) {
        return new NotifCollection_Factory(provider, provider2, provider3, provider4, provider5, provider6);
    }
}
