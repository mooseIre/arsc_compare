package com.android.systemui.media;

import android.content.Context;
import android.media.MediaRouter2Manager;
import com.android.systemui.dump.DumpManager;
import dagger.internal.Factory;
import java.util.concurrent.Executor;
import javax.inject.Provider;

public final class MediaDeviceManager_Factory implements Factory<MediaDeviceManager> {
    private final Provider<Context> contextProvider;
    private final Provider<DumpManager> dumpManagerProvider;
    private final Provider<Executor> fgExecutorProvider;
    private final Provider<LocalMediaManagerFactory> localMediaManagerFactoryProvider;
    private final Provider<MediaDataManager> mediaDataManagerProvider;
    private final Provider<MediaRouter2Manager> mr2managerProvider;

    public MediaDeviceManager_Factory(Provider<Context> provider, Provider<LocalMediaManagerFactory> provider2, Provider<MediaRouter2Manager> provider3, Provider<Executor> provider4, Provider<MediaDataManager> provider5, Provider<DumpManager> provider6) {
        this.contextProvider = provider;
        this.localMediaManagerFactoryProvider = provider2;
        this.mr2managerProvider = provider3;
        this.fgExecutorProvider = provider4;
        this.mediaDataManagerProvider = provider5;
        this.dumpManagerProvider = provider6;
    }

    @Override // javax.inject.Provider
    public MediaDeviceManager get() {
        return provideInstance(this.contextProvider, this.localMediaManagerFactoryProvider, this.mr2managerProvider, this.fgExecutorProvider, this.mediaDataManagerProvider, this.dumpManagerProvider);
    }

    public static MediaDeviceManager provideInstance(Provider<Context> provider, Provider<LocalMediaManagerFactory> provider2, Provider<MediaRouter2Manager> provider3, Provider<Executor> provider4, Provider<MediaDataManager> provider5, Provider<DumpManager> provider6) {
        return new MediaDeviceManager(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get());
    }

    public static MediaDeviceManager_Factory create(Provider<Context> provider, Provider<LocalMediaManagerFactory> provider2, Provider<MediaRouter2Manager> provider3, Provider<Executor> provider4, Provider<MediaDataManager> provider5, Provider<DumpManager> provider6) {
        return new MediaDeviceManager_Factory(provider, provider2, provider3, provider4, provider5, provider6);
    }
}
