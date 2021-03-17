package com.android.systemui;

import com.android.systemui.dump.DumpManager;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class BootCompleteCacheImpl_Factory implements Factory<BootCompleteCacheImpl> {
    private final Provider<DumpManager> dumpManagerProvider;

    public BootCompleteCacheImpl_Factory(Provider<DumpManager> provider) {
        this.dumpManagerProvider = provider;
    }

    @Override // javax.inject.Provider
    public BootCompleteCacheImpl get() {
        return provideInstance(this.dumpManagerProvider);
    }

    public static BootCompleteCacheImpl provideInstance(Provider<DumpManager> provider) {
        return new BootCompleteCacheImpl(provider.get());
    }

    public static BootCompleteCacheImpl_Factory create(Provider<DumpManager> provider) {
        return new BootCompleteCacheImpl_Factory(provider);
    }
}
