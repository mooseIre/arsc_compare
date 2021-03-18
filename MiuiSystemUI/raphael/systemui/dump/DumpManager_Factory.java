package com.android.systemui.dump;

import dagger.internal.Factory;

public final class DumpManager_Factory implements Factory<DumpManager> {
    private static final DumpManager_Factory INSTANCE = new DumpManager_Factory();

    @Override // javax.inject.Provider
    public DumpManager get() {
        return provideInstance();
    }

    public static DumpManager provideInstance() {
        return new DumpManager();
    }

    public static DumpManager_Factory create() {
        return INSTANCE;
    }
}
